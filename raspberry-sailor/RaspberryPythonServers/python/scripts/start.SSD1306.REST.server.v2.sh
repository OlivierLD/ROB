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

PYTHON_SCRIPT_NAME=./REST_SSD1306_server_v2.py
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
DATA="BSP,SOG,COG,POS"
SS_MODE="on"
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
  echo -en "Data to display (CSV list) ? - Default [${DATA}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      DATA=${USER_INPUT}
  fi
  # echo "Will use data option ${DATA}"
  echo -en "Screen Saver Mode ? - Default [${SS_MODE}] > "
  read USER_INPUT
  if [[ "${USER_INPUT}" != "" ]]; then
      SS_MODE=${USER_INPUT}
  fi
  # echo "Will use screen saver mode option ${SS_MODE}"
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
  	  elif [[ ${prm} == "--height:"* ]]; then
  	    SCREEN_HEIGHT=${prm#*:}
  	  elif [[ ${prm} == "--wiring:"* ]]; then
  	    WIRING=${prm#*:}
  	  elif [[ ${prm} == "--data:"* ]]; then
  	    DATA=${prm#*:}
  	  elif [[ ${prm} == "--screen-saver:"* ]]; then
  	    SS_MODE=${prm#*:}
  	  fi
  	done
  fi
fi
COMMAND="python3 ${PYTHON_SCRIPT_NAME} --machine-name:${MACHINE_NAME} --port:${PORT} --verbose:${VERBOSE} --height:${SCREEN_HEIGHT} --wiring:${WIRING} --data:${DATA} --screen-saver:${SS_MODE}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"
echo -e "Use ./scripts/kill.python.rest.sh to stop the server."
echo -e "- Try curl -X GET http://${MACHINE_NAME}:${PORT}/ssd1306/oplist"

popd
