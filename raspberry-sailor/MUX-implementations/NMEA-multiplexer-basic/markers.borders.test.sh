#!/bin/bash
#
# Markers and Borders test
#
echo -e "When server is started, see http://localhost:8080/web/chartless.gps.ais.html"
echo -e "See 'between-records: 100' in nmea.mux.replay.etel.groix.yaml, in the file channel."
echo -e "In chartless.gps.ais.html, see initAjax(false, ping=250);"
./mux.sh nmea.mux.replay.etel.groix.yaml
