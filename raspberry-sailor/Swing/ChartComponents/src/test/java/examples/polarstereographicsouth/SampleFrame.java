package examples.polarstereographicsouth;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {
    private BorderLayout borderLayout1;
    private CommandPanel commandPanel1;

    public SampleFrame() {
        borderLayout1 = new BorderLayout();
        commandPanel1 = new CommandPanel();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout1);
        setSize(new Dimension(800, 600));
        setTitle("Example - Polar Stereographic");
        this.setBackground(new Color(190, 220, 216));
        getContentPane().setBackground(this.getBackground());
        commandPanel1.setSize(800, 600);
        getContentPane().add(commandPanel1, BorderLayout.CENTER);
    }
}
