import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TripManager {
    private Map<Integer, Trip> trips = new LinkedHashMap<>();
    private int nextId = 1;

    public Trip createTrip(String title, String destination, LocalDate startDate,
                           int days, int dayStartMin, int dayEndMin) {
        Trip trip = new Trip(nextId++, title, destination, startDate, days, dayStartMin, dayEndMin);
        trips.put(trip.getId(), trip);
        return trip;
    }

    public void addLoadedTrip(Trip trip) {
        trips.put(trip.getId(), trip);
        nextId = Math.max(nextId, trip.getId() + 1);
    }

    public Trip getTrip(int id) { return trips.get(id); }
    public Collection<Trip> getAllTrips() { return trips.values(); }
    public boolean isEmpty() { return trips.isEmpty(); }

    public boolean deleteTrip(int id) {
        return trips.remove(id) != null;
    }

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

    public List<Trip> searchByDate(LocalDate date) {
        List<Trip> result = new ArrayList<>();
        for (Trip t : trips.values()) {
            if (!date.isBefore(t.getStartDate()) && !date.isAfter(t.getEndDate())) {
                result.add(t);
            }
        }
        return result;
    }

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
