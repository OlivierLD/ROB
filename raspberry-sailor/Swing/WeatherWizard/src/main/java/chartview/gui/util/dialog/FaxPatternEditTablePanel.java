package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.gui.util.param.widget.ColorAndFilePickerCellEditor;

import chartview.gui.util.param.widget.DirectoryPickerCellEditor;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import chartview.gui.util.TableResizeValue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JButton;
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


public final class FaxPatternEditTablePanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel topPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private JPanel centerPane = new JPanel();
  private JLabel fileNameLabel = new JLabel();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();

  private static final String HINT         = WWGnlUtilities.buildMessage("hint");
  private static final String TRANSPARENT  = WWGnlUtilities.buildMessage("transparent");
  private static final String CHANGE_COLOR = WWGnlUtilities.buildMessage("change-color");
  private static final String DYNAMIC      = WWGnlUtilities.buildMessage("dynamic");
  private static final String FAX_URL      = WWGnlUtilities.buildMessage("url");
  private static final String FAX_DIR      = WWGnlUtilities.buildMessage("directory");
  private static final String FAX_PREFIX   = WWGnlUtilities.buildMessage("prefix");
  private static final String FAX_PATTERN  = WWGnlUtilities.buildMessage("pattern");
  private static final String FAX_EXT      = WWGnlUtilities.buildMessage("extension");
  
  private static final String SCALE    = WWGnlUtilities.buildMessage("scale");
  private static final String ROTATION = WWGnlUtilities.buildMessage("rotation");
  private static final String X_OFFSET = WWGnlUtilities.buildMessage("x_offset");
  private static final String Y_OFFSET = WWGnlUtilities.buildMessage("y_offset");

  private static final String[] names =
  { 
    HINT, 
    TRANSPARENT, 
    DYNAMIC, 
    CHANGE_COLOR, 
    FAX_URL, 
    FAX_DIR, 
    FAX_PREFIX, 
    FAX_PATTERN, 
    FAX_EXT, 
    SCALE, 
    ROTATION, 
    X_OFFSET, 
    Y_OFFSET 
  };

  public final static int HINT_COL         =  0;
  public final static int TRANSPARENT_COL  =  1;
  public final static int DYNAMIC_COL      =  2;
  public final static int CHANGE_COLOR_COL =  3;
  public final static int FAX_URL_COL      =  4;
  public final static int FAX_DIR_COL      =  5;
  public final static int FAX_PREFIX_COL   =  6;
  public final static int FAX_PATTERN_COL  =  7;
  public final static int FAX_EXT_COL      =  8;
  public final static int SCALE_COL        =  9;
  public final static int ROTATION_COL     = 10;
  public final static int X_OFFSET_COL     = 11;
  public final static int Y_OFFSET_COL     = 12;  
  
  private transient TableModel dataModel;

  private transient Object[][] data = new Object[0][0];

  private JTable table;
  private JScrollPane scrollPane;
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel titleLabel = new JLabel();
  
  private int tableResize = JTable.AUTO_RESIZE_OFF;

  public FaxPatternEditTablePanel()
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
    this.setSize(new Dimension(460, 170));
    this.setMinimumSize(new Dimension(460, 170));
    this.setPreferredSize(new Dimension(460, 170));
    bottomPanel.setLayout(gridBagLayout1);
    centerPane.setLayout(borderLayout2);
    fileNameLabel.setText(" ");
    titleLabel.setText(WWGnlUtilities.buildMessage("provide-fax-detail"));
    topPanel.add(fileNameLabel, null);
    topPanel.add(titleLabel, null);
    
    // No button
