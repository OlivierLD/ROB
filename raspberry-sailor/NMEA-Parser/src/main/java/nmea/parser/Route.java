package nmea.parser;

import java.util.List;
public class Route {
    private String name;
    private List<Marker> waypoints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Marker> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Marker> waypoints) {
        this.waypoints = waypoints;
    }
}
