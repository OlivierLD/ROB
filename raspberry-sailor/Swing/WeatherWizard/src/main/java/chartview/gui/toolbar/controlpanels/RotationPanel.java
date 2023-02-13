package chartview.gui.toolbar.controlpanels;

import chartview.ctx.WWContext;
import chartview.ctx.ApplicationEventListener;

import chartview.gui.util.dialog.FaxType;

import chartview.util.WWGnlUtilities;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class RotationPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel rotationLabel = new JLabel();
  private JTextField rotationTextField = new JTextField();
  private JButton rotateButton = new JButton();

  public RotationPanel()
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
    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
      {
        public String toString()
        {
          return "from RotationPanel.";
        }
        public void activeFaxChanged(FaxType ft) 
        {
          rotationTextField.setText(Double.toString(ft.getRotation()));
        }
      });
    this.setLayout(gridBagLayout1);
    rotationLabel.setText(WWGnlUtilities.buildMessage("rotation-in-degrees"));
    rotationTextField.setPreferredSize(new Dimension(50, 20));
    rotationTextField.setHorizontalAlignment(JTextField.RIGHT);
    rotationTextField.setToolTipText(WWGnlUtilities.buildMessage("positive-clockwise"));
    rotateButton.setText(WWGnlUtilities.buildMessage("rotate"));
    rotateButton.setActionCommand("rotate");
    rotateButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            rotateButton_actionPerformed(e);
          }
        });
    this.add(rotationLabel, 
             new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(rotationTextField, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(rotateButton, 
             new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 3, 0, 0), 0, 0));
  }

  private void rotateButton_actionPerformed(ActionEvent e)
  {
    double angle = 0D;
    try
    { angle = Double.parseDouble(rotationTextField.getText()); }
    catch (Exception nfe) { System.err.println("RotationPanel:" + nfe.getMessage()); }
    for (int i=0; i < WWContext.getInstance().getListeners().size(); i++)
    {
      ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
      l.rotate(angle);
    }    
  }
  
  public void setEnabled(boolean b)
  {
    rotationLabel.setEnabled(b);
    rotationTextField.setEnabled(b);
    rotateButton.setEnabled(b);
  }
}
