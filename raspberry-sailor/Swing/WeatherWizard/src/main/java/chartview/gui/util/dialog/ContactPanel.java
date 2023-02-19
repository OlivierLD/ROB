package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class ContactPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();
    private final JLabel jLabel3 = new JLabel();
    private final JTextField nameTextField = new JTextField();
    private final JTextField emailTextField = new JTextField();
    private final JPanel jPanel1 = new JPanel();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTextArea messageTextArea = new JTextArea();

    public ContactPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        jLabel1.setText(WWGnlUtilities.buildMessage("your-name"));
        jLabel2.setText(WWGnlUtilities.buildMessage("your-email"));
        jLabel3.setText(WWGnlUtilities.buildMessage("your-message"));
        nameTextField.setMinimumSize(new Dimension(100, 20));
        nameTextField.setPreferredSize(new Dimension(200, 20));
        emailTextField.setPreferredSize(new Dimension(200, 20));
        jPanel1.setPreferredSize(new Dimension(200, 100));
        jPanel1.setLayout(borderLayout1);
        this.add(jLabel1,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(nameTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(emailTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jScrollPane1.getViewport().add(messageTextArea, null);
        jPanel1.add(jScrollPane1, BorderLayout.CENTER);
        this.add(jPanel1,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0,
                        0,
                        0),
                        0, 0));
    }

    public String getName() {
        return nameTextField.getText();
    }

    public String getEmail() {
        return emailTextField.getText();
    }

    public String getMessage() {
        return messageTextArea.getText();
    }
}
