<?php
try {
    $db = new SQLite3('../sql/tides.db');

    $results = $db->query('SELECT COUNT(*) FROM STATIONS');
    while ($row = $results->fetchArray()) {
        var_dump($row);
        echo ("We have " . $row[0] . " entries in Stations.<br/>" . PHP_EOL);
    }
    echo "Done.<br/>" . PHP_EOL;

    $results = $db->query('SELECT S.NAME, S.LATITUDE, S.LONGITUDE FROM STATIONS S LIMIT 300');
    echo ("Tide Stations" . PHP_EOL);
    echo ("<table>" . PHP_EOL);
    echo ("<tr><th>Name</th><th>Latitude</th><th>Longitude</th></tr>" . PHP_EOL);
    while ($row = $results->fetchArray()) {
        echo ("<tr><td>" . $row[0] . "</td><td>" . $row[1] . "</td><td>" . $row[2] . "</td></tr>" . PHP_EOL);
    }
    echo ("</table>");
    echo "Done.<br/>" . PHP_EOL;

} catch (Throwable $bam) {
    echo "[Captured Throwable (big) for db.connect.php : " . $bam . "] " . PHP_EOL;
}
?>
