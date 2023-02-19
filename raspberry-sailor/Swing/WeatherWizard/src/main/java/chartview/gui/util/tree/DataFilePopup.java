package chartview.gui.util.tree;

import chart.components.ui.ChartPanelInterface;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.gui.right.CommandPanel;
import chartview.gui.util.dialog.FaxPatternEditTablePanel;
import chartview.gui.util.dialog.FaxType;
import chartview.gui.util.dialog.FileFilterPanel;
import chartview.gui.util.dialog.PatternEditorPanel;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;
import chartview.util.http.HTTPClient;
import coreutilities.Utilities;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataFilePopup
        extends JPopupMenu
        implements ActionListener,
                   PopupMenuListener {
    private final JMenuItem show;
    private final JMenuItem showNewTab;
    private final JMenuItem refresh;
    private final JMenuItem filter;
    private final JMenuItem gribDetails;
    private final JMenuItem edit;
    private final JMenuItem rename;
    private final JMenuItem copyFavorite;
    private final JMenuItem copyLocally;
    private final JMenuItem fileSystem;
    private final JMenu sortByName;

    private final JMenuItem sortByNameAsc;
    private final JMenuItem sortByNameDesc;

    private final JMenu sortByDate;

    private final JMenuItem sortByDateAsc;
    private final JMenuItem sortByDateDesc;

    private final JMenuItem archiveComposite;
    private final JMenuItem unarchiveComposite;
    private final JMenuItem archiveCompositeDir;

    private final JTreeFilePanel parent;

    private final static String SHOW = WWGnlUtilities.buildMessage("show");
    private final static String SHOW_NEW_TAB = WWGnlUtilities.buildMessage("show-in-new-tab");
    private final static String REFRESH = WWGnlUtilities.buildMessage("refresh-tree");
    private final static String FILTER = WWGnlUtilities.buildMessage("filter");
    private final static String GRIB_DETAILS = WWGnlUtilities.buildMessage("grib-details");
    private final static String EDIT = WWGnlUtilities.buildMessage("edit");
    private final static String RENAME = WWGnlUtilities.buildMessage("rename");
    private final static String COPY_TO_FAVORITE = WWGnlUtilities.buildMessage("copy-to-favorite");
    private final static String SHOW_FILE_SYSTEM = WWGnlUtilities.buildMessage("show-file-system");
    private final static String SORT_BY_NAME = WWGnlUtilities.buildMessage("sort-by-name");
    private final static String SORT_BY_NAME_ASC = WWGnlUtilities.buildMessage("ascending");
    private final static String SORT_BY_NAME_DESC = WWGnlUtilities.buildMessage("descending");
    private final static String SORT_BY_DATE = WWGnlUtilities.buildMessage("sort-by-date");
    private final static String SORT_BY_DATE_ASC = WWGnlUtilities.buildMessage("ascending");
    private final static String SORT_BY_DATE_DESC = WWGnlUtilities.buildMessage("descending");
    private final static String ARCHIVE_COMPOSITE = WWGnlUtilities.buildMessage("archive-composite");
    private final static String UNARCHIVE_COMPOSITE = WWGnlUtilities.buildMessage("unarchive-composite");
    private final static String ARCHIVE_COMPOSITE_DIR = WWGnlUtilities.buildMessage("archive-composite-dir");
    private final static String COPY_LOCALLY = WWGnlUtilities.buildMessage("copy-locally");

    private final static String FAVORITE_DIRECTORY_NAME = "01.Favorites";

    private DefaultMutableTreeNode dtn = null;
    private DefaultMutableTreeNode[] dtnArray = null;

    public DataFilePopup(JTreeFilePanel caller) {
        super();
        this.parent = caller;
        this.add(show = new JMenuItem(SHOW));
        show.addActionListener(this);
        this.add(showNewTab = new JMenuItem(SHOW_NEW_TAB));
        showNewTab.addActionListener(this);
        this.add(refresh = new JMenuItem(REFRESH));
        refresh.addActionListener(this);
        this.add(filter = new JMenuItem(FILTER));
        filter.addActionListener(this);
        this.add(gribDetails = new JMenuItem(GRIB_DETAILS));
        gribDetails.addActionListener(this);
        this.add(edit = new JMenuItem(EDIT));
        edit.addActionListener(this);
        this.add(rename = new JMenuItem(RENAME));
        rename.addActionListener(this);
        this.add(copyFavorite = new JMenuItem(COPY_TO_FAVORITE));
        copyFavorite.addActionListener(this);
        this.add(copyLocally = new JMenuItem(COPY_LOCALLY));
        copyLocally.addActionListener(this);
        this.add(fileSystem = new JMenuItem(SHOW_FILE_SYSTEM));
        fileSystem.addActionListener(this);
        this.add(new JSeparator());
        this.add(sortByDate = new JMenu(SORT_BY_DATE));
        sortByDate.add(sortByDateAsc = new JMenuItem(SORT_BY_DATE_ASC));
        sortByDateAsc.addActionListener(this);
        sortByDate.add(sortByDateDesc = new JMenuItem(SORT_BY_DATE_DESC));
        sortByDateDesc.addActionListener(this);

        this.add(sortByName = new JMenu(SORT_BY_NAME));
        sortByName.add(sortByNameAsc = new JMenuItem(SORT_BY_NAME_ASC));
        sortByNameAsc.addActionListener(this);
        sortByName.add(sortByNameDesc = new JMenuItem(SORT_BY_NAME_DESC));
        sortByNameDesc.addActionListener(this);

        this.add(new JSeparator());
        this.add(archiveComposite = new JMenuItem(ARCHIVE_COMPOSITE));
        archiveComposite.addActionListener(this);
        this.add(unarchiveComposite = new JMenuItem(UNARCHIVE_COMPOSITE));
        unarchiveComposite.addActionListener(this);
        this.add(archiveCompositeDir = new JMenuItem(ARCHIVE_COMPOSITE_DIR));
        archiveCompositeDir.addActionListener(this);
    }

    private void refreshTree() {
        Thread refreshThread = new Thread("tree-refresher") {
            public void run() {
                WWContext.getInstance().fireSetLoading(true, WWGnlUtilities.buildMessage("refreshing"));
                if (withCommentFilter) {
                    parent.reloadTree(fileFilter, regExp, commentFilter, commentRegExp);
                } else {
                    parent.reloadTree(fileFilter, regExp);
                }
//      ((DefaultTreeModel)parent.getJTree().getModel()).reload(parent.getRoot());
                WWContext.getInstance().fireSetLoading(false, WWGnlUtilities.buildMessage("refreshing"));
            }
        };
        refreshThread.start();
    }

    private FileFilterPanel ffp = null;
    private String fileFilter = "";
    private boolean regExp = false;
    private boolean withCommentFilter = false;
    private String commentFilter = "";
    private boolean commentRegExp = false;

    private void filter() {
        // Filter
        if (ffp == null) {
            ffp = new FileFilterPanel(parent.getType() == JTreeFilePanel.COMPOSITE_TYPE);
        }
        int resp = JOptionPane.showConfirmDialog(parent,
                ffp, WWGnlUtilities.buildMessage("file-filter"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            fileFilter = ffp.getFilter();
            regExp = ffp.isRegExpr();
            if (ffp.filterOnComment()) {
                withCommentFilter = true;
                commentFilter = ffp.getCommentFilter();
                commentRegExp = ffp.isCommentRegExp();
            } else {
                withCommentFilter = false;
            }
        }
        // Refresh
        refreshTree();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SHOW)) {
            String fName = ((JTreeFilePanel.DataFileTreeNode) dtn).getFullFileName();
            // System.out.println("Selected :" + fName);
            parent.fireFileOpen(fName);
        } else if (event.getActionCommand().equals(SHOW_NEW_TAB)) {
            String fName = ((JTreeFilePanel.DataFileTreeNode) dtn).getFullFileName();
            // String tabName = ((JTreeFilePanel.DataFileTreeNode) dtn).name;
            // if (tabName.endsWith(".ptrn"))
            //   tabName = tabName.substring(0, tabName.length() - ".ptrn".length());
            ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).addCompositeTab(); // tabName);
            // System.out.println("Selected :" + fName);
            parent.fireFileOpen(fName);
        } else if (event.getActionCommand().equals(REFRESH)) {
            refreshTree();
        } else if (event.getActionCommand().equals(FILTER)) {
            filter();
        } else if (event.getActionCommand().equals(GRIB_DETAILS)) {
            JTreeFilePanel.DataFileTreeNode grib = (JTreeFilePanel.DataFileTreeNode) dtn;
            try {
                GribHelper.displayGRIBDetails(grib.getFullFileName());
            } catch (RuntimeException rte) {
                String mess = rte.getMessage();
                // System.out.println("RuntimeException getMessage(): [" + mess + "]");
                if (mess.startsWith("DataArray (width) size mismatch")) {
                    System.out.println(mess);
                } else {
                    throw rte;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (event.getActionCommand().equals(EDIT)) {
            // Populate the data
            // System.out.println("Parent:" + dtn.getClass().getName());
            if (dtn instanceof JTreeFilePanel.PatternFileTreeNode) {
                JTreeFilePanel.PatternFileTreeNode pftn = (JTreeFilePanel.PatternFileTreeNode) dtn;
                String dir = pftn.dir;
                String name = pftn.name;
                String fullpath = dir + File.separator + name;
                DOMParser parser = WWContext.getInstance().getParser();
                try {
                    synchronized (parser) {
                        parser.setValidationMode(XMLParser.NONVALIDATING);
                        parser.parse(new File(fullpath).toURI().toURL());
                        XMLDocument doc = parser.getDocument();
                        String author = "";
                        try {
                            author = ((XMLElement) doc.selectNodes("//author").item(0)).getAttribute("name");
                        } catch (Exception ex) {
                            // System.out.println("No Author.");
                        }
                        NodeList faxList = doc.selectNodes("//fax-collection/fax");
                        // System.out.println("Found " + faxList.getLength() + " fax(es)");
                        Object[][] faxData = new Object[faxList.getLength()][13];
                        for (int i = 0; i < faxList.getLength(); i++) {
                            XMLElement faxrow = (XMLElement) faxList.item(i);
                            NodeList dynamics = faxrow.selectNodes("./dynamic-resource");
                            String faxOrigin = null;
                            if (dynamics.getLength() > 0) {
                                XMLElement dyn = (XMLElement) dynamics.item(0);
                                faxOrigin = dyn.getAttribute("url");
                            }
                            boolean transparent = Boolean.parseBoolean(faxrow.getAttribute("transparent"));
                            boolean colorChange = Boolean.parseBoolean(faxrow.getAttribute("color-change"));
                            String faxName = faxrow.getAttribute("hint");
                            faxData[i][FaxPatternEditTablePanel.HINT_COL] = new FaxType(faxName,
                                    WWGnlUtilities.buildColor(faxrow.getAttribute("color")),
                                    true,
                                    transparent,
                                    0D,
                                    faxOrigin,
                                    faxName,
                                    colorChange);
                            faxData[i][FaxPatternEditTablePanel.TRANSPARENT_COL] = transparent;
                            if (dynamics.getLength() > 0) {
                                XMLElement dyn = (XMLElement) dynamics.item(0);
                                faxData[i][FaxPatternEditTablePanel.DYNAMIC_COL] = Boolean.TRUE;
                                faxData[i][FaxPatternEditTablePanel.CHANGE_COLOR_COL] = colorChange;
                                faxData[i][FaxPatternEditTablePanel.FAX_URL_COL] = dyn.getAttribute("url");
                                faxData[i][FaxPatternEditTablePanel.FAX_DIR_COL] = new ParamPanel.DataDirectory("Faxes", dyn.getAttribute("dir"));
                                faxData[i][FaxPatternEditTablePanel.FAX_PREFIX_COL] = dyn.getAttribute("prefix");
                                faxData[i][FaxPatternEditTablePanel.FAX_PATTERN_COL] = dyn.getAttribute("pattern");
                                faxData[i][FaxPatternEditTablePanel.FAX_EXT_COL] = dyn.getAttribute("extension");
                            } else {
                                faxData[i][FaxPatternEditTablePanel.DYNAMIC_COL] = Boolean.FALSE;
                                faxData[i][FaxPatternEditTablePanel.CHANGE_COLOR_COL] = Boolean.TRUE;
                                faxData[i][FaxPatternEditTablePanel.FAX_URL_COL] = "";
                                faxData[i][FaxPatternEditTablePanel.FAX_DIR_COL] = new ParamPanel.DataDirectory("Faxes", ".");
                                faxData[i][FaxPatternEditTablePanel.FAX_PREFIX_COL] = "";
                                faxData[i][FaxPatternEditTablePanel.FAX_PATTERN_COL] = "";
                                faxData[i][FaxPatternEditTablePanel.FAX_EXT_COL] = "";
                            }
                            double scale = 0d;
                            double rotation = 0f;
                            int x_offset = 0;
                            int y_offset = 0;

                            try {
                                scale = Double.parseDouble(faxrow.selectNodes("./faxScale").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                            }
                            try {
                                rotation = Float.parseFloat(faxrow.selectNodes("./faxRotation").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                            }
                            try {
                                x_offset = Integer.parseInt(faxrow.selectNodes("./faxXoffset").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                            }
                            try {
                                y_offset = Integer.parseInt(faxrow.selectNodes("./faxYoffset").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                            }

                            faxData[i][FaxPatternEditTablePanel.SCALE_COL] = scale;     // Scale
                            faxData[i][FaxPatternEditTablePanel.ROTATION_COL] = rotation; // Rotation
                            faxData[i][FaxPatternEditTablePanel.X_OFFSET_COL] = x_offset; // X_Offset
                            faxData[i][FaxPatternEditTablePanel.Y_OFFSET_COL] = y_offset; // Y_Offset
                        }
                        Object[][] gribData = new Object[1][23];
                        NodeList gribList = doc.selectNodes("//grib/dynamic-grib");
                        boolean withGrib = false;
                        if (gribList.getLength() == 0) {
                            gribData[0][0] = "";
                            gribData[0][1] = "";
                            gribData[0][2] = new ParamPanel.DataDirectory("GRIBs", "");
                            gribData[0][3] = "";
                            gribData[0][4] = "";
                            gribData[0][5] = "";
                            gribData[0][6] = Boolean.FALSE;
                            gribData[0][7] = Boolean.FALSE;
                            gribData[0][8] = Boolean.FALSE;
                            gribData[0][9] = Boolean.FALSE;
                            gribData[0][10] = Boolean.FALSE;
                            gribData[0][11] = Boolean.FALSE;
                            gribData[0][12] = Boolean.FALSE;
                            gribData[0][13] = Boolean.FALSE;
                            gribData[0][14] = Boolean.FALSE;
                            gribData[0][15] = Boolean.FALSE;
                            gribData[0][16] = Boolean.FALSE;
                            gribData[0][17] = Boolean.FALSE;
                            gribData[0][18] = Boolean.FALSE;
                            gribData[0][19] = Boolean.FALSE;
                            gribData[0][20] = Boolean.FALSE;
                            gribData[0][21] = Boolean.FALSE;
                            gribData[0][22] = Boolean.FALSE;
                        } else {
                            withGrib = true;
                            XMLElement gribNode = (XMLElement) gribList.item(0);
                            gribData[0][0] = gribNode.getAttribute("hint");
                            gribData[0][1] = gribNode.getAttribute("request");
                            gribData[0][2] = new ParamPanel.DataDirectory("GRIBs", gribNode.getAttribute("dir"));
                            gribData[0][3] = gribNode.getAttribute("prefix");
                            gribData[0][4] = gribNode.getAttribute("pattern");
                            gribData[0][5] = gribNode.getAttribute("extension");
                            gribData[0][6] = Boolean.FALSE;
                            gribData[0][7] = Boolean.FALSE;
                            gribData[0][8] = Boolean.FALSE;
                            gribData[0][9] = Boolean.FALSE;
                            gribData[0][10] = Boolean.FALSE;
                            gribData[0][11] = Boolean.FALSE;
                            gribData[0][12] = Boolean.FALSE;
                            gribData[0][13] = Boolean.FALSE;
                            gribData[0][14] = Boolean.FALSE;
                            gribData[0][15] = Boolean.FALSE;
                            gribData[0][16] = Boolean.FALSE;
                            gribData[0][17] = Boolean.FALSE;
                            gribData[0][18] = Boolean.FALSE;
                            gribData[0][19] = Boolean.FALSE;
                            gribData[0][20] = Boolean.FALSE;
                            gribData[0][21] = Boolean.FALSE;
                            gribData[0][22] = Boolean.FALSE;
                            boolean newVersion = false;
                            try {
                                // TASK display-TWS-Data ?
                                gribData[0][6] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRMSL-Data").equals("true");
                                gribData[0][7] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-500HGT-Data").equals("true");
                                gribData[0][8] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-WAVES-Data").equals("true");
                                gribData[0][9] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-TEMP-Data").equals("true");
                                gribData[0][10] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRATE-Data").equals("true");
                                gribData[0][11] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-TWS-3D").equals("true");
                                gribData[0][12] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRMSL-3D").equals("true");
                                gribData[0][13] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-500HGT-3D").equals("true");
                                gribData[0][14] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-WAVES-3D").equals("true");
                                gribData[0][15] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-TEMP-3D").equals("true");
                                gribData[0][16] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRATE-3D").equals("true");
                                gribData[0][17] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-TWS-contour").equals("true");
                                gribData[0][18] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRMSL-contour").equals("true");
                                gribData[0][19] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-500HGT-contour").equals("true");
                                gribData[0][20] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-WAVES-contour").equals("true");
                                gribData[0][21] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-TEMP-contour").equals("true");
                                gribData[0][22] = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("display-PRATE-contour").equals("true");
                                newVersion = true;
                            } catch (Exception ex) {
                                gribData[0][6] = Boolean.FALSE;
                                gribData[0][7] = Boolean.FALSE;
                                gribData[0][8] = Boolean.FALSE;
                                gribData[0][9] = Boolean.FALSE;
                                gribData[0][10] = Boolean.FALSE;
                                gribData[0][11] = Boolean.FALSE;
                                gribData[0][12] = Boolean.FALSE;
                                gribData[0][13] = Boolean.FALSE;
                                gribData[0][14] = Boolean.FALSE;
                                gribData[0][15] = Boolean.FALSE;
                                gribData[0][16] = Boolean.FALSE;
                                gribData[0][17] = Boolean.FALSE;
                                gribData[0][18] = Boolean.FALSE;
                                gribData[0][19] = Boolean.FALSE;
                                gribData[0][20] = Boolean.FALSE;
                                gribData[0][21] = Boolean.FALSE;
                                gribData[0][22] = Boolean.FALSE;
                            }
                            if (!newVersion) {
                                try {
                                    boolean wo = ((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("wind-only").equals("true");
                                    if (!wo) {
                                        gribData[0][6] = Boolean.TRUE; // PRMSL
                                        gribData[0][7] = Boolean.TRUE; // 500
                                        gribData[0][8] = Boolean.FALSE; // WAVES
                                        gribData[0][9] = Boolean.FALSE; // TEMP
                                        gribData[0][10] = Boolean.FALSE; // PRATE
                                        gribData[0][11] = Boolean.TRUE; // 3D TWS
                                        gribData[0][12] = Boolean.TRUE; // 3D PRMSL
                                        gribData[0][13] = Boolean.TRUE; // 3D 500MB
                                        gribData[0][14] = Boolean.FALSE; // 3D WAVES
                                        gribData[0][15] = Boolean.FALSE; // 3D TEMP
                                        gribData[0][16] = Boolean.FALSE; // 3D PRATE
                                        gribData[0][17] = Boolean.FALSE; // Contours ...
                                        gribData[0][18] = Boolean.FALSE;
                                        gribData[0][19] = Boolean.FALSE;
                                        gribData[0][20] = Boolean.FALSE;
                                        gribData[0][21] = Boolean.FALSE;
                                        gribData[0][22] = Boolean.FALSE;
                                    }
                                } catch (Exception ignore) { /* for backward compatibility */ }
                            }
                        }
                        int twoDSmooth = 1;
                        int timeSmooth = 1;
                        try {
                            twoDSmooth = Integer.parseInt(((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("smooth"));
                        } catch (Exception ignore) {
                        }
                        try {
                            timeSmooth = Integer.parseInt(((XMLElement) doc.selectNodes("//grib").item(0)).getAttribute("time-smooth"));
                        } catch (Exception ignore) {
                        }

                        // Chart data
                        int projection = -1;
                        double northBoundary = 0d;
                        double southBoundary = 0d;
                        double eastBoundary = 0d;
                        double westBoundary = 0d;
                        int chartWidth = 0;
                        int chartHeight = 0;
                        int xOffset = 0;
                        int yOffset = 0;
                        boolean showChart = true;
                        try {
                            northBoundary = Double.parseDouble(doc.selectNodes("/pattern/north").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }
                        try {
                            southBoundary = Double.parseDouble(doc.selectNodes("/pattern/south").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }
                        try {
                            eastBoundary = Double.parseDouble(doc.selectNodes("/pattern/east").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }
                        try {
                            westBoundary = Double.parseDouble(doc.selectNodes("/pattern/west").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }

                        try {
                            chartWidth = Integer.parseInt(doc.selectNodes("/pattern/chartwidth").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }
                        try {
                            chartHeight = Integer.parseInt(doc.selectNodes("/pattern/chartheight").item(0).getFirstChild().getNodeValue());
                        } catch (Exception ignore) {
                        }

                        try {
                            showChart = "yes".equals(((XMLElement) doc.selectNodes("/pattern/chart-opt").item(0)).getAttribute("show"));
                        } catch (Exception ignore) {
                        }

                        try {
                            xOffset = Integer.parseInt(((XMLElement) (doc.selectNodes("/pattern/scroll").item(0))).getAttribute("x"));
                        } catch (Exception ignore) {
                        }
                        try {
                            yOffset = Integer.parseInt(((XMLElement) (doc.selectNodes("/pattern/scroll").item(0))).getAttribute("y"));
                        } catch (Exception ignore) {
                        }

                        int faxOption = CommandPanel.CHECKBOX_OPTION;
                        try {
                            String fo = ((XMLElement) doc.selectNodes("//fax-option").item(0)).getAttribute("value");
                            if (fo.equals("RADIOBUTTON")) {
                              faxOption = CommandPanel.RADIOBUTTON_OPTION;
                            }
                        } catch (Exception ex) {
                        }

                        try {
                            String prjStr = ((XMLElement) (doc.selectNodes("/pattern/projection").item(0))).getAttribute("type");
                            switch (prjStr) {
                                case CommandPanel.MERCATOR:
                                    projection = ChartPanelInterface.MERCATOR;
                                    break;
                                case CommandPanel.ANAXIMANDRE:
                                    projection = ChartPanelInterface.ANAXIMANDRE;
                                    break;
                                case CommandPanel.LAMBERT:
                                    projection = ChartPanelInterface.LAMBERT;
                                    break;
                                case CommandPanel.GLOBE:
                                    projection = ChartPanelInterface.GLOBE_VIEW;
                                    break;
                                case CommandPanel.SATELLITE:
                                    projection = ChartPanelInterface.SATELLITE_VIEW;
                                    break;
                                case CommandPanel.CONIC_EQU:
                                    projection = ChartPanelInterface.CONIC_EQUIDISTANT;
                                    break;
                                case CommandPanel.STEREO:
                                    projection = ChartPanelInterface.STEREOGRAPHIC;
                                    break;
                                case CommandPanel.POLAR_STEREO:
                                    projection = ChartPanelInterface.POLAR_STEREOGRAPHIC;
                                    break;
                            }
                        } catch (Exception ignore) {
                        }
                        // TODO In FaxData, add Scale, Offset (X & Y), Rotation
                        PatternEditorPanel pep = new PatternEditorPanel(author,
                                projection,
                                northBoundary,
                                southBoundary,
                                eastBoundary,
                                westBoundary,
                                showChart,
                                chartWidth,
                                chartHeight,
                                xOffset,
                                yOffset,
                                faxData,
                                gribData,
                                twoDSmooth,
                                timeSmooth);
                        pep.setGrib(withGrib);
                        pep.setFaxOption(faxOption);
                        // UI
                        int resp = JOptionPane.showConfirmDialog(this, pep, WWGnlUtilities.buildMessage("edit-pattern"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            if (doc.selectNodes("//author").getLength() > 0) {
                                ((XMLElement) doc.selectNodes("//author").item(0)).setAttribute("name", pep.getAuthor());
                            } else {
                                XMLElement root = (XMLElement) doc.selectNodes("/pattern").item(0);
                                XMLElement authorElem = (XMLElement) doc.createElement("author");
                                root.appendChild(authorElem);
                                authorElem.setAttribute("name", pep.getAuthor());
                            }
                            // Get Chart Parameters
                            doc.selectNodes("/pattern/north").item(0).getFirstChild().setNodeValue(Double.toString(pep.getTopLat()));
                            doc.selectNodes("/pattern/south").item(0).getFirstChild().setNodeValue(Double.toString(pep.getBottomLat()));
                            doc.selectNodes("/pattern/east").item(0).getFirstChild().setNodeValue(Double.toString(pep.getRightLong()));
                            doc.selectNodes("/pattern/west").item(0).getFirstChild().setNodeValue(Double.toString(pep.getLeftLong()));

                            doc.selectNodes("/pattern/chartwidth").item(0).getFirstChild().setNodeValue(Integer.toString(pep.getChartWidth()));
                            doc.selectNodes("/pattern/chartheight").item(0).getFirstChild().setNodeValue(Integer.toString(pep.getChartHeight()));

                            NodeList nl = doc.selectNodes("/pattern/scroll");
                            if (nl.getLength() == 0) {
                                XMLElement scroll = (XMLElement) doc.createElement("scroll");
                                doc.selectNodes("/pattern").item(0).appendChild(scroll);
                            }
                            ((XMLElement) (doc.selectNodes("/pattern/scroll").item(0))).setAttribute("x", Integer.toString(pep.getXOffset()));
                            ((XMLElement) (doc.selectNodes("/pattern/scroll").item(0))).setAttribute("y", Integer.toString(pep.getYOffset()));

                            nl = doc.selectNodes("/pattern/chart-opt");
                            if (nl.getLength() == 0) {
                                XMLElement scroll = (XMLElement) doc.createElement("chart-opt");
                                doc.selectNodes("/pattern").item(0).appendChild(scroll);
                            }
                            ((XMLElement) (doc.selectNodes("/pattern/chart-opt").item(0))).setAttribute("show", pep.getShowChart() ? "yes" : "no");

                            projection = pep.getProjection();
                            String prjStr = "";
                            switch (projection) {
                                case ChartPanelInterface.ANAXIMANDRE:
                                    prjStr = CommandPanel.ANAXIMANDRE;
                                    break;
                                case ChartPanelInterface.MERCATOR:
                                    prjStr = CommandPanel.MERCATOR;
                                    break;
                                case ChartPanelInterface.LAMBERT:
                                    prjStr = CommandPanel.LAMBERT;
                                    break;
                                case ChartPanelInterface.CONIC_EQUIDISTANT:
                                    prjStr = CommandPanel.CONIC_EQU;
                                    break;
                                case ChartPanelInterface.GLOBE_VIEW:
                                    prjStr = CommandPanel.GLOBE;
                                    break;
                                case ChartPanelInterface.SATELLITE_VIEW:
                                    prjStr = CommandPanel.SATELLITE;
                                    break;
                                case ChartPanelInterface.STEREOGRAPHIC:
                                    prjStr = CommandPanel.STEREO;
                                    break;
                                case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                                    prjStr = CommandPanel.POLAR_STEREO;
                                    break;
                                default:
                                    break;
                            }
                            ((XMLElement) (doc.selectNodes("/pattern/projection").item(0))).setAttribute("type", prjStr);

                            faxData = pep.getFaxData();
                            // Replace attributes
                            for (int i = 0; i < faxData.length; i++) {
                                XMLElement faxrow = (XMLElement) faxList.item(i);
                                FaxType ft = (FaxType) faxData[i][FaxPatternEditTablePanel.HINT_COL];
                                faxrow.setAttribute("hint", ft.toString());
                                faxrow.setAttribute("color", WWGnlUtilities.colorToString(ft.getColor()));
                                faxrow.setAttribute("transparent", ((Boolean) faxData[i][FaxPatternEditTablePanel.TRANSPARENT_COL]).toString());
                                // faxrow.setAttribute("color-change", Boolean.toString(ft.isChangeColor()));
                                faxrow.setAttribute("color-change", ((Boolean) faxData[i][FaxPatternEditTablePanel.CHANGE_COLOR_COL]).toString());
                                XMLElement dyn = null;
                                if (faxrow.selectNodes("./dynamic-resource").getLength() != 0) {
                                    dyn = (XMLElement) faxrow.selectNodes("./dynamic-resource").item(0);
                                    if (!(Boolean) faxData[i][FaxPatternEditTablePanel.DYNAMIC_COL]) { // remove dyn
                                        faxrow.removeChild(dyn);
                                        dyn = null;
                                    }
                                } else {
                                    if ((Boolean) faxData[i][FaxPatternEditTablePanel.DYNAMIC_COL]) { // create
                                        dyn = (XMLElement) doc.createElement("dynamic-resource");
                                        faxrow.appendChild(dyn);
                                    }
                                }
                                // TODO Scale, Rotation, X&Y Offsets
                                try {
                                    faxrow.selectNodes("./faxScale").item(0).getFirstChild().setNodeValue(Double.toString((Double) faxData[i][FaxPatternEditTablePanel.SCALE_COL]));
                                } catch (Exception ignore) {
                                }
                                try {
                                    faxrow.selectNodes("./faxRotation").item(0).getFirstChild().setNodeValue(Float.toString((Float) faxData[i][FaxPatternEditTablePanel.ROTATION_COL]));
                                } catch (Exception ignore) {
                                }
                                try {
                                    faxrow.selectNodes("./faxXoffset").item(0).getFirstChild().setNodeValue(Integer.toString((Integer) faxData[i][FaxPatternEditTablePanel.X_OFFSET_COL]));
                                } catch (Exception ignore) {
                                }
                                try {
                                    faxrow.selectNodes("./faxYoffset").item(0).getFirstChild().setNodeValue(Integer.toString((Integer) faxData[i][FaxPatternEditTablePanel.Y_OFFSET_COL]));
                                } catch (Exception ignore) {
                                }
                                if ((Boolean) faxData[i][2]) {
                                    // assume dyn is not null
                                    dyn.setAttribute("url", (String) faxData[i][FaxPatternEditTablePanel.FAX_URL_COL]);
                                    dyn.setAttribute("dir", ((ParamPanel.DataDirectory) faxData[i][FaxPatternEditTablePanel.FAX_DIR_COL]).toString());
                                    dyn.setAttribute("prefix", (String) faxData[i][FaxPatternEditTablePanel.FAX_PREFIX_COL]);
                                    dyn.setAttribute("pattern", (String) faxData[i][FaxPatternEditTablePanel.FAX_PATTERN_COL]);
                                    dyn.setAttribute("extension", (String) faxData[i][FaxPatternEditTablePanel.FAX_EXT_COL]);
                                }
                            }
                            XMLElement gribNode = (XMLElement) doc.selectNodes("//grib").item(0);
                            try { // Remove deprecated attributes
                                gribNode.removeAttribute("wind-only");
                                gribNode.removeAttribute("with-contour");
                            } catch (Exception ignore) {
                            }
                            if (pep.isGrib()) {
                                Object[][] newGribData = pep.getGribData();
                                gribNode.setAttribute("display-TWS-Data", "true");
                                gribNode.setAttribute("display-PRMSL-Data", ((Boolean) newGribData[0][6]).toString());
                                gribNode.setAttribute("display-500HGT-Data", ((Boolean) newGribData[0][7]).toString());
                                gribNode.setAttribute("display-WAVES-Data", ((Boolean) newGribData[0][8]).toString());
                                gribNode.setAttribute("display-TEMP-Data", ((Boolean) newGribData[0][9]).toString());
                                gribNode.setAttribute("display-PRATE-Data", ((Boolean) newGribData[0][10]).toString());
                                gribNode.setAttribute("display-TWS-3D", ((Boolean) newGribData[0][11]).toString());
                                gribNode.setAttribute("display-PRMSL-3D", ((Boolean) newGribData[0][12]).toString());
                                gribNode.setAttribute("display-500HGT-3D", ((Boolean) newGribData[0][13]).toString());
                                gribNode.setAttribute("display-WAVES-3D", ((Boolean) newGribData[0][14]).toString());
                                gribNode.setAttribute("display-TEMP-3D", ((Boolean) newGribData[0][15]).toString());
                                gribNode.setAttribute("display-PRATE-3D", ((Boolean) newGribData[0][16]).toString());
                                gribNode.setAttribute("display-TWS-contour", ((Boolean) newGribData[0][17]).toString());
                                gribNode.setAttribute("display-PRMSL-contour", ((Boolean) newGribData[0][18]).toString());
                                gribNode.setAttribute("display-500HGT-contour", ((Boolean) newGribData[0][19]).toString());
                                gribNode.setAttribute("display-WAVES-contour", ((Boolean) newGribData[0][20]).toString());
                                gribNode.setAttribute("display-TEMP-contour", ((Boolean) newGribData[0][21]).toString());
                                gribNode.setAttribute("display-PRATE-contour", ((Boolean) newGribData[0][22]).toString());

                                gribNode.setAttribute("smooth", Integer.toString(pep.get2DSmooth()));
                                gribNode.setAttribute("time-smooth", Integer.toString(pep.getTimeSmooth()));

                                XMLElement grib;
                                if (gribNode.selectNodes("dynamic-grib").getLength() > 0) {
                                    grib = (XMLElement) gribNode.selectNodes("dynamic-grib").item(0);
                                    grib.setAttribute("hint", (String) newGribData[0][0]);
                                    grib.setAttribute("request", (String) newGribData[0][1]);
                                    grib.setAttribute("dir", ((ParamPanel.DataDirectory) newGribData[0][2]).toString());
                                    grib.setAttribute("prefix", (String) newGribData[0][3]);
                                    grib.setAttribute("pattern", (String) newGribData[0][4]);
                                    grib.setAttribute("extension", (String) newGribData[0][5]);
                                } else {
                                    grib = (XMLElement) doc.createElement("dynamic-grib");
                                    grib.setAttribute("hint", (String) newGribData[0][0]);
                                    grib.setAttribute("request", (String) newGribData[0][1]);
                                    grib.setAttribute("dir", ((ParamPanel.DataDirectory) newGribData[0][2]).toString());
                                    grib.setAttribute("prefix", (String) newGribData[0][3]);
                                    grib.setAttribute("pattern", (String) newGribData[0][4]);
                                    grib.setAttribute("extension", (String) newGribData[0][5]);
                                    gribNode.appendChild(grib);
                                }
                            } else { // remove if necessary
                                if (gribNode.selectNodes("dynamic-grib").getLength() > 0) {
                                    XMLElement grib = (XMLElement) gribNode.selectNodes("dynamic-grib").item(0);
                                    gribNode.removeChild(grib);
                                }
                            }
                            nl = doc.selectNodes("/pattern/fax-option");
                            if (nl.getLength() == 0) {
                                XMLElement faxOp = (XMLElement) doc.createElement("fax-option");
                                doc.selectNodes("/pattern").item(0).appendChild(faxOp);
                            }
                            ((XMLElement) (doc.selectNodes("/pattern/fax-option").item(0))).setAttribute("value", pep.getFaxOption() == CommandPanel.RADIOBUTTON_OPTION ? "RADIOBUTTON" : "CHECKBOX");
                            doc.print(System.out);
                            doc.print(new FileOutputStream(fullpath));
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Parsing " + fullpath + ":");
                    WWContext.getInstance().fireExceptionLogging(ex);
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(RENAME)) {
            if (dtn instanceof JTreeFilePanel.PatternFileTreeNode) {
                JTreeFilePanel.PatternFileTreeNode pftn = (JTreeFilePanel.PatternFileTreeNode) dtn;
                // Prompt for new name
                JTextField pNameTextField = new JTextField(pftn.name);
                int resp = JOptionPane.showConfirmDialog(this, pNameTextField, WWGnlUtilities.buildMessage("new-pattern-name"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    String newName = pNameTextField.getText();
                    newName = Utilities.makeSureExtensionIsOK(newName, ".ptrn");
                    if (!newName.equals(pftn.name)) {
                        File file = new File(pftn.dir + File.separator + pftn.name);
                        file.renameTo(new File(pftn.dir + File.separator + newName));
                        refreshTree();
                    }
                }
            } else {
                // TODO LOCALIZE
                JOptionPane.showMessageDialog(this, "Only available for patterns..., sorry (you bet!).", "Rename", JOptionPane.ERROR_MESSAGE);
            }
        } else if (event.getActionCommand().equals(COPY_TO_FAVORITE)) {
            // Copy to the Favorite Directory
            if (dtn instanceof JTreeFilePanel.PatternFileTreeNode) {
                JTreeFilePanel.PatternFileTreeNode pftn = (JTreeFilePanel.PatternFileTreeNode) dtn;
                String dir = pftn.dir;
                String name = pftn.name;
                // Get the Pattern Default directory
                String patternDir = ParamPanel.data[ParamData.PATTERN_DIR][ParamData.VALUE_INDEX].toString() + File.separator + FAVORITE_DIRECTORY_NAME;
                try {
                    // Prompt for new name
                    JTextField pNameTextField = new JTextField(name);
                    int resp = JOptionPane.showConfirmDialog(WWContext.getInstance().getMasterTopFrame(), pNameTextField, WWGnlUtilities.buildMessage("new-pattern-name"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resp == JOptionPane.OK_OPTION) {
                        String newName = pNameTextField.getText();
                        newName = Utilities.makeSureExtensionIsOK(newName, ".ptrn");
                        File newPattern = new File(patternDir + File.separator + newName);
                        boolean ok2go = true;
                        if (newPattern.exists()) {
                            ok2go = false;
                            int resp2 = JOptionPane.showConfirmDialog(
                                    WWContext.getInstance().getMasterTopFrame(),
                                    WWGnlUtilities.buildMessage("pattern-already-exist", new String[]{newName}),
                                    WWGnlUtilities.buildMessage("store-pattern"),
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            if (resp2 == JOptionPane.OK_OPTION) {
                              ok2go = true;
                            }
                        }
                        if (ok2go) {
                            FileInputStream in = new FileInputStream(dir + File.separator + name);
                            // Make sure Favorite dir exists
                            File favorite = new File(patternDir);
                            if (!favorite.exists()) {
                              favorite.mkdirs();
                            }
                            FileOutputStream out = new FileOutputStream(newPattern);
                            Utilities.copy(in, out);
                            in.close();
                            out.close();
                            // Refresh
                            refreshTree();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(COPY_LOCALLY)) {
            // Choose the file name for "save as"
            String patternDirectory = ParamPanel.data[ParamData.PATTERN_DIR][ParamData.VALUE_INDEX].toString();
            String fileName = WWGnlUtilities.chooseFile(this, JFileChooser.FILES_ONLY,
                    new String[]{"ptrn"},
                    "Patterns",
                    patternDirectory,
                    WWGnlUtilities.buildMessage("save-as-2"),
                    "Save pattern as");
            if (fileName != null && fileName.trim().length() > 0) {
                fileName = Utilities.makeSureExtensionIsOK(fileName, ".ptrn");
                JTreeFilePanel.PatternFileTreeNode pftn = (JTreeFilePanel.PatternFileTreeNode) dtn;
                try {
                    URL patternURL = new URL(pftn.dir + pftn.name);
                    byte[] content = HTTPClient.readURL(patternURL);
                    FileWriter fw = new FileWriter(fileName);
                    fw.write(new String(content));
                    fw.close();
                    parent.reloadTree();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(SHOW_FILE_SYSTEM)) {
            String dir = "";
            if (dtn instanceof JTreeFilePanel.DataFileTreeNode) {
                JTreeFilePanel.DataFileTreeNode dftn = (JTreeFilePanel.DataFileTreeNode) dtn;
                dir = dftn.dir;
            }
            if (dir.trim().length() > 0) {
                try {
                    Utilities.showFileSystem(dir);
                } catch (Exception e) {
                    String errMess = "File directory is [" + dir + "]\n" + e.toString();
                    JOptionPane.showMessageDialog(this, errMess, "Show directory", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(SORT_BY_DATE_DESC) && ((JMenu) ((JPopupMenu) ((JMenuItem) event.getSource()).getParent()).getInvoker()).getActionCommand().equals(SORT_BY_DATE)) {
            this.parent.setSort(JTreeFilePanel.SORT_BY_DATE_DESC);
            if (this.parent.getType() == JTreeFilePanel.COMPOSITE_TYPE) {
              System.setProperty("composite.sort", "date.desc");
            }
            refreshTree();
        } else if (event.getActionCommand().equals(SORT_BY_DATE_ASC) && ((JMenu) ((JPopupMenu) ((JMenuItem) event.getSource()).getParent()).getInvoker()).getActionCommand().equals(SORT_BY_DATE)) {
            this.parent.setSort(JTreeFilePanel.SORT_BY_DATE_ASC);
            if (this.parent.getType() == JTreeFilePanel.COMPOSITE_TYPE) {
              System.setProperty("composite.sort", "date.asc");
            }
            refreshTree();
        } else if (event.getActionCommand().equals(SORT_BY_NAME_ASC) && ((JMenu) ((JPopupMenu) ((JMenuItem) event.getSource()).getParent()).getInvoker()).getActionCommand().equals(SORT_BY_NAME)) {
            this.parent.setSort(JTreeFilePanel.SORT_BY_NAME_ASC);
            if (this.parent.getType() == JTreeFilePanel.COMPOSITE_TYPE) {
              System.setProperty("composite.sort", "name.asc");
            }
            refreshTree();
        } else if (event.getActionCommand().equals(SORT_BY_NAME_DESC) && ((JMenu) ((JPopupMenu) ((JMenuItem) event.getSource()).getParent()).getInvoker()).getActionCommand().equals(SORT_BY_NAME)) {
            this.parent.setSort(JTreeFilePanel.SORT_BY_NAME_DESC);
            if (this.parent.getType() == JTreeFilePanel.COMPOSITE_TYPE) {
              System.setProperty("composite.sort", "name.desc");
            }
            refreshTree();
        } else if (event.getActionCommand().equals(ARCHIVE_COMPOSITE)) {
            if (dtnArray != null) {
                // 1 - Ask: Copy or Move
                int resp = JOptionPane.showConfirmDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("do-you-wish-to-delete"), WWGnlUtilities.buildMessage("archive-composite"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                final boolean deleteWhenDone = resp == JOptionPane.YES_OPTION;
                if (resp != JOptionPane.CANCEL_OPTION) {
                    List<String> fileList = new ArrayList<>();
                    for (DefaultMutableTreeNode defaultMutableTreeNode : dtnArray) {
                        if (defaultMutableTreeNode instanceof JTreeFilePanel.CompositeFileTreeNode) {
                            JTreeFilePanel.CompositeFileTreeNode dftn = (JTreeFilePanel.CompositeFileTreeNode) defaultMutableTreeNode;
                            fileList.add(dftn.dir + File.separator + dftn.name);
                        } else {
                            System.out.println("One node ignored...");
                        }
                    }
                    // Sort
                    Collections.<String>sort(fileList, Collections.reverseOrder());
                    // Process
                    for (String compositeName : fileList) {
                      WWGnlUtilities.archiveComposite(compositeName, deleteWhenDone);
                    }
                    // refreshTree();
                    WWContext.getInstance().fireReloadCompositeTree();
                    WWContext.getInstance().fireReloadFaxTree();
                    WWContext.getInstance().fireReloadGRIBTree();
                }
            } else if (dtn instanceof JTreeFilePanel.CompositeFileTreeNode) {
                JTreeFilePanel.CompositeFileTreeNode dftn = (JTreeFilePanel.CompositeFileTreeNode) dtn;
                WWGnlUtilities.archiveComposite(dftn.dir + File.separator + dftn.name);
                // refreshTree();
                WWContext.getInstance().fireReloadCompositeTree();
                WWContext.getInstance().fireReloadFaxTree();
                WWContext.getInstance().fireReloadGRIBTree();
            }
        } else if (event.getActionCommand().equals(UNARCHIVE_COMPOSITE)) {
            // String compositeLocation = ParamPanel.data[ParamData.CTX_FILES_LOC][ParamData.VALUE_INDEX].toString();
            JTreeFilePanel.CompositeFileTreeNode dftn = (JTreeFilePanel.CompositeFileTreeNode) dtn;
            String where = WWGnlUtilities.unarchiveComposite(dftn.dir + File.separator + dftn.name);
            WWContext.getInstance().fireReloadCompositeTree();
            JOptionPane.showMessageDialog(parent, WWGnlUtilities.buildMessage("unarchived-in", new String[]{where}), WWGnlUtilities.buildMessage("unarchive-composite"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getActionCommand().equals(ARCHIVE_COMPOSITE_DIR)) {
            if (dtn instanceof JTreeFilePanel.DirectoryTreeNode) {
                final JTreeFilePanel.DirectoryTreeNode dirtn = (JTreeFilePanel.DirectoryTreeNode) dtn;
                Thread t = new Thread(() -> {
                    WWGnlUtilities.archiveCompositeDirectory(dirtn.dir);
                    //      refreshTree();
                    WWContext.getInstance().fireReloadCompositeTree();
                    WWContext.getInstance().fireReloadFaxTree();
                    WWContext.getInstance().fireReloadGRIBTree();
                });
                try {
                    t.start();
                    // t.join();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        this.setVisible(false); // Shut popup when done.
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void setTreeNode(DefaultMutableTreeNode n) {
        this.dtn = n;
        this.dtnArray = null;
    }

    public void setTreeNode(DefaultMutableTreeNode[] n) {
        this.dtnArray = n;
        if (n != null && n.length > 0) {
          this.dtn = n[0];
        }
    }

    public void show(Component c, int x, int y) {
        super.show(c, x, y);
        archiveCompositeDir.setEnabled(dtn instanceof JTreeFilePanel.DirectoryTreeNode &&
                parent.getType() == JTreeFilePanel.COMPOSITE_TYPE);
        boolean unarchiveEnabled = false;
        if (dtn != null && dtn instanceof JTreeFilePanel.CompositeFileTreeNode) {
            JTreeFilePanel.CompositeFileTreeNode cftn = (JTreeFilePanel.CompositeFileTreeNode) dtn;
            unarchiveEnabled = cftn.name.endsWith(WWContext.WAZ_EXTENSION);
        }
        unarchiveComposite.setEnabled(unarchiveEnabled);

        copyLocally.setEnabled(false);
        if (dtn instanceof JTreeFilePanel.PatternFileTreeNode) {
            refresh.setEnabled(false);
            edit.setEnabled(true);
            rename.setEnabled(true);
//    System.out.println("Parent:" + dtn.getParent().toString());
            copyFavorite.setEnabled(!dtn.getParent().toString().equals(FAVORITE_DIRECTORY_NAME) && !((JTreeFilePanel.PatternFileTreeNode) dtn).dir.startsWith("http://"));
            fileSystem.setEnabled(!((JTreeFilePanel.PatternFileTreeNode) dtn).dir.startsWith("http://"));
            gribDetails.setEnabled(false);
            archiveComposite.setEnabled(false);

            if (((JTreeFilePanel.PatternFileTreeNode) dtn).dir.startsWith("http://")) {
              copyLocally.setEnabled(true);
            }
        } else if (dtnArray != null || (dtn instanceof JTreeFilePanel.DataFileTreeNode)) {  // TODO Make sure it addresses all the cases.
            refresh.setEnabled(false);
            edit.setEnabled(false);
            rename.setEnabled(false);
            copyFavorite.setEnabled(false);
            fileSystem.setEnabled(true);
            gribDetails.setEnabled(parent.getType() == JTreeFilePanel.GRIB_TYPE);
            //
            boolean enableCompositeArchive = false;

            if (dtnArray != null) {
                for (DefaultMutableTreeNode defaultMutableTreeNode : dtnArray) {
                    if (defaultMutableTreeNode != null && defaultMutableTreeNode instanceof JTreeFilePanel.CompositeFileTreeNode) {
                        JTreeFilePanel.CompositeFileTreeNode cftn = (JTreeFilePanel.CompositeFileTreeNode) defaultMutableTreeNode;
                        if (cftn.name.endsWith(".xml")) { // And nor .waz
                            enableCompositeArchive = true;
                            break;
                        }
                    }
                }
            } else if (dtn != null && dtn instanceof JTreeFilePanel.CompositeFileTreeNode) {
                JTreeFilePanel.CompositeFileTreeNode cftn = (JTreeFilePanel.CompositeFileTreeNode) dtn;
                enableCompositeArchive = cftn.name.endsWith(".xml"); // And nor .waz
                show.setEnabled(true);
            }
            archiveComposite.setEnabled(enableCompositeArchive);
        } else {
            refresh.setEnabled(true);
            edit.setEnabled(false);
            rename.setEnabled(false);
            copyFavorite.setEnabled(false);
            fileSystem.setEnabled(false);
            gribDetails.setEnabled(false);
            archiveComposite.setEnabled(false);
        }
    }
}
