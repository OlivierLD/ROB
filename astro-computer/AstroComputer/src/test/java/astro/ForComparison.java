package astro;

import calc.DeadReckoning;
import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * For Comparison with data used by Yves Robin-Jouan
 * WiP...
 */
public class ForComparison {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public static void main(String... args) {

        double latitude = 0d;
        double longitude = 0d;

        AstroComputerV2 ac = new AstroComputerV2();

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 1995);
        date.set(Calendar.MONTH, 1); // Feb
        date.set(Calendar.DAY_OF_MONTH, 4);
        date.set(Calendar.HOUR_OF_DAY, 23); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 40);
        date.set(Calendar.SECOND, 31);

        System.out.printf("Calculating at %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        final double venusGHA = ac.getVenusGHA();
        final double venusDecl = ac.getVenusDecl();

        DeadReckoning dr = new DeadReckoning(venusGHA, venusDecl, latitude, longitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s,\n" +
                          "Sun at GHA: %.02f\272, Decl: %.02f\272 \n" +
                          " From position %s / %s\n" +
                          "Obs Alt: %s, Z: %.01f\272\n",
                            SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                            venusGHA, venusDecl,
                            GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                            GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                            GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            z);

    }

}