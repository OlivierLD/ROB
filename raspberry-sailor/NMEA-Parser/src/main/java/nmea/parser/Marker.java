package nmea.parser;

public class Marker {
    private double latitude;
    private double longitude;
    private String label;
    private String type;

    public Marker() {}
    public Marker(double latitude, double longitude, String label, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
        this.type = type;
    }
    public Marker latitude(double latitude) {
        this.latitude = latitude;
        return this;
    }
    public Marker longitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
    public Marker label(String label) {
        this.label = label;
        return this;
    }
    public Marker type(String type) {
        this.type = type;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
