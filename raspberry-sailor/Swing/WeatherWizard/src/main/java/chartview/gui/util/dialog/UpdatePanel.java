package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class UpdatePanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel topLabel = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JPanel bottomPanel = new JPanel();
  private JPanel centerBottomPanel = new JPanel();
  private JPanel bottomBottomPanel = new JPanel();
  private JTextArea jTextArea = new JTextArea();
  private JLabel bottomLeft = new JLabel();
  private JLabel bottomRight = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel bottomLabel1 = new JLabel();
  private JLabel bottomLabel2 = new JLabel();

  public UpdatePanel()
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
    topLabel.setText(" - ");
    jScrollPane1.setMaximumSize(new Dimension(400, 240));
    jScrollPane1.setPreferredSize(new Dimension(400, 239));
    jScrollPane1.setMinimumSize(new Dimension(400, 239));
    bottomPanel.setLayout(borderLayout3);
    centerBottomPanel.setLayout(gridBagLayout1);
    bottomBottomPanel.setLayout(borderLayout2);
    jTextArea.setEditable(false);
    jTextArea.setBackground(SystemColor.control);
    bottomLeft.setText(WWGnlUtilities.buildMessage("check-update-nature"));
    bottomRight.setText("<html><u>The Weather Wizard History Page</u></html>");
    bottomRight.setForeground(Color.blue);
    bottomRight.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          url_mouseClicked(e);
        }
        public void mouseEntered(MouseEvent e)
        {
          bottomRight.setForeground(WWGnlUtilities.PURPLE);
          bottomRight.repaint();
        }
        public void mouseExited(MouseEvent e)
        {
          bottomRight.setForeground(Color.blue);
          bottomRight.repaint();
        }
      });

    bottomLabel1.setText(" ");
    bottomLabel2.setText(" ");
    this.add(topLabel, BorderLayout.NORTH);
    jScrollPane1.getViewport().add(jTextArea, null);
    this.add(jScrollPane1, BorderLayout.CENTER);
    bottomBottomPanel.add(bottomLeft, BorderLayout.WEST);
    bottomBottomPanel.add(bottomRight, BorderLayout.CENTER);
    this.add(bottomPanel, BorderLayout.SOUTH);
    centerBottomPanel.add(bottomLabel1,
                          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    centerBottomPanel.add(bottomLabel2,
                          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 5, 0), 0, 0));
    bottomPanel.add(centerBottomPanel, BorderLayout.CENTER);
    bottomPanel.add(bottomBottomPanel, BorderLayout.SOUTH);
  }
  
  private void url_mouseClicked(MouseEvent e)
  {
//  try { Utilities.openInBrowser("http://donpedro.lediouris.net/software/structure/datafiles/news/index.html"); } 
    try { Utilities.openInBrowser("http://code.google.com/p/weatherwizard/wiki/WWHistory?ts=1337007410&updated=WWHistory"); } 
    catch (Exception ignore) {}
  }
  
  public void setFileList(String str)
  {
    jTextArea.setText(str);
  }
  
  public void setTopLabel(String str)
  {
    topLabel.setText(str);
  }
  
  public void setBottomLabel1(String str)
  {
    bottomLabel1.setText(str);
  }
  public void setBottomLabel2(String str)
  {
    bottomLabel2.setText(str);
  }
}
