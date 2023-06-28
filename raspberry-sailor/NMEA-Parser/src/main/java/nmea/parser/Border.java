package nmea.parser;

import java.util.List;

public class Border {

    private String borderName;
    private List<Marker> markerList;

    public Border() {}
    public Border(String name) {
        this.borderName = name;
    }
    public Border markerList(List<Marker> list) {
        this.markerList = list;
        return this;
    }

    public String getBorderName() {
        return borderName;
    }

    public void setBorderName(String borderName) {
        this.borderName = borderName;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public void setMarkerList(List<Marker> markerList) {
        this.markerList = markerList;
    }
}
