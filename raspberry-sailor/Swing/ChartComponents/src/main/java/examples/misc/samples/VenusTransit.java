package examples.misc.samples;

import calc.calculation.SightReductionUtil;
import calc.calculation.nauticalalmanac.*;
import calc.GeomUtil;
import utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class VenusTransit {
    private final static SimpleDateFormat PACIFIC_FMT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
    private final static SimpleDateFormat UTC_FMT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss 'UTC'");

    static {
        PACIFIC_FMT.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        UTC_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    private final static double CURRENT_LATITUDE = 37.6642737d;
    private final static double CURRENT_LONGITUDE = -122.3802127d;

    public static void main(String[] args) {
        double minDeltaH = Double.MAX_VALUE;
        double minDeltaZ = Double.MAX_VALUE;

        for (int h = 17; h < 20; h++) {
            for (int m = 0; m < 60; m++) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                cal.set(Calendar.YEAR, 2012);
                cal.set(Calendar.MONTH, Calendar.JUNE);
                cal.set(Calendar.DAY_OF_MONTH, 5);
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, m);
                cal.set(Calendar.SECOND, 0);

                Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
                utc.setTimeInMillis(cal.getTimeInMillis());

                double deltaT = TimeUtil.getDeltaT(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH) + 1);

                int year = utc.get(Calendar.YEAR);
                int month = utc.get(Calendar.MONTH) + 1;
                int day = utc.get(Calendar.DAY_OF_MONTH);
                int hour = utc.get(Calendar.HOUR_OF_DAY);
                int minute = utc.get(Calendar.MINUTE);
                int second = utc.get(Calendar.SECOND);

                Core.julianDate(year, month, day, hour, minute, second, deltaT); // 66.5706);
                Anomalies.nutation();
                Anomalies.aberration();

                Core.aries();
                Core.sun();

                Moon.compute(); // Important! Moon is used for lunar distances, by planets and stars.

                Venus.compute();
                Mars.compute();
                Jupiter.compute();
                Saturn.compute();

                Core.polaris();
                Core.moonPhase();
                Core.weekDay();

                //    System.out.println("Sun  : GHA=" + GeomUtil.decToSex(Context.GHAsun, GeomUtil.SWING, GeomUtil.NONE) + ", D= " + GeomUtil.decToSex(Context.DECsun, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN));
                //    System.out.println("Venus: GHA=" + GeomUtil.decToSex(Context.GHAvenus, GeomUtil.SWING, GeomUtil.NONE) + ", D= " + GeomUtil.decToSex(Context.DECvenus, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN));

                SightReductionUtil sruSun = new SightReductionUtil(Context.GHAsun, Context.DECsun, CURRENT_LATITUDE, CURRENT_LONGITUDE);
                SightReductionUtil sruVenus = new SightReductionUtil(Context.GHAvenus, Context.DECvenus, CURRENT_LATITUDE, CURRENT_LONGITUDE);
                sruSun.calculate();
                sruVenus.calculate();

                double deltaH = Math.abs(sruSun.getHe() - sruVenus.getHe());
                double deltaZ = Math.abs(sruSun.getZ() - sruVenus.getZ());

                minDeltaH = Math.min(minDeltaH, deltaH);
                minDeltaZ = Math.min(minDeltaZ, deltaZ);

                if (deltaH < 0.05 || deltaZ < 0.2) {
                    System.out.println(PACIFIC_FMT.format(cal.getTime()) + " => " + UTC_FMT.format(utc.getTime()));
                    System.out.println("Sun  : H=" + GeomUtil.decToSex(sruSun.getHe(), GeomUtil.SWING, GeomUtil.NONE) + ", Z=" + GeomUtil.decToSex(sruSun.getZ(), GeomUtil.SWING, GeomUtil.NONE));
                    System.out.println("Venus: H=" + GeomUtil.decToSex(sruVenus.getHe(), GeomUtil.SWING, GeomUtil.NONE) + ", Z=" + GeomUtil.decToSex(sruVenus.getZ(), GeomUtil.SWING, GeomUtil.NONE));
                    System.out.println(" Delta H:" + (GeomUtil.decToSex(deltaH, GeomUtil.SWING, GeomUtil.NONE)) +
                            " Delta Z:" + (GeomUtil.decToSex(deltaZ, GeomUtil.SWING, GeomUtil.NONE)));
                    System.out.println("=============================================================================================");
                }
            }
        }
        System.out.println("Min Delta H=" + GeomUtil.decToSex(minDeltaH, GeomUtil.SWING, GeomUtil.NONE) + " (" + minDeltaH + ")" +
                ", Min Delta Z=" + GeomUtil.decToSex(minDeltaZ, GeomUtil.SWING, GeomUtil.NONE) + " (" + minDeltaZ + ")");
    }
}
