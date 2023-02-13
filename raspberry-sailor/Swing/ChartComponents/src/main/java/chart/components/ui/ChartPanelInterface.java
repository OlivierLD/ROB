package chart.components.ui;

import calc.GeoPoint;

import java.awt.*;

public interface ChartPanelInterface {
    int ANAXIMANDRE = 0;
    int MERCATOR = 1;
    int LAMBERT = 2;
    int GLOBE_VIEW = 3;
    int SATELLITE_VIEW = 4;
    int CONIC_EQUIDISTANT = 5;
    int STEREOGRAPHIC = 6;
    int POLAR_STEREOGRAPHIC = 7;

    enum Projection {
        ANAXIMANDRE("Anaximandre", ChartPanelInterface.ANAXIMANDRE),
        MERCATOR("Mercator", ChartPanelInterface.MERCATOR),
        LAMBERT("Lambert", ChartPanelInterface.LAMBERT),
        GLOBE_VIEW("Globe", ChartPanelInterface.GLOBE_VIEW),
        SATELLITE_VIEW("Satellite", ChartPanelInterface.SATELLITE_VIEW),
        CONIC_EQUIDISTANT("Conic Equidistant", ChartPanelInterface.CONIC_EQUIDISTANT),
        STEREOGRAPHIC("Stereographic", ChartPanelInterface.STEREOGRAPHIC),
        POLAR_STEREOGRAPHIC("Polar Stereographic", ChartPanelInterface.POLAR_STEREOGRAPHIC);

        private final String label;
        private final int index;

        Projection(String label, int index) {
            this.label = label;
            this.index = index;
        }

        public String label() {
            return this.label;
        }

        public int index() {
            return this.index;
        }
    }

    void setZoomFactor(double d);

    double getZoomFactor();

    void zoomIn();

    void zoomOut();

    double getEastG();

    double getWestG();

    double getNorthL();

    double getSouthL();

    double getContactParallel();

    void setEastG(double d);

    void setWestG(double d);

    void setNorthL(double d);

    void setSouthL(double d);

    void setContactParallel(double d);

    void setVerticalGridInterval(double d);

    void setHorizontalGridInterval(double d);

    double getVerticalGridInterval();

    double getHorizontalGridInterval();

    Point getPanelPoint(double d, double d1);

    boolean contains(GeoPoint geopoint);

    void setChartColor(Color color);

    void setGridColor(Color color);

    void setDdRectColor(Color color);

    Color getChartColor();

    Color getGridColor();

    Color getDdRectColor();

    void setProjection(int i);

    int getProjection();

    int getWidth();

    int getHeight();

    void setGlobeViewLngOffset(double d);

    double getGlobeViewLngOffset();

    void setTransparentGlobe(boolean b);

    boolean isTransparentGlobe();

    void setAntiTransparentGlobe(boolean b);

    boolean isAntiTransparentGlobe();

    void setGlobeViewRightLeftRotation(double d);

    double getGlobeViewRightLeftRotation();

    void setGlobeViewForeAftRotation(double d);

    double getGlobeViewForeAftRotation();

    boolean isBehind(double l, double g);

    void setSatelliteAltitude(double rhoS);

    double getSatelliteAltitude();

    void setSatelliteLongitude(double thetaP);

    double getSatelliteLongitude();

    void setSatelliteLatitude(double satLat);

    double getSatelliteLatitude();
}
