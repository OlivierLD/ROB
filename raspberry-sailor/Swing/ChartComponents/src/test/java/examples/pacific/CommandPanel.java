package examples.pacific;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel
        extends JPanel
        implements ChartPanelParentInterface {
    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    int nbe;

    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        nbe = 0;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> jButton1_actionPerformed(e));
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> jButton2_actionPerformed(e));
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        add(bottomPanel, BorderLayout.SOUTH);
//    double nLat = 55D;
//    double sLat = -70D;
//    double wLong = 128.5D;
//    double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);

        double nLat = 66.5D;
        double sLat = -48.5D;
        double wLong = 127.5D;
        double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);

        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);
        chartPanel.setWithScale(false);
        chartPanel.setMouseDraggedEnabled(true);
        chartPanel.setMouseDraggedType(ChartPanel.MOUSE_DRAG_ZOOM);
        chartPanel.setPositionToolTipEnabled(true);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    GeoPoint from = null;
    GeoPoint to = null;

    public void chartPanelPaintComponent(Graphics gr) {
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D) {
            g2d = (Graphics2D) gr;
        }
//  World.drawChart(chartPanel, gr, nbe, Color.RED);
        World.paintChart(null, chartPanel, g2d, Color.orange);
        double ls = GeomUtil.sexToDec("37", "56");
        double gs = -GeomUtil.sexToDec("123", "4");
        double lf = GeomUtil.sexToDec("21", "15");
        double gf = -GeomUtil.sexToDec("157", "40");
        Point gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Hawaii", gp.x, gp.y, Color.yellow);
        drawRhumbLine(g2d, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = GeomUtil.sexToDec("49", "40");
        gs = -GeomUtil.sexToDec("6", "34");
        lf = GeomUtil.sexToDec("14", "15");
        gf = -GeomUtil.sexToDec("61", "12");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "English Channel", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "West Indies", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = GeomUtil.sexToDec("37", "56");
        gs = -GeomUtil.sexToDec("123", "4");
        lf = GeomUtil.sexToDec("34", "45");
        gf = GeomUtil.sexToDec("140", "0");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Tokyo", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = GeomUtil.sexToDec("37", "56");
        gs = -GeomUtil.sexToDec("123", "4");
        lf = -GeomUtil.sexToDec("9", "0");
        gf = -GeomUtil.sexToDec("140", "0");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Marqueses", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = -GeomUtil.sexToDec("9", "0");
        gs = -GeomUtil.sexToDec("140", "0");
        lf = -GeomUtil.sexToDec("41", "41");
        gf = GeomUtil.sexToDec("174", "40");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "Marqueses", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Wellington", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = GeomUtil.sexToDec("37", "56");
        gs = -GeomUtil.sexToDec("123", "4");
        lf = -GeomUtil.sexToDec("41", "41");
        gf = GeomUtil.sexToDec("174", "40");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Wellington", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = -GeomUtil.sexToDec("41", "41");
        gs = GeomUtil.sexToDec("174", "40");
        lf = -GeomUtil.sexToDec("52", "26");
        gf = -GeomUtil.sexToDec("75", "10");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "Wellington", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Magellan", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = -GeomUtil.sexToDec("52", "26");
        gs = -GeomUtil.sexToDec("75", "10");
        lf = -GeomUtil.sexToDec("1", "0");
        gf = -GeomUtil.sexToDec("91", "0");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "Magellan", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Galapagos", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        ls = -GeomUtil.sexToDec("1", "0");
        gs = -GeomUtil.sexToDec("91", "0");
        lf = GeomUtil.sexToDec("37", "56");
        gf = -GeomUtil.sexToDec("123", "4");
        gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "Galapagos", gp.x, gp.y, Color.yellow);
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "San Francisco", gp.x, gp.y, Color.yellow);
        drawRhumbLine(gr, ls, gs, lf, gf);
        plotGreatCircle(gr, ls, gs, lf, gf);
        lf = GeomUtil.sexToDec("37", "56");
        gf = -GeomUtil.sexToDec("123", "4");
        GeoPoint gpt = new GeoPoint(lf, gf);
        chartPanel.plotLOP(gr, gpt, 235D, 5D, "Sun");

        lf = GeomUtil.sexToDec("13", "15");
        gf = GeomUtil.sexToDec("144", "45");
        gp = chartPanel.getPanelPoint(lf, gf);
        chartPanel.postit(gr, "Guam", gp.x, gp.y, Color.yellow);

        if (from != null && to != null) {
            ls = from.getL();
            gs = from.getG();
            lf = to.getL();
            gf = to.getG();
            gp = chartPanel.getPanelPoint(ls, gs);
            drawRhumbLine(gr, ls, gs, lf, gf);
            plotGreatCircle(gr, ls, gs, lf, gf, true);
            from = to = null;
        }
    }

    private void drawRhumbLine(Graphics2D g, double ls, double gs, double lf, double gf) {
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
        gc.setStart(new GreatCirclePoint(ls, gs));
        gc.setArrival(new GreatCirclePoint(lf, gf));
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
            nbe++;
            System.out.println("Painting Section " + nbe);
            if (from == null) {
                from = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            } else {
                to = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            }
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
    }

    public void videoFrameCompleted(Graphics g, Point p) {
    }

    public void zoomFactorHasChanged(double d) {
    }

    public void chartDDZ(double top, double bottom, double left, double right) {
    }
}
