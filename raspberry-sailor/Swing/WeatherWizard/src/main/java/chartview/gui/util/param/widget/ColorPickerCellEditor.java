package chartview.gui.util.param.widget;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;
import java.util.Vector;


public class ColorPickerCellEditor
        extends FieldPlusColorPicker
        implements TableCellEditor {
    protected transient Vector<CellEditorListener> listeners;
    protected transient Color origValue;

    public ColorPickerCellEditor(Color o) {
        super(o);
        this.getTextField().setBackground(o);
        listeners = new Vector<>();
    }

    public void setText(String str) {
        this.getTextField().setText(str);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        if (value == null) {
            this.getTextField().setText("");
            return this;
      //    System.out.println("Value is null, reseting");
      //    value = origValue;
        }
        if (value instanceof String) {
          this.getTextField().setText((String) value);
        } else {
          this.getTextField().setText(value.toString());
        }
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
        origValue = (Color) value;
        return this;
    }

    public Object getCellEditorValue() {
        return this.getValue();
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public void addCellEditorListener(CellEditorListener l) {
        listeners.addElement(l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        listeners.removeElement(l);
    }

    protected void fireEditingCanceled() {
//  this.getTextField().setText(origValue);
        value = origValue;
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = listeners.size(); i >= 0; i--) {
          listeners.elementAt(i).editingCanceled(ce);
        }
    }

    protected void fireEditingStopped() {
        value = origValue;
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = listeners.size() - 1; i >= 0; i--) {
          listeners.elementAt(i).editingStopped(ce);
        }
    }
}
