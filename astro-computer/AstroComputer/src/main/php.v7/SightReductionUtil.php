<?php

/**
 * Astronomical Navigation Tools.
 * <br>
 * Dead Reckoning : Estimated Altitude et Azimuth.
 * <br>
 * This is a Java Bean.
 * <br>
 * Input parameters :
 * <ul>
 * <li type="disc">GHA - Greenwich Hour Angle</li>
 * <li type="disc">Declination</li>
 * <li type="disc">Estimated Latitude</li>
 * <li type="disc">Estimated Longitude</li>
 * </ul>
 * Output data :
 * <ul>
 * <li type="disc">Estimated Altitude</li>
 * <li type="disc">Azimuth</li>
 * </ul>
 * Test with :
 * <ul>
 * <li>GHA = 321&deg;57.9</li>
 * <li>D = 13&deg;57.5 N</li>
 * <li>L = 46&deg;38 N</li>
 * <li>G  = 4&deg;06 W</li>
 * </ul>
 * Result should be
 * <ul>
 * <li type="disc">Ea 42 02</li>
 * <li type="disc">Z 119</li>
 * </ul>
 * This call only performs calculations. No user interface is provided.
 *
 * @author olivier@lediouris.net
 * @version 1.0.0
 */

class SightReductionUtil {
	private $dHe;
	private $dZ;

	private $AHG;
	private $D;
	private $L;
	private $G;

	// private final static DecimalFormat df = new DecimalFormat("##0.000");
    private static $fmt = "%1\$.3f";

	/**
	 * Constructor.
	 * Call it and then use getHe() and getZ() to retrieve result.
	 *
	 * @param dAHG Greenwich Hour Angle
	 * @param dD   Declination
	 * @param dL   Estimated Latitude
	 * @param dG   Estimated Longitude
	 */
	public function __construct(float $dAHG,
                                float $dD,
                                float $dL,
                                float $dG) {
		$this->AHG = $dAHG;
		$this->D = $dD;
		$this->L = $dL;
		$this->G = $dG;

		// echo("SightReductionUtil Constructor invoked, with $dAHG, $dD, $dL, $dG.<br/>");
	}

	public function calculate_withAHG_D(float $ahg, float $d) : void {
		setAHG($ahg);
		setD($d);
		calculate();
	}

	public function calculate_withL_G_AHG_D(float $l, float $g, float $ahg, float $d) : void {
		setL($l);
		setG($g);
		calculate_withAHG_D($ahg, $d);
	}

	/**
	 * Performs the required calculations, after the AHG, D, L and G.
	 * he and Z are after that ready to be retrieved.
	 */
	public function calculate() : void {

		// echo("SightReductionUtil: calculate, ...<br/>");
		// echo("SightReductionUtil: calculate, AHG:" . $this->AHG . "<br/>");

		$AHL = $this->AHG + $this->G;
		while ($AHL < 0.0) {
			$AHL = 360.0 + $AHL;
		}
		// Formula to solve : sin He = sin L sin D + cos L cos D cos AHL
		$sinL = sin(deg2rad($this->L));
		$sinD = sin(deg2rad($this->D));
		$cosL = cos(deg2rad($this->L));
		$cosD = cos(deg2rad($this->D));
		$cosAHL = cos(deg2rad($AHL));

		$sinHe = ($sinL * $sinD) + ($cosL * $cosD * $cosAHL);

		$this->dHe = rad2deg(asin($sinHe));

//  System.out.println("Hauteur Estimée : " + GeomUtil.decToSex(He));

		// Formula to solve : tg Z = sin P / cos L tan D - sin L cos P
		$P = ($AHL < 180.0) ? $AHL : (360.0 - $AHL);
		$sinP = sin(deg2rad($P));
		$cosP = cos(deg2rad($P));
		$tanD = tan(deg2rad($this->D));
		$tanZ = $sinP / (($cosL * $tanD) - ($sinL * $cosP));
		$Z = rad2deg(atan($tanZ));

		if ($AHL < 180.0) { // vers l'West
			if ($Z < 0.0) { // sud vers nord
				$Z = 180.0 - $Z;
			} else {         // Nord vers Sud
				$Z = 360.0 - $Z;
			}
		} else {           // vers l'Est
			if ($Z < 0.0) { // sud vers nord
				$Z = 180.0 + $Z;
//    } else {       // nord vers sud
//      Z = Z;
			}
		}
//  System.out.println("Azimuth : " + GeomUtil.decToSex(Z));
		$this->dZ = $Z;
	}

	/**
	 * Returns estimated elevation (Hauteur estim&eacute;e) after calculation.
	 * This value is decimal. Use GeomUtil.decToSex(getHe()) to read it in DegMinSec.
	 *
	 * @see calc.GeomUtil
	 */
	public function getHe() : float {
		return $this->dHe;
	}

