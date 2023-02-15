package util;

import java.awt.Component;
import java.io.File;

import java.text.SimpleDateFormat;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JFileChooser;

public class GnlUtilities
{
  public static final SimpleDateFormat SDF      = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S_z");

  public static String chooseFile(Component parent, 
                                  int mode, 
                                  String[] flt, 
                                  String desc, 
                                  String where, 
                                  String buttonLabel, 
                                  String dialogLabel)
  {
    String fileName = "";
    JFileChooser chooser = new JFileChooser();
    ToolFileFilter filter = new ToolFileFilter(flt, desc);
    chooser.addChoosableFileFilter(filter);
    chooser.setFileFilter(filter);
    chooser.setFileSelectionMode(mode);

    if (buttonLabel != null)
      chooser.setApproveButtonText(buttonLabel);
    if (dialogLabel != null)
      chooser.setDialogTitle(dialogLabel);

    File ff = new File(where);
    if (ff.isDirectory())
      chooser.setCurrentDirectory(ff);
    else
    {
      File f = new File(".");
      String currPath = f.getAbsolutePath();
      f = new File(currPath.substring(0, currPath.lastIndexOf(File.separator)));
      chooser.setCurrentDirectory(f);
    }
    int retval = chooser.showOpenDialog(parent);
    switch (retval)
    {
      case JFileChooser.APPROVE_OPTION:
        fileName = chooser.getSelectedFile().toString();
        break;
      case JFileChooser.CANCEL_OPTION:
        break;
      case JFileChooser.ERROR_OPTION:
        break;
    }
    return fileName;
  }  
  public static class ToolFileFilter extends javax.swing.filechooser.FileFilter
  {

    public boolean accept(File f)
    {
      if (f != null)
      {
        if (f.isDirectory())
          return true;
        String extension = getExtension(f);
        if (filters == null)
          return true;
        if (extension != null && filters.get(getExtension(f)) != null)
          return true;
      }
      return false;
    }

    public String getExtension(File f)
    {
      if(f != null)
      {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if(i > 0 && i < filename.length() - 1)
          return filename.substring(i + 1).toLowerCase();
      }
      return null;
    }

    public void addExtension(String extension)
    {
      if (filters == null)
        filters = new Hashtable<String, Object>(5);
      filters.put(extension.toLowerCase(), this);
      fullDescription = null;
    }

    public String getDescription()
    {
      if (fullDescription == null)
      {
        if(description == null || isExtensionListInDescription())
        {
          if (description != null)
            fullDescription = description;
          if (filters != null)
          {
            fullDescription += " (";
            Enumeration extensions = filters.keys();
            if (extensions != null)
              for (fullDescription += "." + (String)extensions.nextElement(); extensions.hasMoreElements(); fullDescription += ", " + (String)extensions.nextElement());
            fullDescription += ")";
          }
          else
            fullDescription = "";
        } 
        else
        {
          fullDescription = description;
        }
      }
      return fullDescription;
    }

    public void setDescription(String description)
    {
      this.description = description;
      fullDescription = null;
    }

    public void setExtensionListInDescription(boolean b)
    {
      useExtensionsInDescription = b;
      fullDescription = null;
    }

    public boolean isExtensionListInDescription()
    {
      return useExtensionsInDescription;
    }

    private String TYPE_UNKNOWN;
    private String HIDDEN_FILE;
    private Hashtable<String, Object> filters;
    private String description;
    private String fullDescription;
    private boolean useExtensionsInDescription;

    public ToolFileFilter()
    {
      this((String)null, null);
    }

    public ToolFileFilter(String extension)
    {
      this(extension, null);
    }

    public ToolFileFilter(String extension, String description)
    {
      this(new String[] { extension }, description);
    }

    public ToolFileFilter(String filters[])
    {
      this(filters, null);
    }

    public ToolFileFilter(String filter[], String description)
    {
      TYPE_UNKNOWN = "Type Unknown";
      HIDDEN_FILE = "Hidden File";
      this.filters = null;
      this.description = null;
      fullDescription = null;
      useExtensionsInDescription = true;
      if (filter != null)
      {
        this.filters = new Hashtable<String, Object>(filter.length);
        for (int i = 0; i < filter.length; i++)
          addExtension(filter[i]);
      }
      setDescription(description);
    }
  }
}
