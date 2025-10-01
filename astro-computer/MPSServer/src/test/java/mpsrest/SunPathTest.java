package mpsrest;

import nmea.parser.StringParsers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SunPathTest {

    // Small tests
	public static void main(String... args) {
		String date = "2011-02-06T14:41:42.000Z";
		double lat = -10.761383333333333, lng = -156.24046666666666;
		long ld = StringParsers.durationToDate(date);
		System.out.println(date + " => " + new Date(ld));

		Calendar refDate = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		refDate.setTimeInMillis(ld);

		System.out.println("From Calendar:" + date + " => " + refDate.getTime());

		RESTImplementation me = new RESTImplementation(null);
		List<RESTImplementation.BodyAt> sunPath = me.getSunDataForAllDay(lat, lng, 20, refDate);
		System.out.println("Yo!");
	}


}