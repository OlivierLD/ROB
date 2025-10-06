package examples.casestudy;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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
    private JButton printButton;
    private JButton undoButton;
    private JSlider zoomSlider;
    private JTextField zoomValueFld;
    private JLabel label = new JLabel("South Lat:");
    private JTextField sLatValueFld;
    private JButton updateButton;
    //private Spatial spatial;
    private GeoPoint from;
    private GeoPoint to;

    private double sLat = 21D;
    private double nLat = sLat + 6D;

    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this, 1024, 768);
        chartPanel.setMouseDraggedEnabled(true);
//  chartPanel.setMouseDraggedType(ChartPanel.MOUSE_DRAG_GRAB_SCROLL);
        chartPanel.setMouseDraggedType(ChartPanel.MOUSE_DRAW_ON_CHART);

        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        updateButton = new JButton();
        printButton = new JButton();
        undoButton = new JButton();
        sLatValueFld = new JTextField();
        zoomSlider = new JSlider();
        zoomValueFld = new JTextField();
//  spatial = null;
        from = null;
        to = null;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        setPreferredSize(new Dimension(1024, 768));
        setSize(new Dimension(1024, 768));
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> jButton1_actionPerformed(e));
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> jButton2_actionPerformed(e));
        updateButton.setText("Update Chart");
        updateButton.addActionListener(e -> update_actionPerformed(e));
        undoButton.setText("Undo");
        undoButton.setToolTipText("Undo last hand drawing");
        undoButton.addActionListener(e -> undo_actionPerformed(e));
        sLatValueFld.setText(Double.toString(sLat));
        sLatValueFld.setPreferredSize(new Dimension(40, 20));
        sLatValueFld.setHorizontalAlignment(4);

        printButton.setText("Print");
        printButton.addActionListener(e -> print_actionPerformed(e));
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(evt -> {
            int slider = ((JSlider) evt.getSource()).getValue();
            double zoom = 50D / (double) slider;
            zoomValueFld.setText(Double.toString(zoom));
            chartPanel.applyZoom(zoom);
        });
        zoomValueFld.setText("1");
        zoomValueFld.setPreferredSize(new Dimension(40, 20));
        zoomValueFld.setHorizontalAlignment(4);
        zoomValueFld.addActionListener(e -> zoomValueFld_actionPerformed(e));
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(label, null);
        bottomPanel.add(sLatValueFld, null);
        bottomPanel.add(updateButton, null);
        bottomPanel.add(printButton, null);
        bottomPanel.add(undoButton, null);
        bottomPanel.add(zoomSlider, null);
        bottomPanel.add(zoomValueFld, null);
        add(bottomPanel, BorderLayout.SOUTH);

        double wLong = -146D;

        double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(1.0D);
        chartPanel.setVerticalGridInterval(1.0D);
        chartPanel.setWithScale(true);
        chartPanel.setPositionToolTipEnabled(true);
        chartPanel.setWithLngLabels(false);          // Because this is a canvas
        chartPanel.setWithInvertedLabels(true);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    private void update_actionPerformed(ActionEvent e) {
        try {
            double _sLat = Double.parseDouble(sLatValueFld.getText());
            sLat = _sLat;
            nLat = sLat + 6D;
            chartPanel.zoomIn();
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void undo_actionPerformed(ActionEvent e) {
        chartPanel.undoLastHandDrawing();
    }

    private void print_actionPerformed(ActionEvent e) {
        chartPanel.printGraphics();
    }

    public void chartPanelPaintComponent(Graphics gr) {
//    if(spatial == null)
//      spatial = new Spatial();
//    spatial.drawChart(chartPanel, gr);
        if (from != null && to != null) {
            drawRhumbLine(gr, from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
            plotGreatCircle(gr, from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
        }
        boolean title = false;
        if (title) {
            Font font = gr.getFont();
            gr.setFont(new Font(font.getName(), Font.BOLD, font.getSize() * 2));
            gr.drawString("Mercator Template - Latitude 21 to 27", 20, 30);
            gr.setFont(font);
        }

        GeoPoint drOne = new GeoPoint(GeomUtil.sexToDec("24", "58.5"),
                -GeomUtil.sexToDec("142", "01.9"));
        Point ptOne = chartPanel.getPanelPoint(drOne.getLatitude(),
                drOne.getLongitude());
        chartPanel.plotLOP(gr, drOne, 80D, 1.617, "Sun");
        chartPanel.postit(gr, "Log:99.63", ptOne.x, ptOne.y, Color.yellow);

        GeoPoint drTwo = new GeoPoint(GeomUtil.sexToDec("24", "55.9"),
                -GeomUtil.sexToDec("142", "28.1"));
        Point ptTwo = chartPanel.getPanelPoint(drTwo.getLatitude(),
                drTwo.getLongitude());
        chartPanel.plotLOP(gr, drTwo, 102D, 0.571, "Sun");
        chartPanel.postit(gr, "Log:123.48", ptTwo.x, ptTwo.y, Color.yellow);

        GeoPoint drThree = new GeoPoint(GeomUtil.sexToDec("24", "50.8"),
                -GeomUtil.sexToDec("143", "02.4"));
        Point ptThree = chartPanel.getPanelPoint(drThree.getLatitude(),
                drThree.getLongitude());
        chartPanel.plotLOP(gr, drThree, 276D, 0.647, "Sun");
        chartPanel.postit(gr, "Log:155.26", ptThree.x, ptThree.y, Color.yellow);

        boolean tellMe = false;
        if (tellMe) {
            GreatCircle gc = new GreatCircle();
            gc.setStart(new GreatCirclePoint(drOne));
            gc.setArrival(new GreatCirclePoint(drTwo));
            // gc.calculateRhumbLine();
            double dist = gc.getDistance() * 60.0;
            double heading = Math.toDegrees(gc.calculateRhumbLineRoute());
            System.out.println("1-2: heading " + heading + ", dist:" + dist);
            gc.setStart(new GreatCirclePoint(drTwo));
            gc.setArrival(new GreatCirclePoint(drThree));
            // gc.calculateRhumbLine();
            dist = gc.getDistance() * 60.0;
            heading = Math.toDegrees(gc.calculateRhumbLineRoute());
            System.out.println("2-3: heading " + heading + ", dist:" + dist);
        }
        // Now translate
        float alpha = 0.30f;
        ((Graphics2D) gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // First translation: 25.9 miles in 264t
        GreatCirclePoint dr = GreatCircle.dr(new GreatCirclePoint(drOne), 25.9, 264);
        GeoPoint forecastOne = new GeoPoint(Math.toDegrees(dr.getL()), Math.toDegrees(dr.getG()));  // TODO toDegrees?
        Point gpfOne = chartPanel.getPanelPoint(forecastOne.getLatitude(), forecastOne.getLongitude());
        chartPanel.plotLOP(gr, forecastOne, 80D, 1.617, "Sun");
        chartPanel.postit(gr, "Log:99.63", gpfOne.x, gpfOne.y, Color.yellow);

        // Second translation: 33.6 miles in 261t
        dr = GreatCircle.dr(new GreatCirclePoint(drTwo), 33.6, 261);
        GeoPoint forecastTwo = new GeoPoint(Math.toDegrees(dr.getL()), Math.toDegrees(dr.getG()));
        Point gpfTwo = chartPanel.getPanelPoint(forecastTwo.getLatitude(), forecastTwo.getLongitude());
        chartPanel.plotLOP(gr, forecastTwo, 102D, 0.571, "Sun");
        chartPanel.postit(gr, "Log:123.48", gpfTwo.x, gpfTwo.y, Color.yellow);
        // First again
        dr = GreatCircle.dr(new GreatCirclePoint(forecastOne), 33.6, 261);
        GeoPoint forecastOneTwo = new GeoPoint(Math.toDegrees(dr.getL()), Math.toDegrees(dr.getG()));
        Point gpfOneTwo = chartPanel.getPanelPoint(forecastOneTwo.getLatitude(), forecastOneTwo.getLongitude());
        chartPanel.plotLOP(gr, forecastOneTwo, 80D, 1.617, "Sun");
        chartPanel.postit(gr, "Log:99.63", gpfTwo.x, gpfTwo.y, Color.yellow);

        alpha = 1.0f;
        ((Graphics2D) gr).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf,
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
//    g.drawOval(b.x - 2, b.y - 2, 4, 4);
            if (previous != null) {
                Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }
    }

    public boolean onEvent(EventObject e, int type) {
        if (type == 0) {
            MouseEvent me = (MouseEvent) e;
            int x = me.getX();
            int y = me.getY();
            if (to != null) {
                from = to = null;
            }
            GeoPoint gp = chartPanel.getGeoPos(x, y);
            if (from == null) {
                from = gp;
            } else {
                to = gp;
                GreatCircle gc = new GreatCircle();
                gc.setStart(new GreatCirclePoint(from));
                gc.setArrival(new GreatCirclePoint(to));
                gc.calculateGreatCircle(20);
                double gcDist = gc.getDistance();
                // gc.calculateRhumbLine();
                double rlDist = gc.calculateRhumbLineDistance();
                double rlZ = gc.calculateRhumbLineRoute();
                DecimalFormat df = new DecimalFormat("##0.00");
                String mess = "GC:" + df.format(Math.toDegrees(gcDist * 60D)) + "'\n" + "RL:" + df.format(rlDist) + "'\n" + " Z:" + df.format(Math.toDegrees(rlZ)) + " true";
                JOptionPane.showMessageDialog(this, mess, "Route", 1);
            }
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