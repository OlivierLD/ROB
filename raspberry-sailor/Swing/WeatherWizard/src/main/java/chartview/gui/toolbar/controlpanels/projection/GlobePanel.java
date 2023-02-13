package chartview.gui.toolbar.controlpanels.projection;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GlobePanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JTextField latTextField = new JTextField();
  private JTextField lngTextField = new JTextField();
  private JCheckBox opaqueCheckBox = new JCheckBox();
  private JLabel jLabel4 = new JLabel();
  private JTextField tiltTextField = new JTextField();
  private JComboBox nsComboBox = new JComboBox();
  private JComboBox ewComboBox = new JComboBox();
  private JSeparator jSeparator1 = new JSeparator();

  public GlobePanel()
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
    this.setLayout(gridBagLayout1);
    jLabel1.setText("Eye Zenith");
    jLabel2.setText(WWGnlUtilities.buildMessage("lat"));
    jLabel3.setText(WWGnlUtilities.buildMessage("long"));
    latTextField.setPreferredSize(new Dimension(40, 20));
    latTextField.setHorizontalAlignment(JTextField.RIGHT);
    latTextField.setMinimumSize(new Dimension(60, 20));
    latTextField.setText("0.0");
    lngTextField.setPreferredSize(new Dimension(40, 20));
    lngTextField.setHorizontalAlignment(JTextField.RIGHT);
    lngTextField.setMinimumSize(new Dimension(60, 20));
    lngTextField.setText("0.0");
    opaqueCheckBox.setText(WWGnlUtilities.buildMessage("opaque"));
    jLabel4.setText(WWGnlUtilities.buildMessage("tilt"));
    tiltTextField.setMinimumSize(new Dimension(60, 20));
    tiltTextField.setPreferredSize(new Dimension(60, 20));
    tiltTextField.setHorizontalAlignment(JTextField.RIGHT);
    tiltTextField.setText("0");
    nsComboBox.addItem("N");
    nsComboBox.addItem("S");
    ewComboBox.addItem("E");
    ewComboBox.addItem("W");
    this.add(jLabel1, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 2, 0, 0), 0, 0));
    this.add(latTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(lngTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(opaqueCheckBox, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 2, 0, 0), 0, 0));
    this.add(jLabel4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(tiltTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(nsComboBox, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(ewComboBox, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jSeparator1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(2, 0, 0, 0), 0, 0));
  }
  
  public boolean isGlobeOpaque()
  {
    return opaqueCheckBox.isSelected();
  }
  
  public void setGlobeOpaque(boolean b)
  {
    opaqueCheckBox.setSelected(b);
  }
  
  public double getLatitude() throws Exception
  {
    double d = Double.parseDouble(latTextField.getText());
    if (nsComboBox.getSelectedItem().equals("S"))
      d = -d;
    return d;
  }

  public double getLongitude() throws Exception
  {
    double d = Double.parseDouble(lngTextField.getText());
    if (ewComboBox.getSelectedItem().equals("W"))
      d = -d;
    return d;
  }
  
  public void setLatitude(double d)
  {
    latTextField.setText(Double.toString(Math.abs(d)));
    if (d < 0)
      nsComboBox.setSelectedItem("S");
    else
      nsComboBox.setSelectedItem("N");
  }
  
  public void setLongitude(double d)
  {
    lngTextField.setText(Double.toString(Math.abs(d)));
    if (d < 0)
      ewComboBox.setSelectedItem("W");
    else
      ewComboBox.setSelectedItem("E");
  }
     
  public double getTilt() throws Exception
  {
    return Double.parseDouble(tiltTextField.getText());
  }
}
