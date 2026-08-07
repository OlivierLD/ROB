#!/bin/bash
#
# Start the REST_SSD1306_server_v2.py. SSD1306, and 2 push buttons.
# See CLI prm --interactive. SPI or I2C, and other parameters.
#
INTERACTIVE=false
if [[ $# -gt 0 ]]; then
  if [[ "$1" == "--interactive:false" ]]; then
    INTERACTIVE=false
  elif [[ "$1" == "--help" ]]; then
    HELP=true
  fi
fi
# Move 1 level above the 'script' directory
pushd $(dirname $0)/.. > /dev/null
echo -e "Working from $PWD"

PYTHON_SCRIPT_NAME=./Clear_SSD1306.py
if [[ "$HELP" == "true" ]]; then
  COMMAND="python3 -u ${PYTHON_SCRIPT_NAME} --help "
  echo -e "Running ${COMMAND}"
  ${COMMAND}
  exit 0
fi

VERBOSE=false
SCREEN_HEIGHT=64   # 32 or 64
WIRING="SPI"       # I2C or SPI
#
# Prompted, or get prms from CLI
#
if [[ "${INTERACTIVE}" == "true" ]]; then
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
else
  echo -e "Getting prms from CLI"
  if [[ $# -gt 0 ]]; then
  	for prm in $*; do
  	  echo "Processing ${prm} ..."
  	  if [[ ${prm} == "--verbose:"* ]]; then
  	    VERBOSE=${prm#*:}
  	  elif [[ ${prm} == "--height:"* ]]; then
  	    SCREEN_HEIGHT=${prm#*:}
  	  elif [[ ${prm} == "--wiring:"* ]]; then
  	    WIRING=${prm#*:}
  	  fi
  	done
  fi
fi
COMMAND="python3 -u ${PYTHON_SCRIPT_NAME} --verbose:${VERBOSE} --height:${SCREEN_HEIGHT} --wiring:${WIRING}"
echo -e "Running ${COMMAND}"
${COMMAND} &
echo -e "Done"

popd > /dev/null
