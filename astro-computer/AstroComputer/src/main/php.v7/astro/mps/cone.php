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

$rhoE = 635677.0; // Earth radius, in 100s of km. It's 6356.77 km.
$earthRadiusNM = ($rhoE / 100.0) / 1.852; // Earth radius, in nm.

// Already defined in GeoUtils.php
//
// class GeoPoint {
//     public $latitude;
//     public $longitude;

//     public function __construct(float $lat,
//                                 float $lon) {
//         $this->latitude = $lat;
//         $this->longitude = $lon;
//     }
// }

class ConePoint {
    public $geoPoint;
    public $z;

    public function __construct(GeoPoint $gp,
                                float $z) {
        $this->geoPoint = $gp;
        $this->z = $z;
    }
}

class ConeDefinition {
    public $pg;
    public $obsAlt;
    public $earthCenterToConeSummit;
    public $bodyName;
    public $observationTime;
    public $circle; // Array of ConePoint

    public function __construct() {
        // Empty
    }
    // public function __construct(GeoPoint $pg,
    //                             float $obsAlt,
    //                             float $earthCenterToConeSummit,
    //                             string $bodyName,
    //                             string $observationTime,
    //                             array $circle) {
    //     $this->pg = $pg;
    //     $this->obsAlt = $obsAlt;
    //     $this->earthCenterToConeSummit = $earthCenterToConeSummit;
    //     $this->bodyName = $bodyName;
    //     $this->observationTime = $observationTime;
    //     $this->circle = $circle;
    // }
}

class CelestialDeadReckoning {
    private $dHe;
    private $dZ;
    private $AHG;
    private $D;
    private $L;
    private $G;

	public function __construct(float $dAHG,
								float $dD,
								float $dL,
								float $dG) {
		$this->dHe = null;
		$this->dZ = null;
		$this->AHG = $dAHG;
		$this->D = $dD;
		$this->L = $dL;
		$this->G = $dG;
	}

	public function calculate() : CelestialDeadReckoning {
		$AHL = $this->AHG + $this->G;
		if ($AHL < 0.0) {
			$AHL = 360.0 + $AHL;
		}
		$sinL = sin(deg2rad($this->L));
		$sinD = sin(deg2rad($this->D));
		$cosL = cos(deg2rad($this->L));
		$cosD = cos(deg2rad($this->D));
		$cosAHL = cos(deg2rad($AHL));
		$sinHe = $sinL * $sinD + $cosL * $cosD * $cosAHL;
		$He = rad2deg(asin($sinHe));
		$this->dHe = $He;
		$P = $AHL >= 180.0 ? 360.0 - $AHL : $AHL;
		$sinP = sin(deg2rad($P));
		$cosP = cos(deg2rad($P));
		$tanD = tan(deg2rad($this->D));
		$tanZ = $sinP / ($cosL * $tanD - $sinL * $cosP);
		$Z = rad2deg(atan($tanZ));
		if ($AHL < 180.0) {
			if ($Z < 0.0) {
				$Z = 180.0 - $Z;
			} else {
				$Z = 360.0 - $Z;
			}
		} else if ($Z < 0.0) {
			$Z = 180.0 + $Z;
	//  } else {
	//    Z = Z;
		}
		$this->dZ = $Z;
		return $this;
	}
}

/**
 * Translated from JS's function deadReckoning(start, dist, bearing)
 *
 * AKA Dead Reckoning (GC).
 * Starting from a point, sailing a given distance, in a given bearing (ICA), where do we arrive ?
 *
 * Formula is (with dist, heading, lat, lng in radians):
 * <pre>
 *  finalLat = arcsin(sin(startLat) * cos(dist)) + (cos(startLat) * sin(dist) * cos(heading))
 *  finalLng = startLng + atan2(sin(heading) * sin(dist) * cos(startLat), cos(dist) - (sin(startLat) * sin(finalLat)))
 * </pre>
 * <i>Note:</i> <code>atan2</code> return an angle between -&PI; and +&PI;, as opposed to <code>atan</code>, that returns an angle between -&PI;/2 and +&PI;/2.
 *
 * @param from Starting point. All in degrees
 * @param dist in nm (aka minutes of arc)
 * @param heading bearing in degrees (IRA-ICA)
 * @return The final point, all in degrees
 */

