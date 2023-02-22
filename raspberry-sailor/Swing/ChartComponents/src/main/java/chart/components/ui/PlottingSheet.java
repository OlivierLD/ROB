package chart.components.ui;

import calc.GeoPoint;
import calc.GreatCircle;
import calc.GreatCirclePoint;

import java.awt.*;
import java.text.DecimalFormat;

/*
 * TODO Plot-grid Lat step, Lng Step
 */
public class PlottingSheet extends ChartPanel {
    private double chartLatitudeSpan = 1d;

    private double centerLat = 37d;
    private double sLat = centerLat - (chartLatitudeSpan / 2);
    private double nLat = sLat + chartLatitudeSpan;
    private double centerLong = -122D;

    private boolean withDistanceScale = true;
    private boolean withMoreGrid = true; // Add vertical and horizontal grids
    private double gridStep = 0.5; // for latitude and longitude

    private final static DecimalFormat DF15 = new DecimalFormat("##0.00000");
    private final static DecimalFormat DF41 = new DecimalFormat("###0.0");

    public PlottingSheet(ChartPanelParentInterface chartPanelParentInterface,
                         int w,
                         int h,
                         double cL,
                         double cG,
                         double ls) {
        super(chartPanelParentInterface, w, h);
        centerLat = cL;
        chartLatitudeSpan = ls;
        sLat = centerLat - (chartLatitudeSpan / 2);
        nLat = sLat + chartLatitudeSpan;
        centerLong = cG;

        jbInit();
    }

    private void jbInit() {
        setPreferredSize(new Dimension(this.getW(), this.getH()));
//  setSize(new Dimension(this.getW(), this.getH()));

        double eLong = this.calculateEastG(this.nLat, this.sLat, this.centerLong);
        double lngWidth = Math.abs(eLong - this.centerLong);
        double wLong = this.centerLong - (lngWidth / 2d);
        eLong = wLong + lngWidth;

        this.setEastG(eLong);
        this.setWestG(wLong);
        this.setNorthL(nLat);
        this.setSouthL(sLat);
        this.setHorizontalGridInterval(0.25d);
        this.setVerticalGridInterval(0.25d);
        this.setWithScale(false);
        this.setWithGrid(false);
        this.setMajorLatitudeTick(1d / 6d);
        this.setMinorLatitudeTick(1d / 60d);
        this.setPositionToolTipEnabled(true);
        this.setWithLngLabels(false); // Because this is a canvas
    }

