package chartview.gui.util.param.widget;

import chartview.util.WWGnlUtilities;

import javax.swing.JFileChooser;


public class FieldPlusFilePicker extends FieldPlusFinder
{
  String[] fileTypes = null;
  String fileDesc = "";
  public FieldPlusFilePicker(Object o, String[] sa, String s)
  {
    super(o);
    fileTypes = sa;
    fileDesc = s;
  }

  protected Object invokeEditor()
  {
    String s = WWGnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY, fileTypes, fileDesc, ".", "Save", "Choose File");   
    return s;
  }
}
