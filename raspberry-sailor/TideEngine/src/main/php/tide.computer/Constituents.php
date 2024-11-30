<?php

class ConstSpeed {

    private $idx = 0;
    private $coeffName = "";
    private $coeffValue = 0;
    private $equilibrium = array();
    private $factors = array();

    function __construct(int $idx, string $name, float $val) {
        $this->idx = $idx;
        $this->coeffName = $name;
        $this->coeffValue = $val;
    }

    public function putEquilibrium(int $year, float $val) : void {
        $this->equilibrium[sprintf("%d", $year)] = $val; // Append to array, with key
    }

    public function putFactor(int $year, float $val) : void {
        $this->factors[sprintf("%d", $year)] = $val; // Append to array, with key
    }

    public function getCoeffName() : string {
        return $this->coeffName;
    }

    public function getCoeffValue() : float {
        return $this->coeffValue;
    }

    public function getEquilibrium() : array {
        return $this->equilibrium;
    }

    public function getFactors() : array {
        return $this->factors;
    }
}

class Constituents {
    private $constSpeedMap = array();

	public function getConstSpeedMap() : array {
		return $this->constSpeedMap;
	}

    public function appendToConstSpeedMap(string $key, ConstSpeed $value) : void {
        $this->constSpeedMap[$key] = $value;
    }

}