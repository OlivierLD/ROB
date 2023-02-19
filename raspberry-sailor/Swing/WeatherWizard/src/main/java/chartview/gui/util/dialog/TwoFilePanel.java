package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class TwoFilePanel
        extends JPanel {
    public final static String EVERY_THING = "EVERYTHING";
    public final static String JUST_FAXES = "JUST_FAXES";
    public final static String JUST_GRIBS = "JUST_GRIBS";

    private final TwoFilePanel instance = this;
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel leftLabel = new JLabel();
    private final JLabel rightLabel = new JLabel();
    private final JFileChooser leftChooser = new JFileChooser();
    private final JFileChooser rightChooser = new JFileChooser();
    private final JTextField regExprPatternTextField = new JTextField();
    private final JLabel patternLabel = new JLabel();
    private final JLabel regExpLabel = new JLabel();
    private final JPanel gribOptionPanel = new JPanel();

    private final ButtonGroup compositeGroup = new ButtonGroup();

    private final JRadioButton allRadioButton = new JRadioButton();
    private final JRadioButton justFaxesRadioButton = new JRadioButton();
    private final JRadioButton justGRIBRadioButton = new JRadioButton();
    private final JCheckBox pdfCheckBox = new JCheckBox();
    private final JLabel pdfTitleLabel = new JLabel("A title for the pdf:");  // LOCALIZE
    private final JTextField pdfTitle = new JTextField();
    private final JCheckBox boatAndTrackCheckBox = new JCheckBox();
    private final JCheckBox faxFilterCheckBox = new JCheckBox();
    private final JTextField faxNameFilterTextField = new JTextField();
    private final JCheckBox withCommentOnlyCheckBox = new JCheckBox();

    public TwoFilePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(1_002, 453));
        leftLabel.setText("Left Panel Label");
        leftLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        rightLabel.setText("Right Panel Label");
        rightLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        regExprPatternTextField.setText(".*");
        patternLabel.setText("Regular expression?");
        regExpLabel.setText("<html><u>" + WWGnlUtilities.buildMessage("help-reg-exp") + "</u></html>");
        regExpLabel.setForeground(Color.blue);
        regExpLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                regExpLabel.setForeground(WWGnlUtilities.PURPLE);
                regExpLabel.repaint();
                try {
                    Utilities.openInBrowser("http://www.regular-expressions.info/tutorial.html");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(instance, "Problem with RegExp help\n" + ex.toString(), "Regular Expressions", JOptionPane.WARNING_MESSAGE);
                }
            }

            public void mouseEntered(MouseEvent e) {
                regExpLabel.setForeground(WWGnlUtilities.PURPLE);
                regExpLabel.repaint();
            }

            public void mouseExited(MouseEvent e) {
                regExpLabel.setForeground(Color.blue);
                regExpLabel.repaint();
            }
        });

        compositeGroup.add(allRadioButton);
        compositeGroup.add(justFaxesRadioButton);
        compositeGroup.add(justGRIBRadioButton);

        allRadioButton.setText(WWGnlUtilities.buildMessage("everything"));
        allRadioButton.setSelected(true);
        allRadioButton.addActionListener(e -> faxNameFilterTextField.setEnabled(faxFilterCheckBox.isSelected() && !justGRIBRadioButton.isSelected()));
        justFaxesRadioButton.setText(WWGnlUtilities.buildMessage("just-faxes"));
        justFaxesRadioButton.addActionListener(e -> faxNameFilterTextField.setEnabled(faxFilterCheckBox.isSelected() && !justGRIBRadioButton.isSelected()));
        justGRIBRadioButton.setText(WWGnlUtilities.buildMessage("just-gribs"));
        justGRIBRadioButton.addActionListener(e -> faxNameFilterTextField.setEnabled(faxFilterCheckBox.isSelected() && !justGRIBRadioButton.isSelected()));
        pdfCheckBox.setText("Generate PDF when done");
        pdfCheckBox.setSelected(false);
        pdfTitle.setEnabled(false);
        pdfTitleLabel.setEnabled(false);
        pdfCheckBox.addActionListener(e -> {
            pdfTitle.setEnabled(pdfCheckBox.isSelected());
            pdfTitleLabel.setEnabled(pdfCheckBox.isSelected());
        });
        pdfTitle.setPreferredSize(new Dimension(200, 24));
        boatAndTrackCheckBox.setText("Boat & Track");
        faxFilterCheckBox.setText("Fax Filter");
        faxFilterCheckBox.setToolTipText("Filter Fax names ?");
        faxFilterCheckBox.addActionListener(e -> faxNameFilterTextField.setEnabled(faxFilterCheckBox.isSelected() && !justGRIBRadioButton.isSelected()));
        faxNameFilterTextField.setPreferredSize(new Dimension(150, 24));
        faxNameFilterTextField.setEnabled(false);
        faxNameFilterTextField.setToolTipText("Regular Expression for the Fax Names"); // LOCALIZE
        withCommentOnlyCheckBox.setText("With comments only");  // LOCALIZE
        withCommentOnlyCheckBox.setToolTipText("<html><b><i>Warning</i></b>:<br>This is a demanding operation, will take more time...</html>");
        this.add(leftLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(rightLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(leftChooser, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(rightChooser, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(regExprPatternTextField, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(patternLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(regExpLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        gribOptionPanel.add(allRadioButton, null);
        gribOptionPanel.add(justFaxesRadioButton, null);
        gribOptionPanel.add(justGRIBRadioButton, null);
        gribOptionPanel.add(faxFilterCheckBox, null);
        gribOptionPanel.add(faxNameFilterTextField, null);
        gribOptionPanel.add(boatAndTrackCheckBox, null);
        gribOptionPanel.add(pdfCheckBox, null);
        gribOptionPanel.add(pdfTitleLabel, null);
        gribOptionPanel.add(pdfTitle, null);
        this.add(gribOptionPanel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
        //Previous regexpr?
        this.add(withCommentOnlyCheckBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(WWGnlUtilities.REGEXPR_PROPERTIES_FILE));
            regExprPatternTextField.setText(p.getProperty(WWGnlUtilities.COMPOSITE_FILTER, ".*"));
            faxNameFilterTextField.setText(p.getProperty(WWGnlUtilities.FAX_NAME_FILTER, ""));
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public JLabel getLeftLabel() {
        return leftLabel;
    }

    public JLabel getRightLabel() {
        return rightLabel;
    }

    public JFileChooser getLeftChooser() {
        return leftChooser;
    }

    public JFileChooser getRightChooser() {
        return rightChooser;
    }

    public JTextField getRegExprPatternTextField() {
        return regExprPatternTextField;
    }

    public JLabel getPatternLabel() {
        return patternLabel;
    }

    public String getDisplayOption() {
        if (allRadioButton.isSelected()) {
          return EVERY_THING;
        } else if (justFaxesRadioButton.isSelected()) {
          return JUST_FAXES;
        } else if (justGRIBRadioButton.isSelected()) {
          return JUST_GRIBS;
        }
        return null;
    }

    public String getFaxNameRegExpr() {
        String expr = null;
        if (faxFilterCheckBox.isSelected() && faxNameFilterTextField.isEnabled()) {
          expr = faxNameFilterTextField.getText().trim();
        }
        return expr;
    }

    public boolean withBoatAndTrack() {
        return boatAndTrackCheckBox.isSelected();
    }

    public boolean withCommentsOnly() {
        return withCommentOnlyCheckBox.isSelected();
    }

    public String getPDFTitle() {
        if (pdfCheckBox.isSelected()) {
          return pdfTitle.getText();
        } else {
          return null;
        }
    }
}
