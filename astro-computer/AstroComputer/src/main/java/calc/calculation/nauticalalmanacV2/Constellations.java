package calc.calculation.nauticalalmanacV2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class Constellations {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        URL constellationsUrl = Constellations.class.getResource("constellations.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Object> constellations = mapper.readValue(constellationsUrl, List.class);
            System.out.println("Read a " + constellations.getClass().getName());
            constellations.stream()
                    .map(obj -> (Map<String, Object>)obj)
                    .forEach(map -> {
                        // System.out.println("-> " + map.getClass().getName());
                        String name = (String)map.get("name");
                        List<Object> stars = (List<Object>)map.get("stars");
                        Object linesObj = map.get("lines");
                        if (linesObj instanceof List) {
                            List<Object> lines = (List<Object>) linesObj;
                            System.out.printf("Constellation [%s], %d star(s), %d line(s)\n", name, stars.size(), lines.size());
                        } else if (linesObj instanceof Map) {
                            Map<String, Object> lineMap = (Map<String, Object>)linesObj;
                            System.out.printf("Constellation [%s], %d star(s), %d line(s)\n", name, stars.size(), 1);
                        } else {
                            System.out.printf("For %s, we have a %s...\n", name, linesObj.getClass().getName());
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
