package chartview.gui.util.dialog.places;

public final class Latitude extends LatLong {
    public Latitude(String str) throws Exception {
        super(str, LatLong.LAT);
        if (this.getValue() > 90.0 || this.getValue() < -90.0)
            throw new Exception("Value too big for a latitude. [-90, 90] : " + Double.toString(this.getValue()));
    }
}