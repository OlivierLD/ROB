<?php

try {
// phpinfo();

include __DIR__ . '/autoload.php';


$VERBOSE = false;

$phpVersion = (int)phpversion()[0];
if ($phpVersion < 8) {
    echo("PHP Version is " . phpversion() . "... This might be too low.");
}

/*
 * This is a layer on top of the AstroComputer
 * 
 * We are going to produce a JSON structure, that is then going to be fetched
 * from some ES6 code...
 */

class Container {
    public $deltaT;
    public $calcDate;
    public $ctx;
    public $oneStar;
    public $starGHA;
    public $starCatalog;
}

function doYourJob(bool $verbose) : string {

    try {
        // Quick test
        try {
            // Core test

            // Current dateTime
            $year = (int)date("Y");
            $month = (int)date("m");
            $day = (int)date("d");
            $hours = (int)date("H");
            $minutes = (int)date("i");
            $seconds = (int)date("s");

            $container = new Container();

            // Astro Computer basic test
            $ac = new AstroComputer(); 
            // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
            $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true);
            $context2 = $ac->getContext();
            // echo ("From calculate: EoT:" . $context2->EoT . " ");

            $ALDEBARAN = "Aldebaran";
			$star = Star::getStar($ALDEBARAN);
			// assertTrue(String.format("%s not found in Star Catalog", ALDEBARAN), star != null);

			$ac->starPos($ALDEBARAN);
			$starGHA = $ac->getStarGHA($ALDEBARAN);

            $container->ctx = $context2; // The full Context
            $container->deltaT = $ac->getDeltaT();
            $container->calcDate = "$year:$month:$day $hours:$minutes:$seconds UTC";
            $container->oneStar = Star::getStar("Zubenelgenubi");
            $container->starCatalog = Star::getCatalog();
            $container->starGHA = $starGHA;

            $jsonData = json_encode($container); // , JSON_FORCE_OBJECT);
            // End of Basic Test

            if ($verbose) {
                echo("Invoking SightReductionUtil...<br/>");
            }
            $sru = new SightReductionUtil(
                $ac->getSunGHA(),
                $ac->getSunDecl(),
                43.0,
                -3); // 0, 0, 0, 0);
            $sru->calculate();
            if ($verbose) {
                echo("He:" . Utils::decToSex($sru->getHe()) . ", Z:" . sprintf("%f&deg;", $sru->getZ()) . "<br/>");
                echo("Done invoking SightReductionUtil.<br/>");
            }
        } catch (Throwable $e) {
            if ($verbose) {
                echo "[ Captured Throwable (2) for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
            }
            throw $e;
        }
    
        // Final one
        return $jsonData;

    } catch (Throwable $e) {
        echo "[ Captured Throwable (1) for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
        throw $e;
    }
    return null;
}

function moreSpecific_1(bool $verbose) : string {
    // Sun current status
    try {
        // Current dateTime
        $year = (int)date("Y");
        $month = (int)date("m");
        $day = (int)date("d");
        $hours = (int)date("H");
        $minutes = (int)date("i");
        $seconds = (int)date("s");

        $container = "<h4>Sun current status</h4>" . PHP_EOL;
        $container .= "<ul>" . PHP_EOL;

        // Astro Computer basic test
        $before = microtime(true); // See https://www.w3schools.com/php/func_date_microtime.asp
        $ac = new AstroComputer(); 
        // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
        $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true);
        $after = microtime(true);

        $timeDiff = ($after - $before) * 1000;
        $container .= ("<li>Calculated in " . sprintf("%f ms", $timeDiff)  .  " (" . sprintf("From %f to %f", $before, $after)  . ")</li>" . PHP_EOL);

        $context2 = $ac->getContext();
        // echo ("From calculate: EoT:" . $context2->EoT . " ");

        $container .= ("<li>Calculated at $year:$month:$day $hours:$minutes:$seconds UTC</li>" . PHP_EOL);
        $container .= ("<li>DeltaT: " . $ac->getDeltaT() . " s</li>" . PHP_EOL);
        $container .= ("<li>Sun GHA: " . Utils::decToSex($ac->getSunGHA(), Utils::$NONE) . ", Sun Dec: " . Utils::decToSex($ac->getSunDecl(), Utils::$NS) . "</li>" . PHP_EOL);

        if ($verbose) {
            echo("Invoking SightReductionUtil...<br/>");
        }
        $lat = 43.677667; $lng = -3.135667;
        $sru = new SightReductionUtil(
            $ac->getSunGHA(),
            $ac->getSunDecl(),
            $lat,
            $lng);
        $sru->calculate();
        if ($verbose) {
            echo("He:" . Utils::decToSex($sru->getHe()) . ", Z:" . sprintf("%f&deg;", $sru->getZ()) . "<br/>");
            echo("Done invoking SightReductionUtil.<br/>");
        }
        $container .= ("<li>From Pos: " . Utils::decToSex($lat, Utils::$NS) . " / " . Utils::decToSex($lng, Utils::$EW) . "</li>" . PHP_EOL);
        $container .= ("<li>Sun He:" . Utils::decToSex($sru->getHe()) . ", Sun Z:" . sprintf("%f&deg;", $sru->getZ()) . "</li>" . PHP_EOL);

        $container .= ("</ul>" . PHP_EOL);
        $container .= ("<hr/>" . PHP_EOL);

    } catch (Throwable $e) {
        if ($verbose) {
            echo "[ Captured Throwable (2) for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
        }
        throw $e;
    }

    // Final one
    return $container;
}

