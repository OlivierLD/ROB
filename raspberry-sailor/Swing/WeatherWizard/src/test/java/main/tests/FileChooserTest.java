package main.tests;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class FileChooserTest {

    public static void main(String... args) {

//        System.setProperty("apple.awt.fileDialogForDirectories", "true");

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

//        int returnValue = jfc.showOpenDialog(null); // "FileName" field missing on Mac...
        int returnValue = jfc.showSaveDialog(null); // "FileName" shows up.

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
        } else {
            System.out.println("Canceled, or error.");
        }
    }
}
