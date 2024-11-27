<?php 

// Unused. Fails in memory.

// Constituents
// Read the JSON file
$jsonConstituents = file_get_contents('json/constituents.json'); 
// Check if the file was read successfully
if ($jsonConstituents === false) {
    die('Error reading the Constituents JSON file');
}
// Decode the JSON file
$constituents_json_data = json_decode($jsonConstituents, true); 
// Check if the JSON was decoded successfully
if ($constituents_json_data === null) {
    die('Error decoding the Constituents JSON file');
}

echo("Constituents Map has " . count($constituents_json_data) . " entries.<br/>" . PHP_EOL);

if (false) {
    // Display data
    echo "<pre>";
    print_r($constituents_json_data);
    echo "</pre>";
}

// Stations
// Read the JSON file
$jsonStations = file_get_contents('json/stations.json'); 
// Check if the file was read successfully
if ($jsonStations === false) {
    die('Error reading the Stations JSON file');
}
// Decode the JSON file
$stations_json_data = json_decode($jsonStations, true); 
// Check if the JSON was decoded successfully
if ($stations_json_data === null) {
    die('Error decoding the Stations JSON file');
}

echo("Stations Map has " . count($stations_json_data) . " entries.<br/>" . PHP_EOL);

?>