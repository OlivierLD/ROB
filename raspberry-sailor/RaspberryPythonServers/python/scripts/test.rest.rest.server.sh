#!/bin/bash
echo -e "+--------------------------------------------------------------------------+"
echo -e "| Python Sensors and Actuators.                                            |"
echo -e "| Start ZDA REST server, CacheForwarder (or so) REST server                |"
echo -e "| Run a multiplexer, that stops the 2 python servers before shutting down. |"
echo -e "+--------------------------------------------------------------------------+"
#
# Move 1 level above the 'script' directory
pushd $(dirname $0)/..
#
# Start the Python servers
#
echo -e "Python part..."
echo -e "Script > From $(pwd), starting Python scripts."
# pushd python
#
# echo -e "Script > From $(pwd):"
#
# Actuator
python3 REST_BasicCacheForwarder_server.py --machine-name:localhost --port:8080 --verbose:false &
# sleep 2
#
# Sensor
python3 REST_ZDA_server.py --machine-name:localhost --port:9876 --verbose:false &
# sleep 2
#
# popd
#
echo -e "Now starting Multiplexer"
CONFIG_FILE=~/repos/ROB/raspberry-sailor/RaspberryPythonServers/nmea.mux.log.rest-sensor.rest-actuator.yaml
pushd ../../NMEA-multiplexer
echo -e "Script > From $(pwd), starting MUX:"
./mux.sh ${CONFIG_FILE}
popd
popd
echo -e "Script > Back in $(pwd):"
echo -e "Bye"
