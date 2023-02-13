package chart.components.ui.gui;

import chart.components.util.GnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DistancePanel extends JPanel {
    private double gcDist = 0D, rlDist = 0D;
    private final static NumberFormat df22 = new DecimalFormat("##0.00");

    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel distanceLabel = new JLabel();
    private JLabel fromLabel = new JLabel();
    private JLabel andLabel = new JLabel();
    private JLabel toLabel = new JLabel();
    private JLabel gcLabel = new JLabel();
    private JLabel rhumbLineLabel = new JLabel();
    private JLabel bearingLabel = new JLabel();
    private JLabel gcValueLabel = new JLabel();
    private JLabel rhumblineValueLabel = new JLabel();
    private JLabel bearingValueLabel = new JLabel();
    private JSeparator jSeparator1 = new JSeparator();
    private JLabel atLabel = new JLabel();
    private JPanel drPanel = new JPanel();
    private JTextField speedTextField = new JTextField();
    private JLabel knotsLabel = new JLabel();
    private JButton recalcButton = new JButton();
    private JLabel dotDotDotLabel = new JLabel();
    private JLabel gcResultLabel = new JLabel();
    private JLabel rhumblineResultLabel = new JLabel();

    public DistancePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        distanceLabel.setText(GnlUtilities.buildMessage("distance-between", null));
        fromLabel.setText("");
        andLabel.setText(GnlUtilities.buildMessage("and", null));
        toLabel.setText("");
        gcLabel.setText(GnlUtilities.buildMessage("gc", null));
        rhumbLineLabel.setText(GnlUtilities.buildMessage("rhumbline", null));
        bearingLabel.setText(GnlUtilities.buildMessage("bearing", null));
        gcValueLabel.setText("");
        rhumblineValueLabel.setText("");
        bearingValueLabel.setText("");
        atLabel.setText(GnlUtilities.buildMessage("at"));
        speedTextField.setMinimumSize(new Dimension(30, 20));
        speedTextField.setPreferredSize(new Dimension(30, 20));
        speedTextField.setHorizontalAlignment(JTextField.CENTER);
        speedTextField.setText("5.0");
        knotsLabel.setText(GnlUtilities.buildMessage("knot"));
        recalcButton.setText(GnlUtilities.buildMessage("see"));
        recalcButton.addActionListener(e -> recalcButton_actionPerformed(e));
        dotDotDotLabel.setText("...");
        gcResultLabel.setText("...");
        rhumblineResultLabel.setText("...");
        this.add(distanceLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(fromLabel,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        this.add(andLabel,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(toLabel,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        this.add(gcLabel,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(rhumbLineLabel,
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(bearingLabel,
                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(gcValueLabel,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        this.add(rhumblineValueLabel,
                new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        this.add(bearingValueLabel,
                new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));
        this.add(jSeparator1, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(drPanel, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(gcResultLabel, new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(rhumblineResultLabel, new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        drPanel.add(atLabel, null);
        drPanel.add(speedTextField, null);
        drPanel.add(knotsLabel, null);
        drPanel.add(recalcButton, null);
        drPanel.add(dotDotDotLabel, null);
    }

    public void setFrom(String str) {
        fromLabel.setText(str);
    }

    public void setTo(String str) {
        toLabel.setText(str);
    }

    public void setGCValue(String str) {
        gcValueLabel.setText(str);
    }

    public void setRhumblineValue(String str) {
        rhumblineValueLabel.setText(str);
    }

    public void setBearingValue(String str) {
        bearingValueLabel.setText(str);
    }

    private void recalcButton_actionPerformed(ActionEvent e) {
        computeTime();
    }

    public void computeTime() {
        double bsp = 0.0;
        try {
            bsp = Double.parseDouble(speedTextField.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, nfe.toString(), GnlUtilities.buildMessage("bsp-val-error"), JOptionPane.ERROR_MESSAGE);
        }
        if (bsp < 0D) {
            JOptionPane.showMessageDialog(this, GnlUtilities.buildMessage("bsp-positive-please"), GnlUtilities.buildMessage("bsp-val-error"), JOptionPane.ERROR_MESSAGE);
        }
        if (bsp > 0D) {
            double gcTime = gcDist / bsp;
            double rlTime = rlDist / bsp;
            gcResultLabel.setText(GnlUtilities.buildMessage("gc-result", new String[]{df22.format(gcTime), df22.format(gcTime / 24D)}));
            rhumblineResultLabel.setText(GnlUtilities.buildMessage("rl-result", new String[]{df22.format(rlTime), df22.format(rlTime / 24D)}));
        }
    }

    public void setGcDist(double gcDist) {
        this.gcDist = gcDist;
    }

    public double getGcDist() {
        return gcDist;
    }

    public void setRlDist(double rlDist) {
        this.rlDist = rlDist;
    }
}
