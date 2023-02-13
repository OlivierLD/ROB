package chartview.gui.util.dialog.places;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class PlacesTableMap 
     extends AbstractTableModel 
  implements TableModelListener
{
  public PlacesTableMap()
  {
  }

  protected TableModel dataModel;

  public void setModel(TableModel tm)
  {
    this.dataModel = tm;
    dataModel.addTableModelListener(this);
  }
  public TableModel getModel()
  {
    return this.dataModel;
  }
  public int getColumnCount()
  { return (dataModel==null)?0:dataModel.getColumnCount(); }
  public int getRowCount()
  { return (dataModel==null)?0:dataModel.getRowCount(); }
  public Object getValueAt(int row, int col)
  { return (dataModel==null)?null:dataModel.getValueAt(row, col); }
  public void setValueAt(Object aValue, int row, int column)
  { dataModel.setValueAt(aValue, row, column); }
  public String getColumnName(int column)
  { return dataModel.getColumnName(column); }
  public Class getColumnClass(int c)
  { return dataModel.getColumnClass(c); }
  public boolean isCellEditable(int row, int col)
  { return dataModel.isCellEditable(row, col); }

  public void tableChanged(TableModelEvent e)
  {
    fireTableChanged(e);
  }  
}