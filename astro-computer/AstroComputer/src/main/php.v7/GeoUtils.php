<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */

function sign(float $n) : int {
    if ($n == 0) {
        return 0;
    } else if ($n < 0) {
        return -1;
    } else {
        return 1;
    }
}

class GP {
    private $name; // string
    private $decl; // float
    private $gha;  // float
    private $fromPos; // BodyFromPos

    function __construct() {}

    public function toString() : string {
        return sprintf("Name:%s, decl:%f, gha:%f, From:%s", $name, $decl, $gha, ($fromPos != null ? $fromPos->toString() : "null"));
    }

    public function name(string $body) : GP {
        $this->name = $body;
        return $this;
    }

    public function decl(float $d) : GP {
        $this->decl = $d;
        return $this;
    }

    public function gha(float $d) : GP {
        $this->gha = $d;
        return $this;
    }

    public function bodyFromPos(BodyFromPos $fromPos) : GP {
        $this->fromPos = $fromPos;
        return $this;
    }

    public function getName() : string {
        return $this->name;
    }

    public function getDecl() : float {
        return $this->decl;
    }

    public function getGha() : float {
        return $this->gha;
    }

    public function getFromPos() : BodyFromPos {
        return $this->fromPos;
    }
}

class OBS {
    private $alt; // float
    private $z;   // float

    public function toString() : string {
        return "OBS{" . "alt=" . $this->alt . ", z=" . $this->z . "}";
    }

    public function alt(float $alt) : OBS {
        $this->alt = $alt;
        return $this;
    }

    public function z(float $z) : OBS {
        $this->z = $z;
        return $this;
    }

    public function getAlt() : float {
        return $this->alt;
    }

    public function getZ() : float {
        return $this->z;
    }
}

class Pos {
    private $latitude; // float
    private $longitude; // float

    public function toString() : string {
        return sprintf("%s/%s", Utils::decToSex($this->latitude, Utils::NS, Utils::$LEADING_SIGN, false), Utils::decToSex($longitude, Utils::EW, Utils::$LEADING_SIGN, false));
    }

    public function latitude(float $lat) : Pos {
        $this->latitude = $lat;
        return $this;
    }

    public function longitude(float $lng) : Pos {
        $this->longitude = $lng;
        return $this;
    }

    public function getLatitude() : float {
        return $this->latitude;
    }

    public function getLongitude() : float {
        return $this->longitude;
    }
}

class BodyFromPos {
    private $observer; // Pos
    private $observed; // OBS

    function __construct() {}

    public function toString() : string {
        return "BodyFromPos{" . "observer=" . $this->observer.toString() . ", observed=" . $this->observed.toString() . "}";
    }

    public function observer(Pos $from) : BodyFromPos {
        $this->observer = $from;
        return $this;
    }

    public function observed(OBS $asSeen) : BodyFromPos {
        $this->observed = $asSeen;
        return $this;
    }

    public function getObserver() : Pos {
        return $this->observer;
    }

    public function getObserved() : OBS {
        return $this->observed;
    }
}

/**
 * RhumbLine data holder
 */
class RLData {
    private $rv;
    private $dLoxo;

    function __cnstruct(float $rv, float $d) {
        $this->rv = $rv;
        $this->dLoxo = $d;
    }

    public function getRv() : float {
        return $this->rv;
    }

    public function getdLoxo() : float {
        return $this->dLoxo;
    }
}

class GeoPoint {
	private $latitude;
	private $longitude;

	// function __construct() {}

	function __construct(float $l, float $g) {
		$this->latitude = $l;
		$this->longitude = $g;
	}

	public function getLatitude() : float {
		return $this->latitude;
	}

	public function getLongitude() : float {
		return $this->longitude;
	}

	public function getL() : float {
		return $this->latitude;
	}

	public function getG() : float {
		return $this->longitude;
	}

	public function setL(float $l) : void {
		$this->latitude = $l;
	}

	public function setG(float $g) : void {
		$this->longitude = $g;
	}

	public function equals(GeoPoint $p) : bool {
		return $this.longitude == $p->getG() && $this.latitude == $p.getL();
	}

