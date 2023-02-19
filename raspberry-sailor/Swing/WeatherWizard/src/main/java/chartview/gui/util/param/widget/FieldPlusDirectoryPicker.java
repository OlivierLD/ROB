package chartview.gui.util.param.widget;

import chartview.util.WWGnlUtilities;

import javax.swing.*;


public class FieldPlusDirectoryPicker
        extends FieldPlusFinder {
    String[] fileTypes = new String[]{""};
    String fileDesc = "";

    public FieldPlusDirectoryPicker(Object o, String s) {
        super(o);
        fileDesc = s;
    }

    protected Object invokeEditor() {
        String s = WWGnlUtilities.chooseFile(this,
                JFileChooser.DIRECTORIES_ONLY,
                fileTypes,
                fileDesc,
                ".",
                WWGnlUtilities.buildMessage("save"),
                WWGnlUtilities.buildMessage("choose-directory"));
        return s;
    }
}
