package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public final class FaxPatternTablePanel
  extends JPanel
{
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel topPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  JPanel centerPane = new JPanel();
  JLabel fileNameLabel = new JLabel();

  static final String FILENAME = WWGnlUtilities.buildMessage("file-name");
  static final String HINT = WWGnlUtilities.buildMessage("hint");

  static final String[] names =
  { FILENAME, HINT };

  TableModel dataModel;

  public static Object[][] data = new Object[0][0];

  JTable table;
  JScrollPane scrollPane;
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel titleLabel = new JLabel();

  public FaxPatternTablePanel()
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
    this.add(topPanel, BorderLayout.NORTH);
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(centerPane, BorderLayout.CENTER);
    initTable();
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
            return getValueAt(0, c).getClass();
          }

          public boolean isCellEditable(int row, int col)
          {
            return (col == 1);
          }

          public void setValueAt(Object aValue, int row, int column)
          {
            data[row][column] = aValue;
          }
        };
    table = new JTable(dataModel)
        {
          /* For the tooltip text */
          public Component prepareRenderer(TableCellRenderer renderer, 
                                           int rowIndex, int vColIndex)
          {
            Component c = 
              super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent)
            {
              JComponent jc = (JComponent) c;
              try
              {
                jc.setToolTipText(getValueAt(rowIndex, 
                                             vColIndex).toString());
              }
              catch (Exception ex)
              {
                System.err.println("ParamPanel:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    TableColumn firstColumn = table.getColumn(FILENAME);
    firstColumn.setCellRenderer(new CustomTableFaxCellRenderer());

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
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }

  @SuppressWarnings("serial")
  public class CustomTableFaxCellRenderer
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
      if (curValue instanceof FaxPatternType)
      {
        if (curValue != null)
        {
          FaxPatternType fpt = (FaxPatternType)curValue;
          g.setColor(fpt.getColor());
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
}
