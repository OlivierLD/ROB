package chartview.gui.util.dialog;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class StartRoutingPanel
       extends JPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel jLabel1 = new JLabel();
    private JLabel message = new JLabel();
    private JTextField yearTextField = new JTextField();
    private JComboBox monthComboBox = new JComboBox();
    private JTextField dayTextField = new JTextField();
    private JTextField hourTextField = new JTextField();
    private JTextField minTextField = new JTextField();
    private JTextField secTextField = new JTextField();
    private JLabel gmtLabel = new JLabel();
    private JLabel sepLabel1 = new JLabel();
    private JLabel sepLabel2 = new JLabel();
    private JLabel startDateLabel = new JLabel();

    private int timeInterval = 0;
    private int angularStep = 0;
    private int forkWidth = 0;
    private int maxTWS = -1;
    private int minTWA = -1;
    private boolean stopRoutingOnExhaustedGRIB = false;
    private Date gribFrom = null;

    private JLabel atLabel = new JLabel();
    private JLabel distanceLabel = new JLabel();
    private JLabel jLabel7 = new JLabel();
    private JLabel jLabel8 = new JLabel();
    private JLabel jLabel9 = new JLabel();
    private JCheckBox avoidTWSCheckBox = new JCheckBox();
    private JTextField timeIntervalTextField = new JTextField();
    private JTextField angularStepTextField = new JTextField();
    private JTextField forkWidthTextField = new JTextField();
    private JTextField maxTWSTextField = new JTextField();
    private JLabel jLabel11 = new JLabel();
    private JLabel jLabel12 = new JLabel();
    private JLabel jLabel13 = new JLabel();
    private JLabel knotsLabel = new JLabel();
    private JCheckBox avoidTWACheckBox = new JCheckBox();
    private JTextField minTWATextField = new JTextField();
    private JLabel angleLabel = new JLabel();
    private JCheckBox gribExhaustedCheckBox = new JCheckBox();
    private JCheckBox withTheGribCheckBox = new JCheckBox();
    private JLabel jLabel2 = new JLabel();
    private DecimalFormat df = new DecimalFormat("#0.00");
    private JFormattedTextField polarFactorTextField = new JFormattedTextField(df);
    private JCheckBox avoidLandCheckBox = new JCheckBox();
    private JLabel proximityLabel = new JLabel();

    private final static DecimalFormat MASK = new DecimalFormat("##0.00");
    private JFormattedTextField proximityFormattedTextField = new JFormattedTextField(MASK);
    private JLabel jLabel3 = new JLabel();

    public StartRoutingPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    public void setDate(Date date) {
        TimeZone.setDefault(TimeZone.getTimeZone("127"));
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);

        yearTextField.setText(Integer.toString(year));
        monthComboBox.setSelectedIndex(month);
        dayTextField.setText(Integer.toString(day));
        hourTextField.setText(Integer.toString(hours));
        minTextField.setText(Integer.toString(minutes));
        secTextField.setText(Integer.toString(seconds));
    }

    public Date getDate() {
        Calendar cal = new GregorianCalendar();
        cal.set(Integer.parseInt(yearTextField.getText()),
                monthComboBox.getSelectedIndex(),
                Integer.parseInt(dayTextField.getText()),
                Integer.parseInt(hourTextField.getText()),
                Integer.parseInt(minTextField.getText()),
                Integer.parseInt(secTextField.getText()));
        return cal.getTime();
    }

    public void setDistanceLabel(String str) {
        distanceLabel.setText(str);
    }

    public void setMess(String str) {
        message.setText(str);
    }

    private void jbInit()
            throws Exception {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(400, 348));
        jLabel1.setText(WWGnlUtilities.buildMessage("routing-start-date"));
        jLabel1.setFont(new Font("Tahoma", 1, 12));
        message.setText("Message goes here");
        yearTextField.setPreferredSize(new Dimension(40, 20));
        yearTextField.setHorizontalAlignment(JTextField.CENTER);
        monthComboBox.setPreferredSize(new Dimension(100, 20));
        dayTextField.setPreferredSize(new Dimension(30, 20));
        dayTextField.setHorizontalAlignment(JTextField.CENTER);
        hourTextField.setPreferredSize(new Dimension(30, 20));
        hourTextField.setHorizontalAlignment(JTextField.CENTER);
        minTextField.setPreferredSize(new Dimension(30, 20));
        minTextField.setHorizontalAlignment(JTextField.CENTER);
        secTextField.setPreferredSize(new Dimension(30, 20));
        secTextField.setHorizontalAlignment(JTextField.CENTER);
        gmtLabel.setText("UTC");
        sepLabel1.setText(":");
        sepLabel2.setText(":");
        startDateLabel.setText(WWGnlUtilities.buildMessage("set-start-date"));

        atLabel.setText("@");
        distanceLabel.setText(WWGnlUtilities.buildMessage("requested-distance"));
        jLabel7.setText(WWGnlUtilities.buildMessage("between-isochrones"));
        jLabel8.setText(WWGnlUtilities.buildMessage("angular-step"));
        jLabel9.setText(WWGnlUtilities.buildMessage("fork-width"));
        avoidTWSCheckBox.setText(WWGnlUtilities.buildMessage("avoid-tws-gt"));
        avoidTWSCheckBox.setSelected(false);
        maxTWSTextField.setEnabled(false);
        knotsLabel.setEnabled(false);
        avoidTWSCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                avoidTWSCheckBox_actionPerformed(e);
            }
        });
        timeIntervalTextField.setHorizontalAlignment(JTextField.CENTER);
        angularStepTextField.setHorizontalAlignment(JTextField.CENTER);
        forkWidthTextField.setHorizontalAlignment(JTextField.CENTER);
        maxTWSTextField.setHorizontalAlignment(JTextField.CENTER);
        jLabel11.setText(WWGnlUtilities.buildMessage("hours"));
        jLabel12.setText(WWGnlUtilities.buildMessage("degrees"));
        jLabel13.setText(WWGnlUtilities.buildMessage("degrees"));
        knotsLabel.setText(WWGnlUtilities.buildMessage("knots"));
        avoidTWACheckBox.setText(WWGnlUtilities.buildMessage("avoid-twa"));
        minTWATextField.setEnabled(false);
        angleLabel.setEnabled(false);
        avoidTWACheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                avoidTWACheckBox_actionPerformed(e);
            }
        });
        minTWATextField.setHorizontalAlignment(JTextField.CENTER);
        angleLabel.setText(WWGnlUtilities.buildMessage("degrees"));
        gribExhaustedCheckBox.setText(WWGnlUtilities.buildMessage("stop-routing"));
        gribExhaustedCheckBox.setActionCommand("gribExhaustedCheckBox");
        gribExhaustedCheckBox.setToolTipText(WWGnlUtilities.buildMessage("exhausted-tooltip"));
        withTheGribCheckBox.setText(WWGnlUtilities.buildMessage("start-with-grib"));
        withTheGribCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                withTheGribCheckBox_actionPerformed(e);
            }
        });
        jLabel2.setText(WWGnlUtilities.buildMessage("polar-speed-factor"));
        polarFactorTextField.setHorizontalAlignment(JTextField.RIGHT);
        polarFactorTextField.setText("1.0");
        polarFactorTextField.setToolTipText(WWGnlUtilities.buildMessage("polar-speed-hint"));
        avoidLandCheckBox.setText(WWGnlUtilities.buildMessage("avoid-land"));
        avoidLandCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                avoidLandCheckBox_actionPerformed(e);
            }
        });
        proximityLabel.setText(WWGnlUtilities.buildMessage("routing-completed-below"));
        proximityFormattedTextField.setHorizontalAlignment(JTextField.RIGHT);
        proximityFormattedTextField.setText("0.00");
        jLabel3.setText("nm");
        for (int i = 0; i < WWGnlUtilities.MONTH.length; i++) {
          monthComboBox.addItem(WWGnlUtilities.MONTH[i]);
        }

        this.add(jLabel1,
                new GridBagConstraints(0, 1, 10, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 10, 0), 0, 0));
        this.add(message,
                new GridBagConstraints(0, 2, 10, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 5, 0), 0, 0));
        this.add(yearTextField,
                new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(monthComboBox,
                new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(dayTextField,
                new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(hourTextField,
                new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(minTextField,
                new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(secTextField,
                new GridBagConstraints(8, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        this.add(gmtLabel,
                new GridBagConstraints(9, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(sepLabel1,
                new GridBagConstraints(5, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(sepLabel2,
                new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(startDateLabel,
                new GridBagConstraints(0, 4, 10, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 2, 0), 0, 0));
        this.add(atLabel,
                new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(distanceLabel,
                new GridBagConstraints(0, 0, 10, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 10, 0), 0, 0));
        this.add(jLabel7,
                new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(10, 0, 0, 0), 0, 0));
        this.add(jLabel8,
                new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel9,
                new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(avoidTWSCheckBox,
                new GridBagConstraints(0, 9, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(timeIntervalTextField,
                new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(10, 0, 0, 0), 0, 0));
        this.add(angularStepTextField,
                new GridBagConstraints(4, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(forkWidthTextField,
                new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(maxTWSTextField,
                new GridBagConstraints(4, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel11,
                new GridBagConstraints(6, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(10, 2, 0, 0), 0, 0));
        this.add(jLabel12,
                new GridBagConstraints(6, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        this.add(jLabel13,
                new GridBagConstraints(6, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        this.add(knotsLabel,
                new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        this.add(avoidTWACheckBox,
                new GridBagConstraints(0, 10, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(minTWATextField,
                new GridBagConstraints(4, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(angleLabel,
                new GridBagConstraints(6, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(gribExhaustedCheckBox,
                new GridBagConstraints(0, 11, 10, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(withTheGribCheckBox,
                new GridBagConstraints(0, 3, 10, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2,
                new GridBagConstraints(0, 12, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(polarFactorTextField,
                new GridBagConstraints(4, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(avoidLandCheckBox, new GridBagConstraints(0, 13, 10, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(proximityLabel, new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(proximityFormattedTextField, new GridBagConstraints(4, 14, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(6, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 2, 0, 0), 0, 0));
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
        timeIntervalTextField.setText(Integer.toString(timeInterval));
    }

    public int getTimeInterval() {
        try {
            timeInterval = Integer.parseInt(timeIntervalTextField.getText());
        } catch (Exception ignore) {
        }
        return timeInterval;
    }

    public void setAngularStep(int angularStep) {
        angularStepTextField.setText(Integer.toString(angularStep));
        this.angularStep = angularStep;
    }

    public int getAngularStep() {
        try {
            angularStep = Integer.parseInt(angularStepTextField.getText());
        } catch (Exception ignore) {
        }
        return angularStep;
    }

    public void setForkWidth(int forkWidth) {
        forkWidthTextField.setText(Integer.toString(forkWidth));
        this.forkWidth = forkWidth;
    }

    public int getForkWidth() {
        try {
            forkWidth = Integer.parseInt(forkWidthTextField.getText());
        } catch (Exception ignore) {
        }
        return forkWidth;
    }

    public void setMaxTWS(int maxTWS) {
        maxTWSTextField.setText(Integer.toString(maxTWS));
        this.maxTWS = maxTWS;
        maxTWSTextField.setEnabled(maxTWS > -1);
        avoidTWSCheckBox.setSelected(maxTWS > -1);
    }

    public int getMaxTWS() {
        try {
            maxTWS = Integer.parseInt(maxTWSTextField.getText());
        } catch (Exception ignore) {
        }
        return maxTWS;
    }

    public void setMinTWA(int minTWA) {
        minTWATextField.setText(Integer.toString(minTWA));
        this.minTWA = minTWA;
        minTWATextField.setEnabled(minTWA > -1);
        avoidTWACheckBox.setSelected(minTWA > -1);
    }

    public int getMinTWA() {
        try {
            minTWA = Integer.parseInt(minTWATextField.getText());
        } catch (Exception ignore) {
        }
        return minTWA;
    }

    public void setPolarFactor(double d) {
        polarFactorTextField.setText(df.format(d));
    }

    public double getPolarFactor() {
        double d = 1.0;
        try {
            d = df.parse(polarFactorTextField.getText()).doubleValue();
        } catch (Exception nfe) {
            System.out.println(polarFactorTextField.getText() + " is not a valid double.");
            d = 1.0;
        }
        return d;
    }

    public void setStopRoutingOnExhaustedGRIB(boolean stopRoutingOnExhaustedGRIB) {
        this.stopRoutingOnExhaustedGRIB = stopRoutingOnExhaustedGRIB;
        gribExhaustedCheckBox.setSelected(stopRoutingOnExhaustedGRIB);
    }

    public boolean isStopRoutingOnExhaustedGRIB() {
        this.stopRoutingOnExhaustedGRIB = gribExhaustedCheckBox.isSelected();
        return stopRoutingOnExhaustedGRIB;
    }

    private void withTheGribCheckBox_actionPerformed(ActionEvent e) {
        boolean b = withTheGribCheckBox.isSelected();
        if (b) {
          setDate(gribFrom);
        } else {
          setDate(new Date());
        }
        yearTextField.setEnabled(!b);
        monthComboBox.setEnabled(!b);
        dayTextField.setEnabled(!b);
        hourTextField.setEnabled(!b);
        minTextField.setEnabled(!b);
        secTextField.setEnabled(!b);
        gmtLabel.setEnabled(!b);
        atLabel.setEnabled(!b);
        startDateLabel.setEnabled(!b);
        sepLabel1.setEnabled(!b);
        sepLabel2.setEnabled(!b);
    }

    public void setGribFrom(Date gribFrom) {
        this.gribFrom = gribFrom;
    }

    public boolean avoidLand() {
        return avoidLandCheckBox.isSelected();
    }

    public void setAvoidLand(boolean b) {
        avoidLandCheckBox.setSelected(b);
        proximityFormattedTextField.setEnabled(b);
        jLabel3.setEnabled(b);
    }

    public void setProximity(double d) {
        proximityFormattedTextField.setText(MASK.format(d));
    }

    public double getProximity() {
        double d = 1.0;
        try {
            d = MASK.parse(proximityFormattedTextField.getText()).doubleValue();
        } catch (Exception nfe) {
            System.out.println(proximityFormattedTextField.getText() + " is not a valid double.");
            d = 1.0;
        }
        return d;
    }

    private void avoidTWSCheckBox_actionPerformed(ActionEvent e) {
        maxTWSTextField.setEnabled(avoidTWSCheckBox.isSelected());
        knotsLabel.setEnabled(avoidTWSCheckBox.isSelected());
        if (!avoidTWSCheckBox.isSelected()) {
          maxTWSTextField.setText("-1");
        }
    }

    private void avoidTWACheckBox_actionPerformed(ActionEvent e) {
        minTWATextField.setEnabled(avoidTWACheckBox.isSelected());
        angleLabel.setEnabled(avoidTWACheckBox.isSelected());
        if (!avoidTWACheckBox.isSelected()) {
          minTWATextField.setText("-1");
        }
    }

    private void avoidLandCheckBox_actionPerformed(ActionEvent e) {
        proximityFormattedTextField.setEnabled(avoidLandCheckBox.isSelected());
        jLabel3.setEnabled(avoidLandCheckBox.isSelected());
    }
}