	/**
	 * In nautical miles
	 *
	 * @param target target point
	 * @return distance in nm
	 */
	public function orthoDistanceBetween(GeoPoint $target) : float {
		$gc = new GreatCircle();
		$gc.setStart(new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG())));
		$gc.setArrival(new GreatCirclePoint(deg2rad($target->getL()), deg2rad($target->getG())));
		$gc.calculateGreatCircle(1);
		$d = rad2deg($gc->getDistance());
		return $d * 60;
	}

	/**
	 * In nautical miles
	 *
	 * @param target target point
	 * @return distance in degrees
	 */
	public function gcDistanceBetween(GeoPoint $target) : float {
		$from = new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG()));
		$to = new GreatCirclePoint(deg2rad($target->getL()), deg2rad($target->getG()));
		return GreatCircle::getGCDistanceInDegrees($from, $to);
	}

	/**
	 * AKA Rhumbline. In nautical miles
	 *
	 * @param target target point
	 * @return distance
	 */
	public function loxoDistanceBetween(GeoPoint $target) : float {
		$gc = new GreatCircle();
		$gc.setStart(new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG())));
		$gc.setArrival(new GreatCirclePoint(deg2rad($target->getL()), deg2rad($target->getG())));
		$rlData = $gc->calculateRhumbLine();
		return $rlData->getdLoxo();
	}

	public function toString() : string {
		return sprintf("%s / %s",
				Utils::decToSex($this->latitude, Utils::NS, Utils::$LEADING_SIGN, false),
				Utils::decToSex($this->longitude, Utils::EW, Utils::$LEADING_SIGN, false));
	}

	public function degreesToRadians() : GeoPoint {
		return new GeoPoint(deg2rad($this->getL()), deg2rad($this->getG()));
	}

	public function radiansToDegrees() : GeoPoint {
		return new GeoPoint(rad2deg($this->getL()), rad2deg($this->getG()));
	}

}

class GreatCircle {
	public static $TO_NORTH = 0;
	public static $TO_SOUTH = 1;
	public static $TO_EAST = 2;
	public static $TO_WEST = 3;
	
	private $ewDir;
	private $nsDir;
	private $start;   // GreatCirclePoint, Angles in radians or degrees !!
	private $arrival; //GreatCirclePoint, Angles in radians or degrees !!
	private $route; // array, Vector<GreatCircleWayPoint>

	private static $TOLERANCE = 1;

	// function __construct() {
	// 	$this->start = null;
	// 	$this->arrival = null;
	// }

	/**
	 *
	 * @param startingPoint angle values in radians
	 * @param arrivalPoint  angle values in radians
	 */
	function __construct(GreatCirclePoint $startingPoint, GreatCirclePoint $arrivalPoint) {
		$this->start = $startingPoint;
		$this->arrival = $arrivalPoint;
	}

	/**
	 * Coordinates in radians
	 *
	 * @param p the position to start from
	 */
	public function setStart(GreatCirclePoint $p) : void {
		$this->start = $p;
	}

	public function start(GreatCirclePoint $p) : GreatCircle {
		$this->start = $p;
		return $this;
	}

	public function startInDegrees(GreatCirclePoint $p) : GreatCircle {
		$this->start = new GreatCirclePoint(deg2rad($p->getL()), deg2rad($p->getG()));
		return $this;
	}

	public function arrival(GreatCirclePoint $p) : GreatCircle {
		$this->arrival = $p;
		return $this;
	}

	public function arrivalInDegrees(GreatCirclePoint $p) : GreatCircle {
		$this->arrival = new GreatCirclePoint(deg2rad($p->getL()), deg2rad($p->getG()));
		return $this;
	}

	public function setStartInDegrees(GreatCirclePoint $p) : void {
		$this->start = new GreatCirclePoint(deg2rad($p->getL()), deg2rad($p->getG()));
	}

	/**
	 * Coordinates in radians
	 *
	 * @param p the arrival position
	 */
	public function setArrival(GreatCirclePoint $p) : void {
		$this->arrival = $p;
	}

	public function setArrivalInDegrees(GreatCirclePoint $p) : void {
		$this->arrival = new GreatCirclePoint(deg2rad($p->getL()), deg2rad($p->getG()));
	}

	public function getStart() : GreatCirclePoint {
		return $this->start;
	}

	public function getArrival() : GreatCirclePoint {
		return $this->arrival;
	}

	public function getNS() : int {
		return $this->nsDir;
	}

	public function getEW() : int {
		return $this->ewDir;
	}

	/**
	 * See http://ressources.profmarine.fr/ortho/AC_TDC1_orthoV2.pdf, for validations...
	 *
	 * @return Initial Route Angle, in radians
	 */
	public function getInitialRouteAngle() : float {
		return GreatCircle::getInitialRouteAngleWithPoints($this->start, $this->arrival);
	}

