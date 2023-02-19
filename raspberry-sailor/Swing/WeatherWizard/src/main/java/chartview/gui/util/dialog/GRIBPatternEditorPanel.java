package chartview.gui.util.dialog;

import chartview.ctx.WWContext;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.gui.util.tree.JTreeGRIBRequestPanel;
import chartview.util.RelativePath;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public final class GRIBPatternEditorPanel
        extends JPanel {
    final BorderLayout borderLayout1 = new BorderLayout();
    final JPanel topPanel = new JPanel();
    final JPanel bottomPanel = new JPanel();

    static final String HINT = WWGnlUtilities.buildMessage("hint");
    static final String GRIB_REQUEST = WWGnlUtilities.buildMessage("grib-request");
    static final String GRIB_DIR = WWGnlUtilities.buildMessage("directory");
    static final String GRIB_PREFIX = WWGnlUtilities.buildMessage("prefix");
    static final String GRIB_PATTERN = WWGnlUtilities.buildMessage("pattern");
    static final String GRIB_EXT = WWGnlUtilities.buildMessage("extension");
    static final String JUST_WIND = WWGnlUtilities.buildMessage("just-wind");

    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel titleLabel = new JLabel();
    JCheckBox gribCheckBox = new JCheckBox();
    private final JLabel hintLabel = new JLabel();
    private final JTextField hintTextField = new JTextField();
    private final JLabel requestLabel = new JLabel();
    private final JLabel dirLabel = new JLabel();
    private final JLabel prefixLabel = new JLabel();
    private final JTextField prefixTextField = new JTextField();
    private final JLabel patternLabel = new JLabel();
    private final JTextField patternTextField = new JTextField();
    private final JLabel extLabel = new JLabel();
    private final JTextField extensionTextField = new JTextField();
    private final JPanel requestPanel = new JPanel();
    private final JTextField requestTextField = new JTextField();
    private final JButton requestButton = new JButton();
    private final BorderLayout borderLayout3 = new BorderLayout();

    private final JTextField directoryTextField = new JTextField();
    private final JButton directoryButton = new JButton();
    private final JPanel directoryPanel = new JPanel();
    private final BorderLayout borderLayout2 = new BorderLayout();
//private JCheckBox justWindCheckBox = new JCheckBox(JUST_WIND);

    private final JPanel gribDataOptionPanel = new JPanel();

    private final JCheckBox prmslDataCheckBox = new JCheckBox();
    private final JCheckBox mb500DataCheckBox = new JCheckBox();
    private final JCheckBox wavesDataCheckBox = new JCheckBox();
    private final JCheckBox temperatureDataCheckBox = new JCheckBox();
    private final JCheckBox prateDataCheckBox = new JCheckBox();

    private final JCheckBox tws3DCheckBox = new JCheckBox();
    private final JCheckBox prmsl3DCheckBox = new JCheckBox();
    private final JCheckBox mb5003DCheckBox = new JCheckBox();
    private final JCheckBox waves3DCheckBox = new JCheckBox();
    private final JCheckBox temperature3DCheckBox = new JCheckBox();
    private final JCheckBox prate3DCheckBox = new JCheckBox();

    private final JCheckBox contourTWSCheckBox = new JCheckBox();
    private final JCheckBox contourPRMSLCheckBox = new JCheckBox();
    private final JCheckBox contourMB500CheckBox = new JCheckBox();
    private final JCheckBox contourWavesCheckBox = new JCheckBox();
    private final JCheckBox contourTemperatureCheckBox = new JCheckBox();
    private final JCheckBox contourPrateCheckBox = new JCheckBox();
    private final JLabel twoDSmoothLabel = new JLabel();
    private final JLabel timeSmoothLabel = new JLabel();
    private final transient SpinnerModel model2D = new SpinnerNumberModel(1, 1, 50, 1);
    private final JSpinner twoDSpinner = new JSpinner(model2D);
    private final transient SpinnerModel modelTime = new SpinnerNumberModel(1, 1, 50, 1);
    private final JSpinner timeSpinner = new JSpinner(modelTime);

    public GRIBPatternEditorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setMinimumSize(new Dimension(540, 200));
        this.setSize(new Dimension(540, 200));
        this.setPreferredSize(new Dimension(540, 200));
        bottomPanel.setLayout(gridBagLayout1);
        topPanel.setLayout(new BorderLayout());
        gribCheckBox.setText("GRIB");
        gribCheckBox.addActionListener(this::gribCheckBox_actionPerformed);
        hintLabel.setText(HINT);
        hintTextField.setPreferredSize(new Dimension(400, 20));
        requestLabel.setText(GRIB_REQUEST);
        dirLabel.setText(GRIB_DIR);
        prefixLabel.setText(GRIB_PREFIX);
        prefixTextField.setMinimumSize(new Dimension(50, 20));
        prefixTextField.setPreferredSize(new Dimension(50, 20));
        patternLabel.setText(GRIB_PATTERN);
        patternTextField.setMinimumSize(new Dimension(100, 20));
        patternTextField.setPreferredSize(new Dimension(200, 20));
        patternTextField.setSize(new Dimension(200, 20));
        extLabel.setText(GRIB_EXT);
        extensionTextField.setPreferredSize(new Dimension(30, 20));
        extensionTextField.setSize(new Dimension(30, 20));

        requestTextField.setPreferredSize(new Dimension(200, 20));
        requestTextField.setSize(new Dimension(200, 20));
        requestButton.setText("...");
        requestButton.setActionCommand("requestButton");
        requestButton.setSize(new Dimension(40, 22));
        requestButton.setPreferredSize(new Dimension(40, 22));
        requestButton.setMaximumSize(new Dimension(40, 22));
        requestButton.setMinimumSize(new Dimension(40, 22));
        requestButton.addActionListener(this::requestButton_actionPerformed);
        requestPanel.setPreferredSize(new Dimension(300, 22));
        requestPanel.setSize(new Dimension(300, 32));
        requestPanel.setLayout(borderLayout3);
        requestPanel.setMinimumSize(new Dimension(300, 22));
        //  justWindCheckBox.setText(JUST_WIND);
        requestPanel.add(requestTextField, BorderLayout.CENTER);
        requestPanel.add(requestButton, BorderLayout.EAST);

        directoryTextField.setPreferredSize(new Dimension(200, 20));
        directoryTextField.setSize(new Dimension(200, 20));
        directoryButton.setText("...");
        directoryButton.setActionCommand("directoryButton");
        directoryButton.setSize(new Dimension(40, 22));
        directoryButton.setPreferredSize(new Dimension(40, 22));
        directoryButton.setMaximumSize(new Dimension(40, 22));
        directoryButton.setMinimumSize(new Dimension(40, 22));
        directoryButton.addActionListener(this::directoryButton_actionPerformed);
        directoryPanel.setPreferredSize(new Dimension(300, 22));
        directoryPanel.setSize(new Dimension(300, 32));
        directoryPanel.setLayout(borderLayout2);
        directoryPanel.setMinimumSize(new Dimension(300, 22));
//  justWindCheckBox.setText(JUST_WIND);
        directoryPanel.add(directoryTextField, BorderLayout.CENTER);
        directoryPanel.add(directoryButton, BorderLayout.EAST);


        titleLabel.setText(WWGnlUtilities.buildMessage("provide-grib-detail"));
        topPanel.add(gribCheckBox, BorderLayout.WEST);
//  topPanel.add(justWindCheckBox, BorderLayout.CENTER);
        topPanel.add(titleLabel, BorderLayout.EAST);

        prmslDataCheckBox.setText("PRMSL");
        prmslDataCheckBox.setSelected(true);
        prmslDataCheckBox.addActionListener(e -> {
            prmsl3DCheckBox.setEnabled(prmslDataCheckBox.isSelected());
            contourPRMSLCheckBox.setEnabled(prmslDataCheckBox.isSelected());
        });
        mb500DataCheckBox.setText("500mb");
        mb500DataCheckBox.setSelected(true);
        mb500DataCheckBox.addActionListener(e -> {
            mb5003DCheckBox.setEnabled(mb500DataCheckBox.isSelected());
            contourMB500CheckBox.setEnabled(mb500DataCheckBox.isSelected());
        });
        wavesDataCheckBox.setText("Waves");
        wavesDataCheckBox.setSelected(true);
        wavesDataCheckBox.addActionListener(e -> {
            waves3DCheckBox.setEnabled(wavesDataCheckBox.isSelected());
            contourWavesCheckBox.setEnabled(wavesDataCheckBox.isSelected());
        });
        temperatureDataCheckBox.setText("AirTmp");
        temperatureDataCheckBox.setSelected(true);
        temperatureDataCheckBox.addActionListener(e -> {
            temperature3DCheckBox.setEnabled(temperatureDataCheckBox.isSelected());
            contourTemperatureCheckBox.setEnabled(temperatureDataCheckBox.isSelected());
        });
        prateDataCheckBox.setText("prate");
        prateDataCheckBox.setSelected(true);
        prateDataCheckBox.addActionListener(e -> {
            prate3DCheckBox.setEnabled(prateDataCheckBox.isSelected());
            contourPrateCheckBox.setEnabled(prateDataCheckBox.isSelected());
        });

        tws3DCheckBox.setText("3D TWS");
        tws3DCheckBox.setToolTipText("True Wind Speed");
        tws3DCheckBox.setSelected(true);
        prmsl3DCheckBox.setText("3D PRMSL");
        prmsl3DCheckBox.setToolTipText("Pressure at Mean Sea Level");
        prmsl3DCheckBox.setSelected(true);
        mb5003DCheckBox.setText("3D 500mb");
        mb5003DCheckBox.setSelected(true);
        waves3DCheckBox.setText("3D Waves");
        waves3DCheckBox.setSelected(true);
        temperature3DCheckBox.setText("3D Temp.");
        temperature3DCheckBox.setSelected(true);
        prate3DCheckBox.setText("3D prate");
        prate3DCheckBox.setSelected(true);
        prate3DCheckBox.setToolTipText("Precipitation Rate");

        // LOCALIZE, at least the tooltips
        contourTWSCheckBox.setText("TWS Cont.");
        contourPRMSLCheckBox.setText("PRMSL Cont.");
        contourPRMSLCheckBox.setActionCommand("contourPRMSLCheckBox");
        contourMB500CheckBox.setText("500mb Cont.");
        contourWavesCheckBox.setText("Waves Cont.");
        contourTemperatureCheckBox.setText("Temp Cont.");
        contourPrateCheckBox.setText("prate Cont.");

        twoDSmoothLabel.setText("2D Smooth"); // LOCALIZE
        timeSmoothLabel.setText("Time Smooth"); // LOCALIZE
        twoDSpinner.setMinimumSize(new Dimension(50, 19));
        twoDSpinner.setPreferredSize(new Dimension(50, 19));
        timeSpinner.setMinimumSize(new Dimension(50, 19));
        timeSpinner.setPreferredSize(new Dimension(50, 19));
        gribDataOptionPanel.setLayout(new GridBagLayout());

        gribDataOptionPanel.add(prmslDataCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(mb500DataCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(wavesDataCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(temperatureDataCheckBox, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(prateDataCheckBox, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        gribDataOptionPanel.add(tws3DCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(prmsl3DCheckBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(mb5003DCheckBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(waves3DCheckBox, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(temperature3DCheckBox, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(prate3DCheckBox, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        gribDataOptionPanel.add(contourPRMSLCheckBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(contourMB500CheckBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(contourWavesCheckBox, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(contourTemperatureCheckBox, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        gribDataOptionPanel.add(contourPrateCheckBox, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        gribDataOptionPanel.add(contourTWSCheckBox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        this.add(topPanel, BorderLayout.NORTH);
        this.add(gribDataOptionPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(hintLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        bottomPanel.add(hintTextField,
                new GridBagConstraints(1, 0, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(requestLabel,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        bottomPanel.add(dirLabel,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        bottomPanel.add(prefixLabel,
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
                        0,
                        0,
                        0),
                        0, 0));
        bottomPanel.add(prefixTextField,
                new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(patternLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 8, 0, 0), 0, 0));
        bottomPanel.add(patternTextField,
                new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(extLabel,
                new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,
                        8,
                        0,
                        0),
                        0, 0));
        bottomPanel.add(extensionTextField,
                new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(requestPanel, new GridBagConstraints(1, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(directoryPanel,
                new GridBagConstraints(1, 2, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(twoDSmoothLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));
        bottomPanel.add(timeSmoothLabel, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(5, 0, 5, 0), 0, 0));
        bottomPanel.add(twoDSpinner, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        bottomPanel.add(timeSpinner, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    public boolean getGribOption() {
        return gribCheckBox.isSelected();
    }

    public void setGribOption(boolean b) {
        gribCheckBox.setSelected(b);
        enableGRIBPanel(gribCheckBox.isSelected());
    }

    private void enableGRIBPanel(boolean b) {
        hintLabel.setEnabled(b);
//  justWindCheckBox.setEnabled(b);

        prmslDataCheckBox.setEnabled(b);
        mb500DataCheckBox.setEnabled(b);
        wavesDataCheckBox.setEnabled(b);
        temperatureDataCheckBox.setEnabled(b);
        prateDataCheckBox.setEnabled(b);

        tws3DCheckBox.setEnabled(b); // Manage this one
        prmsl3DCheckBox.setEnabled(b && prmslDataCheckBox.isSelected());
        mb5003DCheckBox.setEnabled(b && mb500DataCheckBox.isSelected());
        waves3DCheckBox.setEnabled(b && wavesDataCheckBox.isSelected());
        temperature3DCheckBox.setEnabled(b && temperatureDataCheckBox.isSelected());
        prate3DCheckBox.setEnabled(b && prateDataCheckBox.isSelected());

        contourTWSCheckBox.setEnabled(b);
        contourPRMSLCheckBox.setEnabled(b && prmslDataCheckBox.isSelected());
        contourMB500CheckBox.setEnabled(b && mb500DataCheckBox.isSelected());
        contourWavesCheckBox.setEnabled(b && wavesDataCheckBox.isSelected());
        contourTemperatureCheckBox.setEnabled(b && temperatureDataCheckBox.isSelected());
        contourPrateCheckBox.setEnabled(b && prateDataCheckBox.isSelected());

        requestLabel.setEnabled(b);
        dirLabel.setEnabled(b);
        prefixLabel.setEnabled(b);
        patternLabel.setEnabled(b);
        extLabel.setEnabled(b);
        hintTextField.setEnabled(b);
        requestTextField.setEnabled(b);
        directoryTextField.setEnabled(b);
        directoryButton.setEnabled(b);
        prefixTextField.setEnabled(b);
        patternTextField.setEnabled(b);
        extensionTextField.setEnabled(b);

        twoDSmoothLabel.setEnabled(b);
        twoDSpinner.setEnabled(b);
        timeSmoothLabel.setEnabled(b);
        timeSpinner.setEnabled(b);
    }

    private void gribCheckBox_actionPerformed(ActionEvent e) {
        enableGRIBPanel(gribCheckBox.isSelected());
    }

    private void directoryButton_actionPerformed(ActionEvent e) {
        String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
        String s = WWGnlUtilities.chooseFile(this,
                JFileChooser.DIRECTORIES_ONLY,
                new String[]{""},
                "GRIBs",
                firstDir,
                "Save",
                "Choose Directory");
        if (s != null && s.trim().length() > 0) {
          directoryTextField.setText(RelativePath.getRelativePath(System.getProperty("user.dir"), s).replace(File.separatorChar, '/'));
        }
    }

    private void requestButton_actionPerformed(ActionEvent e) {
        String request = requestTextField.getText();
        JTreeGRIBRequestPanel dialog = new JTreeGRIBRequestPanel(true);
        dialog.parseGRIBRequest(request);
        int resp = JOptionPane.showConfirmDialog(this, dialog, "GRIB Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE); // LOCALIZE
        if (resp == JOptionPane.OK_OPTION) {
            request = dialog.getCurrentlySelectedRequest();
            requestTextField.setText(request);
        }
    }

    public void setData(String h,
                        String rq,
                        ParamPanel.DataDirectory d,
                        String pre,
                        String patt,
                        String ext,
                        Boolean prmslData,
                        Boolean mb500Data,
                        Boolean wavesData,
                        Boolean tempData,
                        Boolean prateData,
                        Boolean tws3D,
                        Boolean prmsl3D,
                        Boolean mb5003D,
                        Boolean waves3D,
                        Boolean temp3D,
                        Boolean prate3D,
                        Boolean contourTWS,
                        Boolean contourPRMSL,
                        Boolean contour500MB,
                        Boolean contourWAVES,
                        Boolean contourTEMP,
                        Boolean contourPRATE) {
        setData(h, rq, d, pre, patt, ext, prmslData, mb500Data, wavesData, tempData, prateData, tws3D, prmsl3D, mb5003D, waves3D, temp3D, prate3D, contourTWS, contourPRMSL, contour500MB, contourWAVES, contourTEMP, contourPRATE, 1, 1);
    }

    public void setData(String h,
                        String rq,
                        ParamPanel.DataDirectory d,
                        String pre,
                        String patt,
                        String ext,
                        Boolean prmslData,
                        Boolean mb500Data,
                        Boolean wavesData,
                        Boolean tempData,
                        Boolean prateData,
                        Boolean tws3D,
                        Boolean prmsl3D,
                        Boolean mb5003D,
                        Boolean waves3D,
                        Boolean temp3D,
                        Boolean prate3D,
                        Boolean contourTWS,
                        Boolean contourPRMSL,
                        Boolean contour500MB,
                        Boolean contourWAVES,
                        Boolean contourTEMP,
                        Boolean contourPRATE,
                        int two2Smooth,
                        int timeSmooth) {
        setHint(h);
        setRequest(rq);
        setDir(d);
        setPrefix(pre);
        setPattern(patt);
        setExtension(ext);
//  setJustWind(b.booleanValue());
        setPRMSLData(prmslData);
        set500MBData(mb500Data);
        setWAVESData(wavesData);
        setTEMPData(tempData);
        setPRATEData(prateData);
        setTWS3D(tws3D);
        setPRMSL3D(prmsl3D);
        set500MB3D(mb5003D);
        setWAVES3D(waves3D);
        setTEMP3D(temp3D);
        setPRATE3D(prate3D);
        setTWSContour(contourTWS);
        setPRMSLContour(contourPRMSL);
        set500MBContour(contour500MB);
        setWAVESContour(contourWAVES);
        setTEMPContour(contourTEMP);
        setPRATEContour(contourPRATE);

        set2DSmooth(two2Smooth);
        setTimeSmooth(timeSmooth);
    }

//  public void setJustWind(boolean b) 
//  { 
//    justWindCheckBox.setSelected(b); 
//  }  

    public void setPRMSLData(boolean b) {
        prmslDataCheckBox.setSelected(b);
        prmsl3DCheckBox.setEnabled(b);
        contourPRMSLCheckBox.setEnabled(b);
    }

    public void set500MBData(boolean b) {
        mb500DataCheckBox.setSelected(b);
        mb5003DCheckBox.setEnabled(b);
        contourMB500CheckBox.setEnabled(b);
    }

    public void setWAVESData(boolean b) {
        wavesDataCheckBox.setSelected(b);
        waves3DCheckBox.setEnabled(b);
        contourWavesCheckBox.setEnabled(b);
    }

    public void setTEMPData(boolean b) {
        temperatureDataCheckBox.setSelected(b);
        temperature3DCheckBox.setEnabled(b);
        contourTemperatureCheckBox.setEnabled(b);
    }

    public void setPRATEData(boolean b) {
        prateDataCheckBox.setSelected(b);
        prate3DCheckBox.setEnabled(b);
        contourPrateCheckBox.setEnabled(b);
    }

    public void setTWS3D(boolean b) {
        tws3DCheckBox.setSelected(b);
    }

    public void setPRMSL3D(boolean b) {
        prmsl3DCheckBox.setSelected(b);
    }

    public void set500MB3D(boolean b) {
        mb5003DCheckBox.setSelected(b);
    }

    public void setWAVES3D(boolean b) {
        waves3DCheckBox.setSelected(b);
    }

    public void setTEMP3D(boolean b) {
        temperature3DCheckBox.setSelected(b);
    }

    public void setPRATE3D(boolean b) {
        prate3DCheckBox.setSelected(b);
    }

    public void setTWSContour(boolean b) {
        contourTWSCheckBox.setSelected(b);
    }

    public void setPRMSLContour(boolean b) {
        contourPRMSLCheckBox.setSelected(b);
    }

    public void set500MBContour(boolean b) {
        contourMB500CheckBox.setSelected(b);
    }

    public void setWAVESContour(boolean b) {
        contourWavesCheckBox.setSelected(b);
    }

    public void setTEMPContour(boolean b) {
        contourTemperatureCheckBox.setSelected(b);
    }

    public void setPRATEContour(boolean b) {
        contourPrateCheckBox.setSelected(b);
    }

    public void set2DSmooth(int i) {
        twoDSpinner.setValue(i);
    }

    public void setTimeSmooth(int i) {
        timeSpinner.setValue(i);
    }

    public boolean getPRMSLData() {
        return prmslDataCheckBox.isSelected();
    }

    public boolean get500MBData() {
        return mb500DataCheckBox.isSelected();
    }

    public boolean getWAVESData() {
        return wavesDataCheckBox.isSelected();
    }

    public boolean getTEMPData() {
        return temperatureDataCheckBox.isSelected();
    }

    public boolean getPRATEData() {
        return prateDataCheckBox.isSelected();
    }

    public boolean getTWS3D() {
        return tws3DCheckBox.isSelected() && tws3DCheckBox.isEnabled();
    }

    public boolean getPRMSL3D() {
        return prmsl3DCheckBox.isSelected() && prmsl3DCheckBox.isEnabled();
    }

    public boolean get500MB3D() {
        return mb5003DCheckBox.isSelected() && mb5003DCheckBox.isEnabled();
    }

    public boolean getWAVES3D() {
        return waves3DCheckBox.isSelected() && waves3DCheckBox.isEnabled();
    }

    public boolean getTEMP3D() {
        return temperature3DCheckBox.isSelected() && temperature3DCheckBox.isEnabled();
    }

    public boolean getPRATE3D() {
        return prate3DCheckBox.isSelected() && prate3DCheckBox.isEnabled();
    }

    public boolean getTWSContour() {
        return contourTWSCheckBox.isSelected() && contourTWSCheckBox.isEnabled();
    }

    public boolean getPRMSLContour() {
        return contourPRMSLCheckBox.isSelected() && contourPRMSLCheckBox.isEnabled();
    }

    public boolean get500MBContour() {
        return contourMB500CheckBox.isSelected() && contourMB500CheckBox.isEnabled();
    }

    public boolean getWAVESContour() {
        return contourWavesCheckBox.isSelected() && contourWavesCheckBox.isEnabled();
    }

    public boolean getTEMPContour() {
        return contourTemperatureCheckBox.isSelected() && contourTemperatureCheckBox.isEnabled();
    }

    public boolean getPRATEContour() {
        return contourPrateCheckBox.isSelected() && contourPrateCheckBox.isEnabled();
    }

    public void setHint(String s) {
        hintTextField.setText(s);
    }

    public void setRequest(String s) {
        requestTextField.setText(s);
    }

    public void setDir(ParamPanel.DataDirectory d) {
        directoryTextField.setText(d.toString());
    }

    public void setPrefix(String s) {
        prefixTextField.setText(s);
    }

    public void setPattern(String s) {
        patternTextField.setText(s);
    }

    public void setExtension(String s) {
        extensionTextField.setText(s);
    }

    //public boolean getJustWind() { return justWindCheckBox.isSelected(); }
    public String getHint() {
        return hintTextField.getText();
    }

    public String getRequest() {
        return requestTextField.getText();
    }

    public ParamPanel.DataDirectory getDir() {
        return new ParamPanel.DataDirectory("GRIBs",
                directoryTextField.getText());
    }

    public String getPrefix() {
        return prefixTextField.getText();
    }

    public String getPattern() {
        return patternTextField.getText();
    }

    public String getExtension() {
        return extensionTextField.getText();
    }

    public int get2DSmooth() {
        return (Integer) twoDSpinner.getValue();
    }

    public int getTimeSmooth() {
        return (Integer) timeSpinner.getValue();
    }
}
