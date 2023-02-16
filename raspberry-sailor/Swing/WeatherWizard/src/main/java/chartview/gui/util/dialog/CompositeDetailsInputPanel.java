package chartview.gui.util.dialog;

import chartview.ctx.WWContext;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.gui.util.tree.JTreeGRIBRequestPanel;

import chartview.util.GPXUtil;
import chartview.util.WWGnlUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.KeyEvent;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;

import java.util.List;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class CompositeDetailsInputPanel
     extends JPanel
{
  private JTextField topLatTextField = new JTextField();
  private JLabel jLabel1 = new JLabel();
  private JComboBox topLatComboBox = new JComboBox();
  private JLabel jLabel2 = new JLabel();
  private JTextField bottomLatTextField = new JTextField();
  private JComboBox bottomLatComboBox = new JComboBox();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JComboBox leftLongComboBox = new JComboBox();
  private JComboBox rightLongComboBox = new JComboBox();
  private JTextField leftLongTextField = new JTextField();
  private JTextField rightLongTextField = new JTextField();
  private JLabel gribFileLabel = new JLabel();
  private JTextField gribFileTextField = new JTextField();
  private JButton gribButton = new JButton();
  private JLabel jLabel7 = new JLabel();
  private JCheckBox gribSizeCheckBox = new JCheckBox();
  private FaxTablePanel faxTablePanel = new FaxTablePanel();
  private JCheckBox justWindCheckBox = new JCheckBox();
  private JCheckBox contourCheckBox  = new JCheckBox();
  private JCheckBox threeDCheckBox   = new JCheckBox();
  
  private JPanel gribDataOptionPanel = new JPanel();
//private JPanel grib3DOptionPanel = new JPanel();
//private JPanel gribContourOptionPanel = new JPanel();
  
  private JCheckBox prmslDataCheckBox = new JCheckBox();
  private JCheckBox mb500DataCheckBox = new JCheckBox();
  private JCheckBox wavesDataCheckBox = new JCheckBox();
  private JCheckBox temperatureDataCheckBox = new JCheckBox();
  private JCheckBox prateDataCheckBox = new JCheckBox();
  
  private JCheckBox tws3DCheckBox = new JCheckBox();
  private JCheckBox prmsl3DCheckBox = new JCheckBox();
  private JCheckBox mb5003DCheckBox = new JCheckBox();
  private JCheckBox waves3DCheckBox = new JCheckBox();
  private JCheckBox temperature3DCheckBox = new JCheckBox();
  private JCheckBox prate3DCheckBox = new JCheckBox();
  
  private JCheckBox contourTWSCheckBox = new JCheckBox();
  private JCheckBox contourPRMSLCheckBox = new JCheckBox();
  private JCheckBox contourMB500CheckBox = new JCheckBox();
  private JCheckBox contourWavesCheckBox = new JCheckBox();
  private JCheckBox contourTemperatureCheckBox = new JCheckBox();
  private JCheckBox contourPrateCheckBox = new JCheckBox();

  private JLabel commentLabel = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea commentTextArea = new JTextArea();
  private JCheckBox gribCheckBox = new JCheckBox();
  private JSeparator jSeparator1 = new JSeparator();
  private JSeparator jSeparator2 = new JSeparator();

  public final static int PRMSL_DATA    =  0;
  public final static int MB500_DATA    =  1;
  public final static int WAVES_DATA    =  2;
  public final static int TEMP_DATA     =  3;
  public final static int PRATE_DATA    =  4;
  
  public final static int TWS_3D        =  5;
  public final static int PRMSL_3D      =  6;
  public final static int MB500_3D      =  7;
  public final static int WAVES_3D      =  8;
  public final static int TEMP_3D       =  9;
  public final static int PRATE_3D      = 10;
  
  public final static int PRMSL_CONTOUR = 11;
  public final static int MB500_CONTOUR = 12;
  public final static int WAVES_CONTOUR = 13;
  public final static int TEMP_CONTOUR  = 14;
  public final static int PRATE_CONTOUR = 15;
  public final static int TWS_CONTOUR   = 16;
  
  private JPanel compositeOne   = new JPanel();
  private JPanel compositeTwo   = new JPanel();
  private JTabbedPane compositeTabbedPane = new JTabbedPane();
  private JPanel gpxPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JCheckBox gpxCheckBox = new JCheckBox();
  private JLabel gpxLabel = new JLabel();
  private JTextField gpxFileNameTextField = new JTextField();
  private JButton gpxBrowseButton = new JButton();
  private JCheckBox upToCheckBox = new JCheckBox();
  private DateTimePanel dtPanel = new DateTimePanel();
  private JLabel fromGPXLabel = new JLabel();

  public CompositeDetailsInputPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
//  this.setLayout(gridBagLayout1);
    this.setLayout(new BorderLayout());
    this.setSize(new Dimension(505, 502));
    
    compositeOne.setLayout(new GridBagLayout());
    compositeOne.setSize(new Dimension(532, 371));
    compositeTwo.setLayout(new GridBagLayout());

//  this.setToolTipText("null");
    gpxPanel.setLayout(gridBagLayout2);
    gpxCheckBox.setText("GPX Data"); // LOCALIZE
    gpxCheckBox.setToolTipText("Get GPX Data from a GPX File"); // LOCALIZE
    gpxCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          gpxCheckBox_actionPerformed(e);
        }
      });
    gpxLabel.setText("GPX File Name:"); // LOCALIZE
    gpxLabel.setEnabled(false);
    gpxFileNameTextField.setMinimumSize(new Dimension(200, 19));
    gpxFileNameTextField.setPreferredSize(new Dimension(200, 19));
    gpxFileNameTextField.setEnabled(false);
    gpxBrowseButton.setText("Browse..."); // LOCALIZE
    gpxBrowseButton.setEnabled(false);
    gpxBrowseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          gpxBrowseButton_actionPerformed(e);
        }
      });
    upToCheckBox.setText("Up to...");
    upToCheckBox.setEnabled(false);
    upToCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          upToCheckBox_actionPerformed(e);
        }
      });
    dtPanel.setEnabled(false);

    fromGPXLabel.setText("From...");
    fromGPXLabel.setEnabled(false);
    topLatTextField.setPreferredSize(new Dimension(60, 20));
    topLatTextField.setHorizontalAlignment(JTextField.RIGHT);
    jLabel1.setText("N Lat");
    topLatComboBox.setPreferredSize(new Dimension(40, 20));
    topLatComboBox.addItem("N");
    topLatComboBox.addItem("S");
    jLabel2.setText("S Lat");
    bottomLatTextField.setPreferredSize(new Dimension(60, 20));
    bottomLatTextField.setHorizontalAlignment(JTextField.RIGHT);
    bottomLatComboBox.setPreferredSize(new Dimension(40, 20));
    bottomLatComboBox.addItem("N");
    bottomLatComboBox.addItem("S");
    jLabel3.setText("W Long");
    jLabel4.setText("E Long");
    leftLongComboBox.setPreferredSize(new Dimension(40, 20));
    leftLongComboBox.addItem("E");
    leftLongComboBox.addItem("W");
    rightLongComboBox.setPreferredSize(new Dimension(40, 20));
    rightLongComboBox.addItem("E");
    rightLongComboBox.addItem("W");
    leftLongTextField.setPreferredSize(new Dimension(60, 20));
    leftLongTextField.setHorizontalAlignment(JTextField.RIGHT);
    rightLongTextField.setPreferredSize(new Dimension(60, 20));
    rightLongTextField.setHorizontalAlignment(JTextField.RIGHT);
    gribFileLabel.setText(WWGnlUtilities.buildMessage("grib-file"));
    gribFileLabel.setFont(new Font("Tahoma", 3, 11));
    gribFileTextField.setMaximumSize(new Dimension(200, 20));
    gribFileTextField.setPreferredSize(new Dimension(200, 20));
    gribFileTextField.setMinimumSize(new Dimension(200, 20));
    gribFileTextField.setToolTipText(WWGnlUtilities.buildMessage("grib-file-tooltip-file"));
    gribButton.setText(WWGnlUtilities.buildMessage("browse"));
    gribButton.setActionCommand("gribButton");
    gribButton.setPreferredSize(new Dimension(80, 22));
    gribButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            gribButton_actionPerformed(e);
          }
        });
    jLabel7.setText(WWGnlUtilities.buildMessage("chart"));
    jLabel7.setFont(new Font("Tahoma", 3, 11));
    gribSizeCheckBox.setText(WWGnlUtilities.buildMessage("set-chart-size-from-grib"));
    gribSizeCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            gribSizeCheckBox_actionPerformed(e);
          }
        });
    faxTablePanel.setPreferredSize(new Dimension(280, 150));
    justWindCheckBox.setText(WWGnlUtilities.buildMessage("just-wind"));
    justWindCheckBox.setActionCommand("justWindCheckBox");
    justWindCheckBox.setToolTipText(WWGnlUtilities.buildMessage("grib-data-tooltip"));
    justWindCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            justWindCheckBox_actionPerformed(e);
          }
        });
    contourCheckBox.setText(WWGnlUtilities.buildMessage("contours"));
    contourCheckBox.setActionCommand("contourCheckBox");
    contourCheckBox.setToolTipText(WWGnlUtilities.buildMessage("contours-tooltip"));
    contourCheckBox.setSelected(true);
    contourCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            contourCheckBox_actionPerformed(e);
          }
        });
    threeDCheckBox.setText(WWGnlUtilities.buildMessage("3D"));
    threeDCheckBox.setActionCommand("threeDCheckBox");
    threeDCheckBox.setToolTipText(WWGnlUtilities.buildMessage("threeD-tooltip"));
    threeDCheckBox.setSelected(true);
    threeDCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            threeDCheckBox_actionPerformed(e);
          }
        });

    prmslDataCheckBox.setText("PRMSL");
    prmslDataCheckBox.setSelected(true);
    prmslDataCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            prmsl3DCheckBox.setEnabled(prmslDataCheckBox.isSelected());
            contourPRMSLCheckBox.setEnabled(prmslDataCheckBox.isSelected());
          }
        });
    mb500DataCheckBox.setText("500mb");
    mb500DataCheckBox.setSelected(true);
    mb500DataCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            mb5003DCheckBox.setEnabled(mb500DataCheckBox.isSelected());
            contourMB500CheckBox.setEnabled(mb500DataCheckBox.isSelected());
          }
        });
    wavesDataCheckBox.setText("Waves");
    wavesDataCheckBox.setSelected(true);
    wavesDataCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            waves3DCheckBox.setEnabled(wavesDataCheckBox.isSelected());
            contourWavesCheckBox.setEnabled(wavesDataCheckBox.isSelected());
          }
        });
    temperatureDataCheckBox.setText("AirTemp");
    temperatureDataCheckBox.setSelected(true);
    temperatureDataCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            temperature3DCheckBox.setEnabled(temperatureDataCheckBox.isSelected());
            contourTemperatureCheckBox.setEnabled(temperatureDataCheckBox.isSelected());
          }
        });
    prateDataCheckBox.setText("prate");
    prateDataCheckBox.setSelected(true);
    prateDataCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            prate3DCheckBox.setEnabled(prateDataCheckBox.isSelected());
            contourPrateCheckBox.setEnabled(prateDataCheckBox.isSelected());
          }
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
    
    // LOCALIZE at least the tooltips    
    contourTWSCheckBox.setText("TWS Cont.");
    contourPRMSLCheckBox.setText("PRMSL Cont.");
    contourPRMSLCheckBox.setActionCommand("contourPRMSLCheckBox");
    contourMB500CheckBox.setText("500mb Cont.");
    contourWavesCheckBox.setText("Waves Cont.");
    contourTemperatureCheckBox.setText("Temp Cont.");
    contourPrateCheckBox.setText("prate Cont.");

    commentLabel.setText(WWGnlUtilities.buildMessage("composite-comment"));
    commentLabel.setFont(new Font("Tahoma", 3, 11));
    
    jScrollPane1.setSize(new Dimension(400, 100));
    jScrollPane1.setMinimumSize(new Dimension(400, 100));
    jScrollPane1.setPreferredSize(new Dimension(400, 100));
