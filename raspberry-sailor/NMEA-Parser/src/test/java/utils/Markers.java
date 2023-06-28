package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nmea.parser.Border;
import nmea.parser.Marker;
import nmea.utils.NMEAUtils;

import java.util.List;

public class Markers {
    static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        // Markers
        final List<Marker> markers = NMEAUtils.loadMarkers("markers.yaml");
        System.out.println("Done with markers.");
        try {
            final String json = mapper.writeValueAsString(markers);
            System.out.println(json);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
        // Borders
        final List<Border> borders = NMEAUtils.loadBorders("markers.yaml");
        System.out.println("Done with borders.");
        ObjectMapper mapper = new ObjectMapper();
        try {
            final String json = mapper.writeValueAsString(borders);
            System.out.println(json);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
    }
}