	/**
	 *
	 * @param from angle values in radians
	 * @param to angle values in radians
	 * @return Initial Route Angle, in radians
	 */
	public static function getInitialRouteAngleWithPoints(GreatCirclePoint $from, GreatCirclePoint $to) : float {
//		double g = to.getG() - from.getG();
		$g = $from->getG() - $to->getG();
		if ($g > M_PI) {
			$g = (2 * M_PI) - $g;
		}
		if (g < -M_.PI) {
			$g += (2 * M_PI);
		}

		$lA = $to->getL();
		$lD = $from->getL();
		$gcArc = acos((sin($lD) * sin($lA)) + (cos($lD) * cos($lA) * cos($g)));
		// System.out.println(String.format("M: %.03f nm", 60 * rad2deg(gcArc)));
		$ira = asin((sin($g) * cos($lA)) / sin($gcArc));
		if ($ira > 0) { // From the north
			if ($g < 0) { // to West
				$ira = (2 * M_PI) - $ira;
			}
		} else { // From the south
			$ira = abs($ira);
			if ($g > 0) { // to East
				$ira = M_PI - $ira;
			} else { // To West
				$ira = M_PI + $ira;
			}
		}
		return $ira;
	}

	/**
	 * Prefer this one, rather than getInitialRouteAngleInDegreesV2
	 *
	 * @param from all values in degrees
	 * @param to all values in degrees
	 * @return Initial Route Angle in degrees
	 */
	public static function getInitialRouteAngleInDegrees(GreatCirclePoint $from, GreatCirclePoint $to) : float {
//		double g = to.getG() - from.getG();
		$g = $from->getG() - $to->getG();
		if ($g > 180) {
			$g = 360 - $g;
		}
		if ($g < -180) {
			$g += 360;
		}
		$lA = $to->getL();
		$lD = $from->getL();
		$gcArc = acos((sin(deg2rad($lD)) * sin(deg2rad($lA))) + (cos(deg2rad($lD)) * cos(deg2rad($lA)) * cos(deg2rad($g))));
		// System.out.println(String.format("M: %.03f nm", 60 * rad2deg(gcArc)));
		$V = asin((sin(deg2rad($g)) * cos(deg2rad($lA))) / sin($gcArc));
		if ($V > 0) { // From the north
			if ($g < 0) { // to West
				$V = (2 * M_PI) - $V;
			}
		} else { // From the south
//			V = Math.abs(V);
			if ($g > 0) { // to East
				$V = M_PI - $V;
			} else { // To West
				$V = M_PI + $V;
			}
		}
		return rad2deg($V);
	}

	/**
	 * Prefer getInitialRouteAngleInDegrees
	 *
	 * @param from all values in degrees
	 * @param to all values in degrees
	 * @return Initial Route Angle in degrees
	 */
	public static function getInitialRouteAngleInDegreesV2(GreatCirclePoint $from, GreatCirclePoint $to) : float {
//		double g = to.getG() - from.getG();
		$g = $from->getG() - $to->getG();
		if ($g > 180) {
			$g = 360 - $g;
		}
		if ($g < -180) {
			$g += 360;
		}
		$lA = $to->getL();
		$lD = $from->getL();

		$V = atan(sin(deg2rad($g)) / ((cos(deg2rad($lD)) * tan(deg2rad($lA))) - (sin(deg2rad($lD)) * cos(deg2rad($g)))));

		if ($V > 0) { // From the north
			if ($g < 0) { // to West
				$V = (2 * M_PI) - $V;
			}
		} else { // From the south
//			V = Math.abs(V);
			if ($g > 0) { // to East
				$V = M_PI - $V;
			} else { // To West
				$V = M_PI + $V;
			}
		}
		return rad2deg($V);
	}

