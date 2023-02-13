package examples.globe;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {
    private BorderLayout borderLayout;
    private CommandPanel commandPanel;
    private JPanel statusPanel;
    private JLabel status = new JLabel();

    public SampleFrame() {
        borderLayout = new BorderLayout();
        commandPanel = new CommandPanel(this);
        statusPanel = new JPanel();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout);
        setSize(new Dimension(800, 600));
        setTitle("Globe!");
        getContentPane().add(commandPanel, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(status, BorderLayout.WEST);
    }

    public void setStatus(String s) {
        status.setText(s);
    }
}
