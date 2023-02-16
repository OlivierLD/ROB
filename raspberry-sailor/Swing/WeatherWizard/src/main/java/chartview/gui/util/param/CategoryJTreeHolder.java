package chartview.gui.util.param;

import chartview.util.WWGnlUtilities;
import chartview.ctx.WWContext;

import java.awt.BorderLayout;

import java.util.Enumeration;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;


public class CategoryJTreeHolder 
    extends JPanel
{
  public final static String DISPLAY = WWGnlUtilities.buildMessage("display");
  public final static String   COLORS = WWGnlUtilities.buildMessage("colors");
  public final static String   OTHERS = WWGnlUtilities.buildMessage("others");

  public final static String POLARS = WWGnlUtilities.buildMessage("polars-nmea");
  public final static String MISC = WWGnlUtilities.buildMessage("misc");
  
  public final static String HEADLESS = WWGnlUtilities.buildMessage("headless");
    
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane = new JScrollPane();

  private JTree dataTree = new JTree();
  private final TreeSelectionListener treeMonitor = new TreeMonitor();

  private DefaultMutableTreeNode root = null;
  TreeNode currentlySelectedNode = null;

  CategoryPanel parent;
  
  public CategoryJTreeHolder(CategoryPanel pp)
  {
    parent = pp;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
  }
  
  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    jScrollPane.getViewport().add(dataTree, null);
    this.add(jScrollPane, BorderLayout.CENTER);
    dataTree.addTreeSelectionListener(treeMonitor);
    // Enable Tooltips
    ToolTipManager.sharedInstance().registerComponent(dataTree);
    root = new DefaultMutableTreeNode("invisible-root");
    if (root != null)
    {
      dataTree.setModel(new DefaultTreeModel((TreeNode)root)); 
      DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)dataTree.getCellRenderer();
      // Remove the icons
      renderer.setLeafIcon(null);
      renderer.setClosedIcon(null);
      renderer.setOpenIcon(null);
    }
    dataTree.setRootVisible(false);
    
    DefaultMutableTreeNode display = new DefaultMutableTreeNode(DISPLAY, true);
    display.add(new DefaultMutableTreeNode(COLORS,  true));
    display.add(new DefaultMutableTreeNode(OTHERS,  true));
    
    root.add(display);
    root.add(new DefaultMutableTreeNode(POLARS,   true));
    root.add(new DefaultMutableTreeNode(MISC,     true));
    root.add(new DefaultMutableTreeNode(HEADLESS, true));
    
    ((DefaultTreeModel)dataTree.getModel()).reload(root);
    expandAll(dataTree, true);
  }
  
  public void expandAll(JTree tree, boolean expand) 
  {
    TreeNode root = (TreeNode)tree.getModel().getRoot();
    // Traverse tree from root
    expandAll(tree, new TreePath(root), expand);
  }
  
  private void expandAll(JTree tree, TreePath parent, boolean expand) 
  {
    // Traverse children
    TreeNode node = (TreeNode)parent.getLastPathComponent();
    if (node.getChildCount() >= 0) 
    {
      for (Enumeration e=node.children(); e.hasMoreElements(); ) 
      {
        TreeNode n = (TreeNode)e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path, expand);
      }
    }
    // Expansion or collapse must be done bottom-up
    if (expand) 
    {
      tree.expandPath(parent);
    } 
    else 
    {
      tree.collapsePath(parent);
    }
  }
      
  class TreeMonitor implements TreeSelectionListener
  {
    JTextField feedback = null;
    
    public TreeMonitor()
    {
      this(null);
    }

    public TreeMonitor(JTextField fld)
    {
      feedback = fld;
    }
    
    public void valueChanged(TreeSelectionEvent tse)
    {
      TreePath tp = tse.getNewLeadSelectionPath();
      if (tp == null)
        return;
      DefaultMutableTreeNode dtn = (DefaultMutableTreeNode)tp.getLastPathComponent();      
      currentlySelectedNode = dtn;
//    System.out.println("Selected " + dtn.getUserObject().toString());
      parent.getEventFormTree(dtn.getUserObject().toString());
    }
  }
}