<?php

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

}