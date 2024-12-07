<?php

$RISING = 1;
$FALLING = -1;

class TimedValue { // implements Comparable<TimedValue>
    private $cal; // DateTime
    private $value;
    private $coeff = 0; // Coeff in Brest, for HW. Default 0 (ie not set)
    private $type = "";

    private $epoch;
    private $unit;
    private $formattedDate;

    function __construct(string $type, DateTime $cal, float $d) {
        $this->type = $type;
        $this->cal = $cal;
        $this->epoch = $cal->getTimestamp() * 1000; // cal.getTimeInMillis();
        $this->value = $d;
    }

    public function unit(string $unit) : TimedValue {
        $this->unit = $unit;
        return $this;
    }

    public function formattedDate(string $formattedDate) : TimedValue {
        $this->formattedDate = $formattedDate;
        return $this;
    }

    public function setUnit(string $unit) : void {
        $this->unit = $unit;
    }

    public function setFormattedDate(string $formattedDate) : void {
        $this->formattedDate = $formattedDate;
    }

    // See usort, https://www.geeksforgeeks.org/sort-array-of-objects-by-object-fields-in-php/
    public function compareTo(TimedValue $tv) : int {
        return ($this->cal > $tv->getCalendar());  // TODO Check this, is it needed ?
    }

    public function getCalendar() : DateTime {
        return $this->cal;
    }

    public function getValue() : float {
        return $this->value;
    }

    public function getCoeff() : int {
        return $this->coeff;
    }

    public function setCoeff(int $coeff) : void {
        $this->coeff = $coeff;
    }

    public function getType() : string {
        return $this->type;
    }

    public function getCal() : DateTime {
        return $this->cal;
    }

    public function getEpoch() : int {
        return $this->epoch;
    }

    public function getUnit() : string {
        return $this->unit;
    }

    public function getFormattedDate() : string {
        return $this->formattedDate;
    }
}

// To sort the list of TimedValues
function comparator(TimedValue $object1, TimedValue $object2) {
    if ($object1->getCalendar() > $object2->getCalendar()) {
        return 1;
    } else if ($object1->getCalendar() < $object2->getCalendar()) {
        return -1;
    } else {
        return 0;
    }
    // return $object1->getCalendar() > $object2->getCalendar();
}

$NS = 1;
$EW = 2;

function decToSex($value, $type) {
  global $NS, $EW;

  $absValue = abs($value);
  $intValue = floor($absValue);
  $dec = $absValue - $intValue;
  $dec *= 60;

  $formatted = $intValue . "&deg;" . number_format($dec, 2) . "'";
  $sign = "N";
  if ($type == $EW) {
    $sign = "E";
  }
  if ($value < 0) {
    if ($type == $NS) {
      $sign = "S";
    } else {
      $sign = "W";
    }
  }
  return $sign . " " . $formatted;
}

class TideUtilities {
    public static $FEET_2_METERS = 0.30480061; // US feet to meters
	public static $COEFF_FOR_EPOCH = 0.017453292519943289;

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

