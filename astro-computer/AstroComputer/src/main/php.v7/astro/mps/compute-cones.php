<?php

// Must be on top
$timeout = 600;  // In seconds
$applyTimeout = true; // Change at will

if ($applyTimeout) {
    ini_set("max_execution_time", $timeout);
}

/*
 * Implementation of POST /astro/mps/compute-cones.php -d '[
 *         {"bodyName" : "Mars", "date" : "2025-10-07T15:36:00", "gha" : null, "decl" : null, "obsAlt" : 21.942333333333334},
 *         {"bodyName" : "Venus", "date" : "2025-10-07T15:36:00", "gha" : null, "decl" : null, "obsAlt" : 14.014},
 *         {"bodyName" : "Altair", "date" : "2025-10-07T15:36:00", "gha" : null, "decl" : null, "obsAlt" : 32.47716666666667}
 * ]'
 * This can be a demanding one, as it computes multiple cones.
 * Do keep an eye on $distMin, and $firstZStep.
 */
include __DIR__ . '/../../autoload.php';

$VERBOSE = false;
$PHP_LOG = false;

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

$rhoE = 635677.0; // Earth radius, in 100s of km. It's 6356.77 km.
$earthRadiusNM = ($rhoE / 100.0) / 1.852; // Earth radius, in nm.

class BodyData {
    public $bodyName;
    public $date;
    public $gha;
    public $decl;
    public $obsAlt;

    public function __construct(string $bodyName,
                                string $date,
                                float $gha,
                                float $decl,
                                float $obsAlt) {
        $this->bodyName = $bodyName;
        $this->date = $date;
        $this->gha = $gha;
        $this->decl = $decl;
        $this->obsAlt = $obsAlt;
    }
}

