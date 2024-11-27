<?php

class BackEndSQLiteTideComputer {

    private static $db; // SQLite3 

    public function connectDB(string $location) : void {
        self::$db = new SQLite3($location);
    }

    public function closeDB() : void {
        self::$db->close();
    }

    public function getStationData() : array {
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

}
