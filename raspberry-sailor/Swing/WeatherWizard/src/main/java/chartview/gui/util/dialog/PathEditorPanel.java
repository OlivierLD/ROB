package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;
import chartview.gui.util.param.ParamPanel;
import chartview.gui.util.param.widget.DirectoryPickerCellEditor;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Vector;


public final class PathEditorPanel
        extends JPanel {
    final BorderLayout borderLayout1 = new BorderLayout();
    final JPanel topPanel = new JPanel();
    final JPanel bottomPanel = new JPanel();
    final JPanel centerPane = new JPanel();

    static final String VALUE = WWGnlUtilities.buildMessage("directory");

    static final String[] names = {VALUE};

    TableModel dataModel;

    public static Object[][] data = new Object[0][0];

    JTable table;
    JScrollPane scrollPane;
    BorderLayout borderLayout2 = new BorderLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel titleLabel = new JLabel();
    private final JButton addButton = new JButton();
    private final JButton removeButton = new JButton();

    public PathEditorPanel(String title) {
        titleLabel.setText(title);
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(302, 250));
        bottomPanel.setLayout(gridBagLayout1);
        centerPane.setLayout(borderLayout2);
        //  fileNameLabel.setText(" ");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
        addButton.setText(WWGnlUtilities.buildMessage("add"));
        addButton.setPreferredSize(new Dimension(75, 22));
        addButton.addActionListener(this::add_actionPerformed);
        removeButton.setText(WWGnlUtilities.buildMessage("remove"));
        removeButton.setPreferredSize(new Dimension(75, 22));
        removeButton.addActionListener(this::remove_actionPerformed);
        topPanel.setLayout(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(addButton, null);
        bottomPanel.add(removeButton, null);
        removeButton.setEnabled(false);
        this.add(centerPane, BorderLayout.CENTER);
        initTable();
        SelectionListener listener = new SelectionListener(table);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    private void initTable() {
        dataModel = new AbstractTableModel() {
            public int getColumnCount() {
                return names.length;
            }

            public int getRowCount() {
                return data == null ? 0 : data.length;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public String getColumnName(int column) {
                return names[column];
            }

            public Class<?> getColumnClass(int c) {
                //      System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
                return getValueAt(0, c).getClass();
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
            }
        };
        table = new JTable(dataModel) {
            /* For the tooltip text */
            public Component prepareRenderer(TableCellRenderer renderer,
                                             int rowIndex, int vColIndex) {
                Component c =
                        super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    try {
                        jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
                    } catch (Exception ex) {
                        System.err.println("ParamPanel:" + ex.getMessage());
                    }
                }
                return c;
            }
        };
        // Set a specific #Editor for a special column/line cell
        TableColumn firstColumn = table.getColumn(VALUE);
        firstColumn.setCellEditor(new ParamEditor());
        firstColumn.setCellRenderer(new CustomTableCellRenderer());

        scrollPane = new JScrollPane(table);
        centerPane.add(scrollPane, BorderLayout.CENTER);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
    }

    private void add_actionPerformed(ActionEvent e) {
        addLineInTable(new ParamPanel.DataDirectory(WWGnlUtilities.buildMessage("directory"), "."));
    }

    private void remove_actionPerformed(ActionEvent e) {
        removeCurrentLine();
    }


    public static class CustomTableCellRenderer
            extends JLabel
            implements TableCellRenderer {
        Object curValue = null;

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            curValue = value;
            return this;
        }

        public void paintComponent(Graphics g) {
            if (curValue instanceof FaxType) {
                if (curValue != null) {
                    FaxType ft = (FaxType) curValue;
                    g.setColor(ft.getColor());
                }
            } else if (curValue != null) {
                WWContext.getInstance().fireLogging("Renderer: value is a " + curValue.getClass().getName() + "\n");
            } else {
                WWContext.getInstance().fireLogging("Renderer: value is null\n");
            }
            if (curValue != null) {
                g.drawString(curValue.toString(), 1, getHeight() - 1);
            }
        }
    }


    public static class ParamEditor
            extends JComponent
            implements TableCellEditor {
        JComponent componentToApply;
        protected transient Vector<CellEditorListener> listeners;
        protected transient Object originalValue;

        public ParamEditor() {
            super();
            listeners = new Vector<>();
        }

        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            originalValue = value;
            if (column == 0 && value instanceof ParamPanel.DataDirectory) {
                componentToApply = new DirectoryPickerCellEditor((ParamPanel.DataDirectory) value, WWGnlUtilities.buildMessage("directory"));
            } else {
                componentToApply = new JTextField(value != null ? value.toString() : "");
            }
            return componentToApply;
        }

        public Object getCellEditorValue() {
            //    System.out.println("getCellEditorValue invoked");
            if (componentToApply instanceof JTextField) {
                if (originalValue instanceof String) {
                    return ((JTextField) componentToApply).getText();
                } else if (originalValue instanceof Integer) {
                    return Integer.valueOf(((JTextField) componentToApply).getText());
                } else if (originalValue instanceof Double) {
                    return Double.valueOf(((JTextField) componentToApply).getText());
                } else {
                    return null;
                }
            } else if (componentToApply instanceof DirectoryPickerCellEditor) {
                return ((DirectoryPickerCellEditor) componentToApply).getCellEditorValue();
            } else {
                WWContext.getInstance().fireLogging("Null!!" +
                        (componentToApply != null ? componentToApply.getClass().getName() :
                                " null") + "\n");
                return null;
            }
            //    return null;
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
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = listeners.size(); i >= 0; i--) {
                listeners.elementAt(i).editingCanceled(ce);
            }
        }

        protected void fireEditingStopped() {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = (listeners.size() - 1); i >= 0; i--) {
                listeners.elementAt(i).editingStopped(ce);
            }
        }
    }

    public int[] getSelectRows() {
        return table.getSelectedRows();
    }

    public void addLineInTable(ParamPanel.DataDirectory dd) {
        addLineInTable(dd, data);
    }

    private Object[][] addLineInTable(ParamPanel.DataDirectory dd, Object[][] d) {
        int len = 0;
        if (d != null) {
            len = d.length;
        }
        Object[][] newData = new Object[len + 1][names.length];
        for (int i = 0; i < len; i++) {
            System.arraycopy(d[i], 0, newData[i], 0, names.length);
        }
        newData[len][0] = dd;
        data = newData;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        return newData;
    }

    private void removeCurrentLine() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("choose-a-row-to-remove"), WWGnlUtilities.buildMessage("removing-entry"), JOptionPane.WARNING_MESSAGE);
        } else {
            int l = data.length;
            Object[][] newData = new Object[l - 1][names.length];
            int oldInd, newInd;
            newInd = 0;
            for (oldInd = 0; oldInd < l; oldInd++) {
                if (oldInd != selectedRow) {
                    System.arraycopy(data[oldInd], 0, newData[newInd], 0, names.length);
                    newInd++;
                }
            }
            data = newData;
            ((AbstractTableModel) dataModel).fireTableDataChanged();
        }
    }

    public Object[][] getData() {
        return data;
    }

    public void setData(Object[][] newData) {
        data = newData;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
    }

    public void setData(String str) {
        ParamPanel.DataPath dp = new ParamPanel.DataPath(str);
        Object[][] newData = new Object[dp.getPath().length][1];
        for (int i = 0; i < newData.length; i++) {
            newData[i][0] = dp.getPath()[i];
        }
        data = newData;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
    }

    public class SelectionListener implements ListSelectionListener {
        JTable table;

        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            int selectedRow = table.getSelectedRow();
            removeButton.setEnabled(selectedRow >= 0);
        }
    }
}
