package nmea.parser;

public class Marker {
    private double latitude;
    private double longitude;
    private String label;

    public Marker() {}
    public Marker(double latitude, double longitude, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
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
}
