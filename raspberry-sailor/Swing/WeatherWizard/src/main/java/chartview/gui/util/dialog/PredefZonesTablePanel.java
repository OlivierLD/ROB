package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;

import chartview.gui.util.param.widget.ColorAndFilePickerCellEditor;

import chartview.util.WWGnlUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EventObject;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public final class PredefZonesTablePanel
  extends JPanel
{
  private PredefZonesTablePanel instance = this;
  
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel topPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  JPanel centerPane = new JPanel();

  static final String NAME = WWGnlUtilities.buildMessage("name");
  static final String TOP = WWGnlUtilities.buildMessage("top");
  static final String TOP_SIGN = WWGnlUtilities.buildMessage("top-sign");     // 3
  static final String BOTTOM = WWGnlUtilities.buildMessage("bottom");
  static final String BOTTOM_SIGN = WWGnlUtilities.buildMessage("bottom-sign");  // 5
  static final String LEFT = WWGnlUtilities.buildMessage("left");
  static final String LEFT_SIGN = WWGnlUtilities.buildMessage("left-sign");    // 7
  static final String RIGHT = WWGnlUtilities.buildMessage("right");
  static final String RIGHT_SIGN = WWGnlUtilities.buildMessage("right-sign");   // 9

  static final String[] names =
  { NAME, TOP, TOP_SIGN, BOTTOM, BOTTOM_SIGN, LEFT, LEFT_SIGN, RIGHT, RIGHT_SIGN };

  TableModel dataModel;

  protected Object[][] data = new Object[0][0];

  JTable table;
  JScrollPane scrollPane;
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  private JButton addButton = new JButton();
  private JButton removeButton = new JButton();

  public PredefZonesTablePanel()
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
    this.setSize(new Dimension(302, 250));
    bottomPanel.setLayout(gridBagLayout1);
    centerPane.setLayout(borderLayout2);
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
    topPanel.setLayout(new BorderLayout());
    // Top Panel empty for now
    this.add(topPanel, BorderLayout.NORTH);
    this.add(bottomPanel, BorderLayout.SOUTH);
    bottomPanel.add(addButton, null);
    bottomPanel.add(removeButton, null);
    removeButton.setEnabled(false);
    this.add(centerPane, BorderLayout.CENTER);
    initTable();
    PredefZonesTablePanel.SelectionListener listener = new PredefZonesTablePanel.SelectionListener(table);
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
            return getValueAt(0, c).getClass();
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
                System.err.println("PredefZonesTablePanel:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    TableColumn firstColumn = table.getColumn(NAME);
//  firstColumn.setMaxWidth(150);
    firstColumn.setPreferredWidth(150);
    firstColumn.setMinWidth(120);
    // Set a specific #Editor for a special column/line cell
    TableColumn thirdColumn = table.getColumn(TOP_SIGN);
    JComboBox topSign = new JComboBox();
    topSign.addItem("N");
    topSign.addItem("S");
    thirdColumn.setCellEditor(new PoplistEditor(topSign));
    
    TableColumn fifthColumn = table.getColumn(BOTTOM_SIGN);
    JComboBox bottomSign = new JComboBox();
    bottomSign.addItem("N");
    bottomSign.addItem("S");
    fifthColumn.setCellEditor(new PoplistEditor(bottomSign));
    
    TableColumn seventhColumn = table.getColumn(LEFT_SIGN);
    JComboBox leftSign = new JComboBox();
    leftSign.addItem("E");
    leftSign.addItem("W");
    seventhColumn.setCellEditor(new PoplistEditor(leftSign));
    
    TableColumn ninthColumn = table.getColumn(RIGHT_SIGN);
    JComboBox rightSign = new JComboBox();
    rightSign.addItem("E");
    rightSign.addItem("W");
    ninthColumn.setCellEditor(new PoplistEditor(rightSign));
    
    scrollPane = new JScrollPane(table);
    centerPane.add(scrollPane, BorderLayout.CENTER);

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
              // TASK Limit to one row?
              TablePopup popup = new TablePopup(idx[0], instance);
              popup.show(table, e.getX(), e.getY());
            }
          }
        }
      }); 
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  private void add_actionPerformed(ActionEvent e)
  {
    addLineInTable("", 0, "N", 0, "N", 0, "E", 0, "E");
  }

  private void remove_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }

  public void setData(XMLDocument doc)
  {
    try
    {
      NodeList children = doc.selectNodes("/pre-def-zones/*");
      for (int i=0; i<children.getLength(); i++)
      {
        XMLElement node = (XMLElement)children.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
          if ("item".equals(node.getNodeName()))
          {
            String name = WWGnlUtilities.unescapeXML(node.getAttribute("name"));
            int top    = Integer.parseInt(node.getAttribute("top"));
            int bottom = Integer.parseInt(node.getAttribute("bottom"));
            int left   = Integer.parseInt(node.getAttribute("left"));
            int right  = Integer.parseInt(node.getAttribute("right"));
            addLineInTable(name, Math.abs(top),    (top<0?"S":"N"),
                                 Math.abs(bottom), (bottom<0?"S":"N"),
                                 Math.abs(left),   (left<0?"W":"E"),
                                 Math.abs(right),  (right<0?"W":"E"));
          }
          else          
            addLineInTable("Separator", 0, " ", 0, " ", 0, " ", 0, " ");
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public XMLDocument getData()
  {
    XMLDocument newDoc = new XMLDocument();
    XMLElement root = (XMLElement)newDoc.createElement("pre-def-zones");
    newDoc.appendChild(root);
    for (int i=0; i<data.length; i++)
    {
      XMLElement elmt = null;
      if ("Separator".equalsIgnoreCase((String)data[i][0]))
        elmt = (XMLElement)newDoc.createElement("separator");
      else
      {
        elmt = (XMLElement)newDoc.createElement("item");
        elmt.setAttribute("name", WWGnlUtilities.escapeXML((String)data[i][0]));
        elmt.setAttribute("top", (("N".equals((String)data[i][2]))?"":"-") + ((Integer)data[i][1]).toString());
        elmt.setAttribute("bottom", (("N".equals((String)data[i][4]))?"":"-") + ((Integer)data[i][3]).toString());
        elmt.setAttribute("left", (("E".equals((String)data[i][6]))?"":"-") + ((Integer)data[i][5]).toString());
        elmt.setAttribute("right", (("E".equals((String)data[i][8]))?"":"-") + ((Integer)data[i][7]).toString());
      }
      root.appendChild(elmt);
    }
    return newDoc; 
  }

  @SuppressWarnings("serial")
  public class CustomTableCellRenderer
    extends JLabel
    implements TableCellRenderer
  {
    Object curValue = null;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, 
                                                   int column)
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

  @SuppressWarnings("serial")
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
      if (column == 1 && value instanceof FaxType)
      {
        componentToApply = new ColorAndFilePickerCellEditor((FaxType) value, new String[]
              { "gif", "jpg", "jpeg", "tif", "tiff", "png" }, "Faxes");
      }
      else
      {
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
        return /*(FaxType)*/(((ColorAndFilePickerCellEditor) componentToApply).getCellEditorValue());
      }
      else
      {
        WWContext.getInstance().fireLogging("Null!!" + 
                                          (componentToApply != null? componentToApply.getClass().getName(): " null") + "\n");
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
      for (int i = listeners.size(); i >= 0; i--) /*(CellEditorListener)*/
          (listeners.elementAt(i)).editingCanceled(ce);
    }

    protected void fireEditingStopped()
    {
      ChangeEvent ce = new ChangeEvent(this);
      for (int i = (listeners.size() - 1); i >= 0; i--) /*(CellEditorListener)*/
          (listeners.elementAt(i)).editingStopped(ce);
    }
  }

  public int[] getSelectRows()
  {
    return table.getSelectedRows();
  }

  private Object[][] addLineInTable(String name, int top, String topSign, 
                                                 int bottom, String bottomSign,
                                                 int left, String leftSign,
                                                 int right, String rightSign)
  {
    return addLineInTable(name, top, topSign, bottom, bottomSign, left, leftSign, right, rightSign, data);
  }

  private Object[][] addLineInTable(String name, int top, String topSign, 
                                                 int bottom, String bottomSign,
                                                 int left, String leftSign,
                                                 int right, String rightSign, 
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
    newData[len][0] = name;
    newData[len][1] = top;
    newData[len][2] = topSign;
    newData[len][3] = bottom;
    newData[len][4] = bottomSign;
    newData[len][5] = left;
    newData[len][6] = leftSign;
    newData[len][7] = right;
    newData[len][8] = rightSign;
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
    return newData;
  }

  protected void refreshTable()
  {
    ((AbstractTableModel) dataModel).fireTableDataChanged();    
  }
  
  private void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0)
      JOptionPane.showMessageDialog(instance, WWGnlUtilities.buildMessage("choose-a-row-to-remove"), WWGnlUtilities.buildMessage("removing-entry"), JOptionPane.WARNING_MESSAGE);
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

  public void setData(Object[][] newData)
  {
    data = newData;
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }

  @SuppressWarnings("serial")
  class TablePopup extends JPopupMenu
                implements ActionListener,
                           PopupMenuListener
  {
    private JMenuItem moveUp;
    private JMenuItem moveDown;
    private int idx;

    private final static String MOVE_UP   = "Move Up";
    private final static String MOVE_DOWN = "Move Down";

    private PredefZonesTablePanel parent;

    public TablePopup(int rowId, PredefZonesTablePanel caller)
    {
      super();
      idx = rowId;
      parent = caller;
      this.add(moveUp = new JMenuItem(MOVE_UP));
      moveUp.addActionListener(this);
      this.add(moveDown = new JMenuItem(MOVE_DOWN));
      moveDown.addActionListener(this);
    }

    public void show(JTable table, int x, int y)
    {
      moveUp.setEnabled(idx > 0);
      moveDown.setEnabled(idx < (parent.data.length - 1));
      super.show(table, x, y);
    }
    
    public void actionPerformed(ActionEvent event)
    {
      if (event.getActionCommand().equals(MOVE_UP))
      {
        Object data[] = parent.data[idx];
        Object swap[] = parent.data[idx - 1];
        parent.data[idx - 1] = data;
        parent.data[idx] = swap;
        parent.refreshTable();
      }
      else if (event.getActionCommand().equals(MOVE_DOWN))
      {
        Object data[] = parent.data[idx];
        Object swap[] = parent.data[idx + 1];
        parent.data[idx + 1] = data;
        parent.data[idx] = swap;
        parent.refreshTable();
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
