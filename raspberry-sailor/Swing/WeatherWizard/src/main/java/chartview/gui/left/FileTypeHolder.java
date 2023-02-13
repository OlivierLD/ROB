package chartview.gui.left;

import chartview.ctx.ApplicationEventListener;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.gui.util.tree.JTreeFilePanel;

import chartview.ctx.WWContext;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ComponentAdapter;

import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FileTypeHolder
     extends JPanel
{
  private JTreeFilePanel faxTree       = new JTreeFilePanel(ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX].toString(),  
                                                            JTreeFilePanel.FAX_TYPE,         
                                                            this);
  private JTreeFilePanel gribTree      = new JTreeFilePanel(ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX].toString(), 
                                                            JTreeFilePanel.GRIB_TYPE,        
                                                            this);
  private JTreeFilePanel compositeTree = new JTreeFilePanel(ParamPanel.data[ParamData.COMPOSITE_ROOT_DIR][ParamData.VALUE_INDEX].toString(),  
                                                            JTreeFilePanel.COMPOSITE_TYPE,   
                                                            this);
  private JTreeFilePanel patternTree   = new JTreeFilePanel(ParamPanel.data[ParamData.PATTERN_DIR][ParamData.VALUE_INDEX].toString(),    
                                                            JTreeFilePanel.PATTERN_TYPE,     
                                                            this);
  private JTreeFilePanel preDefFaxTree = new JTreeFilePanel(null,                                                    
                                                            JTreeFilePanel.PRE_DEF_FAX_TYPE, 
                                                            this);
  
  private JPanel filler                = new JPanel();
  
  private JTreeFilePanel[] pa = {faxTree, 
                                 gribTree, 
                                 compositeTree, 
                                 patternTree,
                                 preDefFaxTree};

  public final static int FAX_TREE        = 0;
  public final static int GRIB_TREE       = 1;
  public final static int COMPOSITE_TREE  = 2;
  public final static int PATTERN_TREE    = 3;
  public final static int PREDEF_FAX_TREE = 4;
  
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
    
  public FileTypeHolder()
  {
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
    this.setLayout(gridBagLayout1);
    this.setPreferredSize(new Dimension(200, 600));

    this.setSize(new Dimension(200, 300));
    this.add(faxTree, 
             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(5, 5, 0, 5), 0, 0));
    this.add(gribTree, 
             new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(0, 5, 0, 5), 0, 0));
    this.add(compositeTree, 
             new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(0, 5, 0, 5), 0, 0));
    this.add(patternTree, 
             new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(0, 5, 5, 5), 0, 0));

    this.add(preDefFaxTree, 
             new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(0, 5, 5, 5), 0, 0));

    this.add(filler, 
             new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 
                                    new Insets(0, 5, 5, 5), 0, 0));

    this.addComponentListener(new ComponentAdapter()
        {
          public void componentResized(ComponentEvent e)
          {
            notifyComponentSizeChange();
          }
        });
    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
      {
        public String toString()
        {
          return "from FileTypeHolder.";
        }
        
        public void reloadFaxTree() 
        { 
          if ("false".equals(System.getProperty("headless", "false")))
          {
            System.out.println("Reloading Fax Tree"); 
            faxTree.reloadTree(); 
          }
        }
        public void reloadGRIBTree() 
        { 
          if ("false".equals(System.getProperty("headless", "false")))
          {
            System.out.println("Reloading GRIB Tree"); 
            gribTree.reloadTree();
          }
        }
        public void reloadCompositeTree() 
        { 
          if ("false".equals(System.getProperty("headless", "false")))
          {
            System.out.println("Reloading Composite Tree"); 
            compositeTree.reloadTree(); 
          }
        }
        public void reloadPatternTree() 
        { 
          if ("false".equals(System.getProperty("headless", "false")))
          {
            System.out.println("Reloading Pattern Tree"); 
            patternTree.reloadTree(); 
          }
        }
      });

  }
  
  public void notifyComponentSizeChange()
  {
//  System.out.println("Size has changed.");
    int nbExpanded = 0;
    for (int i=0; i<pa.length; i++)
    {
      if (pa[i].isExpanded()) nbExpanded += 1;
    }
    
    int nbCollapsed = pa.length - nbExpanded;    
    int totalHeight = this.getSize().height;
    int currentWidth = this.getSize().width;
    
    // TODO reference to the getMinHeight
    int heightToShare = totalHeight - (nbCollapsed * faxTree.getMinHeight());
    int heightForExpanded = 0;
//    if (nbExpanded == 0) // Enforce first one opened.
//    {
//      pa[0].setExpanded(true);
//      nbExpanded = 1;
//    }  
    if (nbExpanded != 0)
      heightForExpanded = heightToShare / nbExpanded;
    
    Dimension expandedDim = new Dimension(currentWidth, heightForExpanded);
    Dimension collapsedDim = new Dimension(currentWidth, faxTree.getMinHeight());
    for (int i=0; i<pa.length; i++)
    {
      if (pa[i].isExpanded())
      {
        pa[i].setSize(expandedDim);
        pa[i].setPreferredSize(expandedDim);
        pa[i].setMinimumSize(expandedDim);
      }
      else
      {
        pa[i].setSize(collapsedDim);
        pa[i].setPreferredSize(collapsedDim);
        pa[i].setMinimumSize(collapsedDim);
      }
//    pa[i].repaint();
    } 
    if (nbExpanded == 0) // All collapsed
    {
      Dimension d = new Dimension(currentWidth, heightToShare);
      filler.setSize(d);
      filler.setPreferredSize(d);
      filler.setMinimumSize(d);
    }
    else // Zip it up.
    {
      Dimension d = new Dimension(currentWidth, 0);
      filler.setSize(d);
      filler.setPreferredSize(d);
      filler.setMinimumSize(d);
    }
    
    this.validate();
//  this.validateTree();
  }
  
  public void refreshFaxTree()
  { faxTree.reloadTree(); }
  public void refreshGribTree()
  { gribTree.reloadTree(); }
  public void refreshCompositeTree()
  { compositeTree.reloadTree(); }
  public void refreshPatternTree()
  { patternTree.reloadTree(); }
  public void refreshPredefFaxTree()
  { preDefFaxTree.reloadTree(); }
  
  public boolean isTreeExpanded(int i)
  { return pa[i].isExpanded(); }
  public void setExpanded(int i, boolean b)
  { pa[i].setExpanded(b); }
  public int getNbTree() { return pa.length; }
}
