import java.io.IOException;
import java.util.List;

/**
 * 지도 기능(장소 검색, 이동 시간 계산)을 제공하는 인터페이스.
 * Google Maps 버전과 오프라인(직선거리 추정) 버전이 이 인터페이스를 구현한다.
 * → 프로그램의 나머지 부분은 어떤 구현을 쓰는지 몰라도 된다 (다형성).
 */
public interface MapsService {

    /** 이름/주소로 장소를 검색한다. 찾지 못하면 null. */
    Place searchPlace(String query) throws IOException;

    /**
     * 장소들 사이의 이동 시간(분) 표를 만든다.
     * matrix[i][j] = i번 장소에서 j번 장소까지 걸리는 시간
     */
    int[][] travelTimeMatrix(List<Place> points) throws IOException;

    /** 화면에 보여줄 서비스 이름 */
    String getName();

    // ----- 두 구현이 함께 쓰는 직선거리 기반 추정 -----

    /** 두 좌표 사이 직선거리(km) - 하버사인 공식 */
    static double distanceKm(Place a, Place b) {
        double r = 6371.0; // 지구 반지름(km)
        double dLat = Math.toRadians(b.getLat() - a.getLat());
        double dLng = Math.toRadians(b.getLng() - a.getLng());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.getLat())) * Math.cos(Math.toRadians(b.getLat()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    /**
     * 직선거리로 이동 시간을 대충 추정한다.
     * 1km 미만은 도보(시속 4km), 그 이상은 대중교통(실제 경로는 직선의 약 1.3배, 평균 시속 20km + 대기 10분).
     */
    static int estimateMinutes(Place a, Place b) {
        double km = distanceKm(a, b);
        if (km < 0.05) return 0;
        if (km < 1.0) return (int) Math.ceil(km * 1.2 / 4.0 * 60);
        return (int) Math.ceil(km * 1.3 / 20.0 * 60) + 10;
    }
}
