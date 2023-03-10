#!/bin/bash
#
# Start MUX and Co on startup
# Suitable for the mux with nmea.mux.gps.sensor.nmea-fwd.yaml, see that one.
#
# Link the Serial Port
# sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# If started from rc.local, make sure the python modules are installed with sudo.
#
# Start the REST Server for BME280
~pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:$(hostname -I) --port:9876 --verbose:false > bme280.python.log 2>&1
sleep 10
# Start the REST Server for SSD1306 (v2)
~pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:$(hostname -I) --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM > ssd1306.python.log 2>&1
sleep 10
# Start the MUX
pushd ~pi/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
./mux.sh nmea.mux.gps.sensor.nmea-fwd.yaml &
popd

