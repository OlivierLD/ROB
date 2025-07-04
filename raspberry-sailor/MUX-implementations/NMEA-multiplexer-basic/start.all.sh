#!/bin/bash
#
echo -e "Warning !! Make sure this is matching the one in rc.local !!"
#
stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Can be EINK2-13 or BME280-SSD
OPTION=EINK2-13
#
echo -e "Using option ${OPTION}"
#
if [[ "${OPTION}" == "BME280-SSD" ]]; then
  # Start the REST Server for BME280
  echo -e "Starting the BME280 REST server"
  /home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:localhost --port:9876 --verbose:false > /home/pi/nmea-dist/bme280.python.log 2>&1
  sleep 10
  # Start the REST Server for SSD1306 (v2)
  echo -e "Starting the SSD1306 (v2) REST server"
  /home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM --screen-saver:on > /home/pi/nmea-dist/ssd1306.python.log 2>&1
  sleep 10
elif [[ "${OPTION}" == "EINK2-13" ]]; then
  # Start the REST Server for EINK2-13
  echo -e "Starting the EINK2-13 REST server"
  /home/pi/nmea-dist/python/scripts/start.EINK2-13.REST.server.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --data:NAV,POS,SOG,COG --screen-saver:on > /home/pi/nmea-dist/eink2-13.python.log 2>&1
  sleep 10
else
  echo -e "Unmanaged OPTION ${OPTION}"
fi
# Start the MUX
echo -e "Starting the MUX"
cd /home/pi/nmea-dist
if [[ "${OPTION}" == "BME280-SSD" ]]; then
  nohup ./mux.sh nmea.mux.gps.sensor.2.nmea-fwd.yaml &
elif [[ "${OPTION}" == "EINK2-13" ]]; then
  nohup ./mux.sh nmea.mux.gps.nmea-fwd.yaml &
else
  echo -e "Unmanaged OPTION ${OPTION}"
fi
echo -e "Script ${0} completed."
