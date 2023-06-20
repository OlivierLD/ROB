package examples.pacific;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame
{

  public SampleFrame()
  {
    borderLayout1 = new BorderLayout();
    commandPanel1 = new CommandPanel();
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
    getContentPane().setLayout(borderLayout1);
    setSize(new Dimension(600, 400));
    setTitle("Example 1 - Pacific Tour!");
    getContentPane().add(commandPanel1, "Center");
  }

  private BorderLayout borderLayout1;
  private CommandPanel commandPanel1;
}
