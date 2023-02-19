package chartview.gui.util.tree;


import calc.GeoPoint;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.util.WWGnlUtilities;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.StringTokenizer;


public class JTreeGRIBRequestPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTree jTree = new JTree();
    private final transient TreeSelectionListener treeMonitor = new JTreeGRIBRequestPanel.TreeMonitor(this);

    private final JPanel treeHolder = new JPanel();
    private final JPanel editPanel = new JPanel();

    private final JTabbedPane tabbedPane = new JTabbedPane();

    private final JTextField requestField = new JTextField();
    private final JButton saveButton = new JButton();
    private final JPanel bottomPanel = new JPanel();

    private String currentlySelectedRequest = "";
    private DefaultMutableTreeNode root = null;

    private final GribRequestPopup menuPopup = new GribRequestPopup(this);

    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel titleLabel = new JLabel();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();
    private final JLabel jLabel3 = new JLabel();
    private final JLabel jLabel4 = new JLabel();
    private final JLabel jLabel5 = new JLabel();
    private final JLabel jLabel6 = new JLabel();
    private final JLabel jLabel7 = new JLabel();
    private final JLabel jLabel8 = new JLabel();
    private final JSlider periodSlider = new JSlider();
    private final JTextField bottomTextField = new JTextField();
    private final JTextField topTextField = new JTextField();
    private final JTextField leftTextField = new JTextField();
    private final JTextField rightTextField = new JTextField();
    private final JPanel jPanel1 = new JPanel();
    private final JPanel jPanel2 = new JPanel();
    private final JPanel jPanel3 = new JPanel();
    private final JCheckBox prmslCheckBox = new JCheckBox();
    private final JCheckBox windCheckBox = new JCheckBox();
    private final JCheckBox hgt500CheckBox = new JCheckBox();
    private final JCheckBox tempCheckBox = new JCheckBox();
    private final JCheckBox wavesCheckBox = new JCheckBox();
    private final JRadioButton threeRadioButton = new JRadioButton();
    private final JRadioButton sixRadioButton = new JRadioButton();
    private final JRadioButton twelveRadioButton = new JRadioButton();
    private final JRadioButton twentyfourRadioButton = new JRadioButton();
    private final JRadioButton oneRadioButton = new JRadioButton();
    private final JRadioButton twoRadioButton = new JRadioButton();
    private final JRadioButton fourRadioButton = new JRadioButton();

    private XMLDocument gribDocument = null;
    private final JCheckBox rainCheckBox = new JCheckBox();

    private boolean displayOnlyGRIBEditor = false;

    public JTreeGRIBRequestPanel() {
        this(false);
    }

    public JTreeGRIBRequestPanel(boolean gribEditorOnly) {
        displayOnlyGRIBEditor = gribEditorOnly;
        root = new DefaultMutableTreeNode("Invisible", true);
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        treeHolder.setLayout(new BorderLayout());
        editPanel.setLayout(gridBagLayout1);

        editPanel.setSize(new Dimension(462, 251));
        editPanel.setPreferredSize(new Dimension(420, 250));
        editPanel.setMinimumSize(new Dimension(420, 250));
        requestField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                requestField_focusGained(e);
            }

            public void focusLost(FocusEvent e) {
                requestField_focusLost(e);
            }
        });
        requestField.addInputMethodListener(new InputMethodListener() {
            public void inputMethodTextChanged(InputMethodEvent e) {
                requestField_inputMethodTextChanged(e);
            }

            public void caretPositionChanged(InputMethodEvent e) {
                requestField_caretPositionChanged(e);
            }
        });

        titleLabel.setText(WWGnlUtilities.buildMessage("grib-request-editor"));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        jLabel1.setText(WWGnlUtilities.buildMessage("top-grib"));
        jLabel2.setText(WWGnlUtilities.buildMessage("bottom-grib"));
        jLabel3.setText(WWGnlUtilities.buildMessage("left-grib"));
        jLabel4.setText(WWGnlUtilities.buildMessage("right-grib"));
        jLabel5.setText(WWGnlUtilities.buildMessage("period-grib"));
        jLabel6.setText(WWGnlUtilities.buildMessage("data-grib"));
        jLabel7.setText(WWGnlUtilities.buildMessage("time-step-grib"));
        jLabel8.setText(WWGnlUtilities.buildMessage("cell-size-grib"));
        periodSlider.setMajorTickSpacing(24);
        periodSlider.setMinorTickSpacing(6);
        periodSlider.setMaximum(336);
        periodSlider.setPaintLabels(true);
        periodSlider.setPaintTicks(true);
        periodSlider.setValue(72);
        periodSlider.setToolTipText(WWGnlUtilities.XXX12.format(((double) periodSlider.getValue()) / 24D) + " day(s)");
