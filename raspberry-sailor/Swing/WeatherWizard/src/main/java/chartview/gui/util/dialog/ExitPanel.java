package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ExitPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JCheckBox dontAskCheckBox = new JCheckBox();

  public ExitPanel()
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
    this.setLayout(gridBagLayout1);
    jLabel1.setText(WWGnlUtilities.buildMessage("sure"));
    dontAskCheckBox.setText(WWGnlUtilities.buildMessage("dont-ask-again"));
    this.add(jLabel1, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 10, 0), 0, 0));
    this.add(dontAskCheckBox, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public boolean shutUpNextTime()
  {
    return dontAskCheckBox.isSelected();
  }
}
