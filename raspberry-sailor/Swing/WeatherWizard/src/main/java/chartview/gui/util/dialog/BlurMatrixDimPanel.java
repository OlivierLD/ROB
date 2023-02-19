package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;

public class BlurMatrixDimPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel matrixLabel = new JLabel();
    private final transient SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
    private final JSpinner matrixSpinner = new JSpinner(spinnerModel);

    public BlurMatrixDimPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(252, 186));
        matrixLabel.setText(WWGnlUtilities.buildMessage("matrix-dimension"));
        this.add(matrixLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        this.add(matrixSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setMatrixSize(int i) {
        matrixSpinner.setValue(i);
    }

    public int getMatrixSize() {
        return (Integer) matrixSpinner.getValue();
    }
}
