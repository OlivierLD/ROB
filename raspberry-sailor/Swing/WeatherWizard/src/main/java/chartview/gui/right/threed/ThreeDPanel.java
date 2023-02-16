package chartview.gui.right.threed;


import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.right.matrix.Matrix3D;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.grib.GRIBDataUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.List;
import java.util.*;


/**
 * A set of classes to parse, represent and display 3D wireframe models
 * represented in Wavefront .obj format.
 * Adapted from the ThreeD.java sample, delivered with the jdk 1.2.2
 */
public class ThreeDPanel
        extends JPanel
        implements Runnable, MouseListener, MouseMotionListener {
    private Color bgColor = Color.black;
    private Color lineColor = Color.green;
    private Color textColor = Color.red;
    private Color pointColor = Color.yellow;

    private String panelLabel = "";

    public final static short DOT_OPT = 0;
    public final static short CIRC_OPT = 1;
    public final static short DRAW_OPT = 2;

    private short drawingOption = DRAW_OPT;

    private Vector extraPts = null;
    private Vector labelPts = null;

    private Model3D md = null;
    private Model3D extra = null;
    private Model3D labels = null;

    private HashMap<String, Module3D> modules = null;

    boolean painted = true;
    float xfac;
    int prevx, prevy;
    float xtheta = 0f,
            ytheta = 0f;
    float scalefudge = 1.0f;
    Matrix3D amat = new Matrix3D(),
            tmat = new Matrix3D();
    String mdname = null;
    String message = null;

    boolean stbd_port = false;

    boolean displayTws = false,
            displayPrmsl = false,
            display500mb = false,
            displayWaves = false,
            displayTemp = false,
            displayRain = false;

    public ThreeDPanel(String model,
                       Color bg,
                       Color line,
                       Color text,
                       Color pts) {
        this(model, 2.0f, bg, line, text, pts);
    }

    public ThreeDPanel(String model,
                       float scale,
                       Color bg,
                       Color line,
                       Color text,
                       Color pts) {
        if (bg != null)
            bgColor = bg;
        if (line != null)
            lineColor = line;
        if (text != null)
            textColor = text;
        if (pts != null)
            pointColor = pts;

        if (model != null && model.trim().length() > 0)
            init(model, scale);

        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from ThreeDPanel.";
            }

            public void newTWSObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
                    for (Point p : al) {
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_TWS] + ".obj", (Color) ParamPanel.data[ParamData.GRIB_WIND_COLOR][ParamData.VALUE_INDEX], "tws");
                if (displayTws) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void new500mbObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
//            setModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[option] + ".obj", true);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
//              Iterator iterator = al.iterator();
//              while (iterator.hasNext())
                    for (Point p : al) {
//              Point p = (Point)iterator.next();
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_500MB] + ".obj", (Color) ParamPanel.data[ParamData.MB500_CONTOUR][ParamData.VALUE_INDEX], "500mb");
                if (display500mb) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void newPrmslObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
