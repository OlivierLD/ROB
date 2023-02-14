package main.tests;

import chartview.gui.util.dialog.UpdatePanel;

import javax.swing.*;

public class Test {
    public static void main(String... args) {
        UpdatePanel up = new UpdatePanel();
        up.setTopLabel("Akeu");
        up.setBottomLabel1("Coucou");
        up.setBottomLabel2("Larigou");
        up.setFileList("ah!\nBen\nVla ot'choz!\nOn verra...");
        JOptionPane.showMessageDialog(null, up);
    }
}
