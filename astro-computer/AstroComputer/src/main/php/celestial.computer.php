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
 }

function doYourJob(bool $verbose) : string {

    $ac = new AstroComputer(); // See below

    try {
        // Quick test
        try {
            // Core test

            $year = (int)date("Y");
            $month = (int)date("m");
            $day = (int)date("d");
            $hours = (int)date("H");
            $minutes = (int)date("i");
            $seconds = (int)date("s");

            $container = new Container();

            // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
            $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true);
            $context2 = $ac->getContext();
            // echo ("From calculate: EoT:" . $context2->EoT . " ");
        
            $container->ctx = $context2;
            $container->deltaT = $ac->getDeltaT();
            $container->calcDate = "$year:$month:$day $hours:$minutes:$seconds UTC";

            $jsonData = json_encode($container);

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

try {
    $data = doYourJob($VERBOSE);
    header('Content-Type: application/json; charset=utf-8');
    // echo json_encode($data); // This is for text (not json)
    echo $data;
    http_response_code(200);
} catch (Throwable $e) {
    echo "[Captured Throwable (3) for celestial.computer.php : " . $e . "] " . PHP_EOL;
}
?>
