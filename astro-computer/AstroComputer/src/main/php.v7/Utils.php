<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */

class Utils {

	public static function sind(float $x) : float {
		return sin(deg2rad($x));
	}

	public static function cosd(float $x) : float {
		return cos(deg2rad($x));
	}

	public static function tand(float $x) : float {
		return tan(deg2rad($x));
	}

	public static function trunc(float $x) : float {
		return 360 * ($x / 360 - floor($x / 360));
	}

	public static function trunc2(float $x) : float {
		return (2 * M_PI) * ($x / (2 * M_PI) - floor($x / (2 * M_PI)));
	}

	public static function cost(float $x) : float {
		return cos(Utils::trunc2($x));
	}

	public static $NONE = 0;
	public static $NS = 1;
	public static $EW = 2;

	public static $LEADING_SIGN = 0;
	public static $TRAILING_SIGN = 1;

	public static function decToSex(float $v, int $displayType=0, int $signPosition=0, bool $truncMinute=false) : string {
		$s = "";
		$absVal = abs($v);
		$intValue = floor($absVal);
		$dec = $absVal - $intValue;
		$i = (int) $intValue;
		$dec *= 60; // "%02d:%02d:%02.02f"
		$df = ($truncMinute ? "%02f" : "%1\$.2f"); // "%02.02f");
		$s = sprintf("%d", $i) . "&deg;" . sprintf($df, $dec) . "'";
		if ($v < 0.0) {
			switch ($displayType) {
				case self::$NONE:
					$s = "-" . $s;
					break;
				case self::$NS:
					$s = ($signPosition == self::$TRAILING_SIGN ? $s . "S" : "S " . str_pad($s, 13, " ", STR_PAD_LEFT));
					break;
				case self::$EW:
					$s = ($signPosition == self::$TRAILING_SIGN ? $s . "W" : "W " . str_pad($s, 14, " ", STR_PAD_LEFT));
					break;
			}
		} else {
			switch ($displayType) {
				case self::$NONE:
					$s = " " . $s;
					break;
				case self::$NS:
					$s = ($signPosition == self::$TRAILING_SIGN ? $s . "N" : "N " . str_pad($s, 13, " ", STR_PAD_LEFT));
					break;
				case self::$EW:
					$s = ($signPosition == self::$TRAILING_SIGN ? $s . "E" : "E " + str_pad($s, 14, " ", STR_PAD_LEFT));
					break;
			}
		}
		return $s;
	}


}