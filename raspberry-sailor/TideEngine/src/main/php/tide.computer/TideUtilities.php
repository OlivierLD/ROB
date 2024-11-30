<?php

class TideUtilities {
    public static $FEET_2_METERS = 0.30480061; // US feet to meters
	public static $COEFF_FOR_EPOCH = 0.017453292519943289;

    public static function startsWith (string $string, string $startString) : bool { 
        $len = strlen($startString); 
        return (substr($string, 0, $len) === $startString); 
    } 

    public static $COEFF_DEFINITION = array (
		"M2" => "Principal lunar semidiurnal constituent",
		"S2" => "Principal solar semidiurnal constituent",
		"N2" => "Larger lunar elliptic semidiurnal constituent",
		"K1" => "Lunar diurnal constituent",
		"M4" => "Shallow water overtides of principal lunar constituent",
		"O1" => "Lunar diurnal constituent",
		"M6" => "Shallow water overtides of principal lunar constituent",
		"MK3" => "Shallow water terdiurnal",
		"S4" => "Shallow water overtides of principal solar constituent",
		"MN4" => "Shallow water quarter diurnal constituent",
		"NU2" => "Larger lunar evectional constituent",
		"S6" => "Shallow water overtides of principal solar constituent",
		"MU2" => "Variational constituent",
		"2N2" => "Lunar elliptical semidiurnal second",
		"OO1" => "Lunar diurnal",
		"LAM2" => "Smaller lunar evectional constituent",
		"S1" => "Solar diurnal constituent",
		"M1" => "Smaller lunar elliptic diurnal constituent",
		"J1" => "Smaller lunar elliptic diurnal constituent",
		"MM" => "Lunar monthly constituent",
		"SSA" => "Solar semiannual constituent",
		"SA" => "Solar annual constituent",
		"MSF" => "Lunisolar synodic fortnightly constituent",
		"MF" => "Lunisolar fortnightly constituent",
		"RHO" => "Larger lunar evectional diurnal constituent",
		"Q1" => "Larger lunar elliptic diurnal constituent",
		"T2" => "Larger solar elliptic constituent",
		"R2" => "Smaller solar elliptic constituent",
		"2Q1" => "Larger elliptic diurnal",
		"P1" => "Solar diurnal constituent",
		"2SM2" => "Shallow water semidiurnal constituent",
		"M3" => "Lunar terdiurnal constituent",
		"L2" => "Smaller lunar elliptic semidiurnal constituent",
		"2MK3" => "Shallow water terdiurnal constituent",
		"K2" => "Lunisolar semidiurnal constituent",
		"M8" => "Shallow water eighth diurnal constituent",
		"MS4" => "Shallow water quarter diurnal constituent"
    );

	public static $ORDERED_COEFF = [ "M2", "S2", "N2", "K1", "M4", "O1", "M6", "MK3", "S4",
                                     "MN4", "NU2", "S6", "MU2", "2N2", "OO1", "LAM2", "S1", "M1",
                                     "J1", "MM", "SSA", "SA", "MSF", "MF", "RHO", "Q1", "T2",
                                     "R2", "2Q1", "P1", "2SM2", "M3", "L2", "2MK3", "K2", "M8",
                                     "MS4" ];

