package chartview.gui.util.param.widget;

import chartview.gui.util.dialog.FaxType;
import chartview.gui.util.dialog.FileAndColorPanel;

import javax.swing.*;
import java.awt.*;


public class FieldPlusFileAndColorPicker
        extends FieldPlusFinder {
    String[] fileTypes = null;
    String fileDesc = "";

    public FieldPlusFileAndColorPicker(Object o, String[] sa, String s) {
        super(o);
        fileTypes = sa;
        fileDesc = s;
    }

    FileAndColorPanel facp = null;

    protected Object invokeEditor() {
        Object ft = null;
        if (facp == null)
            facp = new FileAndColorPanel();
        FaxType ftObj = (FaxType) getValue();
        facp.setColor(ftObj.getColor());
        facp.setColorApplied(ftObj.isChangeColor());
        facp.setFileName(ftObj.getValue());
        int resp = JOptionPane.showConfirmDialog(this,
                facp,
                "Choose fax & color",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            String s = facp.getFileName();
            Color c = facp.getColor();
            boolean b = facp.isColorApplied();
            ft = new FaxType(s, c, true, true, 0D, null, null, b);
        }
        return ft;
    }
}
