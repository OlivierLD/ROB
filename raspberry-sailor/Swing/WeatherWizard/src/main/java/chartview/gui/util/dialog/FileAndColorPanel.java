package chartview.gui.util.dialog;

import chartview.ctx.WWContext;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class FileAndColorPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JTextField fileNameTextField = new JTextField();
  private JButton browseButton = new JButton();
  private JLabel colorLabel = new JLabel();
  private JButton colorButton = new JButton();
  
  Color faxColor = Color.black;
  private JCheckBox applyColorCheckBox = new JCheckBox();

  public FileAndColorPanel()
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
    fileNameTextField.setPreferredSize(new Dimension(200, 20));
    browseButton.setText(WWGnlUtilities.buildMessage("browse"));
    browseButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseButton_actionPerformed(e);
          }
        });
    colorLabel.setText(WWGnlUtilities.buildMessage("color"));
    colorLabel.setBackground(Color.black);
    colorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    colorLabel.setOpaque(true);
    colorButton.setText(WWGnlUtilities.buildMessage("color-plus"));
    colorButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            colorButton_actionPerformed(e);
          }
        });
    applyColorCheckBox.setText("Apply Color"); // LOCALIZE
    applyColorCheckBox.setSelected(true);
    applyColorCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          applyColorCheckBox_actionPerformed(e);
        }
      });
    this.add(fileNameTextField, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(colorLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
    this.add(colorButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(applyColorCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  private void browseButton_actionPerformed(ActionEvent e)
  {
    String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
    String fax = WWGnlUtilities.chooseFile(this, 
                                           JFileChooser.FILES_ONLY, 
                                           new String[] { "gif", "jpg", "jpeg", "tif", "tiff", "png" }, 
                                           WWGnlUtilities.buildMessage("splash-faxes"), 
                                           firstDir, 
                                           WWGnlUtilities.buildMessage("open"), 
                                           WWGnlUtilities.buildMessage("open-fax"), 
                                           true); // true: with previewer
    if (fax != null && fax.trim().length() > 0)
      fileNameTextField.setText(fax);
  }

  private void colorButton_actionPerformed(ActionEvent e)
  {
    Color orig = faxColor;
    Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("set-fax-color"), orig);
    if (newColor != null)
    {
      faxColor = newColor;
      colorLabel.setBackground(faxColor);
      colorLabel.repaint();
      WWContext.getInstance().fireLogging("New Color:" + faxColor.toString() + "\n");
    }
  }
  
  public Color getColor()
  { return faxColor; }
  public String getFileName()
  { return fileNameTextField.getText(); }
  public void setColor(Color c)
  { 
    faxColor = c; 
    colorLabel.setBackground(c);
  }
  public void setFileName(String s)
  { fileNameTextField.setText(s); }

  private void applyColorCheckBox_actionPerformed(ActionEvent e)
  {
    colorLabel.setEnabled(applyColorCheckBox.isSelected());
    colorButton.setEnabled(applyColorCheckBox.isSelected());
  }
  
  public boolean isColorApplied()
  {
    return applyColorCheckBox.isSelected();
  }
  
  public void setColorApplied(boolean b)
  {
    applyColorCheckBox.setSelected(b);
    applyColorCheckBox_actionPerformed(null);
  }
}