//            setModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[option] + ".obj", true);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
                    Iterator iterator = al.iterator();
                    while (iterator.hasNext()) {
                        Point p = (Point) iterator.next();
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_PRMSL] + ".obj", (Color) ParamPanel.data[ParamData.PRMSL_CONTOUR][ParamData.VALUE_INDEX], "prmsl");
                if (displayPrmsl) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void newTmpObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
                    Iterator iterator = al.iterator();
                    while (iterator.hasNext()) {
                        Point p = (Point) iterator.next();
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_TMP] + ".obj", (Color) ParamPanel.data[ParamData.TEMP_CONTOUR][ParamData.VALUE_INDEX], "temp");
                if (displayTemp) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void newWaveObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
                    Iterator iterator = al.iterator();
                    while (iterator.hasNext()) {
                        Point p = (Point) iterator.next();
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_WAVE] + ".obj", (Color) ParamPanel.data[ParamData.WAVES_CONTOUR][ParamData.VALUE_INDEX], "waves");
                if (displayWaves) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void newRainObj(List<Point> al) {
                if (md == null) {
                    setScale(1.0f);
                    setModel("temp" + File.separator + "chart.obj", true);
                    Vector<ThreeDPoint> labelVector = new Vector<ThreeDPoint>();
                    Iterator iterator = al.iterator();
                    while (iterator.hasNext()) {
                        Point p = (Point) iterator.next();
                        labelVector.add(new ThreeDPoint((float) p.x, (float) p.y, 0.0f));
                    }
                    setLabelPts(labelVector);
                    repaint();
                    setPainted();
                    run();
                }
                addModel("temp" + File.separator + GRIBDataUtil.DATA_NAME[GRIBDataUtil.TYPE_RAIN] + ".obj", Color.gray, "rain");
                if (displayRain) {
                    repaint();
                    setPainted();
                    run();
                }
            }

            public void setZoom3D(double d) {
//          System.out.println("Zoom from " + scalefudge + " to " + d);
                init(mdname, (float) d);
                repaint();
                setPainted();
                run();
            }

            public void setTWSDisplayed(boolean b) {
                displayTws = b;
                repaint();
            }

            public void setPRMSLDisplayed(boolean b) {
                displayPrmsl = b;
                repaint();
            }

            public void set500MBDisplayed(boolean b) {
                display500mb = b;
                repaint();
            }

            public void setWAVESDisplayed(boolean b) {
                displayWaves = b;
                repaint();
            }

            public void setTEMPDisplayed(boolean b) {
                displayTemp = b;
                repaint();
            }

            public void setRAINDisplayed(boolean b) {
                displayRain = b;
                repaint();
            }
        });

    }

    public void setScale(float s) {
        this.scalefudge = s;
    }

    public void setModel(String m) {
        setModel(m, false);
    }

    public void setModel(String m, boolean enforce) {
        if (m != null && (!m.equals(mdname) || enforce)) {
            if (enforce)
                WWContext.getInstance().fireLogging("Enforcing refresh, scale " + scalefudge + "\n");
            this.mdname = m;
            init(mdname, scalefudge);
        }
    }

    public void addModel(String m, Color c, String name) {
        if (m != null) {
            try {
                File f = new File(m);
                if (f.exists()) {
                    FileInputStream is = new FileInputStream(f);
                    Model3D m3d = new Model3D(is);
                    is.close();
                    m3d.findBB();
                    m3d.compress();

                    Module3D module = new Module3D(m3d, c, name);
                    if (modules == null)
                        modules = new HashMap<String, Module3D>(1);
                    modules.put(name, module);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setExtraPts(Vector v) {
        this.extraPts = v;
    }

    public Vector getExtraPts() {
        return this.extraPts;
    }

    public void setLabelPts(Vector v) {
        this.labelPts = v;
    }

    public Vector getLabelPts() {
        return this.labelPts;
    }

    public void setDrawingOption(short s) {
        this.drawingOption = s;
    }


    public void init(String model, float scale) {
        mdname = model;
        scalefudge = scale;

        amat.xrot(180f);
        amat.yrot(25f);
        amat.zrot(40f);

        if (mdname == null)
            mdname = "paperboat.obj";

        addMouseListener(this);
        addMouseMotionListener(this);

        this.setBackground(bgColor);
    }

    public void setBGColor(Color c) {
        bgColor = c;
    }

    public void setPointColor(Color c) {
        pointColor = c;
    }

    public void setTextColor(Color c) {
        textColor = c;
    }

    public void setLineColor(Color c) {
        lineColor = c;
    }

    public void setPanelLabel(String s) {
        panelLabel = s;
    }

    public void run() {
        InputStream is = null;
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            is = new FileInputStream(mdname);
            Model3D m = new Model3D(is);
            md = m;
            m.findBB();
            m.compress();
            float xw = m.xmax - m.xmin;
            float yw = m.ymax - m.ymin;
            float zw = m.zmax - m.zmin;
            if (yw > xw)
                xw = yw;
            if (zw > xw)
                xw = zw;
            float f1 = getSize().width / xw;
            float f2 = getSize().height / xw;
            xfac = 0.7f * (f1 < f2 ? f1 : f2) * scalefudge;

            if (this.extraPts != null) {
                Model3D ptModel = new Model3D(extraPts);
                extra = ptModel;
                ptModel.findBB();
                ptModel.compress();
            } else
                extra = null;

            if (this.labelPts != null) {
                Model3D labelModel = new Model3D(labelPts);
                labels = labelModel;
                labelModel.findBB();
                labelModel.compress();
            }
        } catch (Exception e) {
            md = null;
            message = e.getMessage();
            WWContext.getInstance().fireLogging("ThreeDPanel:" + message + "\n");
            System.out.println("Exception " + message + " for " + mdname);
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        try {
            if (is != null)
                is.close();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        super.setCursor(new Cursor(Cursor.MOVE_CURSOR));
        prevx = e.getX();
        prevy = e.getY();
        e.consume();
    }

    public void mouseReleased(MouseEvent e) {
        super.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        tmat.unit();
        xtheta = (prevy - y) * 360.0f / getSize().width;
        ytheta = (x - prevx) * 360.0f / getSize().height;

//    System.out.println("xTheta:" + xtheta);
//    System.out.println("yTheta:" + ytheta);

        tmat.xrot(xtheta);
        tmat.yrot(ytheta);
        amat.mult(tmat);

//  System.out.println("Amat:" + amat);
//  System.out.println("Tmat:" + tmat);

        if (painted) {
            painted = false;
            repaint();
        }
        prevx = x;
        prevy = y;
        e.consume();

//  System.out.println("xtheta:" + xtheta);
//  System.out.println("ytheta:" + ytheta);
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void paintComponent(Graphics g) {
        init(mdname, scalefudge);
    }

    public void paint(Graphics g) {
        //  System.out.println("Painting for " + mdname);
        Rectangle r = this.getBounds();
        // Background
        g.setColor(bgColor);
        g.fillRect(0, 0, r.width, r.height);

        g.setColor(textColor);
        if (panelLabel.trim().length() > 0)
            g.drawString(panelLabel, 3, 12);
        g.setColor(bgColor);
        // Axis
    /*
    g.setColor(Color.red);
    g.drawLine(0, r.height / 2, r.width, r.height / 2);
    g.drawLine(r.width / 2, 0, r.width / 2, r.height);
    */

        // Get the extrema
        float xmin = Float.MAX_VALUE, xmax = -Float.MAX_VALUE,
                ymin = Float.MAX_VALUE, ymax = -Float.MAX_VALUE,
                zmin = Float.MAX_VALUE, zmax = -Float.MAX_VALUE;

        if (md != null) {
            if (md.xmin < xmin) xmin = md.xmin;
            if (md.ymin < ymin) ymin = md.ymin;
            if (md.zmin < zmin) zmin = md.zmin;

            if (md.xmax > xmax) xmax = md.xmax;
            if (md.ymax > ymax) ymax = md.ymax;
            if (md.zmax > zmax) zmax = md.zmax;
        }
        if (modules != null) {
            Set<String> keys = modules.keySet();
            for (String k : keys) {
                Module3D module = modules.get(k);
                Model3D m3d = module.getModel3D();
                if (m3d.xmin < xmin) xmin = m3d.xmin;
                if (m3d.ymin < ymin) ymin = m3d.ymin;
                if (m3d.zmin < zmin) zmin = m3d.zmin;

                if (m3d.xmax > xmax) xmax = m3d.xmax;
                if (m3d.ymax > ymax) ymax = m3d.ymax;
                if (m3d.zmax > zmax) zmax = m3d.zmax;
            }
        }

        if (md == null && mdname != null)
            run(); // Always reset
        if (md != null) {
            md.mat.unit();
            md.mat.translate(-(xmin + xmax) / 2, -(ymin + ymax) / 2, -(zmin + zmax) / 2);
            md.mat.mult(amat);
            md.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
            md.mat.translate(getSize().width / 2, getSize().height / 2, 8);
            md.transformed = false;
            md.paint(g);
        }
        if (extra != null) {
            extra.mat.unit();
            extra.mat.translate(-(xmin + xmax) / 2, -(ymin + ymax) / 2, -(zmin + zmax) / 2);
            extra.mat.mult(amat);
            extra.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
            extra.mat.translate(getSize().width / 2, getSize().height / 2, 8);
            extra.transformed = false;
            extra.paint(g, ThreeDPanel.Model3D.DRAW, true);
        } else if (message != null) {
            g.drawString("Error in model:", 3, 20);
            g.drawString(message, 10, 40);
            System.err.println("Error in model : " + message);
        }
        if (labels != null) {
            labels.mat.unit();
            labels.mat.translate(-(xmin + xmax) / 2,
                    -(ymin + ymax) / 2,
                    -(zmin + zmax) / 2);
            labels.mat.mult(amat);
            labels.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
            labels.mat.translate(getSize().width / 2, getSize().height / 2, 8);
            labels.transformed = false;
            labels.paint(g, ThreeDPanel.Model3D.WRITE, true);
        } else if (message != null) {
            g.drawString("Error in model:", 3, 20);
            g.drawString(message, 10, 40);
            System.err.println("Error in model : " + message);
        }

        if (modules != null) {
            Set<String> keys = modules.keySet();
            for (String k : keys) {
                Module3D module = modules.get(k);
                String moduleName = module.getName();
                if ((moduleName.equals("prmsl") && displayPrmsl) ||
                        (moduleName.equals("500mb") && display500mb) ||
                        (moduleName.equals("waves") && displayWaves) ||
                        (moduleName.equals("temp") && displayTemp) ||
                        (moduleName.equals("rain") && displayRain) ||
                        (moduleName.equals("tws") && displayTws)) {
                    Model3D m3d = module.getModel3D();
                    m3d.mat.unit();
                    m3d.mat.translate(-(xmin + xmax) / 2, -(ymin + ymax) / 2, -(zmin + zmax) / 2);
                    m3d.mat.mult(amat);
                    m3d.mat.scale(xfac, -xfac, 16 * xfac / getSize().width);
                    m3d.mat.translate(getSize().width / 2, getSize().height / 2, 8);
                    m3d.transformed = false;
                    g.setColor(module.getColor());
                    m3d.paint(g, Model3D.DRAW, false);
                }
            }
        }
        setPainted();
    }

    private synchronized void setPainted() {
        painted = true;
        notifyAll();
    }

    /**
     * The representation of a 3D model
     */
    final class Model3D {
        float vert[];
        int tvert[];
        int nvert, maxvert;
        int con[];
        int ncon, maxcon;
        boolean transformed;
        Matrix3D mat;

        public final static short PLOT = 0;
        public final static short DRAW = 1;
        public final static short WRITE = 2;

        float xmin, xmax, ymin, ymax, zmin, zmax;

        final float initialXRot = 20f,
                initialYRot = 30f;

        Model3D() {
            mat = new Matrix3D();
            mat.xrot(initialXRot);
            mat.yrot(initialYRot);
        }

        /**
         * Create a 3D model by parsing an input stream
         */
        Model3D(InputStream is) throws IOException, FileFormatException {
            this();
            StreamTokenizer st =
                    new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
            st.eolIsSignificant(true);
            st.commentChar('#');
            scan:
            while (true) {
                switch (st.nextToken()) {
                    default:
                        break scan;
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_WORD:
                        if ("v".equals(st.sval)) {
                            double x = 0, y = 0, z = 0;
                            if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
                                x = st.nval;
                                if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
                                    y = st.nval;
                                    if (st.nextToken() == StreamTokenizer.TT_NUMBER)
                                        z = st.nval;
                                }
                            }
                            addVert((float) x, (float) y, (float) z);
                            while (st.ttype != StreamTokenizer.TT_EOL &&
                                    st.ttype != StreamTokenizer.TT_EOF)
                                st.nextToken();
                        } else if ("f".equals(st.sval) || "fo".equals(st.sval) ||
                                "l".equals(st.sval)) {
                            int start = -1;
                            int prev = -1;
                            int n = -1;
                            while (true)
                                if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
                                    n = (int) st.nval;
                                    if (prev >= 0)
                                        add(prev - 1, n - 1);
                                    if (start < 0)
                                        start = n;
                                    prev = n;
                                } else if (st.ttype == '/')
                                    st.nextToken();
                                else
                                    break;
                            if (start >= 0)
                                add(start - 1, prev - 1);
                            if (st.ttype != StreamTokenizer.TT_EOL)
                                break scan;
                        } else {
                            while (st.nextToken() != StreamTokenizer.TT_EOL &&
                                    st.ttype != StreamTokenizer.TT_EOF)
                                ;
                        }
                }
            }
            is.close();
            if (st.ttype != StreamTokenizer.TT_EOF)
                throw new FileFormatException(st.toString());
        }

        Model3D(Vector v) throws Exception {
            this();
            // v
            int nb = 0;
            Enumeration pts = v.elements();
            while (pts.hasMoreElements()) {
                Object pt = pts.nextElement();
                if (pt instanceof ThreeDPoint) {
                    nb++;
                    ThreeDPoint pt3d = (ThreeDPoint) pt;
                    addVert(pt3d.getX(), pt3d.getY(), pt3d.getZ());
                } else
                    throw new Exception("Invalid object in vector");
            }
            // f
            for (int i = 0; i < nb; i++) {
                add(i, i + 1);
            }
        }

        /**
         * Add a vertex to this model
         */
        int addVert(float x, float y, float z) {
            int i = nvert;
            if (i >= maxvert) {
                if (vert == null) {
                    maxvert = 100;
                    vert = new float[maxvert * 3];
                } else {
                    maxvert *= 2;
                    float nv[] = new float[maxvert * 3];
                    System.arraycopy(vert, 0, nv, 0, vert.length);
                    vert = nv;
                }
            }
            i *= 3;
            vert[i] = x;
            vert[i + 1] = y;
            vert[i + 2] = z;
            return nvert++;
        }

        /**
         * Add a line from vertex p1 to vertex p2
         */
        void add(int p1, int p2) {
            int i = ncon;
            if (p1 >= nvert || p2 >= nvert)
                return;
            if (i >= maxcon) {
                if (con == null) {
                    maxcon = 100;
                    con = new int[maxcon];
                } else {
                    maxcon *= 2;
                    int nv[] = new int[maxcon];
                    System.arraycopy(con, 0, nv, 0, con.length);
                    con = nv;
                }
            }
            if (p1 > p2) {
                int t = p1;
                p1 = p2;
                p2 = t;
            }
            con[i] = (p1 << 16) | p2;
            ncon = i + 1;
        }

        /**
         * Transform all the points in this model
         */
        void transform() {
            if (transformed || nvert <= 0)
                return;
            if (tvert == null || tvert.length < nvert * 3)
                tvert = new int[nvert * 3];
            mat.transform(vert, tvert, nvert);
            transformed = true;
        }

        /* Quick Sort implementation
         */

        private void quickSort(int[] a, int left, int right) {
            int leftIndex = left;
            int rightIndex = right;
            int partionElement;
            if (right > left) {
                /* Arbitrarily establishing partition element as the midpoint of
                 * the array.
                 */
                partionElement = a[(left + right) / 2];

                // loop through the array until indices cross
                while (leftIndex <= rightIndex) {
                    /* find the first element that is greater than or equal to
                     * the partionElement starting from the leftIndex.
                     */
                    while ((leftIndex < right) && (a[leftIndex] < partionElement))
                        ++leftIndex;

                    /* find an element that is smaller than or equal to
                     * the partionElement starting from the rightIndex.
                     */
                    while ((rightIndex > left) && (a[rightIndex] > partionElement))
                        --rightIndex;

                    // if the indexes have not crossed, swap
                    if (leftIndex <= rightIndex) {
                        swap(a, leftIndex, rightIndex);
                        ++leftIndex;
                        --rightIndex;
                    }
                }

                /* If the right index has not reached the left side of array
                 * must now sort the left partition.
                 */
                if (left < rightIndex)
                    quickSort(a, left, rightIndex);

                /* If the left index has not reached the right side of array
                 * must now sort the right partition.
                 */
                if (leftIndex < right)
                    quickSort(a, leftIndex, right);
            }
        }

        private void swap(int[] a, int i, int j) {
            int T;
            T = a[i];
            a[i] = a[j];
            a[j] = T;
        }

        /**
         * eliminate duplicate lines
         */
        void compress() {
            int limit = ncon;
            int c[] = con;
            quickSort(con, 0, ncon - 1);
            int d = 0;
            int pp1 = -1;
            for (int i = 0; i < limit; i++) {
                int p1 = c[i];
                if (pp1 != p1) {
                    c[d] = p1;
                    d++;
                }
                pp1 = p1;
            }
            ncon = d;
        }

        Color gr[] = null;

        /**
         * Paint this model to a graphics context.  It uses the matrix associated
         * with this model to map from model space to screen space.
         * The next version of the browser should have double buffering,
         * which will make this *much* nicer
         */
        void paint(Graphics g) {
            paint(g, DRAW, true);
        }

        private final String[] chartLabels = {"N", "S", "W", "E"};

        void paint(Graphics g, short opt, boolean setColor) {
            if (vert == null || nvert <= 0) {
//      System.out.println("Cancelling paint (opt " + (opt==DRAW?"DRAW":(opt==PLOT?"PLOT":"WRITE")) + ")");
                return;
            }
            transform();
            if (opt == DRAW) {
                if (gr == null) {
                    gr = new Color[16];
                    // Green from 255 (light) to 60 (dark)
                    int FROM = 225;
                    int TO = 60;
                    int AMPLITUDE = FROM - TO;
                    int ORIGIN = 255 - AMPLITUDE;
                    for (int i = 0; i < 16; i++) {
                        //    int grey = (int) (170*(1-Math.pow(i/15.0, 2.3)));
                        //    gr[15 - i] = new Color(grey, grey, grey);
                        ////     int green = ORIGIN + ((int) ((double) (15 - i) * ((double) AMPLITUDE / 15.0)));
                        ////     gr[15 - i] = new Color(0, green, 0);
                        gr[15 - i] = lineColor;
                    }
                }
            }
            int lg = 0;
            int lim = ncon;
            int c[] = con;
            int v[] = tvert;
            if (lim <= 0 || nvert <= 0)
                return;
            int p1 = 0, p2 = 0;
            if (stbd_port) {
                int T = c[lim / 2];
                p1 = ((T >> 16) & 0xFFFF) * 3;
                p2 = (T & 0xFFFF) * 3;

                Color cc = g.getColor();
                if (setColor) g.setColor(textColor);
                Font f = g.getFont();
                Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
                g.setFont(f2);
                String str = "STBD";
                int l = g.getFontMetrics(f2).stringWidth(str);
                g.drawString(str, v[p1] - (l / 2), v[p1 + 1]);
                g.setFont(f);
                if (setColor) g.setColor(cc);
            }
            for (int i = 0; i < lim; i++) {
                int T = c[i];
                p1 = ((T >> 16) & 0xFFFF) * 3;
                p2 = (T & 0xFFFF) * 3;
                int grey = v[p1 + 2] + v[p2 + 2];
                if (grey < 0)
                    grey = 0;
                if (grey > 15)
                    grey = 15;
                if (grey != lg) {
                    lg = grey;
                    if (setColor) g.setColor(opt == DRAW ? gr[grey] : pointColor);
                }

                if (opt == DRAW) {
                    if (i == 0) // Axes
                    {
//          g.setColor(Color.black);
                        if (setColor) g.setColor(lineColor);
                    }
                } else if (setColor) g.setColor(pointColor);

                if (opt == DRAW) {
                    // Draw
                    g.drawLine(v[p1], v[p1 + 1], v[p2], v[p2 + 1]);
                } else if (opt == PLOT) {
                    // Plot
                    if (drawingOption == CIRC_OPT)
                        g.drawOval(v[p1] - 2, v[p1 + 1] - 2, 4, 4);
                    else if (drawingOption == DOT_OPT)
                        g.drawLine(v[p1], v[p1 + 1], v[p1], v[p1 + 1]);
                    else if (drawingOption == DRAW_OPT)
                        g.drawLine(v[p1], v[p1 + 1], v[p2], v[p2 + 1]);
                } else if (opt == WRITE) {
                    Color cc = g.getColor();
                    if (setColor) g.setColor(textColor);
                    Font f = g.getFont();
                    Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
                    g.setFont(f2);
                    String str = chartLabels[i];  // N, S, W, E
                    int l = g.getFontMetrics(f2).stringWidth(str);
                    g.drawString(str, v[p1] - (l / 2), v[p1 + 1]);
                    g.setFont(f);
                    if (setColor) g.setColor(cc);
                }
            }
            if (opt == PLOT && drawingOption != DRAW_OPT) // Last point
                g.drawLine(v[p2], v[p2 + 1], v[p2], v[p2 + 1]);
            if (opt == WRITE) // Last point
            {
                Color cc = g.getColor();
                if (setColor) g.setColor(textColor);
                Font f = g.getFont();
                Font f2 = new Font(f.getName(), Font.BOLD, f.getSize());
                g.setFont(f2);
                String str = chartLabels[3];
                int l = g.getFontMetrics(f2).stringWidth(str);
                g.drawString(str, v[p2] - (l / 2), v[p2 + 1]);
                g.setFont(f);
                if (setColor) g.setColor(cc);
            }
        }

        /**
         * Find the bounding box of this model
         */
        void findBB() {
            if (nvert <= 0)
                return;
            float v[] = vert;
            float xmin = v[0], xmax = xmin;
            float ymin = v[1], ymax = ymin;
            float zmin = v[2], zmax = zmin;
            for (int i = nvert * 3; (i -= 3) > 0; ) {
                float x = v[i];
                if (x < xmin)
                    xmin = x;
                if (x > xmax)
                    xmax = x;
                float y = v[i + 1];
                if (y < ymin)
                    ymin = y;
                if (y > ymax)
                    ymax = y;
                float z = v[i + 2];
                if (z < zmin)
                    zmin = z;
                if (z > zmax)
                    zmax = z;
            }
            this.xmax = xmax;
            this.xmin = xmin;
            this.ymax = ymax;
            this.ymin = ymin;
            this.zmax = zmax;
            this.zmin = zmin;
        }
    }

    
    class FileFormatException
            extends Exception {
        public FileFormatException(String s) {
            super(s);
        }
    }

    class Module3D {
        Color c;
        Model3D model;
        String name;

        public Module3D() {
        }

        public Module3D(Model3D model, Color c, String name) {
            this.model = model;
            this.c = c;
            this.name = name;
        }

        public Color getColor() {
            return this.c;
        }

        public Model3D getModel3D() {
            return this.model;
        }

        public String getName() {
            return this.name;
        }

        public void setColor(Color c) {
            this.c = c;
        }

        public void setModel3D(Model3D model) {
            this.model = model;
        }

        public void setName(String s) {
            this.name = s;
        }
    }
}

