package chartview.gui.util.param.widget;

import chartview.gui.util.param.ParamPanel;

import chartview.util.RelativePath;

import java.awt.Component;

import java.io.File;

import javax.swing.JTable;
import java.util.EventObject;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.util.Vector;
import javax.swing.event.ChangeEvent;

@SuppressWarnings("serial")
public class FilePickerCellEditor 
     extends FieldPlusFilePicker 
  implements TableCellEditor 
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient ParamPanel.DataFile origValue;

  public FilePickerCellEditor(ParamPanel.DataFile o, String[] stra, String s)
  {
    super(o, stra, s);
    this.setText(o.toString());
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
      this.getTextField().setText("");
      return this;
    }
    if (value instanceof String)
      this.getTextField().setText((String)value);
    else
      this.getTextField().setText(value.toString());
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);
    origValue = (ParamPanel.DataFile)value;
    return this;
  }

  public Object getCellEditorValue()
  {
    String str = this.getTextField().getText().trim();
    if (str.length() > 0)
      str = RelativePath.getRelativePath(System.getProperty("user.dir"), str).replace(File.separatorChar, '/');
    ((ParamPanel.DataFile)super.getValue()).setValue(str);
    return (ParamPanel.DataFile)super.getValue();
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
    for (int i=listeners.size(); i>=0; i--)
      listeners.elementAt(i).editingCanceled(ce);
  }

  protected void fireEditingStopped()
  {
    ChangeEvent ce = new ChangeEvent(this);
    for (int i=listeners.size() - 1; i>=0; i--)
      listeners.elementAt(i).editingStopped(ce);
  }
}