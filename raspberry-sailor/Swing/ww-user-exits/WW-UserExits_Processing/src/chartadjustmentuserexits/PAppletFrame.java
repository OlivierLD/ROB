package chartadjustmentuserexits;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PAppletFrame
  extends JFrame
{
  private BorderLayout borderLayout = new BorderLayout();
  private JPanel bottomPanel = new JPanel();
  private ProcessingGRIBPApplet pga = new ProcessingGRIBPApplet();
  private JComboBox dataTypeComboBox = new JComboBox();

  public PAppletFrame()
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
    this.getContentPane().setLayout(borderLayout);
    this.setSize( new Dimension(500, 570) );
    this.setTitle( "GRIB Rendering" );
    dataTypeComboBox.removeAllItems();
    dataTypeComboBox.addItem("prmsl");
    dataTypeComboBox.addItem("hgt");
    dataTypeComboBox.addItem("htsgw");
    dataTypeComboBox.addItem("tmp");
    dataTypeComboBox.addItem("prate");

    dataTypeComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dataTypeComboBox_actionPerformed(e);
        }
      });
    bottomPanel.add(dataTypeComboBox, null);
    
    this.getContentPane().add(pga, BorderLayout.CENTER);
    this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    
    pga.init();
  }

  private void dataTypeComboBox_actionPerformed(ActionEvent e)
  {
    String newValue = (String)dataTypeComboBox.getSelectedItem();
//  System.out.println("Changed value to [" + newValue + "]");
    pga.setDType(newValue);
  }
  
  public void stopPApplet()
  {
    pga.stop();
  }
}
