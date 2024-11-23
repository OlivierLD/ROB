<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */
declare(strict_types=1);

class DMS {
    private $hours; // int
    private $minutes; // int
    private $seconds; // float

    public function getHours() : int {
        return $this->hours;
    }

    public function getMinutes() : int {
        return $this->minutes;
    }

    public function getSeconds() : float {
        return $this->seconds;
    }

    public function hours(int $hours) : DMS {
        $this->hours = $hours;
        return $this;
    }
    public function minutes(int $minutes) : DMS  {
        $this->minutes = $minutes;
        return $this;
    }
    public function seconds(float $seconds) : DMS  {
        $this->seconds = $seconds;
        return $this;
    }
}

class TimeUtil {

    private $verbose = false; // Change at will

	public static function decimalToDMS(float $decimalHours) : DMS {
		$hours = (int)floor($decimalHours);
		$min = ($decimalHours - $hours) * 60;
		$sec = ($min - floor($min)) * 60;

        $dms = new DMS();
        $dms = $dms->hours($hours);
        $dms = $dms->minutes((int)floor($min));
        $dms = $dms->seconds($sec);
        return $dms;
	}

	public static function decHoursToDMS(float $decimalHours, string $format="%02d:%02d:%02.02f") : string {
		$dms = TimeUtil::decimalToDMS($decimalHours);
		return sprintf($format, $dms->getHours(), $dms->getMinutes(), $dms->getSeconds());
	}

	public static function getGMT() {
		$dateGMT = date_create(NULL, timezone_open("Etc/UTC"));
		return $dateGMT;
	}

	private static function getY(int $year, int $month) : float {
		if ($year < -1999 || $year > 3000) {
			throw new Exception("Year must be in [-1999, 3000]");
		} else {
			return ($year + (($month - 0.5) / 12));
		}
	}

