package chartview.gui.util.dialog;

import chartview.ctx.WWContext;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public class FileAndColorPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JTextField fileNameTextField = new JTextField();
    private final JButton browseButton = new JButton();
    private final JLabel colorLabel = new JLabel();
    private final JButton colorButton = new JButton();

    Color faxColor = Color.black;
    private final JCheckBox applyColorCheckBox = new JCheckBox();

    public FileAndColorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        fileNameTextField.setPreferredSize(new Dimension(200, 20));
        browseButton.setText(WWGnlUtilities.buildMessage("browse"));
        browseButton.addActionListener(this::browseButton_actionPerformed);
        colorLabel.setText(WWGnlUtilities.buildMessage("color"));
        colorLabel.setBackground(Color.black);
        colorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        colorLabel.setOpaque(true);
        colorButton.setText(WWGnlUtilities.buildMessage("color-plus"));
        colorButton.addActionListener(this::colorButton_actionPerformed);
        applyColorCheckBox.setText("Apply Color"); // LOCALIZE
        applyColorCheckBox.setSelected(true);
        applyColorCheckBox.addActionListener(this::applyColorCheckBox_actionPerformed);
        this.add(fileNameTextField, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(colorLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
        this.add(colorButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(applyColorCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void browseButton_actionPerformed(ActionEvent e) {
        String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
        String fax = WWGnlUtilities.chooseFile(this,
                JFileChooser.FILES_ONLY,
                new String[]{"gif", "jpg", "jpeg", "tif", "tiff", "png"},
                WWGnlUtilities.buildMessage("splash-faxes"),
                firstDir,
                WWGnlUtilities.buildMessage("open"),
                WWGnlUtilities.buildMessage("open-fax"),
                true); // true: with previewer
        if (fax != null && fax.trim().length() > 0) {
          fileNameTextField.setText(fax);
        }
    }

    private void colorButton_actionPerformed(ActionEvent e) {
        Color orig = faxColor;
        Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("set-fax-color"), orig);
        if (newColor != null) {
            faxColor = newColor;
            colorLabel.setBackground(faxColor);
            colorLabel.repaint();
            WWContext.getInstance().fireLogging("New Color:" + faxColor.toString() + "\n");
        }
    }

    public Color getColor() {
        return faxColor;
    }

    public String getFileName() {
        return fileNameTextField.getText();
    }

    public void setColor(Color c) {
        faxColor = c;
        colorLabel.setBackground(c);
    }

    public void setFileName(String s) {
        fileNameTextField.setText(s);
    }

    private void applyColorCheckBox_actionPerformed(ActionEvent e) {
        colorLabel.setEnabled(applyColorCheckBox.isSelected());
        colorButton.setEnabled(applyColorCheckBox.isSelected());
    }

    public boolean isColorApplied() {
        return applyColorCheckBox.isSelected();
    }

    public void setColorApplied(boolean b) {
        applyColorCheckBox.setSelected(b);
        applyColorCheckBox_actionPerformed(null);
    }
}
