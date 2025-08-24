package mps;

import calc.CelestialDeadReckoning;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Sun, Moon, Venus, Mars, Jupiter, Spica, Denebola
 * Used to produce output for the web/index.html (web/json/*.json)
 */
public class Context01 {

    private final static ObjectMapper mapper = new ObjectMapper();

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    // Output
    public static void spitOut(PlanDesSommetsPlayground.ConeDefinition coneDefinition, boolean verbose) {
        try {
            String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(coneDefinition);
            if (verbose) {
                System.out.printf("-------------- %s ----------------\n", coneDefinition.bodyName);
                System.out.printf("Producing an array of %d element(s)\n", coneDefinition.circle.size());
                System.out.println(content);
                System.out.println("--------------------------------");
            }
            String userDir = System.getProperty("user.dir");
            String fileName = String.format("%s/web/json/%s.json", userDir, coneDefinition.bodyName.replace(" ", "-"));
            try {
                File f = new File(fileName);
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(content + "\n");
                bw.close();
                System.out.printf("Data written to %s\n", f.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }

    }

    public static void main(String... args) {

        // Point de reference
        double latitude = 47.677667;
        double longitude = -3.135667;

        AstroComputerV2 ac = new AstroComputerV2();

        /*
         * 19-Aug-2025 12:58:25 UTC
         */
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 2025);
        date.set(Calendar.MONTH, 7); // Aug
        date.set(Calendar.DAY_OF_MONTH, 19);
        date.set(Calendar.HOUR_OF_DAY, 12); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 58);
        date.set(Calendar.SECOND, 25);

        System.out.printf("Calculation launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        double deltaT = ac.getDeltaT(); // Unused for now

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();

        final double venusGHA = ac.getVenusGHA();
        final double venusDecl = ac.getVenusDecl();

        final double marsGHA = ac.getMarsGHA();
        final double marsDecl = ac.getMarsDecl();

        final double jupiterGHA = ac.getJupiterGHA();
        final double jupiterDecl = ac.getJupiterDecl();

        // Star spica = Star.getStar("Spica"); // Case sensitive name
        ac.starPos("Spica");
        final double spicaSHA = ac.getStarSHA("Spica");
        final double spicaGHA = ac.getStarGHA("Spica");
        final double spicaDEC = ac.getStarDec("Spica");

        ac.starPos("Denebola");
        final double denebolaSHA = ac.getStarSHA("Denebola");
        final double denebolaGHA = ac.getStarGHA("Denebola");
        final double denebolaDEC = ac.getStarDec("Denebola");

        // Sun
        final PlanDesSommetsPlayground.ConeDefinition sunCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                sunGHA,
                sunDecl,
                "the Sun");

        // Moon
        final PlanDesSommetsPlayground.ConeDefinition moonCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                moonGHA,
                moonDecl,
                "the Moon");

        // Venus
        final PlanDesSommetsPlayground.ConeDefinition venusCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                venusGHA,
                venusDecl,
                "Venus");

        // Mars
        final PlanDesSommetsPlayground.ConeDefinition marsCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                marsGHA,
                marsDecl,
                "Mars");

        // Jupiter
        final PlanDesSommetsPlayground.ConeDefinition jupiterCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                jupiterGHA,
                jupiterDecl,
                "Jupiter");

        // Spica
        final PlanDesSommetsPlayground.ConeDefinition spicaCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                spicaGHA,
                spicaDEC,
                "Spica");

        // Denebola
        final PlanDesSommetsPlayground.ConeDefinition denebolaCone = PlanDesSommetsPlayground.calculateCone(
                ac,
                latitude,
                longitude,
                2025,
                7,
                19,
                12,
                58,
                25,
                denebolaGHA,
                denebolaDEC,
                "Denebola");

        boolean verbose = "true".equals(System.getProperty("verbose"));

        spitOut(sunCone, verbose);
        spitOut(moonCone, verbose);
        spitOut(venusCone, verbose);
        spitOut(marsCone, verbose);
        spitOut(jupiterCone, verbose);
        spitOut(spicaCone, verbose);
        spitOut(denebolaCone, verbose);

    }
}