package main.help;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Properties;


public class AboutBox
        extends JPanel {
    private transient Border border = BorderFactory.createEtchedBorder();
    private GridBagLayout layoutMain = new GridBagLayout();
    private JLabel labelCompany = new JLabel();
    private JLabel labelCopyright = new JLabel();
    private JLabel labelAuthor = new JLabel();
    private JLabel labelTitle = new JLabel();
    private JLabel jLabel1 = new JLabel();
    private JLabel contactLabel = new JLabel();

    private JTabbedPane tabbedPane = new JTabbedPane();

    private final static String KEY = "Name";
    private final static String VALUE = "Value";

    private final static String[] names =
            {KEY, VALUE};

    private transient TableModel dataModel;

    private transient Object[][] data = new Object[0][names.length];

    JTable table;
    JScrollPane scrollPane;
    BorderLayout borderLayout = new BorderLayout();
    JPanel panelOne = new JPanel();
    JPanel panelTwo = new JPanel();
    JPanel panelThree = new JPanel();
    private JLabel compiledLabel = new JLabel();
    private JLabel proxyLabel = new JLabel();
    private JLabel jLabel2 = new JLabel();

    public AboutBox() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(new BorderLayout());
        this.setBorder(border);

        this.setSize(new Dimension(500, 300));
        this.setPreferredSize(new Dimension(500, 300));
        this.setMaximumSize(new Dimension(600, 300));
        tabbedPane.add("Info", panelOne);
        tabbedPane.add("Properties", panelTwo);
        tabbedPane.add("Disclaimer", panelThree);

        JEditorPane jEditorPane = new JEditorPane();
        JScrollPane jScrollPane = new JScrollPane();
        panelThree.setLayout(new BorderLayout());
        jEditorPane.setEditable(false);
        jEditorPane.setFocusable(false);
        jEditorPane.setFont(new Font("Verdana", Font.PLAIN, 10));
        jEditorPane.setBackground(Color.lightGray);
        jScrollPane.getViewport().add(jEditorPane, null);

        try {
            jEditorPane.setPage(this.getClass().getResource("disclaimer.html"));
            jEditorPane.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.add(tabbedPane, BorderLayout.CENTER);

        panelOne.setLayout(layoutMain);

        compiledLabel.setText("Compiled " + WWContext.getInstance().getCompiled());
        proxyLabel.setText("Proxy");
        // -Dhttp.proxyHost=www-proxy.us.oracle.com  -Dhttp.proxyPort=80
        jLabel2.setText("The Weather Wizard");
        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 12));
        Properties props = System.getProperties();
        String proxyHost = props.getProperty("http.proxyHost", "");
        String proxyPort = props.getProperty("http.proxyPort", "");
        if (proxyHost.trim().length() == 0 || proxyPort.trim().length() == 0) {
            proxyLabel.setText("No Proxy set");
        } else {
            proxyLabel.setText("Proxy: " + proxyHost + ":" + proxyPort);
        }
        labelTitle.setText("GRIB, Weather Faxes, Charts");
        // jLabel1.setIcon(new ImageIcon(this.getClass().getResource("onecameltranspsmall.png")));
        jLabel1.setIcon(new ImageIcon(this.getClass().getResource("wizard150.png")));
        contactLabel.setText("Contact: olivier@lediouris.net");
        labelAuthor.setText("version " + WWContext.VERSION_NUMBER);
        labelCopyright.setText("Copyright 2007 and beyond");
        labelCompany.setText("<html><u>The Weather Wizard Project</u></html>");
        labelCompany.setForeground(Color.blue);
        labelCompany.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                labelCompany_mouseClicked(e);
            }

            public void mouseEntered(MouseEvent e) {
                labelCompany.setForeground(WWGnlUtilities.PURPLE);
                labelCompany.repaint();
            }

            public void mouseExited(MouseEvent e) {
                labelCompany.setForeground(Color.blue);
                labelCompany.repaint();
            }
        });
        panelOne.add(labelTitle, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 15, 0, 15), 0, 0));
        panelOne.add(labelAuthor, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 15, 0, 15), 0, 0));
        panelOne.add(labelCopyright, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 15, 0, 15), 0, 0));
        panelOne.add(labelCompany, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 15, 10, 15), 0, 0));
        panelOne.add(jLabel1, new GridBagConstraints(0, 0, 1, 9, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelOne.add(contactLabel, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(10, 15, 0, 0), 0, 0));
        panelOne.add(compiledLabel, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 15, 0, 0), 0, 0));
        panelOne.add(proxyLabel, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 15, 0, 0), 0, 0));
        panelOne.add(jLabel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 15, 0, 0), 0, 0));
        initTable();

        panelThree.add(jScrollPane, BorderLayout.CENTER);
    }

    private void labelCompany_mouseClicked(MouseEvent e) {
        try {
            labelCompany.setForeground(WWGnlUtilities.PURPLE);
            Utilities.openInBrowser("http://weather.lediouris.net/");
        } catch (Exception ignore) {
            System.err.println(ignore.toString());
        }
    }

    private Object[][] addLineInTable(String k, String v) {
        return addLineInTable(k, v, data);
    }

    private Object[][] addLineInTable(String k, String v, Object[][] d) {
        int len = 0;
        if (d != null)
            len = d.length;
        Object[][] newData = new Object[len + 1][names.length];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < names.length; j++)
                newData[i][j] = d[i][j];
        }
        newData[len][0] = k;
        newData[len][1] = v;
        //  System.out.println("Adding " + k + ":" + v);
        return newData;
    }

    private void initTable() {
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

            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
            }
        };
        table = new JTable(dataModel) {
            /* For the tooltip text */

            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    try {
                        jc.setToolTipText(getValueAt(rowIndex, vColIndex).toString());
                    } catch (Exception ex) {
                        System.err.println("Property Table:" + ex.getMessage());
                        WWContext.getInstance().fireExceptionLogging(ex);
                        ex.printStackTrace();
                    }
                }
                return c;
            }
        };

        scrollPane = new JScrollPane(table);
        panelTwo.setLayout(new BorderLayout());
        panelTwo.add(scrollPane, BorderLayout.CENTER);

        Properties props = System.getProperties();
        Enumeration keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) props.get(key);
            data = addLineInTable(key, value, data);
        }
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        table.repaint();
    }
}