//  bottomPanel.add(addButton, null);
//  bottomPanel.add(removeButton, null);
//  removeButton.setEnabled(false);
    this.add(topPanel, BorderLayout.NORTH);
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(centerPane, BorderLayout.CENTER);
    initTable();

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
  }

  private void add_actionPerformed(ActionEvent e)
  {
    data = addLineInTable(new FaxType("", Color.black, true, true, 0D, null, null), 
                          Boolean.TRUE,
                          Boolean.FALSE, 
                          Boolean.TRUE, // Dynamic
                          "http://...", 
                          (ParamPanel.DataDirectory)ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX], 
                          "Fax_", 
                          "yyyy_MM_dd_HH_mm_ss_z", 
                          "png", 
                          1.0,
                          0.0f,
                          0,
                          0,
                          data);
  }

  private void remove_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }

  private Object[][] addLineInTable(FaxType hint, 
                                    Boolean tr, 
                                    Boolean dyn, 
                                    Boolean changeColor,
                                    String url, 
                                    ParamPanel.DataDirectory dir, 
                                    String prefix, 
                                    String pattern, 
                                    String ext, 
                                    double scale,
                                    float rotation,
                                    int x_offset,
                                    int y_offset,
                                    Object[][] d)
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
    newData[len][HINT_COL]         = hint;
    newData[len][TRANSPARENT_COL]  = tr;
    newData[len][DYNAMIC_COL]      = dyn;
    newData[len][CHANGE_COLOR_COL] = changeColor;
    newData[len][FAX_URL_COL]      = url;
    newData[len][FAX_DIR_COL]      = dir;
    newData[len][FAX_PREFIX_COL]   = prefix;
    newData[len][FAX_PATTERN_COL]  = pattern;
    newData[len][FAX_EXT_COL]      = ext;
    newData[len][SCALE_COL]        = scale;
    newData[len][ROTATION_COL]     = rotation;
    newData[len][X_OFFSET_COL]     = x_offset;
    newData[len][Y_OFFSET_COL]     = y_offset;
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    return newData;
  }

  private void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0)
      JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("choose-a-row-to-remove"), WWGnlUtilities.buildMessage("removing-entry"), JOptionPane.WARNING_MESSAGE);
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
//          System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
            return getValueAt(0, c).getClass();
          }

          public boolean isCellEditable(int row, int col)
          {
            boolean edit = true;
            if (col > DYNAMIC_COL && col < SCALE_COL)
              edit = ((Boolean)getValueAt(row, DYNAMIC_COL)).booleanValue(); // between Col 2 and 9, dyn faxes only
            return edit;
          }

          public void setValueAt(Object aValue, int row, int column)
          {
            data[row][column] = aValue;
//          System.out.println("Value set at row " + row + ", col " + column);
            if (column == HINT_COL || column == CHANGE_COLOR_COL) // Fax Color or color-change, propagate the "apply"
            {
              Boolean cc = (Boolean)getValueAt(row, CHANGE_COLOR_COL);
              FaxType ft = (FaxType)getValueAt(row, HINT_COL);
              if (column == HINT_COL)
              {
                // Then set value of ColorChange in the table (col 3)
                if (cc.booleanValue() != ft.isChangeColor())
                {
                  setValueAt(Boolean.valueOf(ft.isChangeColor()), row, CHANGE_COLOR_COL);
                  // Repaint the other cell
                  this.fireTableCellUpdated(row, CHANGE_COLOR_COL);
                }
              }
              else if (column == CHANGE_COLOR_COL)
              {
                // Then set value in the fax type (col 0)
                if (cc.booleanValue() != ft.isChangeColor())
                {
                  ft.setChangeColor(cc.booleanValue());
                  setValueAt(ft, row, 0);
                }
              }
            }
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

    TableColumn firstColumn = table.getColumn(HINT);
    firstColumn.setCellEditor(new ParamEditor());
    firstColumn.setCellRenderer(new CustomTableCellRenderer());

    TableColumn fourthColumn = table.getColumn(FAX_DIR);
    fourthColumn.setCellEditor(new ParamEditor());

    table.setAutoResizeMode(tableResize); // Allows horizontal scroll
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  public Object[][] getData()
  {
    return data;
  }

  public void setData(Object[][] newData)
  {
    if (data.length == 0)
    {
      table.getColumn(HINT).setPreferredWidth(200);     
      table.getColumn(TRANSPARENT).setPreferredWidth(50);     
      table.getColumn(DYNAMIC).setPreferredWidth(50);     
      table.getColumn(CHANGE_COLOR).setPreferredWidth(50);     
      table.getColumn(FAX_URL).setPreferredWidth(300);     
      table.getColumn(FAX_DIR).setPreferredWidth(200);     
      table.getColumn(FAX_PREFIX).setPreferredWidth(100);     
      table.getColumn(FAX_PATTERN).setPreferredWidth(200);     
      table.getColumn(FAX_EXT).setPreferredWidth(70);
      
      table.getColumn(SCALE).setPreferredWidth(50);     
      table.getColumn(ROTATION).setPreferredWidth(50);     
      table.getColumn(X_OFFSET).setPreferredWidth(50);     
      table.getColumn(Y_OFFSET).setPreferredWidth(50);     
    }
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
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
        System.out.println("Default Editor");
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
        System.out.println("Null!! : [" + (componentToApply != null? componentToApply.getClass().getName(): " null") + "]");
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

  
  public class CustomTableCellRenderer
    extends JLabel
    implements TableCellRenderer
  {
    Object curValue = null;

    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, int column)
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
          FaxType ft = (FaxType)curValue;
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
