import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Maps Platform을 사용하는 지도 서비스.
 * - 장소 검색: Geocoding API
 * - 이동 시간: Routes API 의 computeRouteMatrix (대중교통 TRANSIT 기준)
 *
 * 한국에서는 Google 지도의 자동차/도보 길찾기가 지원되지 않아 대중교통 기준으로 계산하고,
 * 경로가 없는 구간은 직선거리 추정값으로 대신한다.
 * 외부 라이브러리 없이 JDK 기본 HttpClient와 정규식으로 JSON 응답에서 필요한 값만 꺼낸다.
 */
public class GoogleMapsService implements MapsService {
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String MATRIX_URL = "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix";

    private final String apiKey;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public GoogleMapsService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getName() {
        return "Google Maps (대중교통)";
    }

    // ===================== 장소 검색 (Geocoding API) =====================

    @Override
    public Place searchPlace(String query) throws IOException {
        String url = GEOCODE_URL
                + "?address=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&language=ko&region=kr"
                + "&key=" + apiKey;
        String body = send(HttpRequest.newBuilder(URI.create(url)).GET());

        String status = find(body, "\"status\"\\s*:\\s*\"(\\w+)\"");
        if ("ZERO_RESULTS".equals(status)) {
            return null;
        }
        if (!"OK".equals(status)) {
            String msg = find(body, "\"error_message\"\\s*:\\s*\"([^\"]*)\"");
            throw new IOException("Geocoding 오류: " + status + (msg == null ? "" : " - " + msg));
        }
        String address = find(body, "\"formatted_address\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = Pattern.compile(
                "\"location\"\\s*:\\s*\\{\\s*\"lat\"\\s*:\\s*([-\\d.]+)\\s*,\\s*\"lng\"\\s*:\\s*([-\\d.]+)")
                .matcher(body);
        if (!m.find()) {
            return null;
        }
        double lat = Double.parseDouble(m.group(1));
        double lng = Double.parseDouble(m.group(2));
        // Geocoding 결과에는 장소 "이름"이 없으므로 사용자가 입력한 검색어를 이름으로 쓴다
        return new Place(query, address == null ? "" : address, lat, lng, 60, "기타", "");
    }

    // ===================== 이동 시간 표 (Routes API) =====================

    @Override
    public int[][] travelTimeMatrix(List<Place> points) throws IOException {
        int n = points.size();
        int[][] m = new int[n][n];
        int fallbackCount = 0;

        // 대중교통 행렬은 한 번에 100칸까지만 요청할 수 있어서, 출발지 1곳씩 나눠서 요청한다
        for (int i = 0; i < n; i++) {
            String requestJson = "{\"origins\":[" + waypointJson(points.get(i)) + "],"
                    + "\"destinations\":[" + waypointsJson(points) + "],"
                    + "\"travelMode\":\"TRANSIT\"}";
            HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(MATRIX_URL))
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "originIndex,destinationIndex,duration,condition")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson));
            String body = send(req);

            boolean[] filled = new boolean[n];
            for (String element : splitObjects(body)) {
                // 값이 0인 필드는 응답에서 생략되므로 없으면 0번으로 본다
                String dest = find(element, "\"destinationIndex\"\\s*:\\s*(\\d+)");
                int j = dest == null ? 0 : Integer.parseInt(dest);
                String seconds = find(element, "\"duration\"\\s*:\\s*\"(\\d+)s\"");
                if (j < n && element.contains("ROUTE_EXISTS") && seconds != null) {
                    m[i][j] = (int) Math.ceil(Integer.parseInt(seconds) / 60.0);
                    filled[j] = true;
                }
            }
            // 경로를 못 찾은 구간은 직선거리로 추정
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    m[i][j] = 0;
                } else if (!filled[j]) {
                    m[i][j] = MapsService.estimateMinutes(points.get(i), points.get(j));
                    fallbackCount++;
                }
            }
        }
        if (fallbackCount > 0) {
            System.out.println("  (참고: " + fallbackCount + "개 구간은 대중교통 경로가 없어 직선거리로 추정했습니다)");
        }
        return m;
    }

    // ===================== 내부 도우미 메서드 =====================

    /** HTTP 요청을 보내고 응답 본문을 돌려준다. 실패하면 IOException. */
    private String send(HttpRequest.Builder builder) throws IOException {
        try {
            HttpResponse<String> res = http.send(
                    builder.timeout(Duration.ofSeconds(20)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() != 200) {
                String msg = find(res.body(), "\"message\"\\s*:\\s*\"([^\"]*)\"");
                throw new IOException("HTTP " + res.statusCode() + (msg == null ? "" : " - " + msg));
            }
            return res.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("요청이 중단되었습니다", e);
        }
    }

    private static String waypointJson(Place p) {
        return String.format(Locale.ROOT,
                "{\"waypoint\":{\"location\":{\"latLng\":{\"latitude\":%.6f,\"longitude\":%.6f}}}}",
                p.getLat(), p.getLng());
    }

    private static String waypointsJson(List<Place> points) {
        List<String> parts = new ArrayList<>();
        for (Place p : points) {
            parts.add(waypointJson(p));
        }
        return String.join(",", parts);
    }

    /** 정규식의 첫 번째 그룹을 찾아 돌려준다. 없으면 null. */
    private static String find(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    /** JSON 배열 "[{...},{...}]" 을 최상위 객체 문자열 단위로 나눈다. */
    private static List<String> splitObjects(String jsonArray) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            if (inString) {
                if (c == '\\') i++;            // 이스케이프 문자 건너뛰기
                else if (c == '"') inString = false;
            } else if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    result.add(jsonArray.substring(start, i + 1));
                }
            }
        }
        return result;
    }
}
