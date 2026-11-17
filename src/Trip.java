import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 여행 하나 (여행 기본 정보 + 가고 싶은 장소 목록 + 만들어진 일정). */
public class Trip {
    private int id;
    private String title;          // 여행 이름 (예: 부산 2박 3일)
    private String destination;    // 도시/지역 (예: 부산)
    private LocalDate startDate;
    private int days;              // 여행 일수
    private int dayStartMin;       // 하루 일정 시작 시각 (분)
    private int dayEndMin;         // 하루 일정 종료 시각 (분)
    private Place lodging;         // 숙소 (없으면 null)
    private List<Place> places = new ArrayList<>();
    private Itinerary itinerary;   // 아직 만들지 않았으면 null

    public Trip(int id, String title, String destination, LocalDate startDate,
                int days, int dayStartMin, int dayEndMin) {
        this.id = id;
        this.title = title;
        this.destination = destination;
        this.startDate = startDate;
        setDays(days);
        this.dayStartMin = dayStartMin;
        this.dayEndMin = dayEndMin;
    }

    // ----- 장소 관리 -----
    public void addPlace(Place p) {
        places.add(p);
        itinerary = null;  // 장소가 바뀌면 기존 일정은 다시 만들어야 한다
    }

    public Place removePlace(int index) {
        itinerary = null;
        return places.remove(index);
    }

    public List<Place> getPlaces() { return places; }

    /** 장소 정보(체류 시간 등)가 바뀌었을 때 호출: 기존 일정 무효화 */
    public void invalidateItinerary() { itinerary = null; }

    public LocalDate getEndDate() { return startDate.plusDays(days - 1); }

    // ----- getter / setter -----
    public int getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        itinerary = null;
    }

    public int getDays() { return days; }
    public void setDays(int days) {
        this.days = Math.max(1, days);
        itinerary = null;
    }

    public int getDayStartMin() { return dayStartMin; }
    public int getDayEndMin() { return dayEndMin; }
    public void setDayTime(int startMin, int endMin) {
        this.dayStartMin = startMin;
        this.dayEndMin = endMin;
        itinerary = null;
    }

    public Place getLodging() { return lodging; }
    public void setLodging(Place lodging) {
        this.lodging = lodging;
        itinerary = null;
    }

    public Itinerary getItinerary() { return itinerary; }
    public void setItinerary(Itinerary itinerary) { this.itinerary = itinerary; }

    @Override
    public String toString() {
        return String.format("#%d %s (%s) %s ~ %s, %d일, 장소 %d곳%s",
                id, title, destination, startDate, getEndDate(), days, places.size(),
                itinerary == null ? "" : ", 일정 생성됨");
    }
}
