package chartview.gui.util.tree;

import chartview.util.WWGnlUtilities;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;


public class EditGRIBRequestPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JComboBox<String> groupComboBox = new JComboBox<>();
    private final JTextField requestNameTextField = new JTextField();
    private final JTextField gribRequestTextField = new JTextField();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();

    private XMLDocument gribRequests = null;

    public EditGRIBRequestPanel(XMLDocument doc) {
        gribRequests = doc;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setLayout(gridBagLayout1);
        groupComboBox.setSize(new Dimension(150, 20));
        groupComboBox.setEditable(true);
        requestNameTextField.setMinimumSize(new Dimension(150, 20));
        requestNameTextField.setPreferredSize(new Dimension(150, 20));
        jLabel1.setText(WWGnlUtilities.buildMessage("request-group"));
        jLabel2.setText(WWGnlUtilities.buildMessage("request-name"));
        this.add(groupComboBox,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(requestNameTextField,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel1,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        // GRIB Request to save
        // Populate Groups
        this.add(gribRequestTextField,
                new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        NodeList groups = gribRequests.selectNodes("/grib-collection/grib-set");
        groupComboBox.removeAllItems();
        for (int i = 0; i < groups.getLength(); i++) {
            XMLElement node = (XMLElement) groups.item(i);
            String id = node.getAttribute("id");
            groupComboBox.addItem(id);
        }
    }

    public String getGroup() {
        return (String) groupComboBox.getSelectedItem();
    }

    public String getName() {
        return requestNameTextField.getText();
    }

    public String getRequest() {
        return gribRequestTextField.getText();
    }

    public void setGribRequest(String s) {
        gribRequestTextField.setText(s);
    }

    public void setName(String s) {
        requestNameTextField.setText(s);
    }

    public void setGroup(String s) {
        groupComboBox.setSelectedItem(s);
    }
}