function getBodyData(string $date, string $body, float $obsAlt) : BodyData {
    global $VERBOSE;
    try {
        // Current dateTime
        $year = (int)substr($date, 0, 4);
        $month = (int)substr($date, 5, 2);
        $day = (int)substr($date, 8, 2);
        $hours = (int)substr($date, 11, 2);
        $minutes = (int)substr($date, 14, 2);
        $seconds = (int)substr($date, 17, 2);

        if ($VERBOSE) {
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
        $bd = new BodyData(
            $body,
            $date,
            0.0,
            0.0,
            $obsAlt
        );

        switch (strtolower($body)) {
            case 'sun':
                $bd->gha = $ac->getSunGHA();
                $bd->decl = $ac->getSunDecl();
                break;
            case 'moon':
                $bd->gha = $ac->getMoonGHA();
                $bd->decl = $ac->getMoonDecl();
                break;
            case 'venus':
                $bd->gha = $ac->getVenusGHA();
                $bd->decl = $ac->getVenusDecl();
                break;
            case 'mars':
                $bd->gha = $ac->getMarsGHA();
                $bd->decl = $ac->getMarsDecl();
                break;
            case 'jupiter':
                $bd->gha = $ac->getJupiterGHA();
                $bd->decl = $ac->getJupiterDecl();
                break;
            case 'saturn':
                $bd->gha = $ac->getSaturnGHA();
                $bd->decl = $ac->getSaturnDecl();
                break;
            default:
                // For stars
                $ac->starPos($body);
                $bd->gha = $ac->getStarGHA($body);
                $star = Star::getStar($body);
                if ($star != null) {
                    $bd->decl = $star->dec;
                } else {
                    header('HTTP/1.0 404 Not Found');
                    throw new Exception("Star [$body] not found in catalog");
                }
                break;
        }
    } catch (Throwable $e) {
        // if ($verbose) {
        //     echo "[ Captured Throwable (2) for handleGet : " . $e->getMessage() . "] " . PHP_EOL;
        // }
        header('HTTP/1.0 404 Not Found');
        throw $e;
    }
    // Finally
    return $bd;
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

class ConesIntersection {
    public $bodyOneName;
    public $bodyTwoName;

    public $coneOneIntersectionOne;
    public $coneOneIntersectionTwo;
    public $coneTwoIntersectionOne;
    public $coneTwoIntersectionTwo;

    public function __construct(string $bodyOneName, string $bodyTwoName,
                                GeoPoint $coneOneIntersectionOne, GeoPoint $coneOneIntersectionTwo,
                                GeoPoint $coneTwoIntersectionOne, GeoPoint $coneTwoIntersectionTwo) {
        $this->bodyOneName = $bodyOneName;
        $this->bodyTwoName = $bodyTwoName;
        $this->coneOneIntersectionOne = $coneOneIntersectionOne;
        $this->coneOneIntersectionTwo = $coneOneIntersectionTwo;
        $this->coneTwoIntersectionOne = $coneTwoIntersectionOne;
        $this->coneTwoIntersectionTwo = $coneTwoIntersectionTwo;
    }
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

    public function getHe() : float {
        return $this->dHe;
    }
    public function getZ() : float {
        return $this->dZ;
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

// All inputs in degrees.
function haversineRaw(float $lat1, float $long1, float $lat2, float $long2) : float {
    $deltaG = deg2rad($long2 - $long1);
    $deltaL = deg2rad($lat2 - $lat1);
    $a = pow(sin($deltaL / 2.0), 2) + cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * pow(sin($deltaG / 2.0), 2);
    $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
    return $c; // In radians
}

// In nautical miles. Inputs in degrees.
function haversineNm(float $lat1, float $long1, float $lat2, float $long2) : float {

    // echo "Calculating haversineNm from ($lat1, $long1) to ($lat2, $long2)" . PHP_EOL;

    $NM_EQUATORIAL_EARTH_RADIUS = 3443.9184665227;   // nm per radian
    return haversineRaw($lat1, $long1, $lat2, $long2) * $NM_EQUATORIAL_EARTH_RADIUS;
}

function haversineNm2(GeoPoint $from, GeoPoint $to) : float {
    return haversineNm($from->latitude, $from->longitude, $to->latitude, $to->longitude);
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

function intersectionDelegation(ConeDefinition $coneBody1,
                                ConeDefinition $coneBody2,
                                int $loop,
                                float $zStep,
                                bool $verbose) : array { // returns a List<ConePoint>

    $result = array();

    $smallest = PHP_FLOAT_MAX;
    $closestPointBody1 = null;
    $closestPointBody2 = null;
    $closestPointZBody1 = null;
    $closestPointZBody2 = null;

    $smallestSecond = PHP_FLOAT_MAX;
    $closestPointBody1Second = null;
    $closestPointBody2Second = null;
    $closestPointZBody1Second = null;
    $closestPointZBody2Second = null;

    $distMin = 30.0; // 3.0; // TODO Fix that 3... Relate to first firstZStep ?

    foreach ($coneBody1->circle as $conePointBody1) {
        foreach ($coneBody2->circle as $conePointBody2) {
            // GC distance from-to, use GeomUtil.haversineNm
            $dist = haversineNm($conePointBody1->geoPoint->latitude, $conePointBody1->geoPoint->longitude, $conePointBody2->geoPoint->latitude, $conePointBody2->geoPoint->longitude);
            // For some tests..., to find the 2 intersections
            if ($loop == 0 && $dist < $distMin) {
                if ($verbose) {
                    echo sprintf("Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                            $dist, $zStep / 10.0,
                            $conePointBody1->geoPoint,
                            $conePointBody1->z,
                            $conePointBody2->geoPoint,
                            $conePointBody2->z);
                }
                // For loop 0, we'll need 2 smallest dist, identified by their Z
                if ($closestPointZBody1 != null && $closestPointZBody2 != null) {
                    if ($verbose) {
                        echo sprintf("DeltaZ_1 %.04f, DeltaZ_2 %.04f, compare to %.04f\n",
                                abs($conePointBody1->z - $closestPointZBody1),
                                abs($conePointBody2->z - $closestPointZBody2),
                                (5 * $zStep));
                    }
                    if (abs($conePointBody1->z - $closestPointZBody1) > (5 * $zStep) &&
                        abs($conePointBody2->z - $closestPointZBody2) > (5 * $zStep)) {
                        if ($dist < $smallestSecond) {
                            $smallestSecond = $dist;
                            $closestPointBody1Second = $conePointBody1->geoPoint;
                            $closestPointBody2Second = $conePointBody2->geoPoint;
                            $closestPointZBody1Second = $conePointBody1->z;
                            $closestPointZBody2Second = $conePointBody2->z;
                            if ($verbose) {
                                echo sprintf("2nd Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                        $dist, $zStep / 10.0,
                                        $conePointBody1->geoPoint,
                                        $conePointBody1->z,
                                        $conePointBody2->geoPoint,
                                        $conePointBody2->z);
                                echo sprintf("-- (1st : between %s (Z=%.02f) and %s (Z=%.02f))\n",
                                        json_encode($closestPointBody1),
                                        $closestPointZBody1,
                                        json_encode($closestPointBody2),
                                        $closestPointZBody2);
                            }
                        }
                    }
                }
            }
            if (($loop != 0) || ($loop == 0 && $closestPointZBody1Second == null && $closestPointZBody2Second == null)) {
                if ($dist < $smallest) {
                    $smallest = $dist;
                    $closestPointBody1 = $conePointBody1->geoPoint;
                    $closestPointBody2 = $conePointBody2->geoPoint;
                    $closestPointZBody1 = $conePointBody1->z;
                    $closestPointZBody2 = $conePointBody2->z;
                    if ($verbose && $dist < $distMin && $loop == 0) {
                        echo sprintf("1st Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                $dist,
                                $zStep / 10.0,
                                $conePointBody1->geoPoint,
                                $conePointBody1->z,
                                $conePointBody2->geoPoint,
                                $conePointBody2->z);
                    }
                }
            }
        }
    }
    // End of loop #n
    if ($verbose) {
        echo sprintf("Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                $loop + 1,
                $smallest,
                $closestPointZBody1,
                json_encode($closestPointBody1),
                $closestPointZBody2,
                json_encode($closestPointBody2));
        if ($loop == 0) {
            echo sprintf("=> 2nd Intersection: Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                    $loop + 1,
                    $smallestSecond,
                    $closestPointZBody1Second,
                    json_encode($closestPointBody1Second),
                    $closestPointZBody2Second,
                    json_encode($closestPointBody2Second));
        }
    }

    if ($closestPointBody1 != null && $closestPointBody2 != null) {
        array_push($result, new ConePoint($closestPointBody1, $closestPointZBody1));
        array_push($result, new ConePoint($closestPointBody2, $closestPointZBody2));

        if ($loop == 0 && $closestPointBody1Second != null && $closestPointBody2Second != null) {
            array_push($result, new ConePoint($closestPointBody1Second, $closestPointZBody1Second));
            array_push($result, new ConePoint($closestPointBody2Second, $closestPointZBody2Second));
        }
    }

    return $result;
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
        // echo sprintf("Calculating cone point for Z=%.4f (zStep = %.4f)\n", hdg, zStep);
        $bodyPos = new GeoPoint($input->d, AstroComputer::ghaToLongitude($input->gha));
        $drGC = haversineInv($bodyPos, $distInNM, $hdg); // THE dr to use
        // final GeoPoint drGC = GeomUtil.deadReckoning(bodyPos, distInNM, hdg);

        // altitude tests, reverse
        if ($verbose) {
            // for 20-AUG-2025 10:40:31, GHA: 339°17.40', D: N 12°16.80', Obs Alt: 49°22.51'
            $cdr = new CelestialDeadReckoning($input->gha, $input->d, $drGC->latitude, $drGC->longitude);
            $cdr = $cdr->calculate();
            $he = $cdr->getHe();
            echo sprintf("GHA: %s, D: %s \n", Utils::decToSex($input->gha), Utils::decToSex($input->d));
            echo sprintf("For obsAlt=%f (%s), he (from circle)=%f (%s)\n", $input->obsAlt, Utils::decToSex($input->obsAlt), $he, Utils::decToSex($he));
        }
        array_push($cd->circle, new ConePoint($drGC, $hdg));
    }
    return $cd;
}

/**
 * Find the intersection of two cones, defined by their Observed Altitude, GHA and Declination.
 * No user position involved here  - this is what we want to find.
 *
 * This uses a method close to the Newton's method.
 * We look for the points as close to each other as possible, starting from all the points of cone1, and checking for each of them
 * all the points of cone2, with a given step for Z.
 * Then we restrict the Z interval, and the Z step.
 * This as many times as required by the nbLoops parameter.
 *
 * @param firstTime Date when the firstObsAlt was taken
 * @param firstObsAlt Observed Altitude of the first body
 * @param firstGHA GHA of the first body (at firstTime)
 * @param firstDecl Decl of the first body (at firstTime)
 * @param secondTime Date when the secondObsAlt was taken
 * @param secondObsAlt Observed Altitude of the second body
 * @param secondGHA GHA of the second body (at secondTime)
 * @param secondDecl Decl of the second body (at secondTime)
 * @param firstZStep Azimuth step to start with.
 * @param nbLoops Number of recursions
 * @param reverse Build the cones counterclockwise if true
 * @param verbose true or false
 * @return a List of the two GeoPoints (one on each cone) closest to each other.
 */
function resolve2Cones(string $firstTime, float $firstObsAlt, float $firstGHA, float $firstDecl,
                       string $secondTime, float $secondObsAlt, float $secondGHA, float $secondDecl,
                       float $firstZStep, int $nbLoops, bool $reverse, bool $verbose) : array {
    global $PHP_LOG;
    $result = array();

    // double smallest = Double.MAX_VALUE;
    $closestPointBody1 = null;
    $closestPointBody2 = null;
    $closestPointZBody1 = null;
    $closestPointZBody2 = null;

    // double smallestSecond = Double.MAX_VALUE;
    $closestPointBody1Second = null;
    $closestPointBody2Second = null;
    $closestPointZBody1Second = null;
    $closestPointZBody2Second = null;

    $fromZ = 0.0;
    $toZ = 360.0;
    $zStep = $firstZStep * 10.0; // because divided by 10, even when starting the first loop.

    if ($reverse) {
        $fromZ = 360.0;
        $toZ = 0.0;
        $zStep *= -1;
    }

    for ($loop=0; $loop<$nbLoops; $loop++) {
        if ($PHP_LOG) {
            error_log(sprintf("---- Loop %d: fromZ=%.04f toZ=%.04f zStep=%.04f ----", $loop + 1, $fromZ, $toZ, $zStep / 10.0), 0);
        }
        $coneBody1 = calculateCone(new ConeInput("Body 1", $firstObsAlt, $firstGHA, $firstDecl),
                $closestPointZBody1 == null ? $fromZ : $closestPointZBody1 - $zStep,
                $closestPointZBody1 == null ? $toZ : $closestPointZBody1 + $zStep,
                $zStep / 10.0, $verbose);
        $coneBody2 = calculateCone(new ConeInput("Body 2", $secondObsAlt, $secondGHA, $secondDecl),
                $closestPointZBody2 == null ? $fromZ : $closestPointZBody2 - $zStep,
                $closestPointZBody2 == null ? $toZ : $closestPointZBody2 + $zStep,
                $zStep / 10.0, $verbose);

        // Now, find the intersection of the two cones...
        $geoPointsFirst = intersectionDelegation($coneBody1, $coneBody2, $loop, $zStep / 10, $verbose);
        $closestPointBody1 = $geoPointsFirst[0]->geoPoint;
        $closestPointZBody1 = $geoPointsFirst[0]->z;
        $closestPointBody2 = $geoPointsFirst[1]->geoPoint;
        $closestPointZBody2 = $geoPointsFirst[1]->z;

        if ($loop == 0) { // Populate second ones
            if (count($geoPointsFirst) == 4) {
                $closestPointBody1Second = $geoPointsFirst[2]->geoPoint;
                $closestPointZBody1Second = $geoPointsFirst[2]->z;
                $closestPointBody2Second = $geoPointsFirst[3]->geoPoint;
                $closestPointZBody2Second = $geoPointsFirst[3]->z;
            } else {
                echo sprintf("Ooops !!! Second intersection was not found ! Only %d point(s) available.\n", count($geoPointsFirst));
                header('HTTP/1.0 404 Not Found');
                throw new Exception(sprintf("Ooops !!! Second intersection was not found ! Only %d point(s) available.", count($geoPointsFirst)));
            }
        }

        // 2nd intersection ?
        if ($loop > 0) { // Deal with 2nd intersection
            if ($verbose) {
                echo ("Dealing with second Intersection...");
            }
            $coneBody1Second = calculateCone(new ConeInput("Body 1", $firstObsAlt, $firstGHA, $firstDecl),
                    $closestPointZBody1Second == null ? $fromZ : $closestPointZBody1Second - $zStep,
                    $closestPointZBody1Second == null ? $toZ : $closestPointZBody1Second + $zStep,
                    $zStep / 10.0, $verbose);
            $coneBody2Second = calculateCone(new ConeInput("Body 2", $secondObsAlt, $secondGHA, $secondDecl),
                    $closestPointZBody2Second == null ? $fromZ : $closestPointZBody2Second - $zStep,
                    $closestPointZBody2Second == null ? $toZ : $closestPointZBody2Second + $zStep,
                    $zStep / 10.0, $verbose);
            $geoPointsSecond = intersectionDelegation($coneBody1Second, $coneBody2Second, $loop, $zStep / 10.0, $verbose);
            $closestPointBody1Second = $geoPointsSecond[0]->geoPoint;
            $closestPointZBody1Second = $geoPointsSecond[0]->z;
            $closestPointBody2Second = $geoPointsSecond[1]->geoPoint;
            $closestPointZBody2Second = $geoPointsSecond[1]->z;
        }
        $zStep /= 10.0; // For the next loop
        if ($PHP_LOG) {
            error_log(sprintf("---- End of Loop %d ----", $loop + 1), 0);
        }
    }

    array_push($result, $closestPointBody1);
    array_push($result, $closestPointBody2);
    array_push($result, $closestPointBody1Second);
    array_push($result, $closestPointBody2Second);

    return $result;
}

class MapOfGeoPoints {
    public $key;    // GeoPoint
    public $points; // array of GeoPoint

    public function __construct(GeoPoint $key) {
        $this->key = $key;
        $this->points = array();
    }

    public function addPoint(GeoPoint $gp) {
        array_push($this->points, $gp);
    }
}

function processIntersectionsList(array $conesIntersectionList,
                                  bool $verbose) : GeoPoint {

    $CRITICAL_DIST = 5.0;

    if ($verbose) {
        echo sprinf("We have %d intersections to process.\n", count($conesIntersectionList));
    }
    if (count($conesIntersectionList) > 1) {
        if ($verbose) {
            foreach ($conesIntersectionList as $ci) {
                echo sprinf("- Intersection between %s and %s\n", $ci->bodyOneName, $ci->bodyTwoName);
            }
        }
        $candidates = array(); // GeoPoints

        // Iterate on the reference as well...
        for ($ref = 0; $ref < count($conesIntersectionList); $ref++) {

            if ($verbose) {
                echo sprinf("-- Ref: %d\n", $ref);
            }
            $referenceIntersection = $conesIntersectionList[$ref];

            /*
                Need to manage:
                    cone 1 - intersection 1 with cone 1 - intersection 1
                    cone 1 - intersection 1 with cone 1 - intersection 2
                    cone 1 - intersection 2 with cone 1 - intersection 1
                    cone 1 - intersection 2 with cone 1 - intersection 2

                    cone 2 - intersection 1 with cone 1 - intersection 1
                    cone 2 - intersection 1 with cone 1 - intersection 2
                    cone 2 - intersection 2 with cone 1 - intersection 1
                    cone 2 - intersection 2 with cone 1 - intersection 2

                    cone 1 - intersection 1 with cone 2 - intersection 1
                    cone 1 - intersection 1 with cone 2 - intersection 2
                    cone 1 - intersection 2 with cone 2 - intersection 1
                    cone 1 - intersection 2 with cone 2 - intersection 2

                    cone 2 - intersection 1 with cone 2 - intersection 1
                    cone 2 - intersection 1 with cone 2 - intersection 2
                    cone 2 - intersection 2 with cone 2 - intersection 1
                    cone 2 - intersection 2 with cone 2 - intersection 2

                    Good luck.
                */

            for ($i = 0; $i < count($conesIntersectionList); $i++) {
                if ($i != $ref) {
                    if ($verbose) {
                        echo sprinf("---- i: %d\n", $i);
                    }
                    $thatOne = $conesIntersectionList[$i];

                    // Cartesian product !

                    // Cone 1 - Cone 1
                    $oneOneDistOneOne = haversineNm2($referenceIntersection->coneOneIntersectionOne, $thatOne->coneOneIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionOne), json_encode($thatOne->coneOneIntersectionOne), $oneOneDistOneOne);
                    }
                    if ($oneOneDistOneOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionOne);
                    }
                    $oneOneDistOneTwo = haversineNm2($referenceIntersection->coneOneIntersectionOne, $thatOne->coneOneIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionOne), json_encode($thatOne->coneOneIntersectionTwo), $oneOneDistOneTwo);
                    }
                    if ($oneOneDistOneTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionTwo);
                    }
                    $oneOneDistTwoOne = haversineNm2($referenceIntersection->coneOneIntersectionTwo, $thatOne->coneOneIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionTwo), json_encode($thatOne->coneOneIntersectionOne), $oneOneDistTwoOne);
                    }
                    if ($oneOneDistTwoOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionOne);
                    }
                    $oneOneDistTwoTwo = haversineNm2($referenceIntersection->coneOneIntersectionTwo, $thatOne->coneOneIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionTwo), json_encode($thatOne->coneOneIntersectionTwo), $oneOneDistTwoTwo);
                    }
                    if ($oneOneDistTwoTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionTwo);
                    }

                    // Cone 1 - Cone 2
                    $oneTwoDistOneOne = haversineNm2($referenceIntersection->coneOneIntersectionOne, $thatOne->coneTwoIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionOne), json_encode($thatOne->coneTwoIntersectionOne), $oneTwoDistOneOne);
                    }
                    if ($oneTwoDistOneOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionOne);
                    }
                    $oneTwoDistOneTwo = haversineNm2($referenceIntersection->coneOneIntersectionOne, $thatOne->coneTwoIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionOne), json_encode($thatOne->coneTwoIntersectionTwo), $oneTwoDistOneTwo);
                    }
                    if ($oneTwoDistOneTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionTwo);
                    }
                    $oneTwoDistTwoOne = haversineNm2($referenceIntersection->coneOneIntersectionTwo, $thatOne->coneTwoIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionTwo), json_encode($thatOne->coneTwoIntersectionOne), $oneTwoDistTwoOne);
                    }
                    if ($oneTwoDistTwoOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionOne);
                    }
                    $oneTwoDistTwoTwo = haversineNm2($referenceIntersection->coneOneIntersectionTwo, $thatOne->coneTwoIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionTwo), json_encode($thatOne->coneTwoIntersectionTwo), $oneTwoDistTwoTwo);
                    }
                    if ($oneTwoDistTwoTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionTwo);
                    }

                    // Cone 2 - Cone 1
                    $twoOneDistOneOne = haversineNm2($referenceIntersection->coneTwoIntersectionOne, $thatOne->coneOneIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionOne), json_encode($thatOne->coneOneIntersectionOne), $twoOneDistOneOne);
                    }
                    if ($twoOneDistOneOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionOne);
                    }
                    $twoOneDistOneTwo = haversineNm2($referenceIntersection->coneTwoIntersectionOne, $thatOne->coneOneIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionOne), json_encode($thatOne->coneOneIntersectionTwo), $twoOneDistOneTwo);
                    }
                    if ($twoOneDistOneTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionTwo);
                    }
                    $twoOneDistTwoOne = haversineNm2($referenceIntersection->coneTwoIntersectionOne, $thatOne->coneOneIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionOne), json_encode($thatOne->coneOneIntersectionOne), $oneOneDistTwoOne);
                    }
                    if ($twoOneDistTwoOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionOne);
                    }
                    $twoOneDistTwoTwo = haversineNm2($referenceIntersection->coneTwoIntersectionTwo, $thatOne->coneOneIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionTwo), json_encode($thatOne->coneOneIntersectionTwo), $twoOneDistTwoTwo);
                    }
                    if ($twoOneDistTwoTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneOneIntersectionTwo);
                    }

                    // Cone 2 - Cone 2
                    $twoTwoDistOneOne = haversineNm2($referenceIntersection->coneTwoIntersectionOne, $thatOne->coneTwoIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionOne), json_encode($thatOne->coneTwoIntersectionOne), $twoTwoDistOneOne);
                    }
                    if ($twoTwoDistOneOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionOne);
                    }
                    $twoTwoDistOneTwo = haversineNm2($referenceIntersection->coneTwoIntersectionOne, $thatOne->coneTwoIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionOne), json_encode($thatOne->coneTwoIntersectionTwo), $twoTwoDistOneTwo);
                    }
                    if ($twoTwoDistOneTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionTwo);
                    }
                    $twoTwoDistTwoOne = haversineNm2($referenceIntersection->coneTwoIntersectionTwo, $thatOne->coneTwoIntersectionOne);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneTwoIntersectionTwo), json_encode($thatOne->coneTwoIntersectionOne), $twoTwoDistTwoOne);
                    }
                    if ($twoTwoDistTwoOne < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionOne);
                    }
                    $twoTwoDistTwoTwo = haversineNm2($referenceIntersection->coneTwoIntersectionTwo, $thatOne->coneTwoIntersectionTwo);
                    if ($verbose) {
                        echo sprinf("ref: %d, i: %d. Between %s and %s, dist = %f\n", $ref, $i, json_encode($referenceIntersection->coneOneIntersectionTwo), json_encode($thatOne->coneTwoIntersectionTwo), $twoTwoDistTwoTwo);
                    }
                    if ($twoTwoDistTwoTwo < $CRITICAL_DIST) {
                        array_push($candidates, $thatOne->coneTwoIntersectionTwo);
                    }
                }
            }
        }
        // The result...
        if ($verbose) {
            echo ("-----------------------------" . PHP_EOL);
            echo sprinf("%d candidate(s):\n", count($candidates));
            foreach ($candidates as $pt) {
                echo sprinf("\u2022 %s\n", $pt);
            }
            echo ("-----------------------------" . PHP_EOL);
        }
        // Create lists of points, put together by their distance to each other
        // Map<GeoPoint, List<GeoPoint>> pointMap = new HashMap<>();
        $pointMap = array(); // MapOfGeoPoints
        $ref = null;
        foreach ($candidates as $pt) {
            if ($ref != null) {
                // Calculate distance with ref
                $nm = haversineNm2($ref, $pt);
                if ($nm < $CRITICAL_DIST) {
                    // pointMap.get(ref.get()).add(pt);
                    for ($i=0; $i<count($pointMap); $i++) {
                        if ($pointMap[$i]->key == $ref) {
                            $pointMap[$i]->addPoint($pt);
                            break;
                        }
                    }
                } else {
                    // See distance with other lists (ref)
                    if ($verbose) {
                        echo sprinf("%s too far from %s (%f)\n", $pt, $ref, $nm);
                    }
                    // $keys = $pointMap.keySet().toArray();
                    $found = false;
                    // for (int k=0; k<keys.length; k++) {
                    foreach ($pointMap as $mapElement) {
                        // GeoPoint gpKey = (GeoPoint)keys[k];
                        $gpKey = $mapElement->key;
                        $dist = haversineNm2($gpKey, $pt);
                        if ($dist < $CRITICAL_DIST) {
                            $ref = $gpKey;
                            $mapElement->addPoint($pt);
                            $found = true;
                            break;
                        }
                    }
                    if (!$found) {
                        $newPointMap = new MapOfGeoPoints($pt);
                        array_push($pointMap, $newPointMap);
                        $ref = $pt;
                    }
                }
            } else {
                $ref = $pt;
                $newPointMap = new MapOfGeoPoints($pt);
                array_push($pointMap, $newPointMap);
            }
        }
        if ($verbose) {
            echo sprinf("PointMap has %d entries\n", count($pointMap));
        }
        // We will do the average of the biggest list
        $restricted = array();
        $maxCard = -1;
        foreach ($pointMap as $mapElement) { // }.forEach((pt, list) -> {
            $list = $mapElement->points;
            if (count($list) > $maxCard) {
                $maxCard = count($list);
                $restricted = $list;
            }
        }

        // An average ?
        $candidates = $restricted;
        if ($verbose) {
            echo sprinf("Working on %d positions\n", count($candidates));
        }

        $latitudeSum = 0.0;
        $longitudeSum = 0.0;
        foreach ($candidates as $pt) {
            $latitudeSum += $pt->latitude;
            $longitudeSum += $pt->longitude;
        }

        $averageLat = $latitudeSum / count($candidates);
        $averageLng = $longitudeSum / count($candidates);

        $avgPoint = new GeoPoint($averageLat, $averageLng);
        if ($verbose) {
            echo sprinf("=> Average: %s\n", json_encode(avgPoint));
        }
        return $avgPoint;
    } else {
        header('HTTP/1.0 404 Not Found');
        throw new Exception(sprintf("Not enough intersections to process. Need at least 2, got %d", count($conesIntersectionList)));
    }
}

