package chartview.gui.toolbar.controlpanels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class SingleControlPane
        extends JPanel {
    private final CustomPanelButton topPanel;

    private final JPanel controlPanel;
    protected boolean visible;

    protected boolean enabled = true;

    public SingleControlPane(String title, JPanel control, boolean visible) {
        this.controlPanel = control;
        this.visible = visible;
        topPanel = new CustomPanelButton(title);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.NORTH);
        this.add(controlPanel, BorderLayout.CENTER);
        controlPanel.setVisible(visible);
        topPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (topPanel.isEnabled()) {
                    visible = !visible;
                    controlPanel.setVisible(visible);
                }
                onClickOnControl(enabled && visible);
            }
        });
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled && visible) {
          controlPanel.setVisible(false);
        }
        topPanel.setEnabled(enabled);
        onClickOnControl(enabled && visible);
    }

    protected void onClickOnControl(boolean displayingData) {
    }
}
