package chartview.gui.toolbar.controlpanels;

import chartview.util.WWGnlUtilities;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;


public class LoggingPanelPopup
  extends JPopupMenu
  implements ActionListener, PopupMenuListener
{
  JMenuItem refresh;
  JMenuItem edit;

  JTextComponent parent = null;

  private static final String CLEAR = WWGnlUtilities.buildMessage("clear");
  private static final String CLIPBOARD = WWGnlUtilities.buildMessage("copy-to-clipboard");

  public LoggingPanelPopup(JTextComponent caller)
  {
    super();
    this.parent = caller;
    this.add(refresh = new JMenuItem(CLEAR));
    refresh.addActionListener(this);
    this.add(edit = new JMenuItem(CLIPBOARD));
    edit.addActionListener(this);
  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getActionCommand().equals(CLEAR))
    {
      parent.setText("");
    }
    else if (event.getActionCommand().equals(CLIPBOARD))
    {
      String str = parent.getText();
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection stringSelection = new StringSelection(str);
      clipboard.setContents(stringSelection, null);          
    }
    this.setVisible(false); // Shut popup when done.
  }

  public void popupMenuWillBecomeVisible(PopupMenuEvent e)
  {
  }

  public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
  {
  }

  public void popupMenuCanceled(PopupMenuEvent e)
  {
  }

  public void show(Component c, int x, int y)
  {
    super.show(c, x, y);
  }
}
