#!/bin/bash
#
echo -e "Executing ${0}..."
# For tests
echo -e "In the script ${0}, for tests..."
exit
#
# Start MUX and Co on startup
# Suitable for the mux with nmea.mux.gps.nmea-fwd.yaml, see that one.
#
# Link the Serial Port
# sudo ln -s /dev/ttyACM0 /dev/ttyS80
# Look at mk.link.sh
#
# If started from rc.local, make sure the python modules are installed with sudo.
#
#
# "Link" the Serial Port
stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the REST Server for EINK2-13
/home/pi/nmea-dist/python/scripts/start.EINK2-13.REST.server.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --data:NAV,POS,SOG,COG --screen-saver:on --screen-saver-option:sleep > /home/pi/nmea-dist/eink2-13.python.log 2>&1
sleep 10
# Start the MUX
cd /home/pi/nmea-dist
# nohup ./mux.sh nmea.mux.gps.sensor.nmea-fwd.yaml &
nohup ./mux.sh nmea.mux.gps.nmea-fwd.yaml &
#
