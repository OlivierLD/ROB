package utils;

public class WeatherUtil {

	/**
	 * Compliant with http://www.dpcalc.org/
	 *
	 * @param hum in %
	 * @param temp in Celsius
	 * @return dew point temperature in Celsius
	 */
	public static double dewPointTemperature(double hum, double temp) {
		double dewPointTemp = 0d;
		double c1 = 6.10780;
		double c2 = (temp > 0) ? 17.08085 : 17.84362;
		double c3 = (temp > 0) ? 234.175 : 245.425;

		double pz = c1 * Math.exp((c2 * temp) / (c3 + temp));
		double pd = pz * (hum / 100d);

		dewPointTemp = (- Math.log(pd / c1) * c3) / (Math.log(pd / c1) - c2);

		return dewPointTemp;
	}

	/**
	 * See https://carnotcycle.wordpress.com/2012/08/04/how-to-convert-relative-humidity-to-absolute-humidity/
	 * Absolute Humidity (grams/m3) = (6.112 × e^[(17.67 × T)/(T+243.5)] × rh × 2.1674) / (273.15+T)
	 * @param temp Air Temp in Celsius
	 * @param rh Relative Humidity in %
	 * @return the Absolute humidity in g/m3
	 */
	public static double absoluteHumidity(double temp, double rh) {
		double ah = (6.112 * Math.exp((17.67 * temp) / (temp + 243.5)) * rh * 2.1674) / (273.15 + temp);
		return ah;
	}
}