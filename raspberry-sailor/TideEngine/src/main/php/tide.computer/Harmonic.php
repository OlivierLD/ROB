<?php

class Harmonic {

	private $name = "";
	private $amplitude = 0;
	private $epoch = 0;

	function __construct(string $name, float $ampl, float $e) {
		$this->name = $name;
		$this->amplitude = $ampl;
		$this->epoch = $e;
	}

	public function getName() : string {
		return $this->name;
	}

	public function getAmplitude() : float {
		return $this->amplitude;
	}

	public function getEpoch() : float {
		return $this->epoch;
	}

	public function setName(string $name) : void {
		$this->name = $name;
	}

	public function setAmplitude(float $amplitude) : void {
		$this->amplitude = $amplitude;
	}

	public function setEpoch(float $epoch) : void {
		$this->epoch = $epoch;
	}
}