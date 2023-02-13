package examples.incrlat;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel extends JPanel
        implements ChartPanelParentInterface {

    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        this.setBackground(new Color(174, 220, 211));
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> jButton1_actionPerformed(e));
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> jButton2_actionPerformed(e));
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        add(bottomPanel, BorderLayout.SOUTH);
        double nLat = 67D;
        double sLat = -10D;
        double wLong = -100D;
        double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    public void chartPanelPaintComponent(Graphics gr) {
        World.drawChart(chartPanel, gr);
        double ls = GeomUtil.sexToDec("0", "0");
        double gs = GeomUtil.sexToDec("0", "0");
        Point center = chartPanel.getPanelPoint(ls, gs);

        double lf = GeomUtil.sexToDec("45", "0");
        double gf = GeomUtil.sexToDec("0", "0");
        Point top = chartPanel.getPanelPoint(lf, gf);

        int w = 2 * Math.abs(top.y - center.y);
        int h = w;
        gr.setColor(Color.red);
        gr.fillOval(center.x - 4, center.y - 4, 8, 8);
        gr.fillOval(top.x - 2, top.y - 2, 4, 4);
        ((Graphics2D) gr).drawArc(top.x - (w / 2), top.y, w, h, 180, -90);
        gr.fillOval(center.x - (w / 2) - 2, center.y - 2, 4, 4);
        String str = "45\272N";
        chartPanel.postit(gr, str, top.x, top.y, Color.yellow);
        str = "50\27229.9 W";
        chartPanel.postit(gr, str, center.x - (w / 2), center.y, Color.yellow);
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf, double gf) {
        g.setColor(Color.red);
        Point start = chartPanel.getPanelPoint(ls, gs);
        Point finish = chartPanel.getPanelPoint(lf, gf);
        if (start != null && finish != null)
            g.drawLine(start.x, start.y, finish.x, finish.y);
    }

    private GreatCircle plotGreatCircle(Graphics g, double ls, double gs, double lf, double gf) {
        g.setColor(Color.blue);
        GreatCircle gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(ls, gs));
        gc.setArrival(new GreatCirclePoint(lf, gf));
        gc.calculateGreatCircle(20);
        Vector route = gc.getRoute();
        Enumeration enumeration = route.elements();
        GreatCircleWayPoint gcwp;
        for (GreatCircleWayPoint previous = null; enumeration.hasMoreElements(); previous = gcwp) {
            gcwp = (GreatCircleWayPoint) enumeration.nextElement();
            Point b = chartPanel.getPanelPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG()));
            g.drawOval(b.x - 2, b.y - 2, 4, 4);
            if (previous != null) {
                Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        return gc;
    }

    public boolean onEvent(EventObject eventobject, int i) {
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

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;


    public void zoomFactorHasChanged(double d) {
    }

    public void chartDDZ(double top, double bottom, double left, double right) {
    }
}
