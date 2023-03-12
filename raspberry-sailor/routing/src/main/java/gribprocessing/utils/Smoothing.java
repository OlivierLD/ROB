package gribprocessing.utils;

import java.util.ArrayList;
import java.util.List;

public class Smoothing {
	// Thank you Pythagore
	public static double distance(double fromX,
	                              double fromY,
	                              double toX,
	                              double toY) {
		return Math.sqrt(Math.pow(fromX - toX, 2D) + Math.pow(fromY - toY, 2D));
	}

	/**
	 * @param dp array of DataPoint
	 * @param x  abscissa of the point to calculate for
	 * @param y  ordinate of the point to calculate for
	 * @return Direction & Speed, as Doubles
	 */
	public static List<Double> calculate(DataPoint[] dp, double x, double y) {
		List<Double> data;
		double totalcoeff = 0;
		double totalTWD = 0;
		double totalTWS = 0;
		double totalprmsl = 0D;
		double total500htg = 0D;
		double totaltemp = 0D;
		double totalwheight = 0D;
		double totalrain = 0D;
		double totalu = 0, totalv = 0;
		double totaluC = 0, totalvC = 0;
		double totalCDR = 0;
		double totalCSP = 0;

		double finalTWD = 0D;
		double finalTWS = 0D;
		double finalprmsl = 0D;
		double final500htg = 0D;
		double finaltemp = 0D;
		double finalwheight = 0D;
		double finalrain = 0D;
		float finalu = 0, finalv = 0;
		float finaluC = 0, finalvC = 0;
		double finalCDR = 0D;
		double finalCSP = 0D;

		boolean stuck = false;

		for (DataPoint dataPoint : dp) {
			double dist = Smoothing.distance(dataPoint.x, dataPoint.y, x, y);
			if (dist == 0D) {
				finalTWD = dataPoint.d;
				finalTWS = dataPoint.s;
				finalprmsl = dataPoint.prmsl;
				final500htg = dataPoint.hgt500;
				finaltemp = dataPoint.temp;
				finalwheight = dataPoint.whgt;
				finalrain = dataPoint.rain;
				finalu = dataPoint.u;
				finalv = dataPoint.v;
				finaluC = dataPoint.uC;
				finalvC = dataPoint.vC;
				finalCDR = dataPoint.dC;
				finalCSP = dataPoint.sC;
				stuck = true;
				break;
			} else {
				double coeff = 1.0 / (dist * dist);
				totalcoeff += coeff;
				totalTWD += (coeff * dataPoint.d);
				totalTWS += (coeff * dataPoint.s);
				totalprmsl += (coeff * dataPoint.prmsl);
				total500htg += (coeff * dataPoint.hgt500);
				totaltemp += (coeff * dataPoint.temp);
				totalwheight += (coeff * dataPoint.whgt);
				totalrain += (coeff * dataPoint.rain);
				totalu += (coeff * dataPoint.u);
				totalv += (coeff * dataPoint.v);
				totaluC += (coeff * dataPoint.uC);
				totalvC += (coeff * dataPoint.vC);
				totalCDR += (coeff * dataPoint.dC);
				totalCSP += (coeff * dataPoint.sC);
			}
		}
		if (!stuck) {
			finalTWD = totalTWD / totalcoeff;
			finalTWS = totalTWS / totalcoeff;
			finalprmsl = totalprmsl / totalcoeff;
			final500htg = total500htg / totalcoeff;
			finaltemp = totaltemp / totalcoeff;
			finalwheight = totalwheight / totalcoeff;
			finalrain = totalrain / totalcoeff;
			finalu = (int) Math.round(totalu / totalcoeff);
			finalv = (int) Math.round(totalv / totalcoeff);
			finaluC = (int) Math.round(totaluC / totalcoeff);
			finalvC = (int) Math.round(totalvC / totalcoeff);
			finalCDR = totalCDR / totalcoeff;
			finalCSP = totalCSP / totalcoeff;
		}
		while (finalTWD > 360) {
			finalTWD -= 360;
		}
		while (finalCDR > 360) {
			finalCDR -= 360;
		}

		data = new ArrayList<>(2);
		data.add(finalTWD);
		data.add(finalTWS);
		data.add(finalprmsl);
		data.add(final500htg);
		data.add(finaltemp);
		data.add(finalwheight);
		data.add(finalrain);

		data.add((double)finalu);
		data.add((double)finalv);

		data.add((double)finaluC);
		data.add((double)finalvC);
		data.add(finalCDR);
		data.add(finalCSP);

		return data;
	}

	public final static int TWD_INDEX = 0;
	public final static int TWS_INDEX = 1;
	public final static int PRMSL_INDEX = 2;
	public final static int HGT500_INDEX = 3;
	public final static int TEMP_INDEX = 4;
	public final static int WAVE_INDEX = 5;
	public final static int RAIN_INDEX = 6;
	public final static int UWIND_INDEX = 7;
	public final static int VWIND_INDEX = 8;

	public final static int UCURRENT_INDEX = 9;
	public final static int VCURRENT_INDEX = 10;
	public final static int CDR_INDEX = 11;
	public final static int CSP_INDEX = 12;
}
