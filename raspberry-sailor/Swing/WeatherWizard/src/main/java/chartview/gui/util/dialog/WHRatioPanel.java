package chartview.gui.util.dialog;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class WHRatioPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JTextField whRatioTextField = new JTextField();

    public WHRatioPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        jLabel1.setText(WWGnlUtilities.buildMessage("fax-hw-ratio"));
        whRatioTextField.setHorizontalAlignment(JTextField.RIGHT);
        whRatioTextField.setText("1.0");
        this.add(jLabel1,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(whRatioTextField,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
    }

    public double getRatio() {
        return (Double.parseDouble(whRatioTextField.getText()));
    }

    public void setRatio(double d) {
        whRatioTextField.setText(Double.toString(d));
    }
}
