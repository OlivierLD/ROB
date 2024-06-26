package main.splash;

import calc.GeoPoint;
import calc.GeomUtil;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;
import main.help.AboutBox;

import java.lang.reflect.Method;
import java.util.Date;

public class Splasher {
    static {
        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public void applicationLoaded() {
                SplashWindow.disposeSplash();
            }
        });
    }

    public static void main(String... args) {

//        GeoPoint here = new GeoPoint(47.677667, -3.135667);
//        System.out.printf("Developed here: %s\n", here.toString(GeomUtil.DEFAULT_DEG));

        boolean headlessMode = ("true".equals(System.getProperty("headless", "false")) || "yes".equals(System.getProperty("headless", "false")));
        if (!headlessMode) {
            System.out.println(WWGnlUtilities.buildMessage("welcome"));
            //  SplashWindow.splash(Splasher.class.getResource("LogiSail.png"));
            SplashWindow.splash(AboutBox.class.getResource("wizard150.png"));
            SplashWindow.invokeMain("main.ChartAdjust", args);
            SplashWindow.disposeSplash();
        } else {
            try {
                final Date started = new Date();
                Class<?> main = Class.forName("main.ChartAdjust");
                Method mainMethod = main.getMethod("main", String[].class);
                Object[] params = new Object[1];
                params[0] = args;
                mainMethod.invoke(null, params);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    Date now = new Date();
                    System.out.println("** Shutting down (headless) at " + now.toString() + " (was running for " + Utilities.readableTime(now.getTime() - started.getTime()) + ")");
                }, "Shutdown-Hook"));

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }
}