package chartview.gui;


import chartview.util.WWGnlUtilities;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ShiftTabPopup
        extends JPopupMenu
        implements ActionListener, PopupMenuListener {
    final JMenuItem shiftRight;
    final JMenuItem shiftLeft;
    //JMenuItem closeAll;
    final JMenuItem closeOthers;

    private static final String SHIFT_RIGHT = WWGnlUtilities.buildMessage("shift-right");
    private static final String SHIFT_LEFT = WWGnlUtilities.buildMessage("shift-left");
    //private static final String CLOSE_ALL    = WWGnlUtilities.buildMessage("close-all");
    private static final String CLOSE_OTHERS = WWGnlUtilities.buildMessage("close-others");

    AdjustFrame parent;
    int tab = -1;

    public ShiftTabPopup(AdjustFrame caller, int tab) {
        super();
        parent = caller;
        this.tab = tab;
        this.add(shiftRight = new JMenuItem(SHIFT_RIGHT));
        shiftRight.setIcon(new ImageIcon(this.getClass().getResource("img/panright.gif")));
        shiftRight.addActionListener(this);
        this.add(shiftLeft = new JMenuItem(SHIFT_LEFT));
        shiftLeft.setIcon(new ImageIcon(this.getClass().getResource("img/panleft.gif")));
        shiftLeft.addActionListener(this);

        // this.add(closeAll = new JMenuItem(CLOSE_ALL));
        // closeAll.addActionListener(this);
        this.add(closeOthers = new JMenuItem(CLOSE_OTHERS));
        closeOthers.setIcon(new ImageIcon(this.getClass().getResource("img/remove_file.png")));
        closeOthers.addActionListener(this);
    }

    public void enableShiftRight(boolean b) {
        shiftRight.setVisible(b);
    }

    public void enableShiftLeft(boolean b) {
        shiftLeft.setVisible(b);
    }

    public void enableCloseOthers(boolean b) {
        closeOthers.setVisible(b);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SHIFT_RIGHT)) {
            parent.shiftTabRight(tab);
        } else if (event.getActionCommand().equals(SHIFT_LEFT)) {
            parent.shiftTabLeft(tab);
        } else if (event.getActionCommand().equals(CLOSE_OTHERS)) {
            parent.closeOthers(tab);
        }
        this.setVisible(false); // Shut popup when done.
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void show(Component c, int x, int y) {
        super.show(c, x, y);
    }
}
