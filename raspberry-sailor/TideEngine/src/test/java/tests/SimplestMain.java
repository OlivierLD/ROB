package tests;

import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;
import tideengine.TideUtilities.TimedValue;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * This is just a test, not a unit-test.
 * It fails if it does not work ;)
 */
public class SimplestMain {
//    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");

    public static void main(String... args) throws Exception {
        System.out.println(args.length + " Argument(s)...");
        boolean xmlTest = true; // true: XML, false: JSON

        System.setProperty("tide.verbose", "true");

        if (xmlTest) {
            System.out.println("XML Tests");
        } else {
            System.out.println("JSON Tests");
            System.setProperty("tide.flavor", "JSON"); // See BackEndTideComputer.Option
        }
        final BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
        backEndTideComputer.setVerbose(true);
        backEndTideComputer.connect();

        // Some tests
        TideStation ts = null;

        long before = 0;
        long after = 0;

        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

        Calendar now = GregorianCalendar.getInstance();
        String location;
//        final String STATION_PATTERN = "Port Townsend";
//        final String STATION_PATTERN = "Port-Navalo"; // Port-Navalo%2C%20France
//        final String STATION_PATTERN = "ICELAND";
//        final String STATION_PATTERN = "Patreksfj";
//        final String STATION_PATTERN = "Harrington";
//        final String STATION_PATTERN = "Hornaf";
//        final String STATION_PATTERN = "Port-Tudy";
//        final String STATION_PATTERN = "Port-Tudy%2C%20France";
        final String STATION_PATTERN = "Port-Tudy, France";

        System.setProperty("tide.verbose", "false");
        // System.setProperty("with.tide.coeffs", "true");
        location = URLEncoder.encode(STATION_PATTERN, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        int year = now.get(Calendar.YEAR);
        ts = backEndTideComputer.findTideStation(location, year);
        if (ts != null) {
            now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
            if (ts != null) {
                String stationFullName = URLDecoder.decode(ts.getFullName(), StandardCharsets.UTF_8.toString());
                if (false) {
                    double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
                    System.out.println("At " + stationFullName +
                            " in " + now.get(Calendar.YEAR) +
                            ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) +
                            " " + ts.getUnit() +
                            ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) +
                            " " + ts.getDisplayUnit());
                }
                before = System.currentTimeMillis();
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
                after = System.currentTimeMillis();
                System.out.println("-----------------------------");
                System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") +
                        " in [" + stationFullName + "] at " + now.getTime().toString() +
                        " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
                System.out.println("-----------------------------");
                System.out.printf("Done is %s ms.\n", NumberFormat.getInstance().format(after - before));
            }
        } else {
            System.out.println(String.format("%s not found for year %d :(", location, year));
        }
        if (true) {
            year += 1;
            ts = backEndTideComputer.findTideStation(location, year);
            if (ts != null) {
                now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
                if (ts != null) {
                    String stationFullName = URLDecoder.decode(ts.getFullName(), StandardCharsets.UTF_8.toString());
                    if (false) {
                        double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
                        System.out.println("At " + stationFullName +
                                " in " + now.get(Calendar.YEAR) +
                                ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) +
                                " " + ts.getUnit() +
                                ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) +
                                " " + ts.getDisplayUnit());
                    }
                    before = System.currentTimeMillis();
                    double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
                    after = System.currentTimeMillis();
                    System.out.println("-----------------------------");
                    System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") +
                            " in [" + stationFullName + "] at " + now.getTime().toString() +
                            " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
                    System.out.println("-----------------------------");
                    System.out.printf("Done is %s ms.\n", NumberFormat.getInstance().format(after - before));
                }
            } else {
                System.out.println(String.format("%s not found for year %d :(", location, year));
            }

        }
        backEndTideComputer.disconnect();
    }
}
