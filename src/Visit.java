public class Visit {
    private Place place;
    private int travelMinutes;
    private int arrivalMin;
    private int departureMin;

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

    public static String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
