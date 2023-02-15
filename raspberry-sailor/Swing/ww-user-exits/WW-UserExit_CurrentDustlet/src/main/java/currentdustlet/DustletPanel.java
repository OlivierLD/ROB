package currentdustlet;

import weatherwizard.currentuserexits.GRIBCurrentDustlet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class DustletPanel
        extends JPanel {
    private GRIBCurrentDustlet parent;

    private int dustW, dustH;
    private int aw, ah, motes = 20; // 20 is not a valid value :)
    private String bgName = null;

    private BorderLayout borderLayout1 = new BorderLayout();
    private Dustlet dustlet = null;
    private JPanel topPanel = null;
    private JPanel leftPanel = null;
    private JPanel topLeftPanel = null;
    private JPanel leftTopPanel = null;
    private JButton forward = new JButton();
    private JButton backward = new JButton();
    private JButton animate = new JButton();
    private JLabel motesLabel = new JLabel("Motes");
    private JTextField motesTextField = new JTextField();
    private JLabel spinLabel = new JLabel("Current Factor");
    private JSpinner currentSizeSpinner = null;
    private int currentFactorForDustlet = 50;
    private JLabel speedLabel = new JLabel("Animation Rate (s)");
    private JSpinner dustletSpeedSpinner = null;
    private int speedForDustlet = 2;

    private JLabel dateLabel = new JLabel("Date goes here");

    public DustletPanel(GRIBCurrentDustlet from, String bgName, int w, int h, int aw, int ah, int motes, int value) {
        this.parent = from;
        motesTextField.setText(Integer.toString(motes));
        dustH = h;
        dustW = w;
        this.aw = aw;
        this.ah = ah;
        this.motes = motes;
        this.currentFactorForDustlet = value;
        this.bgName = bgName;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDustletDate(Date d) {
        setTitle(d.toString());
    }

    private void jbInit()
            throws Exception {
//  System.out.println("DustletPanel init");
        this.setLayout(borderLayout1);
        dustlet = new Dustlet(dustW, dustH, 1D /* refresh rate */, bgName, 01F, aw, ah, motes, null);
        topPanel = new JPanel();
        leftPanel = new JPanel();
        topLeftPanel = new JPanel();
        leftTopPanel = new JPanel();

        this.add(dustlet, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(leftPanel, BorderLayout.WEST);

        leftPanel.setLayout(new BorderLayout());
        topPanel.setLayout(new BorderLayout());
        topLeftPanel.setLayout(new GridBagLayout());
        leftPanel.add(topLeftPanel, BorderLayout.NORTH);
        leftTopPanel.setLayout(new GridBagLayout());
        topPanel.add(leftTopPanel, BorderLayout.WEST);

        forward.setIcon(new ImageIcon(this.getClass().getResource("panright.gif")));
        forward.setPreferredSize(new Dimension(24, 24));
        forward.setBorderPainted(false);
        forward.setMaximumSize(new Dimension(24, 24));
        forward.setMinimumSize(new Dimension(24, 24));
        forward.setMargin(new Insets(1, 1, 1, 1));
        forward.addActionListener(e -> forwardButton_actionPerformed());
        backward.setIcon(new ImageIcon(this.getClass().getResource("panleft.gif")));
        backward.setPreferredSize(new Dimension(24, 24));
        backward.setBorderPainted(false);
        backward.setMaximumSize(new Dimension(24, 24));
        backward.setMinimumSize(new Dimension(24, 24));
        backward.setMargin(new Insets(1, 1, 1, 1));
        backward.addActionListener(e -> backwardButton_actionPerformed());
        animate.setIcon(new ImageIcon(this.getClass().getResource("refresh.png")));
        animate.setPreferredSize(new Dimension(24, 24));
        animate.setBorderPainted(false);
        animate.setMaximumSize(new Dimension(24, 24));
        animate.setMinimumSize(new Dimension(24, 24));
        animate.setMargin(new Insets(1, 1, 1, 1));
        animate.addActionListener(e -> animateButton_actionPerformed());

        motesTextField.setPreferredSize(new Dimension(40, 20));
        motesTextField.setMinimumSize(new Dimension(40, 20));
        motesTextField.setToolTipText("Motes");
        motesTextField.setHorizontalAlignment(JTextField.RIGHT);
        motesTextField.setText(Integer.toString(motes));
        motesTextField.addActionListener(e -> {
            try {
                motes = Integer.parseInt(motesTextField.getText());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            dustlet.setMotes(motes);
        });

        SpinnerModel model = new SpinnerNumberModel(50, 1, 100, 1);
        currentSizeSpinner = new JSpinner(model);
        currentSizeSpinner.setMinimumSize(new Dimension(40, 20));
        currentSizeSpinner.setMaximumSize(new Dimension(40, 20));
        currentSizeSpinner.setPreferredSize(new Dimension(40, 20));
        currentSizeSpinner.setToolTipText("Current Factor");
        currentSizeSpinner.setValue(new Integer(currentFactorForDustlet));
        currentSizeSpinner.addChangeListener(evt -> {
            JSpinner spinner = (JSpinner) evt.getSource();
            // Get the new value
            Object value = spinner.getValue();
            //      System.out.println("Value is a " + value.getClass().getName());
            if (value instanceof Integer) {
                Integer d = (Integer) value;
                currentFactorForDustlet = d.intValue();
                parent.setWindScale(currentFactorForDustlet);
                dustlet.setMotes(motes);
            }
        });
        SpinnerModel speedModel = new SpinnerNumberModel(2, 1, 30, 1);
        dustletSpeedSpinner = new JSpinner(speedModel);
        dustletSpeedSpinner.setMinimumSize(new Dimension(40, 20));
        dustletSpeedSpinner.setMaximumSize(new Dimension(40, 20));
        dustletSpeedSpinner.setPreferredSize(new Dimension(40, 20));
        dustletSpeedSpinner.setToolTipText("Animation Rate (s)");
        dustletSpeedSpinner.setValue(new Integer(speedForDustlet));
        dustletSpeedSpinner.setFocusable(false);
        dustletSpeedSpinner.addChangeListener(evt -> {
            JSpinner spinner = (JSpinner) evt.getSource();
            // Get the new value
            Object value = spinner.getValue();
            //      System.out.println("Value is a " + value.getClass().getName());
            if (value instanceof Integer) {
                Integer d = (Integer) value;
                speedForDustlet = d.intValue();
                parent.setBetweenLoops(speedForDustlet);
            }
        });

        topLeftPanel.add(forward,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        topLeftPanel.add(backward,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        topLeftPanel.add(animate,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));

        leftTopPanel.add(motesLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        leftTopPanel.add(motesTextField,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        leftTopPanel.add(spinLabel,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        leftTopPanel.add(currentSizeSpinner,
                new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));

        leftTopPanel.add(speedLabel,
                new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        leftTopPanel.add(dustletSpeedSpinner,
                new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));

        leftTopPanel.add(dateLabel,
                new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));
        // System.out.println("DustletPanel initialized...");
    }

    private void setTitle(String str) {
        dateLabel.setText(str);
    }

    public void stop() {
        dustlet.stop();
    }

    public void start() {
        dustlet.start();
    }

    public void setMotes(int motes) {
        motesTextField.setText(Integer.toString(motes));
    }

    private void forwardButton_actionPerformed() {
        parent.forward();
    }

    private void backwardButton_actionPerformed() {
        parent.backward();
    }

    private boolean animationRunning = false;

    private void animateButton_actionPerformed() {
        if (animationRunning) {
            parent.stopAnimation();
            forward.setEnabled(true);
            backward.setEnabled(true);
        } else {
            parent.startAnimation(((Integer) dustletSpeedSpinner.getValue()).intValue());
            forward.setEnabled(false);
            backward.setEnabled(false);
        }
        animationRunning = !animationRunning;
    }
}
