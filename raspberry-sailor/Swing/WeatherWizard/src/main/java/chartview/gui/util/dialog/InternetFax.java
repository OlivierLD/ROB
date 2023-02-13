package chartview.gui.util.dialog;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.ArrayList;

import java.util.Iterator;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import oracle.xml.parser.v2.XMLParser;

import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class InternetFax extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JComboBox faxNameComboBox = new JComboBox();
  private JLabel jLabel3 = new JLabel();
  private JTextField faxURLTextField = new JTextField();
  
  private final static String DOWNLOADABLE_FAX_FILE_NAME = "config" + File.separator + "download.xml";
  
  List<DownloadableFax> faxes = null;
  private JLabel jLabel4 = new JLabel();
  private JButton browseButton = new JButton();
  private JTextField saveAsTextField = new JTextField();

  public InternetFax()
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
    this.setSize(new Dimension(469, 300));
    jLabel1.setText(WWGnlUtilities.buildMessage("select-fax"));
    jLabel1.setFont(new Font("Tahoma", 1, 11));
    jLabel2.setText(WWGnlUtilities.buildMessage("fax"));
    faxNameComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            faxNameComboBox_actionPerformed(e);
          }
        });
    jLabel3.setText(WWGnlUtilities.buildMessage("url"));
    jLabel4.setText(WWGnlUtilities.buildMessage("save-as"));
    browseButton.setText(WWGnlUtilities.buildMessage("browse"));
    browseButton.setToolTipText(WWGnlUtilities.buildMessage("where-to-download"));
    browseButton.setPreferredSize(new Dimension(80, 22));
    browseButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            browseButton_actionPerformed(e);
          }
        });
    this.add(jLabel1, 
             new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 10, 0), 0, 0));
    this.add(jLabel2, 
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 5, 0), 0, 0));
    this.add(faxNameComboBox, 
             new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(0, 0, 5, 0), 0, 0));
    this.add(jLabel3, 
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(faxURLTextField, 
             new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(0, 0, 0, 0), 0, 0));

    // Populate fax list                                    
    this.add(jLabel4, 
             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
                                    new Insets(10, 0, 0, 0), 0, 0));
    this.add(browseButton, 
             new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
                                    new Insets(10, 5, 0, 0), 0, 0));
    this.add(saveAsTextField, 
             new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
                                    new Insets(10, 0, 0, 0), 0, 0));
    File faxFile = new File(DOWNLOADABLE_FAX_FILE_NAME);
    if (!faxFile.exists())
      WWContext.getInstance().fireLogging(DOWNLOADABLE_FAX_FILE_NAME + " not found...\n");
    else
    {
      DOMParser parser = WWContext.getInstance().getParser();
      try
      {
        synchronized (parser)
        {
          parser.setValidationMode(XMLParser.NONVALIDATING);
          parser.parse(faxFile.toURI().toURL());      
          XMLDocument doc = parser.getDocument();
          NodeList nl = doc.selectNodes("/fax-collection/fax");
          for (int i=0; i<nl.getLength(); i++)
          {
            XMLElement elmt = (XMLElement)nl.item(i);
            String fName = elmt.getAttribute("name");
            String fUrl  = elmt.getAttribute("url");
            if (faxes == null)
              faxes = new ArrayList<DownloadableFax>(nl.getLength());
            faxes.add(new DownloadableFax(fName, fUrl));
          }
          // Populate fax list
          faxNameComboBox.removeAllItems();
          Iterator<DownloadableFax> fit = faxes.iterator();
          while (fit.hasNext())
          {
            faxNameComboBox.addItem(fit.next());  
          }
        }
      }
      catch (Exception ex)
      {
        WWContext.getInstance().fireExceptionLogging(ex);
        ex.printStackTrace();
      }
    }
  }

  private void faxNameComboBox_actionPerformed(ActionEvent e)
  {
    DownloadableFax df = (DownloadableFax)faxNameComboBox.getSelectedItem();
    faxURLTextField.setText(df.getUrl());
  }
  
  public String getFaxStrURL() { return faxURLTextField.getText(); }

  private void browseButton_actionPerformed(ActionEvent e)
  {
    String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
    String fName = WWGnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, 
                                            new String[] {"jpg", "png", "gif"}, 
                                            "Faxes", 
                                            firstDir,
                                            "Save", 
                                            "Save Fax as");
    if (fName.trim().length() > 0)
      saveAsTextField.setText(fName);    
  }

  public String getFaxLocalFile() { return saveAsTextField.getText(); }
  
  class DownloadableFax
  {
    String faxName = "";
    String faxUrl  = "";
    public DownloadableFax(String name, String url)
    {
      this.faxName = name;
      this.faxUrl  = url;
    }
    
    public String getName() { return faxName; }
    public String getUrl()  { return faxUrl; }
    
    public String toString() { return faxName; }
  }
}
