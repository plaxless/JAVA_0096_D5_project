import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class TravelPlannerApp {
    private static final String DATA_FILE = "data/trips.txt";

    private final Scanner scanner = new Scanner(System.in);
    private final TripManager manager = new TripManager();
    private final FileStorage storage = new FileStorage(DATA_FILE);
    private final MapsService maps;
    private final RouteOptimizer optimizer;

    public TravelPlannerApp(MapsService maps) {
        this.maps = maps;
        this.optimizer = new RouteOptimizer(maps);
    }

    public static void main(String[] args) {
        new TravelPlannerApp(new OfflineMapsService()).run();
    }

    public void run() {
        try {
            storage.load(manager);
        } catch (IOException e) {
            System.out.println("데이터 불러오기 실패: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));

        System.out.println("========================================");
        System.out.println("   여행 일정 플래너 (지도: " + maps.getName() + ")");
        System.out.println("========================================");
        System.out.println("저장된 여행 " + manager.getAllTrips().size() + "개를 불러왔습니다.");

        while (true) {
            System.out.println();
            System.out.println("[메인 메뉴]");
            System.out.println("1. 새 여행 만들기");
            System.out.println("2. 여행 목록 보기");
            System.out.println("3. 여행 열기 (장소 관리 / 일정 만들기)");
            System.out.println("4. 여행 정보 수정");
            System.out.println("5. 여행 삭제");
            System.out.println("6. 검색");
            System.out.println("0. 종료 (자동 저장)");
            int menu = readInt("선택 >> ", -1);
            switch (menu) {
                case 1: createTrip(); break;
                case 2: listTrips(); break;
                case 3: openTrip(); break;
                case 4: editTrip(); break;
                case 5: deleteTrip(); break;
                case 6: searchMenu(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default: System.out.println("메뉴 번호를 다시 입력하세요.");
            }
        }
    }

    private void save() {
        try {
            storage.save(manager);
            System.out.println("데이터를 " + DATA_FILE + " 에 저장했습니다.");
        } catch (IOException e) {
            System.out.println("저장 실패: " + e.getMessage());
        }
    }

    private void createTrip() {
        String title = readLine("여행 이름 (예: 부산 2박3일) >> ");
        String dest = readLine("여행 지역 (예: 부산) >> ");
        LocalDate start = readDate("출발 날짜 (yyyy-mm-dd) >> ");
        int days = Math.max(1, readInt("여행 일수 >> ", 1));
        int startMin = readTime("하루 일정 시작 시각 (기본 09:00) >> ", 9 * 60);
        int endMin = readTime("하루 일정 종료 시각 (기본 21:00) >> ", 21 * 60);
        Trip t = manager.createTrip(title, dest, start, days, startMin, endMin);
        System.out.println("여행이 만들어졌습니다: " + t);
        System.out.println("→ 3번 메뉴에서 이 여행을 열어 가고 싶은 장소를 추가하세요.");
    }

    private void listTrips() {
        if (manager.isEmpty()) {
            System.out.println("등록된 여행이 없습니다.");
            return;
        }
        for (Trip t : manager.getAllTrips()) {
            System.out.println(t);
        }
    }

    private Trip chooseTrip() {
        if (manager.isEmpty()) {
            System.out.println("등록된 여행이 없습니다.");
            return null;
        }
        listTrips();
        int id = readInt("여행 번호 >> ", -1);
        Trip t = manager.getTrip(id);
        if (t == null) System.out.println("해당 번호의 여행이 없습니다.");
        return t;
    }

    private void editTrip() {
        Trip t = chooseTrip();
        if (t == null) return;
        System.out.println("(바꾸지 않을 항목은 그냥 Enter)");
        String title = readLine("여행 이름 [" + t.getTitle() + "] >> ");
        if (!title.isEmpty()) t.setTitle(title);
        String dest = readLine("여행 지역 [" + t.getDestination() + "] >> ");
        if (!dest.isEmpty()) t.setDestination(dest);
        String date = readLine("출발 날짜 [" + t.getStartDate() + "] >> ");
        if (!date.isEmpty()) {
            try {
                t.setStartDate(LocalDate.parse(date));
            } catch (DateTimeParseException e) {
                System.out.println("날짜 형식이 잘못되어 그대로 둡니다.");
            }
        }
        int days = readInt("여행 일수 [" + t.getDays() + "] >> ", t.getDays());
        if (days != t.getDays()) t.setDays(days);
        int s = readTime("하루 시작 [" + Visit.formatTime(t.getDayStartMin()) + "] >> ", t.getDayStartMin());
        int e = readTime("하루 종료 [" + Visit.formatTime(t.getDayEndMin()) + "] >> ", t.getDayEndMin());
        if (s != t.getDayStartMin() || e != t.getDayEndMin()) t.setDayTime(s, e);
        System.out.println("수정되었습니다: " + t);
    }

    private void deleteTrip() {
        Trip t = chooseTrip();
        if (t == null) return;
        String yn = readLine("'" + t.getTitle() + "' 여행을 삭제할까요? (y/n) >> ");
        if (yn.equalsIgnoreCase("y")) {
            manager.deleteTrip(t.getId());
            System.out.println("삭제했습니다.");
        }
    }

    private void searchMenu() {
        System.out.println("1. 키워드 검색 (여행 이름/지역/장소 이름)");
        System.out.println("2. 날짜로 검색 (그 날짜에 진행 중인 여행)");
        System.out.println("3. 카테고리별 장소 보기");
        int menu = readInt("선택 >> ", -1);
        if (menu == 1) {
            List<Trip> result = manager.searchByKeyword(readLine("키워드 >> "));
            printTrips(result);
        } else if (menu == 2) {
            printTrips(manager.searchByDate(readDate("날짜 (yyyy-mm-dd) >> ")));
        } else if (menu == 3) {
            Set<String> categories = manager.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("등록된 장소가 없습니다.");
                return;
            }
            System.out.println("카테고리: " + categories);
            List<String> result = manager.searchPlacesByCategory(readLine("카테고리 >> "));
            if (result.isEmpty()) System.out.println("결과가 없습니다.");
            for (String s : result) System.out.println(s);
        }
    }

    private void printTrips(List<Trip> list) {
        if (list.isEmpty()) System.out.println("결과가 없습니다.");
        for (Trip t : list) System.out.println(t);
    }

    private void openTrip() {
        Trip trip = chooseTrip();
        if (trip == null) return;
        while (true) {
            System.out.println();
            System.out.println("[" + trip.getTitle() + "] " + trip.getStartDate() + " ~ " + trip.getEndDate()
                    + " / 숙소: " + (trip.getLodging() == null ? "없음" : trip.getLodging().getName()));
            System.out.println("1. 장소 추가 (지도 검색)");
            System.out.println("2. 장소 목록");
            System.out.println("3. 장소 수정");
            System.out.println("4. 장소 삭제");
            System.out.println("5. 숙소 설정");
            System.out.println("6. 동선 최적화 → 일정 만들기");
            System.out.println("7. 일정 보기");
            System.out.println("0. 메인 메뉴로");
            int menu = readInt("선택 >> ", -1);
            switch (menu) {
                case 1: addPlace(trip); break;
                case 2: listPlaces(trip); break;
                case 3: editPlace(trip); break;
                case 4: deletePlace(trip); break;
                case 5: setLodging(trip); break;
                case 6: buildItinerary(trip); break;
                case 7: showItinerary(trip); break;
                case 0: return;
                default: System.out.println("메뉴 번호를 다시 입력하세요.");
            }
        }
    }

    private Place findPlace(String prompt) {
        String query = readLine(prompt);
        if (query.isEmpty()) return null;
        Place found = null;
        try {
            found = maps.searchPlace(query);
        } catch (IOException e) {
            System.out.println("지도 검색 오류: " + e.getMessage());
        }
        if (found != null) {
            System.out.printf("찾은 장소: %s (%s) [%.4f, %.4f]%n",
                    found.getName(), found.getAddress(), found.getLat(), found.getLng());
            if (readLine("이 장소가 맞나요? (y/n) >> ").equalsIgnoreCase("y")) {
                return found;
            }
        } else {
            System.out.println("장소를 찾지 못했습니다.");
        }
        if (!readLine("위도/경도를 직접 입력할까요? (y/n) >> ").equalsIgnoreCase("y")) {
            return null;
        }
        double lat = readDouble("위도 (예: 37.5796) >> ");
        double lng = readDouble("경도 (예: 126.9770) >> ");
        return new Place(query, "직접 입력", lat, lng, 60, "기타", "");
    }

    private void addPlace(Trip trip) {
        Place p = findPlace("가고 싶은 장소 이름 (예: 경복궁) >> ");
        if (p == null) return;
        p.setStayMinutes(readInt("머무를 시간(분, 기본 60) >> ", 60));
        String cat = readLine("카테고리 (관광/맛집/카페/쇼핑 등, 기본 관광) >> ");
        p.setCategory(cat.isEmpty() ? "관광" : cat);
        p.setMemo(readLine("메모 (없으면 Enter) >> "));
        trip.addPlace(p);
        System.out.println("추가했습니다. (현재 " + trip.getPlaces().size() + "곳)");
    }

    private void listPlaces(Trip trip) {
        List<Place> places = trip.getPlaces();
        if (places.isEmpty()) {
            System.out.println("아직 추가한 장소가 없습니다.");
            return;
        }
        for (int i = 0; i < places.size(); i++) {
            System.out.println((i + 1) + ". " + places.get(i));
        }
    }

    private Place choosePlace(Trip trip) {
        listPlaces(trip);
        if (trip.getPlaces().isEmpty()) return null;
        int n = readInt("장소 번호 >> ", -1);
        if (n < 1 || n > trip.getPlaces().size()) {
            System.out.println("잘못된 번호입니다.");
            return null;
        }
        return trip.getPlaces().get(n - 1);
    }

    private void editPlace(Trip trip) {
        Place p = choosePlace(trip);
        if (p == null) return;
        System.out.println("(바꾸지 않을 항목은 그냥 Enter)");
        String name = readLine("이름 [" + p.getName() + "] >> ");
        if (!name.isEmpty()) p.setName(name);
        p.setStayMinutes(readInt("머무를 시간(분) [" + p.getStayMinutes() + "] >> ", p.getStayMinutes()));
        String cat = readLine("카테고리 [" + p.getCategory() + "] >> ");
        if (!cat.isEmpty()) p.setCategory(cat);
        String memo = readLine("메모 [" + p.getMemo() + "] >> ");
        if (!memo.isEmpty()) p.setMemo(memo);
        trip.invalidateItinerary();
        System.out.println("수정했습니다. (일정은 6번 메뉴로 다시 만들어 주세요)");
    }

    private void deletePlace(Trip trip) {
        Place p = choosePlace(trip);
        if (p == null) return;
        trip.removePlace(trip.getPlaces().indexOf(p));
        System.out.println(p.getName() + " 을(를) 삭제했습니다.");
    }

    private void setLodging(Trip trip) {
        System.out.println("숙소를 정하면 매일 숙소에서 출발해 숙소로 돌아오는 일정을 만듭니다.");
        Place p = findPlace("숙소 이름/주소 (해제하려면 그냥 Enter) >> ");
        if (p == null) {
            trip.setLodging(null);
            System.out.println("숙소 없음으로 설정했습니다.");
            return;
        }
        p.setCategory("숙소");
        p.setStayMinutes(0);
        trip.setLodging(p);
        System.out.println("숙소를 " + p.getName() + " 으로 설정했습니다.");
    }

    private void buildItinerary(Trip trip) {
        if (trip.getPlaces().isEmpty()) {
            System.out.println("먼저 장소를 추가하세요.");
            return;
        }
        System.out.println("이동 시간을 계산하고 동선을 최적화하는 중...");
        try {
            Itinerary it = optimizer.buildItinerary(trip);
            trip.setItinerary(it);
            it.print(trip.getLodging());
        } catch (IOException e) {
            System.out.println("일정 만들기 실패: " + e.getMessage());
        }
    }

    private void showItinerary(Trip trip) {
        if (trip.getItinerary() == null) {
            System.out.println("아직 일정이 없습니다. 6번 메뉴로 일정을 만들어 주세요.");
            return;
        }
        trip.getItinerary().print(trip.getLodging());
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt, int defaultValue) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) return defaultValue;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력하세요.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력하세요.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("날짜는 2026-12-24 처럼 입력하세요.");
            }
        }
    }

    private int readTime(String prompt, int defaultMin) {
        while (true) {
            String s = readLine(prompt);
            if (s.isEmpty()) return defaultMin;
            try {
                LocalTime t = LocalTime.parse(s.length() == 4 ? "0" + s : s);
                return t.getHour() * 60 + t.getMinute();
            } catch (DateTimeParseException e) {
                System.out.println("시각은 09:30 처럼 입력하세요.");
            }
        }
    }
}
