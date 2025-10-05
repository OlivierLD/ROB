package chartview.gui.util.dialog;


import calc.GeoPoint;
import calc.GeomUtil;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


public class WhatIfRoutingPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JPanel jPanelOne = new JPanel();
    private final GridBagLayout gridBagLayout2 = new GridBagLayout();
    private final JLabel fromLabel = new JLabel();
    private final JLabel latLabel = new JLabel();
    private final JLabel lngLabel = new JLabel();
    private final JLabel whatIfLabel = new JLabel();
    private final JPanel jPanelTwo = new JPanel();
    private final JPanel jPanelThree = new JPanel();
    private final JPanel jPanelFour = new JPanel();
    private final JLabel iKeepSailingLabel = new JLabel();
    private final JLabel startingLabel = new JLabel();
    private final GridBagLayout gridBagLayout4 = new GridBagLayout();
    private final GridBagLayout gridBagLayout5 = new GridBagLayout();
    private final GridBagLayout gridBagLayout6 = new GridBagLayout();
    private final JRadioButton nowRadioButton = new JRadioButton();
    private final JRadioButton withTheGRIBRadioButton = new JRadioButton();
    private final JRadioButton headingRadioButton = new JRadioButton();
    private final JRadioButton twaRadioButton = new JRadioButton();
    private final JRadioButton gribExhaustedRadioButton = new JRadioButton();
    private final JRadioButton duringRadioButton = new JRadioButton();

    private final ButtonGroup groupOne = new ButtonGroup();
    private final ButtonGroup groupTwo = new ButtonGroup();
    private final ButtonGroup groupThree = new ButtonGroup();

    private final HeadingPanel hp = new HeadingPanel();
    private final TWAPanel tp = new TWAPanel();
    private final DurationPanel dp = new DurationPanel();
    private final GRIBExhaustedPanel gep = new GRIBExhaustedPanel();
    private final JSeparator jSeparator = new JSeparator();
    private final JLabel polarFactorLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();

    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final JFormattedTextField polarFactorTextField = new JFormattedTextField(df);
    private final int defaultStep = (int) (((Double) ParamPanel.data[ParamData.ROUTING_TIME_INTERVAL][ParamData.VALUE_INDEX]).doubleValue());
    private final transient SpinnerModel model = new SpinnerNumberModel(defaultStep, 1, 48, 1);
    private final JSpinner stepJSpinner = new JSpinner(model);

    public WhatIfRoutingPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // LOCALIZE
    private void jbInit() {
        groupOne.add(headingRadioButton);
        groupOne.add(twaRadioButton);
        headingRadioButton.setSelected(true);

        groupTwo.add(nowRadioButton);
        groupTwo.add(withTheGRIBRadioButton);
        nowRadioButton.setSelected(true);

        groupThree.add(gribExhaustedRadioButton);
        groupThree.add(duringRadioButton);
        gribExhaustedRadioButton.setSelected(true);

        this.setLayout(gridBagLayout1);
        jPanelOne.setLayout(gridBagLayout2);
        fromLabel.setText("From:");
        latLabel.setText("XX*XX.XX N");
        lngLabel.setText("XXX*XX.XX W");
        whatIfLabel.setText("What if...");
        whatIfLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
        jPanelTwo.setLayout(gridBagLayout4);
        jPanelThree.setLayout(gridBagLayout5);
        jPanelFour.setLayout(gridBagLayout6);
        iKeepSailingLabel.setText("I keep sailing");
        startingLabel.setText("Starting");
        nowRadioButton.setText("Now");
        withTheGRIBRadioButton.setText("With the GRIB");
        headingRadioButton.setText("Heading");
        headingRadioButton.addChangeListener(e -> hp.setEnabled(headingRadioButton.isSelected()));
        twaRadioButton.setText("TWA");
        twaRadioButton.addChangeListener(e -> tp.setEnabled(twaRadioButton.isSelected()));
        tp.setEnabled(false);
        gribExhaustedRadioButton.setText("Until GRIB exhausted");
        duringRadioButton.setText("During ");
        duringRadioButton.addChangeListener(e -> dp.setEnabled(duringRadioButton.isSelected()));
        polarFactorLabel.setText("Polar Factor:");
        stepLabel.setText("Step (in hours):");
        this.add(jPanelOne, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelOne.add(latLabel,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        jPanelOne.add(lngLabel,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        jPanelOne.add(fromLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        10),
                        0, 0));
        jPanelOne.add(whatIfLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 10, 0), 0, 0));
        jPanelTwo.add(iKeepSailingLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 10), 0, 0));
        jPanelTwo.add(hp /*headingRadioButton*/, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(tp /*twaRadioButton*/, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jPanelTwo, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(startingLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 10), 0, 0));
        jPanelTwo.add(nowRadioButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(withTheGRIBRadioButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(gep /*gribExhaustedRadioButton*/, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(dp /*duringRadioButton*/, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        jPanelTwo.add(jSeparator, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(polarFactorLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        polarFactorTextField.setHorizontalAlignment(JTextField.RIGHT);
        polarFactorTextField.setText(df.format(((Double) ParamPanel.data[ParamData.POLAR_SPEED_FACTOR][ParamData.VALUE_INDEX]).doubleValue()));
        jPanelTwo.add(polarFactorTextField, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanelTwo.add(stepLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        stepJSpinner.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            Integer ds = (Integer) stepJSpinner.getValue();
            stepJSpinner.setValue(ds + (notches * -1));
        });

        jPanelTwo.add(stepJSpinner, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        dp.setEnabled(false);
    }

    public void setFromPos(GeoPoint gp) {
        latLabel.setText(GeomUtil.decToSex(gp.getLatitude(), GeomUtil.SWING, GeomUtil.NS));
        lngLabel.setText(GeomUtil.decToSex(gp.getLongitude(), GeomUtil.SWING, GeomUtil.EW));
    }

    public int getRoutingStep() {
        return (Integer) stepJSpinner.getValue();
    }

    public double getPolarFactor() throws Exception {
//  return Double.parseDouble(polarFactorTextField.getText());
        return df.parse(polarFactorTextField.getText()).doubleValue();
    }


    class HeadingPanel extends JPanel {
        JTextField headingField = new JTextField();
        JLabel trueLabel = new JLabel(" true");

        public HeadingPanel() {
            init();
        }

        private void init() {
            headingField.setPreferredSize(new Dimension(40, 20));
            headingField.setHorizontalAlignment(JTextField.RIGHT);
            this.add(headingRadioButton, null);
            this.add(headingField, null);
            this.add(trueLabel);
        }

        public void setEnabled(boolean b) {
            headingField.setEnabled(b);
            trueLabel.setEnabled(b);
            this.repaint();
        }

        public int getHeading() {
            int hdg = 0;
            try {
                hdg = Integer.parseInt(headingField.getText());
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(nfe);
            }
            return hdg;
        }
    }


    class TWAPanel extends JPanel {
        JTextField twaField = new JTextField();
        JRadioButton starboardRadioButton = new JRadioButton("Stbd");
        JRadioButton portRadioButton = new JRadioButton("Port");
        ButtonGroup grp = new ButtonGroup();

        public TWAPanel() {
            init();
        }

        private void init() {
            grp.add(starboardRadioButton);
            grp.add(portRadioButton);
            starboardRadioButton.setSelected(true);
            twaField.setPreferredSize(new Dimension(40, 20));
            twaField.setHorizontalAlignment(JTextField.RIGHT);

            this.add(twaRadioButton, null);
            this.add(twaField, null);
            this.add(starboardRadioButton, null);
            this.add(portRadioButton, null);
        }

        public void setEnabled(boolean b) {
            starboardRadioButton.setEnabled(b);
            portRadioButton.setEnabled(b);
            twaField.setEnabled(b);
        }

        public int getTWA() {
            int twa = 0;
            try {
                twa = Integer.parseInt(twaField.getText());
                if (portRadioButton.isSelected()) {
                  twa = -twa;
                }
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(nfe);
            }
            return twa;
        }
    }


    class DurationPanel extends JPanel {
        JTextField numField = new JTextField();
        JComboBox<String> unitList = new JComboBox<>();

        public DurationPanel() {
            init();
        }

        private void init() {
            numField.setPreferredSize(new Dimension(40, 20));
            numField.setHorizontalAlignment(JTextField.RIGHT);

            unitList.removeAllItems();
            unitList.addItem("Day(s)");
            unitList.addItem("Week(s)");

            this.add(duringRadioButton, null);
            this.add(numField, null);
            this.add(unitList, null);
        }

        public void setEnabled(boolean b) {
            numField.setEnabled(b);
            unitList.setEnabled(b);
        }

        public int getNbDays() {
            int nbd = 0;
            try {
                nbd = Integer.parseInt(numField.getText());
                if (unitList.getSelectedIndex() == 1) {
                  nbd *= 7;
                }
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(nfe);
            }
            return nbd;
        }
    }


    class GRIBExhaustedPanel extends JPanel {
        public GRIBExhaustedPanel() {
            init();
        }

        private void init() {
            this.add(gribExhaustedRadioButton, null);
        }
    }

    public boolean isHeadingSelected() {
        return headingRadioButton.isSelected();
    }

    public boolean isNowSelected() {
        return nowRadioButton.isSelected();
    }

    public boolean isDuringSelected() {
        return duringRadioButton.isSelected();
    }

    public int getHeading() throws Exception {
        int hdg = 0;
        try {
            hdg = hp.getHeading();
        } catch (Exception ex) {
            throw ex;
        }
        return hdg;
    }

    public int getTWA() {
        int twa = 0;
        try {
            twa = tp.getTWA();
        } catch (Exception ex) {
            throw ex;
        }
        return twa;
    }

    public int getNbDays() {
        int nbd = 0;
        try {
            nbd = dp.getNbDays();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return nbd;
    }
}