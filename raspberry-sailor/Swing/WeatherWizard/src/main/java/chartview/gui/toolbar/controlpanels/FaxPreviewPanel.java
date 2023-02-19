package chartview.gui.toolbar.controlpanels;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;


public class FaxPreviewPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JPanel instance = this;
    private String imgName = "";

    private double zoom = 1D;

    private final FaxPreviewPanelPopup rootPopup = new FaxPreviewPanelPopup(this);

    public FaxPreviewPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }


    private void jbInit() {
        this.setLayout(borderLayout1);

        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 200));
        this.setMinimumSize(new Dimension(ControlPane.WIDTH, 200));
        this.setSize(new Dimension(ControlPane.WIDTH, 200));

        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(jScrollPane1, BorderLayout.CENTER);

        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from FaxPreviewPanel.";
            }

            public void faxSelectedForPreview(String faxname) {
                imgName = faxname;
//          System.out.println("Fax " + faxname + " for preview.");
                try {
                    displayImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        jScrollPane1.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                tryPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dblClicked(e);
                } else {
                    tryPopup(e);
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            private void dblClicked(MouseEvent e) {
                if (e.isConsumed()) {
                    return;
                }
                // Let's make sure we only invoke double click action when
                // we have a treepath. For example; This avoids opening an editor on a
                // selected node when the user double clicks on the expand/collapse icon.
                if (e.getClickCount() == 2) {
                  // Duh ?
                } else if (e.getClickCount() > 2) {
                    // Fix triple-click wanna-be drag events...
                    e.consume();
                }
            }

            private void tryPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    rootPopup.show(jScrollPane1, e.getX(), e.getY());
                }
            }
        });

    }

    private void displayImage() throws Exception {
        final Image faxImg = ImageUtil.blur(ImageUtil.toBufferedImage(ImageUtil.readImage(imgName)));
        final int w = faxImg.getWidth(null);
        final int h = faxImg.getHeight(null);
        double wFact = w / instance.getSize().getWidth();
        double hFact = h / instance.getSize().getHeight();
        final double imgRatio = Math.min(wFact, hFact);
        final AffineTransform tx = new AffineTransform();
        tx.scale((2D * zoom) / imgRatio,
                (2D * zoom) / imgRatio);
        jScrollPane1.getViewport().removeAll();
        // Draw rescaled image

        JPanel imgPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Dimension dim = new Dimension((int) (2 * w / imgRatio), (int) (2 * h / imgRatio));
                this.setPreferredSize(dim);
                g2d.drawImage(faxImg, tx, this);
            }
        };

        jScrollPane1.getViewport().add(imgPanel, null);
        imgPanel.repaint();
        jScrollPane1.repaint();
    }

    public void zoomHasChanged() {
//  System.out.println("Zoom has changed");
        Component[] imgComp = jScrollPane1.getViewport().getComponents();
        if (imgComp == null || imgComp.length == 0 || !(imgComp[0] instanceof JPanel)) {
            System.out.println("Oops!");
        } else {
            try {
                displayImage();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getZoom() {
        return zoom;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }
}