        $secNow = strtotime($when); // Same as $when "2024-11-28 12:34:56");
        // $secNow = strtotime(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, $month, $day, $hours, $minutes, $seconds)); // Same as $when "2024-11-28 12:34:56");
        $secJan1st = strtotime(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, 1, 1, 0, 0, 0)); // "2024-01-01 00:00:00");

        $nbSecSinceJan1st = ($secNow - $secJan1st);

        //  long nbSecSinceJan1st = (d.getTime().getTime() - jan1st.getTime().getTime() ) / 1_000L;
        //  System.out.println(" ----- NbSec for " + d.getTime().toString() + " = " + nbSecSinceJan1st);
		$timeOffset = $nbSecSinceJan1st * 0.00027777777777777778; // aka divided by 3600. That one seems OK.
		if (false) {
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

    // Calculate min/max, for the graph
    public static function getMinMaxWH(TideStation $ts, array $constSpeed, string $when) : array {
		$minMax = array("min" => 0, "max" => 0); // Init keyed array
		if ($ts != null) {

            $calcDate = date_parse($when); // Returns an array
            $year = (int)$calcDate["year"];
            $month = (int)$calcDate["month"];
            $day = (int)$calcDate["day"];
            $hours = (int)$calcDate["hour"]; 
            $minutes = (int)$calcDate["minute"];
            $seconds = (float)$calcDate["second"];
    

            // Calc Jan 1st of the current year
            $jan1st = date_create(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, 1, 1, 0, 0, 0)); // "2024-01-01 00:00:00");
			// 31 Dec, At noon
            $dec31st = date_create(sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, 12, 31, 12, 0, 0)); // "2024-12-31 12:00:00");
			$max = -100000.0;
			$min = 100000.0;;
			$date = $jan1st;
			while ($date < $dec31st) {
				$d = self::getWaterHeight($ts, $constSpeed, date_format($date, "Y-m-d H:i:s")); // (date, jan1st, ts, constSpeed);
                if (false) {
                    echo("-- For MinMax: Height at " . $ts->getFullName() . " at " . date_format($date, "Y-m-d H:i:s") . " = " . $d . ".<br/>" . PHP_EOL);
                }
				$max = max($max, $d);
				$min = min($min, $d);

				// date.add(Calendar.HOUR, 2); // date = new Date(date.getTime() + (7200 * 1000)); // Plus 2 hours
                $date = date_add($date, DateInterval::createFromDateString('2 hours'));
			}
			//  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
			$minMax["min"] = $min;
			$minMax["max"] = $max;
		}
		return $minMax;
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

    public static function getTideTableForOneDay(TideStation $ts, 
                                                 array $constSpeed, 
                                                 int $year, 
                                                 int $month, 
                                                 int $day, 
                                                 ?string $timeZone2Use) : array { // List<TimedValue>
		$low1 = null;
		$low2 = null;
		$high1 = null;
		$high2 = null;
		$low1Cal = null;
		$low2Cal = null;
		$high1Cal = null;
		$high2Cal = null;
		$slackList = array(); // List<TimedValue> 
		$trend = 0;

		$previousWH = null;

		for ($h = 0; $h < 24; $h++) {
			for ($m = 0; $m < 60; $m++) {
                $strDate = sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, $month, $day, $h, $m, 0);
                // $secNow = strtotime($strDate);
                $tz = new DateTimeZone($timeZone2Use != null ? $timeZone2Use: $ts->getTimeZone());
                $dateTime = DateTime::createFromFormat("Y-m-d H:i:s", $strDate, $tz); // String to DateTime
                if (false) {
                    echo("Managing tide data for $strDate (" . $dateTime->format('Y-m-d H:i:s e') . ") <br/>" . PHP_EOL);
                }
				$wh = 0;
				try {
					$wh = TideUtilities::getWaterHeight($ts, $constSpeed, $strDate);
                    if (false) {
                        echo("Water height in " . $ts->getFullName() . " at " . $dateTime->format('Y-m-d H:i:s e') . " is " . sprintf("%.02f", $wh) . " " . $ts->getUnit() . "<br/>" . PHP_EOL);
                    }
                } catch (Throwable $ex) {
                    throw $ex;
                }
                $applyTimeZone = true; // Set to true for local times.
				if ($previousWH != null) {
					if ($ts->isCurrentStation()) {
						if (($previousWH > 0 && $wh <= 0) || ($previousWH < 0 && $wh >= 0)) {
                            array_push($slackList, new TimedValue("Slack", $dateTime, 0)); 
						}
					}
					if ($trend == 0) {
						if ($previousWH > $wh) {
							$trend = $GLOBALS['FALLING'];
						} else if ($previousWH < $wh) {
							$trend = $GLOBALS['RISING'];
						}
					} else {
						switch ($trend) {
							case $GLOBALS['RISING']:
								if ($previousWH > $wh) { // Was going up, now going down
									$prev = $dateTime;
                                    date_sub($prev, date_interval_create_from_date_string("1 minute"));
									// prev.add(Calendar.MINUTE, -1);
									if ($high1 == null) {
										$high1 = $previousWH;
										date_sub($dateTime, date_interval_create_from_date_string("1 minute"));
										$high1Cal = $dateTime;
                                        if ($applyTimeZone) {
                                            $high1Cal->setTimeZone(new DateTimeZone($ts->getTimeZone()));
                                        }
                                        if (false) {
                                            echo("--> Now going down. Water height in " . $ts->getFullName() . " at " . $dateTime->format('Y-m-d H:i:s e') . " is " . sprintf("%.02f", $wh) . " " . $ts->getUnit() . "<br/>" . PHP_EOL);
                                        }                    
									} else {
										$high2 = $previousWH;
										date_sub($dateTime, date_interval_create_from_date_string("1 minute"));
										$high2Cal = $dateTime;
                                        if ($applyTimeZone) {
                                            $high2Cal->setTimeZone(new DateTimeZone($ts->getTimeZone()));
                                        }
									}
									$trend = $GLOBALS['FALLING']; // Now falling
								}
								break;
							case $GLOBALS['FALLING']:
								if ($previousWH < $wh) { // Was going down, now going up
									$prev = $dateTime;
                                    date_sub($prev, date_interval_create_from_date_string("1 minute"));
									if ($low1 == null) {
										$low1 = $previousWH;
                                        if (false) {
                                            echo($dateTime->format('Y-m-d H:i:s') . " - 1 minute is...<br/>" . PHP_EOL);
                                        }                        
										date_sub($dateTime, date_interval_create_from_date_string("1 minute"));
										$low1Cal = $dateTime;
                                        if ($applyTimeZone) {
                                            $low1Cal->setTimeZone(new DateTimeZone($ts->getTimeZone()));
                                        }
                                        if (false) {
                                            echo($low1Cal->format('Y-m-d H:i:s') . " !<br/>" . PHP_EOL);
                                        }                        
									} else {
										$low2 = $previousWH;
										date_sub($dateTime, date_interval_create_from_date_string("1 minute"));
										$low2Cal = $dateTime;
                                        if ($applyTimeZone) {
                                            $low2Cal->setTimeZone(new DateTimeZone($ts->getTimeZone()));
                                        }
									}
									$trend = $GLOBALS['RISING']; // Now rising
								}
								break;
						}
					}
				}
				$previousWH = $wh;
			}
		}
        $timeList = array();
		if ($low1Cal != null) {
            $tv = new TimedValue("LW", $low1Cal, $low1);
            $tv->setUnit($ts->getDisplayUnit());
            $tv->setFormattedDate($low1Cal->format('Y-m-d H:i:s e'));
            array_push($timeList, $tv); 
		}
		if ($low2Cal != null) {
            $tv = new TimedValue("LW", $low2Cal, $low2);
            $tv->setUnit($ts->getDisplayUnit());
            $tv->setFormattedDate($low2Cal->format('Y-m-d H:i:s e'));
            array_push($timeList, $tv); 
		}
		if ($high1Cal != null) {
            $tv = new TimedValue("HW", $high1Cal, $high1);
            $tv->setUnit($ts->getDisplayUnit());
            $tv->setFormattedDate($high1Cal->format('Y-m-d H:i:s e'));
            array_push($timeList, $tv); 
		}
		if ($high2Cal != null) {
            $tv = new TimedValue("HW", $high2Cal, $high2);
            $tv->setUnit($ts->getDisplayUnit());
            $tv->setFormattedDate($high2Cal->format('Y-m-d H:i:s e'));
            array_push($timeList, $tv); 
		}

        // TODO Current stations
		// if (ts.isCurrentStation() && slackList != null && slackList.size() > 0) {
		// 	slackList.stream().forEach(timeList::add);
		// }

        usort($timeList, 'comparator'); // Sort the list on times
		return $timeList;
	}

    /**
     * $brestOneDay is the TideTable for Brest at the given date.
     * It must have been computed befgore invoking this one
     */
    public static function getCoeffInBrest(TideStation $ts, array $brestOneDay) : array {
		$coeffs = array();
		// assert ts.getFullName().equals("Brest%2C%20France");
		$U = 0.032429906542056; // Hard coded
		$baseHeight = $ts->getBaseHeight(); // 4.02 for Brest

        for ($i=0; $i<count($brestOneDay); $i++) {
            $tv = $brestOneDay[$i];
            if ($tv->getType() == "HW") { 
                $hwValue = $tv->getValue();
                $coeff = ($hwValue - $baseHeight) / $U;
                array_push($coeffs, round($coeff));
            }
        }
		return $coeffs;
	}

}