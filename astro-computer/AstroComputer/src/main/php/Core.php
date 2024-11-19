<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */
declare(strict_types=1);

class Core {

	private static $VERBOSE = false;

	/**
	 * @param context
	 * @param year
	 * @param month  1 - Jan, 2 - Feb, etc.
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @param deltaT
	 * @return
	 */
	public static function julianDate(ContextV2 $context, int $year, int $month, int $day, int $hour, int $minute, float $second, float $deltaT) : void {
		// var year, month, day, hour, minute, second, $context->dayfraction, ly=0;
		$context->dayfraction = ((float) $hour + ((float) $minute / 60) + ((float) $second / 3600)) / 24;
		// Calculating Julian date, century, and millennium
		// Julian dacontext.TE (UT1)
		if ($month <= 2) {
			$year -= 1;
			$month += 12;
		}
		$A = floor($year / 100);
		$B = 2 - $A + floor((float)$A / 4);
		$context->JD0h = floor(365.25 * ($year + 4716)) + floor(30.6001 * ($month + 1)) + $day + $B - 1524.5;
		$context->JD = $context->JD0h + $context->dayfraction;

		// Julian centuries (UT1) from 2000 January 0.5
		$context->T = ($context->JD - 2451545) / 36525;
		$context->T2 = $context->T * $context->T;
		$context->T3 = $context->T * $context->T2;
		$context->T4 = $context->T * $context->T3;
		$context->T5 = $context->T * $context->T4;

		// Julian ephemeris dacontext.TE (TDT)
		$context->JDE = $context->JD + $deltaT / 86400;

		// Julian centuries (TDT) from 2000 January 0.5
		$context->TE = ($context->JDE - 2451545) / 36525;
		$context->TE2 = $context->TE * $context->TE;
		$context->TE3 = $context->TE * $context->TE2;
		$context->TE4 = $context->TE * $context->TE3;
		$context->TE5 = $context->TE * $context->TE4;

		// Julian millenniums (TDT) from 2000 January 0.5
		$context->Tau = 0.1 * $context->TE;
		$context->Tau2 = $context->Tau * $context->Tau;
		$context->Tau3 = $context->Tau * $context->Tau2;
		$context->Tau4 = $context->Tau * $context->Tau3;
		$context->Tau5 = $context->Tau * $context->Tau4;
	}

	//GHA Aries, GAST, GMST, equation of the equinoxes

	public static function aries(ContextV2 $context) : void {
		// Mean GHA Aries
		$GHAAmean = Utils::trunc(280.46061837 + 360.98564736629 * ($context->JD - 2451545) + 0.000387933 * $context->T2 - $context->T3 / 38710000);

		// GMST
//  SidTm = OutSidTime(GHAAmean);

		//True GHA Aries
		$context->GHAAtrue = Utils::trunc($GHAAmean + $context->delta_psi * Utils::cosd($context->eps));

		// GAST
//  SidTa = OutSidTime(GHAAtrue);

		// Equation of the equinoxes
		$EoE = 240 * $context->delta_psi * Utils::cosd($context->eps);
		$EoEout = (string)(round(1000 * $EoE) / 1000);
		$EoEout = " " . $EoEout . "s";
		// TODO Store this somewhere ?
	}


