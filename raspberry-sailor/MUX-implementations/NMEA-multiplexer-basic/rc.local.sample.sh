#!/bin/bash
#
# Start MUX and Co on startup
#
# Link the Serial Port
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# Start the REST Server for BME280
~/repos/ROB/raspberry-sailor/NMEA-multiplexer/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:$(hostname -I) --port:9876 --verbose:false &
# Start the REST Server for SSD1306 (v2)
~/repos/ROB/raspberry-sailor/NMEA-multiplexer/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:$(hostname -I) --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG &
# Start the MUX
pushd ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
./mux.sh nmea.mux.gps.sensor.nmea-fwd.yaml &
popd
