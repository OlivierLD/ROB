package chartview.gui.toolbar.controlpanels.projection;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class AnaximandreMercatorPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel jLabel1 = new JLabel();

  public AnaximandreMercatorPanel()
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
    this.setLayout(borderLayout1);
    jLabel1.setText(WWGnlUtilities.buildMessage("nothing-specific"));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setEnabled(false);
    this.add(jLabel1, BorderLayout.CENTER);
  }
}
