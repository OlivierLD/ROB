package main.splash;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;
import main.help.AboutBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;


public class SplashWindow extends JWindow {
    private static SplashWindow instance;
    private final transient Image image;
    private boolean paintCalled = false;

    //private JLabel jLabel1 = new JLabel();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel copyrightLabel = new JLabel();
    //private JLabel toolLabel = new JLabel();
    private final JProgressBar loadProgressBar = new JProgressBar();
    private final JLabel loadingLabel = new JLabel();

    private final JLabel nbFaxes = new JLabel("0");
    private final JLabel nbGRIBs = new JLabel("0");
    private final JLabel nbComposites = new JLabel("0");
    private final JLabel nbPatterns = new JLabel("0");

    private int loadedFaxes = 0;
    private int loadedGRIBs = 0;
    private int loadedComposites = 0;
    private int loadedPatterns = 0;

    private final static int H = 300;
    private final static int W = 325;

    private transient static ApplicationEventListener ael = null;

    /**
     * Creates a new instance.
     *
     * @param parent the parent of the window.
     * @param image  the splash image.
     */
    private SplashWindow(Frame parent, Image image) {
        super(parent);
        this.image = image;

        // Load the image
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
        }

        // Abort on failure
        if (mt.isErrorID(0)) {
            setSize(0, 0);
            System.err.println("Warning: SplashWindow couldn't load splash image.");
            synchronized (this) {
                paintCalled = true;
                notifyAll();
            }
            return;
        }

        // Center the window on the screen
//  int imgWidth = image.getWidth(this);
//  int imgHeight = image.getHeight(this);
//  setSize(imgWidth, imgHeight);
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dim = new Dimension(W, H);
        this.setSize(dim);
        setLocation((screenDim.width - dim.width) / 2,
                (screenDim.height - dim.height) / 2);
        JLayeredPane layer = new JLayeredPane();

//  ImageIcon img = new ImageIcon(this.getClass().getResource("LogiSail.png"));    
        ImageIcon img = new ImageIcon(AboutBox.class.getResource("wizard150.png"));
        JLabel imgHolder = new JLabel(img);
//  imgHolder.setBorder(BorderFactory.createLineBorder(new Color(228, 24, 106), 2));
        imgHolder.setBounds(0, 10, 325, 150);
        layer.add(imgHolder, JLayeredPane.DRAG_LAYER);

        JPanel itemHolder = new JPanel();
        itemHolder.setLayout(gridBagLayout1);
        itemHolder.setOpaque(false);

        // 169 = Copyright
        copyrightLabel.setText((char) 169 + " " + WWGnlUtilities.buildMessage("copyright"));
        copyrightLabel.setForeground(Color.red);

