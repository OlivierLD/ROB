package chartview.gui.toolbar.controlpanels;

import chartview.ctx.WWContext;

import chartview.gui.util.dialog.FaxType;
import chartview.ctx.ApplicationEventListener;

import chartview.util.WWGnlUtilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

@SuppressWarnings("serial")
public class SelectFaxPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JComboBox activeFaxComboBox = new JComboBox();
  private JLabel faxLabel = new JLabel();
  private JCheckBox allFaxesCheckBox = new JCheckBox();

  private transient ListCellRenderer colorRenderer = new ListCellRenderer()
  {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      FaxType ft = (FaxType)value;
      JLabel label = null;
      if (ft != null)
      {
        String val = ft.getValue();
        Color c = ft.getColor();
        label = new JLabel(val);
        if (isSelected)
          label.setBackground(list.getSelectionBackground());
        else
          label.setBackground(Color.white);
        label.setForeground(c);
        label.setOpaque(true);
      }
      return label;
    }
  };

  public SelectFaxPanel()
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
    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
      {
        public String toString()
        {
          return "from SelectFaxPanel.";
        }
        public void faxesLoaded(FaxType[] ft) 
        {
          // Reset poplist
          activeFaxComboBox.removeAllItems();
          for (int i=0; i<ft.length; i++)
          {
            activeFaxComboBox.addItem(ft[i]);
//          System.out.println("Adding " + ft[i].getValue());
          }
          // broadcast ative fax, first one.
          if (ft != null && ft.length > 0)
            WWContext.getInstance().fireActiveFaxChanged(ft[0]);
        }
      });
    this.setLayout(gridBagLayout1);
    activeFaxComboBox.setMinimumSize(new Dimension(100, 20));
    activeFaxComboBox.setPreferredSize(new Dimension(150, 20));
    activeFaxComboBox.setRenderer(colorRenderer);
    activeFaxComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            activeFaxComboBox_actionPerformed(e);
          }
        });
    faxLabel.setText(WWGnlUtilities.buildMessage("active-fax"));
    allFaxesCheckBox.setText(WWGnlUtilities.buildMessage("all-faxes"));
    allFaxesCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            allFaxesCheckBox_actionPerformed(e);
          }
        });
    this.add(activeFaxComboBox, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(faxLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(allFaxesCheckBox, 
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
  }

  private void allFaxesCheckBox_actionPerformed(ActionEvent e)
  {
    activeFaxComboBox.setEnabled(!allFaxesCheckBox.isSelected());
    if (allFaxesCheckBox.isSelected())
    {
      // Broadcast
      faxLabel.setEnabled(false);
      WWContext.getInstance().fireAllFaxesSelected();
    }
    else
    {
      // Broadcast
      faxLabel.setEnabled(true);
      WWContext.getInstance().fireActiveFaxChanged((FaxType)activeFaxComboBox.getSelectedItem());
    }
  }

  private void activeFaxComboBox_actionPerformed(ActionEvent e)
  {
    FaxType ft = (FaxType)activeFaxComboBox.getSelectedItem();
    if (ft != null)
    {
      activeFaxComboBox.setForeground(ft.getColor());
      faxLabel.setForeground(ft.getColor());
      WWContext.getInstance().fireActiveFaxChanged(ft);
      ft.getRotation();
    }
  }
  
  public void setEnabled(boolean b)
  {
    faxLabel.setEnabled(b);
    allFaxesCheckBox.setEnabled(b);
    activeFaxComboBox.setEnabled(b);
  }
}
