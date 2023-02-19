package chartview.gui.util.dialog;

import javax.swing.*;
import java.awt.*;


public class TextAreaPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane = new JScrollPane();
    private final JTextArea jTextArea = new JTextArea();

    public TextAreaPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(290, 200));
        this.setPreferredSize(new Dimension(290, 200));
        jScrollPane.getViewport().add(jTextArea, null);
        this.add(jScrollPane, BorderLayout.CENTER);
    }

    public void setText(String s) {
        jTextArea.setText(s);
    }

    public String getText() {
        return jTextArea.getText();
    }
}
