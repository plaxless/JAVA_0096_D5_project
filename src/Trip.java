import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Trip {
    private int id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private int days;
    private int dayStartMin;
    private int dayEndMin;
    private Place lodging;
    private List<Place> places = new ArrayList<>();
    private Itinerary itinerary;

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

    public void addPlace(Place p) {
        places.add(p);
        itinerary = null;
    }

    public Place removePlace(int index) {
        itinerary = null;
        return places.remove(index);
    }

    public List<Place> getPlaces() { return places; }

    public void invalidateItinerary() { itinerary = null; }

    public LocalDate getEndDate() { return startDate.plusDays(days - 1); }

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