	// Calculations for the Sun
	public static function sun(ContextV2 $context) : void {
		// Mean longitude of the Sun
		$context->Lsun_mean = Utils::trunc(280.4664567 + 360007.6982779 * $context->Tau + 0.03032028 * $context->Tau2 + $context->Tau3 / 49931 - $context->Tau4 / 15299 - $context->Tau5 / 1988000);

		// Heliocentric longitude of the Earth
		// $context->Le = Earth.lEarth($context->Tau);
		try {
			$context->Le = Earth::lEarth($context->Tau);     // Refers to Earth...
		} catch (Throwable $e) {
			if (Core::$VERBOSE) {
            	echo "[ Captured Throwable for Core, Earth::lEarth : " . $e->getMessage() . "] " . PHP_EOL;
			}
			$context->Le =  0;
		}

		// Geocentric longitude of the Sun
		$context->Lsun_true = Utils::trunc($context->Le + 180 - 0.000025);

		// Heliocentric latitude of Earth
		try {
			$context->Be = Earth::bEarth($context->Tau);
		} catch (Throwable $e) {
			if (Core::$VERBOSE) {
            	echo "[ Captured Throwable for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
			}
			$context->Be =  0;
		}

		// Geocentric latitude of the Sun
		$context->beta = Utils::trunc(-$context->Be);

		// Corrections
		$Lsun_prime = Utils::trunc($context->Le + 180 - 1.397 * $context->TE - 0.00031 * $context->TE2);

		$context->beta = $context->beta + 0.000011 * (Utils::cosd($Lsun_prime) - Utils::sind($Lsun_prime));

		// Distance Earth-Sun
		try {
			$context->Re = Earth::rEarth($context->Tau);     // Refers to Earth...
		} catch (Throwable $e) {
			if (Core::$VERBOSE) {
            	echo "[ Captured Throwable for Core, Earth::rEarth : " . $e->getMessage() . "] " . PHP_EOL;
			}
			$context->Re =  10; // Duh... Temporary.
		}
		$context->dES = 149597870.691 * $context->Re;

		// Apparent longitude of the Sun
		$context->lambda_sun = Utils::trunc($context->Lsun_true + $context->delta_psi - 0.005691611 / $context->Re);

		// Right ascension of the Sun, apparent
		// Math.toDegrees(Utils.trunc2(Math.atan2((Utils.sind(context.lambda_sun) * Utils.cosd(context.eps) - Utils.tand(context.beta) * Utils.sind(context.eps)), Utils.cosd(context.lambda_sun))));
		$context->RAsun = rad2deg(Utils::trunc2(atan2((Utils::sind($context->lambda_sun) * Utils::cosd($context->eps) - Utils::tand($context->beta) * Utils::sind($context->eps)), Utils::cosd($context->lambda_sun))));

		// Declination of the Sun, apparent
		$context->DECsun = rad2deg(asin(Utils::sind($context->beta) * Utils::cosd($context->eps) + Utils::cosd($context->beta) * Utils::sind($context->eps) * Utils::sind($context->lambda_sun)));

		// GHA of the Sun
		$context->GHAsun = Utils::trunc($context->GHAAtrue - $context->RAsun);

		// Semidiameter of the Sun
		$context->SDsun = 959.63 / $context->Re;

		// Horizontal parallax of the Sun
		$context->HPsun = 8.794 / $context->Re;

		//Equation of time
		//EOT = 4*(Lsun_mean-0.0057183-0.0008-RAsun+delta_psi*cosd(eps));
		$context->EoT = 4 * $context->GHAsun + 720 - 1440 * $context->dayfraction;

		if (Core::$VERBOSE) {
			echo ("{ \"mess\": \"");
			echo ("EoT : " . $context->EoT . ", for GHASun:" . $context->GHAsun . ", dayFraction:" . $context->dayfraction . " ");
			echo ("GHAATrue: " . $context->GHAAtrue . ", RASun: " .  $context->RAsun . " ");
			echo ("lambda_sun: " . $context->lambda_sun . ", eps: " . $context->eps . ", beta: " . $context->beta . " ");
			echo ("\" } ,");
		}

		if ($context->EoT > 20) {
			$context->EoT -= 1440;
		}
		if ($context->EoT < -20) {
			$context->EoT += 1440;
		}
	}