//  periodSlider.setValueIsAdjusting(true);
        periodSlider.setSnapToTicks(true);
        periodSlider.setPreferredSize(new Dimension(300, 41));
        bottomTextField.setPreferredSize(new Dimension(50, 20));
        bottomTextField.setHorizontalAlignment(JTextField.RIGHT);
        topTextField.setPreferredSize(new Dimension(50, 20));
        topTextField.setHorizontalAlignment(JTextField.RIGHT);
        leftTextField.setPreferredSize(new Dimension(50, 20));
        leftTextField.setHorizontalAlignment(JTextField.RIGHT);
        rightTextField.setPreferredSize(new Dimension(50, 20));
        rightTextField.setHorizontalAlignment(JTextField.RIGHT);
        prmslCheckBox.setText("PRMSL");
        prmslCheckBox.setSelected(true);
        prmslCheckBox.addActionListener(this::prmslCheckBox_actionPerformed);
        windCheckBox.setText("WIND");
        windCheckBox.setSelected(true);
        windCheckBox.addActionListener(this::windCheckBox_actionPerformed);
        hgt500CheckBox.setText("HGT500");
        hgt500CheckBox.setSelected(true);
        hgt500CheckBox.addActionListener(this::hgt500CheckBox_actionPerformed);
        tempCheckBox.setText("AIRTMP");
        tempCheckBox.addActionListener(this::tempCheckBox_actionPerformed);
        wavesCheckBox.setText("WAVES");
        wavesCheckBox.addActionListener(this::wavesCheckBox_actionPerformed);
        rainCheckBox.setText("RAIN");
        rainCheckBox.addActionListener(this::rainCheckBox_actionPerformed);

        threeRadioButton.setText(WWGnlUtilities.buildMessage("3-hours"));
        threeRadioButton.setSelected(false);
        threeRadioButton.addActionListener(this::threeRadioButton_actionPerformed);
        sixRadioButton.setText(WWGnlUtilities.buildMessage("6-hours"));
        sixRadioButton.setSelected(true);
        sixRadioButton.addActionListener(this::sixRadioButton_actionPerformed);
        twelveRadioButton.setText(WWGnlUtilities.buildMessage("12-hours"));
        twelveRadioButton.addActionListener(this::twelveRadioButton_actionPerformed);
        twentyfourRadioButton.setText(WWGnlUtilities.buildMessage("24-hours"));
        twentyfourRadioButton.addActionListener(this::twentyfourRadioButton_actionPerformed);
        oneRadioButton.setText("1\272 x 1\272");
        oneRadioButton.addActionListener(this::oneRadioButton_actionPerformed);
        twoRadioButton.setText("2\272 x 2\272");
        twoRadioButton.setSelected(true);
        twoRadioButton.addActionListener(this::twoRadioButton_actionPerformed);
        fourRadioButton.setText("4\272 x 4\272");

        fourRadioButton.addActionListener(this::fourRadioButton_actionPerformed);
        ButtonGroup timeGroup = new ButtonGroup();
        ButtonGroup sizeGroup = new ButtonGroup();

        timeGroup.add(threeRadioButton);
        timeGroup.add(sixRadioButton);
        timeGroup.add(twelveRadioButton);
        timeGroup.add(twentyfourRadioButton);

        sizeGroup.add(oneRadioButton);
        sizeGroup.add(twoRadioButton);
        sizeGroup.add(fourRadioButton);

        this.setPreferredSize(new Dimension(535, 300));
        this.setMinimumSize(new Dimension(535, 300));
        this.setSize(new Dimension(535, 300));
        jScrollPane1.getViewport().add(jTree, null);
        jTree.addTreeSelectionListener(treeMonitor);
        jTree.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                tryPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dblClicked(e);
                } else {
                    tryPopup(e);
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            private void dblClicked(MouseEvent e) {
                if (e.isConsumed()) {
                    return;
                }
                // Let's make sure we only invoke double click action when
                // we have a treepath. For example; This avoids opening an editor on a
                // selected node when the user double clicks on the expand/collapse icon.
                if (e.getClickCount() == 2) {
                    if (jTree.getPathForLocation(e.getX(), e.getY()) != null) {
                        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                        // Take action here
                    }
                } else if (e.getClickCount() > 2) {
                    // Fix triple-click wanna-be drag events...
                    e.consume();
                }
            }

            private void tryPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath current = jTree.getPathForLocation(e.getX(), e.getY());
                    if (current == null) {
                        return;
                    }
                    TreePath[] paths = jTree.getSelectionPaths();
                    boolean isSelected = false;
                    if (paths != null) {
                      for (TreePath path : paths) {
                        if (path == current) {
                          isSelected = true;
                          break;
                        }
                      }
                    }
                    if (!isSelected) {
                        jTree.setSelectionPath(current);
                    }
                    DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                    // Show popup menu...
                    menuPopup.setTreeNode(dtn);
                    menuPopup.show(jTree, e.getX(), e.getY());
                }
            }
        });
        jTree.setModel(new DefaultTreeModel(root));
        jTree.setRootVisible(false);
        jTree.setCellRenderer(new JTreeGRIBRequestPanel.GribTreeCellRenderer());

        ToolTipManager.sharedInstance().registerComponent(jTree);
        fillUpTree();
        treeHolder.add(jScrollPane1, BorderLayout.CENTER);

        if (displayOnlyGRIBEditor) {
          this.add(editPanel, BorderLayout.CENTER);
        } else {
            this.add(tabbedPane, BorderLayout.CENTER);
            tabbedPane.add(WWGnlUtilities.buildMessage("grib-request"), treeHolder);
            tabbedPane.add(WWGnlUtilities.buildMessage("grib-request-editor"), editPanel);
            tabbedPane.setEnabledAt(1, false); // Edit not available by default.

            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(requestField, BorderLayout.CENTER);
            saveButton.setText(WWGnlUtilities.buildMessage("save"));
            saveButton.setToolTipText(WWGnlUtilities.buildMessage("save-request"));
            saveButton.addActionListener(e -> saveCurrentRequest());
            bottomPanel.add(saveButton, BorderLayout.EAST);

            this.add(bottomPanel, BorderLayout.SOUTH);
        }
        editPanel.add(titleLabel,
                new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 5, 0), 0, 0));
        editPanel.add(jLabel1,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 5), 0, 0));
        editPanel.add(jLabel2,
                new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        editPanel.add(jLabel3,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 5), 0, 0));
        editPanel.add(jLabel4,
                new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 5), 0, 0));
        editPanel.add(jLabel5,
                new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(jLabel6,
                new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(jLabel7,
                new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(jLabel8,
                new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(periodSlider,
                new GridBagConstraints(0, 4, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(5, 0, 0, 0), 0, 0));
        editPanel.add(bottomTextField,
                new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(topTextField,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(leftTextField,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        editPanel.add(rightTextField,
                new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        jPanel1.add(prmslCheckBox, null);
        jPanel1.add(windCheckBox, null);
        jPanel1.add(hgt500CheckBox, null);
        jPanel1.add(tempCheckBox, null);
        jPanel1.add(wavesCheckBox, null);
        jPanel1.add(rainCheckBox, null);
        editPanel.add(jPanel1,
                new GridBagConstraints(1, 5, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        jPanel2.add(threeRadioButton, null);
        jPanel2.add(sixRadioButton, null);
        jPanel2.add(twelveRadioButton, null);
        jPanel2.add(twentyfourRadioButton, null);
        editPanel.add(jPanel2,
                new GridBagConstraints(1, 6, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        jPanel3.add(oneRadioButton, null);
        jPanel3.add(twoRadioButton, null);
        jPanel3.add(fourRadioButton, null);
        editPanel.add(jPanel3,
                new GridBagConstraints(1, 7, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
                                         
/*  requestField.getDocument().addDocumentListener(new DocumentListener()
      {
          public void insertUpdate(DocumentEvent e)
          {
            currentlySelectedRequest = requestField.getText();
            try { parseGRIBRequest(currentlySelectedRequest); }
            catch (Exception ex) { System.out.println(ex.getMessage()); }
          }

          public void removeUpdate(DocumentEvent e)
          {
            currentlySelectedRequest = requestField.getText();
            try { parseGRIBRequest(currentlySelectedRequest); }
            catch (Exception ex) { System.out.println(ex.getMessage()); }
          }

          public void changedUpdate(DocumentEvent e)
          {
            currentlySelectedRequest = requestField.getText();
            try { parseGRIBRequest(currentlySelectedRequest); }
            catch (Exception ex) { System.out.println(ex.getMessage()); }
          }
      });
      */

        topTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                composeGRIBRequest();
            }
        });
        bottomTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                composeGRIBRequest();
            }
        });
        leftTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                composeGRIBRequest();
            }
        });
        rightTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                composeGRIBRequest();
            }
        });
/**
 topTextField.getDocument().addDocumentListener(new DocumentListener()
 {
 public void insertUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void removeUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void changedUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }
 });
 bottomTextField.getDocument().addDocumentListener(new DocumentListener()
 {
 public void insertUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void removeUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void changedUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }
 });
 leftTextField.getDocument().addDocumentListener(new DocumentListener()
 {
 public void insertUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void removeUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void changedUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }
 });
 rightTextField.getDocument().addDocumentListener(new DocumentListener()
 {
 public void insertUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void removeUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }

 public void changedUpdate(DocumentEvent e)
 {
 composeGRIBRequest();
 }
 });
 ***/
        periodSlider.addChangeListener(evt -> {
            JSlider slider = (JSlider) evt.getSource();

            if (!slider.getValueIsAdjusting()) {
                // Get new value
//        float value = (float)slider.getValue() / 100F;
                periodSlider.setToolTipText(WWGnlUtilities.XXX12.format(((double) periodSlider.getValue()) / 24D) + " day(s)");
                composeGRIBRequest();
            }
        });
    }

    protected XMLDocument getXMLDocument() {
        return gribDocument;
    }

    private void saveCurrentRequest() {
        if (requestField.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, WWGnlUtilities.buildMessage("no-request"), WWGnlUtilities.buildMessage("save-grib-request"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Provide: a name, a group
        CreateGRIBRequestPanel cgrp = new CreateGRIBRequestPanel(requestField.getText(), gribDocument);
        int resp = JOptionPane.showConfirmDialog(this, cgrp, WWGnlUtilities.buildMessage("save-grib-request"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
//      System.out.println("Saving " + requestField.getText() +
//                         " in " + cgrp.getGroup() + 
//                         " as " + cgrp.getName());
            // Check if group & name are ok
            if (cgrp.getGroup().trim().length() > 0 && cgrp.getName().trim().length() > 0) {
                String group = cgrp.getGroup().trim();
                String name = cgrp.getName().trim();
                String request = requestField.getText();
                createGRIBRequest(name, request, group);
            } else
                JOptionPane.showMessageDialog(this, WWGnlUtilities.buildMessage("request-group-and-name"), WWGnlUtilities.buildMessage("save-grib-request"),
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void createGRIBRequest(String name, String request, String group) {
        try {
            XMLElement requestGroup = null;
            // Does group exist?
            NodeList nl = gribDocument.selectNodes("/grib-collection/grib-set[./@id = '" + WWGnlUtilities.escapeXML(group) + "']");
            if (nl.getLength() > 0) {
              requestGroup = (XMLElement) nl.item(0);
            } else {
                requestGroup = (XMLElement) gribDocument.createElement("grib-set");
                requestGroup.setAttribute("id", WWGnlUtilities.escapeXML(group));
                gribDocument.getDocumentElement().appendChild(requestGroup);
            }
            XMLElement newRequest = (XMLElement) gribDocument.createElement("grib");
            newRequest.setAttribute("name", WWGnlUtilities.escapeXML(name));
            newRequest.setAttribute("request", request);
            requestGroup.appendChild(newRequest);
        } catch (Exception xmlEx) {
            xmlEx.printStackTrace();
        }
        // Save document
        refreshTreeAndDocument();
    }

    protected void refreshTreeAndDocument(XMLDocument doc) {
        gribDocument = doc;
        refreshTreeAndDocument();
    }

    protected void refreshTreeAndDocument() {
        try {
            gribDocument.print(new FileOutputStream(new File("config" + File.separator + "grib.xml")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Refresh tree
        root.removeAllChildren();
        fillUpTree();
    }

    public static void main(String... args) {
        System.out.println("Escape:[" + WWGnlUtilities.escapeXML("<That's for \"test\">") + "]");
        System.out.println("Unescape:[" + WWGnlUtilities.unescapeXML("&lt;That&apos;s for &quot;test&quot;&gt;") + "]");
    }

    private void fillUpTree() {
        DOMParser parser = WWContext.getInstance().getParser();
        try {
            synchronized (parser) {
                String gribs = "config" + File.separator + "grib.xml";
                parser.setValidationMode(XMLParser.NONVALIDATING);
                parser.parse(new File(gribs).toURI().toURL());
                XMLDocument doc = parser.getDocument();
                gribDocument = doc;
                // Add the first "special" node
                ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewportBorderBounds();
                int topLeftX = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewport().getViewPosition().x;
                int topLeftY = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewport().getViewPosition().y;
                int viewWidth = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewport().getSize().width;
                int viewHeight = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewport().getSize().height;

                GeoPoint topLeft = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getGeoPos(topLeftX, topLeftY);
                GeoPoint bottomRight = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getGeoPos(topLeftX + viewWidth,
                        topLeftY + viewHeight);
                int top = (int) Math.round(topLeft.getL());
                int bottom = (int) Math.round(bottomRight.getL());
                int left = (int) Math.round(topLeft.getG());
                int right = (int) Math.round(bottomRight.getG());

                String currentZoneReq = "GFS:";
                currentZoneReq += (WWGnlUtilities.DF2.format(Math.abs(top)) + (top > 0 ? "N" : "S"));
                currentZoneReq += ",";
                currentZoneReq += (WWGnlUtilities.DF2.format(Math.abs(bottom)) + (bottom > 0 ? "N" : "S"));
                currentZoneReq += ",";
                currentZoneReq += (WWGnlUtilities.DF3.format(Math.abs(left)) + (left > 0 ? "E" : "W"));
                currentZoneReq += ",";
                currentZoneReq += (WWGnlUtilities.DF3.format(Math.abs(right)) + (right > 0 ? "E" : "W"));
                currentZoneReq += "|2,2|0,6..168|PRMSL,WIND,HGT500";
                GRIBRequestTreeNode firstNode = new GRIBRequestTreeNode(WWGnlUtilities.buildMessage("current-view"),
                        currentZoneReq,
                        WWGnlUtilities.buildMessage("current-view-tip"));
                root.add(firstNode);

                NodeList gribSetNodes = doc.selectNodes("/grib-collection/grib-set");
                for (int i = 0; i < gribSetNodes.getLength(); i++) {
                    XMLElement gribSet = (XMLElement) gribSetNodes.item(i);
                    String id = gribSet.getAttribute("id");
                    DefaultMutableTreeNode gsNode = new DefaultMutableTreeNode(WWGnlUtilities.unescapeXML(id), true);
                    root.add(gsNode);
                    NodeList req = gribSet.selectNodes("./grib");
                    for (int j = 0; j < req.getLength(); j++) {
                        XMLElement grib = (XMLElement) req.item(j);
                        String name = grib.getAttribute("name");
                        String request = grib.getAttribute("request");
                        GRIBRequestTreeNode grtn = new GRIBRequestTreeNode(WWGnlUtilities.unescapeXML(name), request, WWGnlUtilities.unescapeXML(name));
                        gsNode.add(grtn);
                    }
                }
                ((DefaultTreeModel) jTree.getModel()).reload(root);
                //    expandAll(jTree, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reloadTree() {
        root.removeAllChildren();
        fillUpTree();
    }

    public String getCurrentlySelectedRequest() {
        currentlySelectedRequest = requestField.getText();
        return currentlySelectedRequest;
    }

    public void parseGRIBRequest(String gr) {
        // Sample: GFS:60N,0N,120E,070W|2,2|0,6..168|PRMSL,WIND,HGT500,SEATMP,WAVES

        int columnIndex = gr.indexOf(":");
        if (columnIndex == -1) {
            throw new RuntimeException("Unparsable GRIB Request - Missing column ':'");
        }
        String prefix = gr.substring(0, columnIndex);
        if (!prefix.equalsIgnoreCase("GFS")) {
            throw new RuntimeException("Unparsable GRIB Request [" + gr + "]\nPrefix not supported " + prefix + ". Only GFS for now.");
        }
        String grib = gr.substring(columnIndex + 1);
        StringTokenizer strtokGRIB = new StringTokenizer(grib, "|");
        int tokenIndex = 0;
        while (strtokGRIB.hasMoreTokens()) {
            String tok = strtokGRIB.nextToken();
//    System.out.println("Token:" + tok);
            switch (tokenIndex) {
                case 0: // Geo Positions
                    StringTokenizer strtokGeo = new StringTokenizer(tok, ",");
                    int index = 0;
                    while (strtokGeo.hasMoreTokens()) {
                        String s = strtokGeo.nextToken();
                        switch (index) {
                            case 0: // Top
                                topTextField.setText(s);
                                break;
                            case 1: // Bottom
                                bottomTextField.setText(s);
                                break;
                            case 2: // Left
                                leftTextField.setText(s);
                                break;
                            case 3: // Right
                                rightTextField.setText(s);
                                break;
                            default:
                                break;
                        }
                        index++;
                    }
                    break;
                case 1: // Cell Size
                    if (tok.equals("1,1")) {
                      oneRadioButton.setSelected(true);
                    } else if (tok.equals("2,2")) {
                      twoRadioButton.setSelected(true);
                    } else {
                      fourRadioButton.setSelected(true);
                    }
                    break;
                case 2: // Period
                    if (tok.equals("0")) {
                        periodSlider.setValue(0);
                        // TODO Disable the radio buttons for the step
                    } else {
                        int comma = tok.indexOf(",");
                        int dotdot = tok.indexOf("..");
                        if (comma == -1 || dotdot == -1) {
                            throw new RuntimeException("Unparsable GRIB Request [" + gr + "]\nInvalid period " + tok);
                        }
                        String stepStr = tok.substring(comma + 1, dotdot);
                        String lenStr = tok.substring(dotdot + "..".length());
                        int step = 24;
                        try {
                            step = Integer.parseInt(stepStr);
                        } catch (Exception ignore) {
                        }
                        switch (step) {
                            case 3:
                                threeRadioButton.setSelected(true);
                                break;
                            case 6:
                                sixRadioButton.setSelected(true);
                                break;
                            case 12:
                                twelveRadioButton.setSelected(true);
                                break;
                            case 24:
                                twentyfourRadioButton.setSelected(true);
                                break;
                            default:
                                break;
                        }
                        int len = 72;
                        try {
                            len = Integer.parseInt(lenStr);
                        } catch (Exception ignore) {
                        }
                        periodSlider.setValue(len);
                    }
                    break;
                case 3: // Data
                    StringTokenizer strtokData = new StringTokenizer(tok, ",");
                    prmslCheckBox.setSelected(false);
                    windCheckBox.setSelected(false);
                    hgt500CheckBox.setSelected(false);
                    tempCheckBox.setSelected(false);
                    wavesCheckBox.setSelected(false);
                    while (strtokData.hasMoreTokens()) {
                        String s = strtokData.nextToken();
//          System.out.println("Data Token:" + s);
                      switch (s) {
                        case "PRMSL":
                          prmslCheckBox.setSelected(true);
                          break;
                        case "WIND":
                          windCheckBox.setSelected(true);
                          break;
                        case "HGT500":
                        case "HGT":
                          hgt500CheckBox.setSelected(true);
                          break;
                        case "TEMP":
                        case "SEATMP":
                        case "AIRTEMP":
                          tempCheckBox.setSelected(true);
                          break;
                        case "WAVES":
                          wavesCheckBox.setSelected(true);
                          break;
                      }
                    }
                    break;
                default:
                    break;
            }
            tokenIndex++;
        }
    }

    private synchronized void composeGRIBRequest() {
        String gribRequest = "GFS:";
        gribRequest += (topTextField.getText() + ",");
        gribRequest += (bottomTextField.getText() + ",");
        gribRequest += (leftTextField.getText() + ",");
        gribRequest += (rightTextField.getText() + "|");
        if (oneRadioButton.isSelected()) {
          gribRequest += ("1,1|");
        } else if (twoRadioButton.isSelected()) {
          gribRequest += ("2,2|");
        } else if (fourRadioButton.isSelected()) {
          gribRequest += ("4,4|");
        }
        gribRequest += "0";
        if (periodSlider.getValue() > 0) {
            gribRequest += ",";
            if (threeRadioButton.isSelected()) {
              gribRequest += "3..";
            } else if (sixRadioButton.isSelected()) {
              gribRequest += "6..";
            } else if (twelveRadioButton.isSelected()) {
              gribRequest += "12..";
            } else if (twentyfourRadioButton.isSelected()) {
              gribRequest += "24..";
            }
            gribRequest += Integer.toString(periodSlider.getValue());
        }

        gribRequest += "|";
        int nbData = 0;
        if (prmslCheckBox.isSelected()) {
            gribRequest += "PRMSL";
            nbData++;
        }
        if (windCheckBox.isSelected()) {
            gribRequest += ((nbData > 0 ? "," : "") + "WIND");
            nbData++;
        }
        if (hgt500CheckBox.isSelected()) {
            gribRequest += ((nbData > 0 ? "," : "") + "HGT500");
            nbData++;
        }
        if (tempCheckBox.isSelected()) {
            gribRequest += ((nbData > 0 ? "," : "") + "AIRTMP");
            nbData++;
        }
        if (wavesCheckBox.isSelected()) {
            gribRequest += ((nbData > 0 ? "," : "") + "WAVES");
            nbData++;
        }
        if (rainCheckBox.isSelected()) {
            gribRequest += ((nbData > 0 ? "," : "") + "RAIN");
            nbData++;
        }
        currentlySelectedRequest = gribRequest;
        requestField.setText(currentlySelectedRequest);
    }

    private void prmslCheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void windCheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void hgt500CheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void tempCheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void wavesCheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void rainCheckBox_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void threeRadioButton_actionPerformed(ActionEvent e) {
        periodSlider.setMinorTickSpacing(3);
        composeGRIBRequest();
    }

    private void sixRadioButton_actionPerformed(ActionEvent e) {
        periodSlider.setMinorTickSpacing(6);
        composeGRIBRequest();
    }

    private void twelveRadioButton_actionPerformed(ActionEvent e) {
        periodSlider.setMinorTickSpacing(12);
        composeGRIBRequest();
    }

    private void twentyfourRadioButton_actionPerformed(ActionEvent e) {
        periodSlider.setMinorTickSpacing(24);
        composeGRIBRequest();
    }

    private void oneRadioButton_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void twoRadioButton_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void fourRadioButton_actionPerformed(ActionEvent e) {
        composeGRIBRequest();
    }

    private void requestField_focusGained(FocusEvent e) {
//  System.out.println("Focus Gained");
    }

    private void requestField_focusLost(FocusEvent e) {
//  System.out.println("Focus lost");
        try {
            parseGRIBRequest(requestField.getText());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void requestField_inputMethodTextChanged(InputMethodEvent e) {
//  System.out.println("Text Changed");
    }

    private void requestField_caretPositionChanged(InputMethodEvent e) {
//  System.out.println("Caret Position Changed");
    }


    public class GRIBRequestTreeNode extends DefaultMutableTreeNode {
        String name;
        String request;
        String bubble;

        public GRIBRequestTreeNode(String name, String request, String bubble) {
            this.name = name;
            this.request = request;
            this.bubble = bubble;
        }

        public String toString() {
            return name;
        }

        public String getBubble() {
            return bubble;
        }
    }

    class TreeMonitor
            implements TreeSelectionListener {
        JTextField feedback = null;
        JTreeGRIBRequestPanel parent;

        public TreeMonitor(JTreeGRIBRequestPanel caller) {
            this(caller, null);
        }

        public TreeMonitor(JTreeGRIBRequestPanel caller, JTextField fld) {
            feedback = fld;
            parent = caller;
        }

        public void valueChanged(TreeSelectionEvent tse) {
            TreePath tp = tse.getNewLeadSelectionPath();
            if (tp == null) {
              return;
            }
            DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
//    currentlySelectedNode = dtn;
            if (dtn instanceof GRIBRequestTreeNode) {
                GRIBRequestTreeNode grtn = (GRIBRequestTreeNode) dtn;
                currentlySelectedRequest = grtn.request;
                requestField.setText(currentlySelectedRequest);
                if (currentlySelectedRequest.trim().length() > 0) {
                    tabbedPane.setEnabledAt(1, true);
                    try {
                        parseGRIBRequest(currentlySelectedRequest);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
//      StaticObjects.getInstance().fireFaxSelectedForPreview(((JTreeFilePanel.DataFileTreeNode) dtn).getFullFileName());
            }
        }
    }


    class GribTreeCellRenderer
            extends DefaultTreeCellRenderer {
        public GribTreeCellRenderer() {
            super();
        }

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof GRIBRequestTreeNode) {
                setIcon(new ImageIcon(this.getClass().getResource("note.png")));
                setToolTipText(((GRIBRequestTreeNode) value).getBubble());
            }
            return this;
        }
    }
}
