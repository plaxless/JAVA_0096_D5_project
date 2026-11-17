import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 하루치 일정: 방문 순서대로 정렬된 Visit 목록. */
public class DayPlan {
    private int dayNumber;            // 몇 일차인지 (1부터)
    private LocalDate date;
    private List<Visit> visits = new ArrayList<>();
    private int returnTravelMinutes;  // 마지막 장소에서 숙소로 돌아가는 시간 (숙소 없으면 0)

    public DayPlan(int dayNumber, LocalDate date) {
        this.dayNumber = dayNumber;
        this.date = date;
    }

    public void addVisit(Visit visit) { visits.add(visit); }

    public int getDayNumber() { return dayNumber; }
    public LocalDate getDate() { return date; }
    public List<Visit> getVisits() { return visits; }

    public int getReturnTravelMinutes() { return returnTravelMinutes; }
    public void setReturnTravelMinutes(int m) { this.returnTravelMinutes = m; }

    /** 이 날 이동에 쓴 시간 합계 */
    public int getTotalTravelMinutes() {
        int sum = returnTravelMinutes;
        for (Visit v : visits) {
            sum += v.getTravelMinutes();
        }
        return sum;
    }
}
