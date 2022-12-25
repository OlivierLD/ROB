package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UTC implements Serializable {
	private final int h, m;
	private final float s;

	public UTC(int h, int m, float s) {
		this.h = h;
		this.m = m;
		this.s = s;
	}

	public int getH() {
		return h;
	}

	public int getM() {
		return m;
	}

	public float getS() {
		return s;
	}

	public Date getDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, this.getH());
		cal.set(Calendar.MINUTE, this.getM());
		cal.set(Calendar.SECOND, (int) this.getS());
		return cal.getTime();
	}

	private final static DecimalFormat DF2 = new DecimalFormat("00");
	private final static DecimalFormat DF23 = new DecimalFormat("00.000");

	public String toString() {
		return DF2.format(h) + ":" + DF2.format(m) + ":" + DF23.format(s);
	}
}
