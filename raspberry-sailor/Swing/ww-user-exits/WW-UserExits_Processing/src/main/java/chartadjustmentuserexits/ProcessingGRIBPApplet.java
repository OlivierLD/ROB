package chartadjustmentuserexits;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.StringTokenizer;

public class ProcessingGRIBPApplet
        extends PApplet {

    /**
     * PRMSL from a GRIB
     * with smoothing between frames.
     * <p>
     * with user interaction (randomMove = false)
     */
    boolean randomMove = false;

    float r = 18f; // (float)(255f * Math.random());
    float g = 118f; // (float)(255f * Math.random());
    float b = 192f; // (float)(255f * Math.random());

    int opacity = 128; // 0 to 255

    String[] allData = null;

    // Vertices, the one displayed
    PVector vertices[][];
    // The one calculated
    PVector coordinates[][][];

    PVector chart[][];

    PVector previous[][] = null;
    PVector next[][] = null;

    int nbStepBetweenGRIB = 30;
    int currentGRIBStep = 0;

    int currentDateIndex = 0;
    int maxDateIndex = 0;

    float ONE_DEGREE = PI / 180f;
    float angleInc;

    String dType = "prmsl"; // hgt, prmsl, htsgw, prate, tmp, ugrd, vgrd ...

    public void setup() {
        if (online) {
            String dataType = param("data-type");
            if (dataType != null) {
              dType = dataType;
            }
        }
        allData = loadStrings("grib.txt");

        size(500, 500, P3D);
        //noStroke();
        if (randomMove) {
          angleInc = PI / 300.0f;
        } else {
          angleInc = 0f; // PI / 6f;
        }
        initFigure();
    }

    public void initFigure() {
        buildMatrixes(allData, dType); //, coordinates, chart);
        maxDateIndex = coordinates.length;
        if (coordinates.length > 1) {
            previous = coordinates[0];
            next = coordinates[1];
        }
    }

    boolean locked = false;
    int mouseDraggedFromX = 0;
    int mouseDraggedFromY = 0;

    float rX = 0f;
    float rY = 0f;
    float rZ = 0f;

    float mouseDraggedCoef = 2f;

    public void draw() {
        if (mousePressed) {
            if (!locked) {
                mouseDraggedFromX = mouseX;
                mouseDraggedFromY = mouseY;
                locked = true;
            } else {
                if ((mouseX - mouseDraggedFromX) > 0) {
                    //      out("To the right");
                    rY += cos(rX) * ONE_DEGREE * mouseDraggedCoef;
                    rZ -= sin(rX) * ONE_DEGREE * mouseDraggedCoef;
                } else if ((mouseX - mouseDraggedFromX) < 0) {
                    //      out("To the left");
                    rY -= cos(rX) * ONE_DEGREE * mouseDraggedCoef;
                    rZ += sin(rX) * ONE_DEGREE * mouseDraggedCoef;
                }
                if ((mouseY - mouseDraggedFromY) > 0) {
                    //      out("Moving Down");
                    rX -= ONE_DEGREE * mouseDraggedCoef;
                } else if ((mouseY - mouseDraggedFromY) < 0) {
                    //      out("Moving Up");
                    rX += ONE_DEGREE * mouseDraggedCoef;
                }
                mouseDraggedFromX = mouseX;
                mouseDraggedFromY = mouseY;
            }
        } else {
            //  if (locked)
            //    out("Mouse has been released");
            locked = false;
        }
        background(170, 95, 95);
        //lights();
        float dirY = (mouseY / PApplet.parseFloat(height) - 0.5f) * 2;
        float dirX = (mouseX / PApplet.parseFloat(width) - 0.5f) * 2;
        directionalLight(204, 204, 204, -dirX, -dirY, -1);
        fill(r, g, b, opacity); // Last prm = 255:Opaque, 0: transparent
        translate(width / 2, height / 2);
        // frameCount gives the mouvement...
        if (randomMove) {
            rX = rY = rZ = frameCount * angleInc;
        }

        rotateX(rX);
        rotateY(rY);
        rotateZ(rZ);

        if (previous != null && next != null) {
            float factor = ((float) currentGRIBStep / (float) nbStepBetweenGRIB);
            // Calculate (smoothed) vertices here
            vertices = new PVector[previous.length][previous[0].length];
            for (int i = 0; i < vertices.length; i++) {
                for (int j = 0; j < vertices[i].length; j++) {
                    float x = previous[i][j].x - (factor * (previous[i][j].x - next[i][j].x));
                    float y = previous[i][j].y - (factor * (previous[i][j].y - next[i][j].y));
                    float z = previous[i][j].z - (factor * (previous[i][j].z - next[i][j].z));
                    vertices[i][j] = new PVector(x, y, z);
                }
            }
            currentGRIBStep++;
            if (currentGRIBStep > nbStepBetweenGRIB) {
                currentGRIBStep = 0;
                currentDateIndex++;
                if (currentDateIndex >= (maxDateIndex - 1)) {
                    currentDateIndex = 0;
                }
                previous = coordinates[currentDateIndex];
                next = coordinates[currentDateIndex + 1];
            }
        } else {
            vertices = coordinates[currentDateIndex];
        }

        // Drawing the skin here, cell by cell
        stroke(0);
        for (int i = 0; vertices != null && i < vertices.length - 1; i++) {
            for (int j = 0; vertices[i] != null && j < vertices[i].length - 1; j++) {
                beginShape();
                vertex(vertices[i][j].x, vertices[i][j].y, vertices[i][j].z);
                vertex(vertices[i + 1][j].x, vertices[i + 1][j].y, vertices[i + 1][j].z);
                vertex(vertices[i + 1][j + 1].x, vertices[i + 1][j + 1].y, vertices[i + 1][j + 1].z);
                vertex(vertices[i][j + 1].x, vertices[i][j + 1].y, vertices[i][j + 1].z);
                endShape(CLOSE);
            }
        }
        // Draw Chart
        stroke(153);
        for (int i = 0; i < chart.length; i++) {
      /*
    double deltaX = Math.abs(chart[i][0].x - chart[i][chart[i].length - 1].x);
    double deltaY = Math.abs(chart[i][0].y - chart[i][chart[i].length - 1].y);
    double dist = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    if (dist > 100)
    {
//    println("Line " + i + ", Dist:" + dist + " begins with:" + chart[i][0].x + "," + chart[i][0].y);
      stroke(255, 0, 0);
    }
    else
      stroke(153);
    */
            try {
                //  beginShape(LINES);
                beginShape();
                try {
                    for (int j = 0; j < chart[i].length - 1; j++) {
                        vertex(chart[i][j].x, chart[i][j].y, chart[i][j].z);
                        vertex(chart[i][j + 1].x, chart[i][j + 1].y, chart[i][j + 1].z);
                    }
                } catch (Exception ex) {
                    out(ex.toString() + " for i=" + i);
                }
                endShape(CLOSE);
                //  endShape();
            } catch (Exception e) {
                out(e.toString() + "...");
            }
        }
    }

    public void out(String s) {
        println(s);
    }

    private final static String FILE_HEADER_NB_DATE = "FileHeaderNbDate:";
    private final static String DATE_HEADER = "DateHeader:";
    private final static String DATE_HEADER_NB_TYPE = "DateHeaderNbType:";
    private final static String DATA_TYPE_HEADER = "DataTypeHeader:";
    private final static String DATA_TYPE_DIM = "DataTypeDim:";
    private final static String END_OF_GRIB_DATA = "EndOFGribData";
    private final static String CHART_HEADER = "ChartHeader:";
    private final static String END_OF_CHART_DATA = "EndOFChartData";

    private final static float PRMSL_OFFSET = 101300f;
    private final static float HGT_OFFSET = 5640f;
    private final static float WAVES_OFFSET = 0f;
    private final static float TMP_OFFSET = 273f;
    private final static float PRATE_OFFSET = 0f;

    private final static float ZOOM = 5.0f;


    public void buildMatrixes(String[] lines, String type) {
        boolean crossAntiM = false;

        PVector[][] vertices = null;

        int w = 0, h = 0;
        int currentW = 0, currentH = 0;
        int currentDateIdx = 0;

        // Read headers unil met first (type) Type
        boolean found = false;
        boolean doneWithGRIB = false;
        boolean goReadChart = false;

        float top = 0f, bot = 0f, left = 0f, right = 0;
        float wAmplitude = 0f, hAmplitude = 0f;
        int chartIdx = 0;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(FILE_HEADER_NB_DATE)) {
                String nbd = lines[i].substring(FILE_HEADER_NB_DATE.length());
                int nbDate = Integer.parseInt(nbd);
                coordinates = new PVector[nbDate][][];
            } else if (!found && lines[i].startsWith(DATA_TYPE_HEADER + type) /*"prmsl"*/) {
                found = true;
                currentH = 0;
            } else if (found && lines[i].startsWith(DATA_TYPE_DIM)) {
                String dim = lines[i].substring(DATA_TYPE_DIM.length());
                StringTokenizer st = new StringTokenizer(dim, "x");
                int idx = 0;
                while (st.hasMoreTokens()) {
                    String s = st.nextToken().trim();
                    if (idx == 0) {
                      h = Integer.parseInt(s);
                    } else if (idx == 1) {
                      w = Integer.parseInt(s);
                    }
                    idx++;
                }
                vertices = new PVector[w][h];
            } else if (found &&
                    (lines[i].startsWith(DATA_TYPE_DIM) || lines[i].startsWith(DATA_TYPE_HEADER) || lines[i].startsWith(FILE_HEADER_NB_DATE) || lines[i].startsWith(DATE_HEADER) || lines[i].startsWith(DATE_HEADER_NB_TYPE) || lines[i].startsWith(END_OF_GRIB_DATA))) {
                // End of section
                found = false;
                coordinates[currentDateIdx++] = vertices;
                if (lines[i].startsWith(END_OF_GRIB_DATA)) {
                    //      out("Done with GRIB Data");
                    doneWithGRIB = true;
                    // w & h already set
                }
            } else if (lines[i].startsWith(END_OF_GRIB_DATA)) {
                found = false;
                //    out("Done with GRIB Data");
                doneWithGRIB = true;
                // w & h already set
            } else if (found) { // Read data here
                // One line
                StringTokenizer st = new StringTokenizer(lines[i]);
                currentW = 0;
                while (st.hasMoreTokens()) {
                    String str = st.nextToken().trim();
                    float f = adjustValue(Float.parseFloat(str), type);
                    float modifiedY = h - (currentH + (h / 2)); // Data and chart are not oriented the same way <->
                    vertices[currentW][currentH] = new PVector((currentW - (w / 2)) * ZOOM, modifiedY * ZOOM, f * ZOOM);
                    currentW++;
                }
                currentH++;
            } else if (doneWithGRIB && lines[i].startsWith(CHART_HEADER)) {
                String ch = lines[i].substring(CHART_HEADER.length());
                StringTokenizer stb = new StringTokenizer(ch, " ");
                //    out("Dimensioning chart:" + (lines.length - i - 2));
                chart = new PVector[lines.length - i - 2][];
                // Boundaries
                int b = 0;
                while (stb.hasMoreTokens()) {
                    if (b == 0) {
                      top = Float.parseFloat(stb.nextToken().trim());
                    } else if (b == 1) {
                      bot = Float.parseFloat(stb.nextToken().trim());
                    } else if (b == 2) {
                      right = Float.parseFloat(stb.nextToken().trim());
                    } else if (b == 3) {
                      left = Float.parseFloat(stb.nextToken().trim());
                    }
                    b++;
                }
                wAmplitude = /*(float)*/ Math.abs(left - right);
                if (wAmplitude > 180) {
                    wAmplitude = 360f - wAmplitude;
                    crossAntiM = true;
                }
                hAmplitude = top - bot;
                goReadChart = true;
            } else if (doneWithGRIB && goReadChart && !lines[i].startsWith(END_OF_CHART_DATA)) {
                StringTokenizer st = new StringTokenizer(lines[i], "|");
                chart[chartIdx] = new PVector[st.countTokens()];

                //    if (chartIdx == 90)
                //      println("Line 90:" + lines[i]);

                int j = 0;
                while (st.hasMoreTokens()) {
                    String s = st.nextToken().trim();
                    StringTokenizer st2 = new StringTokenizer(s, ",");
                    int idx = 0;
                    float lat = 0f, lng = 0f;
                    while (st2.hasMoreTokens()) {
                        float x = Float.parseFloat(st2.nextToken().trim());
                        if (idx++ == 0) {
                          lat = x;
                        } else {
                          lng = (x % 180f);
                        }
                    }
                    float _x = 0;
                    float _y = (((top - lat) / hAmplitude) * h) - (h / 2);
                    if (crossAntiM) {
                        if (lng > 0) {
                          _x = (((lng - left) / wAmplitude) * w) - (w / 2);
                        } else {
                          _x = (((360f - left + lng) / wAmplitude) * w) - (w / 2);
                        }
                    } else {
                      _x = (((lng - left) / wAmplitude) * w) - (w / 2);
                    }
                    //      out("Crossing:" + crossAntiM + ", chart[" + (chartIdx) + "][" + j + "] = " + _x + ", " + _y + " where lng=" + lng + ", left=" + left + ", wAmpl=" + wAmplitude + " and w=" + w);
                    chart[chartIdx][j++] = new PVector(_x * ZOOM, _y * ZOOM, -30f);
                }
                chartIdx++;
            }
        } // End of the for loop on records
    }

    public float adjustValue(float f, String type) {
        float ff = 0;
        if (type.equals("prmsl")) {
          ff = (f - PRMSL_OFFSET) / 200f;
        } else if (type.equals("hgt")) {
          ff = (f - HGT_OFFSET) / 50f;
        } else if (type.equals("htsgw")) {
            if (f > 100) {
              ff = 0;
            } else {
              ff = (f - WAVES_OFFSET);
            }
        } else if (type.equals("prate")) {
          ff = (f - PRATE_OFFSET) * 5000f;
        } else if (type.equals("tmp")) {
          ff = (f - TMP_OFFSET) / 3f;
        }
        return ff;
    }

    public float toDegrees(float f) {
        return (float) Math.toDegrees(f);
    }

    public float toRadians(float f) {
        return (float) Math.toDegrees(f);
    }

    static public void main(String... args) {
        PApplet.main(new String[]
                {"--bgcolor=#ece9d8", "chartadjustmentuserexits.ProcessingGRIBPApplet"});
    }

    public void setDType(String dType) {
        this.dType = dType;
        setup();
    }

    public String getDType() {
        return dType;
    }
}
