package examples.misc.samples;

import calc.calculation.AstroComputerV2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DayLight {
    // private final static double latitude = 38;
    private final static double latitude = 47.677667;
    // private final static double longitude = -122;
    private final static double longitude = -3.135667;

    private final static SimpleDateFormat SDF = new SimpleDateFormat("MMM-dd");

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private final static NumberFormat DF22 = new DecimalFormat("#0.00");
    private final static NumberFormat DF2 = new DecimalFormat("00");

    public static void main(String... args) {
        Calendar utcCal = GregorianCalendar.getInstance();
        utcCal.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        utcCal.getTime();

        AstroComputerV2 astroComputer = new AstroComputerV2();
        astroComputer.setDateTime(utcCal.get(Calendar.YEAR),
                utcCal.get(Calendar.MONTH) + 1,
                utcCal.get(Calendar.DAY_OF_MONTH),
                utcCal.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))),
                utcCal.get(Calendar.MINUTE),
                utcCal.get(Calendar.SECOND));
        astroComputer.calculate();
        double[] rsSun = astroComputer.sunRiseAndSet(latitude, longitude);
        double daylight = (rsSun[AstroComputerV2.UTC_SET_IDX] - rsSun[AstroComputerV2.UTC_RISE_IDX]);
        System.out.println(SDF.format(utcCal.getTime()) + ", Daylight:" + decimalHoursToHMS(daylight, false));
        System.out.println("========================================");
        boolean go = true;
        utcCal.set(Calendar.MONTH, Calendar.JANUARY);
        utcCal.set(Calendar.DAY_OF_MONTH, 1);
        utcCal.set(Calendar.HOUR_OF_DAY, 0);
        utcCal.set(Calendar.MINUTE, 0);
        utcCal.set(Calendar.SECOND, 0);

        int year = utcCal.get(Calendar.YEAR);
        while (go) {
            astroComputer.setDateTime(utcCal.get(Calendar.YEAR),
                    utcCal.get(Calendar.MONTH) + 1,
                    utcCal.get(Calendar.DAY_OF_MONTH),
                    utcCal.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))),
                    utcCal.get(Calendar.MINUTE),
                    utcCal.get(Calendar.SECOND));
            astroComputer.calculate();
            rsSun = astroComputer.sunRiseAndSet(latitude, longitude);
            daylight = (rsSun[AstroComputerV2.UTC_SET_IDX] - rsSun[AstroComputerV2.UTC_RISE_IDX]);
            System.out.println(SDF.format(utcCal.getTime()) + ", Daylight:" + decimalHoursToHMS(daylight, false));
            utcCal.add(Calendar.DAY_OF_MONTH, 1);
            go = (year == utcCal.get(Calendar.YEAR));
        }
    }

    private static String decimalHoursToHMS(double diff) {
        return decimalHoursToHMS(diff, true);
    }

    private static String decimalHoursToHMS(double diff, boolean withSign) {
        double dh = Math.abs(diff);
        String s = "";
        if (dh >= 1)
            s += (DF2.format((int) dh) + "h ");
        double remainder = dh - ((int) dh);
        double minutes = remainder * 60;
        if (s.trim().length() > 0 || minutes >= 1)
            s += (DF2.format((int) minutes) + "m ");
        remainder = minutes - (int) minutes;
        double seconds = remainder * 60;
        s += (DF2.format((int) seconds) + "s");
        if (withSign) {
            if (diff < 0) {
                s = "- " + s;
            } else {
                s = "+ " + s;
            }
        }
        return s.trim();
    }
}
