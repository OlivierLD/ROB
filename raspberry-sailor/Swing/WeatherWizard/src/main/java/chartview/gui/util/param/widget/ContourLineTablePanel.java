package chartview.gui.util.param.widget;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;

import chartview.gui.util.param.ParamData;

import chartview.util.WWGnlUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.StringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public final class ContourLineTablePanel
  extends JPanel
{
  private String renderedValue = "";
  
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel topPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  JPanel centerPane = new JPanel();
  JLabel fileNameLabel = new JLabel();// Not used

  static final String VALUE = WWGnlUtilities.buildMessage("value");
  static final String BOLD = WWGnlUtilities.buildMessage("bold");

  static final String[] names =
  { VALUE, BOLD };

  private transient TableModel dataModel;

  private transient Object[][] data = new Object[0][0];

  JTable table;
  JScrollPane scrollPane;
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();

  public ContourLineTablePanel()
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
    this.setSize(new Dimension(225, 230));
    this.setPreferredSize(new Dimension(225, 230));
    this.setMinimumSize(new Dimension(225, 230));
    bottomPanel.setLayout(gridBagLayout1);
    centerPane.setLayout(borderLayout2);
    fileNameLabel.setText(" "); 
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
    this.add(topPanel, BorderLayout.NORTH);
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(addButton, null);
    bottomPanel.add(removeButton, null);
    removeButton.setEnabled(false);
    this.add(centerPane, BorderLayout.CENTER);
    initTable();
    ContourLineTablePanel.SelectionListener listener = new ContourLineTablePanel.SelectionListener(table);
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
            if (c == 1)
              return Boolean.class;
            else
              return Integer.class;
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
                System.err.println("ContourLineTablePanel:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    TableColumn firstColumn = table.getColumn(VALUE);
    firstColumn.setPreferredWidth(150);

    TableColumn secondColumn = table.getColumn(BOLD);
    secondColumn.setPreferredWidth(30);

//  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allows horizontal scroll
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  private void add_actionPerformed(ActionEvent e)
  {
    addLineInTable(null, Boolean.valueOf(false));
  }

  private void remove_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }

  public int[] getSelectRows()
  {
    return table.getSelectedRows();
  }

  public void addLineInTable()
  {
    //  Integer atTheEnd = new Integer(data.length + 1);
    addLineInTable(null, Boolean.valueOf(false));
  }

  private Object[][] addLineInTable(Integer value, Boolean bold)
  {
    return addLineInTable(value, bold, data);
  }

  private Object[][] addLineInTable(Integer value, Boolean bold, Object[][] d)
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
    newData[len][0] = value;
    newData[len][ParamData.VALUE_INDEX] = bold;
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

  /**
   * 
   * @param renderedValue like "1012, [1016], 1020", between square brackets means bold.
   */
  public void setRenderedValue(String renderedValue)
  {
    this.renderedValue = renderedValue;
    data = null;
//  Pattern brackets = Pattern.compile("^[[^\\]]");
    StringTokenizer strtokContourLines = new StringTokenizer(renderedValue, ",");
    while (strtokContourLines.hasMoreTokens())
    {
      String tok = strtokContourLines.nextToken().trim();
//    System.out.println("Token:{" + tok.trim() + "}");
//    Matcher matcher = brackets.matcher(tok);
//    boolean b = matcher.matches();
      boolean b = tok.startsWith("[") && tok.endsWith("]");
      if (b)
        tok = tok.substring(1, tok.length() - 1);
      addLineInTable(Integer.parseInt(tok), Boolean.valueOf(b));
    }    
  }

  public String getRenderedValue()
  {
    if (data != null)
    {
      renderedValue = "";
      for (int i=0; i<data.length; i++)
      {
        String s = ((Integer)data[i][0]).toString();
        if (((Boolean)data[i][1]).booleanValue())
          s = ("[" + s + "]");
        renderedValue += ((renderedValue.length() > 0?", ":"") + s);
      }
    }
    return renderedValue;
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
  
  // For tests
  public static void main(String... args)
  {
    ContourLineTablePanel cltp = new ContourLineTablePanel();
    cltp.setRenderedValue("1008, 1012, [1016], 1020");    
    cltp.addLineInTable(new Integer(1024), Boolean.valueOf(true));
    String str = cltp.getRenderedValue();
    System.out.println("Rendered:" + str);
  }  
}
