#!/bin/bash
#
# Run ./scripts/start.BME280.REST.server.sh
# ./scripts/start.BME280.REST.server.sh --interactive:true|false  (if false, must be the first parameter)
#
INTERACTIVE=true
if [[ $# -gt 0 ]]; then
  if [[ "$1" == "--interactive:false" ]]; then
    INTERACTIVE=false
  fi
fi
# Move 1 level above the 'script' directory
pushd $(dirname $0)/..
echo -e "Working from $PWD"

PYTHON_SCRIPT_NAME=./REST_BME280_server.py
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
# Prompted, or get prms from CLI
#
if [[ "${INTERACTIVE}" == "true" ]]; then
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
else
  echo -e "Getting prms from CLI"
  if [[ $# -gt 0 ]]; then
  	for prm in $*; do
  	  echo "Processing ${prm} ..."
  	  if [[ ${prm} == "--machine-name:"* ]]; then
  	    MACHINE_NAME=${prm#*:}
  	  elif [[ ${prm} == "--port:"* ]]; then
  	    PORT=${prm#*:}
  	  elif [[ ${prm} == "--verbose:"* ]]; then
  	    VERBOSE=${prm#*:}
  	  elif [[ ${prm} == "--simulate-when-missing:"* ]]; then
  	    SIMULATE_IF_MISSING=${prm#*:}
  	  fi
  	done
  fi
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