	public function calculateGreatCircle(int $nbPoints) : void {
		if ($this->arrival->getL() > $this->start->getL()) {
			$this->nsDir = $TO_NORTH;
		} else {
			$this->nsDir = $TO_SOUTH;
		}
		if ($this->arrival->getG() > $this->start->getG()) {
			$this->ewDir = $TO_EAST;
		} else {
			$this->ewDir = $TO_WEST;
		}
		if (abs($this->arrival->getG() - $this->start->getG()) > M_PI) {
			if ($this->ewDir == $TO_EAST) {
				$this->ewDir = $TO_WEST;
				$this->arrival->setG($this->arrival->getG() - (2 * M_PI));
			} else {
				$this->ewDir = $TO_EAST;
				$this->arrival->setG((2 * M_PI) + $this->arrival->getG());
			}
		}
		$deltaG = $this->arrival->getG() - $this->start->getG();
		$this->route = array(); // new Vector<>(nbPoints);
		$interval = $deltaG / (float)$nbPoints;
		$smallStart = $this->start;
		$go = true;
		for ($g = $this->start->getG(); count($this->route) <= $nbPoints; $g += $interval) {
			$deltag = $this->arrival->getG() - $g;
			$tanStartAngle = sin($deltag) / ((cos($smallStart->getL()) * $this->arrival->getL()) - sin($smallStart->getL()) * cos($deltag));
			$smallL = atan(tan($smallStart->getL()) * cos($interval) + sin($interval) / ($tanStartAngle * cos($smallStart->getL())));
			$rpG = $g + $interval;
			if ($rpG > M_PI) {
				$rpG -= (2 * M_PI);
			}
			if ($rpG < -M_PI) {
				$rpG = (2 * M_PI) + $rpG;
			}
			$routePoint = new GreatCirclePoint($smallL, $rpG);
			$ari = rad2deg(atan($tanStartAngle));
			if ($ari < 0.0) {
				$ari = abs($ari);
			}

			$_nsDir;
			if ($routePoint->getL() > $smallStart->getL()) {
				$_nsDir = $TO_NORTH;
			} else {
				$_nsDir = $TO_SOUTH;
			}
			$arrG = $routePoint->getG();
			$staG = $smallStart->getG();
			if (sign($arrG) != sign($staG)) {
				if (sign($arrG) > 0) {
					$arrG -= (2 * M_PI);
				} else {
					$arrG = M_PI - $arrG;
				}
			}
			$_ewDir;
			if ($arrG > $staG) {
				$_ewDir = $TO_EAST;
			} else {
				$_ewDir = $TO_WEST;
			}
			$_start = 0.0;
			if ($_nsDir == $TO_SOUTH) {
				$_start = 180;
				if ($_ewDir == $TO_EAST) {
					$ari = $_start - $ari;
				} else {
					$ari = $_start + $ari;
				}
			} else if ($_ewDir == $TO_EAST) {
				$ari = $_start + $ari;
			} else {
				$ari = $_start - $ari;
			}
			while ($ari < 0.0) {
				$ari += 360;
			}
            array_push($this->route, new GreatCircleWayPoint($smallStart, $arrival->equals($smallStart) ? null : $ari)); // (Double.isNaN($ari) ? null : $ari)));
			$smallStart = $routePoint;
		}
	}

	public function calculateGreatCircle_degrees(int $nbPoints) : void {
		$arrival = new GreatCirclePoint(deg2rad($this->arrival->getL()), deg2rad($this->arrival->getG()));
		$start = new GreatCirclePoint(deg2rad($this->start->getL()), deg2rad($this->start->getG()));

		if ($arrival->getL() > $start->getL()) {
			$this->nsDir = $TO_NORTH;
		} else {
			$this->nsDir = $TO_SOUTH;
		}
		if ($arrival->getG() > $start->getG()) {
			$this->ewDir = $TO_EAST;
		} else {
			$this->ewDir = $TO_WEST;
		}
		if (abs($arrival->getG() - $start->getG()) > M_PI) {
			if ($this->ewDir == $TO_EAST) {
				$this->ewDir = $TO_WEST;
				$arrival->setG($arrival->getG() - (2 * M_PI));
			} else {
				$this->ewDir = $TO_EAST;
				$arrival->setG((2 * M_PI) + $arrival->getG());
			}
		}
		$deltaG = $arrival->getG() - $start->getG();
		$this->route = array();
		$interval = $deltaG / (float)$nbPoints;
		$smallStart = $start;
		$go = true;
		for ($g = $start->getG(); count($this->route) <= $nbPoints; $g += $interval) {
			$deltag = $arrival->getG() - $g;
			$tanStartAngle = sin(deltag) / (cos($smallStart->getL()) * tan($arrival->getL()) - sin($smallStart->getL()) * cos($deltag));
			$smallL = atan(tan($smallStart->getL()) * cos($interval) + sin($interval) / ($tanStartAngle * cos($smallStart->getL())));
			$rpG = $g + $interval;
			if ($rpG > M_PI) {
				$rpG -= (2 * M_PI);
			}
			if ($rpG < -M_PI) {
				$rpG = (2 * M_PI) + $rpG;
			}
			$routePoint = new GreatCirclePoint($smallL, $rpG);
			$ari = rad2deg(atan($tanStartAngle));
			if ($ari < 0.0) {
				$ari = abs($ari);
			}

			$_nsDir;
			if ($routePoint->getL() > $smallStart->getL()) {
				$_nsDir = $TO_NORTH;
			} else {
				$_nsDir = $TO_SOUTH;
			}
			$arrG = $routePoint->getG();
			$staG = $smallStart->getG();
			if (sign($arrG) != sign($staG)) {
				if (sign($arrG) > 0) {
					$arrG -= (2 * M_PI);
				} else {
					$arrG = M_PI - $arrG;
				}
			}
			$_ewDir;
			if ($arrG > $staG) {
				$_ewDir = $TO_EAST;
			} else {
				$_ewDir = $TO_WEST;
			}
			$_start = 0.0;
			if ($_nsDir == $TO_SOUTH) {
				$_start = 180;
				if ($_ewDir == $TO_EAST) {
					$ari = $_start - $ari;
				} else {
					$ari = $_start + $ari;
				}
			} else if ($_ewDir == $TO_EAST) {
				$ari = $_start + $ari;
			} else {
				$ari = $_start - $ari;
			}
			while ($ari < 0.0) {
				$ari += 360;
			}
			array_push($this->route, new GreatCircleWayPoint($smallStart, $arrival->equals($smallStart) ? null : $ari)); // (Double.isNaN(ari) ? null : ari)));
			$smallStart = $routePoint;
		}
	}

