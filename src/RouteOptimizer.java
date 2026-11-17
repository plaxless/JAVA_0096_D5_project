import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 동선 최적화 및 일정 생성.
 *
 * 1) 지도 서비스로 모든 장소 사이의 이동 시간 표(행렬)를 구한다.
 * 2) 하루씩 일정을 채운다. 현재 위치에서 "가장 가까우면서 오늘 안에 다녀올 수 있는" 장소를
 *    차례로 고른다. (최근접 이웃 + 시간 제약)
 * 3) 하루치 방문 순서를 2-opt 로 다듬는다: 순서의 한 구간을 뒤집어 보고
 *    총 이동 시간이 줄면 바꾼다. 더 이상 줄지 않을 때까지 반복.
 * 4) 여행 날짜를 다 써도 남은 장소는 "일정에 못 넣은 장소"로 알려 준다.
 */
public class RouteOptimizer {
    private final MapsService maps;

    public RouteOptimizer(MapsService maps) {
        this.maps = maps;
    }

    public Itinerary buildItinerary(Trip trip) throws IOException {
        Place lodging = trip.getLodging();
        boolean hasLodging = lodging != null;

        // points: 숙소가 있으면 0번이 숙소, 그 뒤가 방문 장소들
        List<Place> points = new ArrayList<>();
        if (hasLodging) points.add(lodging);
        points.addAll(trip.getPlaces());
        int[][] m = maps.travelTimeMatrix(points);

        int lodgingIdx = hasLodging ? 0 : -1;
        List<Integer> remaining = new ArrayList<>();   // 아직 일정에 안 넣은 장소 번호
        for (int i = hasLodging ? 1 : 0; i < points.size(); i++) {
            remaining.add(i);
        }

        Itinerary itinerary = new Itinerary(maps.getName());
        int lastOfPrevDay = -1;  // 숙소가 없을 때, 전날 마지막 장소 (다음 날 시작점 고를 때 사용)

        for (int d = 0; d < trip.getDays(); d++) {
            // ---- 2) 오늘 갈 장소 고르기 ----
            List<Integer> today = new ArrayList<>();
            int current = hasLodging ? lodgingIdx : lastOfPrevDay;
            int now = trip.getDayStartMin();

            while (!remaining.isEmpty()) {
                // 숙소 없이 하루를 시작할 때는 첫 장소에서 바로 시작하므로 이동 시간 0
                boolean startHere = today.isEmpty() && !hasLodging;
                int best = -1;
                int bestDist = Integer.MAX_VALUE;
                for (int j : remaining) {
                    int dist = (current < 0) ? 0 : m[current][j];   // 가까운 곳을 고르는 기준
                    int travel = startHere ? 0 : dist;               // 실제 일정에 들어가는 이동 시간
                    int back = hasLodging ? m[j][lodgingIdx] : 0;
                    boolean fits = now + travel + points.get(j).getStayMinutes() + back <= trip.getDayEndMin();
                    if (fits && dist < bestDist) {
                        best = j;
                        bestDist = dist;
                    }
                }
                if (best == -1) {
                    // 남은 장소가 하나도 안 들어가는데 오늘이 비어 있으면,
                    // (체류 시간이 하루보다 긴 경우) 남은 첫 장소를 그냥 넣는다
                    if (!today.isEmpty()) break;
                    best = remaining.get(0);
                }
                now += (startHere || current < 0 ? 0 : m[current][best]) + points.get(best).getStayMinutes();
                today.add(best);
                remaining.remove(Integer.valueOf(best));
                current = best;
            }

            // ---- 3) 하루 순서 다듬기 (2-opt) ----
            if (hasLodging) {
                today.add(0, lodgingIdx);                 // 숙소에서 출발해서
                today = twoOpt(today, m, true);           // 숙소로 돌아오는 경로로 최적화
                today.remove(0);
            } else {
                today = twoOpt(today, m, false);
            }
            if (!today.isEmpty()) lastOfPrevDay = today.get(today.size() - 1);

            itinerary.addDay(makeDayPlan(trip, d, today, points, m, lodgingIdx));
        }

        for (int j : remaining) {
            itinerary.addUnscheduled(points.get(j));
        }
        return itinerary;
    }

    /** 정해진 방문 순서로 도착/출발 시각을 계산해 DayPlan 을 만든다. */
    private DayPlan makeDayPlan(Trip trip, int dayIndex, List<Integer> order,
                                List<Place> points, int[][] m, int lodgingIdx) {
        DayPlan day = new DayPlan(dayIndex + 1, trip.getStartDate().plusDays(dayIndex));
        int now = trip.getDayStartMin();
        int prev = lodgingIdx;
        for (int idx : order) {
            int travel = (prev < 0) ? 0 : m[prev][idx];
            int arrive = now + travel;
            int depart = arrive + points.get(idx).getStayMinutes();
            day.addVisit(new Visit(points.get(idx), travel, arrive, depart));
            now = depart;
            prev = idx;
        }
        if (lodgingIdx >= 0 && !order.isEmpty()) {
            day.setReturnTravelMinutes(m[prev][lodgingIdx]);
        }
        return day;
    }

    /**
     * 2-opt: path[i..j] 구간을 뒤집었을 때 총 이동 시간이 줄면 채택한다.
     * 맨 앞(출발점)은 고정. roundTrip 이면 마지막에 출발점으로 돌아오는 시간도 더한다.
     */
    private List<Integer> twoOpt(List<Integer> path, int[][] m, boolean roundTrip) {
        List<Integer> best = new ArrayList<>(path);
        int bestCost = pathCost(best, m, roundTrip);
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < best.size() - 1; i++) {
                for (int j = i + 1; j < best.size(); j++) {
                    List<Integer> candidate = new ArrayList<>(best);
                    Collections.reverse(candidate.subList(i, j + 1));
                    int cost = pathCost(candidate, m, roundTrip);
                    if (cost < bestCost) {
                        best = candidate;
                        bestCost = cost;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    /** 경로의 총 이동 시간(분) */
    private int pathCost(List<Integer> path, int[][] m, boolean roundTrip) {
        int cost = 0;
        for (int k = 0; k + 1 < path.size(); k++) {
            cost += m[path.get(k)][path.get(k + 1)];
        }
        if (roundTrip && path.size() > 1) {
            cost += m[path.get(path.size() - 1)][path.get(0)];
        }
        return cost;
    }
}
