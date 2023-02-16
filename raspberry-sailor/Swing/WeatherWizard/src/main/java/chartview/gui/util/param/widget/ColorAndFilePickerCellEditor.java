package chartview.gui.util.param.widget;

import chartview.gui.util.dialog.FaxType;

import chartview.ctx.WWContext;

import java.awt.Component;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;


public class ColorAndFilePickerCellEditor
     extends FieldPlusFileAndColorPicker
  implements TableCellEditor
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient FaxType origValue;

  public ColorAndFilePickerCellEditor(FaxType o, String[] type, String desc)
  {
    super(o, type, desc);
    this.getTextField().setForeground(o.getColor());
    this.setText(o.getValue());
    listeners = new Vector<CellEditorListener>();
  }

  public void setText(String str)
  {
    this.getTextField().setText(str);
  }

  public Component getTableCellEditorComponent(JTable table, 
                                               Object value, 
                                               boolean isSelected, 
                                               int row, 
                                               int column)
  {
    if (value == null)
    {
      this.getTextField().setText("XXXX");
      return this;
    }    
    if (value instanceof String)
      this.getTextField().setText((String)value);
    else if (value instanceof FaxType)
    {
      WWContext.getInstance().fireLogging("Setting text in editor to " + value.toString());
      this.getTextField().setText(((FaxType)value).getValue());
    }
    else
      this.getTextField().setText(value.toString());
      
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);
    origValue = (FaxType) value;
    return this;
  }

  public Object getCellEditorValue()
  {
//    String str = this.getTextField().getText();
//    ((FaxType)super.getValue()).setValue(str);
    this.getTextField().setForeground(((FaxType)super.getValue()).getColor());
    return super.getValue();
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
//  this.getTextField().setText(origValue);
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
