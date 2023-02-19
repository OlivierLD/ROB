package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class FileFilterPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel topLabel = new JLabel();
    private final JTextField filterTextField = new JTextField();
    private final JRadioButton containsRadioButton = new JRadioButton();
    private final JRadioButton regExpRadioButton = new JRadioButton();
    private final ButtonGroup group = new ButtonGroup();
    private final ButtonGroup groupComment = new ButtonGroup();
    private final JCheckBox commentCheckBox = new JCheckBox();
    private final JLabel commentLabel = new JLabel();
    private final JTextField filterCommentTextField = new JTextField();
    private final JRadioButton commentContainsRadioButton = new JRadioButton();
    private final JRadioButton commentRegexpRadioButton = new JRadioButton();

    private boolean withCommentFilter = false;

    public FileFilterPanel() {
        this(false);
    }

    public FileFilterPanel(boolean b) {
        this.withCommentFilter = b;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        topLabel.setText(WWGnlUtilities.buildMessage("select-contains"));
        containsRadioButton.setText(WWGnlUtilities.buildMessage("contains"));
        regExpRadioButton.setText(WWGnlUtilities.buildMessage("reg-expr"));
        regExpRadioButton.setToolTipText(WWGnlUtilities.buildMessage("reg-expr-tt"));
        regExpRadioButton.addActionListener(this::regExpRadioButton_actionPerformed);
        if (this.withCommentFilter) {
            commentCheckBox.setText("Restriction on Comment");
            commentCheckBox.addActionListener(this::commentCheckBox_actionPerformed);
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
            containsRadioButton.addActionListener(this::containsRadioButton_actionPerformed);
        }
        this.add(topLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(filterTextField, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(containsRadioButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(regExpRadioButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        if (this.withCommentFilter) {
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

    public String getFilter() {
        return filterTextField.getText();
    }

    private void containsRadioButton_actionPerformed(ActionEvent e) {
        topLabel.setText(WWGnlUtilities.buildMessage("select-contains"));
    }

    private void regExpRadioButton_actionPerformed(ActionEvent e) {
        topLabel.setText(WWGnlUtilities.buildMessage("select-matches"));
    }

    public boolean isRegExpr() {
        return regExpRadioButton.isSelected();
    }

    private void commentCheckBox_actionPerformed(ActionEvent e) {
        enableCommentFilter(commentCheckBox.isSelected());
    }

    private void enableCommentFilter(boolean b) {
        commentLabel.setEnabled(b);
        filterCommentTextField.setEnabled(b);
        commentContainsRadioButton.setEnabled(b);
        commentRegexpRadioButton.setEnabled(b);
    }

    public boolean filterOnComment() {
        return commentCheckBox.isSelected();
    }

    public boolean isCommentRegExp() {
        return commentRegexpRadioButton.isSelected();
    }

    public String getCommentFilter() {
        return filterCommentTextField.getText();
    }
}