	/**
	 * Returns Azimuth after calculation.
	 * This value is decimal. Use GeomUtil.decToSex(getZ()) to read it in DegMinSec.
	 *
	 * @see GeomUtil
	 */
	public function getZ() : float {
		return $this->dZ;
	}

	/**
	 * Set the AHG before calculation
	 *
	 * @param ahg the AHG to set
	 */
	public function setAHG(float $ahg) : void {
		$this->AHG = $ahg;
	}

	/**
	 * Set the D before calculation
	 *
	 * @param d the D to set
	 */
	public function setD(float $d) : void {
		$this->D = $d;
	}

	/**
	 * Set the L before calculation
	 *
	 * @param l the L to set
	 */
	public function setL(float $l) : void {
		$this->L = $l;
	}

	/**
	 * Set the G before calculation
	 *
	 * @param g the G to set
	 */
	public function setG($g) : void {
		$this->G = $g;
	}

	/**
	 * Corrections
	 */
	private $horizonDip = 0.0;
	private $refraction = 0.0;
	private $pa = 0.0;

	public static $UPPER_LIMB = 0;
	public static $LOWER_LIMB = 1;
	public static $NEAR_LIMB = 2;
	public static $FAR_LIMB = 3;
	public static $NO_LIMB = -1; // Stars & Planets

	public function getPa() : float {
		return $this->pa;
	}

	public function getHorizonDip() : float {
		return $this->$horizonDip;
	}

	public static function calcHorizonDip(float $eyeHeight) : float { // eyeHeight In meters
		return 1.76 * sqrt($eyeHeight);
	}

	public function getRefraction() : float {
		return $this->refraction;
	}

	public static function calcRefraction(float $alt) : float { // Works, according to the Correction Tables...
		return 0.97127 * tan(deg2rad(90.0 - $alt)) - 0.00137 * pow(tan(deg2rad(90.0 - $alt)), 3.0);
	}

//  public static $getRefraction($alt)
//  {
//    $r = 0.0D;
//    r = 1.0D / (tan(deg2rad(alt)) + (7.31D / (alt + 4.4D)));
//    r -= 0.06D * sin(deg2rad(((14.7D * r) + 13D) / 60d));
//    return r; // Result in minutes
//  }

	/**
	 * Returns the Observed Altitude of a celestial body
	 * <br>
	 * We left in stand by for now:
	 * <ul>
	 * <li type="disc">Oblate Spheroid (Earth is not a sphere)</li>
	 * <li type="disc">Barometric Correction</li>
	 * </ul>
	 *
	 * @param appAltitude The one you want to correct <b>in degrees</b>
	 * @param eyeHeight   Height of the eye above water, <b>in meters</b>
	 * @param hp          Horizontal parallax, <b>in minutes</b>
	 * @param sd          Semi diameter of the celestial body, <b>in minutes</b>
	 * @param limb        Upper or Lower limb
	 * @return the Observed Altitude
	 * @see SightReductionUtil#UPPER_LIMB
	 * @see SightReductionUtil#LOWER_LIMB
	 */
	// public static function observedAltitude(float $appAltitude,
    //                                         float $eyeHeight, // meters
    //                                         float $hp,
    //                                         float $sd,
    //                                         int $limb) : float {
	// 	return observedAltitude($appAltitude,
    //                             $eyeHeight,
    //                             $hp,
    //                             $sd,
    //                             $limb,
    //                             false);
	// }

	// public static function observedAltitude(float $appAltitude,
    //                                         float $hp,
    //                                         float $sd,
    // 	                                    bool $verbose) : float {
	// 	/*
	// 	 * With an artificial horizon.
	// 	 * No semi-diameter correction.
	// 	 * No horizon dip correction
	// 	 *
	// 	 * instrument altitude is to be divided by 2
	// 	 */
	// 	return observedAltitude($appAltitude / 2.0, 0.0, $hp, $sd, self::$NO_LIMB, true, $verbose);
	// }

	/**
	 * Correction for Instrumental to Observed Altitude
	 *
	 * @param appAltitude Instrumental Altitude
	 * @param eyeHeight   Eye Height above sea level
	 * @param hp          Horizontal Parallax
	 * @param sd          Semi Diameter
	 * @param limb        UPPER or LOWER limb
	 * @param verbose     more info
	 * @return Observed Altitude
	 */
	// public static function observedAltitude(float $appAltitude,
    //                                         float $eyeHeight, // meters
    //                                         float $hp,
    //                                         float $sd,
    //                                         int $limb,
    //                                         bool $verbose) : float {
	// 	return observedAltitude($appAltitude, $eyeHeight, $hp, $sd, $limb, false, $verbose);
	// }

