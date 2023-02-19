package chartview.gui.util.dialog.places;

public final class Longitude extends LatLong {
    public Longitude(String str) throws Exception {
        super(str, LatLong.LONG);
        if (this.getValue() > 180.0 || this.getValue() < -180.0) {
            throw new Exception("Value too big for a longitude. [-180, 180]");
        }
    }
}