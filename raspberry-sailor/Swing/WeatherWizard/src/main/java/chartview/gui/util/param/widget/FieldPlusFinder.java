package chartview.gui.util.param.widget;

import chartview.gui.util.dialog.FaxType;
import chartview.gui.util.param.ParamPanel;

import chartview.ctx.WWContext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class FieldPlusFinder 
              extends JPanel 
{
  BorderLayout borderLayout1 = new BorderLayout();
  JButton finderButton = new JButton();
  JTextField textField = new JTextField();
  
  Object value;

  public FieldPlusFinder(Object o)
  {
    value = o;
    textField.setText(o.toString());
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.setSize(new Dimension(181, 29));
    this.setLayout(borderLayout1);
    finderButton.setText("...");
    finderButton.setPreferredSize(new Dimension(30, 20));
    finderButton.setMinimumSize(new Dimension(30, 20));
    finderButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          finderButton_actionPerformed(e);
        }
      });
    textField.setHorizontalAlignment(JTextField.RIGHT);
    this.add(finderButton, BorderLayout.EAST);
    this.add(textField, BorderLayout.CENTER);
  }

  public JTextField getTextField()
  { return this.textField; }
  public JButton getButton()
  { return this.finderButton; }

  protected abstract Object invokeEditor();

  protected void finderButton_actionPerformed(ActionEvent e)
  {
//  String origValue = this.textField.getText();
    Object o = invokeEditor();
    if (o instanceof String)
    {
      String str = (String)o;
      if (str != null && str.length() > 0)
      {
        this.textField.setText(str);
        if (value instanceof ParamPanel.DataFile)
        {
          ((ParamPanel.DataFile)value).setValue(str);
        }
        else if (value instanceof ParamPanel.DataDirectory)
        {
          ((ParamPanel.DataDirectory)value).setValue(str);
        }
        else if (value instanceof FaxType)
        {
          ((FaxType)value).setValue(str);
        }
        else
          value = o;
      }
    }
    else if (o instanceof Color)
    {
      this.value = /*(Color)*/o;
    }
    else if (o instanceof FaxType)
    {
      this.textField.setText(((FaxType)o).getValue());
      this.textField.setForeground(((FaxType)o).getColor());
      this.value = /*(FaxType)*/o;
    }
    else if (o != null)
      WWContext.getInstance().fireLogging("FieldPlusFinder, value, not managed, is a " + o.getClass().getName());
    else
      WWContext.getInstance().fireLogging("FieldPlusFinder, value is null");
  }
  
  public Object getValue()
  { return value; }
}