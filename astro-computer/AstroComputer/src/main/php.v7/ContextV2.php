<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */
declare(strict_types=1);

class ContextV2 {
    public static $EPS0_2000 = 23.439291111;

    public $T, $T2, $T3, $T4, $T5, $TE, $TE2, $TE3, $TE4, $TE5, $Tau, $Tau2, $Tau3, $Tau4, $Tau5, $deltaT;
    public $eps0, $eps, $delta_psi, $delta_eps;
    public $Le, $Be, $Re;
    public $kappa, $pi0, $e;
    public $lambda_sun, $RAsun, $DECsun, $GHAsun, $SDsun, $HPsun, $EoT;
    public $RAvenus, $DECvenus, $GHAvenus, $SDvenus, $HPvenus;
    public $RAmars, $DECmars, $GHAmars, $SDmars, $HPmars;
    public $RAjupiter, $DECjupiter, $GHAjupiter, $SDjupiter, $HPjupiter;
    public $RAsaturn, $DECsaturn, $GHAsaturn, $SDsaturn, $HPsaturn;
    public $RAmoon, $DECmoon, $GHAmoon, $SDmoon, $HPmoon, $moonEoT;
    public $RApol, $DECpol, $GHApol; //, $RApolaris, $DECpolaris, $GHApolaris;
    public $OoE, $tOoE, $LDist, $starMoonDist;

    public $moonJupiterDist, $moonVenusDist, $moonMarsDist, $moonSaturnDist;

    public $moonPhase;

    public $JD0h, $JDE, $JD;
    public $lambda, $beta, $dES, $lambdaMapp, $dayfraction;

    public $GHAAtrue, $Lsun_mean, $Lsun_true, $k_moon, $k_venus, $k_mars, $k_jupiter, $k_saturn;

    public $starName = null; // Nullable
    public $GHAstar, $SHAstar, $DECstar;

}