	/**
	 * GC Distance
	 * @return in radians
	 */
	public function getDistance() : float {
//		double cos = sin(this.start.getL()) * sin(this.arrival.getL()) + cos(this.start.getL()) *
//				cos(this.arrival.getL()) * cos(this.arrival.getG() - this.start.getG());
//		return Math.acos(cos);
		return GreatCircle::getDistanceWithPoints($this->start, $this->arrival);
	}

	public function getDistance_degrees() : float {
		return GreatCircle::getDistanceWithPoints($this->start->degreesToRadians(), $this->arrival->degreesToRadians());
	}

	public static function getDistanceWithPoints(GreatCirclePoint $from, GreatCirclePoint $to) : float {
		$cos = sin($from->getL()) * sin($to->getL()) + cos($from->getL()) * cos($to->getL()) * cos($to->getG() - $from->getG());
		return acos($cos);
	}

	public function getDistanceInDegrees() : float {
		return rad2deg(getDistance());
	}

	public static function getDistanceInDegreesBetweenPoints(GreatCirclePoint $from, GreatCirclePoint $to) : float {
		return rad2deg(getDistance($from, $to));
	}
	
	public function getDistanceInNM() : float {
		return ($this->getDistanceInDegrees() * 60);
	}

	/**
	 * @param from in degrees
	 * @param to   in degrees
	 * @return in nautical miles
	 */
	public static function getDistanceInNMBetweenPoints(GreatCirclePoint $from, GreatCirclePoint $to) : float {
		return (GreatCircle::getDistanceInDegreesBetweenPoints($from, $to) * 60);
	}

	/**
	 * Input in radians, output in nautical miles.
	 * 
	 * @param from in Radians
	 * @param to   in Radians
	 * @return in miles
	 */
	public static function getGCDistance(GreatCirclePoint $from, GreatCirclePoint $to) : float {
		$cos = sin($from->getL()) * sin($to->getL()) + cos($from->getL()) * cos($to->getL()) * cos($to->getG() - $from->getG());
		$dist = acos($cos);
		return rad2deg($dist) * 60;
	}

	/**
	 * Input in degrees, output in nautical miles.
	 *
	 * @param from in Degrees
	 * @param to   in Degrees
	 * @return in miles
	 */
	public static function getGCDistanceInDegrees(GreatCirclePoint $from, GreatCirclePoint $to) : float {
		$cos = sin(deg2rad($from->getL())) * sin(deg2rad($to->getL())) + cos(deg2rad($from->getL())) * cos(deg2rad($to->getL())) * cos(deg2rad($to->getG()) - deg2rad($from->getG()));
		$dist = acos($cos);
		return rad2deg($dist) * 60;
	}

	public function calculateRhumbLine() : RLData {
		return GreatCircle::calculateRhumbLineBetweenPoints($this->start, $this->arrival);
	}

