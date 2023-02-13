package examples.plottingsheet.one;

import calc.GeoPoint;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.ui.PlottingSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.EventObject;

public class PlottingSheetImpl
        extends JPanel
        implements ChartPanelParentInterface {
    private static final double CHART_LATITUDE_SPAN = 1d;
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = (int) (CANVAS_WIDTH / 1.0);

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private PlottingSheet plottingSheet;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    private JButton setWidthButton;
    private JSlider zoomSlider;
    private JTextField zoomValueFld;
    private final static DecimalFormat DF22 = new DecimalFormat("#0.00");
    private JLabel label = new JLabel("Center Lat:");
    private JTextField centerLatValueFld;
    private JTextField widthFld;

    private JButton updateButton;

    private double centerLat = 37d;
    private double sLat = centerLat - (CHART_LATITUDE_SPAN / 2);
    private double nLat = sLat + CHART_LATITUDE_SPAN;

    SampleFrame parent = null;

    public PlottingSheetImpl(SampleFrame caller) {
        parent = caller;

        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        plottingSheet = new PlottingSheet(this, CANVAS_WIDTH, CANVAS_HEIGHT, 37d, -122d, 1d);
        plottingSheet.setWithDistanceScale(true);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        setWidthButton = new JButton();

        updateButton = new JButton();
        centerLatValueFld = new JTextField();
        widthFld = new JTextField();
        widthFld.setText(Integer.toString(CANVAS_WIDTH));

        zoomSlider = new JSlider();
        zoomValueFld = new JTextField();

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        parent.setTitle(plottingSheet.getWidth() + " x " + plottingSheet.getHeight());

        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> {
            jButton1_actionPerformed(e);
            parent.setTitle(plottingSheet.getW() + " x " + plottingSheet.getH());
        });
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> {
            jButton2_actionPerformed(e);
            parent.setTitle(plottingSheet.getW() + " x " + plottingSheet.getH());
        });

        setWidthButton.setText("Set Width");
        setWidthButton.addActionListener(e -> setWidth_actionPerformed(e));

        updateButton.setText("Update Chart");
        updateButton.addActionListener(e -> update_actionPerformed(e));

        centerLatValueFld.setText(Double.toString(sLat + (CHART_LATITUDE_SPAN / 2D)));
        centerLatValueFld.setPreferredSize(new Dimension(40, 20));
        centerLatValueFld.setHorizontalAlignment(4);

        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(evt -> {
            int slider = ((JSlider) evt.getSource()).getValue();
            double zoom = 50D / (double) slider;
            zoomValueFld.setText(DF22.format(zoom));
            plottingSheet.applyZoom(zoom);
        });
        zoomValueFld.setText("1");
        zoomValueFld.setPreferredSize(new Dimension(40, 20));
        zoomValueFld.setHorizontalAlignment(4);
        zoomValueFld.addActionListener(e -> zoomValueFld_actionPerformed(e));
        jScrollPane1.getViewport().add(plottingSheet, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(widthFld, null);
        bottomPanel.add(setWidthButton, null);

        bottomPanel.add(label, null);
        bottomPanel.add(centerLatValueFld, null);
        bottomPanel.add(updateButton, null);
        bottomPanel.add(zoomSlider, null);
        bottomPanel.add(zoomValueFld, null);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        plottingSheet.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        plottingSheet.zoomOut();
    }

    private void update_actionPerformed(ActionEvent e) {
        update();
        setWidth();
    }

    private void update() {
        try {
            centerLat = Double.parseDouble(centerLatValueFld.getText());
            double _sLat = centerLat - (CHART_LATITUDE_SPAN / 2d);
            sLat = _sLat;
            nLat = sLat + CHART_LATITUDE_SPAN;
//    chartPanel.zoomIn();
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setWidth_actionPerformed(ActionEvent e) {
        setWidth();
    }

    private void setWidth() {
        System.out.println("Setting width to :" + widthFld.getText());
        try {
            int w = Integer.parseInt(widthFld.getText());
            double saveZoom = plottingSheet.getZoomFactor();
            double z = (double) w / (double) plottingSheet.getWidth();
            plottingSheet.setZoomFactor(z);
            plottingSheet.zoomIn();
            plottingSheet.setZoomFactor(saveZoom);
            parent.setTitle(plottingSheet.getWidth() + " x " + plottingSheet.getHeight());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void chartPanelPaintComponent(Graphics gr) {
//  plottingSheet.chartPanelPaintComponent(gr);
        plottingSheet.repaint();

        // Specific goes here
      GeoPoint[] groundData = new GeoPoint[]{
              new GeoPoint(37d, -122.0),
              new GeoPoint(36.85d, -122.1),
              new GeoPoint(37.1d, -121.9),
              new GeoPoint(37.25d, -122.15),
              new GeoPoint(37.26d, -121.95)
      };
      GeoPoint[] drData = new GeoPoint[]{
              new GeoPoint(37d, -122.0),
              new GeoPoint(36.859d, -122.14),
              new GeoPoint(37.09d, -121.87),
              new GeoPoint(37.25d, -122.14),
              new GeoPoint(37.20d, -121.90)
      };
        // Over Ground
        gr.setColor(Color.black);
        Point ppt = null;
        for (int i = 0; i < groundData.length; i++) {
            Point pt = plottingSheet.getPanelPoint(groundData[i]);
            if (ppt != null) {
                gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
                ppt = pt;
            }
            ppt = pt;
        }
        // Thru water
        gr.setColor(Color.red);
        ppt = null;
        for (int i = 0; i < drData.length; i++) {
            Point pt = plottingSheet.getPanelPoint(drData[i]);
            if (ppt != null) {
                gr.drawLine(ppt.x, ppt.y, pt.x, pt.y);
                ppt = pt;
            }
            ppt = pt;
        }
        // Current
        gr.setColor(Color.green);
        Point p1 = plottingSheet.getPanelPoint(groundData[groundData.length - 1]);
        Point p2 = plottingSheet.getPanelPoint(drData[drData.length - 1]);
        gr.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    public boolean onEvent(EventObject e, int type) {
        if (type == 0) {
            MouseEvent me = (MouseEvent) e;
            int x = me.getX();
            int y = me.getY();
            GeoPoint gp = plottingSheet.getGeoPos(x, y);
            // TODO Something with the point...
        }
        return true;
    }

    private void zoomValueFld_actionPerformed(ActionEvent e) {
        System.out.println("Action performed on the field");
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
