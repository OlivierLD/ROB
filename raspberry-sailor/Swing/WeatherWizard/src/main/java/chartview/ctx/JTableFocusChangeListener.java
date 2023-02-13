package chartview.ctx;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;

public class JTableFocusChangeListener
  implements PropertyChangeListener
{
  private boolean wasIn = true;
  private Window originalWindowFocusOwner = null;
  private JTable jTable;

  public JTableFocusChangeListener(JTable jt)
  {
    this.jTable = jt;
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    Component oldComp = (Component) evt.getOldValue();
    Component newComp = (Component) evt.getNewValue();

    if ("focusOwner".equals(evt.getPropertyName()))
    {
      Window windowFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
      if (originalWindowFocusOwner == null)
      {
//        if (windowFocusOwner instanceof JFrame)
//          System.out.println("... Checkin if table is part of [" + ((JFrame)windowFocusOwner).getTitle() + "]");
//        else if (windowFocusOwner instanceof JDialog)
//          System.out.println("... Checkin if table is part of [" + ((JDialog)windowFocusOwner).getTitle() + "]");
//        else
//        {
//          System.out.println("... Window is a " + windowFocusOwner.getClass().getName());
//        }
        if (windowFocusOwner.equals(getFirstOwnerWindow(jTable)))
        {
//        System.out.println("......Yes!");
          originalWindowFocusOwner = windowFocusOwner;        
        }
//      else
//        System.out.println(".....No.");
      }
      if (originalWindowFocusOwner != null && originalWindowFocusOwner.equals(windowFocusOwner)) // No stopCellEditing needed when new dialog pops up
      {
        if (oldComp == null)
        {
          boolean b = isChildOf(newComp, jTable);
          // the newComp component gained the focus
          if (!b)
          {
//          System.out.println("New component (" + newComp.getClass().getName() + ") is NOT part of the thing, gained focus (was-in:" + wasIn +")");
            if (wasIn && jTable.isEditing())
            {
//            System.out.println("Stopping Cell Editing");
              jTable.getCellEditor().stopCellEditing();
            }
          }
//          else
//            System.out.println("New component (" + newComp.getClass().getName() + ") is part of the thing, gained focus");
        }
        else
        {
          boolean b = isChildOf(oldComp, jTable);
          // the oldComp component lost the focus
//          if (b)
//            System.out.println("Old component (" + oldComp.getClass().getName() + ") was part of the thing, lost focus");
//          else
//            System.out.println("Old component (" + oldComp.getClass().getName() + ") was NOT part of the thing, lost focus");
          wasIn = b;
        }
      }
    }
//    else
//      System.out.println("Property " + evt.getPropertyName());
  }

  private Window getFirstOwnerWindow(Component comp)
  {
    Window ow = null;
    Component start = comp;
    if (comp instanceof Window)
      ow = (Window)comp;
    else
    {
      while (start.getParent() != null)
      {
        start = start.getParent();
        if (start instanceof Window)
        {
          ow = (Window)start;
          break;
        }
      }
    }    
    return ow;
  }
  
  private boolean isChildOf(Component child, Component parent)
  {
//  System.out.println("Checking if " + child.getClass().getName() + " is a child of " + parent.getClass().getName());
    boolean b = false;
    Component current = child;
    if (child.equals(parent))
      b = true;
    else
    {
      while (current.getParent() != null)
      {
//      System.out.println("... current " + current.getClass().getName());
        current = current.getParent();
        if (current.equals(parent))
        {
          b = true;
          break;
        }
      }
    }
    return b;
  }
}
