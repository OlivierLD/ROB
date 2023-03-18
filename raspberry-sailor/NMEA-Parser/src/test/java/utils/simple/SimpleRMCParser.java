package utils.simple;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleRMCParser {
    static String nmeaSentence = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A\r\n"; // No Type here

    final static int RMC_UTC = 1;
    final static int RMC_ACTIVE_VOID = 2;
    final static int RMC_LATITUDE_VALUE = 3;
    final static int RMC_LATITUDE_SIGN = 4;
    final static int RMC_LONGITUDE_VALUE = 5;
    final static int RMC_LONGITUDE_SIGN = 6;
    final static int RMC_SOG = 7;
    final static int RMC_COG = 8;
    final static int RMC_DDMMYY = 9;
    final static int RMC_VARIATION_VALUE = 10;
    final static int RMC_VARIATION_SIGN = 11;
    final static int RMC_TYPE = 12;

    static int calculateCheckSum(String str) {
        int cs;
        char[] ca = str.toCharArray();
        cs = ca[0];
        for (int i = 1; i < ca.length; i++) {
            cs = cs ^ ca[i]; // XOR
        }
        return cs;
    }

    static boolean validCheckSum(String data) {
        String sentence = data.trim();
        boolean b = false;
        try {
            int starIndex = sentence.indexOf("*");
            if (starIndex < 0) {
                return false;
            }
            String csKey = sentence.substring(starIndex + 1);
            int csk = Integer.parseInt(csKey, 16);
            String str2validate = sentence.substring(1, sentence.indexOf("*"));
            int calcCheckSum = calculateCheckSum(str2validate);
            b = (calcCheckSum == csk);
        } catch (Exception ex) {
            System.err.println("Oops:" + ex.getMessage());
        }
        return b;
    }

    static double sexToDec(String degrees, String minutes)
            throws RuntimeException {
        double ret;
        try {
            double deg = Double.parseDouble(degrees);
            double min = Double.parseDouble(minutes);
            min *= (10.0 / 6.0);
            ret = deg + (min / 100D);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            System.err.println("Degrees:" + degrees);
            System.err.println("Minutes:" + minutes);
            throw new RuntimeException("Bad number [" + degrees + "] [" + minutes + "]");
        }
        return ret;
    }

    static class GeoPos {
        double lat;
        double lng;

        public GeoPos(double l, double g) {
            this.lat = l;
            this.lng = g;
        }
    }

    static class RMC {
        private GeoPos gp = null;
        private double sog = -1D;
        private double cog = -1D;

        private boolean valid = false; // False means warning.

        private Date rmcDate = null;
        private Date rmcTime = null;
        private double declination = -Double.MAX_VALUE;

        public enum RMC_TYPE {
            AUTONOMOUS,
            DIFFERENTIAL,
            ESTIMATED,
            NOT_VALID,
            SIMULATOR
        }

        private RMC_TYPE rmcType = null;

        private final static SimpleDateFormat SDF = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss.SS");

        static {
            SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        }
    }

    private final static NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);

    static RMC parseRMC(String str) {
        RMC rmc = null;
        if (str.length() < 6 || !str.contains("*")) {
            return null;
        }
        if (!validCheckSum(str)) {
            return null;
        }
        String s = str.substring(0, str.indexOf("*"));
        try {
            if (s.contains("RMC,")) {
                rmc = new RMC();

                String[] data = s.split(",");
                rmc.valid = (data[RMC_ACTIVE_VOID].equals("A")); // Active. Does not prevent the date and time from being available.
                if (data[RMC_UTC].length() > 0) { // Time and Date
                    double utc = 0D;
                    try {
                        utc = NF.parse(data[RMC_UTC]).doubleValue();
                    } catch (Exception ex) {
                        System.out.println("data[1] in StringParsers.parseRMC");
                    }
                    int h = (int) (utc / 10_000);
                    int m = (int) ((utc - (10_000 * h)) / 100);
                    float sec = (float) (utc % 100f);

                    // System.out.println("Data[1]:" + data[1] + ", h:" + h + ", m:" + m + ", s:" + sec);

                    Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
                    local.set(Calendar.HOUR_OF_DAY, h);
                    local.set(Calendar.MINUTE, m);
                    local.set(Calendar.SECOND, Math.round(sec));
                    local.set(Calendar.MILLISECOND, 0);
                    if (data[RMC_DDMMYY].length() > 0) {
                        int d = 1;
                        try {
                            d = Integer.parseInt(data[RMC_DDMMYY].substring(0, 2));
                        } catch (Exception ex) {
                            if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
                                ex.printStackTrace();
                            }
                        }
                        int mo = 0;
                        try {
                            mo = Integer.parseInt(data[RMC_DDMMYY].substring(2, 4)) - 1;
                        } catch (Exception ex) {
                            if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
                                ex.printStackTrace();
                            }
                        }
                        int y = 0;
                        try {
                            y = Integer.parseInt(data[RMC_DDMMYY].substring(4));
                        } catch (Exception ex) {
                            if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
                                ex.printStackTrace();
                            }
                        }
                        if (y > 50) {
                            y += 1900;
                        } else {
                            y += 2_000;
                        }
                        local.set(Calendar.DATE, d);
                        local.set(Calendar.MONTH, mo);
                        local.set(Calendar.YEAR, y);
                        // In case the GPS date is wrong (it happens):
                        String gpsOffset = System.getProperty("rmc.date.offset");
                        // Offset in DAYS to add to the RMC Date.
                        // One of mines has an offset of 7168 (0x1C00) days.
                        if (gpsOffset != null) {
                            try {
                                int offset = Integer.parseInt(gpsOffset);
                                if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
                                    System.out.printf(">>> Adding %d days to %s\n", offset, local.getTime());
                                }
                                local.add(Calendar.DATE, offset); // Add in Days
                                if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
                                    System.out.printf(">>>   that becomes %s\n", local.getTime());
                                }
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                        }
                        Date rmcDate = local.getTime();
                        rmc.rmcDate = rmcDate;
                    }
                    Date rmcTime = local.getTime();
                    rmc.rmcTime = rmcTime;
                }
                if (data[RMC_LATITUDE_VALUE].length() > 0 && data[RMC_LONGITUDE_VALUE].length() > 0) {
                    String deg = data[RMC_LATITUDE_VALUE].substring(0, 2);
                    String min = data[RMC_LATITUDE_VALUE].substring(2);
                    double l = sexToDec(deg, min);
                    if ("S".equals(data[RMC_LATITUDE_SIGN])) {
                        l = -l;
                    }
                    deg = data[RMC_LONGITUDE_VALUE].substring(0, 3);
                    min = data[RMC_LONGITUDE_VALUE].substring(3);
                    double g = sexToDec(deg, min);
                    if ("W".equals(data[RMC_LONGITUDE_SIGN])) {
                        g = -g;
                    }
                    rmc.gp = new GeoPos(l, g);
                }
                if (data[RMC_SOG].length() > 0) {
                    double speed = 0;
                    try {
                        speed = NF.parse(data[RMC_SOG]).doubleValue();
                    } catch (Exception ex) {
                        if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
                            ex.printStackTrace();
                        }
                    }
                    rmc.sog = speed;
                }
                if (data[RMC_COG].length() > 0) {
                    double cog = 0;
                    try {
                        cog = NF.parse(data[RMC_COG]).doubleValue();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    rmc.cog = cog;
                }
                if (data[RMC_VARIATION_VALUE].length() > 0 && data[RMC_VARIATION_SIGN].length() > 0) {
                    double d = -Double.MAX_VALUE;
                    try {
                        d = NF.parse(data[RMC_VARIATION_VALUE]).doubleValue();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if ("W".equals(data[RMC_VARIATION_SIGN])) {
                        d = -d;
                    }
                    rmc.declination = d;
                }
                if (data.length > 12) { // Can be missing
                    switch (data[RMC_TYPE]) {
                        case "A":
                            rmc.rmcType = RMC.RMC_TYPE.AUTONOMOUS;
                            break;
                        case "D":
                            rmc.rmcType = RMC.RMC_TYPE.DIFFERENTIAL;
                            break;
                        case "E":
                            rmc.rmcType = RMC.RMC_TYPE.ESTIMATED;
                            break;
                        case "N":
                            rmc.rmcType = RMC.RMC_TYPE.NOT_VALID;
                            break;
                        case "S":
                            rmc.rmcType = RMC.RMC_TYPE.SIMULATOR;
                            break;
                        default:
                            rmc.rmcType = null;
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("In parseRMC for " + str.trim() + ", " + e);
            e.printStackTrace();
        }
        return rmc;
    }

    public static void main(String... args) {
        System.out.println("Parsing " + nmeaSentence);
        RMC rmc = parseRMC(nmeaSentence);
        // Display members
        System.out.println("Valid:" + rmc.valid);
        System.out.println("Lat:" + rmc.gp.lat + ", Lng:" + rmc.gp.lng);
        System.out.println("Speed over Ground:" + rmc.sog);
        System.out.println("Course over Ground:" + rmc.cog);

        System.out.println("UTC Date:" + RMC.SDF.format(rmc.rmcDate));
        System.out.println("UTC Time:" + RMC.SDF.format(rmc.rmcTime));

        System.out.println("Declination:" + rmc.declination);
    }
}
