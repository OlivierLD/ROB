package chartview.gui.toolbar.controlpanels.projection;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class ConicPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JTextField contactParallelTextField = new JTextField();
    private final JComboBox<String> signComboBox = new JComboBox<>();
    private int projType = 0;

    public ConicPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        jLabel1.setText(WWGnlUtilities.buildMessage("contact-parallel"));
        contactParallelTextField.setPreferredSize(new Dimension(40, 20));
        contactParallelTextField.setHorizontalAlignment(JTextField.RIGHT);
        signComboBox.setPreferredSize(new Dimension(40, 20));

        signComboBox.addItem("N");
        signComboBox.addItem("S");
        this.add(jLabel1,
                new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(contactParallelTextField,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(signComboBox,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 0), 0, 0));
    }

    public double getContactParallel() {
        double d = Double.parseDouble(contactParallelTextField.getText());
        if ((signComboBox.getSelectedItem()).equals("S"))
            d = -d;
        return d;
    }

    public void setContactParallel(double d) {
        contactParallelTextField.setText(Double.toString(Math.abs(d)));
        if (d < 0D)
            signComboBox.setSelectedItem("S");
        else
            signComboBox.setSelectedItem("N");
    }

    public void setProjType(int projType) {
        this.projType = projType;
    }

    public int getProjType() {
        return projType;
    }
}
