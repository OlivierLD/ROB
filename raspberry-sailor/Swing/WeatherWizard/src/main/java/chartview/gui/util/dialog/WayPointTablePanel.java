package chartview.gui.util.dialog;

import calc.GeoPoint;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import java.util.ArrayList;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
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


public final class WayPointTablePanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPane = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private JLabel topLabel = new JLabel("");
  private JPanel rightPanel = new JPanel();
  private JButton removeButton = new JButton(WWGnlUtilities.buildMessage("remove"));
  private JButton upButton = new JButton(new ImageIcon(this.getClass().getResource("up.png")));
  private JButton downButton = new JButton(new ImageIcon(this.getClass().getResource("down.png")));

  private String colName = "Way Point(s)"; 
  private List<GeoPoint> aliwp = null;

  private String[] names = { colName };

  private transient TableModel dataModel;

  protected GeoPoint[][] data = new GeoPoint[0][0];
  protected int selectedRow = -1;

  private JTable table;
  private JScrollPane scrollPane;
  private BorderLayout borderLayout2 = new BorderLayout();

  public WayPointTablePanel(List<GeoPoint> aliwp)
  {
    this.aliwp = aliwp;
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
    this.setSize(new Dimension(250, 190));
    this.setPreferredSize(new Dimension(250, 190));
    this.setMinimumSize(new Dimension(250, 190));
    centerPane.setLayout(borderLayout2);
    removeButton.setPreferredSize(new Dimension(75, 22));
    removeButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            remove_actionPerformed(e);
          }
        });
    removeButton.setEnabled(false);
    bottomPanel.setLayout(new GridBagLayout());    
    bottomPanel.add(removeButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 2, 2, 0), 0, 0));

    rightPanel.setLayout(new GridBagLayout());    
    rightPanel.add(upButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 2, 2, 2), 0, 0));
    rightPanel.add(downButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(2, 2, 2, 2), 0, 0));
    upButton.setEnabled(false);
    upButton.setBounds(new Rectangle(2, 2, 2, 2));
    upButton.setMargin(new Insets(1, 1, 1, 1));
    downButton.setEnabled(false);
    downButton.setMargin(new Insets(1, 1, 1, 1));
    upButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            // Move record up
//          System.out.println("Moving row [" + selectedRow + "] up");
            GeoPoint gp = data[selectedRow - 1][0];
            data[selectedRow - 1][0] = data[selectedRow][0];
            data[selectedRow][0] = gp;
            ((AbstractTableModel) dataModel).fireTableDataChanged();
          }
        });
    downButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            // Move record down
//          System.out.println("Moving row [" + selectedRow + "] down");
            GeoPoint gp = data[selectedRow + 1][0];
            data[selectedRow + 1][0] = data[selectedRow][0];
            data[selectedRow][0] = gp;
            ((AbstractTableModel) dataModel).fireTableDataChanged();
          }
        });

    this.add(topLabel, BorderLayout.NORTH);
    this.add(centerPane, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(rightPanel, BorderLayout.EAST);
    initTable();
    WayPointTablePanel.SelectionListener listener = new WayPointTablePanel.SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    
    for (GeoPoint gp : aliwp)
    {
      addLineInTable(gp, data);
    }
  }

  public void setTopLabel(String str)
  {
    topLabel.setText(str);
  }
  
  public List<GeoPoint> getData()
  {
    List<GeoPoint> al = new ArrayList<GeoPoint>(data.length);
    for (int i=0; i<data.length; i++)
      al.add(data[i][0]);
    return al;
  }
  
  private void remove_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
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
      GeoPoint[][] newData = new GeoPoint[l - 1][names.length];
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
          return false; // Never
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          data[row][column] = (GeoPoint)aValue;
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
              System.err.println("WayPointTablePanel:" + ex.getMessage());
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

  private Object[][] addLineInTable(GeoPoint name, GeoPoint[][] d)
  {
    int len = 0;
    if (d != null)
      len = d.length;
    GeoPoint[][] newData = new GeoPoint[len + 1][names.length];
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

  public void setData(ArrayList<GeoPoint> newData)
  {
    GeoPoint[][] d = new GeoPoint[newData.size()][1];
    for (int i=0; i<newData.size(); i++)
      d[i][0] = newData.get(i);
    setData(d);
  }
  
  public void setData(GeoPoint[][] newData)
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
      selectedRow = table.getSelectedRow();
      if (selectedRow < 0)
      {
        WWContext.getInstance().fireHighlightWayPoint(null);
        removeButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
      }
      else
      {
        WWContext.getInstance().fireHighlightWayPoint(data[selectedRow][0]);
        removeButton.setEnabled(true);
        int tableSize = data.length;
        if (tableSize < 2)
        {
          upButton.setEnabled(false);
          downButton.setEnabled(false);
        }
        else
        {
          upButton.setEnabled(selectedRow != 0);
          downButton.setEnabled(selectedRow != (tableSize - 1));
        }
      }
    }
  }
}
