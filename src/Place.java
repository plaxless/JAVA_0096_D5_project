/**
 * 여행지(방문할 장소) 하나를 나타내는 클래스.
 * 위도/경도는 동선(이동 시간) 계산에 사용된다.
 */
public class Place {
    private String name;        // 장소 이름 (예: 경복궁)
    private String address;     // 주소
    private double lat;         // 위도
    private double lng;         // 경도
    private int stayMinutes;    // 머무를 시간(분)
    private String category;    // 분류 (관광, 맛집, 카페, 쇼핑 ...)
    private String memo;        // 메모
    private boolean visited;    // 방문 완료 여부 (GUI 의 체크박스로 표시)

    public Place(String name, String address, double lat, double lng,
                 int stayMinutes, String category, String memo) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        setStayMinutes(stayMinutes);
        this.category = category;
        this.memo = memo;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLat() { return lat; }
    public double getLng() { return lng; }

    public int getStayMinutes() { return stayMinutes; }
    public void setStayMinutes(int stayMinutes) {
        // 체류 시간은 0분 이상이어야 한다
        this.stayMinutes = Math.max(0, stayMinutes);
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    @Override
    public String toString() {
        return String.format("%s%s [%s] 체류 %d분 - %s%s",
                visited ? "(완료) " : "", name, category, stayMinutes, address,
                memo.isEmpty() ? "" : " (메모: " + memo + ")");
    }
}
