package chartview.gui.util.dialog;


import calc.GeoPoint;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.routing.DatedGribCondition;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GRIBSlicePanel
        extends JPanel {
    public final static int GRIB_SLICE_OPTION = 0;
    public final static int ROUTING_OPTION = 1;

    private int dataOption = -1;
    private List<Double> bsp = null;
    private List<Integer> hdg = null;
    private List<Integer> twa = null;
    private List<Double> smoothedBsp = null;
    private List<Integer> smoothedHdg = null;
    private List<Integer> smoothedTwa = null;
    private double bspMini = Double.MAX_VALUE;
    private double bspMaxi = Double.MIN_VALUE;

    private Date dateToPlot = null;

    private transient List<DatedGribCondition> originalData;
    private transient List<DatedGribCondition> data2plot;
    private transient List<DatedGribCondition> smoothedData;


    private final JPanel checkBoxPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            bspCheckBox.setVisible(dataOption == ROUTING_OPTION);
        }
    };
    private final JPanel checkBoxTopPanel = new JPanel();
    private final JPanel smoothFactorPanel = new JPanel();

    public final static int DEFAULT_FORK_WIDTH = 75;
    private int forkWidth = DEFAULT_FORK_WIDTH;

    private final GRIBSliceDataPanel dataPanel = new GRIBSliceDataPanel();

    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel dataLabel = new JLabel();

    private final JCheckBox prmslCheckBox = new JCheckBox();
    private final JCheckBox hgt500CheckBox = new JCheckBox();
    private final JCheckBox twsCheckBox = new JCheckBox();
    private final JCheckBox wavesCheckBox = new JCheckBox();
    private final JCheckBox tempCheckBox = new JCheckBox();
    private final JCheckBox rainCheckBox = new JCheckBox();
    private final JCheckBox bspCheckBox = new JCheckBox();
    private final JCheckBox hdgCheckBox = new JCheckBox();

    private boolean displayTWS = true,
            displayPRMSL = false,
            displayHGT500 = false,
            displayWAVES = false,
            displayTEMP = false,
            displayRAIN = false,
            displayBSP = false,
            displayHDG = false;

    private final JLabel smoothLabel = new JLabel();
    private final GridBagLayout gridBagLayout2 = new GridBagLayout();
    private final JTextField smoothTextField = new JTextField();

    private final static int tempUnit = Integer.parseInt(((ParamPanel.TemperatureUnitList) (ParamPanel.data[ParamData.TEMPERATURE_UNIT][ParamData.VALUE_INDEX])).getStringIndex());
    private final DecimalFormat speedFormat = new DecimalFormat("##0.0 'kts'");
    private final DecimalFormat prmslFormat = new DecimalFormat("##0.0 'mb'");
    private final DecimalFormat hgt500Format = new DecimalFormat("##0 'm'");
    private final DecimalFormat wavesFormat = new DecimalFormat("##0.0 'm'");
    private final DecimalFormat tempFormat = new DecimalFormat("##0'" + ParamPanel.TemperatureUnitList.getLabel(tempUnit) + "'");
    private final DecimalFormat prateFormat = new DecimalFormat("##0.00 'mm/h'");

    public GRIBSlicePanel(List<DatedGribCondition> data,
                          List<Double> bsp,
                          List<Integer> hdg,
                          List<Integer> twa,
                          int opt,
                          int fw) {
        if (fw % 2 == 0) {
            // throw new RuntimeException("Fork Width must be odd");
            // LOCALIZE
            JOptionPane.showMessageDialog(this, "Fork width must be odd.\nUsing " + Integer.toString(fw + 1) + " instead of " + Integer.toString(fw), "Smoothing", JOptionPane.ERROR_MESSAGE);
            forkWidth = fw + 1;
        } else
            forkWidth = fw;
        originalData = data;
        data2plot = data;
        this.bsp = bsp;
        this.hdg = hdg;
        this.twa = twa;
        dataOption = opt;

        // For tests
//    for (DatedGribCondition dgc : data) {
//      System.out.println("wave for " + dgc.getDate() + ":" + dgc.waves);
//    }

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GRIBSlicePanel(List<DatedGribCondition> data) {
        data2plot = data;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param data the GRIB
     * @Deprecated
     */
    public void setData(List<DatedGribCondition> data) {
        data2plot = data;
        computeData();
    }

    public void setData(List<DatedGribCondition> data,
                        List<Double> bsp,
                        List<Integer> hdg,
                        List<Integer> twa,
                        int opt) {
        data2plot = data;
        this.bsp = bsp;
        this.hdg = hdg;
        this.twa = twa;
        dataOption = opt;
        computeData();
    }

    private transient GribHelper.GribCondition gribMini = null;
    private transient GribHelper.GribCondition gribMaxi = null;

    public int[] getPointFromD(double d) // d goes from 0 to 1
    {
        int size = data2plot.size();
        double dIdx = d * size;
        if (dIdx < 0) {
            dIdx = 0;
        }
        if (dIdx > data2plot.size() - 1) {
            dIdx = data2plot.size() - 1;
        }
        int x = data2plot.get((int) dIdx).vertIdx;
        int y = data2plot.get((int) dIdx).horIdx;

        return new int[]{x, y};
    }

    private void jbInit() {
        this.setLayout(new BorderLayout());

        this.setSize(new Dimension(704, 159));
        this.setPreferredSize(new Dimension(700, 190));
        dataPanel.setLayout(null);
        dataPanel.setBackground(Color.white);
        dataPanel.setSize(new Dimension(640, 280));
        dataPanel.setPreferredSize(new Dimension(640, 280));

        dataLabel.setText("Data");
        dataLabel.setFont(new Font("Tahoma", Font.BOLD, 11));

        prmslCheckBox.setText("PRMSL");
        prmslCheckBox.setToolTipText("Pressure at Mean Sea Level");
        Color c = (Color) ParamPanel.data[ParamData.PRMSL_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        prmslCheckBox.setBackground(c);
        prmslCheckBox.setForeground(reverseColor(c));
        prmslCheckBox.addActionListener(this::prmslCheckBox_actionPerformed);
        hgt500CheckBox.setText("HGT500");
        hgt500CheckBox.setToolTipText("500mb Altitude");
        c = (Color) ParamPanel.data[ParamData.HGT500_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        hgt500CheckBox.setBackground(c);
        hgt500CheckBox.setForeground(reverseColor(c));
        hgt500CheckBox.addActionListener(this::hgt500CheckBox_actionPerformed);
        twsCheckBox.setText("TWS");
        twsCheckBox.setToolTipText("True Wind Speed");
        c = (Color) ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        twsCheckBox.setBackground(c);
        twsCheckBox.setForeground(reverseColor(c));
        twsCheckBox.addActionListener(this::twsCheckBox_actionPerformed);
        wavesCheckBox.setText("WAVES");
        wavesCheckBox.setToolTipText("Waves Height");
        wavesCheckBox.setBackground((Color) ParamPanel.data[ParamData.WAVES_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
        wavesCheckBox.addActionListener(this::wavesCheckBox_actionPerformed);
        tempCheckBox.setText("AIRTMP");
        tempCheckBox.setToolTipText("Air Temperature");
        c = (Color) ParamPanel.data[ParamData.AIRTMP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        tempCheckBox.setBackground(c);
        tempCheckBox.setForeground(reverseColor(c));
        tempCheckBox.addActionListener(this::tempCheckBox_actionPerformed);
        rainCheckBox.setText("RAIN");
        rainCheckBox.setToolTipText("Precipitation");
        c = (Color) ParamPanel.data[ParamData.RAIN_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        rainCheckBox.setBackground(c);
        rainCheckBox.setForeground(reverseColor(c));
        rainCheckBox.addActionListener(this::rainCheckBox_actionPerformed);

        bspCheckBox.setText("BSP");
        bspCheckBox.setToolTipText("Boat Speed");
        c = (Color) ParamPanel.data[ParamData.BSP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
        bspCheckBox.setBackground(c);
        bspCheckBox.setForeground(reverseColor(c));
        bspCheckBox.addActionListener(this::bspCheckBox_actionPerformed);

        hdgCheckBox.setText("HDG-TWD");
        hdgCheckBox.setToolTipText("Heading & True Wind Direction");
//    c = (Color)ParamPanel.data[ParamData.BSP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX];
//    bspCheckBox.setBackground(c);
        hdgCheckBox.setForeground(reverseColor(c));
        hdgCheckBox.addActionListener(this::hdgCheckBox_actionPerformed);

        twsCheckBox.setSelected(displayTWS);
        twsCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        prmslCheckBox.setSelected(displayPRMSL);
        prmslCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        hgt500CheckBox.setSelected(displayHGT500);
        hgt500CheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        wavesCheckBox.setSelected(displayWAVES);
        wavesCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        tempCheckBox.setSelected(displayTEMP);
        tempCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        rainCheckBox.setSelected(displayRAIN);
        rainCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        bspCheckBox.setSelected(displayBSP);
        bspCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));
        hdgCheckBox.setSelected(displayHDG);
        hdgCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 9));

        smoothLabel.setText("Smooth");
        smoothLabel.setFont(new Font("Tahoma", Font.PLAIN, 9));
        smoothTextField.setSize(new Dimension(30, 20));
        smoothTextField.setText(Integer.toString(forkWidth));
        smoothTextField.setPreferredSize(new Dimension(30, 20));
        smoothTextField.setFont(new Font("Tahoma", Font.PLAIN, 9));
        // System.out.println("Action performed!");
        smoothTextField.addActionListener(this::smoothTextField_actionPerformed);
        checkBoxPanel.setLayout(new BorderLayout());
        checkBoxTopPanel.setLayout(gridBagLayout1);
        smoothFactorPanel.setLayout(gridBagLayout2);
        checkBoxPanel.add(checkBoxTopPanel, BorderLayout.NORTH);
        smoothFactorPanel.add(smoothLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        smoothFactorPanel.add(smoothTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        checkBoxPanel.add(smoothFactorPanel, BorderLayout.SOUTH);

        this.add(dataPanel, BorderLayout.CENTER);
        this.add(checkBoxPanel, BorderLayout.EAST);

        checkBoxTopPanel.add(dataLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 3, 3, 0), 0, 0));
        checkBoxTopPanel.add(twsCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(prmslCheckBox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(hgt500CheckBox, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(wavesCheckBox, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(tempCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(rainCheckBox, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(bspCheckBox, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));
        checkBoxTopPanel.add(hdgCheckBox, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 3, 0, 0), 0, 0));

        ApplicationEventListener ael = new ApplicationEventListener() {
            public String toString() {
                return "from GRIBSlicePanel.";
            }

            @Override
            public void plotBoatAt(Date d, GeoPoint gp, int hdg) {
                dateToPlot = d;
                repaint();
            }
        };
        WWContext.getInstance().addApplicationListener(ael);

        computeData();
    }

    private void computeData() {
        // Is smoothing possible here (enough points in the array) ?
        if (data2plot.size() < 100) { // Generate new points, and smooth
            // Generate new points in data2smooth
            data2plot = expandArray(data2plot, 75);
            forkWidth = DEFAULT_FORK_WIDTH;
            smoothTextField.setText(Integer.toString(forkWidth));

            if (dataOption == ROUTING_OPTION) {
                bsp = expandBSPArray(bsp, 75);
                hdg = expandHDGArray(hdg, 75);
                twa = expandTWAArray(twa, 75);
            }
        }

        // Detect mini maxi
        gribMini = new GribHelper.GribCondition();
        gribMaxi = new GribHelper.GribCondition();
        gribMini.windspeed = Float.MAX_VALUE;
        gribMaxi.windspeed = 0f;
        gribMini.winddir = 360;
        gribMaxi.winddir = 0;
        gribMini.prmsl = Integer.MAX_VALUE;
        gribMaxi.prmsl = 0;
        gribMini.waves = Integer.MAX_VALUE;
        gribMaxi.waves = 0;
        gribMini.temp = Integer.MAX_VALUE;
        gribMaxi.temp = Integer.MIN_VALUE;
        gribMini.hgt500 = Integer.MAX_VALUE;
        gribMaxi.hgt500 = 0;
        gribMini.rain = Float.MAX_VALUE;
        gribMaxi.rain = 0f;
        int nbNull = 0;
//  System.out.println("Mini-maxi from " + data2plot.size() + " element(s)");
        for (GribHelper.GribCondition ghgc : data2plot) { // Mini/Maxi
            if (ghgc != null) {
                gribMini.windspeed = Math.min(gribMini.windspeed, ghgc.windspeed);
                gribMini.winddir = Math.min(gribMini.winddir, ghgc.winddir);
                gribMini.prmsl = Math.min(gribMini.prmsl, ghgc.prmsl);
                gribMini.waves = Math.min(gribMini.waves, ghgc.waves);
                gribMini.temp = Math.min(gribMini.temp, ghgc.temp);
                gribMini.hgt500 = Math.min(gribMini.hgt500, ghgc.hgt500);
                gribMini.rain = Math.min(gribMini.rain, ghgc.rain);

                gribMaxi.windspeed = Math.max(gribMaxi.windspeed, ghgc.windspeed);
                gribMaxi.winddir = Math.max(gribMaxi.winddir, ghgc.winddir);
                gribMaxi.prmsl = Math.max(gribMaxi.prmsl, ghgc.prmsl);
                gribMaxi.waves = Math.max(gribMaxi.waves, ghgc.waves);
                gribMaxi.temp = Math.max(gribMaxi.temp, ghgc.temp);
                gribMaxi.hgt500 = Math.max(gribMaxi.hgt500, ghgc.hgt500);
                gribMaxi.rain = Math.max(gribMaxi.rain, ghgc.rain);
            } else {
                nbNull++;
            }
        }
//  System.out.println("1 - Mini:" + (gribMini.rain * 3600f) + ", maxi:" + (gribMaxi.rain * 3600f));

        if (bsp != null) {
            for (Double d : bsp) {
                if (d < bspMini) {
                    bspMini = d;
                }
                if (d > bspMaxi) {
                    bspMaxi = d;
                }
            }
        }
        //  System.out.println("Will plot " + data2plot.size() + " point(s), " + nbNull + " null(s).");
        smooth(forkWidth);
    }

    private void smooth(int fork) {
        if ((fork % 2) != 1) {
            JOptionPane.showMessageDialog(this, "Fork width must be odd", "Smoothing", JOptionPane.ERROR_MESSAGE); // LOCALIZE
            throw new RuntimeException("Fork must be odd.");
        }

        List<DatedGribCondition> data2smooth = data2plot; // Clone array

        // New ArrayList
        smoothedData = new ArrayList<>(data2smooth.size());

        for (DatedGribCondition cond : data2smooth) { // Clone the array
            DatedGribCondition dgc = new DatedGribCondition(new GribHelper.GribCondition(cond.windspeed,
                    cond.winddir,
                    cond.hgt500,
                    cond.horIdx,
                    cond.vertIdx,
                    cond.prmsl,
                    cond.waves,
                    cond.temp,
                    cond.rain,
                    cond.currentspeed,
                    cond.currentdir));
            dgc.setDate(cond.getDate());
            smoothedData.add(dgc);
        }
        if (dataOption == ROUTING_OPTION) {
            smoothedBsp = new ArrayList<>(bsp.size());
            smoothedHdg = new ArrayList<>(hdg.size());
            smoothedTwa = new ArrayList<>(twa.size());
            smoothedBsp.addAll(bsp);
            smoothedHdg.addAll(hdg);
            smoothedTwa.addAll(twa);
        }
        int halfFork = ((fork - 1) / 2);
        for (int i = 0; i < data2smooth.size(); i++) {
            double tws = 0D;
            double hgt500 = 0D;
            double prmsl = 0D;
            double waves = 0D;
            double temp = 0D;
            double rain = 0D;

            double boatSpeed = 0D;
            double boatHeadingCos = 0D;
            double boatHeadingSin = 0D;
            double windAngleCos = 0D;
            double windAngleSin = 0D;

            for (int j = (i - halfFork); j <= (i + halfFork); j++) {
                int _j = j;
                if (_j < 0) {
                    _j = 0;
                }
                if (_j >= data2smooth.size()) {
                    _j = data2smooth.size() - 1;
                }
                tws += data2smooth.get(_j).windspeed;
                hgt500 += data2smooth.get(_j).hgt500;
                prmsl += data2smooth.get(_j).prmsl;
                waves += data2smooth.get(_j).waves;
                temp += data2smooth.get(_j).temp;
                rain += data2smooth.get(_j).rain;

                if (dataOption == ROUTING_OPTION) {
                    boatSpeed += bsp.get(_j);
                    boatHeadingCos += Math.cos(Math.toRadians(hdg.get(_j).doubleValue()));
                    boatHeadingSin += Math.sin(Math.toRadians(hdg.get(_j).doubleValue()));
                    windAngleCos += Math.cos(Math.toRadians(twa.get(_j)));
                    windAngleSin += Math.sin(Math.toRadians(twa.get(_j)));
                }
            }
            tws = tws / fork;
            hgt500 = hgt500 / fork;
            prmsl = prmsl / fork;
            waves = waves / fork;
            temp = temp / fork;
            rain = rain / fork;

            boatSpeed = boatSpeed / fork;
            boatHeadingCos = boatHeadingCos / fork;
            boatHeadingSin = boatHeadingSin / fork;
            windAngleCos = windAngleCos / fork;
            windAngleSin = windAngleSin / fork;
            if (dataOption == ROUTING_OPTION) {
                smoothedBsp.set(i, boatSpeed);
                smoothedHdg.set(i, (int) Math.toDegrees(WWGnlUtilities.getAngle(boatHeadingSin, boatHeadingCos)));
                smoothedTwa.set(i, (int) Math.toDegrees(WWGnlUtilities.getAngle(windAngleSin, windAngleCos)));
            }
            smoothedData.get(i).windspeed = (float) tws;
            smoothedData.get(i).hgt500 = (float) hgt500;
            smoothedData.get(i).prmsl = (int) prmsl;
            smoothedData.get(i).waves = (int) waves;
            smoothedData.get(i).temp = (float) temp;
            smoothedData.get(i).rain = (float) rain;
        }
//  return smoothed;    
    }

    private List<DatedGribCondition> expandArray(List<DatedGribCondition> origData, int smoothFactor) {
        List<DatedGribCondition> expanded = new ArrayList<>(origData.size() * smoothFactor);
        // Add points
        for (int i = 0; i < origData.size() - 1; i++) {
            double windspeedDeltaValue = origData.get(i + 1).windspeed - origData.get(i).windspeed;
            double hgt500dDeltaValue = origData.get(i + 1).hgt500 - origData.get(i).hgt500;
            double prmslDeltaValue = origData.get(i + 1).prmsl - origData.get(i).prmsl;
            double wavesDeltaValue = origData.get(i + 1).waves - origData.get(i).waves;
            double tempDeltaValue = origData.get(i + 1).temp - origData.get(i).temp;
            double rainDeltaValue = origData.get(i + 1).rain - origData.get(i).rain;
            double currentspeedDeltaValue = origData.get(i + 1).currentspeed - origData.get(i).currentspeed;

            Date d1 = origData.get(i + 1).getDate();
            Date d2 = origData.get(i).getDate();
            long longDeltaDate = -1L;
            if (d1 != null && d2 != null) {
                longDeltaDate = d1.getTime() - d2.getTime();
            }
            for (int j = 0; j < smoothFactor; j++) {
                double windSpeedValue = origData.get(i).windspeed + (windspeedDeltaValue * ((double) j / (double) smoothFactor));
                double hgt500Value = origData.get(i).hgt500 + (hgt500dDeltaValue * ((double) j / (double) smoothFactor));
                double prmslValue = origData.get(i).prmsl + (prmslDeltaValue * ((double) j / (double) smoothFactor));
                double wavesValue = origData.get(i).waves + (wavesDeltaValue * ((double) j / (double) smoothFactor));
                double tempValue = origData.get(i).temp + (tempDeltaValue * ((double) j / (double) smoothFactor));
                double rainValue = origData.get(i).rain + (rainDeltaValue * ((double) j / (double) smoothFactor));
                double currentSpeedValue = origData.get(i).currentspeed + (currentspeedDeltaValue * ((double) j / (double) smoothFactor));

                GribHelper.GribCondition gc = new GribHelper.GribCondition((float) windSpeedValue,
                        origData.get(i + 1).winddir, // No smooth
                        (float) hgt500Value,
                        origData.get(i + 1).horIdx,  // no smooth
                        origData.get(i + 1).vertIdx, // no smooth
                        (float) prmslValue,
                        (float) wavesValue,
                        (float) tempValue,
                        (float) rainValue,
                        (float) currentSpeedValue,
                        origData.get(i + 1).currentdir); // no smooth
                DatedGribCondition dgc = new DatedGribCondition(gc);
                if (longDeltaDate != -1L) {
                    long dateValue = (long) (d1.getTime() + (longDeltaDate * ((double) j / (double) smoothFactor)));
                    dgc.setDate(new Date(dateValue));
                }
                expanded.add(dgc);
            }
        }
        return expanded;
    }

    private List<Double> expandBSPArray(List<Double> origData, int smoothFactor) {
        List<Double> expanded = new ArrayList<>(origData.size() * smoothFactor);
        // Add points
        for (int i = 0; i < origData.size() - 1; i++) {
            double bspDeltaValue = origData.get(i + 1) - origData.get(i);

            for (int j = 0; j < smoothFactor; j++) {
                double bspValue = origData.get(i) + (bspDeltaValue * ((double) j / (double) smoothFactor));
                expanded.add(bspValue);
            }
        }
        return expanded;
    }

    private List<Integer> expandTWAArray(List<Integer> origData, int smoothFactor) {
        List<Integer> expanded = new ArrayList<>(origData.size() * smoothFactor);
        // Add points
        for (int i = 0; i < origData.size() - 1; i++) {
            double twaDeltaValue = origData.get(i + 1).doubleValue() - origData.get(i).doubleValue();

            for (int j = 0; j < smoothFactor; j++) {
                double twaValue = origData.get(i).doubleValue() + (twaDeltaValue * ((double) j / (double) smoothFactor));
                expanded.add((int) twaValue);
            }
        }
        return expanded;
    }

    // TODO sin & cos
    private List<Integer> expandHDGArray(List<Integer> origData, int smoothFactor) {
        List<Integer> expanded = new ArrayList<>(origData.size() * smoothFactor);
        // Add points
        for (int i = 0; i < origData.size() - 1; i++) {
            double hdgDeltaValue = origData.get(i + 1).doubleValue() - origData.get(i).doubleValue();

            for (int j = 0; j < smoothFactor; j++) {
                double twaValue = origData.get(i).doubleValue() + (hdgDeltaValue * ((double) j / (double) smoothFactor));
                expanded.add((int) twaValue);
            }
        }
        return expanded;
    }

    private void twsCheckBox_actionPerformed(ActionEvent e) {
        displayTWS = twsCheckBox.isSelected();
        repaint();
    }

    private void prmslCheckBox_actionPerformed(ActionEvent e) {
        displayPRMSL = prmslCheckBox.isSelected();
        repaint();
    }

    private void hgt500CheckBox_actionPerformed(ActionEvent e) {
        displayHGT500 = hgt500CheckBox.isSelected();
        repaint();
    }

    private void wavesCheckBox_actionPerformed(ActionEvent e) {
        displayWAVES = wavesCheckBox.isSelected();
        repaint();
    }

    private void tempCheckBox_actionPerformed(ActionEvent e) {
        displayTEMP = tempCheckBox.isSelected();
        repaint();
    }

    private void rainCheckBox_actionPerformed(ActionEvent e) {
        displayRAIN = rainCheckBox.isSelected();
        repaint();
    }

    private void bspCheckBox_actionPerformed(ActionEvent e) {
        displayBSP = bspCheckBox.isSelected();
        repaint();
    }

    private void hdgCheckBox_actionPerformed(ActionEvent e) {
        displayHDG = hdgCheckBox.isSelected();
        repaint();
    }

    private void smoothTextField_actionPerformed(ActionEvent e) {
        try {
            int fw = Integer.parseInt(smoothTextField.getText());
            if (fw % 2 == 1) {
                forkWidth = fw;
                smooth(forkWidth);
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Fork width must be odd", "Smoothing", JOptionPane.ERROR_MESSAGE);
                smoothTextField.setText(Integer.toString(forkWidth));
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, nfe.toString(), "Smoothing", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setForkWidth(int forkWidth) {
        System.out.println("Setting forkWidth to " + forkWidth);
        if (forkWidth % 2 == 1) {
            this.forkWidth = forkWidth;
            smooth(forkWidth);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Fork width must be odd", "Smoothing", JOptionPane.ERROR_MESSAGE);
        }
        smoothTextField.setText(Integer.toString(this.forkWidth));
        smoothTextField.repaint();
    }

    public void setDataOption(int dataOption) {
        this.dataOption = dataOption;
    }

    public void setBsp(List<Double> bsp) {
        this.bsp = bsp;
    }

    private Color reverseColor(Color c) {
        Color reversed = new Color(255 - c.getRed(),
                255 - c.getGreen(),
                255 - c.getBlue());
        return reversed;
    }


    private class GRIBSliceDataPanel extends JPanel {
        private int infoX = -1;

        public GRIBSliceDataPanel() {
            super();
            this.addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                    int x = e.getPoint().x;
                    infoX = x;
                    double pos = (double) x / (double) getWidth();
                    WWContext.getInstance().fireGRIBSliceInfoRequested(pos);
                    repaint();
                }

                public void mouseMoved(MouseEvent e) {
                }
            });
            this.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                    int x = e.getPoint().x;
                    infoX = x;
                    double pos = (double) x / (double) getWidth();
                    WWContext.getInstance().fireGRIBSliceInfoRequested(pos);
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    infoX = -1;
                    WWContext.getInstance().fireGRIBSliceInfoRequestStop();
                    repaint();
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            });
        }

        public void paintComponent(Graphics gr) {
            ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D) gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(0, this.getHeight(), Color.lightGray, 0, 0, Color.white); // vertical, upside down
            ((Graphics2D) gr).setPaint(gradient);
//    gr.setColor(Color.white);
            gr.fillRect(0, 0, this.getWidth(), this.getHeight());
            // Horizontal lines
            gr.setColor(Color.gray);
            for (int i = 0; i < this.getHeight(); i += (this.getHeight() / 10)) {
                gr.drawLine(0, i, this.getWidth(), i);
            }
            Stroke origStroke = null;

            // Calculate Data scales...
            float windscale = (float) this.getHeight() / gribMaxi.windspeed;
            float prmslscale = (float) this.getHeight() / ((gribMaxi.prmsl / 100f) - (gribMini.prmsl / 100f));
            float hgt500scale = (float) this.getHeight() / ((gribMaxi.hgt500) - (gribMini.hgt500));
            float wavescale = (float) this.getHeight() / (gribMaxi.waves / 100f);
            float tempscale = (float) this.getHeight() / ((gribMaxi.temp) - (gribMini.temp));
            float rainscale = (float) this.getHeight() / (gribMaxi.rain * 3600f);

//    System.out.println("2 - RainScale:" + rainscale + ", mini:" + (gribMini.rain * 3600f) + ", maxi:" + (gribMaxi.rain * 3600f));

//    float dirScale    = (float)this.getHeight() / 360f;

            float bspscale = 1f;
            if (dataOption == ROUTING_OPTION) {
                bspscale = (float) this.getHeight() / (float) bspMaxi;
            }
            // Plot
            int gribSize = data2plot.size();

            // Display Raw data
            if (gr instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) gr;
                origStroke = g2d.getStroke();
                Stroke stroke = new BasicStroke(1,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL);
                g2d.setStroke(stroke);
            }
            drawDataArray(gr, data2plot, windscale, prmslscale, hgt500scale, wavescale, tempscale, rainscale);
            if (dataOption == ROUTING_OPTION) {
                // Vertical grid, for each isochron
                if (true) {
                    Color _orig = gr.getColor();
                    //   System.out.println("Plotting " + originalData.size() + " isochrons");
                    int size = originalData.size() - 1;
                    for (int idx = 0; idx < size; idx++) {
                        int x = (int) ((float) idx * (float) this.getWidth() / (float) size);
                        gr.setColor(Color.lightGray);
                        gr.drawLine(x, 0, x, this.getHeight());
                        gr.setColor(Color.black);
                        String s = Integer.toString(idx);
                        int l = gr.getFontMetrics(gr.getFont()).stringWidth(s);
                        gr.drawString(s, x - (l / 2), this.getHeight() - 2);
                    }
                    gr.setColor(_orig);
                }
                // Corresponding position of the boat on the chart
                if (dateToPlot != null) {
                    Color orig = gr.getColor();
                    gr.setColor(Color.pink);
                    Stroke beforeStroke = null;
                    if (gr instanceof Graphics2D) {
                        Graphics2D g2d = (Graphics2D) gr;
                        beforeStroke = g2d.getStroke();
                        Stroke stroke = new BasicStroke(2,
                                BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_BEVEL);
                        g2d.setStroke(stroke);
                    }

                    int idx = 0;
                    for (DatedGribCondition dgdc : smoothedData) {
                        if (dgdc.getDate().after(dateToPlot)) {
                            break;
                        }
                        idx++;
                    }
                    int x = (int) (idx / ((float) gribSize / (float) this.getWidth()));
                    gr.drawLine(x, 0, x, this.getHeight());
                    gr.setColor(orig);
                    if (beforeStroke != null) {
                        ((Graphics2D) gr).setStroke(beforeStroke);
                    }
                }
                drawBoatSpeed(gr, bsp, bspscale);
//      drawTWA(gr, twa, dirScale);
            }
            // Display smoothed data
            if (gr instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) gr;
                origStroke = g2d.getStroke();
                Stroke stroke = new BasicStroke(3,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL);
                g2d.setStroke(stroke);
            }
            drawDataArray(gr, smoothedData, windscale, prmslscale, hgt500scale, wavescale, tempscale, rainscale);
            if (dataOption == ROUTING_OPTION) {
                drawBoatSpeed(gr, smoothedBsp, bspscale);
//      drawTWA(gr, smoothedTwa, dirScale);
                if (displayHDG) {
                    for (int idx = 0; idx <= 10; idx++) {
                        int x = idx * (this.getWidth() / 10);
                        int dataIdx = (int) ((float) x * (float) gribSize / (float) this.getWidth());
                        if (dataIdx > smoothedData.size() - 1) {
                            dataIdx = smoothedData.size() - 1;
                        }
                        if (dataIdx < 0) {
                            dataIdx = 0;
                        }
                        Integer heading = smoothedHdg.get(dataIdx);
                        Integer windAngle = smoothedTwa.get(dataIdx);
                        // Draw the boat with TWA
                        Point boatCenter = new Point(x, (this.getHeight() / 2));
                        WWGnlUtilities.drawBoat((Graphics2D) gr,
                                Color.LIGHT_GRAY,
                                boatCenter,             // Pos on the Panel
                                (this.getHeight() / 4), // Boat Length
                                heading,                // Heading
                                0.5f);                  // Alpha
                        // Now, the wind
                        Color c = gr.getColor();
                        WWGnlUtilities.drawTWAOverBoat((Graphics2D) gr,
                                (this.getHeight() / 8), // Hand Length
                                boatCenter,
                                heading - windAngle, // TODO See why -windAngle
                                Color.GRAY);
                    }
                }
            }
            if (infoX != -1) { // Mouse is pressed
                gr.setColor(Color.gray);
                gr.drawLine(infoX, 0, infoX, this.getHeight());

                int dataIdx = (int) ((float) infoX * (float) gribSize / (float) this.getWidth());
//      System.out.println("GRIB Idx:" + dataIdx + " pour x:" + infoX + " / " + this.getWidth());
//      GribHelper.GribCondition gribPoint = data2plot.get(dataIdx);
                if (dataIdx > smoothedData.size() - 1) {
                    dataIdx = smoothedData.size() - 1;
                }
                if (dataIdx < 0) {
                    dataIdx = 0;
                }
                DatedGribCondition gribPoint = smoothedData.get(dataIdx);
                Date date = gribPoint.getDate();
                if (date != null) {
                    String s = WWGnlUtilities.SDF_UT_3.format(date);
                    postit(gr, s, infoX, this.getHeight() - 12, Color.yellow, Color.blue, 0.75f);
                }

                Double boatSpeed = null;
                Integer windAngle = null;
                Integer heading = null;
                if (dataOption == ROUTING_OPTION) {
                    boatSpeed = smoothedBsp.get(dataIdx);
                    heading = smoothedHdg.get(dataIdx);
                    windAngle = smoothedTwa.get(dataIdx);
                }
                if (displayTWS && !Float.isInfinite(windscale)) {
                    int y = (int) (this.getHeight() - (gribPoint.windspeed * windscale));
                    postit(gr,
                            " " + speedFormat.format(gribPoint.windspeed),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);

//          y = (int)(this.getHeight() - (windAngle * dirScale));
//          postit(gr, 
//                 " TWA:" + windAngle + "\272",
//                 infoX, y, 
//                 (Color)ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX], 
//                 reverseColor((Color)ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]), 
//                 0.75f);
                }
                if (displayPRMSL && !Float.isInfinite(prmslscale)) {
                    int y = (int) (this.getHeight() - (((gribPoint.prmsl / 100f) - (gribMini.prmsl / 100f)) * prmslscale));
                    postit(gr,
                            " " + prmslFormat.format(gribPoint.prmsl / 100f),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.PRMSL_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.PRMSL_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (displayHGT500 && !Float.isInfinite(hgt500scale)) {
                    int y = (int) (this.getHeight() - ((gribPoint.hgt500 - (gribMini.hgt500)) * hgt500scale));
                    postit(gr,
                            " " + hgt500Format.format(gribPoint.hgt500),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.HGT500_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.HGT500_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (displayWAVES && !Float.isInfinite(wavescale)) {
                    int y = (int) (this.getHeight() - ((gribPoint.waves / 100f) * wavescale));
                    postit(gr,
                            " " + wavesFormat.format(gribPoint.waves / 100f),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.WAVES_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.WAVES_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (displayTEMP && !Float.isInfinite(tempscale)) {
                    int y = (int) (this.getHeight() - (((gribPoint.temp - 273) - (gribMini.temp - 273)) * tempscale));
                    postit(gr,
                            " " + tempFormat.format(WWGnlUtilities.convertTemperatureFromKelvin(gribPoint.temp, tempUnit)),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.AIRTMP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.AIRTMP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (displayRAIN && !Float.isInfinite(rainscale)) {
//        System.out.println("-> Rain (1) " + infoX + ":" + (gribPoint.rain * 3600f));
                    int y = (int) (this.getHeight() - (((gribPoint.rain * 3600f) /* - (gribMini.rain * 3600f) */) * rainscale));
                    postit(gr,
                            " " + prateFormat.format(gribPoint.rain * 3600f),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.RAIN_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.RAIN_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (displayBSP && !Float.isInfinite(bspscale)) {
                    int y = (int) (this.getHeight() - (boatSpeed * bspscale));
                    postit(gr,
                            " " + speedFormat.format(boatSpeed.doubleValue()),
                            infoX, y,
                            (Color) ParamPanel.data[ParamData.BSP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX],
                            reverseColor((Color) ParamPanel.data[ParamData.BSP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]),
                            0.75f);
                }
                if (dataOption == ROUTING_OPTION && displayTWS) {
                    // Draw the boat with TWA
                    Point boatCenter = new Point(infoX, (this.getHeight() / 2));
                    WWGnlUtilities.drawBoat((Graphics2D) gr,
                            Color.CYAN,
                            boatCenter,             // Pos on the Panel
                            (this.getHeight() / 3), // Boat Length
                            heading,                // Heading
                            0.5f);                  // Alpha
                    // Now, the wind
                    WWGnlUtilities.drawTWAOverBoat((Graphics2D) gr,
                            (this.getHeight() / 6), // Hand Length
                            boatCenter,
                            heading - windAngle); // TODO See why -windAngle
                }
            }

            if (origStroke != null) {
                ((Graphics2D) gr).setStroke(origStroke);
            }
        }

        private void drawDataArray(Graphics gr,
                                   List<DatedGribCondition> data,
                                   float windscale,
                                   float prmslscale,
                                   float hgt500scale,
                                   float wavescale,
                                   float tempscale,
                                   float rainscale) {
            int gribIdx = 0, gribSize = data.size();

            int prevXtws = -1, prevYtws = -1;
            int prevXprmsl = -1, prevYprmsl = -1;
            int prevXhgt500 = -1, prevYhgt500 = -1;
            int prevXwaves = -1, prevYwaves = -1;
            int prevXtemp = -1, prevYtemp = -1;
            int prevXrain = -1, prevYrain = -1;
            for (DatedGribCondition gribPoint : data) {
                if (gribPoint != null) {
                    float tws = gribPoint.windspeed;
                    int twd = gribPoint.winddir;
                    float prmsl = gribPoint.prmsl / 100f; // hPa
                    float hgt500 = gribPoint.hgt500;     // m
                    float waves = gribPoint.waves / 100F; // m
                    float temp = gribPoint.temp - 273; // Celcius
                    float rain = gribPoint.rain * 3600f; // kg/m^2/h => mm/h
                    int x, y;
                    x = (int) ((float) gribIdx * (float) this.getWidth() / (float) gribSize);
                    // TWS
                    if (displayTWS && !Float.isInfinite(windscale)) {
                        //    System.out.println("Idx:" + gribIdx + ", x:" + x + " for w:" + this.getWidth() + " and gSize:" + gribSize);
                        y = (int) (this.getHeight() - (tws * windscale));
                        gr.setColor((Color) ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXtws > -1 && prevYtws > -1) {
                            gr.drawLine(prevXtws, prevYtws, x, y);
                        }
                        prevXtws = x;
                        prevYtws = y;
                    }
                    // PRMSL
                    if (displayPRMSL && !Float.isInfinite(prmslscale)) {
                        y = (int) (this.getHeight() - ((prmsl - (gribMini.prmsl / 100f)) * prmslscale));
                        gr.setColor((Color) ParamPanel.data[ParamData.PRMSL_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXprmsl > -1 && prevYprmsl > -1) {
                            gr.drawLine(prevXprmsl, prevYprmsl, x, y);
                        }
                        prevXprmsl = x;
                        prevYprmsl = y;
                    }
                    // HGT500
                    if (displayHGT500 && !Float.isInfinite(hgt500scale)) {
                        y = (int) (this.getHeight() - ((hgt500 - (gribMini.hgt500)) * hgt500scale));
                        gr.setColor((Color) ParamPanel.data[ParamData.HGT500_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXhgt500 > -1 && prevYhgt500 > -1) {
                            gr.drawLine(prevXhgt500, prevYhgt500, x, y);
                        }
                        prevXhgt500 = x;
                        prevYhgt500 = y;
                    }
                    // WAVES
                    if (displayWAVES && !Float.isInfinite(wavescale)) {
                        y = (int) (this.getHeight() - (waves * wavescale));
                        gr.setColor((Color) ParamPanel.data[ParamData.WAVES_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXwaves > -1 && prevYwaves > -1) {
                            gr.drawLine(prevXwaves, prevYwaves, x, y);
                        }
                        prevXwaves = x;
                        prevYwaves = y;
                    }
                    // TEMP
                    if (displayTEMP && !Float.isInfinite(tempscale)) {
                        y = (int) (this.getHeight() - ((temp - (gribMini.temp - 273)) * tempscale));
                        gr.setColor((Color) ParamPanel.data[ParamData.AIRTMP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXtemp > -1 && prevYtemp > -1) {
                            gr.drawLine(prevXtemp, prevYtemp, x, y);
                        }
                        prevXtemp = x;
                        prevYtemp = y;
                    }
                    // RAIN
                    if (displayRAIN && !Float.isInfinite(rainscale)) {
//          System.out.println("-> Rain (2) " + x + ":" + (rain));
                        y = (int) (this.getHeight() - (((rain) /* - (gribMini.rain * 3600f) */) * rainscale));
                        gr.setColor((Color) ParamPanel.data[ParamData.RAIN_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXrain > -1 && prevYrain > -1) {
                            gr.drawLine(prevXrain, prevYrain, x, y);
                        }
                        prevXrain = x;
                        prevYrain = y;
                    }
                }
                gribIdx++;
            }
        }

        private void drawBoatSpeed(Graphics gr,
                                   List<Double> data,
                                   float bspscale) {
            int bspIdx = 0, bspSize = data.size();

            int prevXbsp = -1,
                    prevYbsp = -1;
            for (Double d : data) {
                if (d != null) {
                    float bsp = (float) d.doubleValue();
                    int x, y;
                    x = (int) ((float) bspIdx * (float) this.getWidth() / (float) bspSize);
                    // BSP
                    if (displayBSP) {
                        //    System.out.println("Idx:" + gribIdx + ", x:" + x + " for w:" + this.getWidth() + " and gSize:" + gribSize);
                        y = (int) (this.getHeight() - (bsp * bspscale));
                        gr.setColor((Color) ParamPanel.data[ParamData.BSP_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXbsp > -1 && prevYbsp > -1) {
                            gr.drawLine(prevXbsp, prevYbsp, x, y);
                        }
                        prevXbsp = x;
                        prevYbsp = y;
                    }
                }
                bspIdx++;
            }
        }

        private void drawTWA(Graphics gr,
                             List<Integer> data,
                             float scale) {
            int bspIdx = 0, bspSize = data.size();

            int prevXbsp = -1,
                    prevYbsp = -1;
            for (Integer d : data) {
                if (d != null) {
                    float bsp = (float) d.doubleValue();
                    int x, y;
                    x = (int) ((float) bspIdx * (float) this.getWidth() / (float) bspSize);
                    if (displayTWS) {
                        //    System.out.println("Idx:" + gribIdx + ", x:" + x + " for w:" + this.getWidth() + " and gSize:" + gribSize);
                        y = (int) (this.getHeight() - (bsp * scale));
                        gr.setColor((Color) ParamPanel.data[ParamData.TWS_COLOR_IN_ROUTING][ParamData.VALUE_INDEX]);
                        if (prevXbsp > -1 && prevYbsp > -1) {
                            gr.drawLine(prevXbsp, prevYbsp, x, y);
                        }
                        prevXbsp = x;
                        prevYbsp = y;
                    }
                }
                bspIdx++;
            }
        }

        public void postit(Graphics g, String s, int x, int y, Color bgcolor, Color fgcolor, Float transp) {
            int bevel = 2;
            int postitOffset = 5;

            int startX = x;
            int startY = y;

            Color origin = g.getColor();
            g.setColor(fgcolor);
            Font f = g.getFont();
            int nbCr = 0;
            int crOffset;
            for (crOffset = 0; (crOffset = s.indexOf("\n", crOffset) + 1) > 0; ) {
                nbCr++;
            }
            String[] txt = new String[nbCr + 1];
            int i = 0;
            crOffset = 0;
            for (i = 0; i < nbCr; i++) {
                txt[i] = s.substring(crOffset, (crOffset = s.indexOf("\n", crOffset) + 1) - 1);
            }
            txt[i] = s.substring(crOffset);
            int strWidth = 0;
            for (i = 0; i < nbCr + 1; i++) {
                if (g.getFontMetrics(f).stringWidth(txt[i]) > strWidth) {
                    strWidth = g.getFontMetrics(f).stringWidth(txt[i]);
                }
            }
            Color c = g.getColor();
            g.setColor(bgcolor);
            if (g instanceof Graphics2D) {
                // Transparency
                Graphics2D g2 = (Graphics2D) g;
                float alpha = (transp != null ? transp : 0.3f);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            // left or right, up or down...
            Point topRightExtremity = new Point(x + postitOffset + strWidth + (2 * bevel), (y - f.getSize()) + 1);
            Point bottomRightExtremity = new Point(x + postitOffset + strWidth + (2 * bevel), (nbCr + 1) * f.getSize());
            Point bottomLeftExtremity = new Point(x, y + ((nbCr + 1) * f.getSize()));

            if (!this.getVisibleRect().contains(topRightExtremity) && !this.getVisibleRect().contains(bottomRightExtremity)) {
                // This display left
                startX = x - strWidth - (2 * bevel) - (2 * postitOffset);
            }
            if ((startY - f.getSize()) < 0) {
                // This display down
                //  startY = y - ((nbCr + 1) * f.getSize());
                startY = y + ((nbCr + 1) * f.getSize());
                //  System.out.println("Up, y [" + y + "] becomes [" + startY + "]");
            }

            g.fillRect(startX + postitOffset, (startY - f.getSize()) + 1, strWidth + (2 * bevel), (nbCr + 1) * f.getSize());
            if (g instanceof Graphics2D) {
                // Reset Transparency
                Graphics2D g2 = (Graphics2D) g;
                float alpha = 1.0f;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            if (fgcolor != null) {
                g.setColor(fgcolor);
            } else {
                g.setColor(c);
            }
            for (i = 0; i < nbCr + 1; i++) {
                g.drawString(txt[i], startX + bevel + postitOffset, startY + (i * f.getSize()));
            }
            g.setColor(origin);
        }
    }
}
