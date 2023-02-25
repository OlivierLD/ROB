package grib.slug;

import grib.data.GribDate;
import grib.data.GribType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class SlugGRIB extends JFrame {
    private final static boolean verbose = false;

    private String dataType = null;
    private boolean withAxis = false;

    //private SlugGRIB instance = this;
    private Thread parentThread = Thread.currentThread();

    private Float[][] data2display = null;
    private Float[][] previousData2Display = null;

    private int smoothingSteps = 50;
    private int smoothingStep = -1;

    private HashMap<GribDate, HashMap<GribType, Float[][]>> bigmap;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel bottomPanel = new JPanel();
    // See the paintComponent method, all the skill is there
    private JPanel centerPanel = new JPanel() {
        public void paintComponent(Graphics g) {
            synchronized (parentThread) {
                g.setColor(Color.white);
                g.fillRect(0, 0, this.getSize().width, this.getSize().height);

                if (data2display != null) {
                    // Display data2display
                    int height = data2display.length;
                    int width = data2display[0].length;

                    xOffset = width / 2D;
                    yOffset = height / 2D;

                    if (withAxis) {
                        // Axis
                        Point pt1 = spaceToPanel(5000 + xOffset, 0 + yOffset, 0 + valueOffset);
                        Point pt2 = spaceToPanel(-5000 + xOffset, 0 + yOffset, 0 + valueOffset);
                        g.setColor(Color.red);
                        g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                        Point ptStr = spaceToPanel(50 + xOffset, yOffset, valueOffset);
                        g.drawString("x", ptStr.x, ptStr.y);

                        pt1 = spaceToPanel(0 + xOffset, 5000 + yOffset, 0 + valueOffset);
                        pt2 = spaceToPanel(0 + xOffset, -5000 + yOffset, 0 + valueOffset);
                        g.setColor(Color.blue);
                        g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                        ptStr = spaceToPanel(xOffset, 50 + yOffset, valueOffset);
                        g.drawString("y", ptStr.x, ptStr.y);

                        pt1 = spaceToPanel(0 + xOffset, 0 + yOffset, 5000 + valueOffset);
                        pt2 = spaceToPanel(0 + xOffset, 0 + yOffset, -5000 + valueOffset);
                        g.setColor(Color.green);
                        g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                        ptStr = spaceToPanel(xOffset, yOffset, 50 + valueOffset);
                        g.drawString("z", ptStr.x, ptStr.y);
                    }

                    if (slug && previousData2Display != null) {
                        g.setColor(Color.blue);
                        g.drawString(Integer.toString(smoothingStep) + " / " + Integer.toString(smoothingSteps), 10, 15);
                    }
                    g.setColor(Color.black);
                    // H x W
                    if ((slug && previousData2Display != null) || !slug) {
                        for (int h = 0; h < height; h++) {
                            Point prevPoint = null;
                            for (int w = 0; w < width; w++) {
                                float value = data2display[h][w].floatValue();
                                if (previousData2Display != null && smoothingStep > -1) {
                                    try {
                                        float prevValue = previousData2Display[h][w].floatValue();
                                        value = prevValue + (smoothingStep * (value - prevValue) / smoothingSteps);
                                    } catch (ArrayIndexOutOfBoundsException aobe) {
                                        System.err.println("--- Oops ---");
                                        aobe.printStackTrace();
                                    }
                                }
                                // else {
                                //   if (verbose) System.out.println("*** No previousData2Display");
                                // }
                                // Point pt = new Point(w*10, (int)(value / 20f));
                                Point pt = spaceToPanel(w, h, value);
                                if (prevPoint != null) {
                                    g.drawLine(prevPoint.x, prevPoint.y, pt.x, pt.y);
                                    // System.out.println("Drawing "+ prevPoint.x + "/" + prevPoint.y + ", " + pt.x + "/" + pt.y);
                                }
                                prevPoint = pt;
                            }
                        }
                        // W x H
                        for (int w = 0; w < width; w++) {
                            Point prevPoint = null;
                            for (int h = 0; h < height; h++) {
                                float value = data2display[h][w].floatValue();
                                if (previousData2Display != null && smoothingStep > -1) {
                                    try {
                                        float prevValue = previousData2Display[h][w].floatValue();
                                        value = prevValue + (smoothingStep * (value - prevValue) / smoothingSteps);
                                    } catch (ArrayIndexOutOfBoundsException aobe) {
                                        System.err.println("--- Oops ---");
                                        aobe.printStackTrace();
                                    }
                                }
                                // Point pt = new Point(w*10, (int)(value / 20f));
                                Point pt = spaceToPanel(w, h, value);
                                if (prevPoint != null) {
                                    g.drawLine(prevPoint.x, prevPoint.y, pt.x, pt.y);
                                    // System.out.println("Drawing "+ prevPoint.x + "/" + prevPoint.y + ", " + pt.x + "/" + pt.y);
                                }
                                prevPoint = pt;
                            }
                        }
                    }
                    if (slug && previousData2Display == null) {
                        g.setColor(Color.red);
                        String str = "Rewinding...";
                        int fontSize = g.getFont().getSize();
                        int strWidth = g.getFontMetrics(g.getFont()).stringWidth(str);
                        g.drawString(str, (this.getSize().width / 2) - (strWidth / 2), (this.getSize().height / 2) - (fontSize) /* Above the middle */);
                    }
                    // TASK The chart would go here.
                }
                parentThread.notify();
            }
        }
    };
    private JButton processButton = new JButton();

    private double rotXY = 0D;
    private double rotXZ = 0D;
    private double rotYZ = 0D;

    private double zoom = 1D;
    private double valueCoeff = 0.30;
    private double coordCoeff = 10.0;

    private double valueOffset = 5500D;

    private double xOffset = 0D;
    private double yOffset = 0D;
    private JTextField valueCoeffTextField = new JTextField();
    private JTextField coordCoeffTextField = new JTextField();
    private JTextField dataOffsetTextField = new JTextField();

    private JTextField zoomTextField = new JTextField();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JSlider xzSlider = new JSlider();
    private JSlider xySlider = new JSlider();
    private JSlider yzSlider = new JSlider();

    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private JLabel jLabel4 = new JLabel();
    private JLabel jLabel5 = new JLabel();
    private JLabel jLabel6 = new JLabel();
    private JLabel jLabel7 = new JLabel();
    private JLabel jLabel8 = new JLabel();
    private JButton slugButton = new JButton();
    private JComboBox dataTypeComboBox = new JComboBox();
    private JCheckBox axisCheckBox = new JCheckBox();

    private final static boolean OPTICAL = false;

    private JSlider speedSlider = new JSlider();

    private Point spaceToPanel(double x, double y, double z) {
        double centerPanelWidth = centerPanel.getSize().getWidth();
        double centerPanelHeight = centerPanel.getSize().getHeight();

        double workingX = (x - xOffset);
        double workingY = (y - yOffset);
        double workingZ = (z - valueOffset);

        workingX = workingX * coordCoeff * zoom;
        workingY = workingY * coordCoeff * zoom;

        workingZ = workingZ * valueCoeff * zoom;

        // Rotations, in this order (see matrix below)
        double alfa = Math.toRadians(rotYZ); // in plan (y, z), around x (x unchanged)
        double beta = Math.toRadians(rotXZ); // in plan (x, z), around y (y unchanged)
        double gamma = Math.toRadians(rotXY); // in plan (x, y), around z (z unchanged)
        double sinA = Math.sin(alfa),
                sinB = Math.sin(beta),
                sinG = Math.sin(gamma),
                cosA = Math.cos(alfa),
                cosB = Math.cos(beta),
                cosG = Math.cos(gamma);
        /*
         *                   |  1   0      0   |   | cosB   0 -sinB |   | cosG  -sinG  0 |   | x |
         *  | _x, _y, _z | = |  0  cosA  -sinA | * |  0     1   0   | * | sinG   cosG  0 | * | y |
         *                   |  0  sinA   cosA |   | sinB   0  cosB |   |   0      0   1 |   | z |
         */
        // The 3 matrix multiplication above...
        double _x = (workingX * (cosB * cosG)) -
                (workingY * (cosB * sinG)) -
                (workingZ * sinB);

        double _y = (workingX * (-(sinA * sinB * cosG) + (cosA * sinG))) +
                (workingY * ((sinA * sinB * sinG) + (cosA * cosG))) -
                (workingZ * (sinA * cosB));

        double _z = (workingX * ((cosA * sinB * cosG) + (sinA * sinG))) +
                (workingY * (-(cosA * sinB * sinG) + (sinA * cosG))) +
                (workingZ * (cosA * cosB));

        if (OPTICAL) {
            double ex = 10, ey = 10, ez = 10;

            try {
                _x = (_x - ex) * (ez / _z);
                _y = (_y - ey) * (ez / _z);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        Point pt = new Point((int) ((centerPanelWidth / 2D) + _x),
                (int) ((centerPanelHeight / 2D) - _z));  // Notice the minus Z, for the JPanel
        return pt;
    }

    public SlugGRIB(HashMap<GribDate, HashMap<GribType, Float[][]>> bigmap) {
        this.bigmap = bigmap;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final String FRAME_TITLE_RADIX = "Slug ";

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);
        this.setSize(new Dimension(800, 600));
        this.setTitle(FRAME_TITLE_RADIX);
        bottomPanel.setLayout(gridBagLayout1);
        processButton.setText("Apply");
        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDisplayData();
                centerPanel.repaint();
            }
        });
        valueCoeffTextField.setPreferredSize(new Dimension(40, 20));
        valueCoeffTextField.setMinimumSize(new Dimension(40, 20));
        valueCoeffTextField.setText(Double.toString(valueCoeff));
        valueCoeffTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        coordCoeffTextField.setPreferredSize(new Dimension(40, 20));
        coordCoeffTextField.setMinimumSize(new Dimension(40, 20));
        coordCoeffTextField.setText(Double.toString(coordCoeff));
        coordCoeffTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        dataOffsetTextField.setPreferredSize(new Dimension(40, 20));
        dataOffsetTextField.setMinimumSize(new Dimension(40, 20));
        dataOffsetTextField.setText(Double.toString(valueOffset));
        dataOffsetTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        zoomTextField.setPreferredSize(new Dimension(40, 20));
        zoomTextField.setMinimumSize(new Dimension(40, 20));
        zoomTextField.setText(Double.toString(zoom));
        zoomTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        zoomTextField.setToolTipText("Zoom");

        xzSlider.setMajorTickSpacing(45);
        xzSlider.setMaximum(180);
        xzSlider.setMinimum(-180);
        xzSlider.setMinorTickSpacing(5);
        xzSlider.setValue(0);
        xzSlider.setPaintTicks(true);
        xzSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JSlider slider = (JSlider) evt.getSource();

                //   if (!slider.getValueIsAdjusting())
                {
                    int value = slider.getValue();
                    rotXZ = value;
                    xzSlider.setToolTipText(Integer.toString(value));
                    centerPanel.repaint();
                }
            }
        });

        xySlider.setMaximum(180);
        xySlider.setMinimum(-180);
        xySlider.setMajorTickSpacing(45);
        xySlider.setMinorTickSpacing(5);
        xySlider.setValue(0);
        xySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JSlider slider = (JSlider) evt.getSource();

