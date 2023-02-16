package chartview.gui.util.param.widget;

import java.awt.Component;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;


public class BooleanCellEditor
     extends JCheckBox
  implements TableCellEditor
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient Boolean origValue;

  private Boolean value;
  
  public BooleanCellEditor(Boolean b)
  {
    super();
    super.setSelected(b.booleanValue());
    value = b;
    listeners = new Vector<CellEditorListener>();
  }

  public void setText(String str)
  {
  }

  public Component getTableCellEditorComponent(JTable table, Object value, 
                                               boolean isSelected, int row, 
                                               int column)
  {
    return this;
  }

  public Object getCellEditorValue()
  {
    return new Boolean(this.isSelected());
  }

  public boolean isCellEditable(EventObject anEvent)
  {
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent)
  {
    return true;
  }

  public boolean stopCellEditing()
  {
    fireEditingStopped();
    return true;
  }

  public void cancelCellEditing()
  {
    fireEditingCanceled();
  }

  public void addCellEditorListener(CellEditorListener l)
  {
    listeners.addElement(l);
  }

  public void removeCellEditorListener(CellEditorListener l)
  {
    listeners.removeElement(l);
  }

  protected void fireEditingCanceled()
  {
    this.setSelected(origValue);
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size(); i >= 0; i--)
      listeners.elementAt(i).editingCanceled(ce);
  }

  protected void fireEditingStopped()
  {
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size() - 1; i >= 0; i--)
      listeners.elementAt(i).editingStopped(ce);
  }
}
