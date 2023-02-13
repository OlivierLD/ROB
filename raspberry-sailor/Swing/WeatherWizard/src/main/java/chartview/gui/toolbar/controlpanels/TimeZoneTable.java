package chartview.gui.toolbar.controlpanels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class TimeZoneTable
  extends JPanel
{
  @SuppressWarnings("compatibility:8975770705051309092")
  public final static long serialVersionUID = 1L;

  private transient String[] timeZoneData;

  // Table Columns
  final static String TIME_ZONE = "Time Zone";

  final static String[] names =
  { TIME_ZONE };
  // Table content
  private transient Object[][] data = new Object[0][names.length];

  private transient TableModel dataModel;
  private JTable table;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel centerPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JScrollPane centerScrollPane = null; // new JScrollPane();
  private JPanel topPanel = new JPanel();
  private JLabel filterLabel = new JLabel();
  private JTextField filterTextField = new JTextField();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel statusLabel = new JLabel();
  private BorderLayout borderLayout3 = new BorderLayout();

  public TimeZoneTable(String[] tz)
  {
    try
    {
      jbInit();
      if (tz != null)
      {
        setTimeZoneData(tz);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setValues()
  {
    int nbl = 0;
    try
    {
      if (timeZoneData != null)
      {
        for (int i=0; i<timeZoneData.length; i++)
        {
          addLineInTable(timeZoneData[i]);
          nbl++;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    this.setStatusLabel(Integer.toString(nbl) + " time zone(s)");
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    centerPanel.setLayout(borderLayout2);
    bottomPanel.setLayout(borderLayout3);
    topPanel.setLayout(gridBagLayout1);
    filterLabel.setText("Filter:");
    statusLabel.setText("Ready");
    this.add(centerPanel, BorderLayout.CENTER);
    bottomPanel.add(statusLabel, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    topPanel.add(filterLabel,
                 new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                                                                                                                           0,
                                                                                                                           0,
                                                                                                                           0),
                                        0, 0));
    topPanel.add(filterTextField,
                 new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    filterTextField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) // [Return] in the field
        {
          setSelection();
        }
      });
    filterTextField.getDocument().addDocumentListener(new DocumentListener()
      {
        public void insertUpdate(DocumentEvent e)
        {
          setSelection();
        }

        public void removeUpdate(DocumentEvent e)
        {
          setSelection();
        }

        public void changedUpdate(DocumentEvent e)
        {
          setSelection();
        }
      });
    filterTextField.setToolTipText("Enter selection criteria, like part of the name...");
    this.add(topPanel, BorderLayout.NORTH);
    initTable();

    SelectionListener listener = new SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

    this.filterLabel.setEnabled(this.timeZoneData != null);
    this.filterTextField.setEnabled(this.timeZoneData != null);
  }

  private void initTable()
  {
    // Init Table
    dataModel = new AbstractTableModel()
      {
        @SuppressWarnings("compatibility:9070027359489543434")
        public final static long serialVersionUID = 1L;

        public int getColumnCount()
        {
          return names.length;
        }

        public int getRowCount()
        {
          return data.length;
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
          return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col)
        {
          return false;
        }

        public void setValueAt(Object aValue, int row, int column)
        {
          data[row][column] = aValue;
          fireTableCellUpdated(row, column);
        }
      };
    // Create JTable
    table = new JTable(dataModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
    //  KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  private void addLineInTable(String mn)
  {
    int len = data.length;
    Object[][] newData = new Object[len + 1][names.length];
    for (int i = 0; i < len; i++)
    {
      for (int j = 0; j < names.length; j++)
        newData[i][j] = data[i][j];
    }
    newData[len][0] = mn;
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    table.repaint();
  }

  public String getSelectedTimeZoneData()
  {
    String tz = null;
    int sr = table.getSelectedRow();
    if (sr >= 0)
    {
      for (int i=0; i<timeZoneData.length; i++)
      {
        if (timeZoneData[i].equals(data[sr][0]))
        {
          tz = timeZoneData[i];
          break;
        }
      }
    }
    return tz;
  }

  private void setSelection()
  {
    String fieldContent = filterTextField.getText();

    String patternStr = ".*" + fieldContent + ".*";
    Pattern p = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);

    data = new Object[0][names.length];
    int nbl = 0;
    for (int i=0; i<timeZoneData.length; i++)
    {
      Matcher m = p.matcher(timeZoneData[i]);
      if (m.matches())
      {
        // Add in table
        nbl++;
        addLineInTable(timeZoneData[i]);
      }
    }
    this.setStatusLabel(Integer.toString(nbl) + " zone(s)");
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    table.repaint();
  }

  public void setTimeZoneData(String[] tz)
  {
    this.timeZoneData = tz;
    this.filterLabel.setEnabled(this.timeZoneData != null);
    this.filterTextField.setEnabled(this.timeZoneData != null);
    setValues();
  }

  public String[] getTimeZoneData()
  {
    return timeZoneData;
  }

  public class SelectionListener
    implements ListSelectionListener
  {
    JTable table;

    SelectionListener(JTable table)
    {
      this.table = table;
    }

    public void valueChanged(ListSelectionEvent lse)
    {
      int selectedRow = table.getSelectedRow();
      if (selectedRow < 0)
        ;
      else
        ;
    }
  }

  public void setStatusLabel(String s)
  {
    this.statusLabel.setText(s);
  }
}
