package chartview.util.tide;

// import java.text.SimpleDateFormat;

import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.text.SimpleDateFormat;
import java.util.*;


public class TideComputer {

    // private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm z (Z)");

    private final static BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

    public static String calculateTideAt(String location) throws Exception {
        Calendar now = GregorianCalendar.getInstance();
        return calculateTideAt(location, now);
    }

    public static String calculateTideAt(String location, Calendar when) throws Exception {
        String output = "";
        backEndTideComputer.connect();
        backEndTideComputer.setVerbose(false);

        TideStation ts = null;

        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

        Calendar now = when;
        ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
        String timeZone = ts.getTimeZone();

        // A test, CSV format, for a spreadsheet
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, yyyy-MMM-dd HH:mm z (Z)");
        if (ts != null) {
            if (timeZone != null)
                sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
            final int RISING = 1;
            final int FALLING = -1;

            double low1 = Double.NaN;
            double low2 = Double.NaN;
            double high1 = Double.NaN;
            double high2 = Double.NaN;
            Calendar low1Cal = null;
            Calendar low2Cal = null;
            Calendar high1Cal = null;
            Calendar high2Cal = null;
            int trend = 0;

            double previousWH = Double.NaN;
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m++) {
                    Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH),
                            h, m);
                    if (timeZone != null)
                        cal.setTimeZone(TimeZone.getTimeZone(timeZone));
                    double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
                    if (Double.isNaN(previousWH))
                        previousWH = wh;
                    else {
                        if (trend == 0) {
                            if (previousWH > wh)
                                trend = -1;
                            else if (previousWH < wh)
                                trend = 1;
                        } else {
                            switch (trend) {
                                case RISING:
                                    if (previousWH > wh) // Now going down
                                    {
                                        if (Double.isNaN(high1)) {
                                            high1 = previousWH;
                                            cal.add(Calendar.MINUTE, -1);
                                            high1Cal = cal;
                                        } else {
                                            high2 = previousWH;
                                            cal.add(Calendar.MINUTE, -1);
                                            high2Cal = cal;
                                        }
                                        trend = FALLING; // Now falling
                                    }
                                    break;
                                case FALLING:
                                    if (previousWH < wh) // Now going up
                                    {
                                        if (Double.isNaN(low1)) {
                                            low1 = previousWH;
                                            cal.add(Calendar.MINUTE, -1);
                                            low1Cal = cal;
                                        } else {
                                            low2 = previousWH;
                                            cal.add(Calendar.MINUTE, -1);
                                            low2Cal = cal;
                                        }
                                        trend = RISING; // Now rising
                                    }
                                    break;
                            }
                        }
                        previousWH = wh;
                    }
                }
            }
            List<TimedValue> timeAL = new ArrayList<TimedValue>(4);
            if (low1Cal != null) {
              timeAL.add(new TimedValue("LW", low1Cal, low1));
            }
            if (low2Cal != null) {
              timeAL.add(new TimedValue("LW", low2Cal, low2));
            }
            if (high1Cal != null) {
              timeAL.add(new TimedValue("HW", high1Cal, high1));
            }
            if (high2Cal != null) {
              timeAL.add(new TimedValue("HW", high2Cal, high2));
            }

            Collections.sort(timeAL);

            for (TimedValue tv : timeAL) {
              output += (tv.getType() + " " + sdf.format(tv.getCalendar().getTime()) + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit() + "\n");
            }
        }
        backEndTideComputer.disconnect();

        return output;
    }

    private static class TimedValue implements Comparable<TimedValue> {
        private Calendar cal;
        private double value;
        private String type = "";

        public TimedValue(String type, Calendar cal, double d) {
            this.type = type;
            this.cal = cal;
            this.value = d;
        }

        public int compareTo(TimedValue tv) {
            return this.cal.compareTo(tv.getCalendar());
        }

        public Calendar getCalendar() {
            return cal;
        }

        public double getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public boolean equals(Object o) {
            return (o instanceof TimedValue && this.compareTo((TimedValue) o) == 0);
        }
    }

    // Just a test
    public static void main(String... args) throws Exception {
        if (args.length > 0) {
          System.out.println("Duh?");
        }
        String location = "Ocean Beach";
        String out = calculateTideAt(location);
        System.out.println(out);
    }
}

