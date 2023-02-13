package chartview.gui.toolbar.controlpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class CustomPanelButton
  extends JPanel
{
  private JLabel titleLabel = new JLabel();
  private String title = " -- ";
  private boolean enabled = true;
  private ImageIcon icon = null;
//private String tooltipText = null;
  private JPanel holder = new JPanel(new FlowLayout());

  public CustomPanelButton()
  {
    this(null);
  }
  public CustomPanelButton(String title)
  {
    this(title, null);
  }
  public CustomPanelButton(String title, ImageIcon icon)
  {
    this(title, icon, null);
  }
  public CustomPanelButton(String title, ImageIcon icon, String tt)
  {
    this.title = title;
    this.setIcon(icon);
    if (tt != null)
      this.setToolTipText(tt);
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
//  this.setLayout(null);
    titleLabel.setText(title);
//  titleLabel.setForeground(Color.green);
    holder.setOpaque(false);
    if (icon != null)
      holder.add(new JLabel(icon), null);
    holder.add(titleLabel, null);
//    if (tooltipText != null)
//      super.setToolTipText(tooltipText);
    this.add(holder, BorderLayout.WEST);
    validate();
  }

  public void paintComponent(Graphics g)
  {
//  Color startColor = new Color(0x94, 0x9c, 0x84); // new Color(0, 128, 128); // Color.black; // new Color(255, 255, 255);
    Color startColor = Color.lightGray;
//  Color endColor   = new Color(0, 64, 64); // Color.gray; // new Color(102, 102, 102);
    Color endColor   = Color.white;
    GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
    ((Graphics2D)g).setPaint(gradient);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());    
    if (false)
    {
      holder.removeAll();
      if (icon != null)
        holder.add(new JLabel(icon), null);
      holder.add(titleLabel, null);
    }
//  holder.validate();
//  validate();
  }
  
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    titleLabel.setEnabled(enabled);
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
    holder.removeAll();
    if (icon != null)
      holder.add(new JLabel(icon), null);
    holder.add(titleLabel, null);
//  this.repaint();
  }

  public void setText(String title)
  {
    this.title = title;
//  this.repaint();
  }

  public String getText()
  {
    return title;
  }
}
