package utils;

import org.junit.Test;
import static junit.framework.TestCase.assertEquals;

public class DewPointTest {

    @Test
    public void testDewPoint() {
        double airTemp = 20.0;
        double relHum = 65.0;

        double dewPointTemp = WeatherUtil.dewPointTemperature(relHum, airTemp);
        double rounded = Math.round(dewPointTemp * 10) / 10.0;
        // System.out.printf("Dew Point Temp: %f\272C => Rounded %f\272C\n", dewPointTemp, rounded);
        assertEquals("Wrong Value", 13.2, rounded);
    }
}
