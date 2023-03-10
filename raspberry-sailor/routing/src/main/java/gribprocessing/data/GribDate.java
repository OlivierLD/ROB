package gribprocessing.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GribDate implements Serializable, Cloneable, Comparable<GribDate> { // extends Date { // Removed extends Date for Jackson to be happy
	private Date date;
	private final long epoch;
	private final String formattedUTCDate;
	private int height;
	private int width;
	private double stepx;
	private double stepy;
	private double top, bottom, left, right;

	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'"); // I know, its's weird.
//	static {
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//	}

	public GribDate(Date d, int h, int w, double x, double y, double t,
	                double b, double l, double r) {
//		super(d.getTime());
		this.date = d;
		this.epoch = d.getTime();
		this.formattedUTCDate = SDF_UTC.format(d);
		this.height = h;
		this.width = w;
		this.stepx = x;
		this.stepy = y;
		this.left = l;
		this.right = r;
		this.top = t;
		this.bottom = b;
	}

	public void setGDate(Date date) {
		this.date = date;
	}

	public Date getGDate() {
		return date;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setStepx(double stepx) {
		this.stepx = stepx;
	}

	public double getStepx() {
		return stepx;
	}

	public void setStepy(double stepy) {
		this.stepy = stepy;
	}

	public double getStepy() {
		return stepy;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getTop() {
		return top;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getBottom() {
		return bottom;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getLeft() {
		return left;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getRight() {
		return right;
	}

//	@Override
	public Date getDate() {
		return date;
	}

	public long getEpoch() {
		return epoch;
	}

	public String getFormattedUTCDate() {
		return formattedUTCDate;
	}

	@Override
	public int compareTo(GribDate o) {
		return this.getDate().compareTo(o.getDate());
	}

	@Override
	public String toString() {
		return this.getFormattedUTCDate();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other == null) {
			return false;
		} else if (other instanceof GribDate) {
			return this.getDate().equals(((GribDate)other).getDate()) &&
					this.getTop() == ((GribDate)other).getTop() &&
					this.getBottom() == ((GribDate)other).getBottom() &&
					this.getLeft() == ((GribDate)other).getLeft() &&
					this.getRight() == ((GribDate)other).getRight();  // TODO More ?
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.getDate().hashCode();
	}
}
