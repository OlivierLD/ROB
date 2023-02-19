package chartview.gui.toolbar.controlpanels;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.right.CompositeTabbedPane;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.TimeZone;


public class GribPanel
        extends JPanel {
    private final GribPanel instance = this;

    private final JButton forwardButton = new JButton();
    private final JButton backwardButton = new JButton();
    private final JButton animateButton = new JButton();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel gribInfoLineTwo = new JLabel();
    private final JLabel gribInfoLineThree = new JLabel();
    private final JLabel gribInfoLineFour = new JLabel();
    private final JPanel topPanel = new JPanel();

    private final JPanel gribSmoothingPanel = new JPanel();
    private final JTextField smoothValue = new JTextField("1");
    private final JButton smoothButton = new JButton();
    private final JLabel smoothLabel = new JLabel(WWGnlUtilities.buildMessage("grib-smooth"));
    private final JPanel dataOptionsPanel = new JPanel();

    private final JLabel windLabel = new JLabel("WIND");
    private final JLabel prmslLabel = new JLabel("PRMSL");
    private final JLabel hgtLabel = new JLabel("HGT");
    private final JLabel tmpLabel = new JLabel("AIRTMP");
    private final JLabel waveLabel = new JLabel("WAVES");
    private final JLabel rainLabel = new JLabel("RAIN");
    private final JButton googleButton = new JButton();
    private final JSlider gribSlider = new JSlider();

    private int sliderValue = 1;
    private final JLabel googleLabel = new JLabel();
    private final GridBagLayout gridBagLayout2 = new GridBagLayout();
    private final JLabel smoothTimeLabel = new JLabel(WWGnlUtilities.buildMessage("grib-time-smooth"));
    private final JTextField smoothTimeValue = new JTextField();
    private final JButton smoothTimeButton = new JButton();

    private final JCheckBox withLabelOnGribCheckBox = new JCheckBox(WWContext.getInstance().getOriginalDefaultTimeZone().getID());
    private final GridBagLayout gridBagLayout3 = new GridBagLayout();
    private final JSlider replaySpeedSlider = new JSlider();
    private final JLabel gribCoeffLabel = new JLabel(); // "Etc/UTC"
    // private JLabel timeZoneLabel = new JLabel("Etc/UTC");

    public GribPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from GribPanel.";
            }

            public void gribLoaded() {
                googleButton.setEnabled(true);
                backwardButton.setEnabled(true);
//        animateButton.setEnabled(true); 
//        dustletLoopButton.setEnabled(true);
                forwardButton.setEnabled(true);
                gribInfoLineTwo.setEnabled(true);
                gribInfoLineThree.setEnabled(true);
                gribInfoLineFour.setEnabled(true);
                smoothLabel.setEnabled(true);
                smoothValue.setEnabled(true);
                smoothButton.setEnabled(true);
                smoothTimeLabel.setEnabled(true);
                smoothTimeValue.setEnabled(true);
                smoothTimeButton.setEnabled(true);
                gribSlider.setEnabled(true);
                googleLabel.setEnabled(true);

                withLabelOnGribCheckBox.setEnabled(true);
//        timeZoneLabel.setEnabled(withLabelOnGribCheckBox.isSelected());
            }

            public void gribUnloaded() {
                googleButton.setEnabled(false);
                backwardButton.setEnabled(false);
                animateButton.setEnabled(false);
//        dustletLoopButton.setEnabled(false);
                forwardButton.setEnabled(false);
                gribInfoLineTwo.setEnabled(false);
                gribInfoLineThree.setEnabled(false);
                gribInfoLineFour.setEnabled(false);
                smoothLabel.setEnabled(false);
                smoothValue.setEnabled(false);
                smoothButton.setEnabled(false);
                smoothTimeLabel.setEnabled(false);
                smoothTimeValue.setEnabled(false);
                smoothTimeButton.setEnabled(false);
                gribSlider.setEnabled(false);
                googleLabel.setEnabled(false);
                withLabelOnGribCheckBox.setEnabled(false);
//        timeZoneLabel.setEnabled(false);
                WWContext.getInstance().setGribFile(null);
            }

            public void updateGribSmoothingValue(int i) {
                CompositeTabbedPane ctp = WWGnlUtilities.findFirstParentOfType(instance, CompositeTabbedPane.class);
                // if (ctp != null && ctp.getSelectedIndex() == ((AdjustFrame)WWContext.getInstance().getMasterTopFrame()).getMasterTabPane().getSelectedIndex())
                // if (ctp != null && ctp.equals(((AdjustFrame)WWContext.getInstance().getMasterTopFrame()).getMasterTabPane().getSelectedComponent()))
                if (ctp != null && ctp.isVisible()) {
                    // System.out.println("Setting smooth value from " + smoothValue.getText() + " to " + i);
                    smoothValue.setText(Integer.toString(i));
                }
            }

            public void updateGribTimeSmoothingValue(int i) {
                CompositeTabbedPane ctp = WWGnlUtilities.findFirstParentOfType(instance, CompositeTabbedPane.class); // TODO Apply this to othe methods of the listener
                // if (ctp != null && ctp.getSelectedIndex() == ((AdjustFrame)WWContext.getInstance().getMasterTopFrame()).getMasterTabPane().getSelectedIndex())
                // if (ctp != null && ctp.equals(((AdjustFrame)WWContext.getInstance().getMasterTopFrame()).getMasterTabPane().getSelectedComponent()))
                if (ctp != null && ctp.isVisible()) {
                    // System.out.println("Setting time smooth value from " + smoothTimeValue.getText() + " to " + i);
                    smoothTimeValue.setText(Integer.toString(i));
                }
            }

            public void setGribInfo(int currentIndex,
                                    int maxIndex,
                                    String s2,
                                    String s3,
                                    String s4,
                                    boolean windOnly,
                                    boolean b1,
                                    boolean b2,
                                    boolean b3,
                                    boolean b4,
                                    boolean b5,
                                    boolean b6,
                                    boolean b7, // current
                                    boolean displayWind,
                                    boolean display3DPrmsl,
                                    boolean display3D500hgt,
                                    boolean display3DTemp,
                                    boolean display3Dwaves,
                                    boolean display3DRain) {
                setGribInfoLabel(currentIndex, maxIndex, s2, s3, s4, b1, b2, b3, b4, b5, b6);
                sliderValue = currentIndex + 1;
                gribSlider.setMaximum(maxIndex);
                gribSlider.setValue(sliderValue);
            }

            public void startGribAnimation() {
                forwardButton.setEnabled(false);
                backwardButton.setEnabled(false);
                animateButton.setToolTipText("Stop Animation");
            }

            public void stopGribAnimation() {
                forwardButton.setEnabled(true);
                backwardButton.setEnabled(true);
                animateButton.setToolTipText("Start Animation");
            }

            public void thereIsMoreThanOneGrib() {
                animateButton.setEnabled(true);
//        dustletLoopButton.setEnabled(true);          
            }

        });
        this.setSize(new Dimension(ControlPane.WIDTH, 270));
        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 270));
        this.setMinimumSize(new Dimension(ControlPane.WIDTH, 270));
        this.setMaximumSize(new Dimension(1000, 1000));
        this.setLayout(gridBagLayout1);
        this.setEnabled(false);
        forwardButton.setIcon(new ImageIcon(this.getClass().getResource("img/panright.gif")));
        forwardButton.setPreferredSize(new Dimension(24, 24));
        forwardButton.setBorderPainted(false);
        forwardButton.setMaximumSize(new Dimension(24, 24));
        forwardButton.setMinimumSize(new Dimension(24, 24));
        forwardButton.setMargin(new Insets(1, 1, 1, 1));
        forwardButton.addActionListener(this::forwardButton_actionPerformed);
        backwardButton.setIcon(new ImageIcon(this.getClass().getResource("img/panleft.gif")));
        backwardButton.setPreferredSize(new Dimension(24, 24));
        backwardButton.setBorderPainted(false);
        backwardButton.setMaximumSize(new Dimension(24, 24));
        backwardButton.setMinimumSize(new Dimension(24, 24));
        backwardButton.setMargin(new Insets(1, 1, 1, 1));
        backwardButton.addActionListener(this::backwardButton_actionPerformed);
        animateButton.setIcon(new ImageIcon(this.getClass().getResource("img/refresh.png")));
        animateButton.setPreferredSize(new Dimension(24, 24));
        animateButton.setBorderPainted(false);
        animateButton.setToolTipText("Start Animation");
        animateButton.setMaximumSize(new Dimension(24, 24));
        animateButton.setMinimumSize(new Dimension(24, 24));
        animateButton.setMargin(new Insets(1, 1, 1, 1));
        animateButton.addActionListener(this::animateButton_actionPerformed);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backwardButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));
        buttonPanel.add(forwardButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));
        buttonPanel.add(animateButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));

        withLabelOnGribCheckBox.setToolTipText("Display GRIB Date, with this TimeZone");

        buttonPanel.add(withLabelOnGribCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));
        withLabelOnGribCheckBox.setEnabled(false);
        withLabelOnGribCheckBox.addActionListener(e -> {
//        timeZoneLabel.setEnabled(withLabelOnGribCheckBox.isSelected());
            WWContext.getInstance().fireDisplayGRIBDateLabel(withLabelOnGribCheckBox.isSelected());
            if (withLabelOnGribCheckBox.isSelected()) {
                TimeZoneTable tzt = new TimeZoneTable(TimeZone.getAvailableIDs());
                int resp = JOptionPane.showConfirmDialog(instance, tzt, "Time Zones", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    String s = tzt.getSelectedTimeZoneData();
                    // System.out.println("Was [" + withLabelOnGribCheckBox.getText() + "], now [" + s +"]");
                    if (s != null) {
                        withLabelOnGribCheckBox.setText(s);
                    }
                }
                WWContext.getInstance().fireTimeZoneForLabel(withLabelOnGribCheckBox.getText());
            }
        });
