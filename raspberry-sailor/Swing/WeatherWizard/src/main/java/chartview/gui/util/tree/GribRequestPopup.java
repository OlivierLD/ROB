package chartview.gui.util.tree;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class GribRequestPopup
        extends JPopupMenu
        implements ActionListener,
        PopupMenuListener {
    private final JMenuItem delete;
    private final JMenuItem edit;
    private final JMenuItem copy;
    private final JMenuItem moveUp;
    private final JMenuItem moveDown;

    private final JTreeGRIBRequestPanel parent;

    private static final String DELETE = WWGnlUtilities.buildMessage("delete");
    private static final String EDIT = WWGnlUtilities.buildMessage("edit");
    private static final String MOVE_UP = WWGnlUtilities.buildMessage("move-up");
    private static final String MOVE_DOWN = WWGnlUtilities.buildMessage("move-down");
    private static final String COPY = WWGnlUtilities.buildMessage("copy");

    private DefaultMutableTreeNode dtn = null;

    public GribRequestPopup(JTreeGRIBRequestPanel caller) {
        super();
        this.parent = caller;
        this.add(delete = new JMenuItem(DELETE));
        delete.addActionListener(this);
        this.add(edit = new JMenuItem(EDIT));
        edit.addActionListener(this);
        this.add(copy = new JMenuItem(COPY));
        copy.addActionListener(this);
        this.add(new JSeparator());
        this.add(moveUp = new JMenuItem(MOVE_UP));
        moveUp.addActionListener(this);
        this.add(moveDown = new JMenuItem(MOVE_DOWN));
        moveDown.addActionListener(this);
    }

    private void refreshTree() {
        Thread refreshThread = new Thread("tree-reloader") {
            public void run() {
                WWContext.getInstance().fireSetLoading(true, WWGnlUtilities.buildMessage("refreshing"));
                parent.reloadTree();
                //      ((DefaultTreeModel)parent.getJTree().getModel()).reload(parent.getRoot());
                WWContext.getInstance().fireSetLoading(false, WWGnlUtilities.buildMessage("refreshing"));
            }
        };
        refreshThread.start();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(DELETE)) {
            if (dtn instanceof JTreeGRIBRequestPanel.GRIBRequestTreeNode) {
                JTreeGRIBRequestPanel.GRIBRequestTreeNode grtn = (JTreeGRIBRequestPanel.GRIBRequestTreeNode) dtn;
                String request = grtn.request;
                String rname = grtn.name;
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib[./@request = '" + request + "' and ./@name = '" + WWGnlUtilities.escapeXML(rname) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Request not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        int resp = JOptionPane.showConfirmDialog(this, WWGnlUtilities.buildMessage("confirm-delete", new String[]{request}), WWGnlUtilities.buildMessage("grib-request"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            XMLElement node = (XMLElement) nl.item(0);
                            node.getParentNode().removeChild(node);
                            parent.refreshTreeAndDocument(doc);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else // Directory
            {
                System.out.println("Directory delete requested:" + dtn.toString());
                String dirName = dtn.toString();
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib-set[./@id = '" + WWGnlUtilities.escapeXML(dirName) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Directory not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        int resp = JOptionPane.showConfirmDialog(this, WWGnlUtilities.buildMessage("confirm-delete", new String[]{dirName}), WWGnlUtilities.buildMessage("grib-request"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            XMLElement node = (XMLElement) nl.item(0);
                            XMLElement parentNode = (XMLElement) node.getParentNode();
                            parentNode.removeChild(node);
                            parent.refreshTreeAndDocument(doc);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(EDIT)) {
            if (dtn instanceof JTreeGRIBRequestPanel.GRIBRequestTreeNode) {
                JTreeGRIBRequestPanel.GRIBRequestTreeNode grtn = (JTreeGRIBRequestPanel.GRIBRequestTreeNode) dtn;
                String request = grtn.request;
                String rname = grtn.name;
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib[./@request = '" + request + "' and ./@name = '" + WWGnlUtilities.escapeXML(rname) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Request not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        String name = grtn.name;
                        String group = grtn.getParent().toString();

                        XMLElement node = (XMLElement) nl.item(0);
                        EditGRIBRequestPanel egrp = new EditGRIBRequestPanel(doc);
                        egrp.setGribRequest(request);
                        egrp.setName(name);
                        egrp.setGroup(group);
                        int resp = JOptionPane.showConfirmDialog(this, egrp, WWGnlUtilities.buildMessage("grib-request"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            if (!group.equals(egrp.getGroup())) {
                                node.getParentNode().removeChild(node);
                                parent.refreshTreeAndDocument(doc);
                                parent.createGRIBRequest(egrp.getName(), egrp.getRequest(), egrp.getGroup());
                            } else {
                                node.setAttribute("name", egrp.getName());
                                node.setAttribute("request", egrp.getRequest());
                                parent.refreshTreeAndDocument(doc);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(MOVE_UP)) {
            if (dtn instanceof JTreeGRIBRequestPanel.GRIBRequestTreeNode) {
                JTreeGRIBRequestPanel.GRIBRequestTreeNode grtn = (JTreeGRIBRequestPanel.GRIBRequestTreeNode) dtn;
                String request = grtn.request;
                String rname = grtn.name;
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib[./@request = '" + request + "' and ./@name = '" + WWGnlUtilities.escapeXML(rname) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Request not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        XMLElement node = (XMLElement) nl.item(0);
                        // Get parent
                        XMLElement groupNode = (XMLElement) node.getParentNode();
                        // Create an array
                        NodeList siblingList = groupNode.selectNodes("./grib");
                        List<XMLElement> reordered = new ArrayList<>(siblingList.getLength());
                        int idx = -1;
                        for (int i = 0; i < siblingList.getLength(); i++) {
                            XMLElement x = (XMLElement) siblingList.item(i);
                            reordered.add(x);
                            groupNode.removeChild(x);
                            if (x.equals(node)) {
                                idx = i;
                            }
                        }
                        XMLElement swap = reordered.get(idx - 1);
                        reordered.set(idx - 1, node);
                        reordered.set(idx, swap);

                        for (XMLElement x : reordered) {
                            groupNode.appendChild(x);
                        }

                        parent.refreshTreeAndDocument(doc);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(MOVE_DOWN)) {
            if (dtn instanceof JTreeGRIBRequestPanel.GRIBRequestTreeNode) {
                JTreeGRIBRequestPanel.GRIBRequestTreeNode grtn = (JTreeGRIBRequestPanel.GRIBRequestTreeNode) dtn;
                String request = grtn.request;
                String rname = grtn.name;
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib[./@request = '" + request + "' and ./@name = '" + WWGnlUtilities.escapeXML(rname) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Request not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        XMLElement node = (XMLElement) nl.item(0);
                        // Get parent
                        XMLElement groupNode = (XMLElement) node.getParentNode();
                        // Create an array
                        NodeList siblingList = groupNode.selectNodes("./grib");
                        List<XMLElement> reordered = new ArrayList<>(siblingList.getLength());
                        int idx = -1;
                        for (int i = 0; i < siblingList.getLength(); i++) {
                            XMLElement x = (XMLElement) siblingList.item(i);
                            reordered.add(x);
                            groupNode.removeChild(x);
                            if (x.equals(node)) {
                                idx = i;
                            }
                        }
                        XMLElement swap = reordered.get(idx + 1);
                        reordered.set(idx + 1, node);
                        reordered.set(idx, swap);

                        for (XMLElement x : reordered) {
                            groupNode.appendChild(x);
                        }
                        parent.refreshTreeAndDocument(doc);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if (event.getActionCommand().equals(COPY)) {
            if (dtn instanceof JTreeGRIBRequestPanel.GRIBRequestTreeNode) {
                JTreeGRIBRequestPanel.GRIBRequestTreeNode grtn = (JTreeGRIBRequestPanel.GRIBRequestTreeNode) dtn;
                String request = grtn.request;
                String rname = grtn.name;
                try {
                    XMLDocument doc = parent.getXMLDocument();
                    NodeList nl = doc.selectNodes("//grib[./@request = '" + request + "' and ./@name = '" + WWGnlUtilities.escapeXML(rname) + "']");
                    if (nl.getLength() == 0) {
                        JOptionPane.showMessageDialog(this, "Request not found (zarbi)", WWGnlUtilities.buildMessage("grib-request"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        String name = "Copy of " + grtn.name;
                        String group = grtn.getParent().toString();

                        EditGRIBRequestPanel egrp = new EditGRIBRequestPanel(doc);
                        egrp.setGribRequest(request);
                        egrp.setName(name);
                        egrp.setGroup(group);
                        int resp = JOptionPane.showConfirmDialog(this, egrp, WWGnlUtilities.buildMessage("grib-request"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (resp == JOptionPane.OK_OPTION) {
                            parent.createGRIBRequest(egrp.getName(),
                                    egrp.getRequest(),
                                    egrp.getGroup());
                        }
                    }
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
    }

    public void show(Component c, int x, int y) {
        super.show(c, x, y);
        boolean ok = (dtn.getChildCount() == 0); // Show for leaves only

        delete.setEnabled(true); // Can delete directory too
        edit.setEnabled(ok);
        copy.setEnabled(ok);

        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dtn.getParent();
        moveUp.setEnabled(ok && (!dtn.equals(parent.getFirstChild())));
        moveDown.setEnabled(ok && (!dtn.equals(parent.getLastLeaf())));
    }
}