    /**
     * $when: like '2024-11-28 12:34:56', LOCAL!! date.
     * See https://www.w3schools.com/php/func_date_strtotime.asp
     */                                     
    public static function getWaterHeight(TideStation $ts, array $constSpeed, string $when) : float {
		$value = 0.0;

		$stationBaseHeight = $ts->getBaseHeight();
		$stationUnit = $ts->getDisplayUnit();

        // https://www.w3schools.com/php/func_date_mktime.asp
        $calcDate = date_parse($when);
        $year = (int)$calcDate["year"];
        $month = (int)$calcDate["month"];
        $day = (int)$calcDate["day"];
        $hours = (int)$calcDate["hour"]; 
        $minutes = (int)$calcDate["minute"];
        $seconds = (float)$calcDate["second"];

        // $calcDateTime = mktime($hours, $minutes, $seconds, $month, $day, $year); // Watch the order !!
        // $jan1st = mktime(0, 0, 0, 1, 1, $year);

        $secNow = strtotime(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, $month, $day, $hours, $minutes, $seconds)); // Same as $when "2024-11-28 12:34:56");
        $secJan1st = strtotime(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, 1, 1, 0, 0, 0)); // "2024-01-01 00:00:00");

        $nbSecSinceJan1st = ($secNow - $secJan1st);

        //  long nbSecSinceJan1st = (d.getTime().getTime() - jan1st.getTime().getTime() ) / 1_000L;
        //  System.out.println(" ----- NbSec for " + d.getTime().toString() + " = " + nbSecSinceJan1st);
		$timeOffset = $nbSecSinceJan1st * 0.00027777777777777778; // aka divided by 3600. That one seems OK.
		if (true) {
            // gmdate("Y-m-d\TH:i:s\Z", $timestamp) 
            // TODO Change to local time (not Z)
            echo("Current: " . gmdate("Y-m-d\TH:i:sP", $secNow) . ", Jan 1st: " . gmdate("Y-m-d\TH:i:sP", $secJan1st) . "<br/>");
            echo("Current: " . $secNow . " s, Jan 1st: " . $secJan1st . " s<br/>");
			echo("Used TimeOffset: " . sprintf("%d", round($timeOffset)) . " hours (ie " . $nbSecSinceJan1st . " s), base height: " . $stationBaseHeight . " " . $stationUnit . "<br/>" . PHP_EOL);
		}
		$value = $ts->getBaseHeight();
		for ($i = 0; $i < count($constSpeed); $i++) {
            if (false) {
                echo("For " . $ts->getFullName() . ", Harmonics:<br/>" . PHP_EOL);
                var_dump($ts->getHarmonics());
                echo("-- TS Harmonics: --<br/>" . PHP_EOL);
                echo("ConstSpeed :<br/>" . PHP_EOL);
                var_dump($constSpeed);
                echo("-- constSpeed --<br/>" . PHP_EOL);
            }
            if (false) {
                if ($ts->getHarmonics()[$i]->getName() != $constSpeed[$i]->getName()) {
                    echo (".... Mismatch !!!<br/>" . PHP_EOL);
                }
            }
            $currentConstSpeed = $constSpeed[$ts->getHarmonics()[$i]->getName()]; // By its key.

			// value += (ts.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - ts.getHarmonics().get(i).getEpoch()));
			$value += ($ts->getHarmonics()[$i]->getAmplitude() * cos($currentConstSpeed->getCoeffValue() * $timeOffset - $ts->getHarmonics()[$i]->getEpoch()));
			if (false) {  // Verbose
				echo(sprintf("Coeff %s - Amplitude: %f, Speed Value: %f, Epoch: %f => Value: %f\n",
                             $currentConstSpeed->getCoeffName(),
                             $ts->getHarmonics()[$i]->getAmplitude(),
                             $currentConstSpeed->getCoeffValue(),
                             $ts->getHarmonics()[$i]->getEpoch(),
                             $value) . "<br/>" . PHP_EOL);   // Value !!
			}
		}
		// if ($ts->getUnit().indexOf("^2") > -1) {
        if (strpos($ts->getUnit(),  "^2") !== false) {            
			$value = ($value >= 0.0 ? sqrt($value) : -sqrt(-$value));
		}

        return $value;
    }

    public static function getMinMaxWH(TideStation $ts, array $constSpeed, string $when) : array {
        return array("min" => 0, "max" => 0);
    }

    public static function findConstSpeed(Constituents $doc, string $constName) : ConstSpeed {
        $speedMap = $doc->getConstSpeedMap();

        if (false) {
            echo ("SpeedMap:<br/>" . PHP_EOL);
            var_dump($speedMap);
            echo ("<br/>" . PHP_EOL);
        }

        $theConstSpeed = null;

        foreach ($speedMap as $name => $coeff) {
            if ($name == $constName) { // could also be $coeff->getCoeffName
                $theConstSpeed = $coeff;
                break;
            }
        }
        return $theConstSpeed;
    }
}