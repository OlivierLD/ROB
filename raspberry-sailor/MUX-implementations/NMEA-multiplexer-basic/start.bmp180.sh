#!/bin/bash
#
# If Serial port mapping is needed:
# stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the TCP Server for BMP180 (uses I2C)
IP_ADDR=$(hostname -I)
echo -e "Starting the BMP180 TCP server (on ${IP_ADDR})"
VERBOSE=false
#
/home/pi/nmea-dist/python/scripts/start.BMP180.TCP.server.sh --interactive:false  --machine-name:${IP_ADDR} --port:8001 --verbose:${VERBOSE} > /home/pi/nmea-dist/bmp180.python.log 2>&1
sleep 10
# Start the MUX
echo -e "Starting the MUX"
cd /home/pi/nmea-dist
rm nohup.out 2> /dev/null
nohup ./mux.sh nmea.mux.bmp180.yaml &
echo -e "Script ${0} completed."
