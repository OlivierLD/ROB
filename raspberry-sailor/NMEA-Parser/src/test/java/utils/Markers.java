package utils;

import nmea.parser.Marker;
import nmea.utils.NMEAUtils;

import java.util.List;

public class Markers {
    public static void main(String[] args) {
        final List<Marker> markers = NMEAUtils.loadMarkers("markers.yaml");
        System.out.println("Done.");
    }
}
