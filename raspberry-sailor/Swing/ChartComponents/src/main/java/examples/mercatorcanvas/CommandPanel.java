package examples.mercatorcanvas;

import calc.GeoPoint;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.GreatCircleWayPoint;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel
        extends JPanel
        implements ChartPanelParentInterface {
    private final static double CHART_LATITUDE_SPAN = 4d;
    private final static int CANVAS_WIDTH = 650; // Seems to match Letter format (8.5" x 11")
    private final static int CANVAS_HEIGHT = 488;

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    private JButton automateButton;

    private JButton setWidthButton;
    private JButton printButton;
    private JSlider zoomSlider;
    private JTextField zoomValueFld;
    private final static DecimalFormat DF22 = new DecimalFormat("#0.00");
    private JLabel label = new JLabel("South Lat:");
    private JTextField sLatValueFld;
    private JTextField widthFld;

    private JButton updateButton;
    //private Spatial spatial;
    private GeoPoint from;
    private GeoPoint to;

    private double sLat = -2D;
    private double nLat = sLat + CHART_LATITUDE_SPAN;

    SampleFrame parent = null;

    public CommandPanel(SampleFrame caller) {
        parent = caller;

        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this, CANVAS_WIDTH, CANVAS_HEIGHT);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        setWidthButton = new JButton();

        updateButton = new JButton();
        printButton = new JButton();
        sLatValueFld = new JTextField();
        widthFld = new JTextField();
        widthFld.setText(Integer.toString(CANVAS_WIDTH));

        zoomSlider = new JSlider();
        zoomValueFld = new JTextField();

        automateButton = new JButton("Auto");

//    spatial = null;
        from = null;
        to = null;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        setLayout(borderLayout1);
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        parent.setTitle(chartPanel.getWidth() + " x " + chartPanel.getHeight());

        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
                parent.setTitle(chartPanel.getW() + " x " + chartPanel.getH());
            }

        });
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
                parent.setTitle(chartPanel.getW() + " x " + chartPanel.getH());
            }

        });

        setWidthButton.setText("Set Width");
        setWidthButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setWidth_actionPerformed(e);
            }
        });

        updateButton.setText("Update Chart");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update_actionPerformed(e);
            }
        });

        sLatValueFld.setText(Double.toString(sLat));
        sLatValueFld.setPreferredSize(new Dimension(40, 20));
        sLatValueFld.setHorizontalAlignment(4);

        printButton.setText("Print");
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                print_actionPerformed(e);
            }
        });
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {
                int slider = ((JSlider) evt.getSource()).getValue();
                double zoom = 50D / (double) slider;
                zoomValueFld.setText(DF22.format(zoom));
                chartPanel.applyZoom(zoom);
            }

        });
        zoomValueFld.setText("1");
        zoomValueFld.setPreferredSize(new Dimension(40, 20));
        zoomValueFld.setHorizontalAlignment(4);
        zoomValueFld.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                zoomValueFld_actionPerformed(e);
            }

        });
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(widthFld, null);
        bottomPanel.add(setWidthButton, null);

        bottomPanel.add(label, null);
        bottomPanel.add(sLatValueFld, null);
        bottomPanel.add(updateButton, null);
        bottomPanel.add(printButton, null);
        bottomPanel.add(zoomSlider, null);
        bottomPanel.add(zoomValueFld, null);

        automateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateImages();
            }
        });

        bottomPanel.add(automateButton, null);

        add(bottomPanel, BorderLayout.SOUTH);

        double wLong = 0D;

        double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(0.5);
        chartPanel.setVerticalGridInterval(0.5);
        chartPanel.setWithScale(true);
        chartPanel.setPositionToolTipEnabled(false);
        chartPanel.setWithLngLabels(false); // Because this is a canvas
        chartPanel.setWithInvertedLabels(true);

