package chartview.gui.toolbar.controlpanels;

import chart.components.ui.ChartPanel;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.toolbar.controlpanels.projection.AnaximandreMercatorPanel;
import chartview.gui.toolbar.controlpanels.projection.ConicPanel;
import chartview.gui.toolbar.controlpanels.projection.GlobePanel;
import chartview.gui.toolbar.controlpanels.projection.SatellitePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class ProjectionPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final AnaximandreMercatorPanel anaximandre = new AnaximandreMercatorPanel();
    private final AnaximandreMercatorPanel mercator = new AnaximandreMercatorPanel();
    private final AnaximandreMercatorPanel stereographic = new AnaximandreMercatorPanel();
    private final AnaximandreMercatorPanel polarStereographic = new AnaximandreMercatorPanel();
    private final ConicPanel lambert = new ConicPanel();
    private final ConicPanel equidistant = new ConicPanel();
    private final GlobePanel globe = new GlobePanel();
    private final SatellitePanel satellite = new SatellitePanel();
    private final JButton applyButton = new JButton();
    private final JTabbedPane projectionTabbedPane = new JTabbedPane();

    private final transient Projection[] projections = new Projection[]{new Projection(ChartPanel.ANAXIMANDRE, "Anaximandre"),
            new Projection(ChartPanel.MERCATOR, "Mercator"),
            new Projection(ChartPanel.POLAR_STEREOGRAPHIC, "Polar Stereo"),
            new Projection(ChartPanel.STEREOGRAPHIC, "Stereographic"),
            new Projection(ChartPanel.LAMBERT, "Lambert"),
            new Projection(ChartPanel.CONIC_EQUIDISTANT, "Conic Equ."),
            new Projection(ChartPanel.GLOBE_VIEW, "Globe"),
            new Projection(ChartPanel.SATELLITE_VIEW, "Satellite")};
    private static final int ANAXIMANDRE_INDEX = 0;
    private static final int MERCATOR_INDEX = 1;
    private static final int POLAR_STEREO_INDEX = 2;
    private static final int STEREOGRAPHIC_INDEX = 3;
    private static final int LAMBERT_INDEX = 4;
    private static final int CONIC_EQU_INDEX = 5;
    private static final int GLOBE_INDEX = 6;
    private static final int SATELLITE_INDEX = 7;

    public ProjectionPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {

        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from ProjectionPanel.";
            }

            public void setProjection(int p) {
                switch (p) {
                    case ChartPanel.ANAXIMANDRE:
                        projectionTabbedPane.setSelectedIndex(ANAXIMANDRE_INDEX);
                        break;
                    case ChartPanel.LAMBERT:
                        projectionTabbedPane.setSelectedIndex(LAMBERT_INDEX);
                        break;
                    case ChartPanel.CONIC_EQUIDISTANT:
                        projectionTabbedPane.setSelectedIndex(CONIC_EQU_INDEX);
                        break;
                    case ChartPanel.GLOBE_VIEW:
                        projectionTabbedPane.setSelectedIndex(GLOBE_INDEX);
                        break;
                    case ChartPanel.SATELLITE_VIEW:
                        projectionTabbedPane.setSelectedIndex(SATELLITE_INDEX);
                        break;
                    case ChartPanel.STEREOGRAPHIC:
                        projectionTabbedPane.setSelectedIndex(STEREOGRAPHIC_INDEX);
                        break;
                    case ChartPanel.POLAR_STEREOGRAPHIC:
                        projectionTabbedPane.setSelectedIndex(POLAR_STEREO_INDEX);
                        break;
                    case ChartPanel.MERCATOR:
                    default:
                        projectionTabbedPane.setSelectedIndex(MERCATOR_INDEX);
                        break;
                }
            }

            public void setContactParallel(double d) {
                lambert.setContactParallel(d);
                equidistant.setContactParallel(d);
            }

            // FIXME Globe Parameters: lat, lng, tilt, transparent
            public void setGlobeProjPrms(double lat, double lng, double tilt, boolean b) {
                globe.setLatitude(lat);
                globe.setLongitude(lng);
                // Forward parameters to the right panel.
            }

            public void setGlobeProjPrms(double lat, double lng) {
                globe.setLatitude(lat);
                globe.setLongitude(lng);
            }

            public void setSatellitePrms(double lat, double lng, double alt, boolean b) {
                satellite.setLatitude(lat);
                satellite.setLongitude(lng);
                satellite.setAltitude(alt);
                satellite.setSatelliteOpaque(b);
            }

        });
        this.setLayout(gridBagLayout1);
        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 180));
        this.setMinimumSize(new Dimension(ControlPane.WIDTH, 180));
        this.setSize(new Dimension(ControlPane.WIDTH, 180));

        projectionTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        lambert.setProjType(ChartPanel.LAMBERT);
        equidistant.setProjType(ChartPanel.CONIC_EQUIDISTANT);

        projectionTabbedPane.add("Anaximandre", anaximandre);
        projectionTabbedPane.add("Mercator", mercator);
        projectionTabbedPane.add("Polar Stereo", polarStereographic);
        projectionTabbedPane.add("Stereographic", stereographic);
        projectionTabbedPane.add("Lambert", lambert);
        projectionTabbedPane.add("Conic Equ.", equidistant);
        projectionTabbedPane.add("Globe", globe);
        projectionTabbedPane.add("Satellite", satellite);

