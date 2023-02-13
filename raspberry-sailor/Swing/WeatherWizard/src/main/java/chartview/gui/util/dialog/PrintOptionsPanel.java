package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class PrintOptionsPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JTextField titleTextField = new JTextField();
  private JCheckBox fullScreenCheckBox = new JCheckBox();
  private JLabel titleLabel = new JLabel();

  public PrintOptionsPanel()
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
    titleTextField.setPreferredSize(new Dimension(200, 20));
    fullScreenCheckBox.setText(WWGnlUtilities.buildMessage("print-all-composites"));
    fullScreenCheckBox.setToolTipText(WWGnlUtilities.buildMessage("all-vs-visible"));
    fullScreenCheckBox.setSelected(true);
    titleLabel.setText(WWGnlUtilities.buildMessage("title"));
    this.add(titleTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(fullScreenCheckBox, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  public String getTitle()
  { return titleTextField.getText(); }
  
  public boolean getAllCompositeOption()
  { return fullScreenCheckBox.isSelected(); }
}