	public static function calculateRhumbLineBetweenPoints(GreatCirclePoint $from, GreatCirclePoint $to) : RLData {
		$nsDir = -1; $ewDir = -1;
		$rv; $dLoxo;
		if ($to->getL() > $from->getL()) {
			$nsDir = $TO_NORTH;
		} else {
			$nsDir = $TO_SOUTH;
		}
		$arrG = $to->getG();
		$staG = $from->getG();
		if (sign($arrG) != sign($staG) && abs($arrG - $staG) > M_PI) {
			if (sign($arrG) > 0) {
				$arrG -= (2 * M_PI);
			} else {
				$arrG = M_PI - $arrG;
			}
		}
		if ($arrG - $staG > 0.0) {
			$ewDir = $TO_EAST;
		} else {
			$ewDir = $TO_WEST;
		}
		$deltaL = rad2deg(($to->getL() - $from->getL())) * 60;
		$radianDeltaG = $to->getG() - $from->getG();
		if (abs($radianDeltaG) > M_PI) {
			$radianDeltaG = (2 * M_PI) - abs($radianDeltaG);
		}
		$deltaG = rad2deg($radianDeltaG) * 60;
		if ($deltaG < 0.0) {
			$deltaG = -$deltaG;
		}
		$startLC = log(tan((M_PI / 4) + $from->getL() / 2));
		$arrLC = log(tan((M_PI / 4) + $to->getL() / 2));
		$deltaLC = 3437.7467707849396 * ($arrLC - $startLC);
		if ($deltaLC != 0) {
			$rv = atan($deltaG / $deltaLC);
		} else if ($radianDeltaG > 0) {
			$rv = (M_PI / 2);
		} else {
			$rv = (3 * M_PI / 2);
		}
		if ($deltaL != 0) {
			$dLoxo = $deltaL / cos($rv);
		} else {
			$dLoxo = $deltaG * cos($from->getL()); // TASK Make sure that's right...
		}
		if ($dLoxo < 0.0) {
			$dLoxo = -$dLoxo;
		}
		if ($rv < 0.0) {
			$rv = -$rv;
		}
		if ($ewDir == $TO_EAST) {
			if ($nsDir != $TO_NORTH) {
				$rv = M_PI - $rv;
			}
		} else if ($deltaLC != 0) {
			if ($nsDir == $TO_NORTH) {
				$rv = (2 * M_PI) - $rv;
			} else {
				$rv = M_PI + $rv;
			}
		}
		while ($rv >= (2 * M_PI)) {
			$rv -= (2 * M_PI);
		}
		return new RLData($rv, $dLoxo);
	}

	/*
	 * Points coordinates in Radians
	 */
	public function calculateRhumbLineDistance() : float {
		return calculateRhumbLineDistanceBetweenPoints($this->start, $this->arrival);
	}

	public static function calculateRhumbLineDistanceBetweenPoints(GreatCirclePoint $f, GreatCirclePoint $t) : float {
		$_nsDir = 0;
		if ($t->getL() > $f->getL()) {
			$_nsDir = $TO_NORTH;
		} else {
			$_nsDir = $TO_SOUTH;
		}
		$arrG = $t->getG();
		$staG = $f->getG();
		if (sign($arrG) != sign($staG) && abs($arrG - $staG) > M_PI) {
			if (sign($arrG) > 0) {
				$arrG -= (2 * M_PI);
			} else {
				$arrG = M_PI - $arrG;
			}
		}
		$_ewDir;
		if (($arrG - $staG) > 0.0) {
			$_ewDir = $TO_EAST;
		} else {
			$_ewDir = $TO_WEST;
		}
		$deltaL = rad2deg(($t->getL() - $f->getL())) * 60;
		$radianDeltaG = $t->getG() - $f->getG();
		if (abs($radianDeltaG) > M_PI) {
			$radianDeltaG = (2 * M_PI) - abs($radianDeltaG);
		}
		$deltaG = rad2deg(radianDeltaG) * 60;
		if ($deltaG < 0.0) {
			$deltaG = -$deltaG;
		}
		$startLC = log(tan((M_PI / 4) + $f->getL() / 2));
		$arrLC = log(tan((M_PI / 4) + $t->getL() / 2));
		$deltaLC = 3437.7467707849396 * ($arrLC - $startLC);
		$_rv = 0.0;
		if ($deltaLC != 0) {
			$_rv = atan($deltaG / $deltaLC);
		} else {
			if ($radianDeltaG > 0) {
				$_rv = (M_PI / 2);
			} else {
				$_rv = (3 * M_PI / D);
			}
		}
		$_dLoxo = $deltaL / cos($_rv);
		if ($deltaL == 0) {
			$_dLoxo = radianDeltaG * cos(deg2rad($f->getL()));
		}
		if ($_dLoxo < 0.0) {
			$_dLoxo = -$_dLoxo;
		}
		if ($_rv < 0.0) {
			$_rv = -$_rv;
		}
		if ($_ewDir == $TO_EAST) {
			if ($_nsDir != $TO_NORTH) {
				$_rv = M_PI - $_rv;
			}
		} else if ($deltaLC != 0) {
			if ($_nsDir == $TO_NORTH) {
				$_rv = (2 * M_PI) - $_rv;
			} else {
				$_rv = M_PI + $_rv; 
			}
		}
		for (; $_rv >= (2 * M_PI); $_rv -= (2 * M_PI));
		return $_dLoxo;
	}

	public function calculateRhumbLineRoute() : float {
		return calculateRhumbLineRouteBetweenPoints($this->start, $this->arrival);
	}

