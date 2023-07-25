package nmea.utils.gauss;

import java.security.InvalidParameterException;

/**
 * See https://fr.wikipedia.org/wiki/Fonction_gaussienne,
 *     http://apmep.poitiers.free.fr/IMG/pdf/LA_COURBE_DE_GAUSS.pdf
 */
public class GaussCurve {

    public static double gauss(double mu, double sigma, double x) throws InvalidParameterException {
        return gauss(mu, sigma, 1.0, x);
    }
    public static double gauss(double mu, double sigma, double height, double x) throws InvalidParameterException {
        if (sigma == 0) {
            throw new InvalidParameterException("Sigma should not be equal to zero.");
        }
        return height * (1d / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp(- Math.pow((x - mu) / sigma, 2d) / 2);
    }

    public static void main(String... args) {
        double mu = 45.0;
        double sigma = 10.0;
        double height = 250;
        double y = gauss(mu, sigma, height, 45);
        System.out.printf("f(45) = %f\n", y);

        mu = 45.0;
        sigma = 15;
        height = 400.0;
        y = gauss(mu, sigma, height, 45);
        System.out.printf("f(45) = %f\n", y);
        y = gauss(mu, sigma, height, 25);
        System.out.printf("f(25) = %f\n", y);
    }
}
