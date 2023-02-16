package chartview.gui.toolbar.controlpanels;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class SingleControlPane
  extends JPanel
{
  private CustomPanelButton topPanel = null;
  
  private JPanel controlPanel = null;
  protected boolean visible = true;
  
  protected boolean enabled = true;
  
  public SingleControlPane(String title, JPanel control, boolean visible)
  {
    this.controlPanel = control;
    this.visible = visible;
    topPanel = new CustomPanelButton(title);
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
    this.setLayout(new BorderLayout());
    this.add(topPanel, BorderLayout.NORTH);
    this.add(controlPanel, BorderLayout.CENTER);
    controlPanel.setVisible(visible);
    topPanel.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (topPanel.isEnabled())
          {
            visible = !visible;
            controlPanel.setVisible(visible);
          }
          onClickOnControl(enabled && visible);
        }
      });
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    if (!enabled && visible)
      controlPanel.setVisible(false);
    topPanel.setEnabled(enabled);
    onClickOnControl(enabled && visible);
  }
  
  protected void onClickOnControl(boolean displayingData)
  { }
}
