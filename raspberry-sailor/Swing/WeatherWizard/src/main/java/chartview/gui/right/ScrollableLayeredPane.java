package chartview.gui.right;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.Scrollable;


public class ScrollableLayeredPane 
     extends JLayeredPane
  implements Scrollable
{

  private int w;
  private int h;

  public ScrollableLayeredPane(int w, int h)
  {
    this.w = w;
    this.h = h;
    setPreferredSize(new Dimension(w, h));
  }

  public Dimension getPreferredSize()
  {
    return super.getPreferredSize();
  }

  public void setPreferredSize(Dimension preferredSize)
  {
    super.setPreferredSize(preferredSize);
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 1;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 10;
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }
}