	/**
	 * @param appAltitude       in degrees
	 * @param eyeHeight         in meters
	 * @param hp                Horizontal Parallax in degrees
	 * @param sd                Semi-Diameter in degrees
	 * @param limb              upper, lower, none
	 * @param artificialHorizon true/false
	 * @param verbose           true/false
     * 
	 * @return Observed Altitude in degrees
	 */
	public static function observedAltitude(float $appAltitude,
                                            float $eyeHeight,
                                            float $hp,
                                            float $sd,
                                            int $limb = 1, // self::$LOWER_LIMB,
                                            bool $artificialHorizon = false,
                                            bool $verbose = false) : float {
		$correction = 0.0;
		// Dip of horizon, in minutes
		if (!$artificialHorizon) {
			$horizonDip = getHorizonDip($eyeHeight);
			$correction -= ($horizonDip / 60.0);
		}
		if ($verbose) {
			echo("Original Altitude:" . sprintf($fmt, $appAltitude));
		}
		$observedAltitude = $appAltitude + $correction;
		if ($verbose && !$artificialHorizon) {
			echo ("-> With Hor.Dip :" . sprintf($fmt, $observedAltitude) . " (Horizon Dip for " . $eyeHeight . "m:" . sprintf($fmt, $horizonDip) . "', total correction:" . sprintf($fmt, $correction * 60) . "')");
		}
		// Refraction
		$refraction = getRefraction($observedAltitude);
		$correction -= ($refraction / 60.0);
		$observedAltitude = $appAltitude + $correction;
		if ($verbose) {
			echo("-> With Refr    :" . sprintf($fmt, $observedAltitude) . " (Refraction:" . sprintf($fmt, $refraction) . "', total correction:" . sprintf($fmt, $correction * 60) + "')");
		}
		// Barometric & temp correction - stby for now

		// Parallax
	//  $pa  = 0.0;
		$pa = getParallax($hp, $observedAltitude);
		$rpa = deg2rad($pa);
		// Earth is not a sphere...
		$ob = 0.0; // Oblate Spheroid
	  /* Stby */
		$rpa += $ob;

		$correction += ($pa);
		$observedAltitude = $appAltitude + $correction;
		if ($verbose) {
			echo("-> With Parallax:" + sprintf($fmt, $observedAltitude) . " (Parallax for hp " . ($hp * 60) . ":" . sprintf($fmt, $pa * 60) . "', total correction:" . sprintf($fmt, correction * 60) . "')");
		}
		// Semi diameter
		if ($limb == self::$LOWER_LIMB) {
			$correction += ($sd); // Lower Limb;
			if ($verbose) {
				echo("  Semi-Diameter:" . sprintf($fmt, $sd * 60) . "'");
			}
		} else if ($limb == self::$UPPER_LIMB) {
			$correction -= ($sd); // Upper Limb;
			if ($verbose) {
				echo("  Semi-Diameter:" . sprintf($fmt, -$sd * 60) . "'");
			}
		}

		$observedAltitude = $appAltitude + $correction;
		if ($verbose) {
			echo("-> With Semi-Diam:" . sprintf($fmt, $observedAltitude) . ", total correction:" . sprintf($fmt, $correction * 60) . "'");
			echo("- Total Correction:" . sprintf($fmt, $correction) + "\272, " . Utils::decToSex($correction, Utils::$NONE, Utils::$LEADING_SIGN, false));
		}
		return $observedAltitude;
	}

	/**
	 * @param hp     Horizontal Parallax (in degrees)
	 * @param obsAlt Observed Altitude (in degrees)
	 * @return parallax in degrees
	 */
	public static function getParallax($hp, $obsAlt) : float {
		return rad2deg(asin(sin(deg2rad($hp)) * cos(deg2rad($obsAlt))));
//  p += (0.0033528 * hp * (sin(deg2rad(2*latObs) * cos(Zn) * sin(deg2rad(obsAlt)) - sin(deg2rad(latObs)) * sin(deg2rad(latObs)) * cos(deg2rad(obsAlt)))));
	}

	public static function observedToApparentAltitude(float $obsAlt,
                                                      float $hp,
                                                      bool $verbose) : float {
		$parallax = getParallax($hp, $obsAlt);
		$appAlt = $obsAlt - $parallax;

		if ($verbose) {
			echo("Observed:" . sprintf($fmt, $obsAlt));
			echo(" for hp:" . sprintf($fmt, $hp) . ", parallax:" . sprintf($fmt, $parallax) . ", app. alt:" . sprintf($fmt, $appAlt));
		}
		$refraction = getRefraction($appAlt);
		$appAlt += ($refraction / 60.0);
		if ($verbose) {
			echo(" refraction:" . sprintf($fmt, $refraction / 60.0) . ", app. alt:" . sprintf($fmt, $appAlt));
		}
		return $appAlt;
	}

