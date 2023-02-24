package chartview.gui.right;


import calc.GeoPoint;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.toolbar.controlpanels.ChartCommandPanelToolBar;
import chartview.gui.toolbar.controlpanels.ControlPane;
import chartview.gui.toolbar.controlpanels.LoggingPanel;
import chartview.gui.util.dialog.*;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.GPXUtil;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;
import chartview.util.http.HTTPClient;
import coreutilities.Utilities;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CompositeTabbedPane
        extends JTabbedPane {
    private final BorderLayout borderLayout;
    private final CommandPanel commandPanel;
    private final JPanel commandPanelHolder;
    private final Panel3D threeDGRIBPanel;

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    private final ChartCommandPanelToolBar commandPanelToolbar = new ChartCommandPanelToolBar();
    private CompositeDetailsInputPanel inputPanel = null;
    protected CompositeTabbedPane instance = this;

    private final JPanel chartPanelControlPaneHolder = new JPanel(new BorderLayout());
    private final JScrollPane controlPaneScrollPane = new JScrollPane();
    private final ControlPane ccp = new ControlPane();

    private final transient ApplicationEventListener ael = new ApplicationEventListener() {
        public String toString() {
            return "from CompositeTabbedPane.";
        }

        public void collapseExpandToolBar(boolean b) {
            if (instance.isVisible()) {
              chartPanelControlPaneHolder.setVisible(b);
            }
        }

        public void setCompositeRequested() {
            if (instance.isVisible()) {
              setupComposite();
            }
        }

        public void gribUnloaded() {
            if (instance.getSelectedIndex() == 1) {
              instance.setSelectedIndex(0);
            }
            instance.setEnabledAt(1, false);
            instance.setSelectedIndex(0);
            WWContext.getInstance().setGribFile(null);
        }

        public void enable3DTab(boolean b) {
            if (instance.isVisible()) {
              instance.setEnabledAt(1, b);
            }
        }

        public void gribFileOpen(String str) {
            if (instance.isVisible()) {
              setupComposite(null, str);
            }
        }

        public void faxFileOpen(String str) {
            if (instance.isVisible()) {
              setupComposite(str, null);
            }
        }

        public void setLoading(boolean b) {
//          System.out.println("Set loading... " + b);
            if (instance.isVisible()) {
              setLoadingProgressBar(b);
            }
        }

        public void setLoading(boolean b, String s) {
//          System.out.println("Set loading... " + b);
            if (instance.isVisible()) {
              setLoadingProgressBar(b, s);
            }
        }

        public void stopAnyLoadingProgressBar() {
            if (instance.isVisible()) {
              stopAnyOscillatingThread();
            }
        }

        public void gribForward() {
            if (instance.isVisible()) {
              threeDGRIBPanel.getThreeDPanel().setPanelLabel(commandPanel.getGribData()[commandPanel.getGribIndex()].getDate().toString());
            }
        }

        public void gribBackward() {
            if (instance.isVisible()) {
              threeDGRIBPanel.getThreeDPanel().setPanelLabel(commandPanel.getGribData()[commandPanel.getGribIndex()].getDate().toString());
            }
        }
    };

    public CompositeTabbedPane() {
        borderLayout = new BorderLayout();

//  commandPanel = new CommandPanel(this.mainZoomPanel);
        commandPanel = new CommandPanel(this, ccp.getMainZoomPanel());

        String ttOption = System.getProperty("tooltip.option", "on-chart");
        if ("none".equals(ttOption)) {
            commandPanel.getChartPanel().setPositionToolTipEnabled(false);
            commandPanel.setDisplayAltTooltip(false);
        } else if ("on-chart".equals(ttOption)) {
            commandPanel.getChartPanel().setPositionToolTipEnabled(true);
            commandPanel.setDisplayAltTooltip(false);
        } else if ("tt-window".equals(ttOption)) {
            commandPanel.getChartPanel().setPositionToolTipEnabled(true);
            commandPanel.setDisplayAltTooltip(true);
        }

        commandPanelHolder = new JPanel(new BorderLayout());
        commandPanelHolder.add(commandPanel, BorderLayout.CENTER);
        commandPanelHolder.add(commandPanelToolbar, BorderLayout.NORTH);
        chartPanelControlPaneHolder.add(controlPaneScrollPane, BorderLayout.CENTER);
        controlPaneScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        controlPaneScrollPane.getViewport().add(ccp, null);

        commandPanelHolder.add(chartPanelControlPaneHolder, BorderLayout.EAST);
        chartPanelControlPaneHolder.setVisible((Boolean) ParamPanel.data[ParamData.EXPAND_CONTROLS_BY_DEFAULT][ParamData.VALUE_INDEX]);
        threeDGRIBPanel = new Panel3D();

        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void setLoadingProgressBar(boolean b) {
        setLoadingProgressBar(b, WWGnlUtilities.buildMessage("loading"));
    }

    private int nbLoad = 0;
    private transient OscillateThread oscillate = null;

    private void setLoadingProgressBar(boolean b, final String s) {
        if (b) {
          nbLoad += 1;
        }
        if (!b) {
          nbLoad -= 1;
        }

        final boolean x = (nbLoad > 0);

        oscillate = new OscillateThread(s, x);
        oscillate.start();
    }

    public void stopAnyOscillatingThread() {
        if (oscillate != null) {
          oscillate.abort();
        }
    }

    private void jbInit() {
        WWContext.getInstance().addApplicationListener(ael);
        progressBar.setValue(0);
        progressBar.setString(WWGnlUtilities.buildMessage("loading"));
//  progressBar.setStringPainted(true);
        progressBar.setEnabled(false);

        this.add(WWGnlUtilities.buildMessage("chart"), commandPanelHolder);
        this.add("3D GRIB Data", threeDGRIBPanel);
        this.setEnabledAt(1, false);

        ccp.getProjectionPanel().setSelectedProjection(commandPanel.getProjection());
    }

    public void removeListener() {
        WWContext.getInstance().removeApplicationListener(ael);
        commandPanel.removeListener();
        threeDGRIBPanel.removeListener();
        ccp.removeListener();
        commandPanelToolbar.removeListener();
    }

    private void generatePattern() {
        WWContext.getInstance().fireGeneratePattern();
    }

    private void loadWithPattern() {
        WWContext.getInstance().fireLoadWithPattern();
    }

    private void downloadFromTheWebAndDisplayComposite(String url) {
        WWGnlUtilities.readCompositeFromURL(url);
    }

    private void setLandF(ActionEvent e) {
        String lnf = e.getActionCommand();
        setLandF(lnf);
        WWContext.getInstance().setLookAndFeel(lnf);
    }

    private void setLandF(String lnf) {
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        if (info != null) {
          for (UIManager.LookAndFeelInfo lookAndFeelInfo : info) {
            if (lookAndFeelInfo.getName().equals(lnf)) {
              try {
                UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                SwingUtilities.updateComponentTreeUI(this);
              } catch (Exception ex) {
                WWContext.getInstance().fireExceptionLogging(ex);
                ex.printStackTrace();
              }
              break;
            }
          }
        }
    }

    private void setupComposite() {
        setupComposite(null, WWContext.getInstance().getCurrentGribFileName());
    }

    // TODO Merge that one with the same method in AdjsutFrame
    private void setupComposite(String faxFile, String gribFile) {
        if (inputPanel == null) {
          inputPanel = new CompositeDetailsInputPanel();
        }
        FaxType[] faxarray = commandPanel.getFaxes();
        //  if (faxarray != null)
        inputPanel.setFaxes(faxarray);

        if (faxFile != null) {
          inputPanel.addNewFaxFileInTable(faxFile);
        }
        if (faxFile == null && faxarray == null) { // then it might be a GRIB
          inputPanel.setSizeFromGRIB(true);
        }
        if (gribFile != null && gribFile.trim().length() > 0) {
            inputPanel.setGribFileName(gribFile);
            File gf = new File(gribFile);
            inputPanel.setGRIBRequestSelected(!gf.exists()); // If file not found, assume GRIB Request
        } else {
          inputPanel.setGribFileName("");
        }
        inputPanel.setPRMSL(commandPanel.isDisplayPrmsl() && commandPanel.isTherePrmsl());
        inputPanel.set500mb(commandPanel.isDisplay500mb() && commandPanel.isThere500mb());
        inputPanel.setWaves(commandPanel.isDisplayWaves() && commandPanel.isThereWaves());
        inputPanel.setTemp(commandPanel.isDisplayTemperature() && commandPanel.isThereTemperature());
        inputPanel.setPrate(commandPanel.isDisplayRain() && commandPanel.isThereRain());

        inputPanel.set3DTWS(commandPanel.isDisplay3DTws());
        inputPanel.set3DPRMSL(commandPanel.isDisplay3DPrmsl());
        inputPanel.set3D500hgt(commandPanel.isDisplay3D500mb());
        inputPanel.set3DWaves(commandPanel.isDisplay3DWaves());
        inputPanel.set3DTemp(commandPanel.isDisplay3DTemperature());
        inputPanel.set3DRain(commandPanel.isDisplay3DRain());

        inputPanel.setPRMSLContour(commandPanel.isDisplayContourPRMSL());
        inputPanel.set500mbContour(commandPanel.isDisplayContour500mb());
        inputPanel.setWavesContour(commandPanel.isDisplayContourWaves());
        inputPanel.setTempContour(commandPanel.isDisplayContourTemp());
        inputPanel.setPrateContour(commandPanel.isDisplayContourPrate());

        inputPanel.setComment(commandPanel.getCurrentComment());

        inputPanel.setTopLat(commandPanel.getNLat());
        inputPanel.setBottomLat(commandPanel.getSLat());
        inputPanel.setLeftLong(commandPanel.getWLong());
        inputPanel.setRightLong(commandPanel.getELong());
        int resp = JOptionPane.showConfirmDialog(this,
                inputPanel, WWGnlUtilities.buildMessage("set-data-context"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            final boolean gribChanged = haveGRIBOtpionsChanged(inputPanel, commandPanel);

            boolean[] gribOptions = inputPanel.getGRIBOptions();

            boolean atLeastOne3D = gribOptions[CompositeDetailsInputPanel.TWS_3D] ||
                    gribOptions[CompositeDetailsInputPanel.PRMSL_3D] ||
                    gribOptions[CompositeDetailsInputPanel.MB500_3D] ||
                    gribOptions[CompositeDetailsInputPanel.WAVES_3D] ||
                    gribOptions[CompositeDetailsInputPanel.TEMP_3D] ||
                    gribOptions[CompositeDetailsInputPanel.PRATE_3D];
            boolean atLeastOneContour = gribOptions[CompositeDetailsInputPanel.TWS_CONTOUR] ||
                    gribOptions[CompositeDetailsInputPanel.PRMSL_CONTOUR] ||
                    gribOptions[CompositeDetailsInputPanel.MB500_CONTOUR] ||
                    gribOptions[CompositeDetailsInputPanel.WAVES_CONTOUR] ||
                    gribOptions[CompositeDetailsInputPanel.TEMP_CONTOUR] ||
                    gribOptions[CompositeDetailsInputPanel.PRATE_CONTOUR];
            if (((faxFile != null && faxFile.trim().length() > 0) || (faxarray != null && faxarray.length > 0)) &&
                    /* inputPanel.getGribFileName().trim().length() > 0 && */
                    atLeastOneContour) { // Confirm Faxes + GRIB contour lines
                int response = JOptionPane.showConfirmDialog(this,
                        WWGnlUtilities.buildMessage("confirm-fax-plus-cl"),
                        WWGnlUtilities.buildMessage("weather-data"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    System.out.println("Canceling contour lines");
                    gribOptions[CompositeDetailsInputPanel.TWS_CONTOUR] = false;
                    gribOptions[CompositeDetailsInputPanel.PRMSL_CONTOUR] = false;
                    gribOptions[CompositeDetailsInputPanel.MB500_CONTOUR] = false;
                    gribOptions[CompositeDetailsInputPanel.WAVES_CONTOUR] = false;
                    gribOptions[CompositeDetailsInputPanel.TEMP_CONTOUR] = false;
                    gribOptions[CompositeDetailsInputPanel.PRATE_CONTOUR] = false;
                }
            }
            // commandPanel.setDisplayContour(withContourLines);
            commandPanel.setCurrentComment(inputPanel.getComment());

            commandPanel.setDisplayPrmsl(gribOptions[CompositeDetailsInputPanel.PRMSL_DATA]);
            commandPanel.setDisplay500mb(gribOptions[CompositeDetailsInputPanel.MB500_DATA]);
            commandPanel.setDisplayWaves(gribOptions[CompositeDetailsInputPanel.WAVES_DATA]);
            commandPanel.setDisplayTemperature(gribOptions[CompositeDetailsInputPanel.TEMP_DATA]);
            commandPanel.setDisplayRain(gribOptions[CompositeDetailsInputPanel.PRATE_DATA]);

            commandPanel.setDisplay3DTws(gribOptions[CompositeDetailsInputPanel.TWS_3D]);
            commandPanel.setDisplay3DPrmsl(gribOptions[CompositeDetailsInputPanel.PRMSL_3D]);
            commandPanel.setDisplay3D500mb(gribOptions[CompositeDetailsInputPanel.MB500_3D]);
            commandPanel.setDisplay3DWaves(gribOptions[CompositeDetailsInputPanel.WAVES_3D]);
            commandPanel.setDisplay3DTemperature(gribOptions[CompositeDetailsInputPanel.TEMP_3D]);
            commandPanel.setDisplay3DRain(gribOptions[CompositeDetailsInputPanel.PRATE_3D]);
            if (atLeastOne3D) {
                instance.setEnabledAt(1, true);
                if (commandPanel.getGribData() != null) {
                  threeDGRIBPanel.getThreeDPanel().setPanelLabel(commandPanel.getGribData()[commandPanel.getGribIndex()].getDate().toString());
                }
            } else {
                if (this.getSelectedIndex() == 1) {
                  this.setSelectedIndex(0);
                }
                this.setEnabledAt(1, false);
            }
            commandPanel.setDisplayContourTWS(gribOptions[CompositeDetailsInputPanel.TWS_CONTOUR]);
            commandPanel.setDisplayContourPRMSL(gribOptions[CompositeDetailsInputPanel.PRMSL_CONTOUR]);
            commandPanel.setDisplayContour500mb(gribOptions[CompositeDetailsInputPanel.MB500_CONTOUR]);
            commandPanel.setDisplayContourWaves(gribOptions[CompositeDetailsInputPanel.WAVES_CONTOUR]);
            commandPanel.setDisplayContourTemp(gribOptions[CompositeDetailsInputPanel.TEMP_CONTOUR]);
            commandPanel.setDisplayContourPrate(gribOptions[CompositeDetailsInputPanel.PRATE_CONTOUR]);

            // GPX Data?
            if (inputPanel.thereIsGPXData()) {
                try {
                    String gpxDataFileName = inputPanel.getGPXFileName();
                    long to = -1L;
                    Date date = inputPanel.getUpToDate();
                    if (date != null) {
                      to = date.getTime();
                    }
                    List<GeoPoint> algp = GPXUtil.parseGPXData(new File(gpxDataFileName).toURI().toURL(), -1L, to);
                    commandPanel.setGPXData(algp);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else
                commandPanel.setGPXData(null);

            // System.out.println("InputPanel OK");
            WWContext.getInstance().fireSetLoading(true);
            Thread loader = new Thread("composite-data-loader") {
                public void run() {
                    // System.out.println("Top of loader thread");
                    String grib = inputPanel.getGribFileName();
                    // if (!(grib.trim().length() > 0 && inputPanel.isSizeFromGRIB()))
                    {
                        commandPanel.setNLat(inputPanel.getTopLat());
                        commandPanel.setSLat(inputPanel.getBottomLat());
                        commandPanel.setWLong(inputPanel.getLeftLong());
                        commandPanel.setELong(inputPanel.getRightLong());
                        commandPanel.applyBoundariesChanges();
                    }
                    if (grib.trim().length() > 0) {
                        boolean keepGoing = true;
                        GribHelper.GribConditionData[] wgd = null;
                        if (commandPanel.getGribData() != null &&
                                inputPanel.isGRIBRequestSelected() &&
                                grib.equals(commandPanel.getGRIBDataName())) {
                            // Ask if we reload the GRIB (if the GRIB does not come from a waz)
                            String ccName = WWContext.getInstance().getCurrentComposite();
                            if (ccName != null && ccName.endsWith(WWContext.WAZ_EXTENSION) && !gribChanged) {
                                wgd = commandPanel.getGribData();
                                keepGoing = false;
                            }
                            if (keepGoing) {
                                if (ccName != null && ccName.endsWith(WWContext.WAZ_EXTENSION) && gribChanged) {
                                    wgd = commandPanel.getGribData();
                                    keepGoing = false;
                                } else {
                                    int resp = JOptionPane.showConfirmDialog(commandPanel,
                                            WWGnlUtilities.buildMessage("reload-grib-data"),
                                            WWGnlUtilities.buildMessage("grib-download"),
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (resp == JOptionPane.NO_OPTION) {
                                        wgd = commandPanel.getGribData();
                                        keepGoing = false;
                                    }
                                }
                            }
                        }
                        if (keepGoing) {
                            if (inputPanel.isGRIBRequestSelected()) { // Then we assume it is to be reached through http
                                commandPanel.setGribRequest(grib);
                                String gribRequest = WWGnlUtilities.generateGRIBRequest(grib);
                                try {
                                    System.out.println(gribRequest);
                                    URL saildocs = new URL(gribRequest);
                                    URLConnection connection = saildocs.openConnection();
                                    connection.connect();
                                    //    DataInputStream dis = new DataInputStream(connection.getInputStream());
                                    InputStream dis = connection.getInputStream();

                                    long waiting = 0L;
                                    while (dis.available() == 0 && waiting < 30L) { // 30s Timeout...
                                        Thread.sleep(1_000L);
                                        waiting += 1L;
                                    }
                                    //                System.out.println("Waiting: " + waiting);
                                    //                System.out.println("Available:" + dis.available());

                                    if (true) {
                                        final int BUFFER_SIZE = 65_536;
                                        byte[] aByte = new byte[BUFFER_SIZE];
                                        byte[] content = null;
                                        int nBytes;
                                        while ((nBytes = dis.read(aByte, 0, BUFFER_SIZE)) != -1) {
                                          content = Utilities.appendByteArrays(content, aByte, nBytes);
                                        }
                                        WWContext.getInstance().fireSetStatus("Read " + NumberFormat.getInstance().format(content.length) + " bytes of GRIB data.");
                                        System.out.println("Read " + content.length + " bytes.");
                                        dis.close();
                                        ByteArrayInputStream bais = new ByteArrayInputStream(content);
                                        dis = bais; // switch
                                    }
                                    wgd = GribHelper.getGribData(dis, grib); // From an InputStream
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                    wgd = GribHelper.getGribData(grib, true);  // From a File
                                } catch (RuntimeException rte) {
                                    String mess = rte.getMessage();
//                  System.out.println("RuntimeException getMessage(): [" + mess + "]");
                                    if (mess.startsWith("DataArray (width) size mismatch")) {
                                      System.out.println(mess);
                                    } else {
                                      throw rte;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (inputPanel.isSizeFromGRIB() && wgd != null) {
                            commandPanel.setNLat(wgd[0].getNLat());
                            commandPanel.setSLat(wgd[0].getSLat());
                            commandPanel.setWLong(wgd[0].getWLng());
                            commandPanel.setELong(wgd[0].getELng());
                        }
                        commandPanel.applyBoundariesChanges();
                        commandPanel.setGribData(wgd, grib); // Event sent, generates obj, etc.
                    }
                    // System.out.println("Setting command panel boundaries:" + commandPanel.getNLat() + " to " + commandPanel.getSLat() + ", and " + commandPanel.getWLong() + " to " + commandPanel.getELong());

                    FaxType[] faxes = inputPanel.getFaxes();
                    if (faxes != null) {
                      for (FaxType fax : faxes) {
                        // System.out.println("Rank: " + (i+1) + ", " + faxes[i].getValue() + ", " + faxes[i].getColor());
                        String comment = WWGnlUtilities.getHeader(fax.getValue());
                        fax.setComment(comment);
                      }
                      commandPanel.setFaxes(faxes);
                    }
                    // System.out.println("End of loader thread.");
                    WWContext.getInstance().fireSetLoading(false);
                }
            };
            loader.start();
        }
    }

    private boolean haveGRIBOtpionsChanged(CompositeDetailsInputPanel ip, CommandPanel cp) {
        boolean[] gribOptions = ip.getGRIBOptions();
        return (gribOptions[CompositeDetailsInputPanel.PRMSL_DATA] != cp.isDisplayPrmsl() ||
                gribOptions[CompositeDetailsInputPanel.MB500_DATA] != cp.isDisplay500mb() ||
                gribOptions[CompositeDetailsInputPanel.WAVES_DATA] != cp.isDisplayWaves() ||
                gribOptions[CompositeDetailsInputPanel.TEMP_DATA] != cp.isDisplayTemperature() ||
                gribOptions[CompositeDetailsInputPanel.PRATE_DATA] != cp.isDisplayRain() ||
                gribOptions[CompositeDetailsInputPanel.TWS_3D] != cp.isDisplay3DTws() ||
                gribOptions[CompositeDetailsInputPanel.PRMSL_3D] != cp.isDisplay3DPrmsl() ||
                gribOptions[CompositeDetailsInputPanel.MB500_3D] != cp.isDisplay3D500mb() ||
                gribOptions[CompositeDetailsInputPanel.WAVES_3D] != cp.isDisplay3DWaves() ||
                gribOptions[CompositeDetailsInputPanel.TEMP_3D] != cp.isDisplay3DTemperature() ||
                gribOptions[CompositeDetailsInputPanel.PRATE_3D] != cp.isDisplay3DRain() ||
                gribOptions[CompositeDetailsInputPanel.TWS_CONTOUR] != cp.isDisplayContourTWS() ||
                gribOptions[CompositeDetailsInputPanel.PRMSL_CONTOUR] != cp.isDisplayContourPRMSL() ||
                gribOptions[CompositeDetailsInputPanel.MB500_CONTOUR] != cp.isDisplayContour500mb() ||
                gribOptions[CompositeDetailsInputPanel.WAVES_CONTOUR] != cp.isDisplayContourWaves() ||
                gribOptions[CompositeDetailsInputPanel.TEMP_CONTOUR] != cp.isDisplayContourTemp() ||
                gribOptions[CompositeDetailsInputPanel.PRATE_CONTOUR] != cp.isDisplayContourPrate());
    }

    void fileOpen_ActionPerformed(ActionEvent e) {
        setupComposite(null, WWContext.getInstance().getCurrentGribFileName());
    }

    void filePrint_ActionPerformed(ActionEvent e) {
        commandPanel.print();
    }

    void downloadFaxFromInternet() {
        final InternetFax inf = new InternetFax();
        int resp = JOptionPane.showConfirmDialog(this, inf, WWGnlUtilities.buildMessage("download-from-internet"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION && inf.getFaxLocalFile().trim().length() > 0) {
            File f = new File(inf.getFaxLocalFile());
            boolean go = true;
            if (f.exists()) {
                int r = JOptionPane.showConfirmDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("already-exists-override", new String[]{inf.getFaxLocalFile()}), WWGnlUtilities.buildMessage("download"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) {
                  go = false;
                }
            }
            if (go) {
                Thread downLoadThread = new Thread("internet-fax-loader") {
                    public void run() {
                        String saveAs = inf.getFaxLocalFile();
                        WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{inf.getFaxStrURL()}) + "\n", LoggingPanel.WHITE_STYLE);
                        try {
                            HTTPClient.getChart(inf.getFaxStrURL(), ".", saveAs, true);
                            JOptionPane.showMessageDialog(instance, WWGnlUtilities.buildMessage("is-ready", new String[]{saveAs}), WWGnlUtilities.buildMessage("fax-download"), JOptionPane.INFORMATION_MESSAGE);
                            //          allJTrees.refreshFaxTree();
                            if ("false".equals(System.getProperty("headless", "false"))) {
                              WWContext.getInstance().fireReloadFaxTree();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                downLoadThread.start();
            }
        }
    }

    void downloadGRIBFromInternet() {
        final InternetGRIB ing = new InternetGRIB();
        int resp = JOptionPane.showConfirmDialog(this,
                ing, WWGnlUtilities.buildMessage("download-from-internet"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION && ing.getGRIBLocalFile().trim().length() > 0) {
            File f = new File(ing.getGRIBLocalFile());
            boolean go = true;
            if (f.exists()) {
                int r = JOptionPane.showConfirmDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("already-exists-override",
                                new String[]{ing.getGRIBLocalFile()}), WWGnlUtilities.buildMessage("download"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) {
                  go = false;
                }
            }
            if (go) {
                Thread downLoadThread = new Thread("grib-downloader") {
                    public void run() {
                        String saveAs = ing.getGRIBLocalFile();
                        WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{ing.getGRIBLocalFile()}) + "\n", LoggingPanel.WHITE_STYLE);
                        try {
                            HTTPClient.getGRIB(WWGnlUtilities.generateGRIBRequest(ing.getGRIBRequest()), ".", saveAs, true);
                        } catch (Exception ex) {
                            if (ex instanceof HTTPClient.CannotWriteException) {
                                JOptionPane.showMessageDialog(instance,
                                        "Check your write permissions on " + new File(".").getAbsolutePath() + " !",
                                        WWGnlUtilities.buildMessage("grib-download"),
                                        JOptionPane.WARNING_MESSAGE);
                            } else {
                              ex.printStackTrace();
                            }
                        }
                        JOptionPane.showMessageDialog(instance, WWGnlUtilities.buildMessage("is-ready", new String[]{saveAs}), WWGnlUtilities.buildMessage("grib-download"), JOptionPane.INFORMATION_MESSAGE);
                        // allJTrees.refreshGribTree();
                        if ("false".equals(System.getProperty("headless", "false"))) {
                          WWContext.getInstance().fireReloadGRIBTree();
                        }
                    }
                };
                downLoadThread.start();
            }
        }
    }

    private final static String AUTO_DOWNLOAD_CONFIG_FILE_NAME = "config" + File.separator + "autodownload.xml";

    private void setupDownload() {
        // Build the object
        // fax - url - dir - pattern
        Object data[][] = null;
        File autoDownloadConfigFile = new File(AUTO_DOWNLOAD_CONFIG_FILE_NAME);
        if (autoDownloadConfigFile.exists()) {
            DOMParser parser = WWContext.getInstance().getParser();
            try {
                XMLDocument doc = null;
                synchronized (parser) {
                    parser.setValidationMode(XMLParser.NONVALIDATING);
                    parser.parse(autoDownloadConfigFile.toURI().toURL());
                    doc = parser.getDocument();
                }
                NodeList documents = doc.selectNodes("/fax-collection/*");
                data = new Object[documents.getLength()][6];
                for (int i = 0; i < documents.getLength(); i++) {
                    XMLElement document = (XMLElement) documents.item(i);
                    if (document.getNodeName().equals("fax")) {
                        String faxName = document.getAttribute("name");
                        String faxUrl = document.getAttribute("url");
                        String faxDir = document.getAttribute("dir");
                        String faxPrefix = document.getAttribute("prefix");
                        String faxPattern = document.getAttribute("pattern");
                        String faxExt = document.getAttribute("extension");
                        data[i][0] = faxName;
                        data[i][1] = faxUrl;
                        data[i][2] = new ParamPanel.DataDirectory("Faxes", faxDir);
                        data[i][3] = faxPrefix;
                        data[i][4] = faxPattern;
                        data[i][5] = faxExt;
                    } else if (document.getNodeName().equals("grib")) {
                        String gribName = document.getAttribute("name");
                        String gribRequest = document.getAttribute("request");
                        String gribDir = document.getAttribute("dir");
                        String gribPrefix = document.getAttribute("prefix");
                        String gribPattern = document.getAttribute("pattern");
                        String gribExt = document.getAttribute("extension");
                        data[i][0] = gribName;
                        data[i][1] = gribRequest;
                        data[i][2] = new ParamPanel.DataDirectory("GRIBs", gribDir);
                        data[i][3] = gribPrefix;
                        data[i][4] = gribPattern;
                        data[i][5] = gribExt;
                    }
                }
            } catch (Exception ex) {
                WWContext.getInstance().fireExceptionLogging(ex);
                ex.printStackTrace();
            }
        } else
            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("does-not-exist",
                    new String[]{autoDownloadConfigFile.getAbsolutePath()}) + "\n");
        // Display JTable
        if (data != null) {
          WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("found-faxes", new String[]{Integer.toString(data.length)}) + "\n");
        }
        AutoDownloadTablePanel autoTablePanel = new AutoDownloadTablePanel();
        autoTablePanel.setData(data);
        int resp = JOptionPane.showConfirmDialog(this,
                autoTablePanel, WWGnlUtilities.buildMessage("automatic-download"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        // Write the file back
        if (resp == JOptionPane.OK_OPTION) {
            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("validating") + "\n");
            data = autoTablePanel.getData();
            XMLDocument doc = new XMLDocument();
            Element root = doc.createElement("fax-collection");
            doc.appendChild(root);
            for (int i = 0; i < data.length; i++) {
                String urlOrRequest = (String) data[i][1];
                if (urlOrRequest.startsWith("http://")) {
                    XMLElement fax = (XMLElement) doc.createElement("fax");
                    fax.setAttribute("name", (String) data[i][0]);
                    fax.setAttribute("url", (String) data[i][1]);
                    fax.setAttribute("dir", ((ParamPanel.DataDirectory) data[i][2]).toString());
                    fax.setAttribute("prefix", (String) data[i][3]);
                    fax.setAttribute("pattern", (String) data[i][4]);
                    fax.setAttribute("extension", (String) data[i][5]);
                    root.appendChild(fax);
                } else {
                    XMLElement grib = (XMLElement) doc.createElement("grib");
                    grib.setAttribute("name", (String) data[i][0]);
                    grib.setAttribute("request", (String) data[i][1]);
                    grib.setAttribute("dir", ((ParamPanel.DataDirectory) data[i][2]).toString());
                    grib.setAttribute("prefix", (String) data[i][3]);
                    grib.setAttribute("pattern", (String) data[i][4]);
                    grib.setAttribute("extension", (String) data[i][5]);
                    root.appendChild(grib);
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(AUTO_DOWNLOAD_CONFIG_FILE_NAME);
                doc.print(fos);
                fos.close();
            } catch (Exception e) {
                WWContext.getInstance().fireExceptionLogging(e);
                e.printStackTrace();
            }
        }
    }

    void startAutoDownload() {
        File autoDownloadConfigFile = new File(AUTO_DOWNLOAD_CONFIG_FILE_NAME);
        if (!autoDownloadConfigFile.exists()) {
            JOptionPane.showMessageDialog(this, WWGnlUtilities.buildMessage("auto-download-not-configured"), WWGnlUtilities.buildMessage("automatic-download"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            DOMParser parser = WWContext.getInstance().getParser();
            try {
                XMLDocument doc = null;
                synchronized (parser) {
                    parser.setValidationMode(XMLParser.NONVALIDATING);
                    parser.parse(autoDownloadConfigFile.toURI().toURL());
                    doc = parser.getDocument();
                }
                final NodeList downloadableDocuments = doc.selectNodes("/fax-collection/*");
                Thread autoDownload = new Thread("auto-download-thread") {
                    public void run() {
                        String finalMess = WWGnlUtilities.buildMessage("your-faxes");
                        int fax = 0, grib = 0;
                        for (int i = 0; i < downloadableDocuments.getLength(); i++) {
                            XMLElement document = (XMLElement) downloadableDocuments.item(i);
                            if (document.getNodeName().equals("fax")) {
                                fax++;
                                String faxUrl = document.getAttribute("url");
                                String faxDir = document.getAttribute("dir");
                                String faxPrefix = document.getAttribute("prefix");
                                String faxPattern = document.getAttribute("pattern");
                                String faxExt = document.getAttribute("extension");
                                SimpleDateFormat sdf = new SimpleDateFormat(faxPattern);

                                Date now = new Date();
                                faxDir = WWGnlUtilities.translatePath(faxDir, now);

                                String fileName = faxDir + File.separator + faxPrefix + sdf.format(now) + "." + faxExt;

                                WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{faxUrl}) + "\n", LoggingPanel.WHITE_STYLE);
                                File fDir = new File(faxDir);
                                if (!fDir.exists()) {
                                  fDir.mkdirs();
                                }
                                try {
                                    HTTPClient.getChart(faxUrl, faxDir, fileName, true);
                                    finalMess += ("- " + fileName + "\n");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else if (document.getNodeName().equals("grib")) {
                                grib++;
                                String request = document.getAttribute("request");
                                String gribDir = document.getAttribute("dir");
                                String girbPrefix = document.getAttribute("prefix");
                                String gribPattern = document.getAttribute("pattern");
                                String gribExt = document.getAttribute("extension");
                                SimpleDateFormat sdf = new SimpleDateFormat(gribPattern);
                                Date now = new Date();
                                gribDir = WWGnlUtilities.translatePath(gribDir, now);
                                File gDir = new File(gribDir);
                                if (!gDir.exists()) {
                                  gDir.mkdirs();
                                }
                                String fileName = gribDir + File.separator + girbPrefix + sdf.format(new Date()) + "." + gribExt;

                                WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{request}) + "\n", LoggingPanel.WHITE_STYLE);
                                try {
                                    HTTPClient.getGRIB(WWGnlUtilities.generateGRIBRequest(request), gribDir, fileName, true);
                                } catch (Exception ex) {
                                    if (ex instanceof HTTPClient.CannotWriteException) {
                                        JOptionPane.showMessageDialog(instance,
                                                "Check your write permissions on " + new File(gribDir).getAbsolutePath() + " !",
                                                WWGnlUtilities.buildMessage("grib-download"),
                                                JOptionPane.WARNING_MESSAGE);
                                    } else
                                        ex.printStackTrace();
                                }

                                finalMess += ("- " + fileName + "\n");
                            }
                        }
                        finalMess += WWGnlUtilities.buildMessage("are-ready");
                        JOptionPane.showMessageDialog(instance, finalMess, WWGnlUtilities.buildMessage("automatic-download"), JOptionPane.INFORMATION_MESSAGE);
                        if (fax > 0 && "false".equals(System.getProperty("headless", "false"))) {
                          // allJTrees.refreshFaxTree();
                          WWContext.getInstance().fireReloadFaxTree();
                        }
                        if (grib > 0 && "false".equals(System.getProperty("headless", "false"))) {
                          // allJTrees.refreshGribTree();
                          WWContext.getInstance().fireReloadGRIBTree();
                        }
                    }
                };
                autoDownload.start();
            } catch (Exception ex) {
                WWContext.getInstance().fireExceptionLogging(ex);
                ex.printStackTrace();
            }
        }
    }

    void genImage_actionPerformed(ActionEvent e) {
        commandPanel.genImage();
    }

    private void store() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.store();
        }
    }

    private void restore() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.restore();
        }
    }

//    private void gMap() {
//        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
//            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
//            l.googleMapRequested();
//        }
//    }

//    private void gEarth() {
//        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
//            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
//            l.googleEarthRequested();
//        }
//    }

    private void showHelp() {
        try {
            // String lang = Locale.getDefault().getLanguage();
            // System.out.println("I speak " + lang);
            // String docFileName = System.getProperty("user.dir") + File.separator + "doc" + File.separator + "weather" + File.separator + "index.html";
            String docFileName =
                    "." + File.separator + "doc" + File.separator + "weather" +
                            File.separator + "index.html";
            Utilities.openInBrowser(docFileName);
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    public CommandPanel getCommandPanel() {
        return commandPanel;
    }

    public Panel3D getThreeDGRIBPanel() {
        return threeDGRIBPanel;
    }

    public class OscillateThread extends Thread {
        String txt = "";
        boolean b = true;

        public OscillateThread(String txt, boolean b) {
            super("oscillator-2");
            this.txt = txt;
            this.b = b;
        }

        public void run() {
            progressBar.setIndeterminate(b);
            progressBar.setString(txt);
            progressBar.setStringPainted(b);
            progressBar.setEnabled(b);
            progressBar.repaint();
        }

        public void abort() {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
            progressBar.setStringPainted(false);
            progressBar.repaint();
        }
    }
}
