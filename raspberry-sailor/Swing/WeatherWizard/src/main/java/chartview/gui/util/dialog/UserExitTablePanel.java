package chartview.gui.util.dialog;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;
import chartview.util.UserExitAction;
import chartview.util.UserExitInterface;
import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


public class UserExitTablePanel
        extends JPanel {
    XMLDocument originalData;
    // Table Columns
    final static String LABEL = WWGnlUtilities.buildMessage("label");
    final static String ACTION = WWGnlUtilities.buildMessage("action");
    final static String COMMENT = WWGnlUtilities.buildMessage("comment");

    final static String[] names = {LABEL,
            ACTION,
            COMMENT};
    // Table content
    private Object[][] data = new Object[0][names.length];

    TableModel dataModel;
    JTable table;

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel centerPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JScrollPane centerScrollPane = null; // new JScrollPane();
    JPanel topPanel = new JPanel();

    JButton testButton = new JButton();
    JButton removeButton = new JButton();
    JButton addButton = new JButton();

    public UserExitTablePanel(XMLDocument doc) {
        originalData = doc;
        try {
            jbInit();
            setValues(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set values from the XML File
     */
    private void setValues(XMLDocument doc) {
        try {
            NodeList ueList = doc.selectNodes("//user-exit");
            for (int i = 0; i < ueList.getLength(); i++) {
                XMLElement userexit = (XMLElement) ueList.item(i);
                String label = userexit.selectNodes("./label").item(0).getFirstChild().getNodeValue();
                String action = userexit.selectNodes("./action").item(0).getFirstChild().getNodeValue();
                String comment = userexit.selectNodes("./comment").item(0).getFirstChild().getNodeValue();
                int rnk = Integer.parseInt(userexit.getAttribute("id"));
                boolean sync = "true".equals(userexit.getAttribute("sync"));
                boolean ack = "true".equals(userexit.getAttribute("ack"));
                UserExitAction uea = new UserExitAction();
                uea.setRnk(rnk);
                uea.setAck(ack);
                uea.setSync(sync);
                uea.setAction(action);
                uea.setLabel(label);
                uea.setTip(comment);
                addLineInTable(uea, action, comment.trim());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(580, 170));
        this.setPreferredSize(new Dimension(580, 170));
        centerPanel.setLayout(borderLayout2);
        testButton.setText(WWGnlUtilities.buildMessage("test-ue"));
        testButton.setPreferredSize(new Dimension(73, 20));
        testButton.setMinimumSize(new Dimension(73, 20));
        testButton.setMaximumSize(new Dimension(73, 20));
        testButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        testButton.addActionListener(e -> testButton_actionPerformed());
        testButton.setEnabled(false);

        removeButton.setText(WWGnlUtilities.buildMessage("remove-mark"));
        removeButton.setPreferredSize(new Dimension(73, 20));
        removeButton.setMinimumSize(new Dimension(73, 20));
        removeButton.setMaximumSize(new Dimension(73, 20));
        removeButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        removeButton.addActionListener(e -> removeButton_actionPerformed());
        removeButton.setEnabled(false);

        addButton.setText(WWGnlUtilities.buildMessage("add-mark"));
        addButton.setPreferredSize(new Dimension(73, 20));
        addButton.setMinimumSize(new Dimension(73, 20));
        addButton.setMaximumSize(new Dimension(73, 20));
        addButton.setFont(new Font("Dialog", Font.PLAIN, 10));
        addButton.addActionListener(e -> loadUserExit());

        this.add(centerPanel, BorderLayout.CENTER);
        bottomPanel.add(testButton, null);
        bottomPanel.add(removeButton, null);
        bottomPanel.add(addButton, null);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(topPanel, BorderLayout.NORTH);
        initTable();

        TableColumn comment = table.getColumn(COMMENT);
        comment.setCellEditor(new TextFieldEditor());

        SelectionListener listener = new SelectionListener(table);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    private void initTable() {
        // Init Table
        dataModel = new AbstractTableModel() {
            public int getColumnCount() {
                return names.length;
            }

            public int getRowCount() {
                return data.length;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public String getColumnName(int column) {
                return names[column];
            }

            public Class<?> getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            public boolean isCellEditable(int row, int col) {
                return (col > 0); // All editable, except first one
            }

            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
                fireTableCellUpdated(row, column);
            }
        };
        // Create JTable
        table = new JTable(dataModel);

        centerScrollPane = new JScrollPane(table);
        centerPanel.add(centerScrollPane, BorderLayout.CENTER);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
    }

    private void addLineInTable(UserExitAction uea,
                                String action,
                                String comment) {
        int len = data.length;
        Object[][] newData = new Object[len + 1][names.length];
        for (int i = 0; i < len; i++) {
            System.arraycopy(data[i], 0, newData[i], 0, names.length);
        }
        newData[len][0] = uea;
        newData[len][1] = action;
        newData[len][2] = comment.trim();
        data = newData;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        table.repaint();
    }

    private void removeCurrentLine() {
        int selectedRow = table.getSelectedRow();
//  System.out.println("Row " + selectedRow + " is selected");
        if (selectedRow < 0) { // This should not happen
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(),
                    "Please choose a row to remove",
                    "Removing an entry",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            int l = data.length;
            Object[][] newData = new Object[l - 1][names.length];
            int oldInd, newInd;
            newInd = 0;
            for (oldInd = 0; oldInd < l; oldInd++) {
                if (oldInd != selectedRow) {
                    System.arraycopy(data[oldInd], 0, newData[newInd], 0, names.length);
                    newInd++;
                }
            }
            data = newData;
            //    sorter.tableChanged(new TableModelEvent(dataModel));
            //    sorter.checkModel();
            ((AbstractTableModel) dataModel).fireTableDataChanged();
            table.repaint();
        }
    }

    private void testButton_actionPerformed() {
        String classToTest = "- No class -";
        try {
            int selectedRow = table.getSelectedRow();
            classToTest = (String) data[selectedRow][1];

            Class<?> c = Class.forName(classToTest);
            Object o = c.getDeclaredConstructor().newInstance();
            if (o instanceof UserExitInterface) {
                JOptionPane.showMessageDialog(this,
                        WWGnlUtilities.buildMessage("testing-ok", new String[]{classToTest}),
                        WWGnlUtilities.buildMessage("testing"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        WWGnlUtilities.buildMessage("testing-bad-interface", new String[]{classToTest, c.getName()}),
                        WWGnlUtilities.buildMessage("testing"),
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (ClassNotFoundException cnfe) {

            String message = System.getProperty("java.class.path");
            if (message != null) {
                message = message.replace(File.pathSeparator, "\n");
            }

            JOptionPane.showMessageDialog(this,
                    String.format("Class [%s] not found in  classpath [%s]", classToTest, message),
                    WWGnlUtilities.buildMessage("testing"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.toString(),
                    WWGnlUtilities.buildMessage("testing"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeButton_actionPerformed() {
        removeCurrentLine();
    }

    private void loadUserExit() {
        String ueArchive = WWGnlUtilities.chooseFile(this,
                JFileChooser.FILES_ONLY,
                new String[]{"jar"},
                "UserExit Archives",
                ".",
                WWGnlUtilities.SaveOrOpen.OPEN,
                WWGnlUtilities.buildMessage("select"),
                WWGnlUtilities.buildMessage("user-exit"),
                false);
        if (ueArchive != null && ueArchive.trim().length() > 0) {
            try {
                int ueNum = data.length;
                // Open
                JarFile jarFile = new JarFile(ueArchive);
                // Get Manifest and Validate
                List<UserExitAction> ueal = new ArrayList<>(); // Init
                Manifest manifest = jarFile.getManifest();
                Map<String, Attributes> entries = manifest.getEntries();
                for (String key : entries.keySet()) {
                    Attributes sectionAttributes = manifest.getAttributes(key);
                    String ueStrAck = sectionAttributes.getValue("WW-User-Exit-ack");
                    String ueStrSync = sectionAttributes.getValue("WW-User-Exit-sync");
                    String ueAction = sectionAttributes.getValue("WW-User-Exit-Action");
                    String ueComment = sectionAttributes.getValue("WW-User-Exit-Comment");
                    String ueLocation = sectionAttributes.getValue("WW-User-Exit-Location");

                    if (ueAction != null && ueAction.trim().length() > 0 &&
                            ueComment != null && ueComment.trim().length() > 0 &&
                            ueLocation != null && ueLocation.trim().length() > 0 &&
                            ueStrSync != null && ueStrSync.trim().length() > 0 &&
                            ueStrAck != null && ueStrAck.trim().length() > 0) {
                        UserExitAction uea = new UserExitAction();
                        uea.setRnk(++ueNum);
                        uea.setAck("true".equals(ueStrAck));
                        uea.setSync("true".equals(ueStrSync));
                        uea.setAction(ueAction);
                        uea.setLabel(key);
                        uea.setTip(ueComment);

                        XMLElement menuItem = (XMLElement) originalData.getDocumentElement();
                        String[] menuHierarchy = ueLocation.split(";");
                        // Find or Create the menu hierarchy
                        for (String s : menuHierarchy) {
                            NodeList nl = menuItem.selectNodes("./sub-menu[./@label = '" + s + "']");
                            if (nl.getLength() == 0) {
                                XMLElement newNode = (XMLElement) originalData.createElement("sub-menu");
                                newNode.setAttribute("label", s);
                                menuItem.appendChild(newNode);
                                menuItem = newNode;
                            } else {
                                menuItem = (XMLElement) nl.item(0);
                            }
                        }
                        // Add the new one where it belongs
                        XMLElement newUe = (XMLElement) originalData.createElement("user-exit");
                        menuItem.appendChild(newUe);
                        newUe.setAttribute("sync", ueStrSync);
                        newUe.setAttribute("ack", ueStrAck);
                        newUe.setAttribute("id", Integer.toString(ueNum));
                        XMLElement label = (XMLElement) originalData.createElement("label");
                        XMLElement action = (XMLElement) originalData.createElement("action");
                        XMLElement comment = (XMLElement) originalData.createElement("comment");
                        newUe.appendChild(label);
                        newUe.appendChild(action);
                        newUe.appendChild(comment);
                        Text txt = originalData.createTextNode("#txt");
                        label.appendChild(txt);
                        txt.setNodeValue(key);
                        txt = originalData.createTextNode("#txt");
                        action.appendChild(txt);
                        txt.setNodeValue(ueAction);
                        CDATASection cds = originalData.createCDATASection("#cdata");
                        comment.appendChild(cds);
                        cds.setNodeValue(ueComment);

                        addLineInTable(uea, ueAction, ueComment.trim());
                        ueal.add(uea);
                    }
                }
                if (ueal.size() == 0) {
                    // TODO If no entry found in the manifest ?
                    JOptionPane.showMessageDialog(this,
                            WWGnlUtilities.buildMessage("no-user-exit"),
                            WWGnlUtilities.buildMessage("user-exit"),
                            JOptionPane.WARNING_MESSAGE);
                } else {// Move On
                    jarFile.close();
                    // Move/copy to directory
                    FileInputStream fis = new FileInputStream(new File(ueArchive));
                    String newFileName = ".." + File.separator + "all-user-exits" + File.separator + ueArchive.substring(ueArchive.lastIndexOf(File.separator) + 1);
                    File dest = new File(newFileName);
                    boolean proceed = true;
                    if (dest.exists()) {
                        int resp = JOptionPane.showConfirmDialog(this,
                                WWGnlUtilities.buildMessage("already-exists-override", new String[]{newFileName}),
                                WWGnlUtilities.buildMessage("user-exit"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        proceed = (resp == JOptionPane.YES_OPTION);
                    }
                    if (proceed) {
                        FileOutputStream fos = new FileOutputStream(dest);
                        Utilities.copy(fis, fos);
                        fis.close();
                        fos.close();
                        // Load in Classpath + test?
                        Utilities.addURLToClassPath(new File(newFileName).toURI().toURL());
                        // Test UE Classes
                        for (UserExitAction u : ueal) {
                            String actionClass = u.getAction();
                            try {
                                Class<?> c = Class.forName(actionClass);
                                Object o = c.getDeclaredConstructor().newInstance();
                                if (!(o instanceof UserExitInterface)) {
                                    JOptionPane.showMessageDialog(this,
                                            WWGnlUtilities.buildMessage("does-not-implement-interface", new String[]{actionClass}),
                                            WWGnlUtilities.buildMessage("user-exit"),
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (ClassNotFoundException cnfe) {
                                JOptionPane.showMessageDialog(this,
                                        WWGnlUtilities.buildMessage("class-not-found", new String[]{actionClass}),
                                        WWGnlUtilities.buildMessage("user-exit"),
                                        JOptionPane.ERROR_MESSAGE);
                            } catch (Exception loadEx) {
                                loadEx.printStackTrace();
                            }
                        }
                        // TODO Delete Original ?
                        JOptionPane.showMessageDialog(this,
                                WWGnlUtilities.buildMessage("copied-archive", new String[]{ueArchive, newFileName}),
                                WWGnlUtilities.buildMessage("user-exit"),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
//    else
//      JOptionPane.showMessageDialog(this, 
//                                    "Canceled!", 
//                                    "Add User Exit", 
//                                    JOptionPane.INFORMATION_MESSAGE);
    }

    public void saveData() {
        // 1 - Updates
        for (Object[] datum : data) {
            UserExitAction uea = (UserExitAction) datum[0];
            // Removed or modified only. Create is another task.
            int id = uea.getRnk();
            String action = (String) datum[1];
            String comment = (String) datum[2];
            try {
                XMLElement thisOne = (XMLElement) originalData.selectNodes("//user-exit[./@id = '" + Integer.toString(id) + "']").item(0);
//      thisOne.getChildrenByTagName("label").item(0).getFirstChild().setNodeValue(label);
                thisOne.getChildrenByTagName("action").item(0).getFirstChild().setNodeValue(action);
                CDATASection cds = originalData.createCDATASection("#cdata");
                cds.setNodeValue(comment);
                try {
                    thisOne.getChildrenByTagName("comment").item(0).removeChild(thisOne.getChildrenByTagName("comment").item(0).getFirstChild());
                } catch (Exception exe) {
                    exe.printStackTrace();
                }
                thisOne.getChildrenByTagName("comment").item(0).appendChild(cds);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 2 - Deletes
        try {
            NodeList ueList = originalData.selectNodes("//user-exit");
            for (int i = 0; i < ueList.getLength(); i++) {
                Node ue = ueList.item(i);
                int rnk = Integer.parseInt(((XMLElement) ue).getAttribute("id"));
                boolean found = false;
                for (int j = 0; !found && j < data.length; j++) {
                    UserExitAction uea = (UserExitAction) data[j][0];
                    if (rnk == uea.getRnk()) {
                        found = true;
                    }
                }
                if (!found) {
                    XMLElement parent = (XMLElement) ue.getParentNode();
                    parent.removeChild(ue);
                    recurseUp(parent);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 3 - Renumber the IDs
        try {
            NodeList ueList = originalData.selectNodes("//user-exit");
            for (int i = 0; i < ueList.getLength(); i++) {
                XMLElement ue = (XMLElement) ueList.item(i);
                ue.setAttribute("id", Integer.toString(i + 1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            FileOutputStream fos = new FileOutputStream(new File(WWGnlUtilities.USEREXITS_FILE_NAME));
            originalData.print(fos);
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void recurseUp(XMLElement elmt) throws Exception {
        if (elmt != null && elmt.getNodeName().equals("sub-menu")) {
            if (elmt.selectNodes("./*").getLength() == 0) {
                XMLElement parent = (XMLElement) elmt.getParentNode();
                parent.removeChild(elmt);
                recurseUp(parent);
            }
        }
    }

    public class SelectionListener implements ListSelectionListener {
        JTable table;

        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                removeButton.setEnabled(false);
                testButton.setEnabled(false);
            } else {
                removeButton.setEnabled(true);
                testButton.setEnabled(true);
            }
        }
    }

    static public class TextFieldEditor
            extends JComponent
            implements TableCellEditor {
        JComponent componentToApply;
        protected transient Vector<CellEditorListener> listeners;
        protected transient Object originalValue;

        public TextFieldEditor() {
            super();
            listeners = new Vector<>();
        }

        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            if (value == null) {
                System.out.println("Value is null!!!");
                if (originalValue != null) {
                    value = originalValue;
                } else {
                    System.out.println("Original too!");
                }
            }
            originalValue = value;
            if (value instanceof String) {
                componentToApply = new FieldPlusButtonCellEditor(value);
            }
            return componentToApply;
        }

        public Object getCellEditorValue() {
            //    System.out.println("getCellEditorValue invoked");
            if (componentToApply instanceof JTextField) {
                if (originalValue instanceof String) {
                    return ((JTextField) componentToApply).getText();
                } else if (originalValue instanceof Integer) {
                    return Integer.valueOf(((JTextField) componentToApply).getText());
                } else if (originalValue instanceof Double) {
                    return Double.valueOf(((JTextField) componentToApply).getText());
                } else if (originalValue instanceof Float) {
                    return Float.valueOf(((JTextField) componentToApply).getText());
                } else {
                    return null;
                }
            } else if (componentToApply instanceof FieldPlusButtonCellEditor) {
                String s = (String) ((FieldPlusButtonCellEditor) componentToApply).getCellEditorValue();
                if (s != null) {
                    return s;
                } else {
//        System.out.println("Original Value is a " + originalValue.getClass().getName());
                    return originalValue;
                }
            } else {
                WWContext.getInstance().fireLogging("ParamPanel.getCellEditorValue : Null!! [" + (componentToApply != null ? componentToApply.getClass().getName() : " null") + "]");
                return null;
            }
            //    return null;
        }

        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        public void addCellEditorListener(CellEditorListener l) {
            listeners.addElement(l);
        }

        public void removeCellEditorListener(CellEditorListener l) {
            listeners.removeElement(l);
        }

        protected void fireEditingCanceled() {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = listeners.size(); i >= 0; i--) {
                listeners.elementAt(i).editingCanceled(ce);
            }
        }

        protected void fireEditingStopped() {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = (listeners.size() - 1); i >= 0; i--) {
                listeners.elementAt(i).editingStopped(ce);
            }
        }
    }
}