	public function calculateRhumbLineRoute_degrees() : float {
		return calculateRhumbLineRouteBetweenPoints_degrees($this->start, $this->arrival);
	}
	/**
	 * Rhumbline aka loxodrome
	 * Points coordinates in Radians
	 * returned value in radians
	 */
	public static function calculateRhumbLineRouteBetweenPoints(GreatCirclePoint $f, GreatCirclePoint $t) : float {
		$_nsDir = 0;
		if ($t->getL() > $f->getL()) {
			$_nsDir = $TO_NORTH;
		} else {
			$_nsDir = $TO_SOUTH;
		}
		$arrG = $t->getG();
		$staG = $f->getG();
		if (sign($arrG) != sign($staG) && abs($arrG - $staG) > M_PI) {
			if (sign($arrG) > 0) {
				$arrG -= (2 * M_PI);
			} else {
				$arrG = M_PI - $arrG;
			}
		}
		$_ewDir;
		if ($arrG - $staG > 0.0) {
			$_ewDir = $TO_EAST;
		} else {
			$_ewDir = $TO_WEST;
		}
		$deltaL = rad2deg(($t->getL() - $f->getL())) * 60;
		$radianDeltaG = $t->getG() - $f->getG();
		if (abs($radianDeltaG) > M_PI) {
			$radianDeltaG = (2 * M_PI) - abs($radianDeltaG);
		}
		$deltaG = rad2deg($radianDeltaG) * 60;
		if ($deltaG < 0.0) {
			$deltaG = -$deltaG;
		}
		$startLC = log(tan((M_PI / 4) + $f->getL() / 2));
		$arrLC = log(tan((M_PI / 4) + $t->getL() / 2));
		$deltaLC = 3437.7467707849396 * ($arrLC - $startLC);
		$_rv = 0.0;
		if ($deltaLC != 0) {
			$_rv = atan($deltaG / $deltaLC);
		} else if ($radianDeltaG > 0) {
			$_rv = (M_PI / 2);
		} else {
			$_rv = (3 * M_PI / 2);
		}
		$_dLoxo = $deltaL / cos($_rv);
		if ($_dLoxo < 0.0) {
			$_dLoxo = -$_dLoxo;
		}
		if ($_rv < 0.0) {
			$_rv = -$_rv;
		}
		if ($_ewDir == $TO_EAST) {
			if ($_nsDir != $TO_NORTH) {
				$_rv = M_PI - $_rv;
			}
		} else if ($deltaLC != 0) {
			if ($_nsDir == $TO_NORTH) {
				$_rv = (2 * M_PI) - $_rv;
			} else {
				$_rv = M_PI + $_rv;
			}
		}
		for (; $_rv >= (2 * M_PI); $_rv -= (2 * M_PI));
		return $_rv;
	}

	public static function calculateRhumbLineRouteBetweenPoints_degrees(GreatCirclePoint $f, GreatCirclePoint $t) : float {
		return calculateRhumbLineRoute($f->degreesToRadians(), $t->degreesToRadians());
	}

//	public double getRhumbLineDistance() {
//		return dLoxo;
//	}
//
//	public double getRhumbLineRoute() {
//		return rv;
//	}

	public function getRoute() : array { // Vector<GreatCircleWayPoint>
		return $this->route;
	}

	public static function inDegrees(array $inRads) : array { // Vector<GreatCircleWayPoint>
        foreach($inRads as $rad) {
			$rad->getPoint()->latitude = rad2deg($rad->getPoint()->latitude);
			$rad->getPoint()->longitude = rad2deg($rad->getPoint()->longitude);
		};
		return $inRads;
	}

	/**
	 * Dead Reckoning
	 *
	 * @param from  GeoPoint, L &amp; G in Radians
	 * @param dist  distance in nm
	 * @param route route in degrees
	 * @return DR Position, L &amp; G in Radians
	 */
	public static function dr(GreatCirclePoint $from, float $dist, float $route) : GreatCirclePoint {
		$deltaL = deg2rad($dist / 60) * cos(deg2rad($route));
		$l2 = $from->getL() + $deltaL;
//  double lc1 = log(tan((M_PI / 4) + from.getL() / 2));
//  double lc2 = log(tan((M_PI / 4) + l2 / 2));
//  double deltaLc = lc2 - lc1;
//  double deltaG = deltaLc * tan(deg2rad(route));
		$deltaG = deg2rad($dist / (60 * cos(($from->getL() + $l2) / 2))) * sin(deg2rad($route)); // 2009-mar-10
		$g2 = $from->getG() + $deltaG;
		return new GreatCirclePoint($l2, $g2);
	}

