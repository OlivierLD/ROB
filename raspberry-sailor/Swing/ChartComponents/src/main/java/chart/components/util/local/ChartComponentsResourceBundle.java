package chart.components.util.local;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ChartComponentsResourceBundle {
    //                                Package                   File Base name
    private static String baseName = "chart.components.util.local." + "cc";
    private static ResourceBundle resourceBundle;

    private ChartComponentsResourceBundle() {
    }

    public static synchronized ResourceBundle getChartComponentsResourceBundle() {
        if (resourceBundle == null) {
            try {
                resourceBundle = ResourceBundle.getBundle(baseName);
//              System.out.println("ResourceBundle created");
            } catch (MissingResourceException mre) {
                if (true) // verbose of some sort...
                    System.err.println("Missing Resource:" + mre.getMessage());
            }
//        } else {
//            System.out.println("ResourceBundle reused");
        }
        return resourceBundle;
    }
}