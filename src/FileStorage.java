import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 여행 데이터를 텍스트 파일로 저장하고 불러온다.
 * 한 줄에 한 항목, 값은 탭(\t)으로 구분한다.
 *
 * TRIP    id  제목  지역  시작일  일수  하루시작(분)  하루종료(분)
 * LODGING 이름 주소 위도 경도
 * PLACE   이름 주소 위도 경도 체류(분) 카테고리 메모 방문완료(true/false)
 * ITIN    출처
 * DAY     일차번호 숙소복귀이동(분)
 * VISIT   장소번호 이동(분) 도착(분) 출발(분)
 * UNSCHED 장소번호
 * END
 */
public class FileStorage {
    private final File file;

    public FileStorage(String path) {
        this.file = new File(path);
    }

    public void save(TripManager manager) throws IOException {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (Trip t : manager.getAllTrips()) {
                writeLine(w, "TRIP", t.getId(), t.getTitle(), t.getDestination(), t.getStartDate(),
                        t.getDays(), t.getDayStartMin(), t.getDayEndMin());
                Place l = t.getLodging();
                if (l != null) {
                    writeLine(w, "LODGING", l.getName(), l.getAddress(), l.getLat(), l.getLng());
                }
                for (Place p : t.getPlaces()) {
                    writeLine(w, "PLACE", p.getName(), p.getAddress(), p.getLat(), p.getLng(),
                            p.getStayMinutes(), p.getCategory(), p.getMemo(), p.isVisited());
                }
                Itinerary it = t.getItinerary();
                if (it != null) {
                    writeLine(w, "ITIN", it.getSourceName());
                    for (DayPlan d : it.getDays()) {
                        writeLine(w, "DAY", d.getDayNumber(), d.getReturnTravelMinutes());
                        for (Visit v : d.getVisits()) {
                            writeLine(w, "VISIT", t.getPlaces().indexOf(v.getPlace()),
                                    v.getTravelMinutes(), v.getArrivalMin(), v.getDepartureMin());
                        }
                    }
                    for (Place p : it.getUnscheduled()) {
                        writeLine(w, "UNSCHED", t.getPlaces().indexOf(p));
                    }
                }
                writeLine(w, "END");
            }
        }
    }

    public void load(TripManager manager) throws IOException {
        if (!file.exists()) return;  // 처음 실행이면 불러올 것이 없다

        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            Trip trip = null;
            Itinerary it = null;
            DayPlan day = null;
            List<Place> places = null;
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = line.split("\t", -1);
                switch (f[0]) {
                    case "TRIP":
                        trip = new Trip(Integer.parseInt(f[1]), f[2], f[3], LocalDate.parse(f[4]),
                                Integer.parseInt(f[5]), Integer.parseInt(f[6]), Integer.parseInt(f[7]));
                        places = trip.getPlaces();
                        it = null;
                        break;
                    case "LODGING":
                        trip.setLodging(new Place(f[1], f[2], Double.parseDouble(f[3]),
                                Double.parseDouble(f[4]), 0, "숙소", ""));
                        break;
                    case "PLACE":
                        Place place = new Place(f[1], f[2], Double.parseDouble(f[3]),
                                Double.parseDouble(f[4]), Integer.parseInt(f[5]), f[6], f[7]);
                        // 방문 완료 칸은 나중에 추가된 항목이라, 예전 파일에는 없을 수 있다
                        place.setVisited(f.length > 8 && Boolean.parseBoolean(f[8]));
                        trip.addPlace(place);
                        break;
                    case "ITIN":
                        it = new Itinerary(f[1]);
                        break;
                    case "DAY":
                        int n = Integer.parseInt(f[1]);
                        day = new DayPlan(n, trip.getStartDate().plusDays(n - 1));
                        day.setReturnTravelMinutes(Integer.parseInt(f[2]));
                        it.addDay(day);
                        break;
                    case "VISIT":
                        day.addVisit(new Visit(places.get(Integer.parseInt(f[1])), Integer.parseInt(f[2]),
                                Integer.parseInt(f[3]), Integer.parseInt(f[4])));
                        break;
                    case "UNSCHED":
                        it.addUnscheduled(places.get(Integer.parseInt(f[1])));
                        break;
                    case "END":
                        trip.setItinerary(it);   // 장소를 다 넣은 뒤에 일정을 붙인다
                        manager.addLoadedTrip(trip);
                        trip = null;
                        break;
                    default:
                        // 알 수 없는 줄은 무시
                }
            }
        } catch (RuntimeException e) {
            throw new IOException("저장 파일 형식이 올바르지 않습니다: " + e.getMessage(), e);
        }
    }

    /** 값들을 탭으로 이어 한 줄로 쓴다. 값 안의 탭/줄바꿈은 공백으로 바꾼다. */
    private void writeLine(BufferedWriter w, Object... values) throws IOException {
        List<String> parts = new ArrayList<>();
        for (Object v : values) {
            parts.add(String.valueOf(v).replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
        }
        w.write(String.join("\t", parts));
        w.newLine();
    }
}
