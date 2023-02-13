package chartview.gui.util.param.widget;

import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class FieldPlusLOVPicker
     extends FieldPlusFinder
  implements TableCellEditor
{
  protected transient Vector<CellEditorListener> listeners;
  protected transient ParamPanel.ContourLinesList origValue;

  public FieldPlusLOVPicker(Object o)
  {
    super(o);
    setText(o.toString());
    listeners = new Vector<CellEditorListener>();
  }

  public void setText(String str)
  {
    this.getTextField().setText(str);
  }

  protected Object invokeEditor()
  {
    ContourLineTablePanel cltp = new ContourLineTablePanel();
    cltp.setRenderedValue(((ParamPanel.ContourLinesList)this.value).getValue());
    int resp = JOptionPane.showConfirmDialog(this, cltp, WWGnlUtilities.buildMessage("contour-lines"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (resp == JOptionPane.OK_OPTION)
    {
      this.value = new ParamPanel.ContourLinesList(cltp.getRenderedValue());
    }
    return this.value;
  }

  protected void finderButton_actionPerformed(ActionEvent e)
  {
    Object o = invokeEditor();
    if (o instanceof ParamPanel.ContourLinesList)
    {
      ParamPanel.ContourLinesList cll = (ParamPanel.ContourLinesList) o;
      if (cll != null)
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
    origValue = (ParamPanel.ContourLinesList)value;
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