function handleGet() {
    // Error code
    header('HTTP/1.0 404 Not Found');
    echo json_encode(['message' => 'GET Not Implemented here.']);
}

function handlePost($input) {

    global $VERBOSE;
    global $PHP_LOG;

    $listBodyData = array();
    foreach ($input as $bodyRequest) {
        $oneBodyData = getBodyData((string)$bodyRequest["date"], (string)$bodyRequest["bodyName"], (float)$bodyRequest["obsAlt"]);
        array_push($listBodyData, $oneBodyData);
    }

    $conesIntersectionList = array();

    // Next, a serie of resolve2cones.
    $nbIter = 4;        // Hard-coded for now. To be tuned.
    $reverse = false;   // Hard-coded for now.
    $nbProcess = 0;
    for ($i=0; $i<count($listBodyData); $i++) {
        for ($j=0; $j<count($listBodyData); $j++) {
            if ($i != $j) {
                if ($VERBOSE) {
                    echo sprintf("[%d, %d], %s and %s\n", $i, $j, $listBodyData[$i]->bodyName, $listBodyData[$j]->bodyName);
                }
                // Write to the server log, in real time.
                if ($PHP_LOG) {
                    error_log(sprintf("[%d, %d], %s and %s", $i, $j, $listBodyData[$i]->bodyName, $listBodyData[$j]->bodyName), 0);
                }

                $bodyOne = $listBodyData[$i]->bodyName;
                $altOne = $listBodyData[$i]->obsAlt; // saturnObsAlt;
                $ghaOne = $listBodyData[$i]->gha;    // saturnGHA;
                $declOne = $listBodyData[$i]->decl;  // saturnDecl;
                $dateOne = $listBodyData[$i]->date;  // date.getTime();

                $bodyTwo = $listBodyData[$j]->bodyName;
                $altTwo = $listBodyData[$j]->obsAlt; // jupiterObsAlt;
                $ghaTwo = $listBodyData[$j]->gha;    // jupiterGHA;
                $declTwo = $listBodyData[$j]->decl;  // jupiterDecl;
                $dateTwo = $listBodyData[$j]->date;  // date.getTime();

                if ($VERBOSE) {
                    echo ("------------------------------------------------");
                    echo sprintf("Starting resolve process with:\n" .
                                    "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" .
                                    "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                            $dateOne,
                            Utils::decToSex($altOne),
                            Utils::decToSex($ghaOne),
                            Utils::decToSex($declOne),
                            $dateTwo,
                            Utils::decToSex($altTwo),
                            Utils::decToSex($ghaTwo),
                            Utils::decToSex($declTwo));
                    echo ("------------------------------------------------");
                }
                // Ephemeris and Altitudes OK, let's proceed.
                $firstZStep = 1.0; // 0.1;  // More than 0.1 not good enough... See $distMin in intersectionDelegation

                // Now, find the intersection(s) of the two cones...
                $closests = resolve2Cones($dateOne, $altOne, $ghaOne, $declOne,
                                          $dateTwo, $altTwo, $ghaTwo, $declTwo,
                                          $firstZStep, $nbIter, $reverse, $VERBOSE);

                if (/*closests != null ||*/ count($closests) >= 4) {
                    $d1 = haversineNm($closests[0]->latitude, $closests[0]->longitude, $closests[1]->latitude, $closests[1]->longitude);
                    $d2 = haversineNm($closests[2]->latitude, $closests[2]->longitude, $closests[3]->latitude, $closests[3]->longitude);
                    if ($VERBOSE) {
                        echo sprintf("%d - %s & %s\n", ++$nbProcess, $bodyOne, $bodyTwo);
                        echo sprintf("After %d iterations:\n", $nbIter);
                        echo sprintf("1st position between %s (%s) and %s (%s), dist %.02f nm.\n", $closests[0], json_encode($closests[0]), $closests[1], json_encode($closests[1]), $d1);
                        echo sprintf("2nd position between %s (%s) and %s (%s), dist %.02f nm.\n", $closests[2], json_encode($closests[2]), $closests[2], json_encode($closests[2]), $d2);
                    }
                    // For later
                    array_push($conesIntersectionList, new ConesIntersection($bodyOne, $bodyTwo,
                                                                             $closests[0], $closests[1],
                                                                             $closests[2], $closests[3]));
                } else {
                    echo ("Oops ! Not found...");
                }
            }
        }
    }
    if ($VERBOSE) {
        echo sprintf("End of permutations, %d intersections\n",
                     count($conesIntersectionList));
    }
    if ($PHP_LOG) {
        error_log(sprintf("End of permutations, %d intersections",
                        count($conesIntersectionList)), 0);
    }

    // Now process all intersections...
    if ($VERBOSE) {
        echo ("-----------------------------");
    }

    try {
        // Final step!
        $avgPoint = processIntersectionsList($conesIntersectionList, $VERBOSE);
        // $avgPoint = new GeoPoint(47, -3);
        if (false && $VERBOSE) {
            echo sprintf("Found (avg) intersection at %s\n", $avgPoint);
        }
        // Return the position
        // echo json_encode(['message' => 'POST. Not finished yet', 'calculated-position' => $avgPoint, 'current' => $listBodyData, 'intersections' => $conesIntersectionList]);
        echo json_encode($avgPoint); // Finally !

    } catch (Exception $mei) {
        // mei.printStackTrace();
        header('HTTP/1.0 404 Not Found');
        throw $mei;
    }
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