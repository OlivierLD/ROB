package calc.calculation.nauticalalmanacV2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Static constellations data.
 */
public class Constellations {

    private static boolean verbose = false;
    private static ObjectMapper mapper = new ObjectMapper();

    public static class Star {
        String name;
        // SHA(star) = 15 * RA(star). RA is in hours.
        double ra;
        double d;
        public Star() {
        }
        public Star(String name, double ra, double d) {
            this.name = name;
            this.ra = ra;
            this.d = d;
        }
        public Star name(String name) {
            this.name = name;
            return this;
        }
        public Star ra(double ra) {
            this.ra = ra;
            return this;
        }
        public Star d(double d) {
            this.d = d;
            return this;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getRa() {
            return ra;
        }

        public void setRa(double ra) {
            this.ra = ra;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }
    }
    public static class ConstellationLine {
        String from;
        String to;
        public ConstellationLine() {
        }
        public ConstellationLine(String from, String to) {
            this.from = from;
            this.to = to;
        }
        public ConstellationLine from(String from) {
            this.from = from;
            return this;
        }
        public ConstellationLine to(String to) {
            this.to = to;
            return this;
        }
        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
    public static class Constellation {
        String name;
        List<Star> stars;
        List<ConstellationLine> lines;

        public Constellation() {}
        public Constellation(String name) {
            this.name = name;
        }
        public Constellation name(String name) {
            this.name = name;
            return this;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Star> getStars() {
            return stars;
        }

        public void setStars(List<Star> stars) {
            this.stars = stars;
        }

        public List<ConstellationLine> getLines() {
            return lines;
        }

        public void setLines(List<ConstellationLine> lines) {
            this.lines = lines;
        }
    }

    private List<Constellation> constellations;

    private static Constellations instance = null;
    private Constellations() {
        // Build the list here, once.
        this.constellations = buildConstellationList();
    }
    public static Constellations getInstance() {
        if (instance == null) {
            instance = new Constellations();
        }
        return instance;
    }

    /**
     * THE one that turns the json object into a nice java one.
     * @return a beautiful list.
     */
    @SuppressWarnings("unchecked")
    private static List<Constellation> buildConstellationList() {
        List<Constellation> constellationList = new ArrayList<>();
        if ("true".equals("constellations.verbose")) {
            System.out.println("Building Constellation list");
        }

        URL constellationsUrl = Constellations.class.getResource("constellations.json");
        try {
            List<Object> constellations = mapper.readValue(constellationsUrl, List.class);
            if (verbose) {
                System.out.println("Read a " + constellations.getClass().getName());
            }
            constellations.stream()
                    .map(obj -> (Map<String, Object>)obj)
                    .forEach(map -> {
                        // System.out.println("-> " + map.getClass().getName());
                        String name = (String)map.get("name");

                        Constellation constellation = new Constellation(name);

                        List<Map<String, Object>> stars = (List<Map<String, Object>>)map.get("stars");
                        List<Star> starList = new ArrayList<>();
                        stars.forEach(star -> {
                            String starName = (String)star.get("name");
                            double ra = (Double)star.get("ra");
                            double d = (Double)star.get("d");
                            Star st = new Star(starName, ra, d);
                            starList.add(st);
                        });
                        constellation.setStars(starList);

                        Object linesObj = map.get("lines");
                        List<ConstellationLine> lineList = new ArrayList<>();

                        if (linesObj instanceof List) {
                            List<Map<String, String>> lines = (List<Map<String, String>>) linesObj;
                            if (verbose) {
                                System.out.printf("Constellation [%s], %d star(s), %d line(s)\n", name, stars.size(), lines.size());
                            }
                            lines.forEach(line -> {
                               String from = line.get("from");
                               String to = line.get("to");
                               lineList.add(new ConstellationLine(from, to));
                            });
                        } else if (linesObj instanceof Map) {
                            Map<String, Object> lineMap = (Map<String, Object>)linesObj;
                            if (verbose) {
                                System.out.printf("Constellation [%s], %d star(s), %d line(s)\n", name, stars.size(), 1);
                            }
                            String from = (String)((Map<String, Object>)lineMap.get("line")).get("from");
                            String to = (String)((Map<String, Object>)lineMap.get("line")).get("to");
                            lineList.add(new ConstellationLine(from, to));
                        } else {
                            if (verbose) {
                                System.out.printf("For %s, we have a %s...\n", name, linesObj.getClass().getName());
                            }
                        }
                        constellation.setLines(lineList);

                        constellationList.add(constellation);
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return constellationList;
    }
    public List<Constellation> getConstellations() {
        return constellations;
    }

    public static void main(String... args) throws Exception {

        final List<Constellation> constellations = Constellations.getInstance().getConstellations();

        final String jsonConstellations = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(constellations);

        System.out.println("--------------------");
        System.out.println(jsonConstellations);
        System.out.println("--------------------");

        // For fun
        // min-max RA and D
        AtomicReference<Double> minRA = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxRA = new AtomicReference<>(-minRA.get());
        AtomicReference<Double> minD = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> maxD = new AtomicReference<>(-minD.get());
        constellations.forEach(constellation -> {
            constellation.getStars().forEach(star -> {
                minRA.set(Math.min(minRA.get(), star.getRa()));
                maxRA.set(Math.max(maxRA.get(), star.getRa()));

                minD.set(Math.min(minD.get(), star.getD()));
                maxD.set(Math.max(maxD.get(), star.getD()));
            });
        });
        System.out.printf("Min RA: %f\n", minRA.get());
        System.out.printf("Max RA: %f\n", maxRA.get());

        System.out.printf("Min D: %f\n", minD.get());
        System.out.printf("Max D: %f\n", maxD.get());
    }
}
