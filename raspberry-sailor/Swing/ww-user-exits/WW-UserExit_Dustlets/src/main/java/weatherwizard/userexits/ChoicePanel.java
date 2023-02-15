package weatherwizard.userexits;

import javax.swing.*;
import java.awt.*;

public class ChoicePanel
        extends JPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JRadioButton fullRadioButton = new JRadioButton();
    private JRadioButton visibleRadioButton = new JRadioButton();

    private ButtonGroup group = new ButtonGroup();

    public ChoicePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setLayout(gridBagLayout1);
        group.add(fullRadioButton);
        group.add(visibleRadioButton);
        fullRadioButton.setText("Full Chart");
        fullRadioButton.setSelected(true);
        visibleRadioButton.setText("Visible Part Only");
        this.add(fullRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(visibleRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

    public boolean isVisibleSelected() {
        return visibleRadioButton.isSelected();
    }

    public boolean isFullChartSelected() {
        return fullRadioButton.isSelected();
    }
}
