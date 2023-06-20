package examples.shoredetection;

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
    setTitle("Example 0 - Contour detection");
    getContentPane().add(commandPanel1, "Center");
  }

  private BorderLayout borderLayout1;
  private CommandPanel commandPanel1;
}
