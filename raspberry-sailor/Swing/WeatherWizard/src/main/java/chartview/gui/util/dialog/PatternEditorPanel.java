package chartview.gui.util.dialog;


import chartview.gui.right.CommandPanel;
import chartview.gui.util.TableResizeValue;
import chartview.gui.util.param.ParamPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PatternEditorPanel
        extends JPanel {
    private final FaxPatternEditTablePanel faxPatternEditTablePanel = new FaxPatternEditTablePanel();
    private final GRIBPatternEditorPanel gribPatternEditorPanel = new GRIBPatternEditorPanel();
    private final ChartDimensionInputPanel chartDimensionEditorPanel = new ChartDimensionInputPanel();

    private transient Object[][] faxData;
    private transient Object[][] gribData;
    private boolean grib = false;
    private final JCheckBox fitColumnsCheckBox = new JCheckBox();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel authorLabel = new JLabel();
    private final JTextField authorTextField = new JTextField();
    private final JPanel jPanel1 = new JPanel(new GridBagLayout());
    private final JLabel faxDisplayLabel = new JLabel();
    private final JRadioButton checkBoxOptionRadioButton = new JRadioButton();
    private final JRadioButton radioButtonOptionRadioButton = new JRadioButton();
    private final ButtonGroup bg = new ButtonGroup();

    public PatternEditorPanel(String author,
                              int projection,
                              double northBoundary,
                              double southBoundary,
                              double eastBoundary,
                              double westBoundary,
                              boolean showChart,
                              int chartWidth,
                              int chartHeight,
                              int xOffset,
                              int yOffset,
                              Object[][] f,
                              Object[][] g,
                              int twoDSmooth,
                              int timeSmooth) {
        authorTextField.setText(author);

        chartDimensionEditorPanel.setProjection(projection);
        chartDimensionEditorPanel.setTopLat(northBoundary);
        chartDimensionEditorPanel.setBottomLat(southBoundary);
        chartDimensionEditorPanel.setLeftLong(westBoundary);
        chartDimensionEditorPanel.setRightLong(eastBoundary);
        chartDimensionEditorPanel.setChartWidth(chartWidth);
        chartDimensionEditorPanel.setChartHeight(chartHeight);
        chartDimensionEditorPanel.setXOffset(xOffset);
        chartDimensionEditorPanel.setYOffset(yOffset);
        chartDimensionEditorPanel.setShowChart(showChart);

        faxData = f;
        gribData = g;
        faxPatternEditTablePanel.setData(faxData);
        gribPatternEditorPanel.setData((String) gribData[0][0],
                (String) gribData[0][1],
                (ParamPanel.DataDirectory) gribData[0][2],
                (String) gribData[0][3],
                (String) gribData[0][4],
                (String) gribData[0][5],
                (Boolean) gribData[0][6],
                (Boolean) gribData[0][7],
                (Boolean) gribData[0][8],
                (Boolean) gribData[0][9],
                (Boolean) gribData[0][10],
                (Boolean) gribData[0][11],
                (Boolean) gribData[0][12],
                (Boolean) gribData[0][13],
                (Boolean) gribData[0][14],
                (Boolean) gribData[0][15],
                (Boolean) gribData[0][16],
                (Boolean) gribData[0][17],
                (Boolean) gribData[0][18],
                (Boolean) gribData[0][19],
                (Boolean) gribData[0][20],
                (Boolean) gribData[0][21],
                (Boolean) gribData[0][22],
                twoDSmooth,
                timeSmooth);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(619, 687));
        this.setPreferredSize(new Dimension(619, 687));
        fitColumnsCheckBox.setText("Auto-resize Columns"); // LOCALIZE
        fitColumnsCheckBox.addActionListener(e -> fitColumnsCheckBox_actionPerformed(e));
        this.add(chartDimensionEditorPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(faxPatternEditTablePanel, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(fitColumnsCheckBox, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(gribPatternEditorPanel,
                new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), -80, 0));
        this.add(authorLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0,
                        10), 0,
                        0));
        this.add(authorTextField,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jPanel1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(faxDisplayLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        bg.add(checkBoxOptionRadioButton);
        bg.add(radioButtonOptionRadioButton);
        jPanel1.add(checkBoxOptionRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(radioButtonOptionRadioButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        fitColumnsCheckBox.setSelected(faxPatternEditTablePanel.getTableResize() == TableResizeValue.ON);
        authorLabel.setText("Author");  // LOCALIZE
        authorLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
        faxDisplayLabel.setText("Fax Display Option");
        checkBoxOptionRadioButton.setText("Check Box");
        checkBoxOptionRadioButton.setSelected(true);
        radioButtonOptionRadioButton.setText("Radio Button");
    }

    public void setFaxData(Object[][] faxData) {
        faxPatternEditTablePanel.setData(faxData);
        this.faxData = faxData;
    }

    public Object[][] getFaxData() {
        faxData = faxPatternEditTablePanel.getData();
        return faxData;
    }

    public void setGribData(Object[][] gribData) {
        this.gribData = gribData;
        gribPatternEditorPanel.setData((String) gribData[0][0],
                (String) gribData[0][1],
                (ParamPanel.DataDirectory) gribData[0][2],
                (String) gribData[0][3],
                (String) gribData[0][4],
                (String) gribData[0][5],
                (Boolean) gribData[0][6],
                (Boolean) gribData[0][7],
                (Boolean) gribData[0][8],
                (Boolean) gribData[0][9],
                (Boolean) gribData[0][10],
                (Boolean) gribData[0][11],
                (Boolean) gribData[0][12],
                (Boolean) gribData[0][13],
                (Boolean) gribData[0][14],
                (Boolean) gribData[0][15],
                (Boolean) gribData[0][16],
                (Boolean) gribData[0][17],
                (Boolean) gribData[0][18],
                (Boolean) gribData[0][19],
                (Boolean) gribData[0][20],
                (Boolean) gribData[0][21],
                (Boolean) gribData[0][22]);
    }

    public Object[][] getGribData() {
        gribData = new Object[][]{{gribPatternEditorPanel.getHint(),
                gribPatternEditorPanel.getRequest(),
                gribPatternEditorPanel.getDir(),
                gribPatternEditorPanel.getPrefix(),
                gribPatternEditorPanel.getPattern(),
                gribPatternEditorPanel.getExtension(),
                gribPatternEditorPanel.getPRMSLData(), // was JustWind
                gribPatternEditorPanel.get500MBData(),
                gribPatternEditorPanel.getWAVESData(),
                gribPatternEditorPanel.getTEMPData(),
                gribPatternEditorPanel.getPRATEData(),
                gribPatternEditorPanel.getTWS3D(),
                gribPatternEditorPanel.getPRMSL3D(),
                gribPatternEditorPanel.get500MB3D(),
                gribPatternEditorPanel.getWAVES3D(),
                gribPatternEditorPanel.getTEMP3D(),
                gribPatternEditorPanel.getPRATE3D(),
                gribPatternEditorPanel.getTWSContour(),
                gribPatternEditorPanel.getPRMSLContour(),
                gribPatternEditorPanel.get500MBContour(),
                gribPatternEditorPanel.getWAVESContour(),
                gribPatternEditorPanel.getTEMPContour(),
                gribPatternEditorPanel.getPRATEContour()
        }
        };
        return gribData;
    }

    public void setGrib(boolean grib) {
        this.grib = grib;
        gribPatternEditorPanel.setGribOption(grib);
    }

    public boolean isGrib() {
        grib = gribPatternEditorPanel.getGribOption();
        return grib;
    }

    public double getTopLat() {
        return chartDimensionEditorPanel.getTopLat();
    }

    public double getBottomLat() {
        return chartDimensionEditorPanel.getBottomLat();
    }

    public double getLeftLong() {
        return chartDimensionEditorPanel.getLeftLong();
    }

    public double getRightLong() {
        return chartDimensionEditorPanel.getRightLong();
    }

    public int getProjection() {
        return chartDimensionEditorPanel.getProjection();
    }

    public int getChartWidth() {
        return chartDimensionEditorPanel.getChartWidth();
    }

    public int getChartHeight() {
        return chartDimensionEditorPanel.getChartHeight();
    }

    public int getXOffset() {
        return chartDimensionEditorPanel.getXOffset();
    }

    public int getYOffset() {
        return chartDimensionEditorPanel.getYOffset();
    }

    private void fitColumnsCheckBox_actionPerformed(ActionEvent e) {
        faxPatternEditTablePanel.setTableResize(fitColumnsCheckBox.isSelected() ? TableResizeValue.ON : TableResizeValue.OFF);
    }

    public boolean getShowChart() {
        return chartDimensionEditorPanel.getShowChart();
    }

    public String getAuthor() {
        return authorTextField.getText();
    }

    public int get2DSmooth() {
        return gribPatternEditorPanel.get2DSmooth();
    }

    public int getTimeSmooth() {
        return gribPatternEditorPanel.getTimeSmooth();
    }

    public void setFaxOption(int fo) {
        radioButtonOptionRadioButton.setSelected(fo == CommandPanel.RADIOBUTTON_OPTION);
        checkBoxOptionRadioButton.setSelected(fo == CommandPanel.CHECKBOX_OPTION);
    }

    public int getFaxOption() {
        if (radioButtonOptionRadioButton.isSelected())
            return CommandPanel.RADIOBUTTON_OPTION;
        else
            return CommandPanel.CHECKBOX_OPTION;
    }
}
