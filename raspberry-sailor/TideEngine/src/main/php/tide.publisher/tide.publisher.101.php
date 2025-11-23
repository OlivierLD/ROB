<!--
    include __DIR__ . '/../tide.computer/autoload.php';
    include __DIR__ . '/../../astro.php/celestial.computer/autoload.php'; // Modify at will !!
-->
<!DOCTYPE html>
<!--
 | Full UI for Tide Almanac publication.
 +-->
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Tide Publisher</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <style type="text/css">
:root {
    --doc-scale: 0.9;
}
* {
    font-family: 'Courier New', Courier, monospace;
}
html, body {
    height: 100%;
    display: flex;
    flex-flow: column;
}
td {
    border: 1px solid black;
    border-radius: 5px;
    padding: 5px;
}
table {
    border: 1px solid black;
    border-radius: 5px;
    padding: 5px;
}

.final-form {
    border: 2px solid blue;
    border-radius: 5px;
    padding: 10px;
}
.final-submit {
    margin-top: 5px;
}

.scaled {
    transform: scale(var(--doc-scale)); /* Equal to scaleX(0.7) scaleY(0.7) */
    transform-origin: top left;
}

@media screen {
    .screen-only {
        display: inline-block;
    }

    #result-to-publish {
        margin: 0 0;
		display: block;
        /* color: darkred; */
        width: 105vw; /* needs some love... */
        /* height: 100%; */
        /* max-height: 400px; */
        flex: 1 1 auto;
        overflow: auto;
	}
}

@media print {

    /* #result-to-publish > table > * {
        font-size: 0.80rem;
        line-height: 0.5rem;
        color: black;
    }

    td {
        border: 1px solid black;
        border-radius: 5px;
        padding: 3px;
    }
    table {
        border: 1px solid black;
        border-radius: 5px;
        padding: 1px;
    } */

    .table-content, .content, .blank-for-print {
        page-break-before: always;
    }

    tr {
        print-color-adjust: exact; /* For background colors to be printed */
    }

    .doc-section {
        page-break-inside: avoid;
    }

    .page-break {
		page-break-after: always
	}

    .screen-only {
        display: none;
    }

    .print-only {  /* overrides the one in main.css */
        display: block;
    }

    #result-to-publish {
		display: block;
        /* color: black; */
        max-height: none;
	}
}

    </style>
    <script type="text/javascript">
function setDocScale(e) {
    let v = this.value / 100.0;
    document.body.style.setProperty("--doc-scale", v);
}

function specialOptions(cb) {
    let displayDiv = document.getElementById("special-prm-table");
    if (cb.checked) {
        displayDiv.style.display = "block";
    } else {
        displayDiv.style.display = "none";
    }
}
    </script>
</head>

<body style="background-color: rgba(255, 255, 255, 0.2); background-image: none;"> <!-- background="bground.jpg" style="min-height: 900px;"> -->
<div id="user-entry" class="screen-only" style="width: 90%;">
<h2>Passe-Coque PHP Tides Publisher</h2>

<?php

ini_set('memory_limit', '-1'); // Required for reloadOneStation (or its equivalent)

$VERBOSE = false;

$lang = "EN";
// Get it from the browser
$browserLang = substr($_SERVER['HTTP_ACCEPT_LANGUAGE'], 0, 2);
echo "Browser Language: [" . $browserLang . "]<br/>";
if ($browserLang == 'fr') {
    $lang = 'FR';
} else {
    // leave it to English
}

