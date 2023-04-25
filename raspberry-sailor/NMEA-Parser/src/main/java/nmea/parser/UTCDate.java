package nmea.parser;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class
UTCDate implements Serializable {
	private Date date = null;
	private long epoch = 0L;
	private FmtDate fmtDate = null;

	public Date getDate() {
		return date;
	}

	public long getEpoch() {
		return epoch;
	}

	public FmtDate getFmtDate() {
		return fmtDate;
	}

	private final static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss 'UTC'");
	static {
		FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public UTCDate() {
	}

	/**
	 *
	 * @param year Can be null
	 * @param month Can be null (Zero based)
	 * @param day Can be null
	 * @param hours Cannot be null
	 * @param minutes Cannot be null
	 * @param seconds Cannot be null
	 * @param milliseconds  Can be null
	 */
	public UTCDate(Integer year, Integer month, Integer day, Integer hours, Integer minutes, Integer seconds, Integer milliseconds) {
		Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
		if (year != null) {
			local.set(Calendar.YEAR, year);
		}
		if (month != null) {
			local.set(Calendar.MONTH, month);
		}
		if (day != null) {
			local.set(Calendar.DATE, day);
		}
		local.set(Calendar.HOUR_OF_DAY, hours);
		local.set(Calendar.MINUTE, minutes);
		local.set(Calendar.SECOND, seconds);
		if (milliseconds != null) {
			local.set(Calendar.MILLISECOND, milliseconds);
		}

		Date utc = local.getTime();
		this.delegate(utc);
	}
	public UTCDate(Date date) {
//		this.date = date;
//		this.epoch = date.getTime();
//		String[] utc = FmtDate.SDF_ARRAY.format(date).split(";");
//
//		this.fmtDate = new FmtDate()
//				.epoch(date.getTime())
//				.year(Integer.parseInt(utc[0]))
//				.month(Integer.parseInt(utc[1]))
//				.day(Integer.parseInt(utc[2]))
//				.hour(Integer.parseInt(utc[3]))
//				.min(Integer.parseInt(utc[4]))
//				.sec(Integer.parseInt(utc[5]));
		this.delegate(date);
	}

	private void delegate(Date date) {
		this.date = date;
		this.epoch = date.getTime();
		String[] utc = FmtDate.SDF_ARRAY.format(date).split(";");

		this.fmtDate = new FmtDate()
				.epoch(date.getTime())
				.year(Integer.parseInt(utc[0]))
				.month(Integer.parseInt(utc[1]))
				.day(Integer.parseInt(utc[2]))
				.hour(Integer.parseInt(utc[3]))
				.min(Integer.parseInt(utc[4]))
				.sec(Integer.parseInt(utc[5]));
	}

	public Date getValue() {
		return this.date;
	}

	@Override
	public String toString() {
		return (date != null) ? FMT.format(this.date) : null;
	}

	public String toString(SimpleDateFormat sdf) {
		return (date != null) ? sdf.format(this.date) : null;
	}
}
