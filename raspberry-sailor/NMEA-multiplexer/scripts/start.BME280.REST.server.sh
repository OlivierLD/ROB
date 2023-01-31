#!/bin/bash
#
# To be run from the module's root (NMEA-multiplexer), like ./scripts/start.BME280.REST.server.sh
#
# Move 1 level above the 'script' directory
pushd $(dirname $0)/..
echo -e "Working from $PWD"

PYTHON_SCRIPT_NAME=src/main/python/REST_BME280_server.py
MACHINE_NAME=localhost
if MACHINE_NAME=$(hostname -I) ; then
    echo -e "It worked: ${MACHINE_NAME}"
else
    MACHINE_NAME=$(hostname)
    echo -e "Plan B: ${MACHINE_NAME}"
fi
MACHINE_NAME=$(echo ${MACHINE_NAME})  # Trim the blanks
PORT=9999
VERBOSE=false
SIMULATE_IF_MISSING=false
#
echo -en "Enter Machine Name - Default [${MACHINE_NAME}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    MACHINE_NAME=${USER_INPUT}
fi
# echo -e "Will use ${MACHINE_NAME}"
echo -en "Enter HTTP Port - Default [${PORT}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    PORT=${USER_INPUT}
fi
# echo "Will use port ${PORT}"
echo -en "Verbose (true or false) ? - Default [${VERBOSE}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    VERBOSE=${USER_INPUT}
fi
# echo "Will use verbose ${VERBOSE}"
echo -en "Simulate if missing (true or false) ? - Default [${SIMULATE_IF_MISSING}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    SIMULATE_IF_MISSING=${USER_INPUT}
fi
# echo "Will simulate if missing: ${SIMULATE_IF_MISSING}"
COMMAND="python3 ${PYTHON_SCRIPT_NAME} --machine-name:${MACHINE_NAME} --port:${PORT} --verbose:${VERBOSE} --simulate-when-missing:${SIMULATE_IF_MISSING}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.rest.sh to stop the server."
echo -e "- Try curl -X GET http://${MACHINE_NAME}:${PORT}/bme280/oplist"
echo -e "- Try curl -X GET http://${MACHINE_NAME}:${PORT}/bme280/data"
echo -e "- Try curl -X GET http://${MACHINE_NAME}:${PORT}/bme280/nmea-data"

popd
