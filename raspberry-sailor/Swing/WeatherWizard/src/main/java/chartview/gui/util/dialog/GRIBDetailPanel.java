package chartview.gui.util.dialog;

import javax.swing.*;
import java.awt.*;


public class GRIBDetailPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane = new JScrollPane();
    private final JTextArea jTextArea = new JTextArea();

    public GRIBDetailPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(400, 250));
        this.setPreferredSize(new Dimension(400, 250));
        jTextArea.setEditable(false);
        jTextArea.setFont(new Font("Courier", Font.PLAIN, 11));
        jScrollPane.getViewport().add(jTextArea, null);
        this.add(jScrollPane, BorderLayout.CENTER);
    }

    public void setText(String str) {
        jTextArea.setText(str);
    }
}
