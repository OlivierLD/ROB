package tests;

import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is just a test, not a unit-test.
 * Direct access, no REST here.
 */
public class VerySimpleMain {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");

	private final static BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " Argument(s)...");
		boolean xmlTest = true;

		// Set tide.verbose to true
		System.setProperty("tide.verbose", "true");

		if (xmlTest) {
			System.out.println("XML Tests");
			backEndTideComputer.connect();
			backEndTideComputer.setVerbose(false);

			// Some tests
			if (true) {
				TideStation ts = null;

				List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();
				// TODO Dump at will (system variable ?)

				Calendar now = GregorianCalendar.getInstance();
				String location = null;
				if (true) {
//					System.setProperty("tide.verbose", "true");
//					location = URLEncoder.encode("Port Townsend", StandardCharsets.UTF_8.toString()).replace("+", "%20");
//					location = URLEncoder.encode("Port-Navalo", StandardCharsets.UTF_8.toString()).replace("+", "%20");
					location = URLEncoder.encode("Port-Tudy", StandardCharsets.UTF_8.toString()).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						if (ts != null) {
							if (true) {
								double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
								System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
							}
							double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
							System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
							if (false && ts.isTideStation()) {
								System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(TideUtilities.getWaterHeightIn(wh, ts, TideStation.METERS)) + " " + TideStation.METERS);
							}
						}
					} else {
						System.out.printf("%s not found :( \n", location);
					}
				}
			}
			backEndTideComputer.disconnect();
		}
	}
}
