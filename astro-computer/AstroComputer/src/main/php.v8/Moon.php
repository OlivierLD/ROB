<?php
/**
 * @author    Olivier Le Diouris <olivier.lediouris@gmail.com>
 * @copyright 2024, OlivSoft
 * @license   Proprietary
 */

class Moon {
	public static function compute(ContextV2 $context) : void {
		// Mean longitude of the moon
		$Lmm = Utils::trunc(218.3164591 + 481267.88134236 * $context->TE - 0.0013268 * $context->TE2 + $context->TE3 / 538841 - $context->TE4 / 65194000);

		//Mean elongation of the moon
		$D = Utils::trunc(297.8502042 + 445267.1115168 * $context->TE - 0.00163 * $context->TE2 + $context->TE3 / 545868 - $context->TE4 / 113065000);

		// Mean anomaly of the sun
		$Msm = Utils::trunc(357.5291092 + 35999.0502909 * $context->TE - 0.0001536 * $context->TE2 + $context->TE3 / 24490000);

		// Mean anomaly of the moon
		$Mmm = Utils::trunc(134.9634114 + 477198.8676313 * $context->TE + 0.008997 * $context->TE2 + $context->TE3 / 69699 - $context->TE4 / 14712000);

		// Mean distance of the moon from ascending node
		$F = Utils::trunc(93.2720993 + 483202.0175273 * $context->TE - 0.0034029 * $context->TE2 - $context->TE3 / 3526000 + $context->TE4 / 863310000);

		// Corrections
		$A1 = Utils::trunc(119.75 + 131.849 * $context->TE);
		$A2 = Utils::trunc(53.09 + 479264.29 * $context->TE);
		$A3 = Utils::trunc(313.45 + 481266.484 * $context->TE);
		$fE = 1 - 0.002516 * $context->TE - 0.0000074 * $context->TE2;
		$fE2 = $fE * $fE;

		// Periodic terms for the moon:
		// Longitude and distance
		$ld = [
            [ 0, 0, 1, 0, 6288774, -20905355 ],
            [ 2, 0, -1, 0, 1274027, -3699111 ],
            [ 2, 0, 0, 0, 658314, -2955968 ],
            [ 0, 0, 2, 0, 213618, -569925 ],
            [ 0, 1, 0, 0, -185116, 48888 ],
            [ 0, 0, 0, 2, -114332, -3149 ],
            [ 2, 0, -2, 0, 58793, 246158 ],
            [ 2, -1, -1, 0, 57066, -152138 ],
            [ 2, 0, 1, 0, 53322, -170733 ],
            [ 2, -1, 0, 0, 45758, -204586 ],
            [ 0, 1, -1, 0, -40923, -129620 ],
            [ 1, 0, 0, 0, -34720, 108743 ],
            [ 0, 1, 1, 0, -30383, 104755 ],
            [ 2, 0, 0, -2, 15327, 10321 ],
            [ 0, 0, 1, 2, -12528, 0 ],
            [ 0, 0, 1, -2, 10980, 79661 ],
            [ 4, 0, -1, 0, 10675, -34782 ],
            [ 0, 0, 3, 0, 10034, -23210 ],
            [ 4, 0, -2, 0, 8548, -21636 ],
            [ 2, 1, -1, 0, -7888, 24208 ],
            [ 2, 1, 0, 0, -6766, 30824 ],
            [ 1, 0, -1, 0, -5163, -8379 ],
            [ 1, 1, 0, 0, 4987, -16675 ],
            [ 2, -1, 1, 0, 4036, -12831 ],
            [ 2, 0, 2, 0, 3994, -10445 ],
            [ 4, 0, 0, 0, 3861, -11650 ],
            [ 2, 0, -3, 0, 3665, 14403 ],
            [ 0, 1, -2, 0, -2689, -7003 ],
            [ 2, 0, -1, 2, -2602, 0 ],
            [ 2, -1, -2, 0, 2390, 10056 ],
            [ 1, 0, 1, 0, -2348, 6322 ],
            [ 2, -2, 0, 0, 2236, -9884 ],
            [ 0, 1, 2, 0, -2120, 5751 ],
            [ 0, 2, 0, 0, -2069, 0 ],
            [ 2, -2, -1, 0, 2048, -4950 ],
            [ 2, 0, 1, -2, -1773, 4130 ],
            [ 2, 0, 0, 2, -1595, 0 ],
            [ 4, -1, -1, 0, 1215, -3958 ],
            [ 0, 0, 2, 2, -1110, 0 ],
            [ 3, 0, -1, 0, -892, 3258 ],
            [ 2, 1, 1, 0, -810, 2616 ],
            [ 4, -1, -2, 0, 759, -1897 ],
            [ 0, 2, -1, 0, -713, -2117 ],
            [ 2, 2, -1, 0, -700, 2354 ],
            [ 2, 1, -2, 0, 691, 0 ],
            [ 2, -1, 0, -2, 596, 0 ],
            [ 4, 0, 1, 0, 549, -1423 ],
            [ 0, 0, 4, 0, 537, -1117 ],
            [ 4, -1, 0, 0, 520, -1571 ],
            [ 1, 0, -2, 0, -487, -1739 ],
            [ 2, 1, 0, -2, -399, 0 ],
            [ 0, 0, 2, -2, -381, -4421 ],
            [ 1, 1, 1, 0, 351, 0 ],
            [ 3, 0, -2, 0, -340, 0 ],
            [ 4, 0, -3, 0, 330, 0 ],
            [ 2, -1, 2, 0, 327, 0 ],
            [ 0, 2, 1, 0, -323, 1165 ],
            [ 1, 1, -1, 0, 299, 0 ],
            [ 2, 0, 3, 0, 294, 0 ],
            [ 2, 0, -1, -2, 0, 8752 ]
        ];

		$lat = [
            [ 0, 0, 0, 1, 5128122 ],
            [ 0, 0, 1, 1, 280602 ],
            [ 0, 0, 1, -1, 277693 ],
            [ 2, 0, 0, -1, 173237 ],
            [ 2, 0, -1, 1, 55413 ],
            [ 2, 0, -1, -1, 46271 ],
            [ 2, 0, 0, 1, 32573 ],
            [ 0, 0, 2, 1, 17198 ],
            [ 2, 0, 1, -1, 9266 ],
            [ 0, 0, 2, -1, 8822 ],
            [ 2, -1, 0, -1, 8216 ],
            [ 2, 0, -2, -1, 4324 ],
            [ 2, 0, 1, 1, 4200 ],
            [ 2, 1, 0, -1, -3359 ],
            [ 2, -1, -1, 1, 2463 ],
            [ 2, -1, 0, 1, 2211 ],
            [ 2, -1, -1, -1, 2065 ],
            [ 0, 1, -1, -1, -1870 ],
            [ 4, 0, -1, -1, 1828 ],
            [ 0, 1, 0, 1, -1794 ],
            [ 0, 0, 0, 3, -1749 ],
            [ 0, 1, -1, 1, -1565 ],
            [ 1, 0, 0, 1, -1491 ],
            [ 0, 1, 1, 1, -1475 ],
            [ 0, 1, 1, -1, -1410 ],
            [ 0, 1, 0, -1, -1344 ],
            [ 1, 0, 0, -1, -1335 ],
            [ 0, 0, 3, 1, 1107 ],
            [ 4, 0, 0, -1, 1021 ],
            [ 4, 0, -1, 1, 833 ],
            [ 0, 0, 1, -3, 777 ],
            [ 4, 0, -2, 1, 671 ],
            [ 2, 0, 0, -3, 607 ],
            [ 2, 0, 2, -1, 596 ],
            [ 2, -1, 1, -1, 491 ],
            [ 2, 0, -2, 1, -451 ],
            [ 0, 0, 3, -1, 439 ],
            [ 2, 0, 2, 1, 422 ],
            [ 2, 0, -3, -1, 421 ],
            [ 2, 1, -1, 1, -366 ],
            [ 2, 1, 0, 1, -351 ],
            [ 4, 0, 0, 1, 331 ],
            [ 2, -1, 1, 1, 315 ],
            [ 2, -2, 0, -1, 302 ],
            [ 0, 0, 1, 3, -283 ],
            [ 2, 1, 1, -1, -229 ],
            [ 1, 1, 0, -1, 223 ],
            [ 1, 1, 0, 1, 223 ],
            [ 0, 1, -2, -1, -220 ],
            [ 2, 1, -1, -1, -220 ],
            [ 1, 0, 1, 1, -185 ],
            [ 2, -1, -2, -1, 181 ],
            [ 0, 1, 2, 1, -177 ],
            [ 4, 0, -2, -1, 176 ],
            [ 4, -1, -1, -1, 166 ],
            [ 1, 0, 1, -1, -164 ],
            [ 4, 0, 1, -1, 132 ],
            [ 1, 0, -1, -1, -119 ],
            [ 4, -1, 0, -1, 115 ],
            [ 2, -2, 0, 1, 107 ]
        ];
		//Reading periodic terms
		$fD; $fD2; $fM; $fM2; $fMm; $fMm2; $fF; $fF2; $coeffs; $coeffs2; $coeffc; $f; $f2; $sumL = 0; $sumR = 0; $sumB = 0;

		for ($x = 0; $x < count($ld); $x++) {
			$fD = $ld[$x][0];
			$fM = $ld[$x][1];
			$fMm = $ld[$x][2];
			$fF = $ld[$x][3];
			$coeffs = $ld[$x][4];
			$coeffc = $ld[$x][5];
			if ($fM == 1 || $fM == -1) {
				$f = $fE;
			} else if ($fM == 2 || $fM == -2) {
				$f = $fE2;
			} else {
				$f = 1;
			}
			$sumL += $f * $coeffs * Utils::sind($fD * $D + $fM * $Msm + $fMm * $Mmm + $fF * $F);
			$sumR += $f * $coeffc * Utils::cosd($fD * $D + $fM * $Msm + $fMm * $Mmm + $fF * $F);
			$fD2 = $lat[$x][0];
			$fM2 = $lat[$x][1];
			$fMm2 = $lat[$x][2];
			$fF2 = $lat[$x][3];
			$coeffs2 = $lat[$x][4];
			if ($fM2 == 1 || $fM2 == -1) {
				$f2 = $fE;
			} else if ($fM2 == 2 || $fM2 == -2) {
				$f2 = $fE2;
			} else {
				$f2 = 1;
			}
			$sumB += $f2 * $coeffs2 * Utils::sind($fD2 * $D + $fM2 * $Msm + $fMm2 * $Mmm + $fF2 * $F);
		}

		// Corrections
		$sumL = $sumL + 3958 * Utils::sind($A1) + 1962 * Utils::sind($Lmm - $F) + 318 * Utils::sind($A2);
		$sumB = $sumB - 2235 * Utils::sind($Lmm) + 382 * Utils::sind($A3) + 175 * Utils::sind($A1 - $F) + 175 * Utils::sind($A1 + $F) + 127 * Utils::sind($Lmm - $Mmm) - 115 * Utils::sind($Lmm + $Mmm);

		// Longitude of the moon
		$lambdaMm = Utils::trunc($Lmm + $sumL / 1000000);

		// Latitude of the moon
		$betaM = $sumB / 1000000;

		// Distance earth-moon
		$dEM = 385000.56 + $sumR / 1000;

		// Apparent longitude of the moon
		$context->lambdaMapp = $lambdaMm + $context->delta_psi;

		// Right ascension of the moon, apparent
		$context->RAmoon = rad2deg(Utils::trunc2(atan2((Utils::sind($context->lambdaMapp) * Utils::cosd($context->eps) - Utils::tand($betaM) * Utils::sind($context->eps)), Utils::cosd($context->lambdaMapp))));

		// Declination of the moon
		$context->DECmoon = rad2deg(asin(Utils::sind($betaM) * Utils::cosd($context->eps) + Utils::cosd($betaM) * Utils::sind($context->eps) * Utils::sind($context->lambdaMapp)));

		// GHA of the moon
		$context->GHAmoon = Utils::trunc($context->GHAAtrue - $context->RAmoon);

		// Horizontal parallax of the moon
		$context->HPmoon = rad2deg(3600 * asin(6378.14 / $dEM));

		// Semi-diameter of the moon
		$context->SDmoon = rad2deg(3600 * asin(1738 / $dEM));

		// Geocentric angular distance between moon and sun
		$context->LDist = rad2deg(acos(Utils::sind($context->DECmoon) * Utils::sind($context->DECsun) + Utils::cosd($context->DECmoon) * Utils::cosd($context->DECsun) * Utils::cosd($context->RAmoon - $context->RAsun)));

		// Phase angle
		$radianPhase = atan2($context->dES * Utils::sind($context->LDist), ($dEM - $context->dES * Utils::cosd($context->LDist)));
		$context->moonPhase = rad2deg($radianPhase);

		// Illumination of the moon's disk
		$k = 100 * (1 + cos($radianPhase)) / 2;
		$context->k_moon = $k; // Math.round(10D * k) / 10D;

		$context->moonEoT = 4 * $context->GHAmoon + 720 - 1440 * $context->dayfraction;
		if ($context->moonEoT > 20) {
			$context->moonEoT -= 1440;
		}
		if ($context->moonEoT < -20) {
			$context->moonEoT += 1440;
		}
	}
}
