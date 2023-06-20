package examples.casestudy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Celestial Navigation Case Study:
 * ================================
 * 
 * Date: 12-Jul-2006
 * Observation 1:
 * Local Solar Time: 08:09:50
 * GMT : 17:37:58
 * Estimated Position: L:  24 58.5 N
 *                     G: 142 01.9 W
 * log: 99.63
 * hi : 36 01.2 
 * 
 * 3 hours later, done 25.9 miles in the 264 true (log says 23.85 miles)
 * Observation 2:
 * Local Solar Time: 11:14:30
 * GMT : 20:44:23
 * Estimated Position: L:  24 55.9 N
 *                     G: 142 28.1 W
 * log: 23.48 (diff 23.85)
 * heading during last period: 264t
 * hi : 77 40.5 
 *
 * 4 hours later, done 33.6 miles in the 261 true (log says 31.78 miles)
 * Observation 3:
 * Local Solar Time: 15:09:20
 * GMT : 00:41:30 (on Jul-13)
 * Estimated Position: L:  24 50.8 N
 *                     G: 143 02.4 W
 * log: 55.26 (diff 31.78)
 * heading during last period: 261t
 * hi : 47 44.4 
 */
public class Main4Tests
{

  public Main4Tests()
  {
    Frame frame = new SampleFrame();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if(frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if(frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }

    });
    frame.setVisible(true);
  }

  public static void main(String args[])
  {
    try
    {
      if (System.getProperty("swing.defaultlaf") == null)
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    new Main4Tests();
  }
}
