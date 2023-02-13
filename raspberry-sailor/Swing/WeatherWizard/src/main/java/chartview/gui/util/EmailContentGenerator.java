package chartview.gui.util;

import calc.calculation.AstroComputer; // TODO Upgrade to AstroComputerV2
import chartview.gui.util.dialog.places.Position;
import chartview.gui.util.param.ListOfTimeZones;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.tide.TideComputer;
import calc.GeoPoint;
import calc.GeomUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class EmailContentGenerator {

	private static final SimpleDateFormat SDF_FOR_EPHEMERIS = new SimpleDateFormat("EEE d MMM yyyy");
	private static final SimpleDateFormat SDF_FOR_SUN_MOON_EPHEMERIS = new SimpleDateFormat("d MMM yyyy HH:mm (z Z)");
	private static final NumberFormat NF_PHASE = new DecimalFormat("00");
	private static final NumberFormat Z_FORMAT = new DecimalFormat("##0");

	public static String generateEmailContent(String tideStation, Position position, String tz, int weekly)
					throws Exception {
		String content = "<html><body>";

		Calendar reference = GregorianCalendar.getInstance(); // new GregorianCalendar();
		Date date = reference.getTime();
		// reference.setTimeInMillis(date.getTime());
		Calendar started = GregorianCalendar.getInstance();
		boolean keepLooping = true;

		Date firstDay = null;
		while (keepLooping)
		{
			GeoPoint pos = new GeoPoint(position.getLat().getValue(),
							            position.getLong().getValue());
			AstroComputer.setDateTime(reference.get(Calendar.YEAR),
							reference.get(Calendar.MONTH) + 1,
							reference.get(Calendar.DAY_OF_MONTH),
							reference.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))),
							reference.get(Calendar.MINUTE),
							reference.get(Calendar.SECOND));
			AstroComputer.calculate();
			double[] sunRiseSet = AstroComputer.sunRiseAndSet(pos.getLatitude(), pos.getLongitude());
			// See how it is done in the Tide Engine. UI returns the right data.
			double[] moonRiseSet =  AstroComputer.moonRiseAndSet(pos.getLatitude(), pos.getLongitude());

			Calendar sunTransit = new GregorianCalendar();
			sunTransit.setTimeZone(TimeZone.getTimeZone(tz));
			sunTransit.set(Calendar.YEAR, reference.get(Calendar.YEAR));
			sunTransit.set(Calendar.MONTH, reference.get(Calendar.MONTH));
			sunTransit.set(Calendar.DAY_OF_MONTH, reference.get(Calendar.DAY_OF_MONTH));
			sunTransit.set(Calendar.SECOND, 0);

			if (pos != null)
			{
				double tPass = AstroComputer.getSunMeridianPassageTime(position.getLat().getValue(), position.getLong().getValue());
				double r = tPass + AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz), sunTransit.getTime());
				int min = (int)((r - ((int)r)) * 60);
				sunTransit.set(Calendar.MINUTE, min);
				sunTransit.set(Calendar.HOUR_OF_DAY, (int)r);
			}
			double moonPhase = AstroComputer.getMoonPhase(reference.get(Calendar.YEAR),
							reference.get(Calendar.MONTH) + 1,
							reference.get(Calendar.DAY_OF_MONTH),
							reference.get(Calendar.HOUR_OF_DAY), // 12 - (int)Math.round(AstroComputer.getTimeZoneOffsetInHours(TimeZone.getTimeZone(ts.getTimeZone()))),
							reference.get(Calendar.MINUTE),
							reference.get(Calendar.SECOND));
			int phaseInDay = (int)Math.round(moonPhase / (360d / 28d)) + 1;
			if (phaseInDay > 28) phaseInDay = 28;
			if (phaseInDay < 1) phaseInDay = 1;

			SDF_FOR_SUN_MOON_EPHEMERIS.setTimeZone(TimeZone.getTimeZone(tz));
			System.out.println("Sun Rise:" + "TO BE UPDATE"); // new Date(((Calendar)sunRiseSet[0]).getTimeInMillis()));
			System.out.println("Sun Set :" + "TO BE UPDATE"); // new Date(((Calendar)sunRiseSet[1]).getTimeInMillis()));
			System.out.println("Moon Phase: " + phaseInDay + " day(s).");
			double sunD  = AstroComputer.getSunDecl();
			double moonD = AstroComputer.getMoonDecl();
			double deltaT = AstroComputer.getDeltaT();
			if (firstDay == null)
				firstDay = date;
			content += "<h2>" + "Almanac for " + SDF_FOR_EPHEMERIS.format(date) + "</h2>" +
							"<table width='98%'><tr><td valign='top'>" +
							"<pre>At " + pos.toString().replaceAll("\272", "&deg;") + "\n";
			content += ("Sun Rise:" + "TO BE UPDATED"); // SDF_FOR_SUN_MOON_EPHEMERIS.format(new Date(((Calendar)sunRiseSet[0]).getTimeInMillis())) + " (Z=" + lpad(Z_FORMAT.format((Double)sunRiseSet[2]), " ", 3) + "&deg;)\n");
			content += ("Sun Set :" + "TO BE UPDATED"); // SDF_FOR_SUN_MOON_EPHEMERIS.format(new Date(((Calendar)sunRiseSet[1]).getTimeInMillis())) + " (Z=" + lpad(Z_FORMAT.format((Double)sunRiseSet[3]), " ", 3) + "&deg;)\n");
			content += ("Sun transit :" + SDF_FOR_SUN_MOON_EPHEMERIS.format(new Date(sunTransit.getTimeInMillis())) + "\n");

			content += ("Moon Rise:" + "TO BE UPDATED"); // SDF_FOR_SUN_MOON_EPHEMERIS.format(new Date(((Calendar)moonRiseSet[0]).getTimeInMillis())) + "\n");
			content += ("Moon Set :" + "TO BE UPDATED"); // SDF_FOR_SUN_MOON_EPHEMERIS.format(new Date(((Calendar)moonRiseSet[1]).getTimeInMillis())) + "\n");

			content += ("Moon Phase: " + phaseInDay + " day(s).");
			content += "</pre></td><td valign='top' align='right'>";
			content += ("<img src='http://www.lediouris.net/moon/phase" + NF_PHASE.format(phaseInDay) + ".gif'>"); // TODO Southern hemisphere
			content += "</td></tr></table>";
			content += "<hr><pre>";
			content += ("At " + SDF_FOR_SUN_MOON_EPHEMERIS.format(date) + ":\n");
			content += ("Sun D : " + lpad(GeomUtil.decToSex(sunD), " ", 15) + "\n");
			content += ("Moon D: " + lpad(GeomUtil.decToSex(moonD), " ", 15) + "\n");
			content += "<hr>";
			content += ("Tide at " + tideStation + ":\n");
			content += TideComputer.calculateTideAt(tideStation, reference);
			content += "</pre>";
			content += "<hr>";
			content += "<small>Calculated with &Delta;T=" + deltaT + "</small><br>";
			if (started.get(Calendar.DAY_OF_WEEK) == weekly && // On Mondays, print almanac for one week
							TimeUnit.DAYS.convert((reference.getTimeInMillis() - started.getTimeInMillis()), TimeUnit.MILLISECONDS) < 7)
			{
				reference.add(Calendar.HOUR_OF_DAY, 24);
				date = reference.getTime();
			}
			else
				keepLooping = false;
		}
		content += "</body></html>";

		return content;
	}

	private static String lpad(String s, String w, int len)
	{
		while (s.length() < len)
			s = w + s;
		return s;
	}

	/**
	 * For standalone tests
	 *
	 * @param args ignored
	 */
	public static void main(String... args) throws Exception {
//		Position position = new Position( // ~ La Ventana
//						new chartview.gui.util.dialog.places.Latitude("24 03.76 N"),
//						new chartview.gui.util.dialog.places.Longitude("119 59.50 W"));
		Position position = new Position( // ~ Ocean Beach
						new chartview.gui.util.dialog.places.Latitude("37 46 N"),
						new chartview.gui.util.dialog.places.Longitude("122 31 W"));
		int weekly = Calendar.MONDAY;
		String tideStation = "Ocean Beach"; // "La Paz, Baja California Sur, Mexico";
		System.setProperty("deltaT", "68.3964");
		String content = generateEmailContent(
						tideStation,
						position,
						"America/Los_Angeles",// "America/Mazatlan",  //
						weekly);
		System.out.println(content);
	}
}
