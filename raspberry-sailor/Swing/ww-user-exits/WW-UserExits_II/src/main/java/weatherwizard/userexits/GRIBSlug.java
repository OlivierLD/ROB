package weatherwizard.userexits;

import chartview.ctx.WWContext;
import chartview.gui.right.CommandPanel;
import chartview.util.UserExitException;
import chartview.util.UserExitInterface;
import grib.data.GribDate;
import grib.data.GribType;
import grib.slug.SlugGRIB;
import jgrib.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class GRIBSlug
//        extends JApplet
        implements UserExitInterface {
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S_z");
    private HashMap<GribDate, HashMap<GribType, Float[][]>> gribDataMap = null;
    private List<String> feedback = null;

    public GRIBSlug() {
        super();
    }

    @Override
    public boolean isAvailable(CommandPanel commandPanel, WWContext ctx) {
        boolean available = true;
        if (ctx.getGribFile() == null) {
          available = false;
        }
        return available;
    }

    @Override
    public boolean userExitTask(CommandPanel cp, final WWContext ctx)
            throws UserExitException {
        boolean ok = true;
        final Thread me = Thread.currentThread();
        Thread worker = new Thread() {
            public void run() {
                showSlug(me, ctx);
            }

        };
        worker.start();
        synchronized (me) {
            try {
                me.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ok;
    }

    private void showSlug(final Thread parent, WWContext ctx) {
        GribFile gribFile = ctx.getGribFile();
        showSlug(parent, gribFile);
    }

    private void showSlug(final Thread parent, GribFile gribFile) {
        feedback = new ArrayList<>(1);
        try {
            // GribPDSParamTable.turnOffJGRIBLogging();
            TimeZone tz = TimeZone.getTimeZone("127"); // "GMT + 0"
            //  TimeZone.setDefault(tz);
            SDF.setTimeZone(tz);

            gribDataMap = new HashMap<>();

            int lrLength = gribFile.getLightRecords().length;
            for (int i = 0; i < lrLength; i++) {
                try {
                    GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
                    GribRecordPDS grpds = gr.getPDS(); // Headers and Data
                    GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
                    GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

                    Date date = grpds.getGMTForecastTime().getTime();
                    int width = grgds.getGridNX();
                    int height = grgds.getGridNY();
                    double stepX = grgds.getGridDX();
                    double stepY = grgds.getGridDY();
                    double top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
                    double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
                    double left = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
                    double right = Math.max(grgds.getGridLon1(), grgds.getGridLon2());

                    String type = grpds.getType();
                    String description = grpds.getDescription();
                    String unit = grpds.getUnit();

                    GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

                    // long lTime = gDate.getGDate().getTime();
                    // System.out.println("for type:" + type + ", GRIBDate:" + gDate.getGDate() + " (" + lTime + ")");

                    Float[][] data = new Float[height][width];
                    float val = 0F;
                    for (int col = 0; col < width; col++) {
                        for (int row = 0; row < height; row++) {
                            try {
                                val = gr.getValue(col, row);
                                if (val > grbds.getMaxValue() || val < grbds.getMinValue()) {
                                    // System.out.println("type:" + type + " val:" + val + " is out of [" + grbds.getMinValue() + ", " + grbds.getMaxValue() + "]");
                                    val = grbds.getMinValue(); // TODO Make sure that's right...
                                }

                                data[row][col] = val;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    HashMap<GribType, Float[][]> subMap = gribDataMap.get(gDate);
                    if (subMap == null) {
                      subMap = new HashMap<>();
                    }
                    subMap.put(new GribType(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
                    gribDataMap.put(gDate, subMap);
                } catch (NoValidGribException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NotSupportedException e) {
                    e.printStackTrace();
                }
            }

            JFrame frame = new SlugGRIB(gribDataMap);
            //  frame.setSize(new Dimension(800, 600));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) {
              frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
              frameSize.width = screenSize.width;
            }
            frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (standalone) {
                      System.exit(0);
                    } else {
                        e.getComponent().setVisible(false);
                        // System.out.println("Notifying...");
                        feedback.add("Success!");
                        synchronized (parent) {
                            parent.notify();
                        }
                    }
                }
            });
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            feedback.add(ex.toString());
        }
    }

    @Override
    public List<String> getFeedback() {
        return feedback;
    }

    // TODO Remove ?
    public void init() {
        try {
            standalone = true;
            // GRIBSlug gs = new GRIBSlug();
            String gribFileName = "GRIB_2009_02_25_Sample.grb";
            URL gribURL = GRIBSlug.class.getResource(gribFileName);
            // new URL("http://localhost:80/oliv-jnpl/GRIB_2009_02_25_Sample.grb");
            GribFile gf = new GribFile(gribURL.openStream());
            showSlug(Thread.currentThread(), gf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean standalone = false;

    // For standalone tests
    public static void main(String... args) throws Exception {
        standalone = true;
        GRIBSlug gs = new GRIBSlug();
        String gribFileName = "GRIB_2009_02_25_Sample.grb";
        URL gribURL = GRIBSlug.class.getResource(gribFileName);
        //  new URL("http://localhost:80/oliv-jnpl/GRIB_2009_02_25_Sample.grb");
        GribFile gf = new GribFile(gribURL.openStream());
        gs.showSlug(Thread.currentThread(), gf);
    }

}
