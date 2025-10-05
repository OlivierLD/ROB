package examples.satellite;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelInterface;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel
        extends JPanel
        implements ChartPanelParentInterface {
    private BorderLayout borderLayout;
    private JScrollPane jScrollPane;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton setValuesButton;
    private JCheckBox transparentCheckBox;
    private JTextField satAlt;
    private JTextField satLng;
    private JTextField satLat;

    private SampleFrame parent;

    private boolean videoHasBeenPlayed = false;

    public CommandPanel(SampleFrame caller) {
        parent = caller;
        borderLayout = new BorderLayout();
        jScrollPane = new JScrollPane();
        chartPanel = new ChartPanel(this, 600, 400);
        chartPanel.setProjection(ChartPanelInterface.SATELLITE_VIEW);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        satAlt = new JTextField();
        satAlt.setPreferredSize(new Dimension(50, 20));
        satLng = new JTextField();
        satLng.setPreferredSize(new Dimension(50, 20));
        satLat = new JTextField();
        satLat.setPreferredSize(new Dimension(50, 20));
        setValuesButton = new JButton();
        transparentCheckBox = new JCheckBox();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        double nLat = 90D;
        double sLat = -90D;

        double dSatLng = -140D;

        double wLong = dSatLng - 180D;
        double eLong = dSatLng + 180D;

//  chartPanel.setZoomFactor(1.5D);

        chartPanel.setSatelliteLongitude(dSatLng);

        chartPanel.setSatelliteLatitude(40D);

        chartPanel.setPositionToolTipEnabled(false);

//  chartPanel.setGlobeViewLngOffset(dSatLng);

        chartPanel.setGlobeViewRightLeftRotation(0.0);
        chartPanel.setGlobeViewForeAftRotation(0.0);

        chartPanel.setTransparentGlobe(true);

        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);

        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);
        chartPanel.setWithScale(false);
        chartPanel.setChartColor(Color.black);
        chartPanel.setGridColor(Color.gray);
        chartPanel.setPostitBGColor(new Color(0f, 0f, 0f, 0.5f));

        setLayout(borderLayout);
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomInActionPerformed(e);
            }
        });
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomOutActionPerformed(e);
            }
        });

        satAlt.setText(Double.toString(chartPanel.getSatelliteAltitude()));
        satAlt.setToolTipText("Sat. Alt. (km)");
        satLng.setText(Double.toString(chartPanel.getSatelliteLongitude()));
        satLng.setToolTipText("Satellite Long. (degrees)");
        satLat.setText(Double.toString(chartPanel.getSatelliteLatitude()));
        satLat.setToolTipText("Satellite Lat. (degrees)");
        setValuesButton.setText("Set");
        setValuesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setValuesActionPerformed(e);
            }
        });
        transparentCheckBox.setText("Transparent");
        transparentCheckBox.setSelected(true);
        transparentCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                transparentActionPerformed(e);
            }
        });
        jScrollPane.getViewport().add(chartPanel, null);
        add(jScrollPane, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(satAlt, null);
        bottomPanel.add(satLng, null);
        bottomPanel.add(satLat, null);
        bottomPanel.add(setValuesButton, null);
        bottomPanel.add(transparentCheckBox, null);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void transparentActionPerformed(ActionEvent e) {
        chartPanel.setTransparentGlobe(transparentCheckBox.isSelected());
        chartPanel.repaint();
    }

    private void zoomInActionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    boolean spinning = false;
    SpinningThread spinningThread = null;

    private void zoomOutActionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    private void setValuesActionPerformed(ActionEvent e) {
        double sa = 0.0;
        double sg = 0.0;
        double sl = 0.0;
        try {
            sa = Double.parseDouble(satAlt.getText());
            sg = Double.parseDouble(satLng.getText());
            sl = Double.parseDouble(satLat.getText());
        } catch (NumberFormatException nfe) {
            System.err.println(nfe.getMessage());
        }
        double satLng = sg;

        double wLong = satLng - 180D;
        double eLong = satLng + 180D; // chartPanel.calculateEastG(nLat, sLat, wLong);

        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);

        chartPanel.setSatelliteAltitude(sa);
        chartPanel.setSatelliteLongitude(sg);
        chartPanel.setSatelliteLatitude(sl);
        chartPanel.repaint();
    }

    GeoPoint from = null;
    GeoPoint to = null;

    // For video
    GeoPoint[] gpa =
            {
                    new GeoPoint(GeomUtil.sexToDec("37", "56"), -GeomUtil.sexToDec("123", "4")), // SF
                    new GeoPoint(GeomUtil.sexToDec("34", "0"), -GeomUtil.sexToDec("120", "0")), // Channel Islands
                    new GeoPoint(-GeomUtil.sexToDec("9", "0"), -GeomUtil.sexToDec("140", "0")), // Marquesas
                    new GeoPoint(-GeomUtil.sexToDec("15", "29"), -GeomUtil.sexToDec("145", "44")), // Tuamotus
                    new GeoPoint(-GeomUtil.sexToDec("18", "0"), -GeomUtil.sexToDec("149", "0")), // Tahiti
                    new GeoPoint(-GeomUtil.sexToDec("9", "0"), -GeomUtil.sexToDec("158", "0")), // Cook Islands
                    new GeoPoint(-GeomUtil.sexToDec("18", "30"), -GeomUtil.sexToDec("173", "0")), // Tonga
                    new GeoPoint(-GeomUtil.sexToDec("18", "0"), -GeomUtil.sexToDec("180", "0")), // Fiji
                    new GeoPoint(GeomUtil.sexToDec("21", "15"), -GeomUtil.sexToDec("157", "40")), // Hawaii
//    new GeoPoint(-GeomUtil.sexToDec("15", "0"), GeomUtil.sexToDec("168", "0")), // Vanuatu
//    new GeoPoint(-GeomUtil.sexToDec("8", "0"), GeomUtil.sexToDec("159", "0")), // Solomon
//    new GeoPoint(GeomUtil.sexToDec("9", "0"), GeomUtil.sexToDec("155", "0")), // Caroline
//    new GeoPoint(GeomUtil.sexToDec("13", "15"), GeomUtil.sexToDec("144", "45")), // Guam
//    new GeoPoint(GeomUtil.sexToDec("35", "0"),  GeomUtil.sexToDec("140", "0")), // Tokyo
                    new GeoPoint(GeomUtil.sexToDec("50", "0"), -GeomUtil.sexToDec("127", "0")), // Vancouver
                    new GeoPoint(GeomUtil.sexToDec("37", "56"), -GeomUtil.sexToDec("123", "4")) // SF
            };

    private boolean isVisible(double l, double g) {
        boolean plot = true;
        if (chartPanel.getProjection() == ChartPanelInterface.SATELLITE_VIEW &&
                !chartPanel.isTransparentGlobe() &&
                chartPanel.isBehind(l, g)) {
            plot = false;
        }
        return plot;
    }

    public void chartPanelPaintComponent(Graphics gr) {
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D)
            g2d = (Graphics2D) gr;
        World.drawChart(chartPanel, gr);
        // SF
        if (true) {
            double ls = GeomUtil.sexToDec("37", "49");
            double gs = -GeomUtil.sexToDec("122", "28.5");
            Point gp = chartPanel.getPanelPoint(ls, gs);
            if (isVisible(ls, gs)) {
                circleAround(gr, ls, gs);
                chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("SF:" + ls + ", " + gs);
            // Channel Islands
            ls = GeomUtil.sexToDec("34", "00");
            gs = -GeomUtil.sexToDec("120", "00");
            gp = chartPanel.getPanelPoint(ls, gs);
            if (isVisible(ls, gs)) {
                circleAround(gr, ls, gs);
                chartPanel.postit(gr, "Channel\nIslands", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Channel Islands:" + ls + ", " + gs);
            double lf = -GeomUtil.sexToDec("9", "0");
            double gf = -GeomUtil.sexToDec("140", "0");

            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Marquesas", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Marquesas:" + lf + ", " + gf);
            //  drawRhumbLine(gr, ls, gs, lf, gf);
            //  plotGreatCircle(gr, ls, gs, lf, gf);

            lf = -GeomUtil.sexToDec("15", "29");
            gf = -GeomUtil.sexToDec("145", "44");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Tuamotus", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Tuamotus:" + lf + ", " + gf);

            lf = -GeomUtil.sexToDec("17", "32");
            gf = -GeomUtil.sexToDec("149", "34");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Tahiti", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Tahiti:" + lf + ", " + gf);

            lf = -GeomUtil.sexToDec("19", "0");
            gf = -GeomUtil.sexToDec("160", "0");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Cook\nIslands", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Cook:" + lf + ", " + gf);

            lf = -GeomUtil.sexToDec("18", "30");
            gf = -GeomUtil.sexToDec("173", "0");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Tonga", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Tonga:" + lf + ", " + gf);

            lf = -GeomUtil.sexToDec("18", "0");
            gf = -GeomUtil.sexToDec("180", "0");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Fiji", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Fiji:" + lf + ", " + gf);

            // Hawai'i
            lf = GeomUtil.sexToDec("21", "15");
            gf = -GeomUtil.sexToDec("157", "40");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Hawai'i", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Hawai'i:" + lf + ", " + gf);

            //    lf = -GeomUtil.sexToDec("15", "0");
            //    gf = GeomUtil.sexToDec("168", "0");
            //    gp = chartPanel.getPanelPoint(lf, gf);
            //    circleAround(gr, lf, gf);
            //    chartPanel.postit(gr, "Vanuatu", gp.x, gp.y, Color.yellow);
            //    System.out.println("Vanuatu:" + lf + ", " + gf);
            //
            //    lf = -GeomUtil.sexToDec("8", "0");
            //    gf = GeomUtil.sexToDec("159", "0");
            //    gp = chartPanel.getPanelPoint(lf, gf);
            //    circleAround(gr, lf, gf);
            //    chartPanel.postit(gr, "Solomon\nIslands", gp.x, gp.y, Color.yellow);
            //    System.out.println("Solomon:" + lf + ", " + gf);
            //
            //    lf = GeomUtil.sexToDec("9", "0");
            //    gf = GeomUtil.sexToDec("155", "0");
            //    gp = chartPanel.getPanelPoint(lf, gf);
            //    circleAround(gr, lf, gf);
            //    chartPanel.postit(gr, "Caroline\nIslands", gp.x, gp.y, Color.yellow);
            //    System.out.println("Carolines:" + lf + ", " + gf);
            //
            //    lf = GeomUtil.sexToDec("13", "15");
            //    gf = GeomUtil.sexToDec("144", "45");
            //    gp = chartPanel.getPanelPoint(lf, gf);
            //    circleAround(gr, lf, gf);
            //    chartPanel.postit(gr, "Guam", gp.x, gp.y, Color.yellow);
            //    System.out.println("Guam:" + lf + ", " + gf);
            //
            //    lf = GeomUtil.sexToDec("35", "0");
            //    gf = GeomUtil.sexToDec("140", "0");
            //    gp = chartPanel.getPanelPoint(lf, gf);
            //    circleAround(gr, lf, gf);
            //    chartPanel.postit(gr, "Tokyo", gp.x, gp.y, Color.yellow);
            //    System.out.println("Tokyo:" + lf + ", " + gf);

            lf = GeomUtil.sexToDec("50", "0");
            gf = -GeomUtil.sexToDec("127", "0");
            gp = chartPanel.getPanelPoint(lf, gf);
            if (isVisible(lf, gf)) {
                circleAround(gr, lf, gf);
                chartPanel.postit(gr, "Vancouver", gp.x, gp.y, Color.yellow);
            }
