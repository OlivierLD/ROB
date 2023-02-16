package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;
import java.util.Calendar;
import java.util.Date;

import java.util.GregorianCalendar;

import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class DateTimePanel
     extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JTextField yearTextField = new JTextField();
  private JComboBox monthComboBox = new JComboBox();
  private JTextField dayTextField = new JTextField();
  private JTextField hourTextField = new JTextField();
  private JTextField minTextField = new JTextField();
  private JTextField secTextField = new JTextField();
  private JLabel gmtLabel = new JLabel();
  private JLabel sepLabel1 = new JLabel();
  private JLabel sepLabel2 = new JLabel();

  private JLabel atLabel = new JLabel();

  public DateTimePanel()
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

  public void setDate(Date date)
  {
    TimeZone.setDefault(TimeZone.getTimeZone("127"));
    Calendar cal = new GregorianCalendar();
    cal.setTime(date);
    
    int year    = cal.get(Calendar.YEAR);
    int month   = cal.get(Calendar.MONTH);
    int day     = cal.get(Calendar.DAY_OF_MONTH);
    int hours   = cal.get(Calendar.HOUR_OF_DAY);
    int minutes = cal.get(Calendar.MINUTE);
    int seconds = cal.get(Calendar.SECOND);
    
    yearTextField.setText(Integer.toString(year));
    monthComboBox.setSelectedIndex(month);
    dayTextField.setText(Integer.toString(day));
    hourTextField.setText(Integer.toString(hours));
    minTextField.setText(Integer.toString(minutes));
    secTextField.setText(Integer.toString(seconds));
  }
  
  public Date getDate()
  {
    Calendar cal = new GregorianCalendar();
    cal.set(Integer.parseInt(yearTextField.getText()),
            monthComboBox.getSelectedIndex(),
            Integer.parseInt(dayTextField.getText()),
            Integer.parseInt(hourTextField.getText()),
            Integer.parseInt(minTextField.getText()),
            Integer.parseInt(secTextField.getText()));
    return cal.getTime();
  }
  
  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(328, 29));
    yearTextField.setPreferredSize(new Dimension(40, 20));
    yearTextField.setHorizontalAlignment(JTextField.CENTER);
    monthComboBox.setPreferredSize(new Dimension(60, 20));
    dayTextField.setPreferredSize(new Dimension(30, 20));
    dayTextField.setHorizontalAlignment(JTextField.CENTER);
    hourTextField.setPreferredSize(new Dimension(30, 20));
    hourTextField.setHorizontalAlignment(JTextField.CENTER);
    minTextField.setPreferredSize(new Dimension(30, 20));
    minTextField.setHorizontalAlignment(JTextField.CENTER);
    secTextField.setPreferredSize(new Dimension(30, 20));
    secTextField.setHorizontalAlignment(JTextField.CENTER);
    gmtLabel.setText("UTC");
    sepLabel1.setText(":");
    sepLabel2.setText(":");

    atLabel.setText("@");
    for (int i=0; i < WWGnlUtilities.MONTH.length; i++)
      monthComboBox.addItem(WWGnlUtilities.MONTH[i]);

    this.add(yearTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(monthComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(dayTextField, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(hourTextField, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(minTextField, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(secTextField, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 5, 0, 5), 0, 0));
    this.add(gmtLabel, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(sepLabel1, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(sepLabel2, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(atLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
  }

  public void setEnabled(boolean b)
  {
    yearTextField.setEnabled(b);
    monthComboBox.setEnabled(b);
    dayTextField.setEnabled(b);
    hourTextField.setEnabled(b);
    minTextField.setEnabled(b);
    secTextField.setEnabled(b);
    gmtLabel.setEnabled(b);
    sepLabel1.setEnabled(b);
    sepLabel2.setEnabled(b);
    atLabel.setEnabled(b);
  }
}
