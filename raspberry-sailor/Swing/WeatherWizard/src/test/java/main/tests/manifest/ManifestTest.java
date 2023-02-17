package main.tests.manifest;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestTest {


    public static void main(String... args) {
        try {
            // args[0] like .../Swing/ww-user-exits/WW-UserExits/build/libs/WW-UserExits-1.0.jar
            String jarName;
            if (args.length > 0) {
                jarName = args[0];
            } else {
                jarName = "/Users/<blah>/repos/ROB/raspberry-sailor/Swing/ww-user-exits/WW-UserExits/build/libs/WW-UserExits-1.0.jar";
            }
            JarFile jarFile = new JarFile(jarName);
            Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            System.out.println("-- Main Attributes --");
            mainAttributes.forEach((k, v) -> {
                System.out.printf("%s: %s\n", k, v);
            });
            System.out.println("-- Entries --");
            Map<String, Attributes> entries = manifest.getEntries();
            for (String key : entries.keySet()) {
                System.out.printf(" -- Section %s --\n", key);
                Attributes sectionAttributes = manifest.getAttributes(key);
                sectionAttributes.forEach((k, v) -> {
                    System.out.printf("  %s: %s\n", k, v);
                });
            }
            System.out.println("-------------");
        } catch (Throwable spit) {
            spit.printStackTrace();
        }
        System.out.println("That was it.");
    }
}
