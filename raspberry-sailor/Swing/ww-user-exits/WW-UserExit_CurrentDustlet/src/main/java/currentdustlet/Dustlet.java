package currentdustlet;

// Based on Nick Thompson's Dustlet:
//     Dust - a simple particle system for wind visualization
//     Copyright 1999,2005  Nick Thompson
//     see http://nixfiles.com/dust for more information
//

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.File;
import java.net.URL;

public class Dustlet
        extends JPanel
        implements ImageObserver,
        Runnable {
    private Dust dust;
    private double fetchInterval = 0;
    private long lastFetch = 0;
    private boolean running;
    private URL windfileUrl;
    private Image bgimage;
    private Thread animator;
    private double timestep;
    private int arraywidth, arrayheight;
    private int motes = 10000;
    private String dustletData;
    private int w, h;

    public Dustlet(int w, int h,
                   double refetch,
                   String bgimg,
                   float ts,
                   int aw, int ah,
                   int m,
                   String data) {
//    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
//      {
//        public void updateDustletDate(Date d) 
//        {
//          updateWindData();
//        }        
//      });
//    
        if (refetch != -1D)
            fetchInterval = refetch;

        // read the background image
        String datfile = bgimg;
        if (datfile == null)
            datfile = "temp" + File.separator + "grib.png";

        try {
            URL url = new File(datfile).toURI().toURL();
            bgimage = new ImageIcon(url).getImage();
        } catch (java.net.MalformedURLException e) {
            System.err.println("bad url " + datfile + ":\n" + e);
        }

        timestep = ts;

        arraywidth = aw;
        arrayheight = ah;
        motes = m;
        dustletData = data;
        this.w = w;
        this.h = h;
        this.setPreferredSize(new Dimension(w, h));
    
    /*
    System.out.println("Parameters:");
    System.out.println("ArrayWidth:" + Integer.toString(arraywidth));
    System.out.println("ArrayHeight:" + Integer.toString(arrayheight));
    System.out.println("Data:" + datfile);
    System.out.println("ReFetch:" + Double.toString(fetchInterval));
    */
        stopRunning();
        start();
    }

    public void start() {
        int nmotes = motes;
        dust = new Dust(nmotes);

        if (dustletData == null)
            dustletData = "temp" + File.separator + "grib.dust";

//    try
//    {
//      windfile_url = new File(dustletData).toURL();
//    }
//    catch (java.net.MalformedURLException e)
//    {
//      System.err.println("bad url " + dustletData + ":\n" + e);
//    }
        updateWindData();
        dust.set_pixel_size(w, h);
        startRunning();
    }

    public void stop() {
        stopRunning();
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        if ((infoflags & ImageObserver.ALLBITS) != 0) {
            repaint();
            return false;
        }
        return true;
    }

    public void paintBackground(Graphics g) {
        if (bgimage == null) {
            g.setColor(Color.black);
            Rectangle rect = g.getClipBounds();
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            return;
        }

        g.drawImage(bgimage, 0, 0, null);
    }

    // XXX/nix resizing won't work right...
    Image backbuffer;
    Graphics backbuffer_graphics;

    public void update(Graphics g) { // skip the background fill
        paint(g);
    }

    public void paint(Graphics g) {
        // System.err.println("paint called");
        if (backbuffer == null) {
            Rectangle rect = g.getClipBounds();
            backbuffer = createImage(rect.width, rect.height);
            backbuffer_graphics = backbuffer.getGraphics();
        }
        paintBackground(backbuffer_graphics);
        if (dust != null) {
            backbuffer_graphics.setColor(Color.cyan);
            dust.drawmotes(backbuffer_graphics);
        }
        g.drawImage(backbuffer, 0, 0, null);
    }

    public synchronized void awaitRunning() {
        while (!running) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void startRunning() {
        running = true;
        notify();
        if (animator == null) {
            animator = new Thread(this);
            animator.start();
        }
    }

    public synchronized void stopRunning() {
        running = false;
        notify();
    }

    public void updateWindData() {
        java.io.InputStream in = null;
//  System.out.println("Updating wind data");
        // XXXopen url stream
        try {
            try {
                windfileUrl = new File(dustletData).toURI().toURL();
            } catch (java.net.MalformedURLException e) {
                System.err.println("bad url " + dustletData + ":\n" + e);
            }
            boolean ok = false;
            while (!ok) {
                try {
                    in = windfileUrl.openStream();
                    dust.read_windfile(in, arraywidth, arrayheight);
                    ok = true;
                } catch (Throwable tr) {
                    System.out.println("Retrying to read dustlet data");
                    try {
                        Thread.sleep(100L);
                    } catch (Exception ignore) {
                    }
                    ok = false;
                }
                ok = true; // No loop for now
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    System.err.println(e);
                }
            }
        }
        lastFetch = System.currentTimeMillis();
    }

    public void step() {
        dust.stepmotes(0.1);
        repaint();
    }

    // this is the main loop for the update thread

    public void run() {
        while (true) {
            awaitRunning();

            long now = System.currentTimeMillis();
            double dt = (now - lastFetch) / 1000.0;
            if (fetchInterval > 0 && dt > fetchInterval) {
//      System.out.println("Dustlet Refresh.");
                updateWindData();
            }
//    else
//      System.out.println("dT = " + dt + ", no refresh (fetchInterval:" + fetchInterval + ")");
            step();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    public void setMotes(int motes) {
        this.motes = motes;
        stopRunning();
        start();
    }
}
