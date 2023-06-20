package examples.shoredetection;

import calc.GeoPoint;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class CommandPanel
        extends JPanel
        implements ChartPanelParentInterface {

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JCheckBox mouseCheckBox;

    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        mouseCheckBox = new JCheckBox("GrabScroll");
        mouseCheckBox.setSelected(false);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        setLayout(borderLayout1);
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }

        });
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }

        });
        mouseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                swicthMouse(mouseCheckBox.isSelected());
            }
        });

        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(mouseCheckBox, null);
        add(bottomPanel, BorderLayout.SOUTH);
        double nLat = 83D;
        double sLat = -65D;
        double wLong = -180D;
        double eLong = 180D; // chartPanel.calculateEastG(nLat, sLat, wLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);

        chartPanel.setWidthFromChart(nLat, sLat, wLong, eLong);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);
        chartPanel.setWithScale(false);

        chartPanel.setMouseDraggedEnabled(mouseCheckBox.isSelected());

//  chartPanel.setMouseDraggedType(ChartPanel.MOUSE_DRAG_GRAB_SCROLL);
        chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        chartPanel.setPositionToolTipEnabled(true);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    private void swicthMouse(boolean b) {
        chartPanel.setMouseDraggedEnabled(b);
        chartPanel.setMouseDraggedType(b ? ChartPanel.MOUSE_DRAG_GRAB_SCROLL : ChartPanel.MOUSE_DRAG_ZOOM);
    }

    GeoPoint plot = null;

    public void chartPanelPaintComponent(Graphics gr) {
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D)
            g2d = (Graphics2D) gr;
        World.paintChart(null, chartPanel, g2d, Color.orange);
        World.drawChart(chartPanel, gr);

        gr.setColor(Color.red);
        if (plot != null) {
            Point pt = chartPanel.getPanelPoint(plot);
            gr.fillOval(pt.x - 2, pt.y - 2, 4, 4);
        }

        if (from != null && to != null) {
            gr.setColor(Color.blue);
            Point pf = chartPanel.getPanelPoint(from);
            Point pt = chartPanel.getPanelPoint(to);
            gr.fillOval(pf.x - 1, pf.y - 1, 2, 2);
            gr.fillOval(pt.x - 1, pt.y - 1, 2, 2);
            gr.drawLine(pf.x, pf.y, pt.x, pt.y);
        }

        if (crossedPolygon != null) {
            gr.setColor(Color.green);
            Point previous = null;
            Point first = null;
            for (int i = 0; i < crossedPolygon.npoints; i++) {
                Point pt = chartPanel.getPanelPoint(new GeoPoint((double) crossedPolygon.ypoints[i] / 1000, (double) crossedPolygon.xpoints[i] / 1000));
                if (first == null)
                    first = pt;
                if (previous != null)
                    gr.drawLine(previous.x, previous.y, pt.x, pt.y);
                previous = pt;
            }
        }
    }

    GeoPoint from = null, to = null;
    Polygon crossedPolygon = null;

    public boolean onEvent(EventObject e, int type) {
        if (type == ChartPanel.MOUSE_CLICKED) {
            plot = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            if (World.isInLand(plot))
                System.out.print("In land");
            else
                System.out.print("Off shore");

            if (from == null)
                from = plot;
            else {
                if (to == null)
                    to = plot;
                else {
                    from = to;
                    to = plot;
                }
            }
            if (from != null && to != null) {
                crossedPolygon = World.isRouteCrossingLand(from, to);
                boolean crossCountry = crossedPolygon != null;
                if (crossCountry)
                    System.out.print(", Crossing Land");
                else
                    System.out.print(", Just sea");
            }
            System.out.println();

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