	/**
	 * Since the usual (the ones I used to used) on-line resources are not always available, obsolete,
	 * or expecting some serious revamping, here is a method to calculate deltaT out of thin air.
	 *
	 * See http://xjubier.free.fr/en/site_pages/deltaT_LeapSeconds.html
	 *
	 * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
	 * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
	 *               https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
	 *
	 * @param year from -1999 to +3000
	 * @param month in [1..12], NOT in [0..11]
	 * @return deltaT
	 */
	public static function getDeltaT(int $year, int $month) : float {
		if ($year < -1999 || $year > 3000) {
			throw new Exception("Year must be in [-1999, 3000]");
		}
		if ($month < 1 || $month > 12) {
			throw new Exception("Month must be in [1, 12]");
		}

		$deltaT;
		$y = TimeUtil::getY($year, $month);

		if ($year < -500) {
			$u = ($y - 1820) / 100;
			$deltaT = -20 + (32 * ($u * $u));
		} else if ($year < 500) {
			$u = $y / 100;
			$deltaT = 10583.6
					+ (-1014.41 * $u)
					+ (33.78311 * pow($u, 2))
					+ (-5.952053 * pow($u, 3))
					+ (-0.1798452 * pow($u, 4))
					+ (0.022174192 * pow($u, 5))
					+ (0.0090316521 * pow($u, 6));
		} else if ($year < 1600) {
			$u = ($y - 1000) / 100;
			$deltaT = 1574.2
					+ (-556.01 * $u)
					+ (71.23472 * pow($u, 2))
					+ (0.319781 * pow($u, 3))
					+ (-0.8503463 * pow($u, 4))
					+ (-0.005050998 * pow($u, 5))
					+ (0.0083572073 * pow($u, 6));
		} else if ($year < 1700) {
			$t = $y - 1600;
			$deltaT = 120
					+ (-0.9808 * $t)
					+ (-0.01532 * pow($t, 2))
					+ (pow($t, 3) / 7129);
		} else if ($year < 1800) {
			$t = $y - 1700;
			$deltaT = 8.83
					+ 0.1603 * $t
					+ (-0.0059285 * pow($t, 2))
					+ (0.00013336 * pow($t, 3))
					+ (pow($t, 4) / -1174000);
		} else if ($year < 1860) {
			$t = $y - 1800;
			$deltaT = 13.72
					+ (-0.332447 * $t)
					+ (0.0068612 * pow($t, 2))
					+ (0.0041116 * pow($t, 3))
					+ (-0.00037436 * pow($t, 4))
					+ (0.0000121272 * pow($t, 5))
					+ (-0.0000001699 * pow($t, 6))
					+ (0.000000000875 * pow($t, 7));
		} else if ($year < 1900) {
			$t = $y - 1860;
			$deltaT = 7.62 +
					(0.5737 * $t)
					+ (-0.251754 * pow($t, 2))
					+ (0.01680668 * pow($t, 3))
					+ (-0.0004473624 * pow($t, 4))
					+ (pow($t, 5) / 233174);
		} else if ($year < 1920) {
			$t = $y - 1900;
			$deltaT = -2.79
					+ (1.494119 * $t)
					+ (-0.0598939 * pow($t, 2))
					+ (0.0061966 * pow($t, 3))
					+ (-0.000197 * pow($t, 4));
		} else if ($year < 1941) {
			$t = $y - 1920;
			$deltaT = 21.20
					+ (0.84493 * $t)
					+ (-0.076100 * pow($t, 2))
					+ (0.0020936 * pow($t, 3));
		} else if ($year < 1961) {
			$t = $y - 1950;
			$deltaT = 29.07
					+ (0.407 * $t)
					+ (pow($t, 2) / -233)
					+ (pow($t, 3) / 2547);
		} else if ($year < 1986) {
			$t = y - 1975;
			$deltaT = 45.45
					+ (1.067 * $t)
					+ (pow($t, 2) / -260)
					+ (pow($t, 3) / -718);
		} else if ($year < 2005) {
			$t = $y - 2000;
			$deltaT = 63.86
					+ (0.3345 * $t)
					+ (-0.060374 * pow($t, 2))
					+ (0.0017275 * pow($t, 3))
					+ (0.000651814 * pow($t, 4))
					+ (0.00002373599 * pow($t, 5));
 	  	} else if ($year < 2050) {
			$t = $y - 2000;
			$deltaT = 62.92
					+ (0.32217 * $t)
					+ (0.005589 * pow($t, 2));
		} else if ($year < 2150) {
			$deltaT = -20
					+ (32 * pow(($y - 1820) / 100, 2))
					+ (-0.5628 * (2150 - $y));
		} else {
			$u = ($y - 1820) / 100;
			$deltaT = -20
					+ (32 * pow($u, 2));
		}

		return $deltaT;
	}

	private static $DAYS_PER_MONTH = array (
		31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
	);
	private static $JANUARY = 1;
	private static $FEBRUARY = 2;
	private static $MARCH = 3;
	private static $APRIL = 4;
	private static $MAY = 5;
	private static $JUNE = 6;
	private static $JULY = 7;
	private static $AUGUST = 8;
	private static $SEPTEMBER = 9;
	private static $OCTOBER = 10;
	private static $NOVEMBER = 11;
	private static $DECEMBER = 12;

	/**
	 * Get the number of days for a given month and year. Takes care of leap years.
	 *
	 * @param y year
	 * @param m month Jan:1, Dec:12
	 * @return the number of days in the given month.
	 */
	public static function getNbDays(int $y, int $m) : int {
		$nd = self::$DAYS_PER_MONTH[$m - 1];
		if ($m == self::$FEBRUARY) {
			$leap = false;
			if ($y % 4 == 0) { // Leap
				$leap = true;
				if ($y % 100 == 0) { // Not leap
					$leap = $y % 400 == 0; // Except if %400 = 0
				}
			}
			if ($leap) {
				$nd += 1; // 29;
			}
		}
		return $nd;
	}

}