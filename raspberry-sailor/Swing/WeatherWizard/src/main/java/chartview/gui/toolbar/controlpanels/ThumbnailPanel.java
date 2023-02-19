package chartview.gui.toolbar.controlpanels;


import chart.components.ui.ChartPanel;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;


public class ThumbnailPanel
        extends JPanel
        implements MouseListener,
        MouseMotionListener {
    private final BorderLayout borderLayout = new BorderLayout();
    private final JPanel instance = this;

    private transient Rectangle2D visibleZone = null;
    private transient Rectangle2D fullChartView = null;
    private Cursor cursorInZone = null;
    private Cursor cusorDraggingInZone = null;
    private boolean dragged = false;
    private int draggedFromX = -1, draggedFromY = -1;
    private double imgRatio = 1d;

    public ThumbnailPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout);
        this.setBackground(Color.white);

        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 200));
        this.setMinimumSize(new Dimension(ControlPane.WIDTH, 200));
        this.setSize(new Dimension(ControlPane.WIDTH, 200));
        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from ThumnailPanel.";
            }

            public void chartRepaint() {
                try {
                    refreshThumbnail();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        String imgFileName = "PanOpenHand32x32.png";
        Image image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        cursorInZone = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);
        imgFileName = "PanClosedHand32x32.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        cusorDraggingInZone = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);

    }

    public void paintComponent(Graphics gr) {
        Graphics2D g2d = (Graphics2D) gr;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        //  ((AdjustFrame)WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanelScrollPane().getViewportBorderBounds();
        ChartPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel();
        RenderedImage ri = cp.createChartImage(cp.getWidth(), cp.getHeight());
        //  System.out.println("ri is a " + ri.getClass().getName());

        final Image tnImg = (BufferedImage) ri;
        final int w = tnImg.getWidth(null);
        final int h = tnImg.getHeight(null);
        double wFact = w / instance.getSize().getWidth();
        double hFact = h / instance.getSize().getHeight();
        imgRatio = Math.max(wFact, hFact);
        final AffineTransform tx = new AffineTransform();
        double _tx = (instance.getSize().getWidth() - (w / imgRatio)) / 2d;
        double _ty = (instance.getSize().getHeight() - (h / imgRatio)) / 2d;
//  System.out.println("Translation:" + _tx + ", " + _ty);    
        tx.translate(_tx, _ty);
        tx.scale(1 / imgRatio,
                1 / imgRatio);

//  Dimension dim = new Dimension((int)(w / imgRatio), (int)(h / imgRatio));
//  this.setPreferredSize(dim);
        g2d.drawImage(tnImg, tx, this);

//  g2d.setColor(Color.blue);
//  g2d.drawRect((int)_tx, (int)_ty, (int)(w / imgRatio), (int)(h / imgRatio));

        // The actual view on the chart
        Point topLeft = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect().getLocation();
        Rectangle rect = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect();
//    g2d.setColor(Color.blue);
//    g2d.drawRect((int)(_tx + (topLeft.x / imgRatio)), 
//                 (int)(_ty + (topLeft.y / imgRatio)),
//                 (int)(rect.width / imgRatio), 
//                 (int)(rect.height / imgRatio));
        Shape big, small;
        big = new Rectangle2D.Double(0, 0, instance.getSize().getWidth(), instance.getSize().getHeight());
        small = new Rectangle2D.Double((_tx + (topLeft.x / imgRatio)), (_ty + (topLeft.y / imgRatio)), (rect.width / imgRatio), (rect.height / imgRatio));

//  g2d.setColor(Color.red);
//  g2d.drawRect((int)(_tx + (topLeft.x / imgRatio)), (int)(_ty + (topLeft.y / imgRatio)), (int)(rect.width / imgRatio), (int)(rect.height / imgRatio));

        visibleZone = new Rectangle2D.Double((_tx + (topLeft.x / imgRatio)), (_ty + (topLeft.y / imgRatio)), (rect.width / imgRatio), (rect.height / imgRatio));
        fullChartView = new Rectangle2D.Double(_tx, _ty, (w / imgRatio), (h / imgRatio));

        Area bigArea = new Area(big);
        Area smallArea = new Area(small);
        bigArea.exclusiveOr(smallArea);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setPaint(Color.gray);
        g2d.fill(bigArea);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private long lastRefresh = 0L;

    private void refreshThumbnail() {
        refreshThumbnail(false);
    }

    private void refreshThumbnail(boolean force) {
        if (force || (!force && System.currentTimeMillis() - lastRefresh > 1_000L)) { // No more than once every second
            lastRefresh = System.currentTimeMillis();
            repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (visibleZone != null) {
            if (visibleZone.contains(new Point2D.Double(e.getX(), e.getY()))) {
                this.setCursor(cusorDraggingInZone);
                dragged = true;
                draggedFromX = e.getX();
                draggedFromY = e.getY();
            } else
                mouseMoved(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        dragged = false;
        mouseMoved(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (visibleZone != null) {
            if ( /* visibleZone.contains(new Point2D.Double(e.getX(), e.getY())) && */ dragged) {
                int x = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect().x + (int) ((e.getX() - draggedFromX) * imgRatio);
                int y = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect().y + (int) ((e.getY() - draggedFromY) * imgRatio);
                ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().scrollRectToVisible(new Rectangle(x,
                        y,
                        ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect().width,
                        ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().getVisibleRect().height));
                ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getChartPanel().repaint();
                try {
                    refreshThumbnail(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else
                mouseMoved(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (visibleZone != null) {
            if (visibleZone.contains(new Point2D.Double(e.getX(), e.getY()))) {
                this.setCursor(cursorInZone);
            } else {
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}
