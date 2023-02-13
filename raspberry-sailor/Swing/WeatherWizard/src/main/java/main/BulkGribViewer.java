package main;

import chartview.util.WWGnlUtilities;
import chartview.util.grib.JGribBulkViewer;

import javax.swing.JFileChooser;

public class BulkGribViewer
{
  public static void main(String... args) throws Exception
  {
    String fName = WWGnlUtilities.chooseFile(null, 
                                           JFileChooser.FILES_ONLY, 
                                           new String[] { "grb", "grib" }, 
                                           "GRIB files", 
                                           ".", 
                                           "Open", 
                                           "Choose the GRIB File");
    if (fName.trim().length() > 0)
    {
//    new JGribBulkViewer("C:\\_myWork\\_ForExport\\dev-corner\\olivsoft\\all-scripts\\GRIBFiles\\GRIB.4.test\\2008_10_07_newdata.grb");
      new JGribBulkViewer(fName);
    }
  }
}
