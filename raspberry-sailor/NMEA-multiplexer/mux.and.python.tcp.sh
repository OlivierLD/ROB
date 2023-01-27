#!/bin/bash
#
# Start a TCP Python server for a BME280,
# and a MUX consuming it !!
#
echo -e "Starting the PYTHON TCP server for BME280, in background (use kill.python.tcp.sh to stop it)"
python3 ./src/main/python/TCP_BME280_server.py --port:9876 --host:localhost --verbose:true &
#
echo -e "Now staring the MUX consuming the TCP Python Server data (Ctrl-C to stop)"
./mux.sh mux-configs/nmea.mux.big.log.plus.bme280.yaml

