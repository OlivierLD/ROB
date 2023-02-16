package chartview.gui.util.dialog.places;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;

import chartview.util.WWGnlUtilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import oracle.xml.parser.v2.XMLElement;


public class PlacesTablePanel
     extends JPanel 
{
  public final static String PLACES_FILE_NAME = "places.xml";
  
  // Table Columns
  final static String MARK_NAME = WWGnlUtilities.buildMessage("mark-name");
  final static String LATITUDE = WWGnlUtilities.buildMessage("latitude");
  final static String LONGITUDE = WWGnlUtilities.buildMessage("longitude");
  final static String SHOW = WWGnlUtilities.buildMessage("show-mark");

  final static String[] names = {MARK_NAME,
                                 LATITUDE,
                                 LONGITUDE,
                                 SHOW};
  // Table content
  private Object[][] data = new Object[0][names.length];
  
  TableModel dataModel;
  JTable table;

  BorderLayout borderLayout1 = new BorderLayout();
  JPanel centerPanel = new JPanel();
  JPanel bottomPanel = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JScrollPane centerScrollPane = null; // new JScrollPane();
  JPanel topPanel = new JPanel();
  JLabel jLabel1  = new JLabel();
  JButton addButton = new JButton();
  JButton removeButton = new JButton();

  JCheckBox checkBox = new JCheckBox();
  
  public PlacesTablePanel()
  {
    try
    {
      jbInit();
      setValues();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Set values from the XML File
   */
  public void setValues()
  {
    try
    {
      FileInputStream fis = new FileInputStream(PLACES_FILE_NAME);
      DOMParser parser = WWContext.getInstance().getParser();
      synchronized (parser)
      {
        parser.parse(fis);
        XMLDocument doc = parser.getDocument();
        NodeList marks = doc.selectNodes("/places/place");
        for (int i=0; i<marks.getLength(); i++)
        {
          XMLElement place = (XMLElement)marks.item(i);
          String name = place.getAttribute("name");
          String show = place.getAttribute("show");
          if (show.trim().length() == 0)
            show = "true";
          NodeList position = place.selectNodes("./*");
          String latitude = "", longitude = "";
          for (int j=0; j<position.getLength(); j++)
          {
            XMLElement n = (XMLElement)position.item(j);
            if (n.getNodeName().equals("latitude"))
              latitude = n.getAttribute("deg") + " " + n.getAttribute("min") + " " + n.getAttribute("sign");
            else if (n.getNodeName().equals("longitude"))
              longitude = n.getAttribute("deg") + " " + n.getAttribute("min") + " " + n.getAttribute("sign");
          }
          addLineInTable(name, new Latitude(latitude), new Longitude(longitude), new Boolean(show));
        }
        fis.close();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    centerPanel.setLayout(borderLayout2);
    addButton.setText(WWGnlUtilities.buildMessage("add-mark"));
    addButton.setPreferredSize(new Dimension(73, 20));
    addButton.setMinimumSize(new Dimension(73, 20));
    addButton.setMaximumSize(new Dimension(73, 20));
    addButton.setFont(new Font("Dialog", 0, 10));
    addButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addButton_actionPerformed(e);
        }
      });
    removeButton.setText(WWGnlUtilities.buildMessage("remove-mark"));
    removeButton.setPreferredSize(new Dimension(73, 20));
    removeButton.setMinimumSize(new Dimension(73, 20));
    removeButton.setMaximumSize(new Dimension(73, 20));
    removeButton.setFont(new Font("Dialog", 0, 10));
    removeButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          removeButton_actionPerformed(e);
        }
      });
    removeButton.setEnabled(false);
    this.add(centerPanel, BorderLayout.CENTER);
    bottomPanel.add(addButton, null);
    bottomPanel.add(removeButton, null);
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(topPanel, BorderLayout.NORTH);
    initTable();

    SelectionListener listener = new SelectionListener(table);
    table.getSelectionModel().addListSelectionListener(listener);
    table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
}
  
  private void initTable()
  {
    // Init Table
    dataModel = new AbstractTableModel()
    {
      public int getColumnCount()
      { return names.length; }
      public int getRowCount()
      { return data.length; }
      public Object getValueAt(int row, int col)
      { return data[row][col]; }
      public String getColumnName(int column)
      { return names[column]; }
      public Class getColumnClass(int c)
      {
        return getValueAt(0, c).getClass();
      }
      public boolean isCellEditable(int row, int col)
      { 
        return true; // All editable
      }
      public void setValueAt(Object aValue, int row, int column)
      { 
        data[row][column] = aValue; 
        fireTableCellUpdated(row, column);
      }
    };
    // Create Sorter
    TableSorter tableSorter = new TableSorter(dataModel);
    // Create JTable
    table = new JTable(tableSorter);
    tableSorter.addMouseListenerToHeaderInTable(table);
    
    TableColumn checkColumn = table.getColumn(SHOW);
    checkColumn.setCellEditor(new DefaultCellEditor(checkBox));
    
    
    DefaultTableCellRenderer stringRenderer = new DefaultTableCellRenderer()
    {
      public void setValue(Object value)
      {
        super.setValue(value);
      }
    };
    table.setToolTipText(WWGnlUtilities.buildMessage("marks-tooltip"));
    
    centerScrollPane = new JScrollPane(table);
    centerPanel.add(centerScrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  private void addLineInTable(String mn,
                              Latitude lat,
                              Longitude lng)
  {
    addLineInTable(mn, lat, lng, Boolean.FALSE);
  }
  private void addLineInTable(String mn,
                              Latitude lat,
                              Longitude lng,
                              Boolean show)
  {
    int len = data.length;
    Object[][] newData = new Object[len + 1][names.length];
    for (int i=0; i<len; i++)
    {
      for (int j=0; j<names.length; j++)
        newData[i][j] = data[i][j];
    }
    newData[len][0] = mn;
    newData[len][1] = lat;
    newData[len][2] = lng;
    newData[len][3] = show;
    data = newData;
    ((AbstractTableModel)dataModel).fireTableDataChanged();
    table.repaint();
  }

  private void removeCurrentLine()
  {
    int selectedRow = table.getSelectedRow();
//  System.out.println("Row " + selectedRow + " is selected");
    if (selectedRow < 0)
      JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(),
                                    "Please choose a row to remove",
                                    "Removing an entry",
                                    JOptionPane.WARNING_MESSAGE);
    else
    {
      int l = data.length;
      Object[][] newData = new Object[l - 1][names.length];
      int oldInd, newInd;
      newInd = 0;
      for (oldInd=0; oldInd<l; oldInd++)
      {
        if (oldInd != selectedRow)
        {
          for (int j=0; j<names.length; j++)
            newData[newInd][j] = data[oldInd][j];
          newInd++;
        }
      }
      data = newData;
//    sorter.tableChanged(new TableModelEvent(dataModel));
//    sorter.checkModel();
      ((AbstractTableModel)dataModel).fireTableDataChanged();
      table.repaint();
    }
  }

  void addButton_actionPerformed(ActionEvent e)
  {
    try { addLineInTable("", new Latitude("0 0.0 N"), new Longitude("0 0.0 E")); }
    catch (Exception ignore) {}
  }

  void removeButton_actionPerformed(ActionEvent e)
  {
    removeCurrentLine();
  }

  public void saveData()
  {
    XMLDocument doc = new XMLDocument();
    Element elem = doc.createElement("places");
    doc.appendChild(elem);
    for (int i=0; i<data.length; i++)
    {
      Element mark = doc.createElement("place");
      elem.appendChild(mark);
      mark.setAttribute("name", (String)data[i][0]);
      mark.setAttribute("show", ((Boolean)data[i][3]).toString());
      Element latitude = doc.createElement("latitude");
      latitude.setAttribute("deg", Integer.toString(((Latitude)data[i][1]).deg));
      latitude.setAttribute("min", Double.toString(((Latitude)data[i][1]).min));
      latitude.setAttribute("sign", ((Latitude)data[i][1]).nsew);      
      mark.appendChild(latitude);
      
      Element longitude = doc.createElement("longitude");
      longitude.setAttribute("deg", Integer.toString(((Longitude)data[i][2]).deg));
      longitude.setAttribute("min", Double.toString(((Longitude)data[i][2]).min));
      longitude.setAttribute("sign", ((Longitude)data[i][2]).nsew);      
      mark.appendChild(longitude);
    }
    OutputStream os = null;
    try
    {
//    doc.print(System.out);
      os = new FileOutputStream(PLACES_FILE_NAME);
      doc.print(os);
      os.flush();
      os.close();
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, 
                                    ex.toString(), 
                                    "Writing the Marks", 
                                    JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
    }
  }

  public class SelectionListener implements ListSelectionListener
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