//  jScrollPane1.setToolTipText("null");
    gribCheckBox.setText(WWGnlUtilities.buildMessage("grib-request-2"));
    gribCheckBox.setActionCommand("gribCheckBox");
    gribCheckBox.setToolTipText(WWGnlUtilities.buildMessage("grib-checkbox-tooltip"));
    gribCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            gribCheckBox_actionPerformed(e);
          }
        });

    compositeOne.add(gribFileLabel, new GridBagConstraints(0, 2, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(gribFileTextField, new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(gribButton, new GridBagConstraints(4, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(topLatTextField, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(jLabel1, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
    compositeOne.add(topLatComboBox, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(jLabel2, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
    compositeOne.add(bottomLatTextField, new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(bottomLatComboBox, new GridBagConstraints(5, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(jLabel3, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
    compositeOne.add(jLabel4, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 5), 0, 0));
    compositeOne.add(leftLongComboBox, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(rightLongComboBox, new GridBagConstraints(5, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(leftLongTextField, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(rightLongTextField, new GridBagConstraints(4, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(jLabel7, new GridBagConstraints(0, 7, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(gribSizeCheckBox, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 0), 0, 0));
    compositeOne.add(faxTablePanel, new GridBagConstraints(0, 0, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(justWindCheckBox, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 0), 0, 0));
    compositeOne.add(contourCheckBox, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 0), 0, 0));
    compositeOne.add(threeDCheckBox, new GridBagConstraints(5, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 10, 0), 0, 0));
    
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
    compositeOne.add(gribDataOptionPanel, new GridBagConstraints(0, 5, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    //  this.add(grib3DOptionPanel, new GridBagConstraints(0, 6, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    //  this.add(gribContourOptionPanel, new GridBagConstraints(0, 7, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    //  this.add(contourPRMSLCheckBox, new GridBagConstraints(0, 8, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    compositeTwo.add(commentLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    jScrollPane1.getViewport().add(commentTextArea, null);
    compositeTwo.add(jScrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
    // Just Wind by default
    gpxPanel.add(gpxCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    gpxPanel.add(gpxLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    gpxPanel.add(gpxFileNameTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    gpxPanel.add(gpxBrowseButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 2, 0, 0), 0, 0));
    gpxPanel.add(upToCheckBox, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    gpxPanel.add(dtPanel, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
    gpxPanel.add(fromGPXLabel, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    compositeTwo.add(gpxPanel,
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,
                                                                                                                               0,
                                                                                                                               0,
                                                                                                                               0),
                                            0, 0));
    compositeOne.add(gribCheckBox,
                     new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                            new Insets(0, 0, 0, 0), 0, 0));
    compositeOne.add(jSeparator1,
                     new GridBagConstraints(0, 1, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                            new Insets(5, 0, 0, 0), 0, 0));
    compositeOne.add(jSeparator2,
                     new GridBagConstraints(0, 6, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                            new Insets(10, 0, 0, 0), 0, 0));

    //  justWind(true);
    this.add(compositeTabbedPane, BorderLayout.CENTER);
    compositeTabbedPane.add("Dimensions & Documents", compositeOne); // LOCALIZE
    compositeTabbedPane.add("Comments & GPX", compositeTwo); // LOCALIZE
    int keyCodeDimension = KeyEvent.VK_D;
    int keyCodeComments  = KeyEvent.VK_C;
    compositeTabbedPane.setMnemonicAt(0, keyCodeDimension);
    compositeTabbedPane.setMnemonicAt(1, keyCodeComments);
  }

  public void setFaxes(FaxType[] ft)
  {
    int fl = (ft==null?0:ft.length);
    Object[][] faxData = new Object[fl][7];
    try
    {
      for (int i=0; i<fl; i++)
      {
        faxData[i][0] = new Integer(i + 1);
        faxData[i][1] = ft[i];
        faxData[i][2] = ft[i].isShow();
        faxData[i][3] = ft[i].isTransparent();
        faxData[i][4] = ft[i].getOrigin();
        faxData[i][5] = ft[i].getTitle();
        faxData[i][6] = ft[i].getComment();
      }
      faxTablePanel.setData(faxData);
    }
    catch (Exception ex)
    {
      System.out.println("Something might have failed when loading the previous composite...");
    }
  }
  
  public FaxType[] getFaxes()
  {
    Object[][] faxData = faxTablePanel.getData();
    List<FaxType> alft = new ArrayList<FaxType>(faxData.length);
    int arrayLen = 0;
    for (int i=0; i<faxData.length; i++)
    {
      FaxType ft = (FaxType)faxData[i][1];
      if (ft.getValue().trim().length() > 0)
      {
        ft.setRank(((Integer)faxData[i][0]).intValue());
        ft.setShow(((Boolean)faxData[i][2]).booleanValue());
        ft.setTransparent(((Boolean)faxData[i][3]).booleanValue());
        ft.setOrigin((String)faxData[i][4]);
        ft.setTitle((String)faxData[i][5]);
        ft.setComment((String)faxData[i][6]);
        alft.add(ft);
        arrayLen++;
      }
    }
    Collections.<FaxType>sort(alft);
    FaxType[] newFaxData = new FaxType[arrayLen];
    newFaxData = alft.toArray(newFaxData);
    return newFaxData;
  }
  
  public void addNewFaxFileInTable(String faxFile)
  {
    faxTablePanel.addLineInTable(new FaxType(faxFile, Color.black, true, true, 0D, null, null));    
  }
  
  public void setGribFileName(String str)
  {
    gribFileTextField.setText(str);
  }

  public String getGribFileName()
  {
    return gribFileTextField.getText();
  }

  public void setTopLat(double d)
  {
    this.topLatTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
    if (d > 0)
      this.topLatComboBox.setSelectedItem("N");
    else
      this.topLatComboBox.setSelectedItem("S");
  }

  public boolean isSizeFromGRIB()
  {
    return gribSizeCheckBox.isSelected();
  }
  
  public void setSizeFromGRIB(boolean b)
  {
    gribSizeCheckBox.setSelected(b);
    // Set the size check boxes
    gribSizeCheckBox_actionPerformed(null);
  }

  public boolean[] getGRIBOptions()
  {
    boolean[] options = new boolean[]
       {
         // GRIB Data
         (prmslDataCheckBox.isEnabled() && prmslDataCheckBox.isSelected()),
         (mb500DataCheckBox.isEnabled() && mb500DataCheckBox.isSelected()),
         (wavesDataCheckBox.isEnabled() && wavesDataCheckBox.isSelected()),
         (temperatureDataCheckBox.isEnabled() && temperatureDataCheckBox.isSelected()),
         (prateDataCheckBox.isEnabled() && prateDataCheckBox.isSelected()),
         // 3D Data
         (tws3DCheckBox.isEnabled() && tws3DCheckBox.isSelected()),
         (prmsl3DCheckBox.isEnabled() && prmsl3DCheckBox.isSelected()),
         (mb5003DCheckBox.isEnabled() && mb5003DCheckBox.isSelected()),
         (waves3DCheckBox.isEnabled() && waves3DCheckBox.isSelected()),
         (temperature3DCheckBox.isEnabled() && temperature3DCheckBox.isSelected()),
         (prate3DCheckBox.isEnabled() && prate3DCheckBox.isSelected()),
         // Contour
         (contourPRMSLCheckBox.isEnabled() && contourPRMSLCheckBox.isSelected()),
         (contourMB500CheckBox.isEnabled() && contourMB500CheckBox.isSelected()),
         (contourWavesCheckBox.isEnabled() && contourWavesCheckBox.isSelected()),
         (contourTemperatureCheckBox.isEnabled() && contourTemperatureCheckBox.isSelected()),
         (contourPrateCheckBox.isEnabled() && contourPrateCheckBox.isSelected()),
         (contourTWSCheckBox.isEnabled() && contourTWSCheckBox.isSelected())
       };
    return options;
  }

  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  private boolean justWind()
  {
    return justWindCheckBox.isSelected();  
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  private void justWind(boolean b)
  {
//  System.out.println("Setting justWind to " + b);
    justWindCheckBox.setSelected(b);
    justWindCheckBox_actionPerformed(null); // to set the other ones. Fakes user's action
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean getTWS()
  {
    return tws3DCheckBox.isSelected();
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
  @Deprecated
  public boolean getPRMSL()
  {
    return prmsl3DCheckBox.isSelected();
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean get500mb()
  {
    return mb5003DCheckBox.isSelected();
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean getWaves()
  {
    return waves3DCheckBox.isSelected();
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean getTemperature()
  {
    return temperature3DCheckBox.isSelected();
  }
  
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean getRain()
  {
    return prate3DCheckBox.isSelected();
  }
  
  public void setPRMSL(boolean b)
  {
    prmslDataCheckBox.setSelected(b);
    prmsl3DCheckBox.setEnabled(b);
    contourPRMSLCheckBox.setEnabled(b);
    
  }
  public void set500mb(boolean b)
  {
    mb500DataCheckBox.setSelected(b);
    mb5003DCheckBox.setEnabled(b);
    contourMB500CheckBox.setEnabled(true);
  }
  public void setWaves(boolean b)
  {
    wavesDataCheckBox.setSelected(b);
    waves3DCheckBox.setEnabled(b);
    contourWavesCheckBox.setEnabled(b);
  }
  public void setTemp(boolean b)
  {
    temperatureDataCheckBox.setSelected(b);
    temperature3DCheckBox.setEnabled(b);
    contourTemperatureCheckBox.setEnabled(b);
  }
  public void setPrate(boolean b)
  {
    prateDataCheckBox.setSelected(b);
    prate3DCheckBox.setEnabled(b);
    contourPrateCheckBox.setEnabled(b);
  }
      
  public void set3DTWS(boolean b)
  {
    tws3DCheckBox.setSelected(b);
  }
  
  public void set3DPRMSL(boolean b)
  {
    prmsl3DCheckBox.setSelected(b);
  }
  
  public void set3D500hgt(boolean b)
  {
    mb5003DCheckBox.setSelected(b);
  }
  
  public void set3DWaves(boolean b)
  {
    waves3DCheckBox.setSelected(b);
  }
  
  public void set3DTemp(boolean b)
  {
    temperature3DCheckBox.setSelected(b); 
  }
  
  public void set3DRain(boolean b)
  {
    prate3DCheckBox.setSelected(b);
  }
  
  public void setTWSContour(boolean b)
  {
    contourTWSCheckBox.setSelected(b);
  }
  public void setPRMSLContour(boolean b)
  {
    contourPRMSLCheckBox.setSelected(b);
  }
  public void set500mbContour(boolean b)
  {
    contourMB500CheckBox.setSelected(b);
  }
  public void setWavesContour(boolean b)
  {
    contourWavesCheckBox.setSelected(b);
  }
  public void setTempContour(boolean b)
  {
    contourTemperatureCheckBox.setSelected(b);
  }
  public void setPrateContour(boolean b)
  {
    contourPrateCheckBox.setSelected(b);
  }
  /**
   * @deprecated Use getGRIBOptions instead
   */
   @Deprecated
  public boolean withContourLines()
  {
    return (contourPRMSLCheckBox.isEnabled() && contourPRMSLCheckBox.isSelected());
  }
  
  public void setWithContourLines(boolean b)
  {
    contourPRMSLCheckBox.setSelected(b);
  }
  
  public void setBottomLat(double d)
  {
    this.bottomLatTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
    if (d > 0)
      this.bottomLatComboBox.setSelectedItem("N");
    else
      this.bottomLatComboBox.setSelectedItem("S");
  }

  public void setLeftLong(double d)
  {
    this.leftLongTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
    if (d > 0)
      this.leftLongComboBox.setSelectedItem("E");
    else
      this.leftLongComboBox.setSelectedItem("W");
  }

  public void setRightLong(double d)
  {
    this.rightLongTextField.setText(WWGnlUtilities.XX14.format(Math.abs(d)));
    if (d > 0)
      this.rightLongComboBox.setSelectedItem("E");
    else
      this.rightLongComboBox.setSelectedItem("W");
  }

  public double getTopLat()
  {
    double d = 0D;
    try
    {
//    d = Double.parseDouble(topLatTextField.getText());
      d = WWContext.NF.parse(topLatTextField.getText()).doubleValue();
      if (topLatComboBox.getSelectedItem().equals("S"))
        d = -d;
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
    return d;
  }

  public double getBottomLat()
  {
    double d = 0D;
    try
    {
      d = WWContext.NF.parse(bottomLatTextField.getText()).doubleValue();
      if (bottomLatComboBox.getSelectedItem().equals("S"))
        d = -d;
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
    return d;
  }

  public double getLeftLong()
  {
    double d = 0D;
    try
    {
      d = WWContext.NF.parse(leftLongTextField.getText()).doubleValue();
      if (leftLongComboBox.getSelectedItem().equals("W"))
        d = -d;
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
    return d;
  }

  public double getRightLong()
  {
    double d = 0D;
    try
    {
      d = WWContext.NF.parse(rightLongTextField.getText()).doubleValue();
      if (rightLongComboBox.getSelectedItem().equals("W"))
        d = -d;
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
    return d;
  }

  public void setComment(String str)
  {
    commentTextArea.setText(str);
  }
  
  public String getComment()
  { return commentTextArea.getText(); }
  
  public boolean isGRIBRequestSelected()
  {
    return gribCheckBox.isSelected() && gribCheckBox.isEnabled();
  }
  
  public void setGRIBRequestSelected(boolean b)
  {
    gribCheckBox.setSelected(b);
  }
  
  private void gribButton_actionPerformed(ActionEvent e)
  {
    if (gribCheckBox.isSelected())
    {
      JTreeGRIBRequestPanel jtgrp = new JTreeGRIBRequestPanel();
      int resp = JOptionPane.showConfirmDialog(this, 
                                               jtgrp, WWGnlUtilities.buildMessage("grib-request-2"), 
                                               JOptionPane.OK_CANCEL_OPTION, 
                                               JOptionPane.QUESTION_MESSAGE);
      if (resp == JOptionPane.OK_OPTION)
      {
        String grib = jtgrp.getCurrentlySelectedRequest();
        if (grib != null && grib.trim().length() > 0)
          gribFileTextField.setText(grib);
      }
    }
    else
    {
      String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
      String grib = WWGnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, new String[]
                                             { "grb", "grib" }, 
                                             "GRIB Files", 
                                             firstDir,
                                             "Open", 
                                             "Open GRIB File");
      if (grib != null && grib.trim().length() > 0)
        gribFileTextField.setText(grib);
    }
  }

  private void gribSizeCheckBox_actionPerformed(ActionEvent e)
  {
    boolean b = gribSizeCheckBox.isSelected();
    topLatTextField.setEnabled(!b);
    bottomLatTextField.setEnabled(!b);
    leftLongTextField.setEnabled(!b);
    rightLongTextField.setEnabled(!b);
    topLatComboBox.setEnabled(!b);
    bottomLatComboBox.setEnabled(!b);
    leftLongComboBox.setEnabled(!b);
    rightLongComboBox.setEnabled(!b);
  }

  private void justWindCheckBox_actionPerformed(ActionEvent e)
  {
    tws3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && threeDCheckBox.isSelected());
    prmslDataCheckBox.setEnabled(!justWindCheckBox.isSelected());
    mb500DataCheckBox.setEnabled(!justWindCheckBox.isSelected());
    wavesDataCheckBox.setEnabled(!justWindCheckBox.isSelected());
    temperatureDataCheckBox.setEnabled(!justWindCheckBox.isSelected());
    prateDataCheckBox.setEnabled(!justWindCheckBox.isSelected());
    
    prmsl3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && prmslDataCheckBox.isSelected());
    mb5003DCheckBox.setEnabled(!justWindCheckBox.isSelected() && mb500DataCheckBox.isSelected());
    waves3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && wavesDataCheckBox.isSelected());
    temperature3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && temperatureDataCheckBox.isSelected());
    prate3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && prateDataCheckBox.isSelected());
    
    contourTWSCheckBox.setEnabled(!justWindCheckBox.isSelected());
    contourPRMSLCheckBox.setEnabled(!justWindCheckBox.isSelected() && prmslDataCheckBox.isSelected());
    contourMB500CheckBox.setEnabled(!justWindCheckBox.isSelected() && mb500DataCheckBox.isSelected());
    contourWavesCheckBox.setEnabled(!justWindCheckBox.isSelected() && wavesDataCheckBox.isSelected());
    contourTemperatureCheckBox.setEnabled(!justWindCheckBox.isSelected() && temperatureDataCheckBox.isSelected());
    contourPrateCheckBox.setEnabled(!justWindCheckBox.isSelected() && prateDataCheckBox.isSelected());
  }

  private void contourCheckBox_actionPerformed(ActionEvent e)
  {
    contourTWSCheckBox.setEnabled(!justWindCheckBox.isSelected() && contourCheckBox.isSelected());
    contourPRMSLCheckBox.setEnabled(!justWindCheckBox.isSelected() && prmslDataCheckBox.isSelected() && contourCheckBox.isSelected());
    contourMB500CheckBox.setEnabled(!justWindCheckBox.isSelected() && mb500DataCheckBox.isSelected() && contourCheckBox.isSelected());
    contourWavesCheckBox.setEnabled(!justWindCheckBox.isSelected() && wavesDataCheckBox.isSelected() && contourCheckBox.isSelected());
    contourTemperatureCheckBox.setEnabled(!justWindCheckBox.isSelected() && temperatureDataCheckBox.isSelected() && contourCheckBox.isSelected());
    contourPrateCheckBox.setEnabled(!justWindCheckBox.isSelected() && prateDataCheckBox.isSelected() && contourCheckBox.isSelected());
  }
  
  private void threeDCheckBox_actionPerformed(ActionEvent e)
  {
    tws3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && threeDCheckBox.isSelected());
    prmsl3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && prmslDataCheckBox.isSelected() && threeDCheckBox.isSelected());
    mb5003DCheckBox.setEnabled(!justWindCheckBox.isSelected() && mb500DataCheckBox.isSelected() && threeDCheckBox.isSelected());
    waves3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && wavesDataCheckBox.isSelected() && threeDCheckBox.isSelected());
    temperature3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && temperatureDataCheckBox.isSelected() && threeDCheckBox.isSelected());
    prate3DCheckBox.setEnabled(!justWindCheckBox.isSelected() && prateDataCheckBox.isSelected() && threeDCheckBox.isSelected());
  }
  
  private void gribCheckBox_actionPerformed(ActionEvent e)
  {
    boolean grib = gribCheckBox.isSelected();
//  gribButton.setEnabled(!grib);
    if (grib)
      gribFileTextField.setToolTipText(WWGnlUtilities.buildMessage("grib-file-tooltip-grib"));
    else
      gribFileTextField.setToolTipText(WWGnlUtilities.buildMessage("grib-file-tooltip-file"));
  }

  private void gpxCheckBox_actionPerformed(ActionEvent e)
  {
    gpxLabel.setEnabled(gpxCheckBox.isSelected());
    gpxFileNameTextField.setEnabled(gpxCheckBox.isSelected());
    gpxBrowseButton.setEnabled(gpxCheckBox.isSelected());
    upToCheckBox.setEnabled(gpxCheckBox.isSelected());
    dtPanel.setEnabled(gpxCheckBox.isSelected() && upToCheckBox.isSelected());
    fromGPXLabel.setEnabled(gpxCheckBox.isSelected());
  }

  private void gpxBrowseButton_actionPerformed(ActionEvent e)
  {
    String gpxFileName = WWGnlUtilities.chooseFile(JFileChooser.FILES_ONLY, new String[] { "gpx" }, "GPX Data Files", ".");
    if (gpxFileName != null && gpxFileName.trim().length() > 0)
    {
      gpxFileNameTextField.setText(gpxFileName);
      try
      {
        URL gpxURL = new File(gpxFileName).toURI().toURL();
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("127"));
        Date date = GPXUtil.getLastDate(gpxURL);
        if (date != null)
        {
          dtPanel.setDate(date);
          date = GPXUtil.getFirstDate(gpxURL);
          fromGPXLabel.setText("From " + date.toString()); //  + " to " + GPXUtil.getLastDate(gpxURL).toString());
        }
        else
        {
          upToCheckBox.setSelected(false);
          upToCheckBox.setEnabled(false);
          dtPanel.setEnabled(false);
          fromGPXLabel.setEnabled(false);
        }
        TimeZone.setDefault(tz); // Reset      
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  public boolean thereIsGPXData()
  {
    return (gpxFileNameTextField.getText().trim().length() > 0 && gpxCheckBox.isSelected());
  }
  
  public String getGPXFileName()
  {
    return (gpxFileNameTextField.getText().trim());
  }

  private void upToCheckBox_actionPerformed(ActionEvent e)
  {
    dtPanel.setEnabled(gpxCheckBox.isSelected() && upToCheckBox.isSelected());
  }
  
  public Date getUpToDate()
  {
    Date d = null;
    if (gpxCheckBox.isSelected() && upToCheckBox.isSelected())
      d = dtPanel.getDate();
    return d;
  }
}