// Translations, each record is "key" => array("EN" => "text in English", "FR" => "text en Français")
$translations = array(
    "go-back" => array("EN" => "Go Back", "FR" => "Retour"),
    "choose-station" => array("EN" => "Choose your tide station", "FR" => "Choisissez la station"),
    "part-of-name" => array("EN" => "Enter a part of the name", "FR" => "Saisissez une partie du nom"),
    "choose-station-2" => array("EN" => "Choose your tide station, select period", "FR" => "Choisissez la station, et sélectionnez la période"),
    "station" => array("EN" => "Station", "FR" => "Station"),
    "duration" => array("EN" => "Duration", "FR" => "Durée"),
    "one-month" => array("EN" => "One month", "FR" => "Un mois"),
    "one-year" => array("EN" => "One year", "FR" => "Un an"),
    "year" => array("EN" => "Year", "FR" => "Année"),
    "month" => array("EN" => "Month", "FR" => "Mois"),
    "january" => array("EN" => "January", "FR" => "Janvier"),
    "february" => array("EN" => "February", "FR" => "Février"),
    "march" => array("EN" => "March", "FR" => "Mars"),
    "april" => array("EN" => "April", "FR" => "Avril"),
    "may" => array("EN" => "May", "FR" => "Mai"),
    "june" => array("EN" => "June", "FR" => "Juin"),
    "july" => array("EN" => "July", "FR" => "Juillet"),
    "august" => array("EN" => "August", "FR" => "Août"),
    "september" => array("EN" => "September", "FR" => "Septembre"),
    "october" => array("EN" => "October", "FR" => "Octobre"),
    "november" => array("EN" => "November", "FR" => "Novembre"),
    "december" => array("EN" => "December", "FR" => "Décembre"),
    "base-height" => array("EN" => "base height", "FR" => "niveau moyen"),
    "time" => array("EN" => "Time", "FR" => "Heure"),
    "height" => array("EN" => "Height", "FR" => "Hauteur"),
    "unit" => array("EN" => "Unit", "FR" => "Unité"),
    "coeff" => array("EN" => "Coeff", "FR" => "Coeff"),
    "HW" => array("EN" => "HW", "FR" => "PM"),
    "LW" => array("EN" => "LW", "FR" => "BM"),
    "meters" => array("EN" => "meters", "FR" => "mètres"),
    "feet" => array("EN" => "feet", "FR" => "pieds"),
    "knots" => array("EN" => "knots", "FR" => "nœuds"),
    "scale-slider" => array("EN" => "Scale Slider", "FR" => "Échelle"),
    "calc-completed" => array("EN" => "Calculation completed", "FR" => "Calcul terminé"),
    "special-prms" => array("EN" => "Special parameters?", "FR" => "Des paramètres spéciaux ?"),
    "distinguish" => array("EN" => "Highlight the days where the tide is...", "FR" => "Distinguer les jours où la marée est..."),
    "distinguish-legend" => array("EN" => "Legend: Days where the tide is", "FR" => "Légende : Les jours où la marée est"),
    "high" => array("EN" => "High", "FR" => "Haute"),
    "low" => array("EN" => "Low", "FR" => "Basse"),
    "between" => array("EN" => "Between", "FR" => "Entre"),
    "and" => array("EN" => "and", "FR" => "et")
);

function translate (string $lang, string $textId) : string {
    global $translations;

    if (false) {
        echo ("Translation requested for [" . $textId . "] in [" . $lang . "]<br/>" . PHP_EOL);
    }

    $translated = $translations[$textId][$lang];
    return $translated;
}

