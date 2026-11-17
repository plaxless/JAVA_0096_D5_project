import java.util.ArrayList;
import java.util.List;

/** 동선 최적화 결과로 만들어진 전체 여행 일정. */
public class Itinerary {
    private List<DayPlan> days = new ArrayList<>();
    private List<Place> unscheduled = new ArrayList<>(); // 시간이 모자라 일정에 못 넣은 장소
    private String sourceName;                           // 이동 시간을 계산한 방법 (Google / 오프라인 추정)

    public Itinerary(String sourceName) {
        this.sourceName = sourceName;
    }

    public void addDay(DayPlan day) { days.add(day); }
    public void addUnscheduled(Place p) { unscheduled.add(p); }

    public List<DayPlan> getDays() { return days; }
    public List<Place> getUnscheduled() { return unscheduled; }
    public String getSourceName() { return sourceName; }

    /** 일정을 보기 좋게 콘솔에 출력한다. */
    public void print(Place lodging) {
        System.out.println("(이동 시간 출처: " + sourceName + ")");
        for (DayPlan day : days) {
            System.out.println();
            System.out.println("━━━ " + day.getDayNumber() + "일차 (" + day.getDate() + ") ━━━");
            if (day.getVisits().isEmpty()) {
                System.out.println("  일정 없음 (자유 시간)");
                continue;
            }
            if (lodging != null) {
                System.out.println("  출발: " + lodging.getName() + " (숙소)");
            }
            for (Visit v : day.getVisits()) {
                if (v.getTravelMinutes() > 0) {
                    System.out.println("    ↓ 이동 약 " + v.getTravelMinutes() + "분");
                }
                System.out.printf("  %s ~ %s  %s [%s]%n",
                        Visit.formatTime(v.getArrivalMin()),
                        Visit.formatTime(v.getDepartureMin()),
                        v.getPlace().getName(), v.getPlace().getCategory());
            }
            if (lodging != null) {
                System.out.println("    ↓ 이동 약 " + day.getReturnTravelMinutes() + "분");
                int last = day.getVisits().get(day.getVisits().size() - 1).getDepartureMin();
                System.out.println("  " + Visit.formatTime(last + day.getReturnTravelMinutes())
                        + "  " + lodging.getName() + " 도착");
            }
            System.out.println("  (이날 총 이동 시간: " + day.getTotalTravelMinutes() + "분)");
        }
        if (!unscheduled.isEmpty()) {
            System.out.println();
            System.out.println("※ 시간이 부족해 일정에 넣지 못한 장소:");
            for (Place p : unscheduled) {
                System.out.println("  - " + p.getName());
            }
        }
    }
}
