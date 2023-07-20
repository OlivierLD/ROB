#!/bin/bash
stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the REST Server for BME280
echo -e "Starting the BME280 REST server"
/home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:localhost --port:9876 --verbose:false > /home/pi/nmea-dist/bme280.python.log 2>&1
sleep 10
# Start the REST Server for SSD1306 (v2)
echo -e "Starting the SSD1306 (v2) REST server"
/home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM --screen-saver:on > /home/pi/nmea-dist/ssd1306.python.log 2>&1
sleep 10
# Start the MUX
echo -e "Starting the MUX"
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.sensor.2.nmea-fwd.yaml &
echo -e "Script ${0} completed."
