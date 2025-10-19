<?php
/*
 * Implementation of POST /astro/mps/alt-and-z.php -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"gha":230.905951,"d":-1.313542}}'`
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

class Pg {

    public $GHA;
	public $D;
	public $hp;
	public $sd;

    public function __construct(float $gha,
                                float $dec,
                                float $hp,
                                float $sd) {
        $this->GHA = $gha;
        $this->D = $dec;
        $this->hp = $hp;
        $this->sd = $sd;
    }
}

class Position {
    public $latitude;
    public $longitude;

    public function __construct(float $lat,
                                float $lon) {
        $this->latitude = $lat;
        $this->longitude = $lon;
    }
}

function handleGet() {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'GET Not Implemented here.']);
}

function handlePost($input) {

    $pg = new Pg(
        (float)$input["pg"]["gha"],
        (float)$input["pg"]["d"],
        0, 0 // sd, hp
    );

    $position = new Position(
        (float)$input["pos"]["latitude"],
        (float)$input["pos"]["longitude"]
    );

    if (false) {
        echo "Pos:" . json_encode($input["pos"]) . PHP_EOL;
        echo "Pg:" . json_encode($input["pg"]) . PHP_EOL;

        echo "Position latitude: " . $input["pos"]["latitude"] . PHP_EOL;
        echo "Position longitude: " . $input["pos"]["longitude"] . PHP_EOL;

        echo "Requested position " . json_encode($position) . PHP_EOL;
    }
    $sru = new SightReductionUtil($pg->GHA,
                                  $pg->D,
                                  $position->latitude,
                                  $position->longitude);
    $sru->calculate();
    if (false) {
        echo("He:" . Utils::decToSex($sru->getHe()) . ", Z:" . sprintf("%f", $sru->getZ()) . "<br/>");
        echo("Done invoking SightReductionUtil.<br/>");
    }
    echo json_encode(['He' => $sru->getHe(),
                      'Z' => $sru->getZ()
         ]);

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