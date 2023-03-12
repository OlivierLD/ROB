package blind;

import gribprocessing.utils.BlindRouting;

public class BlindRoutingTest {

    public static void main(String... args) {
        try {
            BlindRouting.main("--from-lat", "47.558667", // 47°33.52'N / 4°43.17'W
                    "--from-lng", "-4.719500",
                    "--to-lat", "43.219667",                    // 43°13.18'N / 64°42.64'W
                    "--to-lng", "-64.710667",
                    "--start-time", "2023-03-12T06:00:00",
                    "--time-interval", "12",
                    "--grib-file", "./samples/NorthAtlantic_2023_03_12_15_58_13_CET.grb",
                    "--polar-file", "./samples/CheoyLee42.polar-coeff",
                    "--output-type", "JSON",
                    "--speed-coeff", "0.75",
                    "--limit-twa", "60",
                    "--avoid-land", "true",
                    "--verbose", "false");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