//  projectionTabbedPane.setEnabledAt(4, false);

        applyButton.setText("Apply");
        applyButton.setActionCommand("apply");
        applyButton.addActionListener(this::applyButton_actionPerformed);
        projectionTabbedPane.setPreferredSize(new Dimension(200, 130));

        this.add(applyButton,
                new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(5, 0, 0, 0), 0, 0));
        this.add(projectionTabbedPane,
                new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setSelectedProjection(int projection) {
        //  System.out.println("Setting Projection to " + projection);
        for (int i = 0; i < projections.length; i++) {
            if (projections[i].getType() == projection) {
                projectionTabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    private void applyButton_actionPerformed(ActionEvent e) {
        System.out.println("Setting value to " + (projections[projectionTabbedPane.getSelectedIndex()]).toString());
        System.out.println("(" + projections[projectionTabbedPane.getSelectedIndex()].getType() + ")");
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            int p = (projections[projectionTabbedPane.getSelectedIndex()]).getType();
            l.setProjection(p);
            if (p == ChartPanel.LAMBERT) {
                // Need contact parallel
                double cp = 0.0;
                try {
                    cp = lambert.getContactParallel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                l.setContactParallel(cp);
            } else if (p == ChartPanel.CONIC_EQUIDISTANT) {
                // Need contact parallel
                double cp = 0.0;
                try {
                    cp = equidistant.getContactParallel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                l.setContactParallel(cp);
            } else if (p == ChartPanel.GLOBE_VIEW) {
                // Set chart boundaries to max
                // Need eye position
                try {
                    double lat = globe.getLatitude();
                    double lng = globe.getLongitude();
                    double tlt = globe.getTilt();
                    boolean opaque = globe.isGlobeOpaque();
                    l.setGlobeProjPrms(lat, lng, tlt, opaque);
                } catch (Exception ex) {
                    WWContext.getInstance().fireExceptionLogging(ex);
                    ex.printStackTrace();
                }
            } else if (p == ChartPanel.SATELLITE_VIEW) {
                // Set chart boundaries to max
                // Need eye position
                try {
                    double lat = satellite.getLatitude();
                    double lng = satellite.getLongitude();
                    double alt = satellite.getAltitude();
                    boolean opaque = satellite.isSatelliteOpaque();
                    l.setSatelliteProjPrms(lat, lng, alt, opaque);
                } catch (Exception ex) {
                    WWContext.getInstance().fireExceptionLogging(ex);
                    ex.printStackTrace();
                }
            } else { // Mercator & Anaximandre, Stereo
                // Does not need anything
            }
        }
    }

    private static class Projection {
        int type;
        String name;

        public Projection(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public int getType() {
            return this.type;
        }

        public String toString() {
            return name;
        }
    }
}
