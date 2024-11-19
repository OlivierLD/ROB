<?php

include __DIR__ . '/autoload.php';


$VERBOSE = false;

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
    try {
        // Current dateTime
        $year = (int)date("Y");
        $month = (int)date("m");
        $day = (int)date("d");
        $hours = (int)date("H");
        $minutes = (int)date("i");
        $seconds = (int)date("s");

        $container = "<ul>" . PHP_EOL;

        // Astro Computer basic test
        $ac = new AstroComputer(); 
        // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
        $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true);
        $context2 = $ac->getContext();
        // echo ("From calculate: EoT:" . $context2->EoT . " ");

        $container .= ("<li>Calulated at $year:$month:$day $hours:$minutes:$seconds UTC</li>" . PHP_EOL);
        $container .= ("<li>DeltaT: " . $ac->getDeltaT() . " s</li>" . PHP_EOL);

        // End of Basic Test

        if ($verbose) {
            echo("Invoking SightReductionUtil...<br/>");
        }
        $lat = 43.0; $lng = -3.0;
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
        // echo json_encode($data); // This is for text (not json)
        echo $data;
        // http_response_code(200);
    } catch (Throwable $e) {
        echo "[Captured Throwable (4) for celestial.computer.php : " . $e . "] " . PHP_EOL;
    }
} else { // TODO Invoke the SightReductionUtil
    echo "Option is [$option], not supported.";
}
?>
