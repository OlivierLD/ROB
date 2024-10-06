package tables;

import calc.calculation.SightReductionUtil;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

public class CorrectionTables {
    private final static boolean verbose = false;

    public static final int UPPER_LIMB = 0;
    public static final int LOWER_LIMB = 1;
    public static final int NEAR_LIMB = 2;
    public static final int FAR_LIMB = 3;
    public static final int NO_LIMB = -1;

    private static final DecimalFormat df = new DecimalFormat("##0.000");
    private static PrintStream out = null;

    private final static double eyeHeight[] = {0D, 2D, 4D, 6D, 8D, 10D, 12D, 14D, 16D, 18D, 20D, 22D, 24D};

    public static void getHorizonDipCorrection() {
        // Horizon Dip
        out.println("  <horizon-dips>");
        for (int i = 0; i < eyeHeight.length; i++) {
            double hd = SightReductionUtil.getHorizonDip(eyeHeight[i]);
            out.println("    <horizon-dip eye-height-in-meters='" + eyeHeight[i] + "' dip='" + df.format(hd) + "'/>");
        }
        out.println("  </horizon-dips>");
    }

    public static void getPlanetsHorizontalParallax() {
        int[] alt = {0, 30, 60, 90};

        // Pa
        out.println("  <planet-parallax>");
        for (int i = 0; i < alt.length; i++) {
            out.println("  <alt value='" + alt[i] + "'>");
            for (double pi = 0.1; pi <= 1d; pi += 0.1) {
                double pa = SightReductionUtil.getParallax(pi, alt[i]);
                out.println("    <corr pi='" + df.format(pi) + "'>" + df.format(pa) + "</corr>");
            }
            out.println("  </alt>");
        }
        out.println("  </planet-parallax>");
    }

    public static void getSunCorrectionTable() {
        final double obsAltitude[] = {7D, 7.333333333333333D, 7.6666666666666599D,
                8D, 8.3333333333333321D, 8.6666666666666607D,
                9D, 9.3333333333333321D, 9.6666666666666607D,
                10D, 10.33333333333333D, 10.666666666666661D,
                11D, 11.5D, 12D, 12.5D, 13D, 13.5D, 14D, 15D,
                16D, 17D, 18D, 19D, 20D, 22D, 24D, 26D, 28D, 30D,
                32D, 34D, 36D, 38D, 40D, 45D, 50D, 55D, 60D, 70D,
                80D, 90D};

        double sd = 16d;
        double hp = 0.1;

        out.println("<sun-corrections>");
        for (int i = 0; i < obsAltitude.length; i++) {
            out.println("  <obs-altitude value=\"" + obsAltitude[i] + "\">");
            for (int j = 0; j < eyeHeight.length; j++) {
                double corr = SightReductionUtil.getAltitudeCorrection(obsAltitude[i],
                                                                       eyeHeight[j],
                                                                       hp / 60d,
                                                                       sd / 60d,
                                                                       LOWER_LIMB,
                                                                       false, // TODO A section for artificial horizon !
                                                                       verbose);
                out.println("    <corr eye-height=\"" + eyeHeight[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "</corr>");
            }
            out.println("  </obs-altitude>");
        }
        out.println("</sun-corrections>");
    }

    public static void getMoonCorrectionTable() {
        double obsAltitude[] = {5d, 5.5, 6d, 6.5, 7D, 7.5, 8D, 8.5, 9D,
                10D, 11D, 12d, 13d, 14D, 15D, 16D, 17D, 18D, 19D,
                20D, 21d, 22D, 23d, 24D, 25d, 26D, 27d, 28D, 29d,
                30D, 31d, 32D, 33d, 34D, 35d, 36D, 37d, 38D, 39d,
                40D, 41d, 42d, 43d, 44d, 45D, 46d, 47d, 48d, 49d,
                50D, 51d, 52D, 53d, 54d, 55d, 56d, 57d, 58d, 59d,
                60D, 61d, 62d, 63d, 64d, 65d, 66d, 76d, 68d, 69d,
                70D, 71d, 72d, 73d, 74d, 75d, 76d, 77d, 78d, 79d,
                80D, 81d, 82d, 83d, 84d, 85d, 86d, 87d, 88d, 90D};

        double horParallax[] = {54, 55, 55.5, 56, 56.5, 57, 57.5, 58, 58.5, 59, 59.5, 60, 61};
        double moonDiameter[] = {29.4, 30d, 30.3, 30.6, 30.8, 31.1, 31.4, 31.7, 32d, 32.2, 32.5, 32.8, 33.3};

        double sd = 0d;

        out.println("<moon-corrections>");

        // Parallax + Refraction
        for (int i = 0; i < obsAltitude.length; i++) {
            out.println("  <obs-altitude value=\"" + obsAltitude[i] + "\">");
            for (int j = 0; j < horParallax.length; j++) {
                // Refraction + Parallax
                double corr = SightReductionUtil.getAltitudeCorrection(obsAltitude[i],
                                                                       0d, // Eye Height
                                                                       horParallax[j] / 60d,
                                                                       sd / 60d,
                                                                       NO_LIMB,
                                                                       false, // TODO A section for artificial horizon !
                                                                       verbose);
                corr += ((moonDiameter[j] / 2d) / 60d);
                out.println("    <corr-ref-pa hp=\"" + horParallax[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "</corr-ref-pa>");
            }

            out.println("  </obs-altitude>");
        }
        // Semi diameter, from the almanac

        out.println("</moon-corrections>");
    }

    public static void getPlanetStarsCorrectionTable() {
        double obsAltitude[] = {7D, 7.333333333333333D, 7.6666666666666599D,
                8D, 8.3333333333333321D, 8.6666666666666607D,
                9D, 9.3333333333333321D, 9.6666666666666607D,
                10D, 10.33333333333333D, 10.666666666666661D,
                11D, 11.5D, 12D, 12.5D, 13D, 13.5D, 14D, 15D,
                16D, 17D, 18D, 19D, 20D, 22D, 24D, 26D, 28D, 30D,
                32D, 34D, 36D, 38D, 40D, 45D, 50D, 55D, 60D, 70D,
                80D, 90D};
        double sd = 0d;
        double hp = 0d;

        out.println("<planets-stars-corrections>");
        for (int i = 0; i < obsAltitude.length; i++) {
            out.println("  <obs-altitude value=\"" + obsAltitude[i] + "\">");
            for (int j = 0; j < eyeHeight.length; j++) {
                double corr = SightReductionUtil.getAltitudeCorrection(obsAltitude[i],
                        eyeHeight[j],
                        hp / 60d,
                        sd / 60d,
                        NO_LIMB,
                        false, // TODO A section for artificial horizon !
                        verbose);
                out.println("    <corr eye-height=\"" + eyeHeight[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "</corr>");
            }
            out.println("  </obs-altitude>");
        }
        out.println("</planets-stars-corrections>");
    }

    public static void main(String... args) throws Exception {
    //  out = System.out;
        out = new PrintStream(new FileOutputStream("corrections.xml"));

        out.println("<?xml version='1.0' encoding='utf-8'?>");
        out.println("<altitude-corrections>");
        out.println("<!-- All corrections given in minutes -->");
        getHorizonDipCorrection();
        getPlanetsHorizontalParallax();
        getSunCorrectionTable();
        getMoonCorrectionTable();
        getPlanetStarsCorrectionTable();
        out.println("</altitude-corrections>");

        out.close();
    }

}