//    timeZoneLabel.addMouseListener(new MouseAdapter() {
//        @Override
//        public void mouseClicked(MouseEvent e) {
//          super.mouseClicked(e);
//          if (timeZoneLabel.isEnabled()) {
//            TimeZoneTable tzt = new TimeZoneTable(TimeZone.getAvailableIDs());
//            int resp = JOptionPane.showConfirmDialog(instance, tzt, "Time Zones", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//            if (resp == JOptionPane.OK_OPTION) {
//              String s = tzt.getSelectedTimeZoneData();
//              System.out.println("Was [" + timeZoneLabel.getText() + "], now [" + s +"]");
//              timeZoneLabel.setText(s);
//              WWContext.getInstance().fireTimeZoneForLabel(s);
//            }
//          }
//        }
//      });
//    timeZoneLabel.setPreferredSize(new Dimension(100, 21));
//    timeZoneLabel.setToolTipText("TimeZone for GRIB date display");
//    buttonPanel.add(timeZoneLabel, null);

        replaySpeedSlider.setToolTipText("Animation replay speed");
        replaySpeedSlider.addChangeListener(evt -> {
            JSlider slider = (JSlider) evt.getSource();

            if (!slider.getValueIsAdjusting()) {
                int sv = slider.getValue();
                String tootltipMess = "Normal Speed";
                if (sv < 10) {
                    tootltipMess = "Veeeeeery slow";
                } else if (sv < 40) {
                    tootltipMess = "Slow";
                } else if (sv < 60) {
                    tootltipMess = "Normal Speed";
                } else if (sv < 80) {
                    tootltipMess = "Fast";
                } else {
                    tootltipMess = "As fast as possible";
                }
                slider.setToolTipText(tootltipMess);
                // Broadcast replay delay (not speed).
                WWContext.getInstance().fireSetReplayDelay(100 - sv);
            }
        });

        buttonPanel.setLayout(gridBagLayout3);
        gribCoeffLabel.setText(""); // GRIB TWS Coeff: 1.00");
        gribCoeffLabel.setForeground(Color.red);
        this.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(gribSmoothingPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        this.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 4, 1, 0), 0, 0));
        this.add(gribInfoLineTwo, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 0, 0), 0, 0));
        this.add(gribInfoLineThree, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));
        this.add(gribInfoLineFour, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 10, 0), 0, 0));
        this.add(dataOptionsPanel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        gribSlider.setMinimum(1);
        this.add(gribSlider, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 2, 0), 0, 0));
        this.add(replaySpeedSlider, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(gribCoeffLabel, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        gribSlider.addChangeListener(evt -> {
            JSlider slider = (JSlider) evt.getSource();

            if (!slider.getValueIsAdjusting()) {
                int sv = slider.getValue();
                if (sv != sliderValue) {
                    sliderValue = sv;
//            System.out.println("Slider Value:" + sliderValue);
                    updateSliderData();
                }
                slider.setToolTipText("Frame #" + Integer.toString(sv));
            }
        });

        dataOptionsPanel.add(windLabel, null);
        dataOptionsPanel.add(prmslLabel, null);
        dataOptionsPanel.add(hgtLabel, null);
        dataOptionsPanel.add(tmpLabel, null);
        dataOptionsPanel.add(waveLabel, null);
        dataOptionsPanel.add(rainLabel, null);

        windLabel.setEnabled(false);
        prmslLabel.setEnabled(false);
        hgtLabel.setEnabled(false);
        tmpLabel.setEnabled(false);
        waveLabel.setEnabled(false);
        rainLabel.setEnabled(false);

        //  googleButton.setText("Google");
        googleButton.setIcon(new ImageIcon(this.getClass().getResource("img/google.png")));
        googleButton.setActionCommand("googleMap");
        googleButton.setToolTipText(WWGnlUtilities.buildMessage("show-wind-in-google-map"));
        googleButton.addActionListener(this::googleButton_actionPerformed);
        googleButton.setEnabled(false);
        googleButton.setMaximumSize(new Dimension(24, 24));
        googleButton.setMinimumSize(new Dimension(24, 24));
        googleButton.setPreferredSize(new Dimension(24, 24));
        googleButton.setMargin(new Insets(1, 1, 1, 1));

        gribSlider.setPaintTicks(true);
        gribSlider.setValue(1); // Was 0
        gribSlider.setMaximum(10);
        gribSlider.setMajorTickSpacing(1);
        gribSlider.setEnabled(false);
        gribSlider.setMinimum(1); // Was 0
        gribSlider.setSnapToTicks(true);
        googleLabel.setText(WWGnlUtilities.buildMessage("wind-in-google-map"));
        googleLabel.setEnabled(false);
        smoothTimeLabel.setEnabled(false);
        smoothTimeValue.setPreferredSize(new Dimension(30, 20));
        smoothTimeValue.setText("1");
        smoothTimeValue.setToolTipText(WWGnlUtilities.buildMessage("grib-time-smooth-tooltip"));
        smoothTimeValue.setHorizontalAlignment(JTextField.CENTER);
        smoothTimeValue.setEnabled(false);
        smoothTimeButton.setText("...");
        smoothTimeButton.setToolTipText(WWGnlUtilities.buildMessage("apply-time-smooth"));
        smoothTimeButton.setPreferredSize(new Dimension(30, 20));
        smoothTimeButton.setEnabled(false);
        smoothTimeButton.addActionListener(this::smoothTimeButton_actionPerformed);
        gribSmoothingPanel.setLayout(gridBagLayout2);
        gribSmoothingPanel.add(smoothLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0, 0));
        smoothValue.setPreferredSize(new Dimension(30, 20));
        smoothValue.setToolTipText(WWGnlUtilities.buildMessage("grib-smooth-tooltip"));
        smoothValue.setHorizontalAlignment(JTextField.CENTER);
        gribSmoothingPanel.add(smoothValue,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0, 0));
        smoothButton.setText("...");
        smoothButton.setToolTipText(WWGnlUtilities.buildMessage("apply-smooth"));
        smoothButton.setPreferredSize(new Dimension(30, 20));
        smoothButton.addActionListener(this::smoothButton_actionPerformed);
        gribSmoothingPanel.add(smoothButton,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 0),
                        0, 0));
        gribSmoothingPanel.add(smoothTimeLabel,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0, 0));
        gribSmoothingPanel.add(smoothTimeValue,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0, 0));
        gribSmoothingPanel.add(smoothTimeButton,
                new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 0),
                        0, 0));
        smoothLabel.setEnabled(false);
        smoothValue.setEnabled(false);
        smoothButton.setEnabled(false);

        gribInfoLineTwo.setText("GRIB Info - 2");
        gribInfoLineThree.setText("GRIB Info - 3");
        backwardButton.setEnabled(false);
        forwardButton.setEnabled(false);
        animateButton.setEnabled(false);
        gribInfoLineTwo.setEnabled(false);
        gribInfoLineTwo.setPreferredSize(new Dimension(170, 14));
        gribInfoLineTwo.setFont(new Font("Tahoma", Font.BOLD, 9));
        gribInfoLineTwo.setForeground(Color.blue);
        gribInfoLineThree.setEnabled(false);
        gribInfoLineThree.setPreferredSize(new Dimension(170, 14));
        gribInfoLineThree.setFont(new Font("Tahoma", Font.PLAIN, 9));
        gribInfoLineFour.setText("GRIB Info - 4");
        gribInfoLineFour.setEnabled(false);
        gribInfoLineFour.setPreferredSize(new Dimension(170, 14));
        gribInfoLineFour.setFont(new Font("Tahoma", Font.PLAIN, 9));

        topPanel.add(googleLabel, null);
        topPanel.add(googleButton, null);
    }

    public void setGRIBTWSCoeff(double d) {
        gribCoeffLabel.setText("GRIB TWS Coeff:" + d);
    }

    private void updateSliderData() {
        WWContext.getInstance().fireGribIndex(sliderValue - 1);
    }

    public void setWind(boolean b) {
        windLabel.setEnabled(b);
    }

    public void setPrmsl(boolean b) {
        prmslLabel.setEnabled(b);
    }

    public void setHgt(boolean b) {
        hgtLabel.setEnabled(b);
    }

    public void setTemp(boolean b) {
        tmpLabel.setEnabled(b);
    }

    public void setWaves(boolean b) {
        waveLabel.setEnabled(b);
    }

    public void setRain(boolean b) {
        rainLabel.setEnabled(b);
    }

    private void forwardButton_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireGribForward();
    }

    private void backwardButton_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireGribBackward();
    }

    private void animateButton_actionPerformed(ActionEvent e) {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.gribAnimate();
        }
    }

    private void setGribInfoLabel(int index,
                                  int max,
                                  String s2,
                                  String s3,
                                  String s4,
                                  boolean b1,
                                  boolean b2,
                                  boolean b3,
                                  boolean b4,
                                  boolean b5,
                                  boolean b6) {
        String gribInfo1 = WWGnlUtilities.buildMessage("index-of", new String[]{Integer.toString(index),
                Integer.toString(max)});
        gribInfoLineTwo.setText(s2);
        gribInfoLineThree.setText(s3);
        gribInfoLineFour.setText(s4);

        setWind(b1);
        setPrmsl(b2);
        setHgt(b3);
        setTemp(b4);
        setWaves(b5);
        setRain(b6);

        double d = (Double) ParamPanel.data[ParamData.GRIB_TWS_COEFF][ParamData.VALUE_INDEX];
        if (d != 1.0) {
            setGRIBTWSCoeff(d);
        }
    }

    private void smoothButton_actionPerformed(ActionEvent e) {
        int smooth = 1;
        try {
            smooth = Integer.parseInt(smoothValue.getText());
        } catch (Exception ignore) {
        }
        WWContext.getInstance().fireGribSmoothing(smooth);
    }

    private void googleButton_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireWindInGoogle();
    }

    private void smoothTimeButton_actionPerformed(ActionEvent e) {
        int smooth = 1;
        try {
            smooth = Integer.parseInt(smoothTimeValue.getText());
        } catch (Exception ignore) {
        }
        WWContext.getInstance().fireGribTimeSmoothing(smooth);
    }
}
