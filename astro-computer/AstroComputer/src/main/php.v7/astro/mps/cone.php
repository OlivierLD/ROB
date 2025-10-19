<?php
/*
 * Implementation of POST /astro/mps/cone.php -d '{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}'
 */
include __DIR__ . '/../../autoload.php';

$VERBOSE = false;

$phpVersion = (int)phpversion()[0];
if ($phpVersion < 7) {
    echo("PHP Version is " . phpversion() . "... This might be too low.");
}

header("Content-Type: application/json");

$method = $_SERVER['REQUEST_METHOD'];
$input = json_decode(file_get_contents('php://input'), true);

if ($VERBOSE) {
    echo "Method is [" . $method . "]" . PHP_EOL;
}

switch ($method) {
    case 'GET':
        handleGet();
        break;
    case 'POST':
        handlePost($input); // THAT one
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

// {"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}
class ConeInput {

    public $bodyName;
	public $obsAlt;
	public $gha;
	public $d;

    public function __construct(string $bodyName,
                                float $obsAlt,
                                float $gha,
                                float $d) {
        $this->bodyName = $bodyName;
        $this->obsAlt = $obsAlt;
        $this->gha = $gha;
        $this->d = $d;
    }
}

function handleGet() {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'GET Not Implemented here.']);
}

function handlePost($input) {

    $coneInput = new ConeInput(
        (string)$input["bodyName"],
        (float)$input["obsAlt"],
        (float)$input["gha"],
        (float)$input["d"]
    );


    // And more... See MPSToolBox.calculateCone
    echo json_encode(['message' => 'POST Received', 'reworked-input' => $coneInput]);
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