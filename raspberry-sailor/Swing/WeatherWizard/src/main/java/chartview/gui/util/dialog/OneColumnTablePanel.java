package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


public class OneColumnTablePanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPane = new JPanel();

  private String colName = "File"; 
  private boolean editable = true;
  private boolean withDelete = false;

  private String[] names = { colName };

  private TableModel dataModel;

  protected Object[][] data = new Object[0][0];

  private JTable table;
  private JScrollPane scrollPane;
  private BorderLayout borderLayout2 = new BorderLayout();

  public OneColumnTablePanel(String colName)
  {
    this(colName, true, false);
  }
  
  public OneColumnTablePanel(String colName, boolean editable, boolean withDelete)
  {
    this.colName = colName;
    this.editable = editable;
    this.withDelete = withDelete;
    
    this.names[0] = this.colName;
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
    centerPane.setLayout(borderLayout2);
    this.add(centerPane, BorderLayout.CENTER);
    initTable();
    OneColumnTablePanel.SelectionListener listener = new OneColumnTablePanel.SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    table.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          int mask = e.getModifiers();
          // Right-click only (Actually: no left-click)
          if ((mask & MouseEvent.BUTTON2_MASK) != 0 || (mask & MouseEvent.BUTTON3_MASK) != 0) 
          {
            // get selected row ID
            int[] idx = table.getSelectedRows();
            if (idx.length > 0) // Row must be selected
            {
              String[] indexes = new String[idx.length];
              for (int i=0; i<idx.length; i++)
                indexes[i] = (String)data[idx[i]][0];
              TablePopup popup = new TablePopup(indexes);
              popup.show(table, e.getX(), e.getY());
            }
          }
        }
      }); 
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
          return data == null ? 0 : data.length;
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
          return String.class;
        }

        public boolean isCellEditable(int row, int col)
        {
          return editable;
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
//    TableColumn firstColumn = table.getColumn(COL_NAME);
//    firstColumn.setPreferredWidth(200);
//    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allows horizontal scroll
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  public int[] getSelectRows()
  {
    return table.getSelectedRows();
  }
  
  public List<Object> getSelectedData()
  {
    List<Object> al = new ArrayList<Object>();
    int[] selectedRow = getSelectRows();
    for (int i : selectedRow)
      al.add(data[i][0]);
    return al;
  }

  private Object[][] addLineInTable(String name, Object[][] d)
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
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    return newData;
  }

  public Object[][] getData()
  {
    return data;
  }

  public void setData(List<String> newData)
  {
    Object[][] d = new Object[newData.size()][1];
    for (int i=0; i<newData.size(); i++)
      d[i][0] = newData.get(i);
    setData(d);
  }
  
  public void setData(Object[][] newData)
  {
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }
  
  public void refresh()
  {
    // To be overridden
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
//      int selectedRow = table.getSelectedRow();
//      if (selectedRow < 0)
//        removeButton.setEnabled(false);
//      else
//        removeButton.setEnabled(true);
    }
  }

  
  class TablePopup extends JPopupMenu
                implements ActionListener,
                           PopupMenuListener
  {
    private String[] row2show;

    private JMenuItem gotoDirectory;
    private JMenuItem deleteSelected;

    private final String GO_TO = WWGnlUtilities.buildMessage("go-to-directory");
    private final String DELETE = WWGnlUtilities.buildMessage("delete-selected");

    public TablePopup(String[] rowId)
    {
      super();
      row2show = rowId;
      this.add(gotoDirectory = new JMenuItem(GO_TO));
      gotoDirectory.addActionListener(this);
      if (withDelete)
      {
        this.add(deleteSelected = new JMenuItem(DELETE));
        deleteSelected.addActionListener(this);
      }
    }

    public void actionPerformed(ActionEvent event)
    {
      if (event.getActionCommand().equals(GO_TO))
      {
//      System.out.println("Popup Action detected on row " + row2show[0]);
        String directory = row2show[0].substring(0, row2show[0].lastIndexOf(File.separator));
        try { Utilities.showFileSystem(directory); } catch (Exception ex) { ex.printStackTrace(); }
      }
      if (event.getActionCommand().equals(DELETE))
      {
        if (row2show.length > 0)
        {
          // Confirmation
          int resp = JOptionPane.showConfirmDialog(this, "Delete " + row2show.length + " Document(s) ?", "Delete", JOptionPane.YES_NO_OPTION);
          if (resp == JOptionPane.YES_OPTION)
          {
            for (String s : row2show)
            {
              System.out.println("Deleting " + s);
              File f = new File(s);
              f.delete();
            }
          }
          refresh();
        }
      }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
    }

    public void popupMenuCanceled(PopupMenuEvent e)
    {
    }
  }
}
