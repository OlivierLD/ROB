<?php

class BackEndSQLiteTideComputer {

    private static $db; // SQLite3 
    public $stationList = null;  // Array

    public function connectDB(string $location) : void {
        self::$db = new SQLite3($location);
    }

    public function closeDB() : void {
        self::$db->close();
    }

    public function getStationsData() : array {
        if ($this->stationList == null) {
            $this->stationList = $this->buildStationData();
        }
        return $this->stationList;
    }

    private function buildStationData() : array {
        if (self::$db == null) {
            throw new Exception("DB Not connected yet.");
        } else {
            $stationData = [];
			$selectStatement_01 = "select t1.name, " .
                                         "t1.latitude, " .
                                         "t1.longitude, " .
                                         "t1.tzOffset, " .
                                         "t1.tzName, " . 
                                         "t1.baseheightvalue, " .
                                         "t1.baseheightunit " .
                                   "from stations as t1";
			try {
                $results = self::$db->query($selectStatement_01);
                while ($row = $results->fetchArray()) {
                    // echo ("We have " . $row[0] . "...<br/>" . PHP_EOL);
                    $fullName = $row[0];
                    $latitude = (float)$row[1];
                    $longitude = (float)$row[2];
                    $tzOffset = $row[3];
                    $tzName = $row[4];
                    $baseHeightValue = (float)$row[5];
                    $baseHeightUnit = $row[6];

                    $tideStation = new TideStation();

                    $tideStation->setFullName($fullName); // TODO Encode ?
                    $tideStation->setLatitude($latitude);
                    $tideStation->setLongitude($longitude);
                    $tideStation->setTimeOffset($tzOffset);
                    $tideStation->setTimeZone($tzName);
                    $tideStation->setBaseHeight($baseHeightValue);
                    $tideStation->setUnit($baseHeightUnit);

                    $str_arr = explode (",", $fullName); 
                    // print_r($str_arr);
                    for ($i=0; $i<count($str_arr); $i++) {
                        $tideStation->appendNamePart(trim($str_arr[$i]));
                    }
                    
					// TODO Check Other data, like isCurrentStation, etc
					// Harmonics
                    $coeffStmt = "select t2.coeffname, t2.amplitude, t2.epoch " .
                                 "from stationdata as t2 " .
                                 "join coeffdefs as t3 on t2.coeffname = t3.name " .
                                 "where t2.stationname = :station_name " .
                                 "order by t3.rank";

                    $stmt = self::$db->prepare($coeffStmt);
                    $stmt->bindValue(':station_name', $fullName, SQLITE3_TEXT);

                    $coeffResult = $stmt->execute();
                    // var_dump($coeffResult->fetchArray());

                    while ($coeffRow = $coeffResult->fetchArray()) {
						$coeffName = $coeffRow[0];
						$amplitude = (float)$coeffRow[1];
						$epoch = (float)$coeffRow[2];
                        // echo("For station " . $fullName . ", adding Harmonic [" . $coeffName . "]<br/>" . PHP_EOL);
						$tideStation->appendToHarmonics(
                            new Harmonic($coeffName, $amplitude, $epoch * TideUtilities::$COEFF_FOR_EPOCH)
                        );
					}
                    array_push($stationData, $tideStation);
                }
			} catch (Throwable $ex) {
				throw $ex;
			}

            return $stationData;
        }
    }

    public function buildConstituents() : Constituents { // Constituents, equivaent of the Java buildSiteConstSpeed
		$constituents = null;

        if (self::$db == null) {
            throw new Exception("DB Not connected yet.");
        } else {
			$constituents = new Constituents();
			// Map<String, Constituents.ConstSpeed> constSpeedMap = constituents.getConstSpeedMap();

			$sqlStmt = "select t1.rank, t1.name, t2.coeffvalue from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname order by t1.rank";
			// "select t1.rank, t1.name, t2.coeffvalue, t3.year, t3.value as year_value from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname join equilibriums as t3 on t1.name = t3.coeffname order by t1.rank, t3.year";
			// "select t1.rank, t1.name, t2.coeffvalue, t3.year, t3.value as year_value from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname join nodefactors as t3 on t1.name = t3.coeffname order by t1.rank, t3.year";
			try {
                $results = self::$db->query($sqlStmt);
                while ($row = $results->fetchArray()) {
					$rank = (int)$row[0];
					$name =  $row[1];
					$value = (float)$row[2];
					// Append to map
					$constSpeed = new ConstSpeed($rank, $name, $value * TideUtilities::$COEFF_FOR_EPOCH);
                    $constituents->appendToConstSpeedMap($name, $constSpeed);
					// if ("true".equals(System.getProperty("data.verbose", "false"))) {
					// 	System.out.printf("Rank %d, coeff: %s, value: %f\n", rank, name, value);
					// }
					// Nested queries here
					// Equilibriums
					$speedCoeffsStmt = "select t3.year, t3.value from equilibriums as t3 where t3.coeffname = :coeff_name order by t3.year";
                    $stmt = self::$db->prepare($speedCoeffsStmt);
                    $stmt->bindValue(':coeff_name', $name, SQLITE3_TEXT);

                    $coeffResult = $stmt->execute();
                    while ($coeffRow = $coeffResult->fetchArray()) {
						$year = (int)$coeffRow[0];
						$speedCoeffValue = (float)$coeffRow[1];
						// Populate equilibrium Map
						$constSpeed->putEquilibrium($year, $speedCoeffValue);
						// if ("true".equals(System.getProperty("data.verbose", "false"))) {
						// 	System.out.printf("\tSpeedCoeff -> Year %d, val: %f\n", year, speedCoeffValue);
						// }
					}
					// nodefactors
					$nodeFactorStmt = "select t3.year, t3.value from nodefactors as t3 where t3.coeffname = :coeff_name order by t3.year";
                    $stmt = self::$db->prepare($nodeFactorStmt);
                    $stmt->bindValue(':coeff_name', $name, SQLITE3_TEXT);

                    $coeffResult = $stmt->execute();
                    while ($coeffRow = $coeffResult->fetchArray()) {
						$year = (int)$coeffRow[0]; 
						$nodeFactorValue = (float)$coeffRow[1];
						// Populate nodefactors Map
						$constSpeed->putFactor($year, $nodeFactorValue);
						// if ("true".equals(System.getProperty("data.verbose", "false"))) {
						// 	System.out.printf("\tNodeFactors -> Year %d, val: %f\n", year, nodeFactorValue);
						// }
					}
				}
			} catch (Throwable $sqlEx) {
				throw $sqlEx;
			}
		}
		return $constituents;
	}

