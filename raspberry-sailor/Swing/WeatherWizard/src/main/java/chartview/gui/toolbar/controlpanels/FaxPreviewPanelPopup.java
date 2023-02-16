package chartview.gui.toolbar.controlpanels;

import chartview.util.WWGnlUtilities;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


public class FaxPreviewPanelPopup
  extends JPopupMenu
  implements ActionListener, PopupMenuListener
{
  JMenuItem reset;
  JMenuItem zoomIn;
  JMenuItem zoomOut;

  private static final String RESET_ZOOM = WWGnlUtilities.buildMessage("reset-fax");
  private static final String ZOOM_IN = WWGnlUtilities.buildMessage("fax-zoom-in");
  private static final String ZOOM_OUT = WWGnlUtilities.buildMessage("fax-zoom-out");

  FaxPreviewPanel parent;
  
  public FaxPreviewPanelPopup(FaxPreviewPanel caller)
  {
    super();
    parent = caller;
    this.add(reset = new JMenuItem(RESET_ZOOM));
    reset.addActionListener(this);
    this.add(zoomIn = new JMenuItem(ZOOM_IN));
    zoomIn.addActionListener(this);
    this.add(zoomOut = new JMenuItem(ZOOM_OUT));
    zoomOut.addActionListener(this);
  }

  public void actionPerformed(ActionEvent event)
  {
    if (event.getActionCommand().equals(RESET_ZOOM))
    {
      parent.setZoom(1D);
      parent.zoomHasChanged();
    }
    else if (event.getActionCommand().equals(ZOOM_IN))
    {
      parent.setZoom(parent.getZoom() * 1.1);
      parent.zoomHasChanged();
    }
    else if (event.getActionCommand().equals(ZOOM_OUT))
    {
      parent.setZoom(parent.getZoom() / 1.1);
      parent.zoomHasChanged();
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
