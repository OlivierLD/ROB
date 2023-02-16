package chartview.util.grib.panel;

import java.awt.BorderLayout;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class BulkGribPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane mainTabbedPane = new JTabbedPane();

  public BulkGribPanel()
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
    this.setSize(new Dimension(650, 425));
    this.setMinimumSize(new Dimension(650, 425));
    this.setPreferredSize(new Dimension(650, 425));
    mainTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    this.add(mainTabbedPane, BorderLayout.CENTER);
  }
  
  public JTabbedPane getMainTabbedPane()
  {
    return mainTabbedPane;
  }
}
