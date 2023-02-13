package chart.components.util;

import chart.components.util.local.ChartComponentsResourceBundle;

public final class GnlUtilities {
    public static String buildMessage(String id) {
        return buildMessage(id, null);
    }

    public static String buildMessage(String id, String[] data) {
        String mess = ChartComponentsResourceBundle.getChartComponentsResourceBundle().getString(id);
        for (int i = 0; data != null && i < data.length; i++) {
            String toReplace = String.format("{$%d}", (i + 1));
//            System.out.println("Replacing " + toReplace + " with " + data[i] + " in " + mess);
            mess = mess.replace(toReplace, data[i]);
//            mess = replaceString(mess, toReplace, data[i]);
        }
        return mess;
    }
}
