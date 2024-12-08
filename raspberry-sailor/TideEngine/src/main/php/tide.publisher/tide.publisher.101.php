<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Tide Publisher 101</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <style type="text/css">
		* {
			font-family: 'Courier New', Courier, monospace;
		}
        td {
            border: 1px solid black;
            border-radius: 5px;
            padding: 5px;
        }
    </style>
</head>

<body style="background-color: rgba(255, 255, 255, 0.2); background-image: none;"> <!-- background="bground.jpg" style="min-height: 900px;"> -->
<h2>PHP Tides Publisher</h2>

<?php

$VERBOSE = false;

function getCoeffData (BackEndSQLiteTideComputer $backend, Constituents $constituentsObject, array $stationsData, int $year, int $month, int $day, ?string $tz2Use) : array {
    $brestTideStation = $backend->findTideStation("Brest, France", $year, $constituentsObject, $stationsData);
    // assert (brestTideStation != null);
    $brestTable = TideUtilities::getTideTableForOneDay($brestTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $day, $tz2Use);
    $coeffsInBrest = TideUtilities::getCoeffInBrest($brestTideStation, $brestTable);
    return $coeffsInBrest;
}

function publishAlmanac(string $stationName, 
                        int $year, 
                        int $month, 
                        BackEndSQLiteTideComputer $backend, 
                        Constituents $constituentsObject, 
                        array $stationsData) : void {

    global $VERBOSE;                            

    $theTideStation = $backend->findTideStation($stationName, $year, $constituentsObject, $stationsData);
    if ($theTideStation == null) {
        echo($stationName . " was not found...<br/>" . PHP_EOL);
    } else {
        echo("<b>" . $stationName . "</b> : Base height: " . $theTideStation->getBaseHeight() . " " . $theTideStation->getDisplayUnit() . "<br/>" . PHP_EOL);
        // var_dump($theTideStation);
        if ($theTideStation->isCurrentStation()) {
            echo($stationName . " IS a current station.<br/>" . PHP_EOL);
            // TODO Honk ?
        }

        if (false) {
            // $UTdate = gmdate("Y-m-d H:i:s");
            $UTdate = microtime(true);
            $now = DateTime::createFromFormat('U.u', $UTdate); // UTC
            // echo("Now is " . $now->format("H:i:s.v") . " (UTC).<br/>");
            
            $now->setTimeZone(new DateTimeZone($theTideStation->getTimeZone()));
            if ($VERBOSE) {
                echo "Local Time in " . $stationName . ": " . $now->format('l, Y-m-d H:i:sP') . "<br/>";
            }

            $localTime = date_format($now, 'Y-m-d H:i:s');
        }

        // Tide for one month 
        $nbDaysThisMonth = TideUtilities::getNbDays($year, $month);
        if ($VERBOSE) {
            echo("Will process tide for one month:" . $year . ", " . $month . ", " . $nbDaysThisMonth . " days.<br/>" . PHP_EOL);
        }
        $monthTable = array();
        $tz2Use = null;
        for ($d=1; $d<=$nbDaysThisMonth; $d++) {
            // echo(">>> Processing day :" . $d . ".<br/>" . PHP_EOL);
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
            // Moon phase at 12:00
            if (true) {
                $ac = new AstroComputer();
                $withStars = false;
                $ac->calculate($year, $month, $d, 12, 0, 0, true, $withStars);

                $context2 = $ac->getContext();

                $semiDiamSun = sprintf("%.04f", ($context2->SDsun / 60));
                $sunHP = sprintf("%.04f", ($context2->HPsun / 60)); 
                $semiDiamMoon = sprintf("%.04f", ($context2->SDmoon / 60)); 
                // var_dump($ac->getMoonPhase());
                $moonPhaseAngle = $ac->getMoonPhase()->phase;                      // TODO Fix that
                $moonPhase = sprintf("%.02f %%, ", $context2->k_moon) . $ac->getMoonPhaseStr(); // sprintf("", ) "${calcResult.moon.illum.toFixed(2)}% ${calcResult.moon.phase.phase}";
                $phaseIndex = floor($moonPhaseAngle / (360 / 28.5)) + 1;
                if ($phaseIndex > 28) {
                    $phaseIndex = 28;
                }
                $phaseImageName = sprintf("./moon/phase%02d.gif", $phaseIndex);
            }

            // Done.
            $monthTable += [sprintf("%04d-%02d-%02d", $year, $month, $d) => array("tide.data" => $tideForOneDay, "moon.phase" => $phaseImageName)];
        }
        // var_dump($monthTable);

        $arrayKeys = array_keys($monthTable);

        if (false) {
            $colCounter = 0;
            while ($colCounter < count($arrayKeys)) {
                echo($arrayKeys[$colCounter] . (sprintf(" %02d", $colCounter % 3)) . "<br/>" . PHP_EOL);
                $colCounter += 1;
            }
        }

        // Table for 1 month...
        echo("<p>" . PHP_EOL);
        echo("<b>" . $theTideStation->getFullName() . "</b>, " . 
                        decToSex($theTideStation->getLatitude(), "NS") . " / " . decToSex($theTideStation->getLongitude(), "EW") . ", TZ " . 
                        $theTideStation->getTimeZone() . "<br/>" . PHP_EOL);
        echo("<i>For " . DateTime::createFromFormat("Y-m", sprintf("%04d-%02d", $year, $month))->format("F Y") . "</i><br/>" . PHP_EOL);
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
                    echo(  "<tr><td colspan='6'><b>" . $dateTime->format('l, M d, Y') . "</b></td></tr>" . PHP_EOL);
                    echo(  "<tr><th></th><th>Time</th><th>Height</th><th>Unit</th><th>Coeff</th></tr>" . PHP_EOL);
                    $tideData = $monthTable[$arrayKeys[$colCounter]]["tide.data"];
                    $moonPhase = $monthTable[$arrayKeys[$colCounter]]["moon.phase"];

                    for ($k=0; $k<count($tideData); $k++) {
                        echo("<tr>" . PHP_EOL);
                        echo(  "<td><b>" . $tideData[$k]->getType() . "</b></td>" .
                                "<td>" . $tideData[$k]->getCalendar()->format("H:i") . "</td>" .
                                "<td>" . sprintf("%.02f", $tideData[$k]->getValue()) . "</td>" . 
                                "<td>" . $tideData[$k]->getUnit() . "</td>" .
                                "<td style='text-align: center;'>" . ($tideData[$k]->getCoeff() != 0 ? sprintf("%02d", $tideData[$k]->getCoeff()) : "") . "</td>" . 
                                ($k == 0 ? "<td rowspan='4'><img src='" . $moonPhase . "'></td>" : "") . PHP_EOL);
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

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/../tide.computer/autoload.php';
    include __DIR__ . '/../../../../../../astro-computer/AstroComputer/src/main/php.v7/autoload.php'; // Modify at will !!

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    $backend = new BackEndSQLiteTideComputer();
    if ($VERBOSE) {
        echo("Backend created.<br/>". PHP_EOL);
    }

    // $backend->getStationsData();

    $backend->connectDB("../sql/tides.db");
    if ($VERBOSE) {
        echo("Connection created.<br/>". PHP_EOL);
    }

    if ($VERBOSE) {
        echo("Executing buildConstituents...<br/>". PHP_EOL);
    }
    $constituentsObject = $backend->buildConstituents();

    if ($VERBOSE) {
        var_dump($constituentsObject);
        echo("<br/>" . PHP_EOL);
    }

    // assert-like stuff, to compare with Java values.

    if ($VERBOSE) {
        echo("buildConstituents executed, " . count($constituentsObject->getConstSpeedMap()) . " element(s) in the ConstSpeedMap.<br/>". PHP_EOL);
    }

    if ($VERBOSE) {
        echo("Executing getStationData...<br/>". PHP_EOL);
    }
    $stationsData = $backend->getStationsData();
    // var_dump($stationsData);
    // echo("<br/>" . PHP_EOL);
    if ($VERBOSE) {
        echo("getStationData executed, " . count($stationsData) . " element(s).<br/>". PHP_EOL);
    }

    $year = (int)date("Y"); // gmdate ?
    $month = (int)date("m");
    $day = (int)date("d");

    // Find Port-Tudy... for the given year.
    $stationName = "Port-Tudy";
    publishAlmanac($stationName, $year, $month, $backend, $constituentsObject, $stationsData);

    $backend->closeDB();
    if ($VERBOSE) {
        echo("Connection closed.<br/>". PHP_EOL);
    }

} catch (Throwable $plaf) {
    // See https://www.php.net/manual/en/language.constants.magic.php
    echo "[Captured Throwable (big loop) for " . __FILE__ . " : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>
