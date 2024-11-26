<?php

// Celestial Almanac publication
// Wow.

try {
    set_time_limit(3600); // In seconds. 300: 5 minutes, 3600: one hour
    // phpinfo();
    include __DIR__ . '/../../main/php.v7/autoload.php';

    $VERBOSE = false;

    $phpVersion = (int)phpversion()[0];
    if ($phpVersion < 7) {
        echo("PHP Version is " . phpversion() . "... This might be too low.");
    }

    function decimalHoursToHMS(float $decHours) : string {
		$hours = floor($decHours);
		$min = ($decHours - $hours) * 60;
		$sec = ($min - floor($min)) * 60;
		return sprintf("%02d:%02d:%06.03f", $hours, $min, $sec);
    }

    $MONTHS_EN = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    $DAYS_EN = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    
    $MONTHS_FR = ["Janvier", "F&eacute;vrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Ao&ucirc;t", "Septembre", "Octobre", "Novembre", "D&eacute;cembre"];
    $DAYS_FR = ["Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"];

    function translateDate(int $year, int $month, int $day, $userLang) : string {
        $theDate = date_create(sprintf("%04d-%02d-%02d", $year, $month, $day));

        $defaultDate = date_format($theDate, "l F jS, Y");
        $translatedDate = $defaultDate;
        
        if ($userLang == 'FR') { // Translate to French
            try {
                $dow = (int)date_format($theDate, "w"); // 0..6
                $m = (int)date_format($theDate, "n"); // 1..12
                $dom = (int)date_format($theDate, "d"); // 1..31
                $y = date_format($theDate, "Y");

                if (false) {
                    echo ("DOW:[". $dow . "]<br/>");
                    echo ("MONTH:[". $m . "]<br/>");
                    echo ("DOM:[". $dom . "]<br/>");
                    echo ("YEAR:[". $y . "]<br/>");
                }

                $translatedDate = $GLOBALS['DAYS_FR'][$dow] . " " . sprintf("%d" . ($dom == 1 ? "er" : ""), $dom) . " " . $GLOBALS['MONTHS_FR'][$m - 1] . " " . $y;
            } catch (Throwable $err) {
                echo "[Captured Throwable (-) for translateDate : " . $err . "] " . PHP_EOL;
            }
        }

        return $translatedDate;
    }

    /*
     * This is a layer on top of the AstroComputer
     * 
     * We are going to produce a JSON structure (from AstroComputer), that is then going to be fetched
     * from some ES6 code, that will turn it into (printable) HTML...
     *
     * Astro Symbols: https://www.w3schools.com/charsets/ref_utf_symbols.asp

    ☉	9737	2609	SUN
    ♀	9792	2640	FEMALE SIGN (and Venus)
    ♁	9793	2641	EARTH
    ♂	9794	2642	MALE SIGN (and Mars)
    ♃	9795	2643	JUPITER
    ♄	9796	2644	SATURN

    */
    $pageBreak = "<div class='page-break content'><!--hr/--></div>" . "<br/>";

    function oneDayAlmanac(bool $verbose, DateTime $date, bool $withStars, string $userLang) : string {
        $starCatalog = null;

        try {
            // Current dateTime
            $year = (int)date("Y");
            $month = (int)date("m");
            $day = (int)date("d");
            $hours = (int)date("H");
            $minutes = (int)date("i");
            $seconds = (int)date("s");

            $nbDaysThisMonth = TimeUtil::getNbDays($year, $month);
            // echo("This month, $nbDaysThisMonth days.<br/>" . PHP_EOL);

            $htmlContentSunMoonAries = "";
            $htmlContentPlanets = "";
            $htmlContentSemiDiamAndCo = "";
            $htmlContentStarsPage = "";
    
            // $htmlContentSunMoonAries .= ("<p>Calculated at $year:$month:$day $hours:$minutes:$seconds UTC</p>" . PHP_EOL);
            // date("l jS \of F Y h:i:s A"). See https://www.w3schools.com/php/func_date_date.asp

            $year = (int)date_format($date, "Y");
            $month = (int)date_format($date, "m");
            $day = (int)date_format($date, "d");

            /*
            https://www.w3schools.com/php/func_date_strftime.asp
            setlocale(LC_TIME, "fr_FR");
            strftime(" in French %d.%M.%Y and");
            */
            if (false) {
                $theDate = date_create(sprintf("%04d-%02d-%02d", $year, $month, $day));
                $htmlContentSunMoonAries .= "<div class='sub-title'> " . date_format($theDate, "l F jS, Y") .  "</div>" . PHP_EOL;
            } else {
                $htmlContentSunMoonAries .= "<div class='sub-title'> " . translateDate($year, $month, $day, $userLang) .  "</div>" . PHP_EOL;
            }
            $htmlContentSunMoonAries .= "<table style='margin: auto;'>" . PHP_EOL;
            $htmlContentSunMoonAries .= "<tr>" . 
                               "<th rowspan='2'>" . translateText($userLang, 'ut') . "</th><th colspan='5' style='font-size: 2rem;'>" . translateText($userLang, 'sun') . " &#9737;</th>" . 
                                          "<th colspan='6' style='font-size: 2rem;'>" . translateText($userLang, 'moon') . " &#9790;</th>" . "<th style='font-size: 2rem;'>" . translateText($userLang, 'aries') . " &gamma;</th><th rowspan='2'>" . translateText($userLang, 'ut') . "</th>" . 
                          "</tr>" . PHP_EOL;
            $htmlContentSunMoonAries .= "<tr>" . 
                               "<th>" . translateText($userLang, 'AHvo') . "</th><th>&delta; " . translateText($userLang, 'AHvo') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . "</th><th>&delta; " . translateText($userLang, 'decl') . "</th>" . 
                                          "<th>" . translateText($userLang, 'AHao') . "</th><th>&delta; " . translateText($userLang, 'AHao') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . "</th><th>&delta;" . translateText($userLang, 'decl') . "</th><th>" . translateText($userLang, 'hp') . " (&pi;)</th>" . "<th>" . translateText($userLang, 'AHvo') . "</th>" . 
                          "</tr>" . PHP_EOL;

            $htmlContentPlanets .= "<table style='margin: auto;'>" . PHP_EOL;
            $htmlContentPlanets .= "<tr><th rowspan='2'>" . translateText($userLang, 'ut') . 
                                       "</th><th colspan='3' style='font-size: 2rem;'>" . translateText($userLang, 'venus') . " &#9792;</th><th colspan='3' style='font-size: 2rem;'>" .
                                        translateText($userLang, 'mars') . " &#9794;</th><th colspan='3' style='font-size: 2rem;'>" . 
                                        translateText($userLang, 'jupiter') . " &#9795;</th><th colspan='3' style='font-size: 2rem;'>" . 
                                        translateText($userLang, 'saturn') . " &#9796;</th><th rowspan='2'>" . translateText($userLang, 'ut') . "</th></tr>" . PHP_EOL;
            $htmlContentPlanets .= "<tr><th>" . translateText($userLang, 'AHao') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . 
                                       "</th><th>" . translateText($userLang, 'AHao') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . 
                                       "</th><th>" . translateText($userLang, 'AHao') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . 
                                       "</th><th>" . translateText($userLang, 'AHao') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . "</th></tr>" . PHP_EOL;
    
            // Astro Computer. Roule ma poule.
            $ac = new AstroComputer(); 

            $prevGHASun = null;
            $prevGHAMoon = null;
            $prevDeclSun = null;
            $prevDeclMoon = null;

            for ($i=0; $i<24; $i++) {
                $h = $i;

                $ac->calculate($year, $month, $day, $h, 0, 0, true, $withStars);

                $context2 = $ac->getContext();
                $deltaGHASun = "";
                if ($prevGHASun != null) {
                    $diff = $ac->getSunGHA() - $prevGHASun;
                    while ($diff < 0) {
                        $diff += 360;
                    }
                    $deltaGHASun = sprintf("%1\$.4f&deg;", $diff);
                }
                $deltaDeclSun = "";
                if ($prevDeclSun != null) {
                    $diff = abs($ac->getSunDecl() - $prevDeclSun);
                    // while ($diff < 0) {
                    //     $diff += 360;
                    // }
                    $deltaDeclSun = sprintf("%1\$.4f&apos;", ($diff * 60));
                }
                $deltaGHAMoon = "";
                if ($prevGHAMoon != null) {
                    $diff = $ac->getMoonGHA() - $prevGHAMoon;
                    while ($diff < 0) {
                        $diff += 360;
                    }
                    $deltaGHAMoon = sprintf("%1\$.4f&deg;", $diff);
                }
                $deltaDeclMoon = "";
                if ($prevDeclMoon != null) {
                    $diff = abs($ac->getMoonDecl() - $prevDeclMoon);
                    // while ($diff < 0) {
                    //     $diff += 360;
                    // }
                    $deltaDeclMoon = sprintf("%1\$.4f&apos;", ($diff * 60));
                }
                $prevGHASun = $ac->getSunGHA();
                $prevDeclSun = $ac->getSunDecl();
                $prevDeclMoon = $ac->getMoonDecl();
                $prevGHAMoon = $ac->getMoonGHA();
                $prevDeclMoon = $ac->getMoonDecl();
                $htmlContentSunMoonAries .= 
                            ("<tr>" . 
                                 "<td><b>" . sprintf("%02d", $h) . "</b></td>" .
                                 "<td>" . Utils::decToSex($ac->getSunGHA()) .  "</td>" .
                                 "<td>" . $deltaGHASun .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getSunRA(), Utils::$NONE) .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getSunDecl(), Utils::$NS) .  "</td>" .
                                 "<td>" . $deltaDeclSun .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getMoonGHA()) .  "</td>" .
                                 "<td>" . $deltaGHAMoon .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getSunRA(), Utils::$NONE) .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getMoonDecl(), Utils::$NS) .  "</td>" .
                                 "<td>" . $deltaDeclMoon .  "</td>" .
                                 "<td>" . sprintf("%.04f&apos;", ($ac->getMoonHp() / 60)) .  "</td>" .
                                 "<td>" . Utils::decToSex($ac->getAriesGHA(), Utils::$NONE) . "</td>" .
                                 "<td><b>" . sprintf("%02d", $h) .  "</b></td>" .
                             "</tr>" . PHP_EOL); 

                $venusGHA = Utils::decToSex($context2->GHAvenus);
                $venusRA = Utils::decToSex($context2->RAvenus);
                $venusDecl = Utils::decToSex($context2->DECvenus, Utils::$NS);
    
                $marsGHA = Utils::decToSex($context2->GHAmars);
                $marsRA = Utils::decToSex($context2->RAmars);
                $marsDecl = Utils::decToSex($context2->DECmars, Utils::$NS);
    
                $jupiterGHA = Utils::decToSex($context2->GHAjupiter);
                $jupiterRA = Utils::decToSex($context2->RAjupiter);
                $jupiterDecl = Utils::decToSex($context2->DECjupiter, Utils::$NS);
    
                $saturnGHA = Utils::decToSex($context2->GHAsaturn);
                $saturnRA = Utils::decToSex($context2->RAsaturn);
                $saturnDecl = Utils::decToSex($context2->DECsaturn, Utils::$NS);
    
                $htmlContentPlanets .= ("<tr><td style='font-weight: bold;'>" . sprintf("%02d", $h) .  "</td><td>$venusGHA</td><td>$venusRA</td><td>$venusDecl</td>" . 
                                                                                "<td>$marsGHA</td><td>$marsRA</td><td>$marsDecl</td>" .
                                                                                "<td>$jupiterGHA</td><td>$jupiterRA</td><td>$jupiterDecl</td>" .
                                                                                "<td>$saturnGHA</td><td>$saturnRA</td><td>$saturnDecl</td>" .
                                                "<td style='font-weight: bold;'>" . sprintf("%02d", $h) .  "</td></tr>");

                if ($h === 12) {
                    $semiDiamSun = sprintf("%.04f", ($context2->SDsun / 60));
                    $sunHP = sprintf("%.04f", ($context2->HPsun / 60)); 
                    $semiDiamMoon = sprintf("%.04f", ($context2->SDmoon / 60)); 
                    // var_dump($ac->getMoonPhase());
                    $moonPhaseAngle = $ac->getMoonPhase()->phase;                      // TODO Fix that
                    $moonPhase = sprintf("%.02f %%, ", $context2->k_moon) . $ac->getMoonPhaseStr(); // sprintf("", ) "${calcResult.moon.illum.toFixed(2)}% ${calcResult.moon.phase.phase}";
                    $phaseIndex = floor($moonPhaseAngle / (360 / 28.5)) + 1;
                    if ($phaseIndex > 28) {
                        $phaseIndex = 28;
                    }
                    $phaseImageName = sprintf("./moon/phase%02d.gif", $phaseIndex);

                    $tPassSun = decimalHoursToHMS(12 - ($context2->EoT / 60));
                    $moonAge = ($moonPhaseAngle * 28 / 360);

                    // $htmlContentSemiDiamAndCo .= "<table>";
                    $htmlContentSemiDiamAndCo .= (
                        "<tr><td colspan='14'>&nbsp;</td></tr>" .
                        "<tr>" .
                            "<td rowspan='3'></td>" .
                            "<td colspan='2'>&frac12;&nbsp;&#x2300 " . $semiDiamSun . "'</td>" . 
                            "<td colspan='3'> " . translateText($userLang, 'hp') . " (&pi;) " . $sunHP . "'</td>" . 
                            "<td colspan='3'>&frac12;&nbsp;&#x2300 " . $semiDiamMoon . "'</td>" . 
                            "<td colspan='2'>" . $moonPhase . "</td>" . 
                            "<td rowspan='3' colspan='2'><img src='" . $phaseImageName . "' alt='" . sprintf("%.02f", $moonPhaseAngle)  . "' title='" . sprintf("%.02f", $moonPhaseAngle) . "'&deg'}'>" .
                            "<td rowspan='3'></td>" . 
                        "</tr>" .
                        "<tr><td colspan='5'>" . translateText($userLang, 'eot') . " : " . $context2->EoT . " (" . translateText($userLang, 'in-minutes') . ")</td><td colspan='5'>" . translateText($userLang, 'phase-at') . " : " . sprintf("%.02f", $moonPhaseAngle) . "&deg;</td></tr>" .
                        "<tr><td colspan='5'>" . translateText($userLang, 'tpass') . " : " . $tPassSun . "</td><td colspan='5'>" . translateText($userLang, 'age') . " : " . sprintf("%.01f", $moonAge) . " " . translateText($userLang, 'day-s') . "</td></tr>" 
                    );

                    // TODO Rise and Set for Sun and Moon
                }

                if ($withStars && $h === 0) {
                    if ($starCatalog === null) {
                        $starCatalog = Star::getCatalog(); // from STAR_CATALOG...
                    }

                    if (false) {
                        $htmlContentStarsPage .= "<div class='sub-title'> " . date_format($theDate, "l F jS, Y") .  "</div>" . PHP_EOL;
                    } else {
                        $htmlContentStarsPage .= "<div class='sub-title'> " . translateDate($year, $month, $day, $userLang) .  "</div>" . PHP_EOL;
                    }
                    $htmlContentStarsPage .= "<div style='display: grid; grid-template-columns: auto auto;'>";
                    $htmlContentStarsPage .= "<div>";
                    $htmlContentStarsPage .= (translateText($userLang, 'stars-at'));
    
                    $htmlContentStarsPage .= (
                        "<br/>" .
                        "<table>" .
                        "<tr><th>" . translateText($userLang, 'name') . "</th><th>" . translateText($userLang, 'AHso') . "</th><th>" . translateText($userLang, 'decl') . "</th></tr>"
                    );
    
                    $ariesGHA = $ac->getAriesGHA();
                    $starArray = $ac->getStars();
    
                    $nbStars = count($starArray);
                    // $htmlContentStarsPage .= "<br/>Found $nbStars stars.<br/>";
    
                    for ($j=0; $j<$nbStars; $j++) {
                        $star = Star::getStar($starArray[$j]->name);
                        if ($star === null) {
                            echo("Star " . $starArray[$j][0] . " not found in catalog");
                        } else {
                            // console.log("Found ${starArray[i].name}: ${JSON.stringify(star)}");
                            $starSHA = $starArray[$j]->GHAStar - $ariesGHA;
                            while ($starSHA < 0) {
                                $starSHA += 360;
                            }
                            $starDec = $starArray[$j]->DECStar;
                            $htmlContentStarsPage .= (
                                "<tr><td" . (($starDec < 0) ? " style='background: silver;'" : "") ."><b>" . $starArray[$j]->name . "</b>, " . $star->getConstellation() . "</td>" . 
                                    "<td>" . Utils::decToSex($starSHA) . "</td>" . 
                                    "<td>" . Utils::decToSex($starDec, Utils::$NS) . "</td></tr>"
                            );
                        }
                    }
                    $htmlContentStarsPage .= "</table>";
                    $htmlContentStarsPage .= "</div>";
    
                    $htmlContentStarsPage .= "<div>";
                    $htmlContentStarsPage .= "<b>" . translateText($userLang, 'calculated-at') . "</b>";
    
                    $htmlContentStarsPage .= "<table>";
    
                    $htmlContentStarsPage .= "<tr><td>" . translateText($userLang, 'moe') . "</td><td>" . $context2->eps0 . "&deg;</td></tr>";                    
                    $htmlContentStarsPage .= "<tr><td>" . translateText($userLang, 'toe') . "</td><td>" . $context2->eps . "&deg;</td></tr>";   
                    $htmlContentStarsPage .= "<tr><td>Delta &psi;</td><td>" . round(3600000 * $context2->delta_psi) / 1000 . "&quot;</td></tr>";                    
                    $htmlContentStarsPage .= "<tr><td>Delta &epsilon;</td><td> " . round(3600000 * $context2->delta_eps) / 1000 . "&quot;</td></tr>";                    
                    $htmlContentStarsPage .= "<tr><td>" . translateText($userLang, 'jj') . "</td><td>" . round(1000000 * $context2->JD) / 1000000 . "</td></tr>";                    
                    $htmlContentStarsPage .= "<tr><td>" . translateText($userLang, 'jje') . "</td><td>" . round(1000000 * $context2->JDE) / 1000000 . "</td></tr>";                    
    
                    $htmlContentStarsPage .= "</table>";
    
                    $htmlContentStarsPage .= "</div>";
    
                    $htmlContentStarsPage .= "</div>";

                    // echo ("End of Stars<br/>");
    
                }
                // echo ("End of hour $h.<br/>". PHP_EOL);
            }
            $htmlContentSunMoonAries .= $htmlContentSemiDiamAndCo;
            $htmlContentSunMoonAries .= ("</table>" . PHP_EOL);

            $htmlContentPlanets .=      ("</table>" . PHP_EOL);

            return ($GLOBALS['pageBreak'] . $htmlContentSunMoonAries . "<br/>" . 
                    $htmlContentPlanets  . 
                    ($withStars ? ($GLOBALS['pageBreak'] . $htmlContentStarsPage) : "")
                   );
        } catch (Throwable $e) {
            if ($verbose) {
                echo "[Captured Throwable (2) for oneDayAlmanac : " . $e->getMessage() . "] " . PHP_EOL;
            }
            throw $e;
        }
        // return null;
    }

    $TRANSLATIONS = [
        array("id" => "main-title", "content" => array("EN" => "Celestial Almanac", "FR" => "&Eacute;ph&eacute;m&eacute;rides Nautiques")),
        array("id" => "for-main-title", "content" => array("EN" => "for", "FR" => "pour")),
        array("id" => "from", "content" => array("EN" => "from", "FR" => "de")),
        array("id" => "to", "content" => array("EN" => "to", "FR" => "&agrave;")),
        array("id" => "sun", "content" => array("EN" => "Sun", "FR" => "Soleil")),
        array("id" => "moon", "content" => array("EN" => "Moon", "FR" => "Lune")),
        array("id" => "aries", "content" => array("EN" => "Aries", "FR" => "Pt Vernal")),
        array("id" => "AHvo", "content" => array("EN" => "GHA", "FR" => "AHvo")),
        array("id" => "AHso", "content" => array("EN" => "SHA", "FR" => "AHso")),
        array("id" => "AHao", "content" => array("EN" => "GHA", "FR" => "AHao")),
        array("id" => "decl", "content" => array("EN" => "D", "FR" => "D")),
        array("id" => "ut", "content" => array("EN" => "UT", "FR" => "TU")),
        array("id" => "hp", "content" => array("EN" => "hp", "FR" => "ph")),
        array("id" => "venus", "content" => array("EN" => "Venus", "FR" => "V&eacute;nus")),
        array("id" => "mars", "content" => array("EN" => "Mars", "FR" => "Mars")),
        array("id" => "jupiter", "content" => array("EN" => "Jupiter", "FR" => "Jupiter")),
        array("id" => "saturn", "content" => array("EN" => "Saturn", "FR" => "Saturne")),
        array("id" => "eot", "content" => array("EN" => "EoT at 12:00 UTC", "FR" => "&Eacute;qu. du temps &agrave; 12:00 TU")),
        array("id" => "in-minutes", "content" => array("EN" => "in minutes", "FR" => "en minutes")),
        array("id" => "phase-at", "content" => array("EN" => "Phase at 12:00 UTC", "FR" => "Phase &agrave; 12:00 TU")),
        array("id" => "tpass", "content" => array("EN" => "Meridian Pass. Time", "FR" => "Temps pass. au m&eacute;ridien")),
        array("id" => "age", "content" => array("EN" => "Age", "FR" => "Age")),
        array("id" => "day-s", "content" => array("EN" => "day(s)", "FR" => "jour(s)")),
        array("id" => "calculated-at", "content" => array("EN" => "Calculated at 00:00:00 U.T.", "FR" => "Calcul&eacute; &agrave; 00:00:00 TU.")),
        array("id" => "stars-at", "content" => array("EN" => "Stars at 0000 U.T. (GHA(star) = SHA(star) + GHA(Aries))", "FR" => "&Eacute;toiles &agrave; 0000 TU. (AHao(&eacute;toile) = AHso(&eacute;toile) + AHvo(Pt Vernal))")),
        array("id" => "name", "content" => array("EN" => "Name", "FR" => "Nom")),
        array("id" => "moe", "content" => array("EN" => "Mean Obliquity of Ecliptic", "FR" => "Obliquit&eacute; moyenne de l'&eacute;cliptique")),
        array("id" => "toe", "content" => array("EN" => "True Obliquity of Ecliptic", "FR" => "Obliquit&eacute; vraie de l'&eacute;cliptique")),
        array("id" => "jj", "content" => array("EN" => "Julian Date", "FR" => "Jour Julien")),
        array("id" => "jje", "content" => array("EN" => "Julian Ephemeris Date", "FR" => "Jour Julien des &Eacute;ph&eacute;merides"))
    ];

    function translateText(string $userLang, string $itemId) : string {
        $translation = "Not Found/Introuvable";
        for ($i=0; $i<count($GLOBALS['TRANSLATIONS']); $i++) {
            $oneItem = $GLOBALS['TRANSLATIONS'][$i];
            if ($oneItem['id'] == $itemId) {
                $translation = $oneItem['content'][$userLang];
                break;
            }
        }
        return $translation;
    }

    function plublishAlmanac(bool $verbose, string $from, string $to, bool $withStars, string $userLang) : string {
        $finalOutput = "";

        $startDate = date_create($from); // Format "2024-11-25"
        $endDate = date_create($to);

        $year = (int)date_format($startDate, "Y");
        $month = (int)date_format($startDate, "m");
        $day = (int)date_format($startDate, "d");
        /*
        https://www.w3schools.com/php/func_date_strftime.asp
        setlocale(LC_TIME, "fr_FR");
        strftime(" in French %d.%M.%Y and");
        */
        $fromDate = date_create(sprintf("%04d-%02d-%02d", $year, $month, $day));
        $fromDateFmt = translateDate($year, $month, $day, $userLang);

        // Front page
        $firstPage = "<div class='content print-only front-page'>";
        $firstPage .=    "<h3 style='text-align: center;'>Passe-Coque</h3>";
        $firstPage .=    "<div class='in-the-middle'>";
        if ($from === $to) {
            $firstPage .= "<h1 style='text-align: center; font-size: 4.5em;'>" . translateText($userLang, 'main-title') . 
                             "<br/>" . translateText($userLang, 'for-main-title') . " " . /*date_format($fromDate, "l F jS, Y")*/ $fromDateFmt . "</h1>";
        } else {
            $year = (int)date_format($endDate, "Y");
            $month = (int)date_format($endDate, "m");
            $day = (int)date_format($endDate, "d");
            /*
            https://www.w3schools.com/php/func_date_strftime.asp
            setlocale(LC_TIME, "fr_FR");
            strftime(" in French %d.%M.%Y and");
            */
            if (false) {
                $toDate = date_create(sprintf("%04d-%02d-%02d", $year, $month, $day)); // main-title
                $firstPage .= "<h1 style='text-align: center; font-size: 4.5em; color: black;'>" . translateText($userLang, 'main-title') . " <br/>" . translateText($userLang, 'from') . " " . date_format($fromDate, "l F jS, Y") . "<br/>" . translateText($userLang, 'to') . " " . date_format($toDate, "l F jS, Y") . "</h1>";
            } else {
                $toDateFmt = translateDate($year, $month, $day, $userLang);
                $firstPage .= "<h1 style='text-align: center; font-size: 4.5em; color: black;'>" . translateText($userLang, 'main-title') . " <br/>" . translateText($userLang, 'from') . " " . $fromDateFmt . "<br/>" . translateText($userLang, 'to') . " " . $toDateFmt . "</h1>";
            }
        }
        $firstPage .= "<div style='text-align: center;'>";
        $firstPage .=   "<img src='sextant.gif' style='margin-top: 30px;'>";
        $firstPage .= "</div>";
        $firstPage .= "</div>";
        $firstPage .= "<div style='width: 100%; font-style: italic; font-weight: bold; text-align: center;' class='bottom-of-page'>Passe-Coque never stops</div>";
        $firstPage .= "</div>";

        $finalOutput .= ($firstPage /* . $GLOBALS['pageBreak'] */);

        // $startDate = date_create($from); // Format "2024-11-25"
        // $endDate = date_create($to);
        $currentDate = $startDate;
        while ($currentDate <= $endDate) {
            // $finalOutput .= "Calculating Almanac for " . date_format($currentDate,"Y/m/d") . "<br/>";
            $finalOutput .= oneDayAlmanac($verbose, $currentDate, $withStars, $userLang) . "<br/>";
            // See this: https://www.w3schools.com/php/func_date_date_add.asp
            // date_add($date, date_interval_create_from_date_string("1 day"));
            date_add($currentDate, date_interval_create_from_date_string("1 day"));
        }
        return $finalOutput;
    }

    $option = "none";
    $fromDate = "-";
    $toDate = "-";
    $withStars = "false";
    $userLang = "EN";

    // Whatever you want it to be 
    if (isset($_GET['option'])) {
        $option = $_GET['option'];
    }
    if (isset($_GET['from'])) {
        $fromDate = $_GET['from'];
    }
    if (isset($_GET['to'])) {
        $toDate = $_GET['to'];
    }
    if (isset($_GET['stars'])) {
        $withStars = $_GET['stars'];
    }
    if (isset($_GET['lang'])) {
        $userLang = $_GET['lang'];
    }

    if ($option == "1") {
        try {
            $data = plublishAlmanac($VERBOSE, $fromDate, $toDate, $withStars == 'true', $userLang); // oneDayAlmanac($VERBOSE);
            header('Content-Type: text/html; charset=utf-8');
            echo $data;
            // http_response_code(200);
        } catch (Throwable $e) {
            echo "[Captured Throwable (5) for celestial.computer.php : " . $e . "] " . PHP_EOL;
        }
    } else { 
        echo "Option is [$option], not supported.<br/>";
    }

} catch (Throwable $plaf) {
    echo "[Captured Throwable (big) for celestial.computer.php : " . $plaf . "] " . PHP_EOL;
}
?>
