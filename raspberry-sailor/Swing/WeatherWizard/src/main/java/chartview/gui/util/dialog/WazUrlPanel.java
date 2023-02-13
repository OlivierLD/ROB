package chartview.gui.util.dialog;

import chartview.ctx.WWContext;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class WazUrlPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JTextField urlTextField = new JTextField();
  private JLabel urlLabel = new JLabel();

  public WazUrlPanel()
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
    jLabel1.setText(WWGnlUtilities.buildMessage("waz-url"));
    jLabel1.setFont(new Font("Tahoma", 1, 11));
    urlTextField.setHorizontalAlignment(JTextField.LEFT);
    urlTextField.setText("http://donpedro.lediouris.net/weather/waz/...");
    urlTextField.setPreferredSize(new Dimension(300, 20));
    urlLabel.setText("<html><u>Waz Directory</u></html>");
    urlLabel.setForeground(Color.blue);
    urlLabel.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          labelCompany_mouseClicked(e);
        }
        public void mouseEntered(MouseEvent e)
        {
          urlLabel.setForeground(WWGnlUtilities.PURPLE);
          urlLabel.repaint();
        }
        public void mouseExited(MouseEvent e)
        {
          urlLabel.setForeground(Color.blue);
          urlLabel.repaint();
        }
      });


    this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(urlTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(urlLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
  }

  public String getURL()
  {
    return urlTextField.getText();
  }

  public void setURL(String s)
  {
    urlTextField.setText(s);
  }
  
  private void labelCompany_mouseClicked(MouseEvent e)
  {
    try
    {
      urlLabel.setForeground(WWGnlUtilities.PURPLE);
      Utilities.openInBrowser("http://donpedro.lediouris.net/weather/waz/");
    }
    catch (Exception ignore)
    {
    }
  }
}