function oneDayAlmanac(bool $verbose) : string {
    try {
        // Current dateTime
        $year = (int)date("Y");
        $month = (int)date("m");
        $day = (int)date("d");
        $hours = (int)date("H");
        $minutes = (int)date("i");
        $seconds = (int)date("s");

        $nbDaysThisMonth = TimeUtil::getNbDays($year, $month);
        // echo("This month, $nbDaysThisMonth days.<br/>" . PHP_EOL);

        $container = ("<p>Calculated at $year:$month:$day $hours:$minutes:$seconds UTC</p>" . PHP_EOL);
        // date("l jS \of F Y h:i:s A"). See https://www.w3schools.com/php/func_date_date.asp
        $container .= "<div class='sub-title'>Sun almanac for " . date("l F jS, Y") .  "</div>" . PHP_EOL;
        $container .= "<table>" . PHP_EOL;
        $container .= "<tr><th>UT</th><th>Sun GHA</th><th>&delta; GHA</th><th>Sun RA</th><th>Sun Decl</th><th>Aries GHA</th></tr>" . PHP_EOL;
        $container .= "<tr><th>TU</th><th>Soleil AHvo</th><th>&delta; AHvo</th><th>Soleil AHso</th><th>Soleil Decl</th><th>Pt Vernal AHso</th></tr>" . PHP_EOL;

        // Astro Computer
        $ac = new AstroComputer(); 

        $prevGHA = null;

        for ($i=0; $i<24; $i++) {
            $h = $i;
            $ac->calculate($year, $month, $day, $h, 0, 0, true);
            $context2 = $ac->getContext();
            $deltaGHA = "";
            if ($prevGHA != null) {
                $diff = $ac->getSunGHA() - $prevGHA;
                while ($diff < 0) {
                    $diff += 360;
                }
                $deltaGHA = sprintf("%1\$.4f&deg;", $diff);
            }
            $prevGHA = $ac->getSunGHA();
            $container .= ("<tr><td>" . sprintf("%02d", $h) . 
                               "</td><td>" . Utils::decToSex($ac->getSunGHA()) . 
                               "</td><td>" . $deltaGHA . 
                               "</td><td>" . Utils::decToSex($ac->getSunRA(), Utils::$NONE) . 
                               "</td><td>" . Utils::decToSex($ac->getSunDecl(), Utils::$NS) . 
                               "</td><td>" . Utils::decToSex($ac->getAriesGHA(), Utils::$NONE) .
                               "</td></tr>" . PHP_EOL); 
        }
        // End of Test

        $container .= ("</table>" . PHP_EOL);
        $container .= ("<hr/>" . PHP_EOL);

        return $container;

    } catch (Throwable $e) {
        if ($verbose) {
            echo "[ Captured Throwable (2) for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
        }
        throw $e;
    }
}

$option = "basic";
// Whatever you want it to be 
if (isset($_GET['option'])) {
    $option = $_GET['option'];
}

if ($option == "basic") {
    try {
        $data = doYourJob($VERBOSE);
        header('Content-Type: application/json; charset=utf-8');
        // echo json_encode($data); // This is for text (not json)
        echo $data;
        // http_response_code(200);
    } catch (Throwable $e) {
        echo "[Captured Throwable (3) for celestial.computer.php : " . $e . "] " . PHP_EOL;
    }
} else if ($option == "1") {
    try {
        $data = moreSpecific_1($VERBOSE);
        header('Content-Type: text/html; charset=utf-8');
        echo $data;
        // http_response_code(200);
    } catch (Throwable $e) {
        echo "[Captured Throwable (4) for celestial.computer.php : " . $e . "] " . PHP_EOL;
    }
} else if ($option == "2") {
    try {
        $data = oneDayAlmanac($VERBOSE);
        header('Content-Type: text/html; charset=utf-8');
        echo $data;
        // http_response_code(200);
    } catch (Throwable $e) {
        echo "[Captured Throwable (5) for celestial.computer.php : " . $e . "] " . PHP_EOL;
    }
} else if ($option == "info") {
    phpinfo();
} else { 
    echo "Option is [$option], not supported.<br/>";
}

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for celestial.computer.php : " . $plaf . "] " . PHP_EOL;
}
?>
