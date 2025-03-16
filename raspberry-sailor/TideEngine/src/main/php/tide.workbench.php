<?php

// To use with curl. Not from a browser...
// like curl -X GET http://localhost:8000/tide.workbench.php
// Plain text output.

function getCoeffData (BackEndSQLiteTideComputer $backend, Constituents $constituentsObject, array $stationsData, int $year, int $month, int $day, ?string $tz2Use) : array {
    $brestTideStation = $backend->findTideStation("Brest, France", $year, $constituentsObject, $stationsData);
    // assert (brestTideStation != null);
    $brestTable = TideUtilities::getTideTableForOneDay($brestTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $day, $tz2Use);
    $coeffsInBrest = TideUtilities::getCoeffInBrest($brestTideStation, $brestTable);
    return $coeffsInBrest;
}

function stationTest(string $stationName, 
                     int $year, 
                     int $month, 
                     int $day,
                     BackEndSQLiteTideComputer $backend, 
                     Constituents $constituentsObject, 
                     array $stationsData, 
                     ?bool $withCoeffs=false,
                     ?bool $oneMonthTable=false) : void {

    $theTideStation = $backend->findTideStation($stationName, $year, $constituentsObject, $stationsData);
    if ($theTideStation == null) {
        echo($stationName . " was not found..." . PHP_EOL);
    } else {
        echo("" . $stationName . " : Base height: " . $theTideStation->getBaseHeight() . " " . $theTideStation->getDisplayUnit() . "" . PHP_EOL);
        // var_dump($theTideStation);
        if ($theTideStation->isCurrentStation()) {
            echo($stationName . " IS a current station." . PHP_EOL);
        } else {
            echo($stationName . " IS NOT a current station." . PHP_EOL);
            echo("Display Unit: " . $theTideStation->getDisplayUnit() . "" . PHP_EOL);
            echo("Time Zone: " . $theTideStation->getTimeZone() . ", Time Zone Offset: " . $theTideStation->getTimeOffset() . "" . PHP_EOL);
        }

        // Water Heights test
        // $UTdate = gmdate("Y-m-d H:i:s");
        $UTdate = microtime(true);
        $now = DateTime::createFromFormat('U.u', $UTdate); // UTC

        echo("Now is " . $now->format("H:i:s.v") . " (UTC)." . PHP_EOL);
        
        // $date = new DateTime("2024-11-28 15:47:26"); // Yeah !!!
        // // Convert DateTime to string using date_format()
        // $UTdate = date_format($date, 'Y-m-d H:i:s');

        // $date->setTimezone(new DateTimeZone('Pacific/Chatham'));
        // echo $date->format('Y-m-d H:i:sP') . "\n";
        $now->setTimeZone(new DateTimeZone($theTideStation->getTimeZone()));
        echo "Local Time in " . $stationName . ": " . $now->format('l, Y-m-d H:i:sP') . "" . PHP_EOL;

        $localTime = date_format($now, 'Y-m-d H:i:s');

        $wh = TideUtilities::getWaterHeight($theTideStation, $constituentsObject->getConstSpeedMap(), $localTime);
        echo("Water Height in " . $stationName . ", at " . $localTime . " (local) : " . sprintf("%.02f", $wh) . " " . $theTideStation->getDisplayUnit() . "" . PHP_EOL);
        $mm = TideUtilities::getMinMaxWH($theTideStation, $constituentsObject->getConstSpeedMap(), $localTime);
        echo("Min-Max Height in " . $stationName . ", at " . $localTime . " (local) : min: " . sprintf("%.02f", $mm["min"]) . ", max: " . sprintf("%.02f", $mm["max"])  . ", in " . $theTideStation->getDisplayUnit() . "" . PHP_EOL);

        // More...
        echo("Tide table for one day..." . PHP_EOL);
        $before = microtime(true);
        $tz2Use = null ; // "Europe/Paris"; // Enforce
        $tideForOneDay = TideUtilities::getTideTableForOneDay($theTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $day, $tz2Use /*$theTideStation->getTimeZone()*/);

        $after = microtime(true);
        $timeDiff = ($after - $before) * 1000;
        echo("Tide table for one day, done in " . sprintf("%.02f", $timeDiff) . " ms" . PHP_EOL);

        if ($withCoeffs) {
            // $brestTideStation = $backend->findTideStation("Brest, France", $year, $constituentsObject, $stationsData);

			// // assert (brestTideStation != null);
            // $brestTable = TideUtilities::getTideTableForOneDay($brestTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $day, $tz2Use);
            // $coeffsInBrest = TideUtilities::getCoeffInBrest($brestTideStation, $brestTable);

            $coeffsInBrest = getCoeffData ($backend, $constituentsObject, $stationsData, $year, $month, $day, $tz2Use);
            $indexInCoeffs = 0;
            for ($i=0; $i<count($tideForOneDay); $i++) {
                $tv = $tideForOneDay[$i];
                if ($tv->getType() == "HW" && count($coeffsInBrest) > $indexInCoeffs) {
                    $tv->setCoeff($coeffsInBrest[$indexInCoeffs]);
                    $indexInCoeffs++;
                }
            }
        }

        // var_dump($tideForOneDay);
        for ($i=0; $i<count($tideForOneDay); $i++) {
            echo("- " . $tideForOneDay[$i]->getType() . 
                 " at " . $tideForOneDay[$i]->getFormattedDate() . 
                 ", " . sprintf("%.02f", $tideForOneDay[$i]->getValue()) . " " . $tideForOneDay[$i]->getUnit() . 
                 ($tideForOneDay[$i]->getCoeff() != 0 ? sprintf(", Coeff: %02d", $tideForOneDay[$i]->getCoeff()) : "") . "" . PHP_EOL);
        }

        // Tide for one month ?
        if ($oneMonthTable) {
            $nbDaysThisMonth = TideUtilities::getNbDays($year, $month);
            echo("Will process tide for one month:" . $year . ", " . $month . ", " . $nbDaysThisMonth . " days." . PHP_EOL);
            $monthTable = array();
            for ($d=1; $d<=$nbDaysThisMonth; $d++) {
                // echo(">>> Processing day :" . $d . "." . PHP_EOL);
                $tideForOneDay = TideUtilities::getTideTableForOneDay($theTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $d, $tz2Use /*$theTideStation->getTimeZone()*/);
                // Coeffs
                $coeffsInBrest = getCoeffData($backend, $constituentsObject, $stationsData, $year, $month, $d, $tz2Use);
                $indexInCoeffs = 0;
                for ($i=0; $i<count($tideForOneDay); $i++) {
                    $tv = $tideForOneDay[$i];
                    if ($tv->getType() == "HW" && count($coeffsInBrest) > $indexInCoeffs) {
                        $tv->setCoeff($coeffsInBrest[$indexInCoeffs]);
                        $indexInCoeffs++;
                    }
                }
                // Done.
                $monthTable += [sprintf("%04d-%02d-%02d", $year, $month, $d) => $tideForOneDay];
            }
            // var_dump($monthTable);

            $arrayKeys = array_keys($monthTable);

            if (false) {
                $colCounter = 0;
                while ($colCounter < count($arrayKeys)) {
                    echo($arrayKeys[$colCounter] . (sprintf(" %02d", $colCounter % 3)) . "" . PHP_EOL);
                    $colCounter += 1;
                }
            }

            if (false) {
                // A table test...
                echo("<p>" . PHP_EOL);
                echo("<b>" . $theTideStation->getFullName() . "</b>, " . 
                              decToSex($theTideStation->getLatitude(), "NS") . " / " . decToSex($theTideStation->getLongitude(), "EW") . ", TZ " . 
                              $theTideStation->getTimeZone() . "" . PHP_EOL);
                echo("<i>For " . DateTime::createFromFormat("Y-m", sprintf("%04d-%02d", $year, $month))->format("F Y") . "</i>" . PHP_EOL);
                echo("<table style='border: 1px solid black;'>" . PHP_EOL);
                $colCounter = 0;
                $nbCol = 4;
                while ($colCounter < count($arrayKeys)/* && $colCounter < 10*/) {
                    echo("<tr>" . PHP_EOL);
                    for ($j=0; $j<$nbCol; $j++) {
                        if ($colCounter < count($arrayKeys)) {
                            $dateTime = DateTime::createFromFormat("Y-m-d", $arrayKeys[$colCounter]); // , $tz); 
                            echo("<td style='vertical-align: top;'>" . PHP_EOL);
                            // Inner table
                            echo("<table>" . PHP_EOL);
                            echo(  "<tr><td colspan='5'><b>" . $dateTime->format('l, M d, Y') . "</b></td></tr>" . PHP_EOL);
                            echo(  "<tr><th></th><th>Time</th><th>Height</th><th>Unit</th><th>Coeff</th></tr>" . PHP_EOL);
                            $tideData = $monthTable[$arrayKeys[$colCounter]];

                            for ($k=0; $k<count($tideData); $k++) {
                                echo("<tr>" . PHP_EOL);
                                echo(  "<td><b>" . $tideData[$k]->getType() . "</b></td>" .
                                       "<td>" . $tideData[$k]->getCalendar()->format("H:i") . "</td>" .
                                       "<td>" . sprintf("%.02f", $tideData[$k]->getValue()) . "</td>" . 
                                       "<td>" . $tideData[$k]->getUnit() . "</td>" .
                                       "<td style='text-align: center;'>" . ($tideData[$k]->getCoeff() != 0 ? sprintf("%02d", $tideData[$k]->getCoeff()) : "") . "</td>" . PHP_EOL);
                                echo("<tr>" . PHP_EOL);
                            }
                            echo("</table>" . PHP_EOL);
                            echo("</td>" . PHP_EOL);
                            $colCounter++;
                        }
                    }
                    // $colCounter += 1; // Oho !
                    echo("</tr>" . PHP_EOL);
                }
                echo("</table>" . PHP_EOL);
                echo("</p>" . PHP_EOL);
            }
        }
    }
}

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/tide.computer/autoload.php';

    $VERBOSE = false;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low." . PHP_EOL);
    }

    $backend = new BackEndSQLiteTideComputer();
    echo("Backend created.". PHP_EOL);

    // $backend->getStationsData();

    $backend->connectDB("./sql/tides.db");
    echo("Connection created.". PHP_EOL);

    echo("Executing buildConstituents...". PHP_EOL);
    $constituentsObject = $backend->buildConstituents();

    if ($VERBOSE) {
        var_dump($constituentsObject);
        echo("" . PHP_EOL);
    }

    // assert-like stuff, to compare with Java values.

    echo("buildConstituents executed, " . count($constituentsObject->getConstSpeedMap()) . " element(s) in the ConstSpeedMap.". PHP_EOL);

    echo("Executing getStationData..." . PHP_EOL);
    $stationsData = $backend->getStationsData();
    // var_dump($stationsData);
    // echo("" . PHP_EOL);
    echo("getStationData executed, " . count($stationsData) . " element(s).". PHP_EOL);

    echo("-------------------------------" . PHP_EOL);
    echo("----- Water Height Tests ------" . PHP_EOL);
    echo("-------------------------------" . PHP_EOL);

    $year = (int)date("Y"); // gmdate ?
    $month = (int)date("m");
    $day = (int)date("d");

    // Find Port-Tudy... for the given year.
    $stationName = "Port-Tudy";
    stationTest($stationName, $year, $month, $day, $backend, $constituentsObject, $stationsData, true, true);

    echo("-------------------------------" . PHP_EOL);
    // And so on...
    $stationName = "Half Moon Bay";
    stationTest($stationName, $year, $month, $day, $backend, $constituentsObject, $stationsData);

    echo("-------------------------------" . PHP_EOL);
    // Find Port-Tudy... for the given year, again.
    $stationName = "Port-Tudy";
    stationTest($stationName, $year, $month, $day, $backend, $constituentsObject, $stationsData);

    echo("-------------------------------" . PHP_EOL);
    // Find Johnston Atoll, Pacific Ocean... for the given year.
    $stationName = "Johnston Atoll";
    stationTest($stationName, $year, $month, $day, $backend, $constituentsObject, $stationsData);

    echo("-------------------------------" . PHP_EOL);
    $stationName = "Falmouth";
    stationTest($stationName, $year, $month, $day, $backend, $constituentsObject, $stationsData);

    echo("-------------------------------" . PHP_EOL);
    
    $backend->closeDB();
    echo("Connection closed.". PHP_EOL);

    echo("Test Completed.". PHP_EOL);

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big loop) for tide.workbench.php : " . $plaf . "] " . PHP_EOL;
}
