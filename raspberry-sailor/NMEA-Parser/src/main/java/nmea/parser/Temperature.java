package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Temperature implements Serializable {
	private double temperature = 0d;
	private static final String CELSIUS = "\272C";
	private static final String FARENHEIT = "\272F";
	private static final DecimalFormat FMT = new DecimalFormat("##0.0");

	public Temperature() {
	}

	public double getTemperature() {
		return temperature;
	}

	public Temperature(double temp) {
		this.temperature = temp;
	}

	public double getValue() {
		return this.temperature;
	}

	public String toString() {
		double d = this.temperature;
		return FMT.format(d) + CELSIUS + ", " + FMT.format((5 * d / 9) + 32) + FARENHEIT;
	}
}
