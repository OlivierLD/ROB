package chart.components.ui;

import java.awt.*;
import java.util.EventObject;

public interface ChartPanelParentInterface {
    void chartPanelPaintComponent(Graphics g);
    boolean onEvent(EventObject eventobject, int i); // return false to override the default behavior
    String getMessForTooltip();
    boolean replaceMessForTooltip();
    void videoCompleted();
    void videoFrameCompleted(Graphics g, Point p);
    void zoomFactorHasChanged(double d);
    void chartDDZ(double top, double bottom, double left, double right);
}