        loadingLabel.setText(WWGnlUtilities.buildMessage("splash-loading"));
        loadingLabel.setForeground(Color.red);
        itemHolder.add(loadingLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(140, 0, 0, 0), 0, 0));

        loadProgressBar.setIndeterminate(true);
        itemHolder.add(loadProgressBar, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        itemHolder.add(copyrightLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        JLabel faxLabel = new JLabel(WWGnlUtilities.buildMessage("splash-faxes"));
        faxLabel.setForeground(Color.red);
        faxLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(faxLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
        nbFaxes.setForeground(Color.red);
        nbFaxes.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(nbFaxes, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));

        JLabel gribLabel = new JLabel(WWGnlUtilities.buildMessage("splash-grib"));
        gribLabel.setForeground(Color.red);
        gribLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(gribLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        nbGRIBs.setForeground(Color.red);
        nbGRIBs.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(nbGRIBs, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        JLabel compositeLabel = new JLabel(WWGnlUtilities.buildMessage("splash-composites"));
        compositeLabel.setForeground(Color.red);
        compositeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(compositeLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        nbComposites.setForeground(Color.red);
        nbComposites.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(nbComposites, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        JLabel patternLabel = new JLabel(WWGnlUtilities.buildMessage("splash-patterns"));
        patternLabel.setForeground(Color.red);
        patternLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(patternLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        nbPatterns.setForeground(Color.red);
        nbPatterns.setFont(new Font("Arial", Font.PLAIN, 12));
        itemHolder.add(nbPatterns, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        itemHolder.setBounds(0, 0, W, H);
        layer.add(itemHolder, JLayeredPane.PALETTE_LAYER);

        this.setContentPane(layer);

        // Users shall be able to close the splash window by
        // clicking on its display area. This mouse listener
        // listens for mouse clicks and disposes the splash window.
        MouseAdapter disposeOnClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Note: To avoid that method splash hangs, we
                // must set paintCalled to true and call notifyAll.
                // This is necessary because the mouse click may
                // occur before the contents of the window
                // has been painted.
                synchronized (SplashWindow.this) {
                    SplashWindow.this.paintCalled = true;
                    SplashWindow.this.notifyAll();
                    WWContext.getInstance().removeApplicationListener(ael);
                }
                dispose();
            }
        };
        addMouseListener(disposeOnClick);

        ael = new ApplicationEventListener() {
            public String toString() {
                return "from SplashWindow.";
            }

            public void readFax() {
                nbFaxes.setText(Integer.toString(++loadedFaxes));
            }

            public void readGRIB() {
                nbGRIBs.setText(Integer.toString(++loadedGRIBs));
            }

            public void readPattern() {
                nbPatterns.setText(Integer.toString(++loadedPatterns));
            }

            public void readComposite() {
                nbComposites.setText(Integer.toString(++loadedComposites));
            }
        };
        WWContext.getInstance().addApplicationListener(ael);
    }

    /**
     * Updates the display area of the window.
     */
    public void update(Graphics g) {
        // Note: Since the paint method is going to draw an
        // image that covers the complete area of the component we
        // do not fill the component with its background color
        // here. This avoids flickering.
        paint(g);
    }

    /**
     * Paints the image on the window.
     */
    public void paint(Graphics g) {
        super.paint(g);
        super.setBackground(Color.white);
        int w = this.getWidth();
        int h = this.getHeight();
        g.setColor(Color.RED);
        g.drawRoundRect(2, 2, w - 4, h - 4, 5, 5);
//  g.drawImage(image, 0, 0, this);

        // Notify method splash that the window
        // has been painted.
        // Note: To improve performance we do not enter
        // the synchronized block unless we have to.
        if (!paintCalled) {
            paintCalled = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Open's a splash window using the specified image.
     *
     * @param image The splash image.
     */
    public static void splash(Image image) {
        if (instance == null && image != null) {
            Frame f = new Frame();

            // Create the splash image
            instance = new SplashWindow(f, image);

            // Show the window.
            instance.setVisible(true); // .show();

            // Note: To make sure the user gets a chance to see the
            // splash window we wait until its paint method has been
            // called at least once by the AWT event dispatcher thread.
            // If more than one processor is available, we don't wait,
            // and maximize CPU throughput instead.
            if (!EventQueue.isDispatchThread() && Runtime.getRuntime().availableProcessors() == 1) {
                synchronized (instance) {
                    while (!instance.paintCalled) {
                        try {
                            instance.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Open's a splash window using the specified image.
     *
     * @param imageURL The url of the splash image.
     */
    public static void splash(URL imageURL) {
        if (imageURL != null) {
            splash(Toolkit.getDefaultToolkit().createImage(imageURL));
        }
    }

    /**
     * Closes the splash window.
     */
    public static void disposeSplash() {
        if (instance != null) {
            instance.getOwner().dispose();
            instance = null;
            WWContext.getInstance().removeApplicationListener(ael);
        }
    }

    /**
     * Invokes the main method of the provided class name.
     *
     * @param args the command line arguments
     */
    public static void invokeMain(String className, String[] args) {
        try {
            Class.forName(className).getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{args});
        } catch (Exception e) {
            InternalError error = new InternalError("Failed to invoke main method");
            error.initCause(e);
            throw error;
        }
    }
}