	/**
	 * @param hp     Horizontal Parallax in minutes
	 * @param appAlt App Altitude, in degrees
	 * @return the correcter SD, in degrees
	 */
	public static function getMoonSD(float $hp,
	                                 float $appAlt) : float {
		return (0.2725 * ($hp / 60.0)) / (1 - (sin(deg2rad($hp / 60.0)) * sin(deg2rad($appAlt))));
	}

	public static function getDistance(float $decBodyOne,
                                       float $decBodyTwo,
                                       float $ghaBodyOne,
                                       float $ghaBodyTwo) : float {
		$ld = acos((sin($decBodyOne) * sin($decBodyTwo)) + (cos($decBodyOne) * cos($decBodyTwo) * cos(abs($ghaBodyTwo - $ghaBodyOne))));
		return rad2deg($ld);
	}

	/**
	 * @param instrAltitude     Instrument Altitude in degrees
	 * @param eyeHeight         Eye height in meters
	 * @param hp                Horizontal parallax in degrees
	 * @param sd                Semi-diameter in degrees
	 * @param limb              UPPER_LIMB, LOWER_LIMB, NO_LIMB
	 * @param artificialHorizon true/false
	 * @param verbose           true/false
	 * @return Altitude correction, in degrees
	 */
	public static function getAltitudeCorrection(float $instrAltitude,
                                                 float $eyeHeight,
                                                 float $hp,
                                                 float $sd,
	                                             int $limb,
                                                 bool $artificialHorizon,
                                                 bool $verbose) : float {
		$correction = 0.0;
		if (!$artificialHorizon) {
			$horizonDip = getHorizonDip($eyeHeight);
			$correction -= ($horizonDip / 60);
		}
		if ($verbose) {
			echo("Original Altitude:" . sprintf($fmt, $instrAltitude));
		}
		$observedAltitude = $instrAltitude + $correction;
		if ($verbose && !$artificialHorizon) {
			echo("-> With Hor.Dip :" . sprintf($fmt, $observedAltitude) . " (Horizon Dip:" . sprintf($fmt, $horizonDip) . "')");
		}
		$refraction = getRefraction($observedAltitude);
		$correction -= ($refraction / 60);
		$observedAltitude = $instrAltitude + $correction;
		if ($verbose) {
			echo("-> With Refr    :" . sprintf($fmt, $observedAltitude) . " (Refraction:" . sprintf($fmt, $refraction) . "')");
		}
		$pa = getParallax($hp, $observedAltitude);
		$correction += $pa;
		$observedAltitude = $instrAltitude + $correction;
		if ($verbose) {
			echo("-> With Parallax:" . sprintf($fmt, $observedAltitude) . " (Parallax:" . sprintf($fmt, $pa * 57.295779513082323) . "')");
		}
		if ($limb == self::$LOWER_LIMB) {
			$correction += $sd;
			if ($verbose) {
				echo("  Semi-Diameter:" . sprintf($fmt, $sd * 60) . "'");
			}
		} else if ($limb == self::$UPPER_LIMB) {
			$correction -= $sd;
			if ($verbose) {
				echo("  Semi-Diameter:" . sprintf($fmt, -$sd * 60) . "'");
			}
		}
		$observedAltitude = $instrAltitude + $correction;
		if ($verbose) {
			echo("-> With Semi-Diam:" . sprintf($fmt, $observedAltitude));
			echo("- Total Correction:" . sprintf($fmt, $correction) . "\272");
		}
		return $correction;
	}

	/**
	 * All values in degrees, in and out
	 * Implementation of the Young's formula (1856)
	 * See http://www.titulosnauticos.net/astro/Chapter7.pdf
	 *
	 * @param hMoon Moon's Height (elev) (in degrees)
	 * @param appHMoon Apparent Moon's Height (corrected with index, dip, ND SD)
	 * @param hBody Body's Height (elev) (in degrees)
	 * @param appHBody Apparent Body's Height (corrected with index, dip, ND SD)
	 * @param obsDist Observer Distance (degrees)
	 * @return The lunar distance, corrected
	 */
	public static function clearLunarDistance(float $hMoon, float $appHMoon, float $hBody, float $appHBody, float $obsDist) : float {
		$cosHm_cosHb = cos(deg2rad($hMoon)) * cos(deg2rad($hBody));
		$cosHmApp_cosHbApp = cos(deg2rad($appHMoon)) * cos(deg2rad($appHBody));

		$cosDapp_cosHMappHBapp = cos(deg2rad($obsDist)) + cos(deg2rad($appHMoon + $appHBody));
		$cosHmHb = cos(deg2rad($hMoon + $hBody));

		$cosD = ($cosHm_cosHb / $cosHmApp_cosHbApp) * $cosDapp_cosHMappHBapp - $cosHmHb;

		return rad2deg(acos($cosD));
	}
}
