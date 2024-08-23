package tiderest.utils;

import tideengine.BackEndTideComputer;
import tideengine.TideStation;
import tideengine.publisher.TidePublisher;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static tideengine.publisher.TidePublisher.TIDE_TABLE;

/**
 * Two main features:
 * - Filter stations, based on their location
 * - Publish almanacs for the selected station, for a given year
 *
 * See the script publishtides.sh for an example of the way to use it.
 */
public class StationSelector {

    static void positionSelector(double nLat, double sLat, double wLng, double eLng) {
        try {
            List<TideStation> selectedList = TidePublisher.getStationList(nLat, sLat, wLng, eLng); // TODO FIRST FILTER
            if (selectedList != null) {
                selectedList.stream().forEach(station -> {
                    try {
                        // String decodedStationName = URLDecoder.decode(station.getFullName(), StandardCharsets.ISO_8859_1.toString());
                        // System.out.printf("%s\n", decodedStationName); // , GeomUtil.decToSex(station.getLatitude()));
                        System.out.printf("%s\n", station.getFullName()); // Encoded !! No blank !
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                });
            }
            // System.out.println("Et hop!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final static String N_LAT = "--n-lat:";
    private final static String S_LAT = "--s-lat:";
    private final static String W_LNG = "--w-lng:";
    private final static String E_LNG = "--e-lng:";
    private final static String PUBLISH = "--publish";
    private final static String SELECT = "--select";
    private final static String TIDE_YEAR = "--tide-year:";
    private final static String STATION_NAME = "--station-name:";
    private final static String LANG = "--lang:";

    public static void main(String[] args) {

        // Default values
        double nLat =  90d;
        double sLat = -90d;
        double eLng =  180d;
        double wLng = -180d;

        // Select station, or publish almanac ?
        boolean select = false;
        boolean publish = false;

        String stationName = "Brest, France";
        int tideYear = 2024;

        String lang = null;

        // Script prms management
        for (String arg : args) {
            // System.out.printf("Processing arg [%s]\n", arg);
            if (arg.startsWith(N_LAT)) {
                String value = arg.substring(N_LAT.length());
                try {
                    nLat = Double.parseDouble(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(S_LAT)) {
                String value = arg.substring(S_LAT.length());
                try {
                    sLat = Double.parseDouble(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(E_LNG)) {
                String value = arg.substring(E_LNG.length());
                try {
                    eLng = Double.parseDouble(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(W_LNG)) {
                String value = arg.substring(W_LNG.length());
                try {
                    wLng = Double.parseDouble(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(TIDE_YEAR)) {
                String value = arg.substring(TIDE_YEAR.length());
                try {
                    tideYear = Integer.parseInt(value);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(STATION_NAME)) {
                String value = arg.substring(STATION_NAME.length());
                try {
                    stationName = value;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.startsWith(LANG)) {
                String value = arg.substring(LANG.length());
                try {
                    lang = value;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (arg.equals(SELECT)) {
                select = true;
            } else if (arg.equals(PUBLISH)) {
                publish = true;
            }
        }
        // Now, proceed
        if (select) {
            try {
                positionSelector(nLat, sLat, wLng, eLng); // Spits out the list out the standard output
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (publish) {
            if ((stationName.startsWith("'") && stationName.endsWith("'")) || stationName.startsWith("\"") && stationName.endsWith("\"")) { // Trim quotes
                stationName = stationName.substring(1, stationName.length() - 1);
            }
            stationName = stationName.replace("+", " ");  // Ugly escape trick...
            try {
                System.out.printf(">> Publishing for [%s], year %d, lang %s\n", URLDecoder.decode(stationName, StandardCharsets.ISO_8859_1.toString()), tideYear, lang);

                BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
                backEndTideComputer.connect();

                String f = TidePublisher.publish(
                        stationName, // URLEncoder.encode(stationName, StandardCharsets.UTF_8.toString()).replace("+", "%20"),
                        Calendar.JANUARY,
                        tideYear,
                        1,
                        Calendar.YEAR,
                        TIDE_TABLE, // Change at will AGENDA_TABLE, MOON_CALENDAR
                        String.format("%s.%d", URLDecoder.decode(stationName, StandardCharsets.ISO_8859_1.toString()).replace(" ", "_"), tideYear), // final file name
                        lang);
                System.out.printf("Generated %s\n", f);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main__(String[] args) {

        String stationName = "Brest, France";
        TideStation ts = null;
        try {
            BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
            backEndTideComputer.connect();

            // List<TideStation> stationData = BackEndTideComputer.getStationData();
            Optional<TideStation> optTs = BackEndTideComputer.getStationData()
                    .stream()
                    .filter(station -> station.getFullName().equals(stationName))
                    .findFirst();
            if (!optTs.isPresent()) {
                throw new Exception(String.format("Station [%s] not found.", stationName));
            } else {
                ts = optTs.get();
                System.out.println("Found it");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
