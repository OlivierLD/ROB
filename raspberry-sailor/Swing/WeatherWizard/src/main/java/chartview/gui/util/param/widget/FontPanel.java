package chartview.gui.util.param.widget;

import chartview.util.WWGnlUtilities;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FontPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JComboBox fontNameComboBox = new JComboBox();
  private JComboBox fontSizeComboBox = new JComboBox();
  private JCheckBox italicCheckBox = new JCheckBox();
  private JCheckBox boldCheckBox = new JCheckBox();

  private static final String[] DEFAULT_FONT_SIZE_STRINGS = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72" };
  private JLabel sampleLabel = new JLabel();

  private String fName;
  private String fSize;
  private boolean italic = false;
  private boolean bold   = false;
  
  public FontPanel(Font f)
  {
    this(fontToString(f));
  }
  
  public FontPanel(String fontDesc)
  {
    if (fontDesc != null && fontDesc.trim().length() > 0)  
    {
      String[] selem = fontDesc.split(",");
      if (selem.length == 4)
      {
        String name = selem[0].trim();
        String size = selem[1].trim();
        Boolean italic = Boolean.valueOf(selem[2].trim().equals("italic"));
        Boolean bold = Boolean.valueOf(selem[3].trim().equals("bold"));
        init(name, size, italic, bold);
      }
    }
  }
  
  public FontPanel(String name, String size, Boolean it, Boolean bo)
  {
    init(name, size, it, bo);
  }

  private void init(String name, String size, Boolean it, Boolean bo)
  {
    this.fName = name.trim();
    this.fSize = size.trim();
    if (it != null)
      italic = it.booleanValue();
    if (bo != null)
      bold = bo.booleanValue();
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
    this.setLayout(gridBagLayout1);
    fontNameComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          fontNameComboBox_actionPerformed(e);
        }
      });
    fontSizeComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          fontSizeComboBox_actionPerformed(e);
        }
      });
    italicCheckBox.setText("Italic");
    italicCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          italicCheckBox_actionPerformed(e);
        }
      });
    boldCheckBox.setText("Bold");
    boldCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          boldCheckBox_actionPerformed(e);
        }
      });
    sampleLabel.setText(WWGnlUtilities.buildMessage("all-letter-sentence"));
    this.add(fontNameComboBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(fontSizeComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 5, 0, 0), 0, 0));
    this.add(italicCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 5, 0, 0), 0, 0));
    this.add(boldCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(0, 5, 0, 0), 0, 0));
    this.add(sampleLabel, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
          new Insets(10, 0, 0, 0), 0, 0));
    fontNameComboBox.removeAllItems();    
    String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (String s : fonts)
      fontNameComboBox.addItem(s);
    fontSizeComboBox.removeAllItems();
    for (String s : DEFAULT_FONT_SIZE_STRINGS)
      fontSizeComboBox.addItem(s);
    
    if (fName != null)
      fontNameComboBox.setSelectedItem(fName);
    if (fSize != null)
      fontSizeComboBox.setSelectedItem(fSize);
    boldCheckBox.setSelected(bold);
    italicCheckBox.setSelected(italic);
    displayFont();
  }

  private void fontNameComboBox_actionPerformed(ActionEvent e)
  {
    displayFont();
  }

  private void fontSizeComboBox_actionPerformed(ActionEvent e)
  {
    displayFont();
  }

  private void italicCheckBox_actionPerformed(ActionEvent e)
  {
    displayFont();
  }

  private void boldCheckBox_actionPerformed(ActionEvent e)
  {
    displayFont();
  }
  
  public String getChosenFont()
  {
    return fontNameComboBox.getSelectedItem().toString() + ", " + fontSizeComboBox.getSelectedItem().toString() + ", " + (italicCheckBox.isSelected() ? "italic" : "") + ", " + (boldCheckBox.isSelected() ? "bold" : "");
  }
  
  private void displayFont()
  {
    try
    {
      int style = 0;
      if (italicCheckBox.isSelected())
        style |= Font.ITALIC;
      if (boldCheckBox.isSelected())
        style |= Font.BOLD;
      int size = Integer.parseInt((String)fontSizeComboBox.getSelectedItem());
      Font f = new Font(fontNameComboBox.getSelectedItem().toString(), style, size);
      sampleLabel.setFont(f);
      sampleLabel.repaint();
    }
    catch (Exception ex)
    {
      
    }
  }
  
  public static Font stringToFont(String fontDesc)
  {
    Font f = null;
    if (fontDesc != null && fontDesc.trim().length() > 0)  
    {
      String[] selem = fontDesc.split(",");
      if (selem.length == 4)
      {
        String name = selem[0].trim();
        String size = selem[1].trim();
        Boolean italic = Boolean.valueOf(selem[2].trim().equals("italic"));
        Boolean bold = Boolean.valueOf(selem[3].trim().equals("bold"));

        int style = 0;
        if (italic.booleanValue())
          style |= Font.ITALIC;
        if (bold.booleanValue())
          style |= Font.BOLD;
        int s = Integer.parseInt(size);
        f = new Font(name, style, s);
      }
    }
//  System.out.println("[" + fontDesc + "] becomes " + f.toString());
    return f;    
  }

  public static String fontToString(Font f)
  {
    String str = "";
    if (f != null)  
    {
      str += f.getName();
      str += (", " + Integer.toString(f.getSize()==0?12:f.getSize())); // WEIRD
      str += (", " + ((f.getStyle() & Font.ITALIC) != 0 ? "italic" : ""));
      str += (", " + ((f.getStyle() & Font.BOLD) != 0 ? "bold" : ""));
    }
//  System.out.println(f.toString() + " becomes [" + str + "]");
    return str;    
  }
}
