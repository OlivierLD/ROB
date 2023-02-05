#!/bin/bash
#
# See CLI prm --interactive
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

PYTHON_SCRIPT_NAME=./TCP_Serial_server.py
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
SERIAL_PORT=/dev/ttyACM0
BAUD_RATE=4800
#
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
  echo -en "Enter TCP Port - Default [${PORT}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      PORT=${USER_INPUT}
  fi
  # echo "Will use port ${PORT}"
  echo -en "Enter Serial Port Name - Default [${SERIAL_PORT}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      SERIAL_PORT=${USER_INPUT}
  fi
  # echo "Will use serial port ${SERIAL_PORT}"
  echo -en "Enter Baud Rate - Default [${BAUD_RATE}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      BAUD_RATE=${USER_INPUT}
  fi
  # echo "Will use baud rate ${BAUD_RATE}"
  echo -en "Verbose (true or false) ? - Default [${VERBOSE}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      VERBOSE=${USER_INPUT}
  fi
  # echo "Will use verbose ${VERBOSE}"
else
  echo -e "Getting prms from CLI"
  if [[ $# -gt 0 ]]; then
  	for prm in $*; do
  	  echo "Processing ${prm} ..."
  	  if [[ ${prm} == "--machine-name:"* ]]; then
  	    MACHINE_NAME=${prm#*:}
  	  elif [[ ${prm} == "--port:"* ]]; then
  	    PORT=${prm#*:}
  	  elif [[ ${prm} == "--serial-port:"* ]]; then
  	    SERIAL_PORT=${prm#*:}
  	  elif [[ ${prm} == "--baud-rate:"* ]]; then
  	    BAUD_RATE=${prm#*:}
  	  elif [[ ${prm} == "--verbose:"* ]]; then
  	    VERBOSE=${prm#*:}
  	  fi
  	done
  fi
fi
COMMAND="python3 ${PYTHON_SCRIPT_NAME} --machine-name:${MACHINE_NAME} --port:${PORT} --serial-port:${SERIAL_PORT} --baud-rate:${BAUD_RATE} --verbose:${VERBOSE}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.tcp.sh to stop the server."

popd
