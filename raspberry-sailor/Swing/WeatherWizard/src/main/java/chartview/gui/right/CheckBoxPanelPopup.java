package chartview.gui.right;


import chartview.util.WWGnlUtilities;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CheckBoxPanelPopup
        extends JPopupMenu
        implements ActionListener, PopupMenuListener {
    private CommandPanel parent;

    private JMenuItem checkBoxOption;
    private JMenuItem radioButtonOption;

    private final String CHECKBOX_OPTION = WWGnlUtilities.buildMessage("checkbox-panel-option");
    private final String RADIOBUTTON_OPTION = WWGnlUtilities.buildMessage("radiobutton-panel-option");
    private int _x = 0, _y = 0;

    public CheckBoxPanelPopup(CommandPanel ccp, int x, int y) {
        super();
        this.parent = ccp;
        this._x = x;
        this._y = y;

        this.setBackground(Color.white);

        checkBoxOption = new JMenuItem(CHECKBOX_OPTION);
//  checkBoxOption.setIcon(new ImageIcon(this.getClass().getResource("remove_composite.png")));
        this.add(checkBoxOption);
        checkBoxOption.addActionListener(this);
        checkBoxOption.setBackground(Color.white);

        radioButtonOption = new JMenuItem(RADIOBUTTON_OPTION);
//  radioButtonOption.setIcon(new ImageIcon(this.getClass().getResource("composite.png")));
        this.add(radioButtonOption);
        radioButtonOption.addActionListener(this);
        radioButtonOption.setBackground(Color.white);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(CHECKBOX_OPTION)) {
            parent.setCheckBoxPanelOption(CommandPanel.CHECKBOX_OPTION);
        }
        if (event.getActionCommand().equals(RADIOBUTTON_OPTION)) {
            parent.setCheckBoxPanelOption(CommandPanel.RADIOBUTTON_OPTION);
        }
        parent.repaint();
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void show(Component c, int x, int y) {
        super.show(c, x, y);
        _x = x;
        _y = y;

        checkBoxOption.setEnabled(parent.getCheckBoxPanelOption() != CommandPanel.CHECKBOX_OPTION);
        radioButtonOption.setEnabled(parent.getCheckBoxPanelOption() != CommandPanel.RADIOBUTTON_OPTION);
    }
}
