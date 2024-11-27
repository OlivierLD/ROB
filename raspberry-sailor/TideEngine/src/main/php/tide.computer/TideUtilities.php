<?php

class TideUtilities {
    public static $FEET_2_METERS = 0.30480061; // US feet to meters
	public static $COEFF_FOR_EPOCH = 0.017453292519943289;

    public static function startsWith (string $string, string $startString) : bool { 
        $len = strlen($startString); 
        return (substr($string, 0, $len) === $startString); 
    } 
}