<?php
declare(strict_types=1);

/**
 * Class ContextV2
 */
class ContextV2 {
    public float $EPS0_2000 = 23.439291111;

    public float $T, $T2, $T3, $T4, $T5, $TE, $TE2, $TE3, $TE4, $TE5, $Tau, $Tau2, $Tau3, $Tau4, $Tau5, $deltaT;
    public float $eps0, $eps, $delta_psi, $delta_eps;
    public float $Le, $Be, $Re;
    public float $kappa, $pi0, $e;
    public float $lambda_sun, $RAsun, $DECsun, $GHAsun, $SDsun, $HPsun, $EoT;
    public float $RAvenus, $DECvenus, $GHAvenus, $SDvenus, $HPvenus;
    public float $RAmars, $DECmars, $GHAmars, $SDmars, $HPmars;
    public float $RAjupiter, $DECjupiter, $GHAjupiter, $SDjupiter, $HPjupiter;
    public float $RAsaturn, $DECsaturn, $GHAsaturn, $SDsaturn, $HPsaturn;
    public float $RAmoon, $DECmoon, $GHAmoon, $SDmoon, $HPmoon, $moonEoT;
    public float $RApol, $DECpol, $GHApol; //, $RApolaris, $DECpolaris, $GHApolaris;
    public float $OoE, $tOoE, $LDist, $starMoonDist;

    public float $moonJupiterDist, $moonVenusDist, $moonMarsDist, $moonSaturnDist;

    public float $moonPhase;

    public float $JD0h, $JDE, $JD;
    public float $lambda, $beta, $dES, $lambdaMapp, $dayfraction;

    public float $GHAAtrue, $Lsun_mean, $Lsun_true, $k_moon, $k_venus, $k_mars, $k_jupiter, $k_saturn;

    public ?string $starName = null; // Nullable
    public float $GHAstar, $SHAstar, $DECstar;

}
