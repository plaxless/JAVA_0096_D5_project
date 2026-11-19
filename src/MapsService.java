import java.io.IOException;
import java.util.List;

public interface MapsService {
    Place searchPlace(String query) throws IOException;

    int[][] travelTimeMatrix(List<Place> points) throws IOException;

    String getName();

    static double distanceKm(Place a, Place b) {
        double r = 6371.0;
        double dLat = Math.toRadians(b.getLat() - a.getLat());
        double dLng = Math.toRadians(b.getLng() - a.getLng());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.getLat())) * Math.cos(Math.toRadians(b.getLat()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    static int estimateMinutes(Place a, Place b) {
        double km = distanceKm(a, b);
        if (km < 0.05) return 0;
        if (km < 1.0) return (int) Math.ceil(km * 1.2 / 4.0 * 60);
        return (int) Math.ceil(km * 1.3 / 20.0 * 60) + 10;
    }
}
