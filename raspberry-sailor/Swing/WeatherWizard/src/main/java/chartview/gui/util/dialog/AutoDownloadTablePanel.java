package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;

import chartview.gui.util.param.ParamPanel;
import chartview.gui.util.param.widget.ColorAndFilePickerCellEditor;

import chartview.gui.util.param.widget.DirectoryPickerCellEditor;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import chartview.gui.util.TableResizeValue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public final class AutoDownloadTablePanel
  extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel topPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  JPanel centerPane = new JPanel();
  JLabel fileNameLabel = new JLabel();

  static final String DOC_NAME = WWGnlUtilities.buildMessage("description");
  static final String DOC_URL = WWGnlUtilities.buildMessage("url-or-request");
  static final String DOC_DIR = WWGnlUtilities.buildMessage("directory");
  static final String DOC_PREFIX = WWGnlUtilities.buildMessage("prefix");
  static final String DOC_PATTERN = WWGnlUtilities.buildMessage("pattern");
  static final String DOC_EXT = WWGnlUtilities.buildMessage("extension");

  static final String[] names =
  { DOC_NAME, DOC_URL, DOC_DIR, DOC_PREFIX, DOC_PATTERN, DOC_EXT };

  TableModel dataModel;

  public static Object[][] data = new Object[0][0];

  JTable table;
  JScrollPane scrollPane;
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel titleLabel = new JLabel();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();

  private int tableResize = JTable.AUTO_RESIZE_OFF;
  private JCheckBox tableAutoResizeCheckBox = new JCheckBox();

  public AutoDownloadTablePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    this.setSize(new Dimension(560, 190));
    this.setPreferredSize(new Dimension(560, 190));
    this.setMinimumSize(new Dimension(560, 190));
    bottomPanel.setLayout(gridBagLayout1);
    centerPane.setLayout(borderLayout2);
    fileNameLabel.setText(" ");
    titleLabel.setText(WWGnlUtilities.buildMessage("faxes-and-gribs"));
    addButton.setText(WWGnlUtilities.buildMessage("add"));
    addButton.setPreferredSize(new Dimension(75, 22));
    addButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            add_actionPerformed(e);
          }
        });
    removeButton.setText(WWGnlUtilities.buildMessage("remove"));
    removeButton.setPreferredSize(new Dimension(75, 22));
    removeButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            remove_actionPerformed(e);
          }
        });
    topPanel.add(fileNameLabel, null);
    topPanel.add(titleLabel, null);
    this.add(topPanel, BorderLayout.NORTH);
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(addButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
    bottomPanel.add(removeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0));
    bottomPanel.add(tableAutoResizeCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    removeButton.setEnabled(false);
    tableAutoResizeCheckBox.setText("Auto-resize Columns"); // LOCALIZE
    tableAutoResizeCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          tableAutoResizeCheckBox_actionPerformed(e);
        }
      });
    this.add(centerPane, BorderLayout.CENTER);
    initTable();
    AutoDownloadTablePanel.SelectionListener listener = new AutoDownloadTablePanel.SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
  }

  private void initTable()
  {
    dataModel = new AbstractTableModel()
        {
          public int getColumnCount()
          {
            return names.length;
          }

          public int getRowCount()
          {
            return data == null? 0: data.length;
          }

          public Object getValueAt(int row, int col)
          {
            return data[row][col];
          }

          public String getColumnName(int column)
          {
            return names[column];
          }

          public Class getColumnClass(int c)
          {
    //      System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
            if (c == 2)
              return ParamPanel.DataDirectory.class;
            else
              return String.class;
          }

          public boolean isCellEditable(int row, int col)
          {
            return true;
          }

          public void setValueAt(Object aValue, int row, int column)
          {
            data[row][column] = aValue;
          }
        };
    table = new JTable(dataModel)
        {
          /* For the tooltip text */

          public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex)
          {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent)
            {
              JComponent jc = (JComponent) c;
              try
              {
                jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
              }
              catch (Exception ex)
              {
                System.err.println("ParamPanel:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    TableColumn firstColumn = table.getColumn(DOC_NAME);
    firstColumn.setPreferredWidth(150);

    TableColumn secondColumn = table.getColumn(DOC_URL);
    secondColumn.setPreferredWidth(300);

    TableColumn thirdColumn = table.getColumn(DOC_DIR);
    thirdColumn.setCellEditor(new ParamEditor());
    thirdColumn.setPreferredWidth(150);
    
    TableColumn fourthColumn = table.getColumn(DOC_PREFIX);
    fourthColumn.setPreferredWidth(100);

    TableColumn fifthColumn = table.getColumn(DOC_PATTERN);    
    fifthColumn.setPreferredWidth(150);

    TableColumn sixthColumn = table.getColumn(DOC_EXT);
    sixthColumn.setPreferredWidth(40);

    table.setAutoResizeMode(tableResize); // Allows horizontal scroll
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);
    
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  private void add_actionPerformed(ActionEvent e)
  {
    addLineInTable("", "", "", "WeatherFax_", "yyyy_MM_dd_HH_mm_ss_z", "png");
  }

  private void remove_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }

  public void setTableResize(TableResizeValue tr)
  {
    if (tr == TableResizeValue.OFF)
      this.tableResize = JTable.AUTO_RESIZE_OFF;
    else
      this.tableResize = JTable.AUTO_RESIZE_ALL_COLUMNS;
    table.setAutoResizeMode(tableResize);
  }

  public TableResizeValue getTableResize()
  {
    if (tableResize == JTable.AUTO_RESIZE_ALL_COLUMNS)
      return TableResizeValue.ON;
    else
      return TableResizeValue.OFF;
  }

  private void tableAutoResizeCheckBox_actionPerformed(ActionEvent e)
  {
    setTableResize(tableAutoResizeCheckBox.isSelected() ? TableResizeValue.ON : TableResizeValue.OFF);
  }

  
  public class CustomTableCellRenderer
    extends JLabel
    implements TableCellRenderer
  {
    Object curValue = null;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
      curValue = value;
      return this;
    }

    public void paintComponent(Graphics g)
    {
      if (curValue instanceof FaxType)
      {
        if (curValue != null)
        {
          FaxType ft = (FaxType) curValue;
          g.setColor(ft.getColor());
        }
      }
      else if (curValue != null)
        WWContext.getInstance().fireLogging("Renderer: value is a " + curValue.getClass().getName() + "\n");
      else
        WWContext.getInstance().fireLogging("Renderer: value is null\n");
      if (curValue != null)
        g.drawString(curValue.toString(), 1, getHeight() - 1);
    }
  }

  
  public class ParamEditor
    extends JComponent
    implements TableCellEditor
  {
    JComponent componentToApply;
    protected transient Vector<CellEditorListener> listeners;
    protected transient Object originalValue;

    public ParamEditor()
    {
      super();
      listeners = new Vector<CellEditorListener>();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
      originalValue = value;
      if (value instanceof FaxType)
      {
        componentToApply = new ColorAndFilePickerCellEditor((FaxType) value, new String[]
              { "gif", "jpg", "jpeg", "tif", "tiff", "png" }, "Faxes");
      }
      else if (value instanceof ParamPanel.DataDirectory)
      {
        ParamPanel.DataDirectory dd = (ParamPanel.DataDirectory)value;
        componentToApply = new DirectoryPickerCellEditor(dd, dd.getDesc());
      }
      else
      {
        WWContext.getInstance().fireLogging("Default Editor\n");
        componentToApply = new JTextField(value != null? value.toString(): "");
      }
      return componentToApply;
    }

    public Object getCellEditorValue()
    {
      //    System.out.println("getCellEditorValue invoked");
      if (componentToApply instanceof JTextField)
      {
        if (originalValue instanceof String)
          return ((JTextField) componentToApply).getText();
        else if (originalValue instanceof Integer)
          return new Integer(((JTextField) componentToApply).getText());
        else if (originalValue instanceof Double)
          return new Double(((JTextField) componentToApply).getText());
        else
          return null;
      }
      else if (componentToApply instanceof ColorAndFilePickerCellEditor)
      {
        return (((ColorAndFilePickerCellEditor) componentToApply).getCellEditorValue());
      }
      else if (componentToApply instanceof DirectoryPickerCellEditor)
      {
        return (((DirectoryPickerCellEditor) componentToApply).getCellEditorValue());
      }
      else
      {
        WWContext.getInstance().fireLogging("Null!! : [" + (componentToApply != null? componentToApply.getClass().getName(): " null") + "]\n");
        return null;
      }
      //    return null;
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
      ChangeEvent ce = new ChangeEvent(this);
      for (int i = listeners.size(); i >= 0; i--)
        listeners.elementAt(i).editingCanceled(ce);
    }

    protected void fireEditingStopped()
    {
      ChangeEvent ce = new ChangeEvent(this);
      for (int i = (listeners.size() - 1); i >= 0; i--)
        listeners.elementAt(i).editingStopped(ce);
    }
  }


  public int[] getSelectRows()
  {
    return table.getSelectedRows();
  }

  public void addLineInTable()
  {
//  Integer atTheEnd = new Integer(data.length + 1);
    addLineInTable("", "", ".", "", "yyyy_MM_dd_HH_mm_ss_z", "");
  }

  private Object[][] addLineInTable(String name, String url, String dir, String prefix, String pattern, String ext)
  {
    return addLineInTable(name, url, dir, prefix, pattern, ext, data);
  }

  private Object[][] addLineInTable(String name, String url, String dir, String prefix, String pattern, String ext, Object[][] d)
  {
    int len = 0;
    if (d != null)
      len = d.length;
    Object[][] newData = new Object[len + 1][names.length];
    for (int i = 0; i < len; i++)
    {
      for (int j = 0; j < names.length; j++)
        newData[i][j] = d[i][j];
    }
    newData[len][0] = name;
    newData[len][1] = url;
    newData[len][2] = new ParamPanel.DataDirectory(WWGnlUtilities.buildMessage("faxes"), dir);
    newData[len][3] = prefix;
    newData[len][4] = pattern;
    newData[len][5] = ext;
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    return newData;
  }

  private void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0)
      JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("choose-a-row-to-remove"), WWGnlUtilities.buildMessage("removing-entry"), 
                                    JOptionPane.WARNING_MESSAGE);
    else
    {
      int l = data.length;
      Object[][] newData = new Object[l - 1][names.length];
      int oldInd, newInd;
      newInd = 0;
      for (oldInd = 0; oldInd < l; oldInd++)
      {
        if (oldInd != selectedRow)
        {
          for (int j = 0; j < names.length; j++)
            newData[newInd][j] = data[oldInd][j];
          newInd++;
        }
      }
      data = newData;
      ((AbstractTableModel) dataModel).fireTableDataChanged();
    }
  }

  public Object[][] getData()
  {
    return data;
  }

  public void setData(Object[][] newData)
  {
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }

  public class SelectionListener
    implements ListSelectionListener
  {
    JTable table;

    SelectionListener(JTable table)
    {
      this.table = table;
    }

    public void valueChanged(ListSelectionEvent e)
    {
      int selectedRow = table.getSelectedRow();
      if (selectedRow < 0)
        removeButton.setEnabled(false);
      else
        removeButton.setEnabled(true);
    }
  }
}
