/**
 * 일정 안에서 장소 한 곳을 방문하는 정보 (도착/출발 시각, 직전 장소에서의 이동 시간).
 * 시각은 "자정부터 몇 분"인지 정수로 저장한다. (예: 9시 30분 = 570)
 */
public class Visit {
    private Place place;
    private int travelMinutes;   // 직전 장소(또는 숙소)에서 오는 데 걸린 시간
    private int arrivalMin;      // 도착 시각
    private int departureMin;    // 출발 시각

    public Visit(Place place, int travelMinutes, int arrivalMin, int departureMin) {
        this.place = place;
        this.travelMinutes = travelMinutes;
        this.arrivalMin = arrivalMin;
        this.departureMin = departureMin;
    }

    public Place getPlace() { return place; }
    public int getTravelMinutes() { return travelMinutes; }
    public int getArrivalMin() { return arrivalMin; }
    public int getDepartureMin() { return departureMin; }

    /** 분 단위 시각을 "HH:mm" 문자열로 바꾼다. */
    public static String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
