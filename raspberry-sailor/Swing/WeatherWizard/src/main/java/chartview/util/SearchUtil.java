package chartview.util;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import coreutilities.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class SearchUtil
{
  private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));
  /**
   * Returns the List containing all the strings (file names) matching the pattern, starting from the startPath directory
   * 
   * @param pattern
   * @param startPath
   * @return
   * @throws Exception
   */
  public static List<String> findMatchingFiles(String pattern, String startPath) throws Exception
  {
    List<String> ret = null;
    final Pattern p = Pattern.compile(pattern);

    FilenameFilter fnf = new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          File f = new File(dir, name);
          boolean accept = false;
          if (f.isDirectory())
            accept = true;
          else
          {
            Matcher m = p.matcher(name);
            if (m.find())
            {
              accept = true;
            }
          }
//        System.out.println("name [" + name + "] is " + (accept?"":"not ") + "matching");
          return accept;
        }
      };
    
    String[] pathElements = startPath.split(File.pathSeparator);
    if (pathElements != null)
    {
      ret = new ArrayList<String>();
      for (int i=0; i<pathElements.length; i++)
      {
//      System.out.println("Scanning " + pathElements[i]);
        File root = new File(pathElements[i]);
        ret = recurseDirectory(root, ret, fnf);
      }
    }
    return ret;
  }

  private static List<String> recurseDirectory(File dir, List<String> list, FilenameFilter filter)
  {
    if (dir.isDirectory())
    {
      File[] fa = dir.listFiles(filter);
      for (int i=0; i<fa.length; i++)
        list = recurseDirectory(fa[i], list, filter);
    }
    else
      list.add(dir.getAbsolutePath());
    return list;
  }
 
  /**
   * Returns the most recent file matching a given pattern within the startpath directory and all its sub-directories
   * 
   * @param pattern
   * @param startpath
   * @return
   * @throws Exception
   */
  public static String findMostRecentFile(String pattern, String startpath) throws Exception
  {
    String ret = null;
    List<String> mf = findMatchingFiles(pattern, startpath);
    long mostRecent = 0L;
    
    for (String f : mf)
    {
      File file = new File(f);
      if (file.lastModified() > mostRecent)
      {
        mostRecent = file.lastModified();
        ret = f;
      }
    }        
    return ret;
  }
  
  public static String findMostRecentFax(String pattern, String startPath) throws Exception
  {
    String ret = null;
    List<String> mf = findMatchingSailmailFax(pattern, startPath);
    long mostRecent = 0L;
    
    for (String f : mf)
    {
      File file = new File(f);
      if (file.lastModified() > mostRecent)
      {
        mostRecent = file.lastModified();
        ret = f;
      }
    }       
    if (ret != null)
      ret = Utilities.replaceString(ret, HEADER_EXTENSION, FAX_EXTENSION);
    System.out.println("findMostRecentFax found [" + ret + "] for [" + pattern + "], from [" + startPath + "]");
    return ret;
  }
  
  private final static String HEADER_EXTENSION = ".hdr";
  private final static String FAX_EXTENSION    = ".tif";
  
  public static List<String> findMatchingSailmailFax(String pattern, String startPath) throws Exception
  {
    List<String> ret = null;
    final Pattern p = Pattern.compile(pattern);

    FilenameFilter fnf = new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          File f = new File(dir, name);
          boolean accept = false;
          if (f.isDirectory())
            accept = true;
          else
          {
            try
            {
//            System.out.println("Name:" + name);
              if (name.endsWith(HEADER_EXTENSION))
              {
                Properties props = new Properties();
                props.load(new FileInputStream(f));
                String comment = props.getProperty("Comment", "");
                Matcher m = p.matcher(comment);
                if (m.find())
                {
                  accept = true;
                }
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
  //        System.out.println("name [" + name + "] is " + (accept?"":"not ") + "matching");
          return accept;
        }
      };
    
    String[] pathElements = startPath.split(File.pathSeparator);
    if (pathElements != null)
    {
      ret = new ArrayList<String>();
      for (int i=0; i<pathElements.length; i++)
      {
//      System.out.println("Scanning " + pathElements[i]);
        File root = new File(pathElements[i]);
        ret = recurseDirectory(root, ret, fnf);
      }
    }
    return ret;
  }
  
  /**
   * The heart of the dynamic search
   */
  public final static String SEARCH_PROTOCOL = "search:";
  public static String dynamicSearch(String searchString) throws Exception
  {
    String faxName = "";
    if (searchString.startsWith(SEARCH_PROTOCOL))
    {
      String javaString = searchString.substring(SEARCH_PROTOCOL.length());
      if (verbose)
        System.out.println("serachString:" + javaString);
      String beforeParenthesis = javaString.substring(0, javaString.indexOf("("));
      // get class name
      String className = beforeParenthesis.substring(0, beforeParenthesis.lastIndexOf("."));
      if (verbose)
        System.out.println("Class:" + className);
      // get method name
      String methodName = beforeParenthesis.substring(beforeParenthesis.lastIndexOf(".") + 1).trim();
      if (verbose)
        System.out.println("Method:" + methodName);
      // get the parameters
      String prms = javaString.substring(javaString.indexOf("(") + 1, javaString.lastIndexOf(")"));
      if (verbose)
        System.out.println("Prms:" + prms);
      String[] prm = prms.split(",");
      Class c = Class.forName(className);
  //  Object o = c.newInstance(); // No instance required, static method.
//    Class<?>[] parameterTypes = new Class<?>[2];
//    parameterTypes[0] = String.class;
//    parameterTypes[1] = String.class;
      @SuppressWarnings("unchecked")
      Method method = c.getDeclaredMethod(methodName, (Class<?>)String.class, (Class<?>)String.class); // Assume 2 String prms. TODO Make it more flexible.
//    Method method = c.getDeclaredMethod(methodName, parameterTypes); // Assume 2 String prms.
      Object[] argList = new Object[prm.length];
      for (int i=0; i<prm.length; i++)
      {
        String s = prm[i].trim();
        if (s.startsWith("\""))
          s = s.substring(1);
        if (s.endsWith("\""))
          s = s.substring(0, s.length() - 1);
        s = substitute(s);
        if (verbose)
          System.out.println("Prm #" + Integer.toString(i+1) + ":[" + s + "]");
        argList[i] = s;
      }
      try
      {
        Object ret = method.invoke(null, argList);
        if (ret instanceof String)
          faxName = (String)ret;
        else
        {
          System.out.println("Returned a " + (ret==null?"null":ret.getClass().getName()));
        }
      }
      catch (Exception ex)
      {
        String errMess = "dynamicSearch failure for [" + searchString + "]\n";
        errMess += (ex.toString() + "\n");        
        errMess += ("Class Name :" + className + "\n");
        errMess += ("Method Name:" + methodName + "\n");
        errMess += ("Prms:" + "\n");
        for (int i=0; i<argList.length; i++)
          errMess += ("-> " + argList[i].toString() + "\n");
        System.err.println(errMess);
        JOptionPane.showMessageDialog(null, errMess, "Dynamic Invocation", JOptionPane.ERROR_MESSAGE);
      }
    }        
    return faxName;
  }
  
  private static String substitute(String str)
  {
    String ret = str;
    if ("${fax.path}".equals(ret))
    {
      ret = ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX].toString();
    }
    else if ("${grib.path}".equals(ret))
    {
      ret = ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX].toString();
    }
    // else if, else...
    
    return ret;
  }

  public static void main(String... args)
  {
    new ParamPanel(); // For substitution
    verbose = true;
    try
    {
//    String st = "search:chartview.util.SearchUtil.findMostRecentFax(\".*NMC.*\", \"" + "C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\Faxfiles;C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2009;C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2010" + "\")";
//    String st = "search:chartview.util.SearchUtil.findMostRecentFax(\".*500.*\", \"" + "${fax.path}" + "\")";
      String st = "search:chartview.util.SearchUtil.findMostRecentFile(\".*\\.(grb|grib)$\", \"" + "${grib.path}" + "\")";
      String doc = dynamicSearch(st);
      System.out.println("Dynamically found document:" + doc);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      System.out.println("Done.");
    }

  }

  public static void main2(String[] args)
  {
    try
    {
      String p = ".*2009-(0[8-9]|1[0-2])-.*AllPacific\\.waz$";
      String dir = "C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\composites";
      
      List<String> al = findMatchingFiles(p, dir);
      System.out.println("Found " + al.size() + " waz files.");
      
      System.out.println("Most recent WAZ: " + findMostRecentFile(p, dir));
      
//    p = ".*Tropical Surface Analysis";
//    p = ".*Final Surface Analysis \\(Part 2 - NW Pac\\)";
//    p = ".*Final Surface Analysis \\(Part 1 - NE Pac\\)";
//    p = ".*Final Surface Analysis \\(Part [12] - N[EW] Pac\\)";
      p = ".*NMC.*";
      dir = "C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\Faxfiles;C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2009;C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\WeatherFaxes\\2010";
      al = findMatchingSailmailFax(p, dir);
      System.out.println("Found " + al.size() + " fax files.");
      for (String s : al)
      {
        String imgName = "";
        if (false)
        {
          Properties props = new Properties();
          props.load(new FileInputStream(s));
          props.list(System.out);        
          imgName = props.getProperty("FileName", "[unknown]"); // Where are the slashes??!
        }
        else
          // Derive the image name from the header name. More portable
          imgName = Utilities.replaceString(s, HEADER_EXTENSION, FAX_EXTENSION);
        System.out.println("=>" + s + ":" + imgName);
      }
      String fax = findMostRecentFax(p, dir);
      System.out.println("Most Recent Matching Fax: " + fax);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
