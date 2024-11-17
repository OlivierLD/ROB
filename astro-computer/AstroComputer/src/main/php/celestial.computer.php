<?php

include __DIR__ . '/autoload.php';


$VERBOSE = false;

/*
 * There will be some code here...
 * We are going to produce a JSON structure, that is then going to be fetched
 * from some ES6 code...
 */

function doYourJob(bool $verbose): string {


    try {
        $json_result = "[";


        $json_result .= "{ \"text\": \"Function doYourJob\"}, ";

        // Quick test
        try {
            // $util = new Utils();
            $data = 60.0;
            // echo ("COSD $data degrees = " . Utils::cosd($data));
            $json_result .= ("{ \"cosd\" : " . Utils::cosd($data) . "}, ");
    
        } catch (Throwable $e) {
            echo "[ Captured Throwable for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
            throw $e;
        }
    
        // . . .
        $json_result .= "{ \"akeu\": \"coucou\" }";

        $json_result .= "]";
        return $json_result;

    } catch (Throwable $e) {
        echo "[ Captured Throwable for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
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
    echo "[Captured Throwable for celestial.computer.php : " . $e . "] " . PHP_EOL;
}
?>
