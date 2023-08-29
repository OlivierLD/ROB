#!/bin/bash
#
# stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the TCP Server for BMP180
echo -e "Starting the BMP180 TCP server"
/home/pi/nmea-dist/python/scripts/start.BMP180.TCP.server.sh --interactive:false  --machine-name:localhost --port:8001 --verbose:false > /home/pi/nmea-dist/bmp180.python.log 2>&1
sleep 10
# Start the MUX
echo -e "Starting the MUX"
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.bmp180.yaml &
echo -e "Script ${0} completed."
