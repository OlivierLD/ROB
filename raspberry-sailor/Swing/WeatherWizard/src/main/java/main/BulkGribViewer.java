package main;

import chartview.util.WWGnlUtilities;
import chartview.util.grib.JGribBulkViewer;

import javax.swing.*;

/**
 * Display GRIB's raw data, like a spreadsheet
 */
public class BulkGribViewer {
    public static void main(String... args) throws Exception {
        String fName = WWGnlUtilities.chooseFile(null,
                JFileChooser.FILES_ONLY,
                new String[]{"grb", "grib"},
                "GRIB files",
                ".",
                WWGnlUtilities.SaveOrOpen.OPEN,
                "Open",
                "Choose the GRIB File to Open",
                false);
        if (fName.trim().length() > 0) {
            // new JGribBulkViewer(".../2008_10_07_newdata.grb");
            new JGribBulkViewer(fName);
        }
    }
}
