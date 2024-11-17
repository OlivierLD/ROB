<?php

class AstroComputer {

    private $context;
    private $calculateHasBeenInvoked = false;
    private $year;
    private $month;
    private $day;
    private $hour;
    private $minute;
    private $second;
    private $deltaT;

    function __construct() {
       $this->context = new ContextV2();
       $this->calculateHasBeenInvoked = false;
    }

    public function setDateTime(int $y, int $m, int $d, int $h, int $mi, int $s) : void {
        $this->year = $y;
        $this->month = $m;
        $this->day = $d;
        $this->hour = $h;
        $this->minute = $mi;
        $this->second = $s;
        $this->calculateHasBeenInvoked = false;
        $this->context->starName = null;
    }

    public function setDeltaT(float $deltaT) : void {
        $this->deltaT = $deltaT;
    }
    public function getDeltaT() : float {
        return $this->deltaT;
    }

    public function calculate(int $y, int $m, int $d, int $h, int $mi, int $s, bool $reCalcDeltaT) : void {
        $this->setDateTime($y, $m, $d, $h, $mi, $s);
        // this.calculate(reCalcDeltaT);

        if ($reCalcDeltaT) {
            $deltaT = TimeUtil::getDeltaT($this->year, $this->month);
            $this->deltaT = $deltaT;
        }

        Core::julianDate($this->context, $this->year, $this->month, $this->day, $this->hour, $this->minute, $this->second, $this->deltaT);
        Anomalies::nutation($this->context);
        Anomalies::aberration($this->context);

        Core::aries($this->context);
        Core::sun($this->context);

        // And more here...

    }

    public function getContext() : ContextV2 {
        return $this->context;
    }

}