package examples.nightnday;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {

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
        setSize(new Dimension(600, 400));
        setTitle("Example - The world");
        getContentPane().add(commandPanel1, BorderLayout.CENTER);
    }

    private BorderLayout borderLayout1;
    private CommandPanel commandPanel1;
}
