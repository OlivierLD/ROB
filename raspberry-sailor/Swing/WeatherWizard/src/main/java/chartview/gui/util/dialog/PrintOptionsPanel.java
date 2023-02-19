package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class PrintOptionsPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JTextField titleTextField = new JTextField();
    private final JCheckBox fullScreenCheckBox = new JCheckBox();
    private final JLabel titleLabel = new JLabel();

    public PrintOptionsPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        titleTextField.setPreferredSize(new Dimension(200, 20));
        fullScreenCheckBox.setText(WWGnlUtilities.buildMessage("print-all-composites"));
        fullScreenCheckBox.setToolTipText(WWGnlUtilities.buildMessage("all-vs-visible"));
        fullScreenCheckBox.setSelected(true);
        titleLabel.setText(WWGnlUtilities.buildMessage("title"));
        this.add(titleTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(fullScreenCheckBox, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

    public String getTitle() {
        return titleTextField.getText();
    }

    public boolean getAllCompositeOption() {
        return fullScreenCheckBox.isSelected();
    }
}
