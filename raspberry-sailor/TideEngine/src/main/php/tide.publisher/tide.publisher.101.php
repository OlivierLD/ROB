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

/**
 * Publish for one month
 */
function publishAlmanac(string $stationName, 
                        int $year, 
                        int $month, 
                        BackEndSQLiteTideComputer $backend, 
                        Constituents $constituentsObject, 
                        array $stationsData) : string {

    global $VERBOSE;        
    
    $content = "";

    $theTideStation = $backend->findTideStation($stationName, $year, $constituentsObject, $stationsData);
    if ($theTideStation == null) {
        $content .= ($stationName . " was not found...<br/>" . PHP_EOL);
    } else {
        // echo("<b>" . $stationName . "</b> : Base height: " . $theTideStation->getBaseHeight() . " " . $theTideStation->getDisplayUnit() . "<br/>" . PHP_EOL);
        // var_dump($theTideStation);
        if ($theTideStation->isCurrentStation()) {
            $content .= ($stationName . " IS a current station.<br/>" . PHP_EOL);
            // TODO Honk ?
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

                // $context2 = $ac->getContext();

                // $semiDiamSun = sprintf("%.04f", ($context2->SDsun / 60));
                // $sunHP = sprintf("%.04f", ($context2->HPsun / 60)); 
                // $semiDiamMoon = sprintf("%.04f", ($context2->SDmoon / 60)); 
                // var_dump($ac->getMoonPhase());
                $moonPhaseAngle = $ac->getMoonPhase()->phase; 
                // $moonPhase = sprintf("%.02f %%, ", $context2->k_moon) . $ac->getMoonPhaseStr(); 
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
        // Table for 1 month...
        $content .= ("<p>" . PHP_EOL);
        $content .= ("<b>" . $theTideStation->getFullName() . "</b>, " . 
                        decToSex($theTideStation->getLatitude(), "NS") . " / " . decToSex($theTideStation->getLongitude(), "EW") . ", TZ " . 
                        $theTideStation->getTimeZone() . ", base height " . 
                        $theTideStation->getBaseHeight() . " " . $theTideStation->getDisplayUnit() . "<br/>" . PHP_EOL);
        $content .= ("<i>For " . DateTime::createFromFormat("Y-m", sprintf("%04d-%02d", $year, $month))->format("F Y") . "</i><br/>" . PHP_EOL);
        $content .= ("<table style='border: 1px solid black;'>" . PHP_EOL);
        $colCounter = 0;
        $nbCol = 4;
        while ($colCounter < count($arrayKeys)/* && $colCounter < 10*/) {
            $content .= ("<tr>" . PHP_EOL);
            for ($j=0; $j<$nbCol; $j++) {
                if ($colCounter < count($arrayKeys)) {
                    $dateTime = DateTime::createFromFormat("Y-m-d", $arrayKeys[$colCounter]); // , $tz); 
                    $content .= ("<td style='vertical-align: top;'>" . PHP_EOL);
                    // Inner table
                    $content .= ("<table>" . PHP_EOL);
                    $content .= (  "<tr><td colspan='6'><b>" . $dateTime->format('l, M d, Y') . "</b></td></tr>" . PHP_EOL);
                    $content .= (  "<tr><th></th><th>Time</th><th>Height</th><th>Unit</th><th>Coeff</th></tr>" . PHP_EOL);
                    $tideData = $monthTable[$arrayKeys[$colCounter]]["tide.data"];
                    $moonPhase = $monthTable[$arrayKeys[$colCounter]]["moon.phase"];

                    for ($k=0; $k<count($tideData); $k++) {
                        $content .= ("<tr>" . PHP_EOL);
                        $content .= (  "<td><b>" . $tideData[$k]->getType() . "</b></td>" .
                                "<td>" . $tideData[$k]->getCalendar()->format("H:i") . "</td>" .
                                "<td>" . sprintf("%.02f", $tideData[$k]->getValue()) . "</td>" . 
                                "<td>" . $tideData[$k]->getUnit() . "</td>" .
                                "<td style='text-align: center;'>" . ($tideData[$k]->getCoeff() != 0 ? sprintf("%02d", $tideData[$k]->getCoeff()) : "") . "</td>" . 
                                ($k == 0 ? "<td rowspan='4'><img src='" . $moonPhase . "'></td>" : "") . PHP_EOL);
                        $content .= ("<tr>" . PHP_EOL);
                    }
                    $content .= ("</table>" . PHP_EOL);
                    $content .= ("</td>" . PHP_EOL);
                    $colCounter++;
                }
            }
            // $colCounter += 1; // Oho !
            $content .= ("</tr>" . PHP_EOL);
        }
        $content .= ("</table>" . PHP_EOL);
        $content .= ("</p>" . PHP_EOL);
    }
    return $content;
}

function publishStationDuration(string $stationName, int $year, ?int $month=null) : void {
    global $VERBOSE;

    $backend = new BackEndSQLiteTideComputer();
    if ($VERBOSE) {
        echo("Backend created.<br/>". PHP_EOL);
    }
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

    // $year = (int)date("Y"); // gmdate ?
    // $month = (int)date("m");
    if ($month != null) {
        $content = publishAlmanac($stationName, $year, $month, $backend, $constituentsObject, $stationsData);
        // display it
        echo($content);
    } else {
        // One year, loop.
        $fullContent = "";
        for ($m=1; $m<=12; $m++) {
            $fullContent .= publishAlmanac($stationName, $year, $m, $backend, $constituentsObject, $stationsData);
        }
        // Display
        echo($fullContent);
    }

    $backend->closeDB();
    if ($VERBOSE) {
        echo("Connection closed.<br/>". PHP_EOL);
    }
}

function getStationsList(string $pattern) : array {
    global $VERBOSE;

    $backend = new BackEndSQLiteTideComputer();
    if ($VERBOSE) {
        echo("Backend created.<br/>". PHP_EOL);
    }
    $backend->connectDB("../sql/tides.db");
    if ($VERBOSE) {
        echo("Connection created.<br/>". PHP_EOL);
    }
    $stationList = $backend->getStationList($pattern);

    $backend->closeDB();
    if ($VERBOSE) {
        echo("Connection closed.<br/>". PHP_EOL);
    }
    return $stationList;
}

function blankScreen() : void {
    ?>
    <h2>Choose your tide station</h2>
    <form action="<?php echo basename(__FILE__); ?>" 
          method="get" 
          name="formStation" 
          style="padding:0; margin:0">
        
        Enter a part of the name: <input type="text" size="40" name="pattern" placeholder="Name pattern">
        <br/>
        <input type="submit" value="Submit">
    </form>
    <?php
}

function selectStationAndDuration(array $list) : void {
    echo("Selected " . count($list) . " station(s)<br/>" . PHP_EOL);

    if (count($list) == 0) {
        echo("No station selected... Try again.<br/>" . PHP_EOL);
    } else {
    ?>
    <h2>Choose your tide station, select duration</h2>

    <script type="text/javascript">
        let updateMonthField = (item) => {
            console.log(`Duration is now ${item.value}`);
            document.getElementById('month-list').disabled = (item.value === 'YEAR');
        };
    </script>

    <form action="<?php echo basename(__FILE__); ?>" 
          method="get" 
          id="formStation" 
          style="padding:0; margin:0">
        <table>
            <tr>
                <td>Station</td>
                <td>
                    <select name="station-name" form="formStation">
    <?php
    for ($i=0; $i<count($list); $i++) {
        echo("<option value='" . $list[$i] . "'>" . $list[$i] . "</option>" . PHP_EOL);
    }
    ?>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Duration</td>
                <td>
                    <!--input type="hidden" name="duration" value="MONTH"-->
                    <select name="duration" form="formStation" onchange="updateMonthField(this);">
                        <option value="MONTH">One month</option>
                        <option value="YEAR">One year</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Year</td>
                <td>
                    <input type="number" name="year" value="2024">
                </td>
            </tr>
            <tr>
                <td>Month</td>
                <td>
                    <select id="month-list" name="month" form="formStation">
                        <option value="1">January</option>
                        <option value="2">February</option>
                        <option value="3">March</option>
                        <option value="4">April</option>
                        <option value="5">May</option>
                        <option value="6">June</option>
                        <option value="7">July</option>
                        <option value="8">August</option>
                        <option value="9">September</option>
                        <option value="10">October</option>
                        <option value="11">November</option>
                        <option value="12">December</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Submit">
                </td>
            </tr>
        </table>
    </form>
    <?php
    }
}

function selectDuration(string $stationName) : void {
    // Unused...
}

$stationName = null;
$publishDuration = null;
$publishYear = null;
$publishMonth = null;
$pattern = null;

$BLANK_SCREEN = 0;
$PICK_UP_STATION = 1;
$SELECT_DURATION = 2;
$PUBLISH = 3;

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/../tide.computer/autoload.php';
    include __DIR__ . '/../../../../../../astro-computer/AstroComputer/src/main/php.v7/autoload.php'; // Modify at will !!

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    /**
     * URL Like http://.../tide.publisher.101.php?station-name=Port-Tudy&duration=MONTH&year=2024&month=12
     */

    if (isset($_GET['station-name'])) {
        $stationName = $_GET['station-name'];
    }
    if (isset($_GET['duration'])) {
        $publishDuration = $_GET['duration']; // YEAR | MONTH
    }
    if (isset($_GET['year'])) {
        $publishYear = $_GET['year']; 
    }
    if (isset($_GET['month'])) {
        $publishMonth = $_GET['month']; 
    }
    if (isset($_GET['pattern'])) {
        $pattern = $_GET['pattern']; 
    }

    // Verify coherence
    $option = -1;
    if ($pattern == null && $stationName == null && $publishDuration == null && $publishYear == null && $publishMonth == null) {
        // All good
        $option = $BLANK_SCREEN; // To select a station
    } else if ($pattern != null) {
        $option = $PICK_UP_STATION;
    } else if ($stationName != null && $publishDuration == null) {
        $option = $SELECT_DURATION; // YEAR, MONTH, year, month
    } else if ($stationName != null && $publishDuration != null) {
        $option = $PUBLISH;
    }
    
    if ($option == -1) {
        echo ("WTF???<br/>" . PHP_EOL);
    } else if ($option == $BLANK_SCREEN) {
        // echo ("Blank Screen<br/>" . PHP_EOL);
        blankScreen();
    } else if ($option == $PICK_UP_STATION) {
        echo ("Pick-up Station<br/>" . PHP_EOL);
        // $pattern = "Brest";
        $list = getStationsList($pattern);
        selectStationAndDuration($list);
    } else if ($option == $SELECT_DURATION) {
        echo ("Select Duration<br/>" . PHP_EOL);
        selectDuration($stationName); // Done in the above, probably useless.
    } else if ($option == $PUBLISH) {
        // like publishStationDuration("Port-Tudy", 2024, 12);
        if ($publishDuration == 'MONTH') {
            publishStationDuration($stationName, $publishYear, $publishMonth);
        } else {
            publishStationDuration($stationName, $publishYear);
        }
    }

} catch (Throwable $plaf) {
    // See https://www.php.net/manual/en/language.constants.magic.php
    echo "[Captured Throwable (big loop) for " . __FILE__ . " : " . $plaf . "] " . PHP_EOL;
}
?>
</body>
</html>
