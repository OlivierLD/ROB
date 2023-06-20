// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Main4Tests.java

package examples.midatlantic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Referenced classes of package examples.midatlantic:
//      SampleFrame

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
