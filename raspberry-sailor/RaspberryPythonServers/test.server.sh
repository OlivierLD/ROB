#!/bin/bash
echo -e "TODO: Start ZDA TCP server, CacheForwarder (or so) REST server"
echo -e "Run a multiplexer, that stops the 2 python servers before shutting down."
#
# Start the Python servers
#
echo -e "From $(pwd):"
pushd python
#
echo -e "From $(pwd):"
#
python3 REST_BasicCacheForwarder_server.py --machine-name:localhost --port:8080 --verbose:false &
#
sleep 2
#
python3 TCP_ZDA_server.py --machine-name:localhost --port:7001 --verbose:false &
#
sleep 2
#
popd
#
echo -e "From $(pwd):"
CONFIG_FILE=~/repos/ROB/raspberry-sailor/RaspberryPythonServers/nmea.mux.log.sensor.actuator.yaml
pushd ../NMEA-multiplexer
echo -e "From $(pwd):"
./mux.sh ${CONFIG_FILE}
popd
echo -e "Back in $(pwd):"

