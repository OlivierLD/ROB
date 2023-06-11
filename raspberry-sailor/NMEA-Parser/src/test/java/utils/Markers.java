package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nmea.parser.Marker;
import nmea.utils.NMEAUtils;

import java.util.List;

public class Markers {
    public static void main(String[] args) {
        final List<Marker> markers = NMEAUtils.loadMarkers("markers.yaml");
        System.out.println("Done with markers.");
        ObjectMapper mapper = new ObjectMapper();
        try {
            final String json = mapper.writeValueAsString(markers);
            System.out.println(json);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
    }
}