//    System.out.println("Vancouver:" + lf + ", " + gf);

            // Recap
//    System.out.println("---------------------------");
            for (int i = 0; false && i < gpa.length - 1; i++) {
                GeoPoint start = gpa[i];
                GeoPoint finish = gpa[i + 1];
                System.out.println("From " + start.toString() + " to " + finish.toString());
                displayGreatCircle(start.getLatitude(), start.getLongitude(), finish.getLatitude(), finish.getLongitude());
            }
//    System.out.println("---------------------------");

            if (from != null && to != null) {
                ls = from.getLatitude();
                gs = from.getLongitude();
                lf = to.getLatitude();
                gf = to.getLongitude();
                gp = chartPanel.getPanelPoint(ls, gs);
                drawRhumbLine(gr, ls, gs, lf, gf);
                plotGreatCircle(gr, ls, gs, lf, gf, true);
                from = to = null;
            }

            // Plot eye position
            circleAround(gr, chartPanel.getSatelliteLatitude(), chartPanel.getSatelliteLongitude(), Color.cyan);
            String status = "Eye position: " +
                    GeomUtil.decToSex(chartPanel.getSatelliteLatitude(), GeomUtil.SWING, GeomUtil.NS) + " " +
                    GeomUtil.decToSex(chartPanel.getSatelliteLongitude(), GeomUtil.SWING, GeomUtil.EW);
            parent.setStatus(status);

            if (videoHasBeenPlayed) {
                // Plot trip
//      System.out.println("Plotting trip... " + Integer.toString(gpa.length) + " steps.");
                for (int i = 0; i < gpa.length - 1; i++) {
                    GeoPoint start = gpa[i];
                    GeoPoint finish = gpa[i + 1];
//        Point gps = chartPanel.getPanelPoint(start.getLatitude(), start.getLongitude());
                    plotGreatCircle(gr, start.getLatitude(), start.getLongitude(), finish.getLatitude(), finish.getLongitude());
                }
            }
        }
    }

    private void circleAround(Graphics gr,
                              double lat,
                              double lng) {
        circleAround(gr, lat, lng, null);
    }

    private void circleAround(Graphics gr,
                              double lat,
                              double lng,
                              Color c) {
        final int RADIUS = 10;
        Stroke originalStroke = null;
        Color originalColor = null;
        if (gr instanceof Graphics2D) {
            originalStroke = ((Graphics2D) gr).getStroke();
            Stroke stroke = new BasicStroke(2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL);
            ((Graphics2D) gr).setStroke(stroke);
        }
        originalColor = gr.getColor();
        gr.setColor(c == null ? Color.red : c);
        Point gp = chartPanel.getPanelPoint(lat, lng);
        gr.drawOval(gp.x - RADIUS, gp.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        ((Graphics2D) gr).setStroke(originalStroke);
        gr.setColor(originalColor);
    }

    private void displayGreatCircle(double ls,
                                    double gs,
                                    double lf,
                                    double gf) {
        GreatCircle gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(Math.toRadians(ls), Math.toRadians(gs)));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(lf), Math.toRadians(gf)));
        gc.calculateRhumbLine();
        System.out.println("Between " + GeomUtil.decToSex(ls, GeomUtil.SWING, GeomUtil.NS) + " - " +
                GeomUtil.decToSex(gs, GeomUtil.SWING, GeomUtil.EW) + " and " +
                GeomUtil.decToSex(lf, GeomUtil.SWING, GeomUtil.NS) + " - " +
                GeomUtil.decToSex(gf, GeomUtil.SWING, GeomUtil.EW) + ":");
        System.out.println("Distance:" + gc.calculateRhumbLineDistance() + " nm");
        double heading = gc.calculateRhumbLineRoute();
        System.out.println("Heading " + Integer.toString((int) Math.toDegrees(heading)));
    }

    private void drawRhumbLine(Graphics2D g,
                               double ls,
                               double gs,
                               double lf,
                               double gf) {
        g.setColor(Color.red);
        g.setStroke(new BasicStroke(1.0F));
        Point start = chartPanel.getPanelPoint(ls, gs);
        Point finish = chartPanel.getPanelPoint(lf, gf);
        if (start != null && finish != null)
            g.drawLine(start.x, start.y, finish.x, finish.y);
    }

    private void drawRhumbLine(Graphics g,
                               double ls,
                               double gs,
                               double lf,
                               double gf) {
        g.setColor(Color.red);
        Point start = chartPanel.getPanelPoint(ls, gs);
        Point finish = chartPanel.getPanelPoint(lf, gf);
        if (start != null && finish != null)
            g.drawLine(start.x, start.y, finish.x, finish.y);
    }

    private void plotGreatCircle(Graphics g,
                                 double ls,
                                 double gs,
                                 double lf,
                                 double gf) {
        plotGreatCircle(g, ls, gs, lf, gf, false);
    }

    private void plotGreatCircle(Graphics g,
                                 double ls,
                                 double gs,
                                 double lf,
                                 double gf,
                                 boolean print) {
        g.setColor(Color.blue);
        GreatCircle gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(Math.toRadians(ls), Math.toRadians(gs)));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(lf), Math.toRadians(gf)));
        gc.calculateGreatCircle(20);
        if (print) {
            System.out.println("Distance:" + Math.toDegrees(gc.getDistance()) * 60 + " nm");
        }
        Vector route = gc.getRoute();
        Enumeration enumeration = route.elements();
        GreatCircleWayPoint gcwp;
        for (GreatCircleWayPoint previous = null; enumeration.hasMoreElements(); previous = gcwp) {
            gcwp = (GreatCircleWayPoint) enumeration.nextElement();
            Point b = chartPanel.getPanelPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG()));
