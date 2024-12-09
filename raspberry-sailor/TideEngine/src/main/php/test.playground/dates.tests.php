<?php

$FRENCH_MONTHS = array("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre");
$FRENCH_DOW = array("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

function frenchDate(DateTime $date) : string {
    global $FRENCH_MONTHS, $FRENCH_DOW;

    $dow =   (int)$date->format("N");
    $month = (int)$date->format("m");
    $year =  (int)$date->format("Y");
    $day =   (int)$date->format("d");

    $frenchDate = $FRENCH_DOW[$dow - 1] . " " . $day . " " . $FRENCH_MONTHS[$month - 1] . " " . $year;
    return $frenchDate;
}

$year = 2024;
$month = 12;
$day = 9;
$theDate = DateTime::createFromFormat("Y-m-d", sprintf("%04d-%02d-%02d", $year, $month, $day));

echo "Default: " . $theDate->format("l, F d Y") . "<br/>" . PHP_EOL;
echo "In French: " . frenchDate($theDate) . "<br/>" . PHP_EOL;

/*
echo "Now: " . date('N m d, Y').'<br>';        
echo "Week Day [1..7], [Mon..Sun] : " . date('N') .'<br/>';        
echo "Month [1..12] : " . date('m') .'<br/>';        
echo "Day of Month [1..31] : " . date('d') .'<br/>';        
echo "Year : " . date('Y') .'<br/>';      
*/

?>
