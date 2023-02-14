package chartview.gui.util.dialog;


import chart.components.ui.ChartPanelInterface;
import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


public class ChartDimensionInputPanel
        extends JPanel {
    private JTextField topLatTextField = new JTextField();
    private JLabel nLabel = new JLabel();
    private JComboBox<String> topLatComboBox = new JComboBox<>();
    private JLabel sLabel = new JLabel();
    private JTextField bottomLatTextField = new JTextField();
    private JComboBox<String> bottomLatComboBox = new JComboBox<>();
    private JLabel wLabel = new JLabel();
    private JLabel eLabel = new JLabel();
    private JComboBox<String> leftLongComboBox = new JComboBox<>();
    private JComboBox<String> rightLongComboBox = new JComboBox<>();
    private JTextField leftLongTextField = new JTextField();
    private JTextField rightLongTextField = new JTextField();
    private JLabel chartItalicLabel = new JLabel();

    private JSeparator jSeparator1 = new JSeparator();
    private JLabel dimensionLabel = new JLabel();
    private JLabel widthLabel = new JLabel();
    private JLabel heightLabel = new JLabel();
    private JFormattedTextField widthFormattedTextField = new JFormattedTextField(new DecimalFormat("####0"));
    private JFormattedTextField heightFormattedTextField = new JFormattedTextField(new DecimalFormat("####0"));
    private JSeparator jSeparator2 = new JSeparator();
    private JLabel offsetLabel = new JLabel();
    private JLabel xOffsetLabel = new JLabel();
    private JLabel yOffsetLabel = new JLabel();
    private JFormattedTextField xOffsetFormattedTextField = new JFormattedTextField(new DecimalFormat("####0"));
    private JFormattedTextField yOffsetFormattedTextField = new JFormattedTextField(new DecimalFormat("####0"));
    private JLabel projectionLabel = new JLabel();
    private JComboBox<String> projectionComboBox = new JComboBox<>();
    private JSeparator jSeparator3 = new JSeparator();
    private JCheckBox showChartCheckBox = new JCheckBox();

    public ChartDimensionInputPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(532, 371));

        topLatTextField.setPreferredSize(new Dimension(120, 20));
        topLatTextField.setHorizontalAlignment(JTextField.RIGHT);
        nLabel.setText("N Lat");
        topLatComboBox.setPreferredSize(new Dimension(80, 20));
        topLatComboBox.addItem("N");
        topLatComboBox.addItem("S");
        sLabel.setText("S Lat");
        bottomLatTextField.setPreferredSize(new Dimension(120, 20));
        bottomLatTextField.setHorizontalAlignment(JTextField.RIGHT);
        bottomLatComboBox.setPreferredSize(new Dimension(80, 20));
        bottomLatComboBox.addItem("N");
        bottomLatComboBox.addItem("S");
        wLabel.setText("W Long");
        eLabel.setText("E Long");
        leftLongComboBox.setPreferredSize(new Dimension(80, 20));
        leftLongComboBox.addItem("E");
        leftLongComboBox.addItem("W");
        rightLongComboBox.setPreferredSize(new Dimension(80, 20));
        rightLongComboBox.addItem("E");
        rightLongComboBox.addItem("W");
        leftLongTextField.setPreferredSize(new Dimension(120, 20));
        leftLongTextField.setHorizontalAlignment(JTextField.RIGHT);
        rightLongTextField.setPreferredSize(new Dimension(120, 20));
        rightLongTextField.setHorizontalAlignment(JTextField.RIGHT);
        chartItalicLabel.setText(WWGnlUtilities.buildMessage("chart"));
        chartItalicLabel.setFont(new Font("Tahoma", 3, 11));

        projectionComboBox.removeAllItems();
        for (ChartPanelInterface.Projection prj : ChartPanelInterface.Projection.values())
            projectionComboBox.addItem(prj.label());

        dimensionLabel.setText("Dimension");
        dimensionLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
        widthLabel.setText("Width");
        heightLabel.setText("Height");
        widthFormattedTextField.setHorizontalAlignment(JTextField.CENTER);
        heightFormattedTextField.setHorizontalAlignment(JTextField.CENTER);
        offsetLabel.setText("Offsets");
        offsetLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
        xOffsetLabel.setText("X");
        yOffsetLabel.setText("Y");
        xOffsetFormattedTextField.setHorizontalAlignment(JTextField.CENTER);
        yOffsetFormattedTextField.setHorizontalAlignment(JTextField.CENTER);
        projectionLabel.setText("Projection");
        showChartCheckBox.setText("Show Chart");
        showChartCheckBox.setSelected(true);
        this.add(topLatTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(nLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(topLatComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(sLabel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        this.add(bottomLatTextField, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(bottomLatComboBox, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(wLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(eLabel, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        this.add(leftLongComboBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(rightLongComboBox, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(leftLongTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(rightLongTextField, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(chartItalicLabel, new GridBagConstraints(0, 0, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jSeparator1, new GridBagConstraints(0, 3, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 0, 5, 0), 0, 0));
        this.add(dimensionLabel, new GridBagConstraints(0, 6, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(widthLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(heightLabel, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(widthFormattedTextField, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(heightFormattedTextField, new GridBagConstraints(4, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jSeparator2, new GridBagConstraints(0, 8, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 0, 5, 0), 0, 0));
        this.add(offsetLabel, new GridBagConstraints(0, 9, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(xOffsetLabel, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(yOffsetLabel, new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(xOffsetFormattedTextField, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(yOffsetFormattedTextField, new GridBagConstraints(4, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(projectionLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(projectionComboBox, new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jSeparator3, new GridBagConstraints(0, 5, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(5, 0, 5, 0), 0, 0));
        this.add(showChartCheckBox, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 0), 0, 0));
    }

    public void setTopLat(double d) {
        this.topLatTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
        if (d > 0) {
          this.topLatComboBox.setSelectedItem("N");
        } else {
          this.topLatComboBox.setSelectedItem("S");
        }
    }

    public void setBottomLat(double d) {
        this.bottomLatTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
        if (d > 0) {
          this.bottomLatComboBox.setSelectedItem("N");
        } else {
          this.bottomLatComboBox.setSelectedItem("S");
        }
    }

    public void setLeftLong(double d) {
        this.leftLongTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
        if (d > 0) {
          this.leftLongComboBox.setSelectedItem("E");
        } else {
          this.leftLongComboBox.setSelectedItem("W");
        }
    }

    public void setRightLong(double d) {
        this.rightLongTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
        if (d > 0) {
          this.rightLongComboBox.setSelectedItem("E");
        } else {
          this.rightLongComboBox.setSelectedItem("W");
        }
    }

    public double getTopLat() {
        double d = 0D;
        try {
//    d = Double.parseDouble(topLatTextField.getText());
            d = WWContext.NF.parse(topLatTextField.getText()).doubleValue();
            if (topLatComboBox.getSelectedItem().equals("S")) {
              d = -d;
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        return d;
    }

    public double getBottomLat() {
        double d = 0D;
        try {
            d = WWContext.NF.parse(bottomLatTextField.getText()).doubleValue();
            if (bottomLatComboBox.getSelectedItem().equals("S")) {
              d = -d;
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        return d;
    }

    public double getLeftLong() {
        double d = 0D;
        try {
            d = WWContext.NF.parse(leftLongTextField.getText()).doubleValue();
            if (leftLongComboBox.getSelectedItem().equals("W")) {
              d = -d;
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        return d;
    }

    public double getRightLong() {
        double d = 0D;
        try {
            d = WWContext.NF.parse(rightLongTextField.getText()).doubleValue();
            if (rightLongComboBox.getSelectedItem().equals("W")) {
              d = -d;
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        return d;
    }

    public int getChartWidth() {
        return Integer.parseInt(widthFormattedTextField.getText());
    }

    public int getChartHeight() {
        return Integer.parseInt(heightFormattedTextField.getText());
    }

    public int getXOffset() {
        return Integer.parseInt(xOffsetFormattedTextField.getText());
    }

    public int getYOffset() {
        return Integer.parseInt(yOffsetFormattedTextField.getText());
    }

    public void setChartWidth(int i) {
        widthFormattedTextField.setText(Integer.toString(i));
    }

    public void setChartHeight(int i) {
        heightFormattedTextField.setText(Integer.toString(i));
    }

    public void setXOffset(int i) {
        xOffsetFormattedTextField.setText(Integer.toString(i));
    }

    public void setYOffset(int i) {
        yOffsetFormattedTextField.setText(Integer.toString(i));
    }

    public void setProjection(int i) {
        for (ChartPanelInterface.Projection prj : ChartPanelInterface.Projection.values()) {
            if (i == prj.index()) {
                projectionComboBox.setSelectedItem(prj.label());
                break;
            }
        }
    }

    public int getProjection() {
        String str = (String) projectionComboBox.getSelectedItem();
        int index = -1;
        for (ChartPanelInterface.Projection prj : ChartPanelInterface.Projection.values()) {
            if (str.equals(prj.label())) {
                index = prj.index();
                break;
            }
        }
        return index;
    }

    public void setShowChart(boolean b) {
        showChartCheckBox.setSelected(b);
    }

    public boolean getShowChart() {
        return showChartCheckBox.isSelected();
    }
}
