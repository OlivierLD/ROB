package chartview.gui.util.dialog;

import java.awt.*;

public class FaxPatternType {
    String text = "";
    Color color = null;

    public FaxPatternType(String str, Color c) {
        this.text = str;
        this.color = c;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public String toString() {
        return text;
    }
}
