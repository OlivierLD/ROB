<?php

include __DIR__ . '/autoload.php';


$VERBOSE = false;

/*
 * This is a layer on top of the AstroComputer
 * 
 * We are going to produce a JSON structure, that is then going to be fetched
 * from some ES6 code...
 */


function doYourJob(bool $verbose): string {

    $ac = new AstroComputer(); // See below

    try {
        $json_result = "[";


        $json_result .= "{ \"text\": \"Function doYourJob\"}, ";

        // Quick test
        try {

            // ContextV2::$EPS0_2000
            $json_result .= ("{ \"EPS_2000\" : " . ContextV2::$EPS0_2000 . "}, ");

            $data = 60.0;
            $json_result .= ("{ \"cosd\" : " . Utils::cosd($data) . "}, ");

            // Core test
            $context = new ContextV2();

            $year = (int)date("Y");
            $month = (int)date("m");
            $day = (int)date("d");
            $hours = (int)date("H");
            $minutes = (int)date("i");
            $seconds = (int)date("s");

            // $deltaT = TimeUtil::getDeltaT($year, $month);
            // $json_result .= ("{ \"deltaT\" : " . $deltaT . "}, ");

            $json_result .= ("{ \"calc-for\" : \"$year:$month:$day $hours:$minutes:$seconds UTC\" }, ");

            // $ac->setDateTime($year, $month, $day, $hours, $minutes, $seconds);
            $ac->calculate($year, $month, $day, $hours, $minutes, $seconds, true);
            $json_result .= ("{ \"deltaT\" : " . $ac->getDeltaT() . "}, ");
            $context2 = $ac->getContext();
            // echo ("From calculate: EoT:" . $context2->EoT . " ");
            $json_result .= ("{ \"from-calc-eot\" : " . $context2->EoT . " }, ");
        
            if (false) {
                Core::julianDate($context, $year, $month, $day, $hours, $minutes, $seconds, $deltaT);

                $json_result .= ("{ \"TE\": " . $context->TE . " }, ");
                $json_result .= ("{ \"Tau\": " . $context->Tau . " }, "); 
                $json_result .= ("{ \"Tau2\": " . $context->Tau2 . " }, ");
                $json_result .= ("{ \"Tau3\": " . $context->Tau3 . " }, ");
                $json_result .= ("{ \"Tau4\": " . $context->Tau4 . " }, ");
                $json_result .= ("{ \"Tau5\": " . $context->Tau5 . " }, ");

                Anomalies::nutation($context);
                Anomalies::aberration($context);

                Core::aries($context);
                Core::sun($context);
        
                // Moon.compute(this.context);
        
                // Venus.compute(this.context);
                // Mars.compute(this.context);
                // Jupiter.compute(this.context);
                // Saturn.compute(this.context);
                // Core.polaris(this.context);
                // this.moonPhase = Core.moonPhase(this.context);
                // this.dow = WEEK_DAYS[Core.weekDay(this.context)];
        
                // this.calculateHasBeenInvoked = true;
        

                $json_result .= ("{ \"Sun EoT\": " . $context->EoT . " }, ");
            }
        } catch (Throwable $e) {
            if ($VERBOSE) {
                echo "[ Captured Throwable for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
            }
            throw $e;
        }
    
        // Final one
        $json_result .= "{ \"akeu\": \"coucou\" }";

        $json_result .= "]";
        return $json_result;

    } catch (Throwable $e) {
        echo "[ Captured Throwable for doYourJob : " . $e->getMessage() . "] " . PHP_EOL;
        throw $e;
    }
    return null;
}

try {
    $data = doYourJob($VERBOSE);
    header('Content-Type: application/json; charset=utf-8');
    // echo json_encode($data); // This is for text (not json)
    echo $data;
    http_response_code(200);
} catch (Throwable $e) {
    echo "[Captured Throwable for celestial.computer.php : " . $e . "] " . PHP_EOL;
}
?>
