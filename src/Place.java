public class Place {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private int stayMinutes;
    private String category;
    private String memo;
    private boolean visited;

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
