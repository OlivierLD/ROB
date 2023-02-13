package chartview.gui.toolbar.controlpanels;

import chartview.ctx.WWContext;

import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public abstract class ButtonCommandPanel
  extends JPanel
{
  private JButton upButton = new JButton();
  private JButton downButton = new JButton();
  private JButton rightButton = new JButton();
  private JButton leftButton = new JButton();
  private ZoomPanel zoomPanel = new ZoomPanel(this);
  private GridBagLayout gridBagLayout = new GridBagLayout();
  
  protected boolean shiftDown = false;

  public ButtonCommandPanel()
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
    this.setLayout(gridBagLayout);
    this.setSize(new Dimension(110, 80));
    this.setPreferredSize(new Dimension(110, 80));
    this.setMinimumSize(new Dimension(110, 80));
    this.setMaximumSize(new Dimension(110, 80));
//  upButton.setIcon(new ImageIcon(this.getClass().getResource("up.png")));
    upButton.setIcon(new ImageIcon(this.getClass().getResource("img/panup.gif")));
    upButton.setPreferredSize(new Dimension(24, 24));
    upButton.setBorderPainted(false);
    upButton.setMinimumSize(new Dimension(24, 24));
    upButton.setMaximumSize(new Dimension(24, 24));
    upButton.setMargin(new Insets(1, 1, 1, 1));
    upButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            upButton_actionPerformed(e);
          }
        });
    upButton.setToolTipText("Shift: x10");
//  downButton.setIcon(new ImageIcon(this.getClass().getResource("down.png")));
    downButton.setIcon(new ImageIcon(this.getClass().getResource("img/pandown.gif")));
    downButton.setPreferredSize(new Dimension(24, 24));
    downButton.setBorderPainted(false);
    downButton.setMaximumSize(new Dimension(24, 24));
    downButton.setMinimumSize(new Dimension(24, 24));
    downButton.setMargin(new Insets(1, 1, 1, 1));
    downButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            downButton_actionPerformed(e);
          }
        });
    downButton.setToolTipText("Shift: x10");
//  rightButton.setIcon(new ImageIcon(this.getClass().getResource("right.png")));
    rightButton.setIcon(new ImageIcon(this.getClass().getResource("img/panright.gif")));
    rightButton.setPreferredSize(new Dimension(24, 24));
    rightButton.setBorderPainted(false);
    rightButton.setMaximumSize(new Dimension(24, 24));
    rightButton.setMinimumSize(new Dimension(24, 24));
    rightButton.setMargin(new Insets(1, 1, 1, 1));
    rightButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            rightButton_actionPerformed(e);
          }
        });
    rightButton.setToolTipText("Shift: x10");
//  leftButton.setIcon(new ImageIcon(this.getClass().getResource("left.png")));
    leftButton.setIcon(new ImageIcon(this.getClass().getResource("img/panleft.gif")));
    leftButton.setPreferredSize(new Dimension(24, 24));
    leftButton.setBorderPainted(false);
    leftButton.setMaximumSize(new Dimension(24, 24));
    leftButton.setMinimumSize(new Dimension(24, 24));
    leftButton.setMargin(new Insets(1, 1, 1, 1));
    leftButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            leftButton_actionPerformed(e);
          }
        });
    leftButton.setToolTipText("Shift: x10");
    zoomPanel.setSize(new Dimension(60, 30));
    zoomPanel.setPreferredSize(new Dimension(60, 30));
    this.add(upButton, 
             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(downButton, 
             new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(rightButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(leftButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(zoomPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
  }

  private void upButton_actionPerformed(ActionEvent e)
  {
    shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
    fireUp();
  }

  private void downButton_actionPerformed(ActionEvent e)
  {
    shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
    fireDown();
  }

  private void leftButton_actionPerformed(ActionEvent e)
  {
    shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
    fireLeft();
  }

  private void rightButton_actionPerformed(ActionEvent e)
  {
    shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
    fireRight();
  }
  
  public void zoomIn()
  {
    shiftDown = zoomPanel.shiftDown;
    fireZoomIn();
  }
  
  public void zoomOut()
  {
    shiftDown = zoomPanel.shiftDown;
    fireZoomOut();
  }
  
  public abstract void fireUp();  
  public abstract void fireDown();
  public abstract void fireLeft();
  public abstract void fireRight();
  public abstract void fireZoomIn();
  public abstract void fireZoomOut();
  
  public void setEnabled(boolean b)
  {
    upButton.setEnabled(b);
    downButton.setEnabled(b);
    leftButton.setEnabled(b);
    rightButton.setEnabled(b);
    zoomPanel.setEnabled(b);
  }
}
