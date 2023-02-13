package examples.nightnday;

import calc.GeoPoint;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.MercatorUtil;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        mouseCheckBox = new JCheckBox("GrabScroll enabled");
        mouseCheckBox.setSelected(false);
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
        mouseCheckBox.addActionListener(e -> swicthMouse(mouseCheckBox.isSelected()));

        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(mouseCheckBox, null);
        add(bottomPanel, BorderLayout.SOUTH);
        double nLat = 90D;
        double sLat = -90D;
        double wLong = -180D;
        double eLong = 180D; // chartPanel.calculateEastG(nLat, sLat, wLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setProjection(ChartPanel.ANAXIMANDRE);
        chartPanel.setWidthFromChart(nLat, sLat, wLong, eLong);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);
        chartPanel.setWithScale(false);
        chartPanel.setChartColor(Color.blue);
        chartPanel.setGridColor(Color.gray);

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

    GeoPoint from = null;
    GeoPoint to = null;

    private final GeoPoint sunPos = new GeoPoint(-21.6, -23.0);
    private final double nightDistance = 90 * 60; // 90 degrees, in miles.

    public void chartPanelPaintComponent(Graphics gr) {
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D) {
          g2d = (Graphics2D) gr;
        }
        World.drawChart(chartPanel, gr);

        gr.setColor(Color.gray);
        Point prev = null;
        for (float hdg = 0.5f; hdg < 361; hdg++) {
            GeoPoint deadReckoning = MercatorUtil.deadReckoning(sunPos, nightDistance, hdg);
            Point pt = chartPanel.getPanelPoint(deadReckoning);
            if (prev != null) {
              gr.drawLine(prev.x, prev.y, pt.x, pt.y);
            }
            prev = pt;
        }

    }

    public boolean onEvent(EventObject e, int type) {
        if (type == ChartPanel.MOUSE_CLICKED) {
            GeoPoint clicked = chartPanel.getGeoPos(((MouseEvent) e).getX(), ((MouseEvent) e).getY());
            // Doing nothing
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
