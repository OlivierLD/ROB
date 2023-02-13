package chartview.gui.util.dialog;

import java.awt.BorderLayout;

import java.awt.Dimension;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class GRIBDetailPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane = new JScrollPane();
  private JTextArea jTextArea = new JTextArea();

  public GRIBDetailPanel()
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
    this.setSize(new Dimension(400, 250));
    this.setPreferredSize(new Dimension(400, 250));
    jTextArea.setEditable(false);
    jTextArea.setFont(new Font("Courier", 0, 11));
    jScrollPane.getViewport().add(jTextArea, null);
    this.add(jScrollPane, BorderLayout.CENTER);
  }
  
  public void setText(String str)
  {
    jTextArea.setText(str);
  }
}
