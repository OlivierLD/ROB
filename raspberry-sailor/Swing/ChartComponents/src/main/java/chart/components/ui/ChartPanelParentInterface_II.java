package chart.components.ui;

import java.awt.*;
import java.util.EventObject;

public interface ChartPanelParentInterface_II extends ChartPanelParentInterface {
    void chartPanelPaintComponentAfter(Graphics g);
    void afterEvent(EventObject eventobject, int i);
}
