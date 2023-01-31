#!/bin/bash
#
# To be run from the module's root (NMEA-multiplexer), like ./scripts/start.BME280.REST.server.sh
#
MACHINE_NAME=$(hostname)  # $(hostname -I) for IP address
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
# echo "Will use verbose ${SIMULATE_IF_MISSING}"
COMMAND="python3 src/main/python/REST_BME280_server.py --machine-name:${MACHINE_NAME} --port:${PORT} --verbose:${VERBOSE} --simulate-when-missing:${SIMULATE_IF_MISSING}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.rest.sh to stop the server."
