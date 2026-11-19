import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteOptimizer {
    private final MapsService maps;

    public RouteOptimizer(MapsService maps) {
        this.maps = maps;
    }

    public Itinerary buildItinerary(Trip trip) throws IOException {
        Place lodging = trip.getLodging();
        boolean hasLodging = lodging != null;

        List<Place> points = new ArrayList<>();
        if (hasLodging) points.add(lodging);
        points.addAll(trip.getPlaces());
        int[][] m = maps.travelTimeMatrix(points);

        int lodgingIdx = hasLodging ? 0 : -1;
        List<Integer> remaining = new ArrayList<>();
        for (int i = hasLodging ? 1 : 0; i < points.size(); i++) {
            remaining.add(i);
        }

        Itinerary itinerary = new Itinerary(maps.getName());
        int lastOfPrevDay = -1;

        for (int d = 0; d < trip.getDays(); d++) {
            List<Integer> today = new ArrayList<>();
            int current = hasLodging ? lodgingIdx : lastOfPrevDay;
            int now = trip.getDayStartMin();

            while (!remaining.isEmpty()) {
                boolean startHere = today.isEmpty() && !hasLodging;
                int best = -1;
                int bestDist = Integer.MAX_VALUE;
                for (int j : remaining) {
                    int dist = (current < 0) ? 0 : m[current][j];
                    int travel = startHere ? 0 : dist;
                    int back = hasLodging ? m[j][lodgingIdx] : 0;
                    boolean fits = now + travel + points.get(j).getStayMinutes() + back <= trip.getDayEndMin();
                    if (fits && dist < bestDist) {
                        best = j;
                        bestDist = dist;
                    }
                }
                if (best == -1) {
                    if (!today.isEmpty()) break;
                    best = remaining.get(0);
                }
                now += (startHere || current < 0 ? 0 : m[current][best]) + points.get(best).getStayMinutes();
                today.add(best);
                remaining.remove(Integer.valueOf(best));
                current = best;
            }

            if (hasLodging) {
                today.add(0, lodgingIdx);
                today = twoOpt(today, m, true);
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
