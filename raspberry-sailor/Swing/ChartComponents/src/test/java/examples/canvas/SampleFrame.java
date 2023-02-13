package examples.canvas;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {
    private BorderLayout borderLayout;
    //private CommandPanel commandPanel;
    private CommandPanel_II commandPanel;

    public SampleFrame() {
        borderLayout = new BorderLayout();
//  commandPanel = new CommandPanel(this);
        commandPanel = new CommandPanel_II(this);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout);
        setSize(new Dimension(600, 400));
        setTitle("Mercator Template");
        getContentPane().add(commandPanel, BorderLayout.CENTER);
    }
}
