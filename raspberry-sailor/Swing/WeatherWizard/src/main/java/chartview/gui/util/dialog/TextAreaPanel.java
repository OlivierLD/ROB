package chartview.gui.util.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class TextAreaPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane = new JScrollPane();
  private JTextArea jTextArea = new JTextArea();

  public TextAreaPanel()
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
    this.setSize(new Dimension(290, 200));
    this.setPreferredSize(new Dimension(290, 200));
    jScrollPane.getViewport().add(jTextArea, null);
    this.add(jScrollPane, BorderLayout.CENTER);
  }
  
  public void setText(String s)
  {
    jTextArea.setText(s);
  }
  
  public String getText()
  {
    return jTextArea.getText();
  }
}