	public static function polaris(ContextV2 $context) : void {
		// Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
		$RApol0 = 37.95293333;
		$DECpol0 = 89.26408889;

		// Proper motion per year
		$dRApol = 2.98155 / 3600;
		$dDECpol = -0.0152 / 3600;

		// Equatorial coordinates at Julian Dacontext.TE T (mean equinox and equator 2000.0)
		$RApol1 = $RApol0 + 100 * $context->TE * $dRApol;
		$DECpol1 = $DECpol0 + 100 * $context->TE * $dDECpol;

		// Mean obliquity of ecliptic at 2000.0 in degrees
		// $eps0_2000 = 23.439291111;

		// Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
		$lambdapol1 = atan2((Utils::sind($RApol1) * Utils::cosd(ContextV2::$EPS0_2000) + Utils::tand($DECpol1) * Utils::sind(ContextV2::$EPS0_2000)), Utils::cosd($RApol1));
		$betapol1 = asin(Utils::sind($DECpol1) * Utils::cosd(ContextV2::$EPS0_2000) - Utils::cosd($DECpol1) * Utils::sind(ContextV2::$EPS0_2000) * Utils::sind($RApol1));

		// Precession
		$eta = deg2rad(47.0029 * $context->TE - 0.03302 * $context->TE2 + 0.00006 * $context->TE3) / 3600;
		$PI0 = deg2rad(174.876384 - (869.8089 * $context->TE + 0.03536 * $context->TE2) / 3600);
		$p0 = deg2rad(5029.0966 * $context->TE + 1.11113 * $context->TE2 - 0.0000006 * $context->TE3) / 3600;
		$A1 = cos($eta) * cos($betapol1) * sin($PI0 - $lambdapol1) - sin($eta) * sin($betapol1);
		$B1 = cos($betapol1) * cos($PI0 - $lambdapol1);
		$C1 = cos($eta) * sin($betapol1) + sin($eta) * cos($betapol1) * sin($PI0 - $lambdapol1);
		$lambdapol2 = $p0 + $PI0 - atan2($A1, $B1);
		$betapol2 = asin($C1);

		// Nutation in longitude
		$lambdapol2 += deg2rad($context->delta_psi);

		// Aberration
		$dlambdapol = ($context->e * $context->kappa * cos($context->pi0 - $lambdapol2) - $context->kappa * cos(deg2rad($context->Lsun_true) - $lambdapol2)) / cos($betapol2);
		$dbetapol = -$context->kappa * sin($betapol2) * (sin(deg2rad($context->Lsun_true) - $lambdapol2) - $context->e * sin($context->pi0 - $lambdapol2));

		$lambdapol2 += $dlambdapol;
		$betapol2 += $dbetapol;

		// Transformation back to equatorial coordinates in radians
		$RApol2 = atan2((sin($lambdapol2) * Utils::cosd($context->eps) - tan($betapol2) * Utils::sind($context->eps)), cos($lambdapol2));
		$DECpol2 = asin(sin($betapol2) * Utils::cosd($context->eps) + cos($betapol2) * Utils::sind($context->eps) * sin($lambdapol2));

		// Finals
		$context->GHApol = $context->GHAAtrue - rad2deg($RApol2);
		$context->GHApol = Utils::trunc($context->GHApol);
		$context->RApol = rad2deg($RApol2);
		$context->DECpol = rad2deg($DECpol2);
	}

