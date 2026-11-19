import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayPlan {
    private int dayNumber;
    private LocalDate date;
    private List<Visit> visits = new ArrayList<>();
    private int returnTravelMinutes;

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

    public int getTotalTravelMinutes() {
        int sum = returnTravelMinutes;
        for (Visit v : visits) {
            sum += v.getTravelMinutes();
        }
        return sum;
    }
}