	/**
	 * Dead Reckoning
	 *
	 * @param from  GeoPoint, L &amp; G in Degrees
	 * @param dist  distance in nm
	 * @param route route in degrees
	 * @return DR Position, L &amp; G in Degrees
	 */
	public static function dr_degrees(GreatCirclePoint $from, float $dist, float $route) : GreatCirclePoint {
		$deltaL = deg2rad($dist / 60) * cos(deg2rad($route));
		$l2 = deg2rad($from->getL()) + $deltaL;
		$deltaG = deg2rad($dist / (60 * cos((deg2rad($from->getL()) + $l2) / 2))) * sin(deg2rad($route)); // 2009-mar-10
		$g2 = deg2rad($from->getG()) + $deltaG;
		return new GreatCirclePoint(rad2deg($l2), rad2deg($g2));
	}
}

class GreatCirclePoint {
    private $latitude;   // In Radians
    private $longitude;  // In Radians

    function __construct(float $l, float $g) {
        $this->latitude = $l;
        $this->longitude = $g;
    }

    // function __construct(GeoPoint $gp) {
    //     $this->latitude = $gp->getL();
    //     $this->longitude = $gp->getG();
    // }

    public function getL() : float {
        return $this->latitude;
    }

    public function getG() : float {
        return $this->longitude;
    }

    public function setL(float $l) : void {
        $this->latitude = $l;
    }

    public function setG(float $g) : void {
        $this->longitude = $g;
    }

    public function equals(GreatCirclePoint $p) : bool {
        return ($this->longitude == $p->longitude && $this->latitude == $p->latitude);
    }

    /**
     * In nautical miles
     *
     * @param target the point to aim to.
     * @return the distance, in nm.
     */
    public function orthoDistanceBetween(GreatCirclePoint $target) : float {
        $gc = new GreatCircle();
        $gc->setStart(new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG())));
        $gc->setArrival(new GreatCirclePoint(deg2rad($target->getL()), deg2rad($target->getG())));
        $gc->calculateGreatCircle(1);
        $d = rad2deg($gc->getDistance());
        return $d * 60;
    }

    /**
     * In nautical degrees
     *
     * @param target the point to aim to.
     * @return the distance, in degrees.
     */
    public function gcDistanceBetween(GreatCirclePoint $target) : float {
        return GreatCircle::getGCDistanceInDegrees($this, $target);
    }

    /**
     * In nautical miles
     *
     * @param target the point to aim to.
     * @return the distance, in nm.
     */
    public function loxoDistanceBetween(GreatCirclePoint $target) : float {
        $gc = new GreatCircle();
        $gc->setStart(new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG())));
        $gc->setArrival(new GreatCirclePoint(deg2rad($target->getL()), deg2rad($target->getG())));
        $rlData = $gc->calculateRhumbLine();
        return $rlData->getdLoxo();
    }

    public function toString() : string {
        return sprintf("%s / %s", Utils::decToSex($this->latitude, Utils::NS, Utils::$LEADING_SIGN, false), Utils::decToSex($this->longitude, Utils::EW, Utils::$LEADING_SIGN, false));
    }

    public function degreesToRadians() : GreatCirclePoint {
        return new GreatCirclePoint(deg2rad($this->getL()), deg2rad($this->getG()));
    }

    public function radiansToDegrees() : GreatCirclePoint {
        return new GreatCirclePoint(rad2deg($this->getL()), rad2deg($this->getG()));
    }

}

class GreatCircleWayPoint {
	private $p; // GreatCirclePoint
	private $z; // float

	public function toString() : string {
		return "GreatCircleWayPoint{ p=" . $this->p->toString() . ", z=" . $this->z . "}";
	}

	function __construct(GreatCirclePoint $p, float $z) {
		$this->p = $p;
		$this->z = $z;
	}

	public function getPoint() : GreatCirclePoint {
		return $this->p;
	}

	public function getZ() : float {
		return $this->z;
	}
}

class GreatCircleWayPointWithBodyFromPos extends GreatCircleWayPoint {
    private $wpFromPos; // BodyFromPos

    // function __construct(GreatCirclePoint $p, float $z) {
    //     super($p, $z);
    // }

    public function getWpFromPos() : BodyFromPos {
        return $this->wpFromPos;
    }

    public function setWpFromPos(BodyFromPos $wpFromPos) : void {
        $this->wpFromPos = $wpFromPos;
    }

    public function toString() : string {
        return "GreatCircleWayPointWithBodyFromPos{" . " wpFromPos=" . $this->wpFromPos->toString() . "}";
    }
}
