#!/bin/bash
#
# To be run from the module's root (NMEA-multiplexer), like ./scripts/start.SSD1306.REST.server.sh
#
# Move 1 level above the 'script' directory
pushd $(dirname $0)/..
echo -e "Working from $PWD"

PYTHON_SCRIPT_NAME=src/main/python/REST_SSD1306_server.py
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
SCREEN_HEIGHT=32
WIRING="I2C"
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
echo -en "Screen Height (32 or 64) ? - Default [${SCREEN_HEIGHT}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    SCREEN_HEIGHT=${USER_INPUT}
fi
# echo "Will use screen height ${SCREEN_HEIGHT}"
echo -en "Wiring Option (I2C or SPI) ? - Default [${WIRING}] > "
read USER_INPUT
if [[ "${USER_INPUT}" != "" ]]; then
    WIRING=${USER_INPUT}
fi
# echo "Will use screen wiring option ${WIRING}"
COMMAND="python3 ${PYTHON_SCRIPT_NAME} --machine-name:${MACHINE_NAME} --port:${PORT} --verbose:${VERBOSE} --height:${SCREEN_HEIGHT} --wiring:${WIRING}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.rest.sh to stop the server."
echo -e "- Try curl -X PUT http://${MACHINE_NAME}:${PORT}/ssd1306/nmea-data -d 'This is|a test.'"
echo -e "- Try curl -X GET http://${MACHINE_NAME}:${PORT}/ssd1306/oplist"

popd
