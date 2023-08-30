package chartview.gui.util.tree;


import calc.GeomUtil;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.gui.left.FileTypeHolder;
import chartview.gui.toolbar.controlpanels.CustomPanelButton;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;
import coreutilities.Utilities;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipFile;


public class JTreeFilePanel
        extends JPanel {
    public final static String OLP_URL = "http://www.lediouris.net/ww/patterns/olp.xml";
    public final static String PDF_URL = "dyn.fax.xml";

    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTree jTree = new JTree();
    //private JButton showHideButton = new JButton();
    private CustomPanelButton showHideButton = null;
    private final transient TreeSelectionListener treeMonitor = new TreeMonitor(this);

    private DefaultMutableTreeNode currentlySelectedNode = null;

    private boolean expanded = true;
    private final String path;

    private final DefaultMutableTreeNode root;
    private final DataFilePopup rootPopup = new DataFilePopup(this);

    public final static int GRIB_TYPE = 0;
    public final static int FAX_TYPE = 1;
    public final static int COMPOSITE_TYPE = 2;
    public final static int PATTERN_TYPE = 3;
    public final static int PRE_DEF_FAX_TYPE = 4;

    private int type = -1;

    public final static int SORT_BY_NAME_ASC = 1;
    public final static int SORT_BY_NAME_DESC = 2;
    public final static int SORT_BY_DATE_DESC = 3;
    public final static int SORT_BY_DATE_ASC = 4;

    private int sort = SORT_BY_NAME_ASC;

    private final static String[] names = {
            WWGnlUtilities.buildMessage("grib-files-button"),
            WWGnlUtilities.buildMessage("faxes-button"),
            WWGnlUtilities.buildMessage("composite-button"),
            WWGnlUtilities.buildMessage("pattern-button"),
            WWGnlUtilities.buildMessage("pre-def-faxes")};
    private final static ImageIcon[] expanded_icons = {
            new ImageIcon(JTreeFilePanel.class.getResource("exp_page.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("exp_script.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("exp_map.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("exp_layout.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("exp_script.png"))};
    private final static ImageIcon[] collapsed_icons = {
            new ImageIcon(JTreeFilePanel.class.getResource("col_page.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("col_script.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("col_map.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("col_layout.png")),
            new ImageIcon(JTreeFilePanel.class.getResource("col_script.png"))};

    private FileTypeHolder parent = null;

    public JTreeFilePanel(String path, int type, FileTypeHolder caller) {
        this.path = path;
        this.type = type;
        this.parent = caller;

        switch (type) {
            case GRIB_TYPE:
            case FAX_TYPE:
            case COMPOSITE_TYPE:
//    case COMPOSITE_ARCHIVE_TYPE:
                String sortType = System.getProperty("composite.sort", "date.desc");
                if ("date.desc".equals(sortType)) {
                    this.sort = SORT_BY_DATE_DESC;
                } else if ("date.asc".equals(sortType)) {
                    this.sort = SORT_BY_DATE_ASC;
                } else if ("name.asc".equals(sortType)) {
                    this.sort = SORT_BY_NAME_ASC;
                } else if ("name.desc".equals(sortType)) {
                    this.sort = SORT_BY_NAME_DESC;
                }
                break;
            case PATTERN_TYPE:
            case PRE_DEF_FAX_TYPE:
            default:
                this.sort = SORT_BY_NAME_ASC;
                break;
        }

        root = new DefaultMutableTreeNode(names[type]);
//  showHideButton.setText(names[type]);
//  showHideButton.setIcon(exanped_icons[type]);
//  showHideButton.setVerticalTextPosition(SwingConstants.CENTER);
//  showHideButton.setHorizontalTextPosition(SwingConstants.RIGHT);
//  showHideButton.setHorizontalAlignment(SwingConstants.LEADING);
//  showHideButton.setBorderPainted(false);
//  showHideButton.setToolTipText(WWGnlUtilities.buildMessage("collapse"));

        showHideButton = new CustomPanelButton(names[type],
                expanded_icons[type],
                WWGnlUtilities.buildMessage("collapse"));

        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        jScrollPane1.getViewport().add(jTree, null);
        jTree.addTreeSelectionListener(treeMonitor);
        jTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            // Lazy Loading of the tooltips
            public void treeWillExpand(TreeExpansionEvent event) {
                Object eventSource = event.getPath().getLastPathComponent();
                if (eventSource instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) eventSource;
                    Enumeration children = parent.children();
                    while (children.hasMoreElements()) {
                        final DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) children.nextElement();
                        if (dtn instanceof DataFileTreeNode && !(dtn instanceof DirectoryTreeNode)) {
                            Thread lazyLoad = new Thread("bubble-generator") {
                                public void run() {
                                    DataFileTreeNode dftn = (DataFileTreeNode) dtn;
                                    String localFileName = dftn.getFullFileName();
                                    String shortFileName = localFileName.substring(localFileName.lastIndexOf(File.separator) + 1);
                                    if (dftn.getBubble().trim().length() == 0 ||
                                            dftn.getBubble().trim().equals(shortFileName)) {
                                        if (dftn instanceof CompositeFileTreeNode) {
                                            String hint = getCompositeBubble(shortFileName, localFileName);
                                            dftn.setBubble(hint);
                                        } else if (dftn instanceof FaxFileTreeNode) {
                                            String comment = WWGnlUtilities.getHeader(localFileName);
                                            dftn.setBubble(comment);
                                        } else if (dftn instanceof GribFileTreeNode) {
                                            try {
                                                GribHelper.GribConditionData[] wgd = GribHelper.getGribData(localFileName, false);
                                                String bubble =
                                                        "<html>" + GribHelper.formatGMTDateTime(wgd[0].getDate()) +
                                                                "<br>Contains <b>" + Integer.toString(wgd.length) + "</b> frame(s)<br>" +
                                                                GeomUtil.decToSex(wgd[0].getNLat(), GeomUtil.HTML, GeomUtil.NS) + " - " +
                                                                GeomUtil.decToSex(wgd[0].getWLng(), GeomUtil.HTML, GeomUtil.EW) + " to<br>" +
                                                                GeomUtil.decToSex(wgd[0].getSLat(), GeomUtil.HTML, GeomUtil.NS) + " - " +
                                                                GeomUtil.decToSex(wgd[0].getELng(), GeomUtil.HTML, GeomUtil.EW) +
                                                                "</html>";
                                                dftn.setBubble(bubble);
                                            } catch (Exception npe) // inline-requests...
                                            {
                                                System.out.println("Problem -> " + localFileName);
                                            }
                                        } else if (dftn instanceof PatternFileTreeNode) {
                                            String bubble = getPatternBubble(localFileName);
                                            dftn.setBubble(bubble);
                                        }
                                    }
                                }
                            };
                            lazyLoad.start();
                        } else if (dtn instanceof DirectoryTreeNode) {
//                System.out.println("Directory:" + dtn.toString());
                            int nb = dtn.getChildCount();
                            ((DirectoryTreeNode) dtn).setBubble(dtn.toString() + ":" + Integer.toString(nb) + " document" + (nb > 1 ? "s" : "") + ".");
                        }
                    } // End if lazyload
                }
            }

            public void treeWillCollapse(TreeExpansionEvent event) {
            }
        });
        jTree.addMouseListener(new MouseListener() {
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
                    if (jTree.getPathForLocation(e.getX(), e.getY()) != null) {
                        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                        if (dtn instanceof PredefinedFaxTreeNode) {
                            String faxUrl = ((DataFileTreeNode) dtn).getBubble();
                            fireFileOpen(faxUrl);
                        } else if (dtn instanceof DataFileTreeNode) {
                            String fName = ((DataFileTreeNode) dtn).getFullFileName();
                            //              System.out.println("Selected :" + fName);
                            fireFileOpen(fName);
                        }
                    }
                } else if (e.getClickCount() > 2) {
                    // Fix triple-click wanna-be drag events...
                    e.consume();
                }
            }

            private void tryPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath current = jTree.getPathForLocation(e.getX(), e.getY());
                    if (current == null) {
                        return;
                    }
                    TreePath[] paths = jTree.getSelectionPaths();
                    boolean isSelected = false;
                    if (paths != null) {
                        for (TreePath treePath : paths) {
                            if (treePath == current) {
                                isSelected = true;
                                break;
                            }
                        }
                    }
                    if (!isSelected) {
                        jTree.setSelectionPath(current);
                    }
                    int nbSelected = jTree.getSelectionCount();
                    if (nbSelected > 1) {
                        TreePath[] treePath = jTree.getSelectionPaths();
                        DefaultMutableTreeNode[] dmtnArray = new DefaultMutableTreeNode[treePath.length];
                        for (int i = 0; i < treePath.length; i++) {
                            dmtnArray[i] = (DefaultMutableTreeNode) treePath[i].getLastPathComponent();
                        }
                        rootPopup.setTreeNode(dmtnArray);
                    } else {
                        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                        rootPopup.setTreeNode(dtn);
                    }
                    rootPopup.show(jTree, e.getX(), e.getY());
                }
            }
        });
        TreeModel treeModel = new DefaultTreeModel(root);
        jTree.setModel(treeModel);
        jTree.setCellRenderer(new GribTreeCellRenderer());

