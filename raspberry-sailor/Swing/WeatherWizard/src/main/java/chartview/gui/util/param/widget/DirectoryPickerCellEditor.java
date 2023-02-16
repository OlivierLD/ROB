package chartview.gui.util.param.widget;

import chartview.gui.util.param.ParamPanel;

import chartview.util.RelativePath;

import java.awt.Component;

import java.io.File;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;


public class DirectoryPickerCellEditor
  extends FieldPlusDirectoryPicker
  implements TableCellEditor
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient ParamPanel.DataDirectory origValue;

  public DirectoryPickerCellEditor(ParamPanel.DataDirectory o, String desc)
  {
    super(o, desc);
    this.setText(o.toString());
    listeners = new Vector<CellEditorListener>();
  }

  public void setText(String str)
  {
    this.getTextField().setText(str);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, 
                                               boolean isSelected, int row, 
                                               int column)
  {
    if (value == null)
    {
      this.getTextField().setText("");
      return this;
    }
    if (value instanceof String)
      this.getTextField().setText((String) value);
    else
      this.getTextField().setText(value.toString());
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);
    origValue = (ParamPanel.DataDirectory)value;
    return this;
  }

  public Object getCellEditorValue()
  {
    String str = this.getTextField().getText();
    str = RelativePath.getRelativePath(System.getProperty("user.dir"), str).replace(File.separatorChar, '/');
    ((ParamPanel.DataDirectory)super.getValue()).setValue(str);
    return (ParamPanel.DataDirectory)super.getValue();
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
    this.getTextField().setText(origValue.toString());
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
