package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class OneFilePanel
  extends JPanel
{
  private OneFilePanel instance = this;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel leftLabel = new JLabel();
  private JFileChooser leftChooser = new JFileChooser();
  private JTextField regExprPatternTextField = new JTextField();
  private JLabel patternLabel = new JLabel();
  private JLabel regExpLabel = new JLabel();
  

  public OneFilePanel()
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
    this.setSize(new Dimension(666, 453));
    leftLabel.setText("Composite Directory");
    leftLabel.setFont(new Font("Tahoma", 1, 11));
    regExprPatternTextField.setText(".*");
    patternLabel.setText("Regular expression?");
    regExpLabel.setText("<html><u>" + WWGnlUtilities.buildMessage("help-reg-exp") + "</u></html>"); 
    regExpLabel.setForeground(Color.blue);
    regExpLabel.addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent e)
          {
            regExpLabel.setForeground(WWGnlUtilities.PURPLE);
            regExpLabel.repaint();
            try { Utilities.openInBrowser("http://www.regular-expressions.info/tutorial.html"); }
            catch (Exception ex)
            { JOptionPane.showMessageDialog(instance, "Problem with RegExp help\n" + ex.toString(), "Regular Expressions", JOptionPane.WARNING_MESSAGE); }
          }
          public void mouseEntered(MouseEvent e)
          {
            regExpLabel.setForeground(WWGnlUtilities.PURPLE);
            regExpLabel.repaint();
          }
          public void mouseExited(MouseEvent e)
          {
            regExpLabel.setForeground(Color.blue);
            regExpLabel.repaint();
          }
    });

    this.add(leftLabel,               new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(leftChooser,             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(regExprPatternTextField, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(patternLabel,            new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(regExpLabel,             new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    //Previous regexpr?
    Properties p = new Properties();
    try
    {
      p.load(new FileInputStream(WWGnlUtilities.REGEXPR_ROUTING_PROPERTIES_FILE));
      regExprPatternTextField.setText(p.getProperty(WWGnlUtilities.COMPOSITE_FILTER_FOR_ROUTING, ".*"));
    }
    catch (FileNotFoundException fnfe)
    {
      fnfe.printStackTrace();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  public JLabel getLeftLabel()
  {
    return leftLabel;
  }

  public JFileChooser getLeftChooser()
  {
    return leftChooser;
  }

  public JTextField getRegExprPatternTextField()
  {
    return regExprPatternTextField;
  }

  public JLabel getPatternLabel()
  {
    return patternLabel;
  }
}
