package chartview.gui.util.dialog;

import chartview.gui.util.param.widget.FieldPlusFinder;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;


public class FieldPlusButtonCellEditor 
     extends FieldPlusFinder 
{
  private transient Object object;
  public FieldPlusButtonCellEditor(Object o)
  {
    super(o);
    this.object = o;
  }

  protected Object invokeEditor()
  {
    String s = (String)object;
    TextAreaPanel ta = new TextAreaPanel();
    ta.setText(s);
    int resp = JOptionPane.showConfirmDialog(this, ta, "Edit...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (resp == JOptionPane.OK_OPTION)  
    {
      s = ta.getText();
      object = s;
    }
    return object;
  }
  
  protected Object getCellEditorValue()
  {
    return object;
  }
}
