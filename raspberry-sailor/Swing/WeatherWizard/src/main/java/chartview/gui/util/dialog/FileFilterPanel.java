package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class FileFilterPanel
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel topLabel = new JLabel();
  private JTextField filterTextField = new JTextField();
  private JRadioButton containsRadioButton = new JRadioButton();
  private JRadioButton regExpRadioButton = new JRadioButton();
  private ButtonGroup group = new ButtonGroup();
  private ButtonGroup groupComment = new ButtonGroup();
  private JCheckBox commentCheckBox = new JCheckBox();
  private JLabel commentLabel = new JLabel();
  private JTextField filterCommentTextField = new JTextField();
  private JRadioButton commentContainsRadioButton = new JRadioButton();
  private JRadioButton commentRegexpRadioButton = new JRadioButton();

  private boolean withCommentFilter = false;

  public FileFilterPanel()
  {
    this(false);
  }
  
  public FileFilterPanel(boolean b)
  {
    this.withCommentFilter = b;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(gridBagLayout1);
    topLabel.setText(WWGnlUtilities.buildMessage("select-contains"));
    containsRadioButton.setText(WWGnlUtilities.buildMessage("contains"));
    regExpRadioButton.setText(WWGnlUtilities.buildMessage("reg-expr"));
    regExpRadioButton.setToolTipText(WWGnlUtilities.buildMessage("reg-expr-tt"));
    regExpRadioButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            regExpRadioButton_actionPerformed(e);
          }
        });
    if (this.withCommentFilter)
    {
      commentCheckBox.setText("Restriction on Comment");
      commentCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          commentCheckBox_actionPerformed(e);
        }
      });
      commentLabel.setText(WWGnlUtilities.buildMessage("select-comment-contains"));
      commentLabel.setEnabled(false);
      filterCommentTextField.setEnabled(false);
      commentContainsRadioButton.setText(WWGnlUtilities.buildMessage("contains"));
      commentRegexpRadioButton.setText(WWGnlUtilities.buildMessage("reg-expr"));
      commentRegexpRadioButton.setEnabled(false);
      group.add(containsRadioButton);
      group.add(regExpRadioButton);
      containsRadioButton.setSelected(true);
      groupComment.add(commentContainsRadioButton);
      groupComment.add(commentRegexpRadioButton);
      commentContainsRadioButton.setSelected(true);
  
      commentContainsRadioButton.setEnabled(false);
      containsRadioButton.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              containsRadioButton_actionPerformed(e);
            }
          });
    }
    this.add(topLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(filterTextField, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(containsRadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    this.add(regExpRadioButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));

    if (this.withCommentFilter)
    {
      this.add(commentCheckBox,
               new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                      new Insets(10, 0, 0, 0), 0, 0));
      this.add(commentLabel,
               new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                      new Insets(0, 0, 0, 0), 0, 0));
      this.add(filterCommentTextField,
               new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                      new Insets(0, 0, 0, 0), 0, 0));
      this.add(commentContainsRadioButton,
               new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                      new Insets(0, 0, 0, 0), 0, 0));
      this.add(commentRegexpRadioButton,
               new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                      new Insets(0, 0, 0, 0), 0, 0));
    }
  }
  
  public String getFilter()
  { return filterTextField.getText(); }

  private void containsRadioButton_actionPerformed(ActionEvent e)
  {
    topLabel.setText(WWGnlUtilities.buildMessage("select-contains"));
  }

  private void regExpRadioButton_actionPerformed(ActionEvent e)
  {
    topLabel.setText(WWGnlUtilities.buildMessage("select-matches"));
  }
  
  public boolean isRegExpr()
  {
    return regExpRadioButton.isSelected();
  }

  private void commentCheckBox_actionPerformed(ActionEvent e)
  {
    enableCommentFilter(commentCheckBox.isSelected());
  }
  
  private void enableCommentFilter(boolean b)
  {
    commentLabel.setEnabled(b);
    filterCommentTextField.setEnabled(b);
    commentContainsRadioButton.setEnabled(b);
    commentRegexpRadioButton.setEnabled(b);    
  }
  
  public boolean filterOnComment()
  {
    return commentCheckBox.isSelected();
  }
      
  public boolean isCommentRegExp()
  {
    return commentRegexpRadioButton.isSelected();
  }
  
  public String getCommentFilter()
  {
    return filterCommentTextField.getText();
  }
}