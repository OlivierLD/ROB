#!/bin/bash
#
# If Serial port mapping is needed:
# stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the TCP Server for BME280 (uses I2C, see address below)
IP_ADDR=$(hostname -I | awk '{ print $1 }')
echo -e "Starting the BME280 TCP server (on ${IP_ADDR})"
VERBOSE=false
#
/home/pi/nmea-dist/python/scripts/start.BME280.TCP.server.sh --interactive:false \
                                                             --machine-name:${IP_ADDR} \
                                                             --port:8001 \
                                                             --verbose:${VERBOSE} \
                                                             --address:0x76 > /home/pi/nmea-dist/bme280.python.log 2>&1
sleep 10
# Start the MUX
echo -e "Starting the MUX"
cd /home/pi/nmea-dist
rm nohup.out 2> /dev/null
nohup ./mux.sh nmea.mux.bmp180.yaml &
echo -e "Script ${0} completed."
