<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */
declare(strict_types=1);

class AstroComputer {

    private $context;
    private $calculateHasBeenInvoked = false;

    private $year = -1, $month = -1, $day = -1, $hour = -1, $minute = -1, $second = -1;
    private $deltaT = 66.4749; // 2011. Overridden by deltaT system variable, or calculated on the fly.

    private static $WEEK_DAYS = [ "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" ];
    private $dow = "";
    private $moonPhase = "";

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

        Moon::compute($this->context);

        Venus::compute($this->context);
        Mars::compute($this->context);
        Jupiter::compute($this->context);
        Saturn::compute($this->context);

        Core::polaris($this->context);

        $this->moonPhase = Core::moonPhase($this->context);
        $this->dow = AstroComputer::$WEEK_DAYS[Core::weekDay($this->context)];

        $this->calculateHasBeenInvoked = true;

    }

    public function getContext() : ContextV2 {
        return $this->context;
    }

    // More functions, for stars !

    public function starPos(string $starName) : void { 
        if (!$this->calculateHasBeenInvoked) {
            throw new Exception("Calculation was never invoked in this context");
        }
        $this->context->starName = $starName;
        //Read catalog
        $star = Star::getStar($starName);
        if ($star != null) {
            // Read star in catalog
            $RAstar0 = 15 * $star->getRa();
            $DECstar0 = $star->getDec();
            $dRAstar = 15 * $star->getDeltaRa() / 3600;
            $dDECstar = $star->getDeltaDec() / 3600;
            $par = $star->getPar() / 3600;

            // Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
            $RAstar1 = $RAstar0 + $this->context->TE * $dRAstar;
            $DECstar1 = $DECstar0 + $this->context->TE * $dDECstar;

            // Mean obliquity of ecliptic at 2000.0 in degrees
            //    double eps0_2000 = 23.439291111;

            // Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
            $lambdastar1 = atan2((Utils::sind($RAstar1) * Utils::cosd(ContextV2::$EPS0_2000) + Utils::tand($DECstar1) * Utils::sind(ContextV2::$EPS0_2000)), Utils::cosd($RAstar1));
            $betastar1 = asin(Utils::sind($DECstar1) * Utils::cosd(ContextV2::$EPS0_2000) - Utils::cosd($DECstar1) * Utils::sind(ContextV2::$EPS0_2000) * Utils::sind($RAstar1));

            // Precession
            $eta = deg2rad(47.0029 * $this->context->TE - 0.03302 * $this->context->TE2 + 0.00006 * $this->context->TE3) / 3600;
            $PI0 = deg2rad(174.876384 - (869.8089 * $this->context->TE + 0.03536 * $this->context->TE2) / 3600);
            $p0 = deg2rad(5029.0966 * $this->context->TE + 1.11113 * $this->context->TE2 - 0.0000006 * $this->context->TE3) / 3600;
            $A1 = cos($eta) * cos($betastar1) * sin($PI0 - $lambdastar1) - sin($eta) * sin($betastar1);
            $B1 = cos($betastar1) * cos($PI0 - $lambdastar1);
            $C1 = cos($eta) * sin($betastar1) + sin($eta) * cos($betastar1) * sin($PI0 - $lambdastar1);
            $lambdastar2 = $p0 + $PI0 - atan2($A1, $B1);
            $betastar2 = asin($C1);

            //Annual parallax
            $par_lambda = deg2rad($par * sin(deg2rad($this->context->Lsun_true) - $lambdastar2) / cos($betastar2));
            $par_beta = -deg2rad($par * sin($betastar2) * cos(deg2rad($this->context->Lsun_true) - $lambdastar2));

            $lambdastar2 += $par_lambda;
            $betastar2 += $par_beta;

            // Nutation in longitude
            $lambdastar2 += deg2rad($this->context->delta_psi);

            // Aberration
//    double kappa = deg2rad(20.49552) / 3600d;
//    double pi0 = deg2rad(102.93735 + 1.71953 *$this->context->TE + 0.00046 * Context.TE2);
//    double e = 0.016708617 - 0.000042037 * Context.TE - 0.0000001236 * Context.TE2;

            $dlambdastar = ($this->context->e * $this->context->kappa * cos($this->context->pi0 - $lambdastar2) - $this->context->kappa * cos(deg2rad($this->context->Lsun_true) - $lambdastar2)) / cos($betastar2);
            $dbetastar = -$this->context->kappa * sin($betastar2) * (sin(deg2rad($this->context->Lsun_true) - $lambdastar2) - $this->context->e * sin($this->context->pi0 - $lambdastar2));

            $lambdastar2 += $dlambdastar;
            $betastar2 += $dbetastar;

            // Transformation back to equatorial coordinates in radians
            $RAstar2 = atan2((sin($lambdastar2) * Utils::cosd($this->context->eps) - tan($betastar2) * Utils::sind($this->context->eps)), cos($lambdastar2));
            $DECstar2 = asin(sin($betastar2) * Utils::cosd($this->context->eps) + cos($betastar2) * Utils::sind($this->context->eps) * sin($lambdastar2));

            //Lunar distance of star
           $this->context->starMoonDist = rad2deg(acos(Utils::sind($this->context->DECmoon) * sin($DECstar2) + Utils::cosd($this->context->DECmoon) * cos($DECstar2) * Utils::cosd($this->context->RAmoon - rad2deg($RAstar2))));

            // Finals
           $this->context->GHAstar = Utils::trunc($this->context->GHAAtrue - rad2deg($RAstar2));
           $this->context->SHAstar = Utils::trunc(360 - rad2deg($RAstar2));
           $this->context->DECstar = rad2deg($DECstar2);
        } else {
            throw new Exception(sprintf(starName + " not found in the catalog..."));
        }
    }

    public function getStarGHA(string $starName) : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new Exception("Calculation was never invoked in this context");
        }
        if ($starName != ($this->context->starName)) {
            throw new Exception(sprintf("starPos was not invoked for %s (%s)", starName, $this->context->starName));
        }
        return $this->context->GHAstar;
    }
    public function getStarSHA(string $starName) : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new Exception("Calculation was never invoked in this context");
        }
        if ($starName != $this->context->starName) {
            throw new Exception(sprintf("starPos was not invoked for %s (%s)", $starName, $this->context->starName));
        }
        return $this->context->SHAstar;
    }
    public function getStarDec(string $starName) : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new Exception("Calculation was never invoked in this context");
        }
        if ($starName != $this->context->starName) {
            throw new Exception(sprintf("starPos was not invoked for %s (%s)", starName, $this->context->starName));
        }
        return $this->context->DECstar;
    }
    public function getStarMoonDist(string $starName) : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new Exception("Calculation was never invoked in this context");
        }
        if ($starName != $this->context->starName) {
            throw new Exception(sprintf("starPos was not invoked for %s (%s)", starName, $this->context->starName));
        }
        return $this->context->starMoonDist;
    }

    public function getSunDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECsun;
    }

    public function getSunGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAsun;
    }

    public function getSunRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAsun;
    }

    public function getSunSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDsun;
    }

    public function getSunHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPsun;
    }

    public function getAriesGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAAtrue;
    }

    public function getMoonDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECmoon;
    }

    public function getMoonGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAmoon;
    }

    public function getMoonRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAmoon;
    }

    public function getMoonSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDmoon;
    }

    public function getMoonHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPmoon;
    }

    public function getVenusDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECvenus;
    }

    public function getVenusGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAvenus;
    }

    public function getVenusRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAvenus;
    }

    public function getVenusSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDvenus;
    }

    public function getVenusHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPvenus;
    }

    public function getMarsDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECmars;
    }

    public function getMarsGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAmars;
    }

    public function getMarsRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAmars;
    }

    public function getMarsSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDmars;
    }

    public function getMarsHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPmars;
    }

    public function getJupiterDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECjupiter;
    }

    public function getJupiterGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAjupiter;
    }

    public function getJupiterRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAjupiter;
    }

    public function getJupiterSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDjupiter;
    }

    public function getJupiterHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPjupiter;
    }

    public function getSaturnDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECsaturn;
    }

    public function getSaturnGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHAsaturn;
    }

    public function getSaturnRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RAsaturn;
    }

    public function getSaturnSd() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->SDsaturn;
    }

    public function getSaturnHp() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->HPsaturn;
    }

    public function getPolarisDecl() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->DECpol;
    }

    public function getPolarisGHA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->GHApol;
    }

    public function getPolarisRA() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->RApol;
    }

    public function getEoT() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->EoT;
    }

    public function getLDist() : float { // Moon Sun
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->LDist;
    }

    public function getVenusMoonDist() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->moonVenusDist;
    }

    public function getMarsMoonDist() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->moonMarsDist;
    }

    public function getJupiterMoonDist() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->moonJupiterDist;
    }

    public function getSaturnMoonDist() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->moonSaturnDist;
    }

    public function getWeekDay() : string {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.dow;
    }

    public function getMoonPhaseStr() : string {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.moonPhase;
    }

    // Etc. Whatever is needed

    public function getMeanObliquityOfEcliptic() : float {
        if (!$this->calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return $this->context->eps0;
    }

    public static function ghaToLongitude(float $gha) : float {
        $longitude = 0;
        if ($gha < 180) {
            $longitude = -$gha;
        }
        if ($gha >= 180) {
            $longitude = 360 - $gha;
        }
        return $longitude;
    }

    public static function longitudeToGHA(float $longitude) : float {
        $gha = 0;
        if ($longitude < 0) { // W
            $gha = -$longitude;
        }
        if ($longitude >= 0) { // E
            $gha = 360 - $longitude;
        }
        return gha;
    }

    public static function raToGHA(float $ra, float $ghaAries) : float {
        $gha = (360 - (15 * $ra) + $ghaAries) % 360.0;
        return $gha;
    }

}