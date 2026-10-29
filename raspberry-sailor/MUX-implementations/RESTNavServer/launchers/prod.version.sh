#!/bin/bash
# Close to demoLauncher.sh, but with no choice from a menu.
# Uses runNavServer.sh
# Hard-coded PROP_FILE=mux-configs/nmea.mux.no.gps.yaml in the script.
# 
# For parameters --no-rmc-time --no-date : see in runNavServer.sh
#
# Escape codes (for colors): https://en.wikipedia.org/wiki/ANSI_escape_code
#    or https://chrisyeh96.github.io/2020/03/28/terminal-colors.html
#
export SCRIPT_DIR=$(dirname ${0})
echo -e "Moving to ${SCRIPT_DIR}"
cd ${SCRIPT_DIR}
echo -e "Working from $(pwd -P)"
#
RED='\033[0;31;1m'                 # Red and Bold
BOLD_GREEN_BLINK='\033[0;32;1;5m'  # Green, bold, blink.
NC='\033[0m'                       # Back to No Color
#
HTTP_PORT=9999
#
INTERACTIVE=Y
LAUNCH_BROWSER=N
LNCH_BRWSR_PROVIDED=N
WITH_PROXY=N
USER_OPTION=
WITH_NOHUP=
SMALL_MENU=N
export MUX_VERBOSE=false
export CMD_VERBOSE=N
# Program parameters
NAV_SERVER_EXTRA_OPTIONS=
#
function displayUsage() {
  echo -e "-- Several CLI Parameters --"
  echo -e " ${RED}--help, -h, help, ?${NC}: produces this message."
  echo -e " ${RED}--small, ${NC}: reduced menu."
  echo -e " ${RED}--http-port:XXXX${NC}, default ${HTTP_PORT}, where XXXX overrides the default port, ${HTTP_PORT}, hard-coded in ${0}."
  echo -e " ${RED}--option:XX${NC}, automatically launches option XX, without prompting the user"
  echo -e " ${RED}--browser:Y|N${NC}, default N, will open a browser (URL depends on the option)"
  echo -e " ${RED}--nohup:Y|N${NC}, no default, will use nohup or not (when/if option uses it)"
  echo -e " ${RED}--proxy:Y|N${NC}, default N, will use a proxy or not (proxy is defined in runNavServer.sh)"
  echo -e " ${RED}--cmd-verbose:Y|N${NC}, default N, will display the command sent to runNavServer.sh"
  echo -e "----------------------------"
  echo -e "Also check the '${RED}H:XX${NC}' options in the menu, giving details on the config."
  echo -e "----------------------------"
}
#
if [[ $# -gt 0 ]]; then
	for prm in $*; do
	  echo "Processing ${prm} ..."
	  if [[ ${prm} == "--small" ]]; then
	    SMALL_MENU=Y
	  elif [[ ${prm} == "--browser:"* ]]; then
	    LAUNCH_BROWSER=${prm#*:}
	    LNCH_BRWSR_PROVIDED=Y
	  elif [[ ${prm} == "--http-port:"* ]]; then
	    HTTP_PORT=${prm#*:}
	  elif [[ ${prm} == "--nohup:"* ]]; then
	    WITH_NOHUP=${prm#*:}
	  elif [[ ${prm} == "--cmd-verbose:"* ]]; then
	    export CMD_VERBOSE=${prm#*:}
	  elif [[ ${prm} == "--proxy:"* ]]; then
	    WITH_PROXY=${prm#*:}
	    if [[ "${WITH_PROXY}" == "Y" ]] || [[ "${WITH_PROXY}" == "y" ]]; then
	      NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --proxy"
	    fi
	  elif [[ ${prm} == "--help" ]] || [[ ${prm} == "-h" ]] || [[ ${prm} == "help" ]] || [[ ${prm} == "?" ]]; then
	    displayUsage
	    echo -e "Hit [return] to move on"
	    read dummy
	  elif [[ ${prm} == "--option:"* ]]; then
	    USER_OPTION=${prm#*:}
	    INTERACTIVE=N
	  else
	    echo "Unsupported parameter ${prm}"
	  fi
	done
fi
#
URL_OPTION_1_01="http://localhost:${HTTP_PORT}/web/index.html"
#
function openBrowser() {
  if [[ $(uname -s) == *Linux* ]]; then
    sensible-browser "$1"
  else
    open "$1"  # Darwin
  fi
}
#
function displayHelp() {
  echo -e "--------------------------------"
  echo -e "PropFile $1, will open $2 (on demand)"
  echo -e "--------------------------------"
  cat $1
  echo -e "" # CR-NL
  echo -e "--------------------------------"
}
#
echo -e "+------------------------------------------+"
echo -e "| Starting NMEA Multiplexer, full version. |"
echo -e "+------------------------------------------+"
# cat banner.stby.txt
cat banner.txt
echo -e " "
sleep 2
#
NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --http-port:${HTTP_PORT}"
#
# clear
PROP_FILE=mux-configs/nmea.mux.no.gps.yaml
# PROP_FILE=mux-configs/nmea.mux.udp.input.yaml
displayHelp ${PROP_FILE} ${URL_OPTION_1_01}
echo -en "Hit [Return]"
read a
#
NOHUP=""
if [[ "${WITH_NOHUP}" == "Y" ]] || [[ "${WITH_NOHUP}" == "N" ]]; then
  if [[ "${WITH_NOHUP}" == "Y" ]]; then
    NOHUP="nohup "
    echo -e ">> Will use nohup"
    rm nohup.out
  else
    NOHUP=""
    echo -e ">> Will not use nohup"
  fi
else
  # Ask if nohup, just in this case
  echo -en " ==> Use nohup (y|n) ? > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    NOHUP="nohup "
    echo -e ">> Will use nohup"
    rm nohup.out
  fi
fi
# Ask to launch a browser in interactive mode (and not provided already)
if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
  echo -en "Launch a browser ? y|[n] > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    LAUNCH_BROWSER=Y
    echo -e ">> Will launch a browser, for option [${option}]"
  fi
fi
if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
  URL_01=${URL_OPTION_1_01}
fi
echo -e "Launching Nav Server with ${PROP_FILE}:"
echo -e "-----------------------------"
cat ${PROP_FILE}
echo -e ""
echo -e "-----------------------------"
# QUESTION: a 'screen' option ?
# screen -S navserver -dm "sleep 5; ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS}"
# echo -e "A screen session 'navserver' was started"
#
# bash -c "exec -a ProcessName Command"
if [[ "${CMD_VERBOSE}" == "Y" ]]; then
  echo -e "- CMD_VERBOSE=Y"
  NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --http-verbose:true"
  echo -e "Running command: [${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &]"
fi
${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
  echo -e ">>> Waiting for the server to start..."
  sleep 5  # Wait (5s) for the server to be operational
  openBrowser ${URL_01} &
else
  echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
fi
echo -e "Also try: curl -X GET http://localhost:${HTTP_PORT}/mux/cache | jq"
#######
echo -e "Done launching. See you ✋"
#