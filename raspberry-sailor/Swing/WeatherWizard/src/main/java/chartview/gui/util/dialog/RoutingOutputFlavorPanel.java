package chartview.gui.util.dialog;

import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class RoutingOutputFlavorPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();

    private final ButtonGroup groupOne = new ButtonGroup();
    private final ButtonGroup groupTwo = new ButtonGroup();
    private final JRadioButton csvRadioButton = new JRadioButton();
    private final JRadioButton gpxRadioButton = new JRadioButton();
    private final JRadioButton txtRadioButton = new JRadioButton();
    private final JPanel rightPanel = new JPanel();
    private final JRadioButton clipboardRadioButton = new JRadioButton();
    private final JRadioButton saveFileRadioButton = new JRadioButton();
    private final GridBagLayout gridBagLayout2 = new GridBagLayout();
    private final JSeparator separator = new JSeparator();
    private final JPanel filePanel = new JPanel();
    private final JButton saveAsButton = new JButton();
    private final JTextField fileNameTextField = new JTextField();
    private final JRadioButton kmlRadioButton = new JRadioButton();
    private final JRadioButton jsonRadioButton = new JRadioButton();

    public RoutingOutputFlavorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(518, 127));
        csvRadioButton.setText("CSV");
        csvRadioButton.setToolTipText("<html>For a spreadsheet</html>");
        csvRadioButton.setSelected(true);
        txtRadioButton.setText("Text");
        txtRadioButton.setToolTipText("<html>Raw text.<br>Readable in any text editor</html>");
        rightPanel.setLayout(gridBagLayout2);
        clipboardRadioButton.setText("Clipboard");
        clipboardRadioButton.setSelected(true);
        saveFileRadioButton.setText("Save File");
        saveFileRadioButton.addActionListener(this::saveFileRadioButton_actionPerformed);
        separator.setOrientation(SwingConstants.VERTICAL);
        saveAsButton.setText("Save as...");
        fileNameTextField.setPreferredSize(new Dimension(200, 19));
        gpxRadioButton.setText("GPX");
        gpxRadioButton.setToolTipText("<html>For OpenCPN,<br>or other navigation software</html>");
        this.add(csvRadioButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(gpxRadioButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(txtRadioButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(rightPanel, new GridBagConstraints(3, 1, 1, 3, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                new Insets(0, 10, 0, 0), 0, 0));
        this.add(separator, new GridBagConstraints(2, 1, 1, 5, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 5, 0, 5), 0, 0));
        this.add(kmlRadioButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jsonRadioButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        rightPanel.add(clipboardRadioButton,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        rightPanel.add(saveFileRadioButton,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 5, 0, 0), 0, 0));
        rightPanel.add(filePanel,
                new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,
                        0,
                        0,
                        0),
                        0, 0));

        filePanel.add(fileNameTextField, null);
        filePanel.add(saveAsButton, null);
        fileNameTextField.setEnabled(false);
        kmlRadioButton.setText("KML");
        kmlRadioButton.setToolTipText("<html>For Google Earth</html>");
        jsonRadioButton.setText("json");
        jsonRadioButton.setToolTipText("<html>JavaScript Object Notation,<br>suitable for GoogleMaps</html>");
        saveAsButton.setEnabled(false);

        saveAsButton.addActionListener(this::saveAsButton_actionPerformed);
        groupOne.add(csvRadioButton);
        groupOne.add(gpxRadioButton);
        groupOne.add(txtRadioButton);
        groupOne.add(kmlRadioButton);
        groupOne.add(jsonRadioButton);

        groupTwo.add(clipboardRadioButton);
        groupTwo.add(saveFileRadioButton);
    }

    public int getSelectedOption() {
        int option = -1;
        if (csvRadioButton.isSelected()) {
          option = ParamPanel.RoutingOutputList.CSV;
        } else if (txtRadioButton.isSelected()) {
          option = ParamPanel.RoutingOutputList.TXT;
        } else if (gpxRadioButton.isSelected()) {
          option = ParamPanel.RoutingOutputList.GPX;
        } else if (kmlRadioButton.isSelected()) {
          option = ParamPanel.RoutingOutputList.KML;
        } else if (jsonRadioButton.isSelected()) {
          option = ParamPanel.RoutingOutputList.JSON;
        }
        return option;
    }

    public String getFileOutput() {
        String output = null;
        if (saveFileRadioButton.isSelected()) {
          output = fileNameTextField.getText();
        }
        return output;
    }

    private void saveFileRadioButton_actionPerformed(ActionEvent e) {
        fileNameTextField.setEnabled(saveFileRadioButton.isSelected());
        saveAsButton.setEnabled(saveFileRadioButton.isSelected());
    }

    private void saveAsButton_actionPerformed(ActionEvent e) {
        String extension = "csv";
        if (txtRadioButton.isSelected()) {
          extension = "txt";
        } else if (gpxRadioButton.isSelected()) {
          extension = "gpx";
        } else if (kmlRadioButton.isSelected()) {
          extension = "kml";
        } else if (jsonRadioButton.isSelected()) {
          extension = "json";
        }
        String fileName = WWGnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY,
                new String[]{extension},
                "Routing output",
                ".",
                WWGnlUtilities.buildMessage("save-as-2"),
                "Routing output");
        if (fileName.trim().length() > 0) {
          fileNameTextField.setText(Utilities.makeSureExtensionIsOK(fileName, "." + extension));
        }
    }
}
