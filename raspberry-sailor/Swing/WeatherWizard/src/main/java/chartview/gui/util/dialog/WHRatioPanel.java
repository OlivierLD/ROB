package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class WHRatioPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JTextField whRatioTextField = new JTextField();

  public WHRatioPanel()
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
    jLabel1.setText(WWGnlUtilities.buildMessage("fax-hw-ratio"));
    whRatioTextField.setHorizontalAlignment(JTextField.RIGHT);
    whRatioTextField.setText("1.0");
    this.add(jLabel1, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(whRatioTextField, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public double getRatio() throws Exception
  {
    return (Double.parseDouble(whRatioTextField.getText()));
  }
  
  public void setRatio(double d)
  {
    whRatioTextField.setText(Double.toString(d));
  }
}
