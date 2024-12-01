<?php

class TideStation {
	public static $METERS = "meters";
	public static $FEET = "feet";
	public static $KNOTS = "knots";
	public static $SQUARE_KNOTS = "knots^2";

	private $fullName = "";
	private $nameParts = array(); // new ArrayList<>();
	private $latitude = 0;
	private $longitude = 0;
	private $baseHeight = 0;
	private $unit = "";
	private $timeZone = "";
	private $timeOffset = "";
	private $harmonics = array(); //  new ArrayList<>();

	private $harmonicsHaveBeenFixedForYear = -1;

	public function setFullName(string $fullName) : void {
		$this->fullName = $fullName;
	}

	public function getFullName() : string {
		return $this->fullName;
	}

	public function getNameParts() : array {
		return $this->nameParts;
	}

	public function appendNamePart(string $part) : void {
		array_push($this->nameParts, $part);
	}

	public function setLatitude(float $latitude) : void {
		$this->latitude = $latitude;
	}

	public function getLatitude() : float {
		return $this->latitude;
	}

	public function setLongitude(float $longitude) : void {
		$this->longitude = $longitude;
	}

	public function getLongitude() : float {
		return $this->longitude;
	}

	public function setBaseHeight(float $baseHeight) : void {
		$this->baseHeight = $baseHeight;
	}

	public function getBaseHeight() : float {
		return $this->baseHeight;
	}

	public function setUnit(string $unit) : void {
		$this->unit = $unit;
	}

	public function getUnit() : string {
		return $this->unit;
	}

	public function getHarmonics() : array {
		return $this->harmonics;
	}

	public function setHarmonics(array $harmonics) : void {
		$this->harmonics = $harmonics;
	}

    public function appendToHarmonics(Harmonic $harmonic) : void {
		// echo("In " . $this->getFullName() . ", appending harmonic " . $harmonic->getName() . ".<br/>" . PHP_EOL);
        array_push($this->harmonics, $harmonic);
    }

	public function setTimeZone(string $timeZone) : void {
		$this->timeZone = $timeZone;
	}

	public function getTimeZone() : string {
		return $this->timeZone;
	}

	public function setTimeOffset(string $timeOffset) : void {
		$this->timeOffset = $timeOffset;
	}

	public function getTimeOffset() : string {
		return $this->timeOffset;
	}

	public function isCurrentStation() : bool {
		return TideUtilities::startsWith($this->unit, self::$KNOTS);
	}

	public function isTideStation() : bool {
		return ! TideUtilities::startsWith($this->unit, self::$KNOTS);
	}

	public function getDisplayUnit() : string {
		if ($this->unit == (self::$SQUARE_KNOTS)) {
			return self::$KNOTS;
		} else {
			return $this->unit;
		}
	}

	public function setHarmonicsFixedForYear(int $y) : void {
		$this->harmonicsHaveBeenFixedForYear = $y;
	}

	public function yearHarmonicsFixed() : int {
		return $this->harmonicsHaveBeenFixedForYear;
	}

	public function toString() : string {
		return $this->getFullName();
	}
}
