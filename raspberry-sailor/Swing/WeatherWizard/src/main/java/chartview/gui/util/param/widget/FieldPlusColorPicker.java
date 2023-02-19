package chartview.gui.util.param.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class FieldPlusColorPicker
        extends FieldPlusFinder {
    public FieldPlusColorPicker(Object o) {
        super(o);
    }

    protected Object invokeEditor() {
        Color c = JColorChooser.showDialog(this, "Color", this.textField.getBackground());
        this.value = c;
        return c;
    }

    protected void finderButton_actionPerformed(ActionEvent e) {
        Object o = invokeEditor();
        if (o instanceof Color) {
            Color c = (Color) o;
            if (c != null) {
                this.value = o;
                this.textField.setBackground(c);
            }
        }
    }
}
