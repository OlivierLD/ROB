package utils;

import nmea.utils.NMEAUtils;

public class trueWindCalculation {

    public static void main(String... args) {

        double awsCoeff = 1.0;
        double awaOffset = 0.0;
        double hdgOffset = 0.0;

        // double bsp = 6.6;
        double aws = 13.0;
        double awa = 45d;
        double sog = 6.8;
        double cog = 196d;
        double hdg = 241d;

        double[] tw = NMEAUtils.calculateTWwithGPS(aws, awsCoeff, awa, awaOffset, hdg, hdgOffset, sog, cog);

        // Like 72, 14.x, 313.x
        System.out.printf("TWA: %f, TWS: %f, TWD: %f\n", tw[0], tw[1], tw[2]);

        aws = 12.8;
        awa = -45.0;
        sog = 4.4;
        cog = 308;
        hdg = 305;
        tw = NMEAUtils.calculateTWwithGPS(aws, awsCoeff, awa, awaOffset, hdg, hdgOffset, sog, cog);
        // Like -63, 10.x, 241.x
        System.out.printf("TWA: %f, TWS: %f, TWD: %f\n", tw[0], tw[1], tw[2]);

        aws = 11.4;
        awa = 336;
        sog = 4.1;
        cog = 286;
        hdg = 300.8515783113226;
        tw = NMEAUtils.calculateTWwithGPS(aws, awsCoeff, awa, awaOffset, hdg, hdgOffset, sog, cog);
        // Like -29, 7.x, 271.x
        System.out.printf("TWA: %f, TWS: %f, TWD: %f\n", tw[0], tw[1], tw[2]);
    }
}
