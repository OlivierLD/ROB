package examples.misc;

import calc.calculation.SightReductionUtil;
import calc.calculation.nauticalalmanac.*;
import nmea.parser.GeoPos;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/*
 * For a Viking Sun Compass, see https://www.instructables.com/Make-a-Viking-Sun-Compass/ and related articles.
 * https://www.youtube.com/watch?v=yX3zEX14-r8
 */
public class VikingSunCompass extends JFrame {
    private CompassPanel vscPanel = null;

    private static float deltaT = 0f;
    private final static SimpleDateFormat SDF = new SimpleDateFormat("EEE, dd MMM yyyy', at' HH:mm:ss 'UTC'");
    private final static SimpleDateFormat DAY = new SimpleDateFormat("dd MMM"); // yyyy");
    private final static SimpleDateFormat HOUR = new SimpleDateFormat("HH:mm:ss");

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }
//static { DAY.setTimeZone(TimeZone.getTimeZone("Americas/Los_Angeles")); }
//static { HOUR.setTimeZone(TimeZone.getTimeZone("Americas/Los_Angeles")); }

    private final static NumberFormat LENGTH_FMT = new DecimalFormat("#0.00");
    private final static NumberFormat Z_FMT = new DecimalFormat("#00");

    private static GeoPos gps = null;
    private static double latitude = 38;
    private static double longitude = 0; // -122;
    private static int frameSize = 750;

    private static double timeOffset = -(longitude / 15d);

    private static double poleLength = 5; // in cm
    private transient List<SunCompassData> data = null;
    private String dateStr = "";

    public VikingSunCompass() {
        vscPanel = new CompassPanel() {
            public void paintComponent(Graphics gr) {
                Graphics2D g2d = (Graphics2D) gr;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)); // , 10, dashPattern, 0));

                gr.setColor(Color.white);
                gr.fillRect(0, 0, this.getWidth(), this.getHeight());
                gr.setColor(Color.black);
                // Rose
                Dimension dim = this.getSize();
                double radius = (Math.min(dim.width, dim.height) * 0.9) / 2d;
                int graphicXOffset = 0;
                int graphicYOffset = 0;
                for (int i = 0; i < 360; i += 5) {
                    int x1 = (dim.width / 2) + (int) ((radius - (i % 45 == 0 ? 20 : 10)) * Math.cos(Math.toRadians(i)));
                    int y1 = (dim.height / 2) + (int) ((radius - (i % 45 == 0 ? 20 : 10)) * Math.sin(Math.toRadians(i)));
                    int x2 = (dim.width / 2) + (int) ((radius) * Math.cos(Math.toRadians(i)));
                    int y2 = (dim.height / 2) + (int) ((radius) * Math.sin(Math.toRadians(i)));
                    gr.drawLine(x1 + graphicXOffset, y1 + graphicYOffset, x2 + graphicXOffset, y2 + graphicYOffset);
                    if (i == 270) { // Pole eleve
                        gr.drawLine(x1 + graphicXOffset, y1 + graphicYOffset, (this.getWidth() / 2) + graphicXOffset, (this.getHeight() / 2) + graphicYOffset);
                    }
                }

                // Pole location
                g2d.fillOval((this.getWidth() / 2) - 2, (this.getHeight() / 2) - 2, 4, 4);
                g2d.drawOval((this.getWidth() / 2) - 4, (this.getHeight() / 2) - 4, 8, 8);

                g2d.drawString(dateStr, 10, 20);
                g2d.drawString("Lat: " + LENGTH_FMT.format(latitude), 10, 32);

                double ratio = this.getWidth() / (poleLength * 5);
                // Pole size
                g2d.drawLine(10, this.getHeight() - 10, 10 + (int) (ratio * poleLength), this.getHeight() - 10);

                if (data != null && data.size() != 0) {
                    Point prev = null;
                    for (SunCompassData scd : data) {
                        double z = scd.getZ();
                        double len = scd.getLen();
                        int x = (this.getWidth() / 2) + (int) (ratio * len * Math.sin(Math.toRadians(z)));
                        int y = (this.getHeight() / 2) - (int) (ratio * len * Math.cos(Math.toRadians(z)));
                        Point pt = new Point(x, y);
                        if (prev != null) {
                            g2d.drawLine(prev.x, prev.y, pt.x, pt.y);
                        }
                        prev = pt;
                    }
//          System.out.println(">>> " + data.size() + " positions");
                }
//        else
//          System.out.println(">>> No data");
            }
        };

        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(new Dimension(frameSize, frameSize));
        this.setTitle("Viking Sun Compass");
        this.getContentPane().add(vscPanel, BorderLayout.CENTER);
    }

    @SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
    public void paintCompass() {
        vscPanel.repaint();
        File f = new File(dateStr.replace(' ', '_') + ".png");
        vscPanel.genImage(f, "png", vscPanel.getWidth(), vscPanel.getHeight());
//  try { Thread.sleep(1000L); } catch (Exception ex) {}
    }

    private void work() {
        Calendar now = GregorianCalendar.getInstance();
        int year = now.get(Calendar.YEAR);

        Calendar calculationDate = GregorianCalendar.getInstance();
        calculationDate.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        calculationDate.set(year, Calendar.JANUARY, 1, 0, 0, 0);

        boolean keepGoing = true;

        while (keepGoing) {
            Calendar oneDay = (Calendar) calculationDate.clone();
            oneDay.add(Calendar.HOUR, (int) Math.floor(timeOffset));
            // Print the date
            dateStr = DAY.format(oneDay.getTime());
//    System.out.println("-----------------------");
//    System.out.println(dateStr);
//    System.out.println("-----------------------");

            data = new ArrayList<SunCompassData>();

            int incrementUnit = Calendar.MINUTE;
            int nbUnitPerIncrement = 10;

            for (int h = 0; h < (24 * 6); h++) // 24x6: 24 hours, 6 incrs per hour (10 minutes).
            {
                Core.julianDate(oneDay.get(Calendar.YEAR),
                        oneDay.get(Calendar.MONTH) + 1,
                        oneDay.get(Calendar.DAY_OF_MONTH),
                        oneDay.get(Calendar.HOUR_OF_DAY),
                        oneDay.get(Calendar.MINUTE),
                        (float)oneDay.get(Calendar.SECOND),
                        deltaT);

                Anomalies.nutation();
                Anomalies.aberration();

                Core.aries();
                Core.sun();
                Venus.compute();
                Mars.compute();
                Jupiter.compute();
                Saturn.compute();
                Moon.compute();
                Core.polaris();
                Core.moonPhase();
                Core.weekDay();

                SightReductionUtil sru = new SightReductionUtil();
                if (gps != null) {
                    if (sru == null) {
                        sru = new SightReductionUtil();
                    }
                    sru.setL(gps.lat);
                    sru.setG(gps.lng);
                    sru.setAHG(Context.GHAsun);
                    sru.setD(Context.DECsun);
                    sru.calculate();
                    if (sru.getHe() > 0) {
                        double he = sru.getHe().doubleValue();
                        double z = sru.getZ().doubleValue();
                        z += 180;
                        double shadowLength = poleLength * (1 / Math.tan(Math.toRadians(he)));
//          String str = HOUR.format(oneDay.getTime()) + ", Sun Alt :" + GeomUtil.decToSex(sru.getHe(), GeomUtil.SWING, GeomUtil.NONE) + ", Z :" + GeomUtil.decToSex(sru.getZ(), GeomUtil.SWING, GeomUtil.NONE) + ",\tShadow: " + LENGTH_FMT.format(shadowLength) + " cm, " + Z_FMT.format(z % 360) + "\272";            
//          System.out.println(str);
                        data.add(new SunCompassData(z % 360, shadowLength));
                    }
                }
                oneDay.add(incrementUnit, nbUnitPerIncrement);
            }
            paintCompass();
            calculationDate.add(Calendar.WEEK_OF_YEAR, 1);
            int newYear = calculationDate.get(Calendar.YEAR);
            if (newYear != year) {
                keepGoing = false; // Done
            }
        }
    }

    private static void setPrms(String[] prms) {
        for (int i = 0; i < prms.length; i++) {
            if (LAT_PRM.equals(prms[i])) {
                try {
                    latitude = Double.parseDouble(prms[i + 1]);
                    if (latitude < 0) {
                        throw new IllegalArgumentException("Latitude cannot be negative");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            } else if (SIZE_PRM.equals(prms[i])) {
                try {
                    frameSize = Integer.parseInt(prms[i + 1]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            } else if (HELP_PRM.equals(prms[i])) {
                System.out.println("Usage is :");
                System.out.println("  java [-DdeltaT=67.53] app.samples.VikingSunCompass <name value>");
                System.out.println("  names can be:");
                System.out.println("    -lat  for latitude, a decimal, in decimal degrees (default is 38)");
                System.out.println("    -size for the frame size (width = height), an integer, in pixels (default is 750)");
                System.out.println("    -help for help (you're on it)");
                System.out.println("Example:");
                System.out.println("  java app.samples.VikingSunCompass -lat 37.5 -size 600");
                System.out.println();
                System.out.println("This will generate image files (png) in the current directory.");
                System.out.println();
                System.out.println(" The length of the pole (pivot) is at the bottom left of the generated images");
                System.out.println(" The direction of the rose is the elevated pole (north if your latitude is north, south if your latitude is south).");
                System.exit(0);
            }
        }
    }

    private final static String LAT_PRM = "-lat";
    private final static String SIZE_PRM = "-size";
    private final static String HELP_PRM = "-help";

    public static void main(String[] args) {
//  latitude  = 38; // Must be positive
        longitude = 0;

        setPrms(args);

        timeOffset = -(longitude / 15d);

        poleLength = 5;

        gps = new GeoPos(latitude, longitude);

        try {
            deltaT = Float.parseFloat(System.getProperty("deltaT", "67.5353"));
        } // Jan-2015
        catch (NumberFormatException nfe) {
            System.out.println("-DdeltaT contains bad value...");
        }
        System.out.println("Delta T = " + deltaT);

        VikingSunCompass frame = new VikingSunCompass();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.work();
    }

    private static class SunCompassData {
        private double z = 0;
        private double len = 0;

        public double getZ() {
            return z;
        }

        public double getLen() {
            return len;
        }

        public SunCompassData(double z, double l) {
            this.z = z;
            this.len = l;
        }
    }

    private static class CompassPanel extends JPanel {
        public int[] genImage(File f, String ext, int width, int height) {
            //  int w = this.getWidth();
            //  int h = this.getHeight();
            RenderedImage rendImage = createChartImage(width, height);
            // Write generated image to a file
            try {
                OutputStream os = new FileOutputStream(f);
                ImageIO.write(rendImage, ext, os);
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  return new int[] { this.getWidth(), this.getHeight() };
            //  return new int[] { w, h };
            return new int[]{width, height};
        }

        public RenderedImage createChartImage(int w, int h) {
            int width = w;
            int height = h;

            // Create a buffered image in which to draw
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // Create a graphics contents on the buffered image
            Graphics2D g2d = bufferedImage.createGraphics();

            this.paintComponent((Graphics) g2d);
            // Graphics context no longer needed so dispose it
            g2d.dispose();

            return bufferedImage;
        }
    }
}
