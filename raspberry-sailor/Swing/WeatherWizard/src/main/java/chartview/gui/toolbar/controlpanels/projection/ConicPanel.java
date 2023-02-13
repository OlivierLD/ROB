package chartview.gui.toolbar.controlpanels.projection;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ConicPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JTextField contactParallelTextField = new JTextField();
  private JComboBox signComboBox = new JComboBox();
  private int projType = 0;

  public ConicPanel()
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
    this.setLayout(gridBagLayout1);
    jLabel1.setText(WWGnlUtilities.buildMessage("contact-parallel"));
    contactParallelTextField.setPreferredSize(new Dimension(40, 20));
    contactParallelTextField.setHorizontalAlignment(JTextField.RIGHT);
    signComboBox.setPreferredSize(new Dimension(40, 20));

    signComboBox.addItem("N");
    signComboBox.addItem("S");
    this.add(jLabel1, 
             new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(contactParallelTextField, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(signComboBox, 
             new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 5, 0, 0), 0, 0));
  }
  
  public double getContactParallel() throws Exception
  {
    double d = Double.parseDouble(contactParallelTextField.getText());
    if (((String)signComboBox.getSelectedItem()).equals("S"))
      d = -d;
    return d;
  }
  
  public void setContactParallel(double d)
  {
    contactParallelTextField.setText(Double.toString(Math.abs(d)));
    if (d < 0D)
      signComboBox.setSelectedItem("S");
    else
      signComboBox.setSelectedItem("N");
  }

  public void setProjType(int projType)
  {
    this.projType = projType;
  }

  public int getProjType()
  {
    return projType;
  }
}
