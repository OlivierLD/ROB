package chartview.util.grib.panel;

import chartview.ctx.JTableFocusChangeListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


public class OneGRIBTablePanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel topPanel = new JPanel();
  private JPanel bottomPanel = new JPanel();
  private JPanel centerPane = new JPanel();
  
  private float min, max;

  private String[] names = null;
  private transient TableModel dataModel;
  private transient Object[][] data = new Object[0][0];

  private JTable table;
  private JScrollPane scrollPane;
  private BorderLayout borderLayout2 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel titleLabel = new JLabel();
  private JButton generateObjButton = new JButton();

  public OneGRIBTablePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
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
    //  fileNameLabel.setText(" ");
    titleLabel.setText("Title...");
    titleLabel.setFont(new Font("Tahoma", 1, 11));
    generateObjButton.setText("Generate Obj");
    generateObjButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            generateObj_actionPerformed(e);
          }
        });
    topPanel.setLayout(new BorderLayout());
    //  topPanel.add(fileNameLabel, null);
    topPanel.add(titleLabel, BorderLayout.WEST);
    this.add(topPanel, BorderLayout.NORTH);
    bottomPanel.add(generateObjButton,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                           GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(centerPane, BorderLayout.CENTER);
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
            //            System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
            return getValueAt(0, c).getClass();
          }

          public boolean isCellEditable(int row, int col)
          {
            return false;
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
                jc.setToolTipText("Row " + Integer.toString(rowIndex + 1) +
                                  ", Col " +
                                  Integer.toString(vColIndex + 1));
              }
              catch (Exception ex)
              {
                System.err.println("TablePanel:" + ex.getMessage());
              }
            }
            return c;
          }
        };
    scrollPane = new JScrollPane(table);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    centerPane.add(scrollPane, BorderLayout.CENTER);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
  }

  public void setData(Object[][] newData)
  {
    names = new String[newData[0].length];
    for (int i = 0; i < newData[0].length; i++)
      names[i] = Integer.toString(i + 1);
    data = newData;
    initTable();
    ((AbstractTableModel) dataModel).fireTableDataChanged();
  }

  public void setText(String str)
  {
    titleLabel.setText(str);
  }

  private void generateObj_actionPerformed(ActionEvent e)
  {
    // data
    int height = data.length;
    int width  = data[0].length;
    float amplitude = max - min;
    System.out.println("H:" + height + ", W:" + width + ", ampl:" + amplitude);

    float valueCoeff = amplitude / 20F;
    float coordCoeff = amplitude / 2F;
    
    try
    {
      BufferedWriter bw = new BufferedWriter(new FileWriter("test.obj"));
      bw.write("# Vertices\n");
      for (int h = 0; h < height; h++)
      {
        for (int w = 0; w < width; w++)
        {
          bw.write("v " + Float.toString(((w * amplitude) - ((width * amplitude) / 2F) )/ coordCoeff) + " " + Float.toString(((h * amplitude) - ((height - amplitude) / 2F) ) / coordCoeff) + " " + Float.toString((((Float) data[h][w]).floatValue() - amplitude)  / valueCoeff) + "\n");
        }
      }
      bw.write("# Vertices Textures\n");
      for (int h = 0; h < height; h++)
      {
        for (int w = 0; w < width; w++)
        {
          bw.write("vt " + Float.toString(((w * amplitude) - ((width * amplitude) / 2F) )/ coordCoeff) + " " + Float.toString(((h * amplitude) - ((height - amplitude) / 2F) ) / coordCoeff) + " " + Float.toString((((Float) data[h][w]).floatValue() - amplitude)  / valueCoeff) + "\n");
        }
      }
      bw.write("# Faces\n");
      for (int h = 0; h < height-1; h++)
      {
        for (int w = 0; w < width-1; w++)
        {
          bw.write("f " + Integer.toString((h * width) + (w + 1)) + "/" + Integer.toString((h * width) + (w + 1)) + " " +
                          Integer.toString((h * width) + (w + 2)) + "/" + Integer.toString((h * width) + (w + 2)) + " " +
                          Integer.toString(((h + 1) * width) + (w + 2)) + "/" + Integer.toString(((h + 1) * width) + (w + 2)) + " " +
                          Integer.toString(((h + 1) * width) + (w + 1)) + "/" + Integer.toString(((h + 1) * width) + (w + 1)) + "\n");
        }
      }
      bw.write("\n");
      bw.close();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  public void setMin(float min)
  {
    this.min = min;
  }

  public float getMin()
  {
    return min;
  }

  public void setMax(float max)
  {
    this.max = max;
  }

  public float getMax()
  {
    return max;
  }
}
