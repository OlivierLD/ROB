package chartview.util.local;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WeatherAssistantResourceBundle {
    //                               Package                   File Base name
    private static String baseName = "chartview.util.local." + "wa";
    private static ResourceBundle resourceBundle; // Singleton

    private WeatherAssistantResourceBundle() {
    }

    public static synchronized ResourceBundle getWeatherAssistantResourceBundle() {
        if (resourceBundle == null) {
            try {
                resourceBundle = ResourceBundle.getBundle(baseName);
                // System.out.println("ResourceBundle created");
            } catch (MissingResourceException mre) {
                if (true) { // verbose of some sort...
                    System.err.println("Missing Resource:" + mre.getMessage());
                }
            }
        }
//  else
//    System.out.println("ResourceBundle reused");
        return resourceBundle;
    }
}