    public static function getAmplitudeFix(Constituents $doc, int $year, string $name) : float {
		$d = 0;
		try {
            $cs = TideUtilities::findConstSpeed($doc, $name); // ConstSpeed
            if ($cs != null) {
                $f = $cs->getFactors()[sprintf("%04d", $year)];
                $d = $f;
            } else {
                // TODO Throw something
                echo("!!!! getAmplitudeFix : " . $name . " NOT FOUND for " . $year . ", in " . $year . "<br/>" . PHP_EOL);
            }
		} catch (Throwable $ex) {
			echo("Error in getAmplitudeFix for [" . $name . "] in [" . $year . "].<br/>" . PHP_EOL);
			throw $ex;
		}
		return $d;
	}

	public static function getEpochFix(Constituents $doc, int $year, string $name) : float {
		$d = 0;
		try {
            $cs = TideUtilities::findConstSpeed($doc, $name); // ConstSpeed
            if ($cs != null) {
                $f = $cs->getEquilibrium()[sprintf("%04d", $year)];
                $d = $f * TideUtilities::$COEFF_FOR_EPOCH;
            } else {
                // TODO Throw something
                echo("!!!! getEpochFix : " . $name . " NOT FOUND for " . $year . ", in " . $year . "<br/>" . PHP_EOL);
            }
		} catch (Throwable $ex) {
			echo("Error in getEpochFix for [" . $name . "] in [" . $year . "].<br/>" . PHP_EOL);
			throw $ex;
		}
		return $d;
	}

    public function findTideStation(string $stationName, int $year, Constituents $constituents, array $stations) : TideStation {
        $before = microtime(true); // See https://www.w3schools.com/php/func_date_microtime.asp

        $tideStation = null;
        $tideStationIndex = -1;
        for ($i=0; $i<count($stations); $i++) {
            // if (str_contains($stationData[$i]->getFullName(), "Port-Tudy")) { // PhP 8...
            if (strpos($stations[$i]->getFullName(),  $stationName) !== false) { // Partial match already
                $tideStation = $stations[$i];
                $tideStationIndex = $i;
                break;
            }
        }
        if ($tideStation == null) {
            echo("Station [" . $stationName . "] was not found, trying partial match...<br/>" . PHP_EOL);
            // try with strtoupper
            for ($i=0; $i<count($stations); $i++) {
                // if (str_contains($stationData[$i]->getFullName(), "Port-Tudy")) { // PhP 8...
                if (strpos(strtoupper($stations[$i]->getFullName()),  strtoupper($stationName)) !== false) { // Partial match, uppercase
                    $tideStation = $stations[$i];
                    break;
                }
            }
        }

        $after = microtime(true);
        $timeDiff = ($after - $before) * 1000;

		if (true) {
			echo(sprintf("Finding the station node took %d ms.<br/>", $timeDiff) . PHP_EOL);
		}
		// Fix for the given year
        //  System.out.println("findTideStation: We are in " + year + ", coeff fixed for " + station.yearHarmonicsFixed());
		// Correction to the Harmonics
		if ($tideStation != null && ($tideStation->yearHarmonicsFixed() == -1 || $tideStation->yearHarmonicsFixed() != $year)) {
            $stationHarmonics = $tideStation->getHarmonics();
			for ($i=0; $i<count($stationHarmonics); $i++) {
                $harm = $stationHarmonics[$i];
				$name = $harm->getName();
				if ("x" != $name) {
					$amplitudeFix = self::getAmplitudeFix($constituents, $year, $name);
					$epochFix = self::getEpochFix($constituents, $year, $name);

		/* $harm */ $stationHarmonics[$i]->setAmplitude($harm->getAmplitude() * $amplitudeFix);
		/* $harm */ $stationHarmonics[$i]->setEpoch($harm->getEpoch() - $epochFix);
                    if (false) {
                        echo($stationName . ": Amplitude Fix for " . $name . " in " . $year . " is " . $amplitudeFix . " (->" . $harm->getAmplitude() . ")<br/>" . PHP_EOL);
                        echo($stationName . ": Epoch Fix for " . $name . " in " . $year ." is " . $epochFix . " (->" . $harm->getEpoch() . ")<br/>" . PHP_EOL);
                    }
				}
			}
            $tideStation->setHarmonics($stationHarmonics);
			$tideStation->setHarmonicsFixedForYear($year);

            // Push it, with its harmonics !!
            $this->stationList[$tideStationIndex] = $tideStation;

			if (true) {
				echo("==> Sites coefficients of [" . $tideStation->getFullName() . "] fixed for " . $year . ".<br/>" . PHP_EOL);
			}
		} else if (true) {
			echo("Coefficients were <i><b>already fixed</b></i> for " . $year . ".<br/>" . PHP_EOL);
		}
		return $tideStation;
	}

}