//      if (!slider.getValueIsAdjusting()) 
                {
                    int value = slider.getValue();
                    rotXY = value;
                    xySlider.setToolTipText(Integer.toString(value));
                    centerPanel.repaint();
                }
            }
        });

        yzSlider.setMajorTickSpacing(45);
        yzSlider.setMaximum(180);
        yzSlider.setMinimum(-180);
        yzSlider.setMinorTickSpacing(5);
        yzSlider.setValue(0);
        yzSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JSlider slider = (JSlider) evt.getSource();

//      if (!slider.getValueIsAdjusting()) 
                {
                    int value = slider.getValue();
                    rotYZ = value;
                    yzSlider.setToolTipText(Integer.toString(value));
                    centerPanel.repaint();
                }
            }
        });

        jLabel1.setText("XY");
        jLabel2.setText("YZ");
        jLabel3.setText("XZ");
        jLabel4.setText("Val. Coeff:");
        jLabel5.setText("Coord. Coeff:");
        jLabel6.setText("Data Offset:");
        jLabel7.setText("Zoom:");
        jLabel8.setText("Data");
        slugButton.setText("Slug");
        slugButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                slugButton_actionPerformed(e);
            }
        });
        dataTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataTypeComboBox_actionPerformed(e);
            }
        });
        axisCheckBox.setText("With axis");
        axisCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                axisCheckBox_actionPerformed(e);
            }
        });
        speedSlider.setToolTipText("Animation Smoothing");
        speedSlider.setMinimum(1);
        speedSlider.setEnabled(false);
        speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JSlider slider = (JSlider) evt.getSource();
                int value = slider.getValue();
                smoothingSteps = value;
                centerPanel.repaint();
            }
        });

        bottomPanel.add(valueCoeffTextField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(coordCoeffTextField,
                new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(dataOffsetTextField,
                new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(zoomTextField,
                new GridBagConstraints(7, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));

        bottomPanel.add(processButton,
                new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));

        bottomPanel.add(xzSlider, new GridBagConstraints(1, 4, 8, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(xySlider, new GridBagConstraints(1, 2, 8, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(yzSlider, new GridBagConstraints(1, 3, 8, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(jLabel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(jLabel2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(jLabel3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(jLabel4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(jLabel5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 0), 0, 0));
        bottomPanel.add(jLabel6, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 0), 0, 0));
        bottomPanel.add(jLabel7, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 0), 0, 0));
        bottomPanel.add(jLabel8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(slugButton, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(dataTypeComboBox,
                new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(axisCheckBox,
                new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        bottomPanel.add(speedSlider, new GridBagConstraints(5, 1, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        this.getContentPane().add(centerPanel, BorderLayout.CENTER);

        // List the data types
        SortedSet<GribDate> ss = new TreeSet<GribDate>(bigmap.keySet());
        for (GribDate d : ss) {
            HashMap<GribType, Float[][]> dMap = bigmap.get(d);
            SortedSet<GribType> type4date = new TreeSet<GribType>(dMap.keySet());
//    System.out.println(d.getGDate().toString() + " : " + type4date.size() + " type(s)");

            for (GribType t : type4date) {
                boolean found = false;
                for (int i = 0; i < dataTypeComboBox.getItemCount(); i++) {
                    if (t.getType().equals(dataTypeComboBox.getItemAt(i))) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    dataTypeComboBox.addItem(t.getType());
//      System.out.println("  For " + d + " and type " + t + " (" + t.getDesc() + ", " + t.getUnit() + ")");
            }
        }

        prepareData(null);
        centerPanel.repaint();
    }

    private void setDisplayData() {
        rotXZ = xzSlider.getValue();
        rotYZ = yzSlider.getValue();
        rotXY = xySlider.getValue();

        zoom = Double.parseDouble(zoomTextField.getText());
        valueCoeff = Double.parseDouble(valueCoeffTextField.getText());
        coordCoeff = Double.parseDouble(coordCoeffTextField.getText());

        valueOffset = Double.parseDouble(dataOffsetTextField.getText());
    }

    private GribDate prepareData(GribDate previous) {
        GribDate toReturn = null;

        boolean keepLooping = true;
        boolean okNextLoop = false;
        SortedSet<GribDate> ss = new TreeSet<GribDate>(bigmap.keySet());
        for (GribDate d : ss) {
            //    d.getWidth();
            //    d.getHeight();
            //    d.getStepx();
            //    d.getStepy();
            //    d.getTop();
            //    d.getBottom();
            //    d.getLeft();
            //    d.getRight();

            HashMap<GribType, Float[][]> dMap = bigmap.get(d);
            SortedSet<GribType> type4date = new TreeSet<GribType>(dMap.keySet());
            if (verbose) System.out.println(d.getGDate().toString() + " : " + type4date.size() + " type(s)");

            for (GribType t : type4date) {
                Float[][] data = dMap.get(t);
//      System.out.println("For " + d.toString() + " and type " + t + " (" + t.getDesc() + ", " + t.getUnit() + ")");          

                if (previous == null)
                    keepLooping = false;
                if (dataType == null || dataType.equals(t.getType())) {
                    data2display = data;
                    break;
                }
            }
            if (okNextLoop || previous == null) {
                setTitle(FRAME_TITLE_RADIX + d.getGDate().toString());
                toReturn = d;
            }
            if (!keepLooping || okNextLoop)
                break;
            if (d.equals(previous)) {
                if (verbose)
                    System.out.println("PreviousData2Display set to " + (data2display == null ? "null" : "not null"));
                previousData2Display = data2display;
                okNextLoop = true;
            }
        }
        if (toReturn == null) // Restart
        {
            if (verbose) System.out.println("Restarting...");
            previousData2Display = null;
        }
        centerPanel.repaint();

        return toReturn;
    }

    boolean slug = false;

    private void slugButton_actionPerformed(ActionEvent e) {
        if (!slug) {
            slugButton.setText("Stop");
            slug = true;
        } else {
            slugButton.setText("Slug");
            slug = false;
        }
        speedSlider.setEnabled(slug);
        if (slug) {
            Thread slugThread = new Thread() {
                public void run() {
                    GribDate prevDate = null;
                    while (slug) {
                        if (smoothingStep == -1 || smoothingStep >= smoothingSteps) {
                            smoothingStep = 0; // Reset
                            if (verbose)
                                System.out.println("*** Preparing Data (" + (prevDate != null ? prevDate.toString() : "-null-") + ") ***");
                            prevDate = prepareData(prevDate);
                        }
                        if (smoothingStep > 0 && smoothingStep < smoothingSteps) // && previousData2Display != null)
                        {
                            if (verbose) System.out.println("Tick... (" + smoothingStep + "/" + smoothingSteps + ")");
                            centerPanel.repaint();
                        }
                        try {
//              Thread.sleep(50L); 
                            synchronized (parentThread) {
                                parentThread.wait();
                            }
                        } catch (InterruptedException ie) {
                        }
//            if (verbose) System.out.println("Waiting...");
                        smoothingStep++;
                    }
                    if (verbose) System.out.println("Stop Slugging");
                    smoothingStep = -1;
                }
            };
            slugThread.start();
        }
    }

    private void dataTypeComboBox_actionPerformed(ActionEvent e) {
        dataType = (String) dataTypeComboBox.getSelectedItem();
        if (dataType.equals("hgt")) {
            valueCoeff = 0.30;
            valueOffset = 5500D;
        } else if (dataType.equals("htsgw")) {
            valueCoeff = 20D;
            valueOffset = 0D;
        } else if (dataType.equals("prate")) {
            valueCoeff = 50000D;
            valueOffset = 0D;
        } else if (dataType.equals("prmsl")) {
            valueCoeff = 0.05;
            valueOffset = 100000D;
        } else if (dataType.equals("tmp")) {
            valueCoeff = 2.5;
            valueOffset = 273D;
        } else {
            valueCoeff = 1D;
            valueOffset = 0D;
        }
        dataOffsetTextField.setText(Double.toString(valueOffset));
        valueCoeffTextField.setText(Double.toString(valueCoeff));
        prepareData(null);
    }

    private void axisCheckBox_actionPerformed(ActionEvent e) {
        withAxis = axisCheckBox.isSelected();
        centerPanel.repaint();
    }
}
