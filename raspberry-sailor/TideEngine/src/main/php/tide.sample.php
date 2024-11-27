<?php

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/tide.computer/autoload.php';

    $VERBOSE = false;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    $backend = new BackEndSQLiteTideComputer();
    echo("Backend created.<br/>". PHP_EOL);

    // $backend->getStationData();

    $backend->connectDB("./sql/tides.db");
    echo("Connection created.<br/>". PHP_EOL);

    echo("Executing getStationData...<br/>". PHP_EOL);
    $stationData = $backend->getStationData();
    // var_dump($stationData);
    // echo("<br/>" . PHP_EOL);
    echo("getStationData executed, " . count($stationData) . " element(s).<br/>". PHP_EOL);

    // Find Port-Tudy...
    $portTudyStation = null;
    for ($i=0; $i<count($stationData); $i++) {
        // if (str_contains($stationData[$i]->getFullName(), "Port-Tudy")) { // PhP 8...
        if (strpos($stationData[$i]->getFullName(),  "Port-Tudy") !== false) {
            $portTudyStation = $stationData[$i];
            break;
        }
    }
    if ($portTudyStation == null) {
        echo("Port-Tudy was not found...<br/>" . PHP_EOL);
    } else {
        // var_dump($portTudyStation);
        if ($portTudyStation->isCurrentStation()) {
            echo("Port-Tudy IS a current station.<br/>" . PHP_EOL);
        } else {
            echo("Port-Tudy IS NOT a current station.<br/>" . PHP_EOL);
            echo("Displa Unit: " . $portTudyStation->getDisplayUnit() . "<br/>" . PHP_EOL);
        }
    }

    $backend->closeDB();
    echo("Connection closed.<br/>". PHP_EOL);

    echo("Test Completed.<br/>". PHP_EOL);

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for tide.sample.php : " . $plaf . "] " . PHP_EOL;
}
?>
