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


public class InternetGRIB
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JComboBox gribNameComboBox = new JComboBox();
  private JLabel jLabel3 = new JLabel();
  private JTextField gribRequestTextField = new JTextField();

  private static final String DOWNLOADABLE_GRIB_FILE_NAME = "config" + File.separator + "grib.xml";

  List<InternetGRIB.DownloadableGRIB> gribs = null;
  private JLabel jLabel4 = new JLabel();
  private JButton browseButton = new JButton();
  private JTextField saveAsTextField = new JTextField();

  public InternetGRIB()
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
    jLabel1.setText(WWGnlUtilities.buildMessage("select-grib"));
    jLabel1.setFont(new Font("Tahoma", 1, 11));
    jLabel2.setText(WWGnlUtilities.buildMessage("grib"));
    gribNameComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            gribNameComboBox_actionPerformed(e);
          }
        });
    jLabel3.setText(WWGnlUtilities.buildMessage("grib-request-column"));
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
    this.add(jLabel1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 
          new Insets(0, 0, 10, 0), 0, 0));
    this.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
          new Insets(0, 0, 5, 0), 0, 0));
    this.add(gribNameComboBox, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
          new Insets(0, 0, 5, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(gribRequestTextField, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
          new Insets(0, 0, 0, 0), 0, 0));

    // Populate grib list                                    
    this.add(jLabel4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
          new Insets(10, 0, 0, 0), 0, 0));
    this.add(browseButton, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 
          new Insets(10, 5, 0, 0), 0, 0));
    this.add(saveAsTextField, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 
          new Insets(10, 0, 0, 0), 0, 0));
    File gribFile = new File(DOWNLOADABLE_GRIB_FILE_NAME);
    if (!gribFile.exists())
      WWContext.getInstance().fireLogging(DOWNLOADABLE_GRIB_FILE_NAME + " not found...\n");
    else
    {
      DOMParser parser = WWContext.getInstance().getParser();
      try
      {
        XMLDocument doc = null;
        synchronized (parser)
        {
          parser.setValidationMode(XMLParser.NONVALIDATING);
          parser.parse(gribFile.toURI().toURL());
          doc = parser.getDocument();
        }
        NodeList nl = doc.selectNodes("/grib-collection/grib-set/grib");
        for (int i = 0; i < nl.getLength(); i++)
        {
          XMLElement elmt = (XMLElement) nl.item(i);
          String gName = elmt.getAttribute("name");
          String gUrl = elmt.getAttribute("request");
          if (gribs == null)
            gribs = new ArrayList<InternetGRIB.DownloadableGRIB>(nl.getLength());
          gribs.add(new InternetGRIB.DownloadableGRIB(gName, gUrl));
        }
        // Populate grib list
        gribNameComboBox.removeAllItems();
        Iterator<InternetGRIB.DownloadableGRIB> fit = gribs.iterator();
        while (fit.hasNext())
        {
          gribNameComboBox.addItem(fit.next());
        }
      }
      catch (Exception ex)
      {
        WWContext.getInstance().fireExceptionLogging(ex);
        ex.printStackTrace();
      }
    }
  }

  private void gribNameComboBox_actionPerformed(ActionEvent e)
  {
    InternetGRIB.DownloadableGRIB df = (InternetGRIB.DownloadableGRIB) gribNameComboBox.getSelectedItem();
    gribRequestTextField.setText(df.getRequest());
  }

  public String getGRIBRequest()
  {
    return gribRequestTextField.getText();
  }

  private void browseButton_actionPerformed(ActionEvent e)
  {
    String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
    String fName = WWGnlUtilities.chooseFile(this, 
                                           JFileChooser.FILES_ONLY, 
                                           new String[] { "grb", "grib" }, 
                                           "GRIBs", 
                                           firstDir, 
                                           "Save", 
                                           "Save GRIB as");
    if (fName.trim().length() > 0)
      saveAsTextField.setText(fName);
  }

  public String getGRIBLocalFile()
  {
    return saveAsTextField.getText();
  }

  class DownloadableGRIB
  {
    String gribName = "";
    String gribRequest = "";

    public DownloadableGRIB(String name, String url)
    {
      this.gribName = name;
      this.gribRequest = url;
    }

    public String getName()
    {
      return gribName;
    }

    public String getRequest()
    {
      return gribRequest;
    }

    public String toString()
    {
      return gribName;
    }
  }
}
