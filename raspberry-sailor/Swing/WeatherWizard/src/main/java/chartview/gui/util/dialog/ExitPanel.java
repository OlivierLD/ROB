package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class ExitPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JCheckBox dontAskCheckBox = new JCheckBox();

    public ExitPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        jLabel1.setText(WWGnlUtilities.buildMessage("sure"));
        dontAskCheckBox.setText(WWGnlUtilities.buildMessage("dont-ask-again"));
        this.add(jLabel1,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 10, 0), 0, 0));
        this.add(dontAskCheckBox,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
    }

    public boolean shutUpNextTime() {
        return dontAskCheckBox.isSelected();
    }
}
