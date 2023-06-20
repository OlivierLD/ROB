package examples.casestudy;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame
{
  private BorderLayout borderLayout;
  private CommandPanel commandPanel;
//private JPanel commandPanel;

  public SampleFrame()
  {
    borderLayout = new BorderLayout();
    commandPanel = new CommandPanel();
//  commandPanel = new JPanel();
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    getContentPane().setLayout(borderLayout);
    setSize(new Dimension(816, 547));
    setTitle("Case Study");
    getContentPane().add(commandPanel, BorderLayout.CENTER);
  }
}
