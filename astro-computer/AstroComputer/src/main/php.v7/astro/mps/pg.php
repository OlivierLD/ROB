<?php
/*
 * Implementation of GET /astro/mps/pg.php?body=XXX&date=YYY
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

// GET parameters, body, and date
$body = '';
$date = '';
if (isset($_GET['body'])) {
  $body = $_GET['body'];
}
if (isset($_GET['date'])) {
  $date = $_GET['date'];
}

if ($body == '' || $date == '') {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'Missing required parameters: body and date']);
    exit;
}


if ($VERBOSE) {
    echo "Method is [" . $method . "]" . PHP_EOL;
}

switch ($method) {
    case 'GET':
        handleGet($body, $date); // GET astro/mps/pg.php?body=XXX&date=YYY
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

/*
 * UTC Date in duration format
 * Sample: "2006-05-05T17:35:48"
 * Sample: "2006-05-05T17:35:48.000Z"
 *          |    |  |  |  |  |  |
 *          |    |  |  |  |  |  20
 *          |    |  |  |  |  17
 *          |    |  |  |  14
 *          |    |  |  11
 *          |    |  8
 *          |    5
 *          0
 */

function handleGet($body, $date) {

    try {
        // Current dateTime
        $year = (int)substr($date, 0, 4);
        $month = (int)substr($date, 5, 2);
        $day = (int)substr($date, 8, 2);
        $hours = (int)substr($date, 11, 2);
        $minutes = (int)substr($date, 14, 2);
        $seconds = (int)substr($date, 17, 2);

        if (false) {
            echo "Request for body [" . $body . "] at date [" . $date . "]" . PHP_EOL;
            echo "Parsed to Y=" . $year . " M=" . $month . " D=" . $day .
                 " h=" . $hours . " m=" . $minutes . " s=" . $seconds . PHP_EOL;
        }
        // Astro Computer
        $ac = new AstroComputer();
        // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
        $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true, true);
        // $context2 = $ac->getContext();
        // echo ("From calculate: EoT:" . $context2->EoT . " ");

        // Switch on body
        $pg = new Pg(0, 0, 0, 0);

        switch (strtolower($body)) {
            case 'sun':
                $pg->GHA = $ac->getSunGHA();
                $pg->D = $ac->getSunDecl();
                $pg->hp = $ac->getSunHp() / 3600.0;
                $pg->sd = $ac->getSunSd() / 3600.0;
                break;
            case 'moon':
                $pg->GHA = $ac->getMoonGHA();
                $pg->D = $ac->getMoonDecl();
                $pg->hp = $ac->getMoonHp() / 3600.0;
                $pg->sd = $ac->getMoonSd() / 3600.0;
                break;
            case 'venus':
                $pg->GHA = $ac->getVenusGHA();
                $pg->D = $ac->getVenusDecl();
                $pg->hp = $ac->getVenusHp() / 3600.0;
                $pg->sd = $ac->getVenusSd() / 3600.0;
                break;
            case 'mars':
                $pg->GHA = $ac->getMarsGHA();
                $pg->D = $ac->getMarsDecl();
                $pg->hp = $ac->getMarsHp() / 3600.0;
                $pg->sd = $ac->getMarsSd() / 3600.0;
                break;
            case 'jupiter':
                $pg->GHA = $ac->getJupiterGHA();
                $pg->D = $ac->getJupiterDecl();
                $pg->hp = $ac->getJupiterHp() / 3600.0;
                $pg->sd = $ac->getJupiterSd() / 3600.0;
                break;
            case 'saturn':
                $pg->GHA = $ac->getSaturnGHA();
                $pg->D = $ac->getSaturnDecl();
                $pg->hp = $ac->getSaturnHp() / 3600.0;
                $pg->sd = $ac->getSaturnSd() / 3600.0;
                break;
            default:
                // For stars
                $ac->starPos($body);
                $pg->GHA = $ac->getStarGHA($body);
                $star = Star::getStar($body);
                if ($star != null) {
                    $pg->D = $star->dec;
                    $pg->hp = 0.0;
                    $pg->sd = 0.0;
                } else {
                    throw new Exception("Star [$body] not found in catalog");
                }
                break;
        }
    } catch (Throwable $e) {
        // if ($verbose) {
        //     echo "[ Captured Throwable (2) for handleGet : " . $e->getMessage() . "] " . PHP_EOL;
        // }
        throw $e;
    }
    // Finally
    echo json_encode($pg); // Maybe a return some day ?...
}

function handlePost($input) {
    echo json_encode(['message' => 'POST Not Implemented', 'input' => $input]);
}

function handlePut($input) {
    echo json_encode(['message' => 'PUT Not Implemented', 'input' => $input]);
}

function handleDelete($input) {
    echo json_encode(['message' => 'DELETE Not Implemented', 'input' => $input]);
}
?>