//    g.drawOval(b.x - 2, b.y - 2, 4, 4);
            if (previous != null) {
                Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }
    }

    public boolean onEvent(EventObject e, int type) {
        if (type == ChartPanel.MOUSE_CLICKED) {
            if (from == null)
                from = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            else
                to = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            chartPanel.repaint();
        }
        return true;
    }

    public String getMessForTooltip() {
        return null;
    }

    public boolean replaceMessForTooltip() {
        return false;
    }

    public void videoCompleted() {
        videoHasBeenPlayed = true;
        System.out.println("Video completed");
    }

    public void videoFrameCompleted(Graphics g, Point p) {
    }

    public void zoomFactorHasChanged(double d) {
    }

    public void chartDDZ(double top, double bottom, double left, double right) {
    }

    class SpinningThread extends Thread {
        private boolean go = true;

        public SpinningThread() {
            super();
        }

        public void stopSpinning() {
            go = false;
        }

        public void run() {
            while (go) {
                try {
                    double g = chartPanel.getGlobeViewLngOffset();
                    g -= 1.0;
                    while (g < -180) g += 360;
                    chartPanel.setGlobeViewLngOffset(g);
//        System.out.println("G:" + g);
                    synchronized (this) {
                        chartPanel.repaint();
                    }
                    sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}