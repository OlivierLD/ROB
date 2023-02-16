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


public class SatellitePanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JTextField latTextField = new JTextField();
  private JTextField lngTextField = new JTextField();
  private JCheckBox opaqueCheckBox = new JCheckBox();
  private JComboBox nsComboBox = new JComboBox();
  private JComboBox ewComboBox = new JComboBox();
  private JLabel jLabel4 = new JLabel();
  private JTextField altitudeTextField = new JTextField();
  private JSeparator jSeparator1 = new JSeparator();

  public SatellitePanel()
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
    jLabel1.setText(WWGnlUtilities.buildMessage("satellite-nadir"));
    jLabel2.setText("L:");
    jLabel3.setText("G:");
    latTextField.setPreferredSize(new Dimension(40, 20));
    latTextField.setHorizontalAlignment(JTextField.RIGHT);
    latTextField.setMinimumSize(new Dimension(60, 20));
    latTextField.setText("0.0");
    lngTextField.setPreferredSize(new Dimension(40, 20));
    lngTextField.setHorizontalAlignment(JTextField.RIGHT);
    lngTextField.setMinimumSize(new Dimension(60, 20));
    lngTextField.setText("0.0");
    opaqueCheckBox.setText(WWGnlUtilities.buildMessage("opaque"));
    nsComboBox.setPreferredSize(new Dimension(32, 20));
    jLabel4.setText(WWGnlUtilities.buildMessage("altitude"));
    altitudeTextField.setHorizontalAlignment(JTextField.RIGHT);
    altitudeTextField.setText("40000");
    nsComboBox.addItem("N");
    nsComboBox.addItem("S");
    ewComboBox.addItem("E");
    ewComboBox.addItem("W");
    this.add(jLabel1, new GridBagConstraints(0, 0, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 2, 0, 0), 0, 0));
    this.add(latTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(lngTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(opaqueCheckBox, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 2, 0, 0), 0, 0));
    this.add(nsComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(ewComboBox, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(altitudeTextField,
             new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(jSeparator1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(2, 0, 0, 0), 0, 0));
  }

  public boolean isSatelliteOpaque()
  {
    return opaqueCheckBox.isSelected();
  }

  public void setSatelliteOpaque(boolean b)
  {
    opaqueCheckBox.setSelected(b);
  }

  public double getLatitude()
    throws Exception
  {
    double d = Double.parseDouble(latTextField.getText());
    if (nsComboBox.getSelectedItem().equals("S"))
      d = -d;
    return d;
  }

  public double getLongitude()
    throws Exception
  {
    double d = Double.parseDouble(lngTextField.getText());
    if (ewComboBox.getSelectedItem().equals("W"))
      d = -d;
    return d;
  }

  public double getAltitude() throws Exception
  {
    double d = Double.parseDouble(altitudeTextField.getText());
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

  public void setAltitude(double d)
  {
    altitudeTextField.setText(Double.toString(Math.abs(d)));
  }
}