$ENGLISH_MONTHS = array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
$ENGLISH_DOW = array("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

$FRENCH_MONTHS = array("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre");
$FRENCH_DOW = array("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

$DATE_FMT_DOW_DAY_MONTH_YEAR = 0;
$DATE_FMT_FULL_MONTH_YEAR = 1;

function englishDate(DateTime $date, ?int $option=0) : string {
    global $ENGLISH_MONTHS, $ENGLISH_DOW;
    global $DATE_FMT_DOW_DAY_MONTH_YEAR, $DATE_FMT_FULL_MONTH_YEAR;

    $dow =   (int)$date->format("N");
    $month = (int)$date->format("m");
    $year =  (int)$date->format("Y");
    $day =   (int)$date->format("d");

    $englishDate = "";
    switch ($option) {
        case $DATE_FMT_DOW_DAY_MONTH_YEAR:
            $englishDate = $ENGLISH_DOW[$dow - 1] . " " . $ENGLISH_MONTHS[$month - 1] . " " . $day . ", " . $year;
            break;
        case $DATE_FMT_FULL_MONTH_YEAR:
            $englishDate = $ENGLISH_MONTHS[$month - 1] . " " . $year;
            break;
    }
    return $englishDate;
}

function frenchDate(DateTime $date, ?int $option=0) : string {
    global $FRENCH_MONTHS, $FRENCH_DOW;
    global $DATE_FMT_DOW_DAY_MONTH_YEAR, $DATE_FMT_FULL_MONTH_YEAR;

    $dow =   (int)$date->format("N");
    $month = (int)$date->format("m");
    $year =  (int)$date->format("Y");
    $day =   (int)$date->format("d");

    $frenchDate = "";
    switch ($option) {
        case $DATE_FMT_DOW_DAY_MONTH_YEAR:
            $frenchDate = $FRENCH_DOW[$dow - 1] . " " . $day . " " . $FRENCH_MONTHS[$month - 1] . " " . $year;
            break;
        case $DATE_FMT_FULL_MONTH_YEAR:
            $frenchDate = $FRENCH_MONTHS[$month - 1] . " " . $year;
            break;
    }
    return $frenchDate;
}

function translateDate(string $lang, DateTime $date, ?int $option=0) : string {
    if ($lang == "FR") {
        return frenchDate($date, $option);
    } else {
        return englishDate($date, $option);
    }
}

function getCoeffData (BackEndSQLiteTideComputer $backend, Constituents $constituentsObject, array $stationsData, int $year, int $month, int $day, ?string $tz2Use) : array {
    $brestTideStation = $backend->findTideStation("Brest, France", $year, $constituentsObject, $stationsData);
    // assert (brestTideStation != null);
    $brestTable = TideUtilities::getTideTableForOneDay($brestTideStation, $constituentsObject->getConstSpeedMap(), $year, $month, $day, $tz2Use);
    $coeffsInBrest = TideUtilities::getCoeffInBrest($brestTideStation, $brestTable);
    return $coeffsInBrest;
}

// For decToSex
$NS = 1;
$EW = 2;

function isDateBetween(DateTime $dateToCheck, DateTime $fromDate, DateTime $toDate) : bool {
    if (false) {
        echo ("Is  [" . $dateToCheck->format("H:i") . "] between " . $fromDate->format("H:i"). " and " .$toDate->format("H:i") . "?<br/>" . PHP_EOL);
    }
    $result = false;
    if ($dateToCheck >= $fromDate && $dateToCheck <= $toDate) {
        $result = true;
    }
    return $result;
}

/**
 * Publish for one month
 */
function publishAlmanac(string $stationName,
                        int $year,
                        int $month,
                        BackEndSQLiteTideComputer $backend,
                        Constituents $constituentsObject,
                        array $stationsData,
                        ?string $tideType=null,
                        ?string $fromTime=null,
                        ?string $toTime=null) : string {

    global $VERBOSE, $lang;
    global $DATE_FMT_DOW_DAY_MONTH_YEAR, $DATE_FMT_FULL_MONTH_YEAR;
    global $NS, $EW;

    $fromDateTime = null;
    $toDateTime = null;
    $extraPrms = false;
    if ($fromTime != null && $toTime != null) {
        $extraPrms = true;
        $fromDateTime = DateTime::createFromFormat("H:i", $fromTime);
        $toDateTime = DateTime::createFromFormat("H:i", $toTime);
    }

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
        // Dsplay Table for 1 month...
        $content .= ("<p>" . PHP_EOL);
        $content .= ("<span style='font-size: 1.5rem;'><b>" . $theTideStation->getFullName() . "</b>, " .
                        decToSex($theTideStation->getLatitude(), "NS") . " / " . decToSex($theTideStation->getLongitude(), "EW") . ", TZ : " .
                        $theTideStation->getTimeZone() . ", " . translate($lang, "base-height") . " : " .
                        $theTideStation->getBaseHeight() . " " . translate($lang, $theTideStation->getDisplayUnit()) . "<span><br/>" . PHP_EOL);
        $content .= ("<span style='font-size: 2.0rem; font-style: italic; font-weight: bold;'>" .
                        translateDate($lang, DateTime::createFromFormat("Y-m", sprintf("%04d-%02d", $year, $month)), $DATE_FMT_FULL_MONTH_YEAR) . "</span><br/>" . PHP_EOL);

        if ($extraPrms) {
            $content .= ("<span style='font-size: 1.2rem; background-color: yellow; print-color-adjust: exact;'>" .
                            translate($lang, "distinguish-legend") . " " .
                            ( $tideType == "high-tide" ? translate($lang, "high") : translate($lang, "low") ) . " " .
                            translate($lang, "between") . " " . $fromDateTime->format("H:i") . " " .
                            translate($lang, "and") . " " . $toDateTime->format("H:i") . "</span><br/>" . PHP_EOL);
        }
        $content .= ("<table>" . PHP_EOL);
        $colCounter = 0;
        $nbCol = 4;
        while ($colCounter < count($arrayKeys)/* && $colCounter < 10*/) {
            $content .= ("<tr>" . PHP_EOL);
            for ($j=0; $j<$nbCol; $j++) {
                if ($colCounter < count($arrayKeys)) {
                    $dateTime = DateTime::createFromFormat("Y-m-d", $arrayKeys[$colCounter]); // , $tz);

                    $tideData = $monthTable[$arrayKeys[$colCounter]]["tide.data"];
                    $moonPhase = $monthTable[$arrayKeys[$colCounter]]["moon.phase"];

                    $bgColor = ''; // $bgColor = ' background-color: lime;';
                    if ($extraPrms) {
                        if (false) {
                            echo("Processing extra prms for type " . $tideType . " between " . $fromTime . " and " .$toTime . "<br/>" . PHP_EOL);
                        }
                        $highlightDay = false;
                        if ($tideType == "high-tide") {
                            for ($k=0; $k<count($tideData); $k++) {
                                $highOrLow = $tideData[$k]->getType(); // HW/LW
                                if ($highOrLow == "HW") {
                                    $tideTime = $tideData[$k]->getCalendar()->format("H:i");
                                    $tideDateTime = DateTime::createFromFormat("H:i", $tideTime); // The same day
                                    if (isDateBetween($tideDateTime, $fromDateTime, $toDateTime)) {
                                        $highlightDay = true;
                                        if (false) {
                                            echo ("--> Highlighting day " . $dateTime->format("Y-m-d") . "<br/>" . PHP_EOL);
                                        }
                                    }
                                }
                            }
                            if ($highlightDay) {
                                $bgColor = ' background-color: yellow;';
                            }
                        } elseif ($tideType == "low-tide") {
                            for ($k=0; $k<count($tideData); $k++) {
                                $highOrLow = $tideData[$k]->getType(); // HW/LW
                                if ($highOrLow == "LW") {
                                    $tideTime = $tideData[$k]->getCalendar()->format("H:i");
                                    $tideDateTime = DateTime::createFromFormat("H:i", $tideTime); // The same day
                                    if (isDateBetween($tideDateTime, $fromDateTime, $toDateTime)) {
                                        $highlightDay = true;
                                        if (false) {
                                            echo ("--> Highlighting day " . $dateTime->format("Y-m-d") . "<br/>" . PHP_EOL);
                                        }
                                    }
                                }
                            }
                            if ($highlightDay) {
                                $bgColor = ' background-color: yellow;';
                            }
                        }
                        // $bgColor = ' background-color: lime;';
                    }
                    if (false) {
                        echo ("Processing day " . $dateTime->format("Y-m-d") . " with bgColor=[" . $bgColor . "]<br/>" . PHP_EOL);
                    }

                    $content .= ("<td style='vertical-align: top;$bgColor;'>" . PHP_EOL);
                    // Inner table
                    $content .= ("<table>" . PHP_EOL);
                    $content .= (  "<tr><td colspan='6'><b>" . translateDate($lang, $dateTime, $DATE_FMT_DOW_DAY_MONTH_YEAR) . "</b></td></tr>" . PHP_EOL);
                    $content .= (  "<tr><th></th><th>" . translate($lang, "time") . "</th><th>" . translate($lang, "height") . "</th><th>" . translate($lang, "unit") . "</th><th>" . translate($lang, "coeff") . "</th></tr>" . PHP_EOL);

                    for ($k=0; $k<count($tideData); $k++) {
                        $content .= ("<tr>" . PHP_EOL);
                        $content .= (  "<td><b>" . translate($lang, $tideData[$k]->getType()) . "</b></td>" .
                                "<td>" . $tideData[$k]->getCalendar()->format("H:i") . "</td>" .
                                "<td>" . sprintf("%.02f", $tideData[$k]->getValue()) . "</td>" .
                                "<td>" . translate($lang, $tideData[$k]->getUnit()) . "</td>" .
                                "<td style='text-align: center;'>" . ($tideData[$k]->getCoeff() != 0 ? sprintf("%02d", $tideData[$k]->getCoeff()) : "") . "</td>" .
                                ($k == 0 ? "<td rowspan='4'><img src='" . $moonPhase . "'/></td>" : "") . PHP_EOL);
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

// TODO Add 1 month, 2 months, 3 months, 6 months...
function publishStationDuration(string $stationName, int $year, ?int $month=null, ?string $tideType=null, ?string $fromTime=null, ?string $toTime=null) : void {
    global $VERBOSE, $lang;

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

    ?>
    <p>
        <button onclick="history.back()"><?php echo translate($lang, "go-back"); ?></button>
    </p>
    <p>
        <!-- Scale slider. WiP. Still some CSS work to do for the margins when scaled -->
        <div style="display: grid; grid-template-columns: 10% auto 10%;">
            <span><?php echo translate($lang, "scale-slider"); ?></span>
            <input type="range" value="90" min="0" max="100" step="1" style="width: 100%;"
                   oninput="setDocScale.call(this, event); docscale.value = this.value + '%';"/>
            <output name="docscale" id="docscale" style="color: navy; text-align: left; margin-left: 10px;">90%</output>
        </div>
    </p>
    <?php

    // $year = (int)date("Y"); // gmdate ?
    // $month = (int)date("m");
    $content = "";
    if ($month != null) {
        $content = publishAlmanac($stationName, $year, $month, $backend, $constituentsObject, $stationsData, $tideType, $fromTime, $toTime);
    } else {
        // One year, loop.
        $content = "";
        for ($m=1; $m<=12; $m++) {
            $content .= publishAlmanac($stationName, $year, $m, $backend, $constituentsObject, $stationsData, $tideType, $fromTime, $toTime);
            $content .= ("<div class='page-break'></div>" . PHP_EOL);
        }
    }
    // Completion date
    $UTdate = microtime(true);
    // echo ("microtime: " . $UTdate . "<br/>");
    // var_dump($UTdate);
    // echo ("<br/>");
    $now = DateTime::createFromFormat('U.u', $UTdate); // UTC
    echo(translate($lang, "calc-completed") .  " " . $now->format("d-M-Y H:i:s") . " (UTC).<br/>" . PHP_EOL);
    echo("<hr/>" . PHP_EOL);

    echo("</div>" . PHP_EOL); // Close the screen-only div
    echo("<div id='result-to-publish' class='scaled'>" . PHP_EOL);
    echo($content); // The expected result
    // echo("</div>"); // Close at the end of the doc

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
    $stationList = $backend->getStationList($pattern, true); // Tide Stations only

    $backend->closeDB();
    if ($VERBOSE) {
        echo("Connection closed.<br/>". PHP_EOL);
    }
    return $stationList;
}

function blankScreen() : void {
    global $lang;
    ?>
    <h2><?php echo translate($lang, "choose-station"); ?></h2>
    <form action="<?php echo basename(__FILE__); ?>"
          method="get"
          name="formStation"
          style="padding:0; margin:0">
        <input type="hidden" name="lang" value="<?php echo $lang; ?>">

        <?php echo translate($lang, "part-of-name"); ?>:
        <input type="text" size="40" name="pattern" placeholder="Name pattern" title="Joker is '%'.&#13;Enter '%' for all stations" required>
        <br/>
        <input type="submit" value="Submit">
    </form>
    <?php
}

function selectStationAndDuration(array $list) : void {
    global $lang;
    echo("Selected " . count($list) . " station(s)<br/>" . PHP_EOL);

    if (count($list) == 0) {
        echo("No station selected... Try again.<br/>" . PHP_EOL);
    ?>
    <p>
        <button onclick="history.back()"><?php echo translate($lang, "go-back"); ?></button>
    </p>
    <?php
    } else {
    ?>
    <p>
        <button onclick="history.back()"><?php echo translate($lang, "go-back"); ?></button>
    </p>
    <div class="final-form"> <!-- The div for the final form -->
        <h2><?php echo translate($lang, "choose-station-2"); ?></h2>

        <script type="text/javascript">
            let updateMonthField = (item) => {
                console.log(`Duration is now ${item.value}`);
                document.getElementById('month-list').disabled = (item.value === 'YEAR');
            };
            let sendAck = (form) => {
                // console.log("Generating ack");
                // debugger;
                let lang = form.querySelector("input[name = 'lang']").value;
                // let station = form.querySelector("select[name = 'station-name']").value;
                let station = form.querySelector("select[name = 'station-name']").querySelector('option:checked').innerText
                let duration = form.querySelector("select[name = 'duration']").value;

                let message = "";
                if (lang === 'FR') {
                    message = `Votre requête pour "${station}", sur une durée d'un ${(duration === 'YEAR') ? 'an' : 'mois'}, est en cours de traitement.\nSoyez patient, ça peut prendre du temps...`;
                } else {
                    message = `Your request for "${station}", on one ${(duration === 'YEAR') ? 'year' : 'month'}, is being processed.\nBe patient, it may take some time...`;
                }
                setTimeout(() => { // Non-blocking alert. TODO A custom dialog (with the header I want)
                    alert(message);
                }, 1);
            };
        </script>

        <form action="<?php echo basename(__FILE__); ?>"
            onsubmit="sendAck(this);"
            method="get"
            id="formStation"
            style="padding:0; margin:0">
            <input type="hidden" name="lang" value="<?php echo $lang; ?>">
            <table>
                <tr>
                    <td><?php echo translate($lang, "station"); ?></td>
                    <td>
                        <select name="station-name" form="formStation">
        <?php
                        for ($i=0; $i<count($list); $i++) {
                            // str_replace("'", "&quote;", $list[$i])
                            echo("<option value='" . $list[$i] . "'>" . $list[$i] . "</option>" . PHP_EOL);
                        }
        ?>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><?php echo translate($lang, "duration"); ?></td>
                    <td>
                        <!--input type="hidden" name="duration" value="MONTH"-->
                        <select name="duration" form="formStation" onchange="updateMonthField(this);">
                            <option value="MONTH"><?php echo translate($lang, "one-month"); ?></option>
                            <option value="YEAR"><?php echo translate($lang, "one-year"); ?></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><?php echo translate($lang, "year"); ?></td>
                    <td>
                        <input type="number" name="year" value="<?php echo date("Y"); ?>">
                    </td>
                </tr>
                <tr>
                    <td><?php echo translate($lang, "month"); ?></td>
                    <td>
                        <select id="month-list" name="month" form="formStation"> <!-- defaulted to current month -->
                            <option value="1"<?php echo ((date("m") == 1) ? " selected" : "") ?>><?php echo translate($lang, "january"); ?></option>
                            <option value="2"<?php echo ((date("m") == 2) ? " selected" : "") ?>><?php echo translate($lang, "february"); ?></option>
                            <option value="3"<?php echo ((date("m") == 3) ? " selected" : "") ?>><?php echo translate($lang, "march"); ?></option>
                            <option value="4"<?php echo ((date("m") == 4) ? " selected" : "") ?>><?php echo translate($lang, "april"); ?></option>
                            <option value="5"<?php echo ((date("m") == 5) ? " selected" : "") ?>><?php echo translate($lang, "may"); ?></option>
                            <option value="6"<?php echo ((date("m") == 6) ? " selected" : "") ?>><?php echo translate($lang, "june"); ?></option>
                            <option value="7"<?php echo ((date("m") == 7) ? " selected" : "") ?>><?php echo translate($lang, "july"); ?></option>
                            <option value="8"<?php echo ((date("m") == 8) ? " selected" : "") ?>><?php echo translate($lang, "august"); ?></option>
                            <option value="9"<?php echo ((date("m") == 9) ? " selected" : "") ?>><?php echo translate($lang, "september"); ?></option>
                            <option value="10"<?php echo ((date("m") == 10) ? " selected" : "") ?>><?php echo translate($lang, "october"); ?></option>
                            <option value="11"<?php echo ((date("m") == 11) ? " selected" : "") ?>><?php echo translate($lang, "november"); ?></option>
                            <option value="12"<?php echo ((date("m") == 12) ? " selected" : "") ?>><?php echo translate($lang, "december"); ?></option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><?php echo translate($lang, "special-prms"); ?></td><td><input name="extra-prm" type="checkbox" onchange="specialOptions(this);"></td>
                </tr>
                <tr id="special-prm-table" style="display: none;">
                    <td colspan="2">
                        <table style="width: 100%;">
                            <tr>
                                <td colspan="2"><?php echo translate($lang, "distinguish") ?></td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <select name="tide-type" form="formStation">
                                        <option value="high-tide"><?php echo translate($lang, "high") ?></option>
                                        <option value="low-tide"><?php echo translate($lang, "low") ?></option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <?php echo translate($lang, "between") ?> <input type="time" name="from-time" value="00:00">
                                </td>
                                <td>
                                    <?php echo translate($lang, "and") ?> <input type="time" name="to-time" value="23:59">
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            <div class="final-submit">
                <input type="submit" value="Submit">
            </div>
        </form>
    </div>
    <?php
    }
}

function selectDuration(string $stationName) : void {
    // Unused...
}

function checkChronology(string $fromTime, string $toTime) : void {
    global $lang;

    $fromDateTime = DateTime::createFromFormat("H:i", $fromTime);
    $toDateTime = DateTime::createFromFormat("H:i", $toTime);

    if ($fromDateTime >= $toDateTime) {
        if ($lang == 'FR') {
            echo("Le temps de début doit être antérieur au temps de fin.<br/>" . PHP_EOL);
        } else {
            echo("The from-time must be earlier than the to-time.<br/>" . PHP_EOL);
        }
    }
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
    include __DIR__ . '/../../astro.php/celestial.computer/autoload.php'; // Modify at will !!

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    /**
     * URL Like http://.../tide.publisher.101.php?station-name=Port-Tudy&duration=MONTH&year=2024&month=12 optional &lang=FR
     *          http://.../tide.publisher/tide.publisher.101.php?lang=FR&station-name=Port-Tudy%2C+France&duration=MONTH&year=2025&month=11&extra-prm=on&tide-type=high-tide&from-time=10%3A00&to-time=14%3A00
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
    if (isset($_GET['lang'])) {
        $lang = $_GET['lang'];
    }
    // extra-prm=on&tide-type=high-tide&from-time=10%3A00&to-time=14%3A00
    if (isset($_GET['extra-prm']) && $_GET['extra-prm'] == 'on') {
        $extraPrm = true;
        if (isset($_GET['tide-type'])) {
            $tideType = $_GET['tide-type']; // high-tide | low-tide
        }
        if (isset($_GET['from-time'])) {
            $fromTime = $_GET['from-time']; // HH:MM
        }
        if (isset($_GET['to-time'])) {
            $toTime = $_GET['to-time']; // HH:MM
        }
    } else {
        $extraPrm = false;
    }

?>
    <!-- Language switch -->
     Language switch. Current lang is <?php echo $lang; ?>.
    <form id="lang-swicth" action="<?php echo basename(__FILE__); ?>" method="get" name="lang-switch">
        <input type="radio" onchange="this.form.submit();" id="fr" name="lang" value="FR"<?php echo(($lang == 'FR') ? " checked" : ""); ?>>
        <label for="fr">FR</label><br/>
        <input type="radio" onchange="this.form.submit();" id="en" name="lang" value="EN"<?php echo(($lang == 'EN') ? " checked" : ""); ?>>
        <label for="en">EN</label><br/>
    </form>
    <hr/>
<?php

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
    // TODO from-to time, high/low tide
    if ($extraPrm) {
        // TODO Handle special parameters
        // $tideType, $fromTime, $toTime
        echo("Special parameters requested: " . $tideType . ", from " . $fromTime . " to " . $toTime . "<br/>" . PHP_EOL);
        checkChronology($fromTime, $toTime);
    }

    if ($option == -1) {
        echo ("WTF???<br/>" . PHP_EOL);
    } else if ($option == $BLANK_SCREEN) {
        // echo ("Blank Screen<br/>" . PHP_EOL);
        blankScreen();
    } else if ($option == $PICK_UP_STATION) {
        // echo ("Pick-up Station<br/>" . PHP_EOL);
        // $pattern = "Brest";
        $list = getStationsList($pattern);
        selectStationAndDuration($list);
    } else if ($option == $SELECT_DURATION) {
        // echo ("Select Duration<br/>" . PHP_EOL);
        selectDuration($stationName); // Done in the above, probably useless.
    } else if ($option == $PUBLISH) {
        // like publishStationDuration("Port-Tudy", 2024, 12);
        // With extraPrms if needed
        if ($publishDuration == 'MONTH') {
            if ($extraPrm) {
                echo("Special parameters will be applied.<br/>" . PHP_EOL);
                publishStationDuration($stationName, $publishYear, $publishMonth, $tideType, $fromTime, $toTime);
            } else {
                publishStationDuration($stationName, $publishYear, $publishMonth);
            }
        } else {
            if ($extraPrm) {
                echo("Special parameters will be applied.<br/>" . PHP_EOL);
                publishStationDuration($stationName, $publishYear, null, $tideType, $fromTime, $toTime);
            } else {
                publishStationDuration($stationName, $publishYear);
            }
        }
    }

} catch (Throwable $plaf) {
    // See https://www.php.net/manual/en/language.constants.magic.php
    echo "[Captured Throwable (big loop) for " . __FILE__ . " : " . $plaf . "] " . PHP_EOL;
}
?>
</div>
<div class="screen-only">
    <hr/>
    <i>&copy; Passe-Coque, 2025</i>
</div>
</body>
</html>