    public void paintComponent(Graphics gr) {
//public void chartPanelPaintComponent(Graphics gr) {
        super.paintComponent(gr);
        //  MercatorUtil.drawMercatorScale(gr, 100, 100, 10, 10, Color.darkGray);

        // Mercator Plotting Sheet circle.
        Rectangle rect = null; // chartPanel.getVisibleRect();
        rect = new Rectangle(this.getSize());
        final int BETWEEN_CIRCLE = 5;
        int diameter = (int) (Math.min(rect.height, rect.width) * 0.6d);
        // Center
        Point center = null;
        try {
            center = this.getPanelPoint(centerLat,
                    this.getWestG() + ((this.getEastG() - this.getWestG()) / 2));
        } catch (Exception npe) {
            System.err.println("PlottingSheet:" + npe.toString());
        }
        if (center == null) {
            return;
        }
        // Outer circle
        gr.drawOval(center.x - (diameter / 2),
                center.y - (diameter / 2),
                diameter,
                diameter);
        // Inner circle
        gr.drawOval(center.x - (diameter / 2) + BETWEEN_CIRCLE,
                center.y - (diameter / 2) + BETWEEN_CIRCLE,
                diameter - (BETWEEN_CIRCLE * 2),
                diameter - (BETWEEN_CIRCLE * 2));
        // Graduations on the circle
        final float increment = 11.25f; // TODO Parameter ?
        for (float i = 0; i < 360; i += increment) {
            gr.drawLine(0 + (center.x) + (int) (Math.sin(Math.toRadians(i)) * ((diameter / 2) - (BETWEEN_CIRCLE))),
                    0 + (center.y) + (int) (Math.cos(Math.toRadians(i)) * ((diameter / 2) - (BETWEEN_CIRCLE))),
                    0 + (center.x) + (int) (Math.sin(Math.toRadians(i)) * ((diameter / 2))),
                    0 + (center.y) + (int) (Math.cos(Math.toRadians(i)) * ((diameter / 2))));
        }
        // Vertical axis
        gr.drawLine(center.x, 0, center.x, rect.height);

        // Vertical graduation
        // wLong, eLong
        double eLong = this.calculateEastG(this.nLat, this.sLat, this.centerLong);
        double lngWidth = Math.abs(eLong - this.centerLong);
        double wLong = this.centerLong - (lngWidth / 2d);
        eLong = wLong + lngWidth;

        // System.out.printf("W lng: %f, E lng: %f\n", wLong, eLong);
        if (withMoreGrid) {
            double gStep = gridStep;
            // Now, left
            boolean keepGoingLeft = true;
            double currentG = this.centerLong - gStep;
            if (currentG < -180) {
                currentG += 360;
            }
            while (keepGoingLeft) { // To west
                Point left = this.getPanelPoint(centerLat, currentG);
                // System.out.printf("PointLeft: x %d, y %d\n", left.x, left.y);
                if (left.x < 0) {
                    keepGoingLeft = false;
                } else {
                    gr.drawLine(left.x, 0, left.x, rect.height);
                    currentG -= gStep;
                    if (currentG < -180) {
                        currentG += 360;
                    }
                }
            }
            // Now, right
            boolean keepGoingRight = true;
            currentG = this.centerLong + gStep;
            if (currentG > 180) {
                currentG -= 360;
            }
            while (keepGoingRight) { // To east
                Point right = this.getPanelPoint(centerLat, currentG);
                // System.out.printf("PointRight: x %d, y %d\n", right.x, right.y);
                if (right.x > rect.width) {
                    keepGoingRight = false;
                } else {
                    gr.drawLine(right.x, 0, right.x, rect.height);
                    currentG += gStep;
                    if (currentG > 180) {
                        currentG -= 360;
                    }
                }
            }
        }

        // Horizontal axis
        gr.drawLine(0, center.y, rect.width, center.y);

        // Horizontal graduation
        if (withMoreGrid) {
            double lStep = gridStep;
            // Now, up
            boolean keepGoingUp = true;
            double currentL = this.centerLat + lStep;
            if (currentL > 90) {
                keepGoingUp = false;
            }
            while (keepGoingUp) { // To north
                Point up = this.getPanelPoint(currentL, this.centerLong);
                // System.out.printf("PointUp: x %d, y %d\n", up.x, up.y);
                if (up.y < 0) {
                    keepGoingUp = false;
                } else {
                    gr.drawLine(0, up.y, rect.width, up.y);
                    currentL += lStep;
                    if (currentL > 90) {
                        keepGoingUp = false;
                    }
                }
            }
            // Now, down
            boolean keepGoingDown = true;
            currentL = this.centerLat - lStep;
            if (currentL < -90) {
                keepGoingDown = false;
            }
            while (keepGoingDown) { // To south
                Point down = this.getPanelPoint(currentL, this.centerLong);
                // System.out.printf("PointDown: x %d, y %d\n", down.x, down.y);
                if (down.y > rect.height) {
                    keepGoingDown = false;
                } else {
                    gr.drawLine(0, down.y, rect.width, down.y);
                    currentL -= lStep;
                    if (currentL < -90) {
                        keepGoingDown = false;
                    }
                }
            }
        }

        // Scale ?
        if (withDistanceScale) {
            Point p1 = new Point(10, rect.height - 10);
            Point p2 = new Point(10 + rect.width / 4, rect.height - 10);
            gr.drawLine(p1.x, p1.y, p2.x, p2.y);
            gr.drawLine(p1.x, p1.y, p1.x, p1.y - 5);
            gr.drawLine(p2.x, p2.y, p2.x, p2.y - 5);
            GeoPoint g1 = this.getGeoPos(p1.x, p1.y);
            GeoPoint g2 = this.getGeoPos(p2.x, p2.y);
            double rhumbLineDistance = GreatCircle.calculateRhumbLineDistance(new GreatCirclePoint(g1), new GreatCirclePoint(g2)) * 60d;
            String mess = DF15.format(rhumbLineDistance) + " nm";
            if (rhumbLineDistance < 0.05) {
                mess += (" ( " + DF41.format(rhumbLineDistance * 1852) + " meter(s) )");
            }
            gr.drawString(mess, 10 + (rect.width / 4) + 10, rect.height - 10);
        }
    }

    public void setChartLatitudeSpan(double chartLatitudeSpan) {
        this.chartLatitudeSpan = chartLatitudeSpan;
        jbInit();
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
        sLat = centerLat - (chartLatitudeSpan / 2);
        nLat = sLat + chartLatitudeSpan;
        jbInit();
    }

    public void setCenterLong(double centerLong) {
        this.centerLong = centerLong;
        jbInit();
    }

    public void setWithDistanceScale(boolean withScale) {
        this.withDistanceScale = withScale;
    }

    public void setWithMoreGrid(boolean withMoreGrid) {
        this.withMoreGrid = withMoreGrid;
    }

    public void setGridStep(double gridStep) {
        this.gridStep = gridStep;
    }

    public double getChartLatitudeSpan() {
        return chartLatitudeSpan;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLong() {
        return centerLong;
    }
}
