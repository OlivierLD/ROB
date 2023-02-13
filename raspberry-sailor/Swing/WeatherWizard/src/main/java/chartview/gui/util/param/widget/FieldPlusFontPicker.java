package chartview.gui.util.param.widget;

import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FieldPlusFontPicker
     extends FieldPlusFinder
  implements TableCellEditor
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient String origValue;

  public FieldPlusFontPicker(Object o)
  {
    super(o);
    if (o instanceof Font)
      setText(FontPanel.fontToString((Font)o));
    else
      setText(o.toString());
    listeners = new Vector<CellEditorListener>();
  }

  public void setText(String str)
  {
    this.getTextField().setText(str);
  }

  protected Object invokeEditor()
  {
    FontPanel fp = new FontPanel((Font)this.value);
    int resp = JOptionPane.showConfirmDialog(this, fp, WWGnlUtilities.buildMessage("default-font-size"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (resp == JOptionPane.OK_OPTION)
      this.value = fp.getChosenFont();
    return this.value;
  }

  protected void finderButton_actionPerformed(ActionEvent e)
  {
    Object o = invokeEditor();
    if (o instanceof String)
    {
      String df = (String) o;
      if (df != null)
      {
        this.value = o;
      }
    }
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
//    System.out.println("Value is null, reseting");
//    value = origValue;
    }
    this.getTextField().setText(value.toString());
    table.setRowSelectionInterval(row, row);
    table.setColumnSelectionInterval(column, column);
    origValue = (String)value;
    return this;
  }

  public Object getCellEditorValue()
  {
    return this.getValue();
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
    value = origValue;
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size(); i >= 0; i--)
      listeners.elementAt(i).editingCanceled(ce);
  }

  protected void fireEditingStopped()
  {
    value = origValue;
    ChangeEvent ce = new ChangeEvent(this);
    for (int i = listeners.size() - 1; i >= 0; i--)
      listeners.elementAt(i).editingStopped(ce);
  }
}
