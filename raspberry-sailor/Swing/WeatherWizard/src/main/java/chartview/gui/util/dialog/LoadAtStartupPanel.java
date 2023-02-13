package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LoadAtStartupPanel
  extends JPanel
{
  private String patternName = "";
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel lineOne = new JLabel();
  private JLabel lineTwo = new JLabel();
  private JLabel lineThree = new JLabel();
  private JLabel lineFour = new JLabel();
  private JLabel lineFive = new JLabel();

  public LoadAtStartupPanel(String compName)
  {
    patternName = compName;
    if (patternName.indexOf("/") > -1)
      patternName = patternName.substring(patternName.lastIndexOf("/") + 1);
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
    lineOne.setText(WWGnlUtilities.buildMessage("you-said-1"));
    lineTwo.setText(WWGnlUtilities.buildMessage("you-said-2"));
    lineThree.setText(WWGnlUtilities.buildMessage("you-said-3", new String[] { patternName }));
    lineFour.setText(WWGnlUtilities.buildMessage("you-said-4"));
    lineFive.setText(WWGnlUtilities.buildMessage("you-said-5", new String[] { "XX" }));
    this.add(lineOne, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(lineTwo, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(lineThree, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(lineFour, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(5, 0, 0, 0), 0, 0));
    this.add(lineFive, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(10, 0, 0, 0), 0, 0));
  }
  
  public void setLineFiveMessage(String str)
  { lineFive.setText(str); }
}