function haversineInv(GeoPoint $from, float $dist, float $heading) : GeoPoint {
	$radianDistance = deg2rad($dist / 60.0);
	$finalLat = (asin((sin(deg2rad($from->latitude)) * cos($radianDistance)) +
						(cos(deg2rad($from->latitude)) * sin($radianDistance) * cos(deg2rad($heading)))));
	$finalLng = deg2rad($from->longitude) + atan2(sin(deg2rad($heading)) * sin($radianDistance) * cos(deg2rad($from->latitude)),
						cos($radianDistance) - sin(deg2rad($from->latitude)) * sin($finalLat));
	$finalLat = rad2deg($finalLat);
	$finalLng = rad2deg($finalLng);

	return new GeoPoint($finalLat, $finalLng);
}

function calculateCone(ConeInput $input,
                       float $fromZ,
                       float $toZ,
                       float $zStep,
                       bool $verbose) : ConeDefinition {

    global $earthRadiusNM;
    $distInNM = (90.0 - $input->obsAlt) * 60.0;

    // Find MS, distance from observer to summit.
    $MS = $earthRadiusNM * (1 / tan(deg2rad($input->obsAlt)));
    if ($verbose) {
        echo sprintf("MS (obs to summit), in nautical miles: %.02f'\n", $MS);
    }
    $coneDiameter = $earthRadiusNM * cos(deg2rad($input->obsAlt));
    if ($verbose) {
        echo sprintf("Cone radius, in nautical miles: %.02f'\n", $coneDiameter);
    }

    $earthCenterToConeSummit = sqrt(($MS * $MS) + ($earthRadiusNM * $earthRadiusNM));

    // Find all the points seeing the body at the same altitude
    if ($verbose) {
        echo ("---- The Circle, Cone base ----");
    }

    $cd = new ConeDefinition();
    $cd->circle = array();
    $cd->bodyName = $input->bodyName;
    $cd->obsAlt = $input->obsAlt;
    $cd->pg = new GeoPoint($input->d, AstroComputer::ghaToLongitude($input->gha));
    $cd->earthCenterToConeSummit = $earthCenterToConeSummit;
    $cd->observationTime = ""; // SDF_UTC.format(calculationTime); // Not needed...

    for ($z=$fromZ; ($zStep > 0 && $z < $toZ) || ($zStep < 0 && $z > $toZ); $z += $zStep) { // The steps and interval here !
        $hdg = $z;
        // System.out.printf("Calculating cone point for Z=%.4f (zStep = %.4f)\n", hdg, zStep);
        $bodyPos = new GeoPoint($input->d, AstroComputer::ghaToLongitude($input->gha));
        $drGC = haversineInv($bodyPos, $distInNM, $hdg); // THE dr to use
        // final GeoPoint drGC = GeomUtil.deadReckoning(bodyPos, distInNM, hdg);

        // altitude tests, reverse
        if ($verbose) {
            // for 20-AUG-2025 10:40:31, GHA: 339°17.40', D: N 12°16.80', Obs Alt: 49°22.51'
            $cdr = new CelestialDeadReckoning($input->gha, $input->d, $drGC->latitude, $drGC->longitude).calculate();
            $he = $cdr->He;
            echo sprintf("GHA: %s, D: %s \n", Utils::decToSex($input->gha), Utils::decToSex($input->d));
            echo sprintf("For obsAlt=%f (%s), he (from circle)=%f (%s)\n", $input->obsAlt, Utils::decToSex($input->obsAlt), $he, Utils::decToSex($he));
        }
        array_push($cd->circle, new ConePoint($drGC, $hdg));
    }
    return $cd;
}

function handleGet() {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'GET Not Implemented here.']);
}

function handlePost($input) {

    global $VERBOSE;
    $coneInput = new ConeInput(
        (string)$input["bodyName"],
        (float)$input["obsAlt"],
        (float)$input["gha"],
        (float)$input["d"]
    );

    $coneDefinition = calculateCone($coneInput,
                                    0.0,
                                    360.0,
                                    1.0,
                                    $VERBOSE);
    // Return the ConeDefinition as JSON
    echo json_encode($coneDefinition);
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