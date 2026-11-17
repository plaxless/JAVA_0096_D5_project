import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 여행 목록을 관리하는 클래스 (추가/조회/수정/삭제/검색).
 * - Map: 여행 번호(id) → 여행, 번호로 바로 찾기 위해 사용 (입력 순서 유지를 위해 LinkedHashMap)
 * - Set: 중복 없는 카테고리 목록 (가나다순 정렬을 위해 TreeSet)
 * - List: 검색 결과 목록
 */
public class TripManager {
    private Map<Integer, Trip> trips = new LinkedHashMap<>();
    private int nextId = 1;

    // ----- Create -----
    public Trip createTrip(String title, String destination, LocalDate startDate,
                           int days, int dayStartMin, int dayEndMin) {
        Trip trip = new Trip(nextId++, title, destination, startDate, days, dayStartMin, dayEndMin);
        trips.put(trip.getId(), trip);
        return trip;
    }

    /** 파일에서 불러온 여행을 그대로 등록 */
    public void addLoadedTrip(Trip trip) {
        trips.put(trip.getId(), trip);
        nextId = Math.max(nextId, trip.getId() + 1);
    }

    // ----- Read -----
    public Trip getTrip(int id) { return trips.get(id); }
    public Collection<Trip> getAllTrips() { return trips.values(); }
    public boolean isEmpty() { return trips.isEmpty(); }

    // ----- Delete -----
    public boolean deleteTrip(int id) {
        return trips.remove(id) != null;
    }

    // (Update 는 Trip 의 setter 로 처리한다)

    // ----- 검색 -----

    /** 여행 이름, 지역, 장소 이름에 키워드가 들어간 여행 찾기 */
    public List<Trip> searchByKeyword(String keyword) {
        List<Trip> result = new ArrayList<>();
        for (Trip t : trips.values()) {
            boolean match = t.getTitle().contains(keyword) || t.getDestination().contains(keyword);
            for (Place p : t.getPlaces()) {
                if (p.getName().contains(keyword)) {
                    match = true;
                }
            }
            if (match) result.add(t);
        }
        return result;
    }

    /** 특정 날짜가 여행 기간에 들어 있는 여행 찾기 */
    public List<Trip> searchByDate(LocalDate date) {
        List<Trip> result = new ArrayList<>();
        for (Trip t : trips.values()) {
            if (!date.isBefore(t.getStartDate()) && !date.isAfter(t.getEndDate())) {
                result.add(t);
            }
        }
        return result;
    }

    /** 카테고리가 일치하는 장소를 "여행이름 - 장소" 형식으로 모으기 */
    public List<String> searchPlacesByCategory(String category) {
        List<String> result = new ArrayList<>();
        for (Trip t : trips.values()) {
            for (Place p : t.getPlaces()) {
                if (p.getCategory().equals(category)) {
                    result.add("[" + t.getTitle() + "] " + p);
                }
            }
        }
        return result;
    }

    /** 등록된 모든 장소의 카테고리 (중복 제거, 정렬) */
    public Set<String> getAllCategories() {
        Set<String> categories = new TreeSet<>();
        for (Trip t : trips.values()) {
            for (Place p : t.getPlaces()) {
                categories.add(p.getCategory());
            }
        }
        return categories;
    }
}
