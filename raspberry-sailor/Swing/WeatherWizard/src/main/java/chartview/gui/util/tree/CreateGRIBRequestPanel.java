package chartview.gui.util.tree;

import chartview.util.WWGnlUtilities;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;


public class CreateGRIBRequestPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel gribRequestLabel = new JLabel();
    private final JComboBox<String> groupComboBox = new JComboBox<>();
    private final JTextField requestNameTextField = new JTextField();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();

    private XMLDocument gribRequests = null;
    private String currentRequest = null;

    public CreateGRIBRequestPanel(String req, XMLDocument doc) {
        gribRequests = doc;
        currentRequest = req;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setLayout(gridBagLayout1);
        gribRequestLabel.setText("GFS:Yada-yada");
        groupComboBox.setSize(new Dimension(150, 20));
        groupComboBox.setEditable(true);
        requestNameTextField.setMinimumSize(new Dimension(150, 20));
        requestNameTextField.setPreferredSize(new Dimension(150, 20));
        jLabel1.setText(WWGnlUtilities.buildMessage("request-group"));
        jLabel2.setText(WWGnlUtilities.buildMessage("request-name"));
        this.add(gribRequestLabel,
                new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(groupComboBox,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(requestNameTextField,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        // GRIB Request to save
        gribRequestLabel.setText(currentRequest);
        // Populate Groups
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
}
