package chartview.gui.toolbar.controlpanels;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.util.dialog.FaxType;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class SelectFaxPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JComboBox<FaxType> activeFaxComboBox = new JComboBox<>();
    private final JLabel faxLabel = new JLabel();
    private final JCheckBox allFaxesCheckBox = new JCheckBox();

    private final transient ListCellRenderer colorRenderer = (list, value, index, isSelected, cellHasFocus) -> {
        FaxType ft = (FaxType) value;
        JLabel label = null;
        if (ft != null) {
            String val = ft.getValue();
            Color c = ft.getColor();
            label = new JLabel(val);
            if (isSelected) {
              label.setBackground(list.getSelectionBackground());
            } else {
              label.setBackground(Color.white);
            }
            label.setForeground(c);
            label.setOpaque(true);
        }
        return label;
    };

    public SelectFaxPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from SelectFaxPanel.";
            }

            public void faxesLoaded(FaxType[] ft) {
                // Reset poplist
                activeFaxComboBox.removeAllItems();
              for (FaxType faxType : ft) {
                activeFaxComboBox.addItem(faxType);
    //          System.out.println("Adding " + ft[i].getValue());
              }
                // broadcast ative fax, first one.
                if (ft != null && ft.length > 0) {
                  WWContext.getInstance().fireActiveFaxChanged(ft[0]);
                }
            }
        });
        this.setLayout(gridBagLayout1);
        activeFaxComboBox.setMinimumSize(new Dimension(100, 20));
        activeFaxComboBox.setPreferredSize(new Dimension(150, 20));
        activeFaxComboBox.setRenderer(colorRenderer);
        activeFaxComboBox.addActionListener(this::activeFaxComboBox_actionPerformed);
        faxLabel.setText(WWGnlUtilities.buildMessage("active-fax"));
        allFaxesCheckBox.setText(WWGnlUtilities.buildMessage("all-faxes"));
        allFaxesCheckBox.addActionListener(this::allFaxesCheckBox_actionPerformed);
        this.add(activeFaxComboBox,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(faxLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(allFaxesCheckBox,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
    }

    private void allFaxesCheckBox_actionPerformed(ActionEvent e) {
        activeFaxComboBox.setEnabled(!allFaxesCheckBox.isSelected());
        if (allFaxesCheckBox.isSelected()) {
            // Broadcast
            faxLabel.setEnabled(false);
            WWContext.getInstance().fireAllFaxesSelected();
        } else {
            // Broadcast
            faxLabel.setEnabled(true);
            WWContext.getInstance().fireActiveFaxChanged((FaxType) activeFaxComboBox.getSelectedItem());
        }
    }

    private void activeFaxComboBox_actionPerformed(ActionEvent e) {
        FaxType ft = (FaxType) activeFaxComboBox.getSelectedItem();
        if (ft != null) {
            activeFaxComboBox.setForeground(ft.getColor());
            faxLabel.setForeground(ft.getColor());
            WWContext.getInstance().fireActiveFaxChanged(ft);
            ft.getRotation();
        }
    }

    public void setEnabled(boolean b) {
        faxLabel.setEnabled(b);
        allFaxesCheckBox.setEnabled(b);
        activeFaxComboBox.setEnabled(b);
    }
}
