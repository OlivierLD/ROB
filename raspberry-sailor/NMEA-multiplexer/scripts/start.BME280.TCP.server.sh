#!/bin/bash
#
# To be run from the module's root (NMEA-multiplexer), like ./scripts/start.BME280.REST.server.sh
#
PYTHON_SCRIPT_NAME=src/main/python/TCP_BME280_server.py
MACHINE_NAME=localhost
if MACHINE_NAME=$(hostname -I) ; then
    echo -e "It worked: ${MACHINE_NAME}"
else
    MACHINE_NAME=$(hostname)
    echo -e "Plan B: ${MACHINE_NAME}"
fi
PORT=9999
VERBOSE=false
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
COMMAND="python3 ${PYTHON_SCRIPT_NAME} --machine-name:${MACHINE_NAME} --port:${PORT} --verbose:${VERBOSE}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.tcp.sh to stop the server."
