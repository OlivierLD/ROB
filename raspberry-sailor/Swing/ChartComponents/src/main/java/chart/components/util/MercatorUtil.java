package chart.components.util;

import calc.GeoPoint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public final class MercatorUtil {

    /**
     * Computes the Increasing Latitude. Mercator formula.
     *
     * @param lat in degrees
     * @return Increasing Latitude, in degrees.
     */
    public static double getIncLat(double lat) {
        return Math.toDegrees(Math.log(Math.tan((Math.PI / 4D) + (Math.toRadians(lat) / 2D))));
    }

    public static double getInvIncLat(double il) {
        double ret = Math.toRadians(il);
        ret = Math.exp(ret);
        ret = Math.atan(ret);
        ret -= (Math.PI / 4d); // 0.78539816339744828D;
        ret *= 2;
        ret = Math.toDegrees(ret);
        return ret;
    }

    // TODO Use the AstroComputer dead Reckoning
    public static GeoPoint deadReckoning(GeoPoint p, double d, double r) {
        return deadReckoning(p.getL(), p.getG(), d, r);
    }

    public static GeoPoint deadReckoning(double l, double g, double d, double r) {
        double deltaL = (d / 60D) * Math.cos(Math.toRadians(r));
        double l2 = l + deltaL;
        double lc1 = getIncLat(l);
        double lc2 = getIncLat(l2);
        double deltaLc = lc2 - lc1;
        double deltaG = deltaLc * Math.tan(Math.toRadians(r));
        double g2 = g + deltaG;
        return new GeoPoint(l2, g2);
    }

    private final static int MERCATOR_SCALE_HEIGHT = 70;

    public static void drawMercatorScale(Graphics gr, int width, int height, int topLeftX, int topLeftY) {
        drawMercatorScale(gr, width, height, topLeftX, topLeftY, Color.red);
    }

    public static void drawMercatorScale(Graphics gr, int width, int height, int topLeftX, int topLeftY, Color scaleColor) {
        gr.setColor(Color.white);
        gr.fillRect(topLeftX, topLeftY, width, height);
        gr.setColor(Color.black);
        gr.drawRect(topLeftX, topLeftY, width, height);
        gr.setColor(scaleColor);

        width -= 6;
        height -= 6;
        topLeftX += 3;
        topLeftY += 3;

        double oneUnitH = (double) width / 60d;
        double oneUnitV = (double) height / (double) MERCATOR_SCALE_HEIGHT;
        // Draw the curves
        for (int m = 0; m <= 60; m += 2) { // Minutes
            if (m > 10 && m % 10 != 0) {
                continue;
            }
            Point previousPoint = null;
            for (double l = 0d; l <= MERCATOR_SCALE_HEIGHT; l += 1d) {
                double r = getIncLatRatio(l);
                double val2plot = r * (double) m;
                int x = topLeftX + width - (int) (val2plot * oneUnitH);
                int y = topLeftY + height - (int) (l * oneUnitV);
                Point p = new Point(x, y);
                if (previousPoint != null) {
                    gr.drawLine(previousPoint.x, previousPoint.y, p.x, p.y);
                }
                previousPoint = p;
            }
        }
        // Horizontal lines
        for (double l = 0d; l <= MERCATOR_SCALE_HEIGHT; l += 10d) {
            double r = getIncLatRatio(l);
            double val2plot = r * 60d;
            int x = topLeftX + width - (int) (val2plot * oneUnitH);
            int y = topLeftY + height - (int) (l * oneUnitV);
            gr.drawLine(topLeftX + width, y, x, y);
        }
    }

    // Ratio on *one* degree, that is the trick.
    public static double getIncLatRatio(double lat) {
        if (lat == 0d) {
            return 1d;
        } else {
            double bottom = lat - 1d;
            if (bottom < 0d) {
                bottom = 0d;
            }
            return ((lat - bottom) / (getIncLat(lat) - getIncLat(bottom)));
        }
    }

    public static void main(String... args) {
        double d = getIncLat(45D);
        System.out.println("IncLat(45)=" + d);
        System.out.println("Rad(45)=" + Math.toRadians(45D));

        System.out.println("IncLat(60)=" + getIncLat(60D));
        System.out.println("Ratio at L=60:" + getIncLatRatio(60D));

        System.out.println("-----------------------");
        for (int i = 0; i <= 90; i += 10) {
            System.out.println("Ratio at " + i + "=" + getIncLatRatio((double) i));
        }
        System.out.println("IncLat(90)=" + getIncLat(90D));
    }
}