//    showHideButton.addActionListener(new ActionListener()
//        {
//          public void actionPerformed(ActionEvent e)
//          {
//            showHideButton_actionPerformed(e);
//          }
//        });
        showHideButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (showHideButton.isEnabled()) {
                    showHideButton_actionPerformed(null);
                }
            }
        });


        ToolTipManager.sharedInstance().registerComponent(jTree);
        fillUpTree();
        removeEmptyBranches(root);

        this.add(showHideButton, BorderLayout.NORTH);
        this.add(jScrollPane1, BorderLayout.CENTER);
    }

    private void fillUpTree() {
        fillUpTree(null, false, null, false);
    }

    private void fillUpTree(String filter, boolean regExp, String commentFilter, boolean commentRegExp) {
        pattern = null;
        commentPattern = null;

        if (regExp) {
            try {
                pattern = Pattern.compile(filter);
            } catch (PatternSyntaxException pse) {
                JOptionPane.showMessageDialog(this,
                        pse.toString(), WWGnlUtilities.buildMessage("reg-expr-tt"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (commentRegExp) {
            try {
                commentPattern = Pattern.compile(commentFilter);
            } catch (PatternSyntaxException pse) {
                JOptionPane.showMessageDialog(this,
                        pse.toString(), WWGnlUtilities.buildMessage("reg-expr-tt"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{names[type]}));

        DefaultMutableTreeNode localPatterns = null;
        //  DefaultMutableTreeNode preDefFaxes    = null;
        if (getType() == PATTERN_TYPE) { // Add a node for the on-line patterns
            final DefaultMutableTreeNode onLinePatterns = new DefaultMutableTreeNode(WWGnlUtilities.buildMessage("on-line-patterns"));
            localPatterns = new DefaultMutableTreeNode(WWGnlUtilities.buildMessage("local-patterns"));
            root.add(localPatterns);
            // jTree.setRootVisible(false);

            Thread olpThread = new Thread(() -> {
                try {
                    URL url = new URL(OLP_URL);
                    // We create a new parser, for other thread not to wait for it to be released
                    DOMParser parser = new DOMParser(); // (DOMParser)WWContext.getInstance().getParser();
                    synchronized (parser) {
                        parser.setValidationMode(XMLParser.NONVALIDATING);
                        parser.parse(url);
                        XMLDocument olpDoc = parser.getDocument();
                        drillDownWebPatterns((XMLElement) olpDoc.getDocumentElement(), onLinePatterns);
                        // onLinePatterns.add(new PatternFileTreeNode("http://", "weather.lediouris.net/patterns/01.Favorites/06.01.bis.AllPac.Faxes.Satellite.ptrn", "Web Pattern"));
                        root.add(onLinePatterns);
                        ((DefaultTreeModel) jTree.getModel()).reload(root);
                    }
                } catch (Throwable ex) {
                    System.err.println("No connection for OnLine patterns:" + ex.getLocalizedMessage());
                    // ex.printStackTrace();
                }
                System.out.println("Online Pattern Thread completed.");
            }, "olp thread");
            olpThread.start();
        } else if (getType() == PRE_DEF_FAX_TYPE) {
            // preDefFaxes = new DefaultMutableTreeNode(WWGnlUtilities.buildMessage("pre-def-faxes"));
            // root.add(preDefFaxes);
            jTree.setRootVisible(true);
            Thread predefLoader = new Thread(() -> {
                try {
                    URL url = new File(PDF_URL).toURI().toURL();
                    DOMParser parser = WWContext.getInstance().getParser();
                    synchronized (parser) {
                        parser.setValidationMode(XMLParser.NONVALIDATING);
                        parser.parse(url);
                        XMLDocument pdfDoc = parser.getDocument();
                        //        drillDownPreDefFaxes((XMLElement)pdfDoc.getDocumentElement(), preDefFaxes);
                        drillDownPreDefFaxes((XMLElement) pdfDoc.getDocumentElement(), root);
                        System.out.println("Pre-defined faxes thread completed.");
                    }
                } catch (Exception ex) {
                    root.add(new DefaultMutableTreeNode("No pre-defined fax definition found.")); // LOCALIZE
                    System.out.println("No PreDefined faxes...:" + ex.getLocalizedMessage());
                    ex.printStackTrace();
                }
            }, "Pre-def loader");
            predefLoader.start();
        }

        nbFile = 0;
        if (path != null) {
            // Split if this is a path
            String[] pathElem = path.split(File.pathSeparator);
            DirectoryTreeNode[] insertedNodes = null;
            if (pathElem.length > 1) {
                // Add node for the origin of the documents (directory they come from).
                insertedNodes = new DirectoryTreeNode[pathElem.length];
                for (int i = 0; i < pathElem.length; i++) {
                    String name = pathElem[i].substring(pathElem[i].lastIndexOf("/") + 1);
                    insertedNodes[i] = new DirectoryTreeNode(pathElem[i], name, name);
                    if (localPatterns != null) {
                        localPatterns.add(insertedNodes[i]);
                    } else {
                        root.add(insertedNodes[i]);
                    }
                }
            }
            for (int i = 0; i < pathElem.length; i++) {
                //    System.out.println("--> Drilling down " + pathElem[i]);
                drillDown(new File(pathElem[i]),
                        (insertedNodes != null ? insertedNodes[i] : (localPatterns != null ? localPatterns : root)),
                        filter,
                        regExp,
                        commentFilter,
                        commentRegExp);
            }
            WWContext.getInstance().fireLogging("\n" + Integer.toString(nbFile) + " " + names[type] + ".");
        }
    }

    public void reloadTree() {
        reloadTree(null, false);
    }

    public void reloadTree(String filter, boolean regExp) {
        reloadTree(filter, regExp, null, false);
    }

    public void reloadTree(String filter, boolean regExp, String commentFilter, boolean commentRegExp) {
        root.removeAllChildren();
        fillUpTree(filter, regExp, commentFilter, commentRegExp);

        removeEmptyBranches(root);

        ((DefaultTreeModel) jTree.getModel()).reload(root);
    }

    private void removeEmptyBranches(DefaultMutableTreeNode fromRoot) {
        Enumeration children = fromRoot.children();
        List<DirectoryTreeNode> toRemove = new ArrayList<>();
        while (children.hasMoreElements()) {
            TreeNode dtn = (TreeNode) children.nextElement();
            if (dtn instanceof DirectoryTreeNode) {
                DirectoryTreeNode dirNode = (DirectoryTreeNode) dtn;

                if (dirNode.getChildCount() > 0) {
                    removeEmptyBranches(dirNode);
                }
                if (dirNode.getChildCount() == 0) {
                    toRemove.add(dirNode);
                }
            }
        }
        // Remove here!
        if (toRemove.size() > 0) {
            for (DirectoryTreeNode tn : toRemove) {
                tn.removeFromParent();
            }
        }
    }

    public JTree getJTree() {
        return jTree;
    }

    private void drillDownWebPatterns(XMLElement dir, DefaultMutableTreeNode parent) throws Exception {
        NodeList dirNodes = dir.selectNodes("./dir");
        for (int i = 0; i < dirNodes.getLength(); i++) {
            XMLElement elmt = (XMLElement) dirNodes.item(i);
            String name = elmt.getAttribute("name");
            DirectoryTreeNode node = new DirectoryTreeNode(name, name, name);
            parent.add(node);
            drillDownWebPatterns(elmt, node);
        }

        NodeList patternNodes = dir.selectNodes("./pattern");
        for (int i = 0; i < patternNodes.getLength(); i++) {
            XMLElement elmt = (XMLElement) patternNodes.item(i);
            PatternFileTreeNode pftn = new PatternFileTreeNode("http://", elmt.getAttribute("fullpath"), elmt.getAttribute("bubble"));
            parent.add(pftn);
        }
    }

    private void drillDownPreDefFaxes(XMLElement dir, DefaultMutableTreeNode parent) throws Exception {
        NodeList dirNodes = dir.selectNodes("./folder");
        for (int i = 0; i < dirNodes.getLength(); i++) {
            XMLElement elmt = (XMLElement) dirNodes.item(i);
            String name = elmt.getAttribute("label");
            DirectoryTreeNode node = new DirectoryTreeNode(name, name, name);
            parent.add(node);
            drillDownPreDefFaxes(elmt, node);
        }

        NodeList predefNodes = dir.selectNodes("./dyn-fax");
        for (int i = 0; i < predefNodes.getLength(); i++) {
            XMLElement elmt = (XMLElement) predefNodes.item(i);
            PredefinedFaxTreeNode pftn = new PredefinedFaxTreeNode("", elmt.getAttribute("title"), elmt.getAttribute("origin"));
            parent.add(pftn);
        }
    }

    private int nbFile = 0;

    private void drillDown(File dir, DefaultMutableTreeNode parent) {
        drillDown(dir, parent, null, false, null, false);
    }

    private Pattern pattern = null;
    private Pattern commentPattern = null;
    private transient Matcher matcher = null;
    private transient Matcher commentMatcher = null;

    private void drillDown(File dir,
                           DefaultMutableTreeNode parent,
                           final String filter,
                           final boolean regExp,
                           final String commentFilter,
                           final boolean commentRegExp) {

        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("[" + dir.getAbsolutePath() + "] not found, or is not a directory (from " + System.getProperty("user.dir") + ")");
        } else {
            File[] flist = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean cond = false;
                    switch (type) {
                        case JTreeFilePanel.GRIB_TYPE:
                            cond = new File(dir, name).isDirectory() ||
                                    name.endsWith(".grb") ||
                                    name.endsWith(".grib");
                            if (cond && filter != null && filter.trim().length() > 0) {
                                if (!new File(dir, name).isDirectory()) {
                                    if (!regExp) {
                                        cond = cond && (name.indexOf(filter) > -1);
                                    } else {
                                        matcher = pattern.matcher(name);
                                        cond = cond && matcher.find();
                                    }
                                }
                            }
                            break;
                        case JTreeFilePanel.FAX_TYPE:
                            cond = new File(dir, name).isDirectory() ||
                                    name.endsWith(".tif") ||
                                    name.endsWith(".tiff") ||
                                    name.endsWith(".gif") ||
                                    name.endsWith(".jpg") ||
                                    name.endsWith(".jpeg") ||
                                    name.endsWith(".png");
                            if (cond && filter != null && filter.trim().length() > 0) {
                                if (!new File(dir, name).isDirectory()) {
                                    if (!regExp) {
                                        cond = cond && (name.indexOf(filter) > -1);
                                    } else {
                                        matcher = pattern.matcher(name);
                                        cond = cond && matcher.find();
                                    }
                                }
                            }
                            break;
                        case JTreeFilePanel.COMPOSITE_TYPE:
                            cond = new File(dir, name).isDirectory() || name.endsWith(".xml") || name.endsWith(WWContext.WAZ_EXTENSION);
                            if (cond && ((filter != null && filter.trim().length() > 0) ||
                                    (commentFilter != null && commentFilter.trim().length() > 0))) {
                                File f = new File(dir, name);
                                if (!f.isDirectory()) {
                                    if (filter != null && filter.trim().length() > 0) {
                                        if (!regExp) {
                                            cond = cond && (name.indexOf(filter) > -1);
                                        } else {
                                            matcher = pattern.matcher(name);
                                            cond = cond && matcher.find();
                                        }
                                    }
                                    if (cond && commentFilter != null && commentFilter.trim().length() > 0) {
                                        // Get the comment
                                        String compositeComment = WWGnlUtilities.getCompositeComment(f.getAbsolutePath());
//                      System.out.println("Matching [" + compositeComment + "] against ["+ commentFilter + "]");
                                        if (!commentRegExp) {
                                            cond = cond && (compositeComment.indexOf(commentFilter) > -1);
                                        } else {
                                            commentMatcher = commentPattern.matcher(compositeComment);
                                            cond = cond && commentMatcher.find();
                                        }
                                    }
                                }
                            }
                            break;
//              case JTreeFilePanel.COMPOSITE_ARCHIVE_TYPE:
//                cond = new File(dir, name).isDirectory() || name.endsWith(Context.WAZ_EXTENSION);
//                break;
                        case JTreeFilePanel.PATTERN_TYPE:
                            cond = new File(dir, name).isDirectory() || name.endsWith(".ptrn");
                            if (cond && filter != null && filter.trim().length() > 0) {
                                if (!new File(dir, name).isDirectory()) {
                                    if (!regExp) {
                                        cond = cond && (name.indexOf(filter) > -1);
                                    } else {
                                        matcher = pattern.matcher(name);
                                        cond = cond && matcher.find();
                                    }
                                }
                            }
                            break;
                        case JTreeFilePanel.PRE_DEF_FAX_TYPE:
                            cond = true;
                            break;
                        default: // Not supposed to happen
                            cond = true;
                            break;
                    }
                    return cond;
                }
            });
            List<File> list = Arrays.asList(flist);
            // Sort the Files, most recent on top, for all others than pattern
        //  if (type != JTreeFilePanel.PATTERN_TYPE)
            Collections.<File>sort(list, new FileSorter(type));

            try {
                flist = (File[]) list.toArray(new File[list.size()]);
            } catch (ClassCastException cce) {
                // ignore?
                System.err.println("\t>> list.toArray...");
                cce.printStackTrace();
            }

            for (File f : flist) {
                if (f.isDirectory()) {
//        DefaultMutableTreeNode dtn = new DefaultMutableTreeNode(flist[i].getName());
                    DirectoryTreeNode dtn = new DirectoryTreeNode(f.getAbsolutePath(), f.getName(), f.getName());
                    parent.add(dtn);
                    drillDown(f, dtn, filter, regExp, commentFilter, commentRegExp);
                } else {
                    nbFile++;
                    System.out.print(".");
//        System.out.print(Integer.toString(nbFile) + "-");
                    DefaultMutableTreeNode dtn = null;
                    String localFileName = f.getName(); //flist[i].getAbsolutePath().substring(flist[i].getAbsolutePath().lastIndexOf(File.separator) + 1);
                    try {
                        switch (type) {
                            case JTreeFilePanel.FAX_TYPE:
                                String comment = localFileName;
                                dtn = new FaxFileTreeNode(dir.getAbsolutePath(), localFileName, comment);
                                parent.add(dtn);
                                WWContext.getInstance().fireReadFax();
                                break;
                            case JTreeFilePanel.GRIB_TYPE:
                                try {
                                    dtn = new GribFileTreeNode(dir.getAbsolutePath(),
                                            localFileName,
                                            "");
                                    parent.add(dtn);
                                    WWContext.getInstance().fireReadGRIB();
                                } catch (Exception npe) // inline-requests...
                                {
                                    System.out.println("Problem -> " + f.getAbsolutePath());
                                }
                                break;
                            case JTreeFilePanel.COMPOSITE_TYPE:
                                String hint = "";
                                dtn = new CompositeFileTreeNode(dir.getAbsolutePath(), localFileName, hint);
                                parent.add(dtn);
                                WWContext.getInstance().fireReadComposite();
                                break;
                            case JTreeFilePanel.PATTERN_TYPE:
                                String bubble = "";
                                dtn = new PatternFileTreeNode(dir.getAbsolutePath(), localFileName, bubble);
                                parent.add(dtn);
                                WWContext.getInstance().fireReadPattern();
                                break;
                        }
                    } catch (Exception oops) {
                        System.err.println(oops.getMessage());
                        oops.printStackTrace();
                    }
                }
            }
        }
    }

    protected int getType() {
        return type;
    }

    private static String parseFileName(String str) {
        String date = "";
        // YYMMDDHHmm
        // 0123456789
        try {
            date = "20" + str.substring(0, 2) + "-" + WWGnlUtilities.MONTH[Integer.parseInt(str.substring(2, 4)) - 1] + "-" +
                    str.substring(4, 6) +
                    " UT" + str.substring(6, 8) + ":" + str.substring(8, 10);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return date;
    }

    public int getMinHeight() {
        return showHideButton.getSize().height + 2; // 2 = 1 above, 1 below
    }

    private void showHideButton_actionPerformed(ActionEvent e) {
//  boolean show = jScrollPane1.isVisible();
        expanded = !expanded;

        showHideButton.setIcon(expanded ? expanded_icons[type] : collapsed_icons[type]);
        showHideButton.setToolTipText(expanded ? WWGnlUtilities.buildMessage("collapse") : WWGnlUtilities.buildMessage("expand"));
        parent.notifyComponentSizeChange();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        showHideButton.setIcon(expanded ? expanded_icons[type] : collapsed_icons[type]);
        showHideButton.setToolTipText(expanded ? WWGnlUtilities.buildMessage("collapse") : WWGnlUtilities.buildMessage("expand"));
    }

    public boolean isExpanded() {
        return expanded;
    }

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getSort() {
        return sort;
    }

    class FileSorter implements Comparator<File> {
        private int fileType = -1;

        public FileSorter(int type) {
            fileType = type;
        }

        public int compare(File f1, File f2) {
//      File file1 = (File)f1;
//      File file2 = (File)f2;
//    System.out.println("Comparing " + f1.getName() + " and " + f2.getName() + " (" + (fileType == JTreeFilePanel.PATTERN_TYPE?"pattern":"no-pattern") + ")");
//    if (fileType == JTreeFilePanel.PATTERN_TYPE)
            if (sort == SORT_BY_NAME_ASC) {
                return (f1.getName().compareTo(f2.getName()));
            } else if (sort == SORT_BY_NAME_DESC) {
                return (f2.getName().compareTo(f1.getName()));
            } else if (sort == SORT_BY_DATE_DESC) {
                // Most recent on top
                if (f1.lastModified() > f2.lastModified()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (sort == SORT_BY_DATE_ASC) {
                // Most recent on top
                if (f2.lastModified() > f1.lastModified()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 0; // Should never occur
            }
        }
    }

    public static synchronized String getCompositeBubble(String fName, String fPath) {
        String bubble = fName;
        boolean go = true;
        try {
            String str = "<html>";
            str += (fName + "<br>");
            DOMParser parser = WWContext.getInstance().getParser();
            synchronized (parser) {
                parser.setValidationMode(XMLParser.NONVALIDATING);
                File f = new File(fPath);
                if (!f.exists()) {
                    if (fPath.endsWith(".xml")) {
                        System.out.print("Swapping from [" + fPath + "]...");
                        fPath = fPath.substring(0, fPath.length() - ".xml".length()) + ".waz"; // Swapping pointers?
                        fName = fName.substring(0, fName.length() - ".xml".length()) + ".waz"; // Swapping pointers?
                        System.out.println("... to [" + fPath + "]");
                    }
                }
                if (fPath.endsWith(".xml")) {
                    try {
                        parser.parse(new File(fPath).toURI().toURL());
                    } catch (FileNotFoundException fnfe) {
                        System.err.println(fnfe.getLocalizedMessage());
                        go = false;
                    }
                } else // .waz
                {
                    // System.out.println("Looking for archive [" + fPath + "] name [" + fName + "]");
                    ZipFile waz = new ZipFile(fPath);
                    InputStream is = waz.getInputStream(waz.getEntry("composite.xml"));
                    parser.parse(is);
                }
                XMLDocument doc = null;
                if (go) {
                    doc = parser.getDocument();
                    NodeList comment = doc.selectNodes("//composite-comment");
                    if (comment.getLength() > 0) {
                        String commentStr = Utilities.superTrim(comment.item(0).getFirstChild().getNodeValue());
                        if (commentStr.trim().length() > 200) {
                            commentStr = commentStr.trim().substring(0, 200) + " ...";
                        }
                        str += commentStr.replaceAll("\n", "<br>");
                        str += "<br>";
                    }
                    NodeList faxes = doc.selectNodes("//fax-collection/fax");
                    if (faxes.getLength() > 0) {
                        str += ("<b>Faxes</b><br>");
                        for (int i = 0; i < faxes.getLength(); i++) {
                            XMLElement fax = (XMLElement) faxes.item(i);
                            String faxName = fax.getAttribute("file");
                            XMLElement faxTtl = null;
                            try {
                                faxTtl = (XMLElement) fax.selectNodes("./faxTitle").item(0);
                            } catch (Exception ignore) {
                            }
                            if (faxTtl != null) {
                                str += (faxTtl.getNodeValue() /*.getTextContent()*/ + " - ");
                            }
                            str += (WWGnlUtilities.truncateBigFileName(faxName) + "<br>");
                        }
                    }
                    NodeList grib = doc.selectNodes("//grib[./* | ./text() | @in-line-request]");
                    if (grib.getLength() > 0) {
                        str += ("<b>GRIB</b><br>");
                        for (int i = 0; i < grib.getLength(); i++) {
                            XMLElement grb = (XMLElement) grib.item(i);
                            String gribName = "";
                            String ilr = grb.getAttribute("in-line-request");
                            if (ilr != null && ilr.trim().length() > 0) {
                                str += (ilr + "<br>");
                            } else {
                                try {
                                    gribName = grb.getFirstChild().getNodeValue();
                                    str += (WWGnlUtilities.truncateBigFileName(gribName) + "<br>");
                                } catch (Exception ex) {
                                    //  System.out.println("Composite Bubble:" + ex.toString());
                                }
                            }
                        }
                    }
                }
                str += "</html>";
                bubble = str;
            }
        } catch (Exception ex) {
            System.err.println("Error when processing " + fName + " in " + fPath);
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
        return bubble;
    }

    private synchronized String getPatternBubble(String fName) {
        String bubble = fName;
        try {
            String str = "<html>";
            DOMParser parser = WWContext.getInstance().getParser();
            synchronized (parser) {
                parser.setValidationMode(XMLParser.NONVALIDATING);
                parser.parse(new File(fName).toURI().toURL());
                XMLDocument doc = parser.getDocument();
                try {
                    String author = ((XMLElement) doc.selectNodes("//author").item(0)).getAttribute("name");
                    if (author != null && author.trim().length() > 0) {
                        str += (WWGnlUtilities.buildMessage("author") + " " + author + "<br>");
                    }
                } catch (Exception ex) {
                    // System.out.println("No Author.");
                }
                NodeList fax = doc.selectNodes("//fax-collection/fax");
                for (int i = 0; i < fax.getLength(); i++) {
                    XMLElement f = (XMLElement) fax.item(i);
                    str += (f.getAttribute("hint") + " ");
                    if (f.getChildrenByTagName("dynamic-resource").getLength() > 0) {
                        str += "<font color='red'>dynamic</font><br>";
                    } else {
                        str += "<font color='blue'>non-dynamic</font><br>";
                    }
                }

                NodeList grib = doc.selectNodes("//grib[string-length(text()) > 0 or ./dynamic-grib]");
                if (grib.getLength() > 0) {
                    str += ("1 GRIB file<br>");
                }
                String projection = ((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("type");
                str += ("Projection " + projection);
                // Done
                str += "</html>";
                bubble = str;
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        return bubble;
    }

    protected void fireFileOpen(String fName) {
        if (type == JTreeFilePanel.COMPOSITE_TYPE) {
            if (((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().isBusy()) {
                ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).addCompositeTab();
            }
        }

        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            File f = new File(fName);
            if (type != JTreeFilePanel.PRE_DEF_FAX_TYPE && (fName.startsWith("http://") || (!f.isDirectory() && f.exists()))) {
                switch (type) {
                    case JTreeFilePanel.GRIB_TYPE:
                        l.gribFileOpen(fName);
                        break;
                    case JTreeFilePanel.FAX_TYPE:
                        l.faxFileOpen(fName);
                        break;
                    case JTreeFilePanel.COMPOSITE_TYPE:
                        l.compositeFileOpen(fName);
                        break;
                    //        case JTreeFilePanel.COMPOSITE_ARCHIVE_TYPE:
                    //          l.compositeFileOpen(fName);
                    //          break;
                    case JTreeFilePanel.PATTERN_TYPE:
                        l.patternFileOpen(fName);
                        break;
                }
            } else if (type == JTreeFilePanel.PRE_DEF_FAX_TYPE) {
                l.predfinedFaxOpen(fName);
            }
    //      else // DEBUG
    //        System.out.println("Not doing anything for [" + fName + "]");
        }
    }

    class TreeMonitor implements TreeSelectionListener {
        JTextField feedback = null;
        JTreeFilePanel parent;

        public TreeMonitor(JTreeFilePanel caller) {
            this(caller, null);
        }

        public TreeMonitor(JTreeFilePanel caller, JTextField fld) {
            feedback = fld;
            parent = caller;
        }

        public void valueChanged(TreeSelectionEvent tse) {
            TreePath tp = tse.getNewLeadSelectionPath();
            if (tp == null) {
                currentlySelectedNode = null;
                return;
            }
            DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
            currentlySelectedNode = dtn;
            if (dtn instanceof DataFileTreeNode && parent.type == JTreeFilePanel.FAX_TYPE) { // Fax Preview
                WWContext.getInstance().fireFaxSelectedForPreview(((DataFileTreeNode) dtn).getFullFileName());
            }
        }
    }

    public static class DataFileTreeNode extends DefaultMutableTreeNode {
        String dir;
        String name;
        String bubble;

        public DataFileTreeNode(String dir, String name, String bubble) {
            this.dir = dir;
            this.name = name;
            this.bubble = bubble;
        }

        public String toString() {
            String s = name;
            if (dir.startsWith("http://")) {
                s = name.substring(name.lastIndexOf("/") + 1);
            }
            return s;
        }

        public String getBubble() {
            return bubble;
        }

        public void setBubble(String str) {
            this.bubble = str;
        }

        public String getFullFileName() {
            String s = dir + File.separator + name;
            if (dir.equals("http://")) {
                s = dir + name;
            }
            return s;
        }
    }

    public static class PatternFileTreeNode extends DataFileTreeNode {
        public PatternFileTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    public static class PredefinedFaxTreeNode extends DataFileTreeNode {
        public PredefinedFaxTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    public static class FaxFileTreeNode extends DataFileTreeNode {
        public FaxFileTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    public static class GribFileTreeNode extends DataFileTreeNode {
        public GribFileTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    public static class CompositeFileTreeNode extends DataFileTreeNode {
        public CompositeFileTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    public static class DirectoryTreeNode extends DataFileTreeNode {
        public DirectoryTreeNode(String dir, String name, String bubble) {
            super(dir, name, bubble);
        }
    }

    static class GribTreeCellRenderer extends DefaultTreeCellRenderer {
        public GribTreeCellRenderer() {
            super();
        }

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree,
                    value,
                    sel,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
            if (value instanceof DataFileTreeNode && !(value instanceof DirectoryTreeNode)) {
                if (value instanceof FaxFileTreeNode || value instanceof PredefinedFaxTreeNode) {
                    setIcon(new ImageIcon(this.getClass().getResource("script.png")));
                } else if (value instanceof CompositeFileTreeNode) {
                    if (((CompositeFileTreeNode) value).name.endsWith(WWContext.WAZ_EXTENSION)) {
                        setIcon(new ImageIcon(this.getClass().getResource("mapZ.png")));
                    } else {
                        setIcon(new ImageIcon(this.getClass().getResource("map.png")));
                    }
                } else if (value instanceof PatternFileTreeNode) {
                    setIcon(new ImageIcon(this.getClass().getResource("layout.png")));
                } else if (value instanceof GribFileTreeNode) {
                    setIcon(new ImageIcon(this.getClass().getResource("page.png")));
                } else {
                    setIcon(new ImageIcon(this.getClass().getResource("note.png")));
                }
                setToolTipText(((DataFileTreeNode) value).getBubble());
            } else if (value instanceof DirectoryTreeNode) {
                setToolTipText(((DirectoryTreeNode) value).getBubble());
            }
            return this;
        }
    }
}