//  chartPanel.setZoomFactor(1.01);
    }

    private void generateImages() {
        genimage = true;
        Thread generator = new Thread() {
            public void run() {
                for (int i = -2; i <= 60; i += (CHART_LATITUDE_SPAN / 2)) {
                    sLatValueFld.setText(Integer.toString(i));
                    update();
                    setWidth();
                    print();
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception ignore) {
                    }
                }
            }
        };
        // TODO Disable all others
        generator.start();
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    private void update_actionPerformed(ActionEvent e) {
        update();
        setWidth();
    }

    private void update() {
        try {
            double _sLat = Double.parseDouble(sLatValueFld.getText());
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
            double saveZoom = chartPanel.getZoomFactor();
            double z = (double) w / (double) chartPanel.getWidth();
            chartPanel.setZoomFactor(z);
            chartPanel.zoomIn();
            chartPanel.setZoomFactor(saveZoom);
            parent.setTitle(chartPanel.getWidth() + " x " + chartPanel.getHeight());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean genimage = false;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_z");

    private void print_actionPerformed(ActionEvent e) {
        print();
    }

    private void print() {
        if (genimage) {
            String fname = "canvas_" + sdf.format(new Date());
            // Generate Image
            chartPanel.genImage(fname, "png");
            System.out.println(fname + ".png generated...");
        } else {
            // Print
            chartPanel.setPrintResize(false);
            chartPanel.printGraphics((double) chartPanel.getWidth(),
                    (double) chartPanel.getHeight());
        }
    }

    public void chartPanelPaintComponent(Graphics gr) {
//    if(spatial == null)
//      spatial = new Spatial();
//    spatial.drawChart(chartPanel, gr);
        if (from != null && to != null) {
            drawRhumbLine(gr, from.getL(), from.getG(), to.getL(), to.getG());
            plotGreatCircle(gr, from.getL(), from.getG(), to.getL(), to.getG());
        }
        boolean title = true;
        if (title) {
            Font font = gr.getFont();
            gr.setFont(new Font(font.getName(), Font.BOLD, font.getSize() * 2));
            String label = "Mercator Template - Latitude " + Integer.toString((int) sLat) + "\272 to " + Integer.toString((int) nLat) + "\272";
            gr.drawString(label, 20, 30);
            Graphics2D g2 = (Graphics2D) gr;
            // Upside-down
            AffineTransform oldTx = g2.getTransform();
            label = "Canevas Mercator - Latitude " + Integer.toString((int) sLat) + "\272 � " + Integer.toString((int) nLat) + "\272";
            int strWidth = gr.getFontMetrics(gr.getFont()).stringWidth(label);
            int strHeight = gr.getFont().getSize();

            AffineTransform ct = AffineTransform.getTranslateInstance(strWidth + 1, 30);
            g2.transform(ct);

            g2.transform(AffineTransform.getRotateInstance(Math.PI));
            gr.drawString(label, strWidth + 20 - chartPanel.getW(),
                    strHeight + 40 - chartPanel.getH());
            g2.setTransform(oldTx);

//    gr.drawString(label, 20, 30);
            // Reset
            gr.setFont(font);
        }
        if (gr instanceof Graphics2D && false) {
            Graphics2D g2 = (Graphics2D) gr;
            AffineTransform oldTx = g2.getTransform();
            g2.fillOval(190, 290, 20, 20);
            AffineTransform ct = AffineTransform.getTranslateInstance(200, 300);
            g2.transform(ct);
            int limit = 6;
            for (int i = 1; i <= limit; i++)
//    for (int i=0; i<limit; i++)
            {
                float ratio = (float) i / (float) limit;
                g2.transform(AffineTransform.getRotateInstance(Math.PI * (ratio - 1.0f)));
//      float alpha = ((i == limit)?1.0f:ratio/3);
//      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//      g2.drawString("Rotated", 150.0f, 50.0f);        
                g2.drawString("Rotated " + Double.toString(Math.toDegrees(Math.PI * (ratio - 1f))), 0, 0);
                System.out.println("Rotated " + Double.toString(Math.toDegrees(Math.PI * (ratio - 1f))));
                g2.setTransform(oldTx);
            }
        }
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
        gc.setStart(new GreatCirclePoint(Math.toRadians(ls), Math.toRadians(gs)));
        gc.setArrival(new GreatCirclePoint(Math.toRadians(lf), Math.toRadians(gf)));
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
            if (to != null)
                from = to = null;
            GeoPoint gp = chartPanel.getGeoPos(x, y);
            if (from == null) {
                from = gp;
            } else {
                to = gp;
                GreatCircle gc = new GreatCircle();
                gc.setStart(new GreatCirclePoint(Math.toRadians(from.getL()), Math.toRadians(from.getG())));
                gc.setArrival(new GreatCirclePoint(Math.toRadians(to.getL()), Math.toRadians(to.getG())));
                gc.calculateGreatCircle(20);
                double gcDist = gc.getDistance();
                gc.calculateRhumbLine();
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
