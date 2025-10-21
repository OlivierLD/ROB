<?php
/*
 * Implementation of GET astro/bodies.php
 */
include __DIR__ . '/../autoload.php';

$VERBOSE = false;

$phpVersion = (int)phpversion()[0];
if ($phpVersion < 7) {
    echo("PHP Version is " . phpversion() . "... This might be too low.");
}

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
// header('Access-Control-Allow-Credentials: true');

$method = $_SERVER['REQUEST_METHOD'];
$input = json_decode(file_get_contents('php://input'), true);

if ($VERBOSE) {
    echo "Method is [" . $method . "]" . PHP_EOL;
}

switch ($method) {
    case 'GET':
        handleGet(); // GET astro/bodies
        break;
    case 'POST':
        handlePost($input);
        break;
    case 'PUT':
        handlePut($input);
        break;
    case 'DELETE':
        handleDelete($input);
        break;
    default:
        echo json_encode(['message' => 'Invalid request method [' . $method . ']']);
        break;
}

function handleGet() {
    $starCatalog = Star::getCatalog();
    $stars = array();
    for ($i=0; $i<count($starCatalog); $i++) {
        $starName = $starCatalog[$i][0];
        array_push($stars, $starName);
    }

    $incontournables = array("Sun", "Moon");
    $wanderingBodies = array("Aries", "Venus", "Mars", "Jupiter", "Saturn");

    echo json_encode(array_merge($incontournables, $wanderingBodies, $stars));
}

function handlePost($input) {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'POST Not Implemented', 'input' => $input]);
}

function handlePut($input) {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'PUT Not Implemented', 'input' => $input]);
}

function handleDelete($input) {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'DELETE Not Implemented', 'input' => $input]);
}
?>