	public static function starPos(ContextV2 $context, string $starName) : void {
		$star = Star::getStar($starName);
		if ($star != null) {
			// Read catalog
			$RAstar0 = 15 * $star->getRa();
			$DECstar0 = $star->getDec();
			$dRAstar = 15 * $star->getDeltaRa() / 3600;
			$dDECstar = $star->getDeltaDec() / 3600;
			$par = $star->getPar() / 3600;

			// Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
			$RAstar1 = $RAstar0 + $context->TE * $dRAstar;
			$DECstar1 = $DECstar0 + $context->TE * $dDECstar;

			// Mean obliquity of ecliptic at 2000.0 in degrees
//    $eps0_2000 = 23.439291111;

			// Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
			$lambdastar1 = atan2((Utils::sind($RAstar1) * Utils::cosd($context::EPS0_2000) + Utils::tand($DECstar1) * Utils::sind($context::EPS0_2000)), Utils::cosd($RAstar1));
			$betastar1 = asin(Utils::sind($DECstar1) * Utils::cosd($context::EPS0_2000) - Utils::cosd($DECstar1) * Utils::sind($context::EPS0_2000) * Utils::sind($RAstar1));

			//Precession
			$eta = deg2rad(47.0029 * $context->TE - 0.03302 * $context->TE2 + 0.00006 * $context->TE3) / 3600;
			$PI0 = deg2rad(174.876384 - (869.8089 * $context->TE + 0.03536 * $context->TE2) / 3600);
			$p0 = deg2rad(5029.0966 * $context->TE + 1.11113 * $context->TE2 - 0.0000006 * $context->TE3) / 3600;
			$A1 = cos($eta) * cos($betastar1) * sin($PI0 - $lambdastar1) - sin($eta) * sin($betastar1);
			$B1 = cos($betastar1) * cos($PI0 - $lambdastar1);
			$C1 = cos($eta) * sin($betastar1) + sin($eta) * cos($betastar1) * sin($PI0 - $lambdastar1);
			$lambdastar2 = $p0 + $PI0 - atan2($A1, $B1);
			$betastar2 = asin($C1);

			//Annual parallax
			$par_lambda = deg2rad($par * sin(deg2rad($context->Lsun_true) - $lambdastar2) / cos($betastar2));
			$par_beta = -deg2rad($par * sin($betastar2) * cos(deg2rad($context->Lsun_true) - $lambdastar2));

			$lambdastar2 += $par_lambda;
			$betastar2 += $par_beta;

			// Nutation in longitude
			$lambdastar2 += deg2rad($context->delta_psi);

			// Aberration
			//    $kappa = deg2rad(20.49552) / 3600;
			//    $pi0 = deg2rad(102.93735 + 1.71953 * $context->TE + 0.00046 * $context->TE2);
			//    $e = 0.016708617 - 0.000042037 * $context->TE - 0.0000001236 * $context->TE2;

			$dlambdastar = ($context->e * $context->kappa * cos($context->pi0 - $lambdastar2) - $context->kappa * cos(deg2rad($context->Lsun_true) - $lambdastar2)) / cos($betastar2);
			$dbetastar = -$context->kappa * sin($betastar2) * (sin(deg2rad($context->Lsun_true) - $lambdastar2) - $context->e * sin($context->pi0 - $lambdastar2));

			$lambdastar2 += $dlambdastar;
			$betastar2 += $dbetastar;

			// Transformation back to equatorial coordinates in radians
			$RAstar2 = atan2((sin($lambdastar2) * Utils::cosd($context->eps) - tan($betastar2) * Utils::sind($context->eps)), cos($lambdastar2));
			$DECstar2 = asin(sin($betastar2) * Utils::cosd($context->eps) + cos($betastar2) * Utils::sind($context->eps) * sin($lambdastar2));

			//Lunar distance of star
			$context->starMoonDist = rad2deg(Math.acos(Utils::sind($context->DECmoon) * sin($DECstar2) + Utils::cosd($context->DECmoon) * cos($DECstar2) * Utils::cosd($context->RAmoon - rad2deg($RAstar2))));

			// Finals
			$context->GHAstar = Utils::trunc($context->GHAAtrue - rad2deg($RAstar2));
			$context->SHAstar = Utils::trunc(360 - rad2deg($RAstar2));
			$context->DECstar = rad2deg($DECstar2);
		} else
			echo(starName + " not found in the catalog...");
	}

	public static function moonPhase(ContextV2 $context) : string {
		$quarter = "";
		$x = $context->lambdaMapp - $context->lambda_sun;
		$x = Utils::trunc($x);
		$x = round(10 * $x) / 10;
		if ($x == 0)
			$quarter = " New";
		if ($x > 0 && $x < 90)
			$quarter = " +cre";
		if ($x == 90)
			$quarter = " FQ";
		if ($x > 90 && $x < 180)
			$quarter = " +gib";
		if ($x == 180)
			$quarter = " Full";
		if ($x > 180 && $x < 270)
			$quarter = " -gib";
		if ($x == 270)
			$quarter = " LQ";
		if ($x > 270 && $x < 360)
			$quarter = " -cre";
		return $quarter;
	}

	public static function weekDay(ContextV2 $context) : int {
		return (int) (($context->JD0h + 1.5) - 7 * floor(($context->JD0h + 1.5) / 7));
	}
}