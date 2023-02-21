package chartview.gui.util.param;

import chartview.ctx.WWContext;

import javax.swing.*;
import java.awt.*;


public class CategoryPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();

    private final JSplitPane jSplitPane = new JSplitPane();
    private final CategoryJTreeHolder tree = new CategoryJTreeHolder(this);
    private final ParamPanel table = new ParamPanel();

    public CategoryPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(470, 245));
        this.setPreferredSize(new Dimension(470, 245));
        this.add(jSplitPane, BorderLayout.CENTER);
        jSplitPane.setDividerLocation(150);

        jSplitPane.add(tree, JSplitPane.LEFT);
        jSplitPane.add(table, JSplitPane.RIGHT);
    }

    public void getEventFormTree(String str) {
        if (CategoryJTreeHolder.COLORS.equals(str)) {
            table.setDisplayColorPrm();
        } else if (CategoryJTreeHolder.OTHERS.equals(str)) {
            table.setDisplayOtherPrm();
        } else if (CategoryJTreeHolder.POLARS.equals(str)) {
            table.setRoutingPrm();
        } else if (CategoryJTreeHolder.MISC.equals(str)) {
            table.setMiscPrm();
//        } else if (CategoryJTreeHolder.HEADLESS.equals(str)) {
//            table.setHeadlessPrm();
        }
//  else
//    System.out.println("Un-managed event:" + str);
    }

    public void finalPrmUpdate() {
        table.updateData();
    }
}
