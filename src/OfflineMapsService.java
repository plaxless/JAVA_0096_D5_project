import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 오프라인 지도 서비스 (외부 API 없이 동작).
 * - 장소 검색: 미리 넣어 둔 대표 관광지 좌표에서 찾는다.
 * - 이동 시간: 직선거리로 추정한다.
 */
public class OfflineMapsService implements MapsService {
    // 장소 이름 → {위도, 경도} (대략적인 좌표)
    private Map<String, double[]> knownPlaces = new HashMap<>();
    private Map<String, String> knownAddresses = new HashMap<>();

    public OfflineMapsService() {
        // 서울
        add("경복궁", "서울 종로구 사직로 161", 37.5796, 126.9770);
        add("북촌한옥마을", "서울 종로구 계동길 37", 37.5826, 126.9831);
        add("인사동", "서울 종로구 인사동길", 37.5740, 126.9850);
        add("명동", "서울 중구 명동길", 37.5636, 126.9850);
        add("남산서울타워", "서울 용산구 남산공원길 105", 37.5512, 126.9882);
        add("동대문디자인플라자", "서울 중구 을지로 281", 37.5665, 127.0092);
        add("홍대", "서울 마포구 홍익로", 37.5563, 126.9236);
        add("여의도한강공원", "서울 영등포구 여의동로 330", 37.5284, 126.9326);
        add("롯데월드타워", "서울 송파구 올림픽로 300", 37.5126, 127.1025);
        add("코엑스", "서울 강남구 영동대로 513", 37.5116, 127.0595);
        add("서울역", "서울 용산구 한강대로 405", 37.5547, 126.9707);
        add("건국대학교", "서울 광진구 능동로 120", 37.5408, 127.0794);
        // 부산
        add("해운대해수욕장", "부산 해운대구 우동", 35.1587, 129.1604);
        add("광안리해수욕장", "부산 수영구 광안해변로", 35.1532, 129.1186);
        add("감천문화마을", "부산 사하구 감내2로 203", 35.0975, 129.0106);
        add("자갈치시장", "부산 중구 자갈치해안로 52", 35.0966, 129.0306);
        add("해동용궁사", "부산 기장군 기장읍 용궁길 86", 35.1884, 129.2233);
        add("부산역", "부산 동구 중앙대로 206", 35.1151, 129.0421);
        // 제주
        add("성산일출봉", "제주 서귀포시 성산읍 일출로 284-12", 33.4581, 126.9426);
        add("협재해수욕장", "제주 제주시 한림읍 협재리", 33.3940, 126.2397);
        add("한라산", "제주 서귀포시 토평동", 33.3617, 126.5292);
        add("제주국제공항", "제주 제주시 공항로 2", 33.5104, 126.4914);
    }

    private void add(String name, String address, double lat, double lng) {
        knownPlaces.put(name, new double[]{lat, lng});
        knownAddresses.put(name, address);
    }

    @Override
    public Place searchPlace(String query) {
        String q = query.replace(" ", "");
        for (String name : knownPlaces.keySet()) {
            // 공백을 무시하고, 이름이 서로 포함되면 같은 장소로 본다
            if (name.contains(q) || q.contains(name)) {
                double[] ll = knownPlaces.get(name);
                return new Place(name, knownAddresses.get(name), ll[0], ll[1], 60, "기타", "");
            }
        }
        return null;
    }

    @Override
    public int[][] travelTimeMatrix(List<Place> points) {
        int n = points.size();
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = (i == j) ? 0 : MapsService.estimateMinutes(points.get(i), points.get(j));
            }
        }
        return m;
    }

    @Override
    public String getName() {
        return "오프라인 추정(직선거리)";
    }
}
