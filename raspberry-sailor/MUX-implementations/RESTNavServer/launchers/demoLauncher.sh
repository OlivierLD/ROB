#!/bin/bash
# Interactive script to launch the RESTNavServer in several configurations.
# For demo and examples purpose.
# Describes different scenarios.
# Uses runNavServer.sh
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
RED='\033[0;31;1m'    # Red and Bold
BOLD_GREEN_BLINK='\033[0;32;1;5m'  # Green, bold, blink.
NC='\033[0m'          # Back to No Color
#
HTTP_PORT=9999
#
INTERACTIVE=Y
LAUNCH_BROWSER=N
LNCH_BRWSR_PROVIDED=N
WITH_PROXY=N
USER_OPTION=
WITH_NOHUP=
export CMD_VERBOSE=N
# Program parameters
NAV_SERVER_EXTRA_OPTIONS=
#
function displayUsage() {
  echo -e "-- Several CLI Parameters --"
  echo -e " ${RED}--help, -h, help, ?${NC}: produces this message."
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
	  if [[ ${prm} == "--browser:"* ]]; then
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
URL_OPTION_1_00="http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
URL_OPTION_1_01="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_1a="http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
URL_OPTION_2="http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
URL_OPTION_4="http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y"
URL_OPTION_5="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_6="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_6b="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_7="http://localhost:${HTTP_PORT}/web/leaflet.driving.html"
URL_OPTION_8="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_9="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_9b="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_9c="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_9d="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_9e="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_10="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_11="http://localhost:${HTTP_PORT}/web/index.html"
URL_OPTION_12="http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
URL_OPTION_13="http://localhost:${HTTP_PORT}/web/ais/ais.102.html"
URL_OPTION_13b="http://localhost:${HTTP_PORT}/web/ais/ais.102.html"
URL_OPTION_13c="http://localhost:${HTTP_PORT}/web/chartless.gps.html"
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
  echo -e "Option $1, property file is $2"
  cat $2
  echo -e "--------------------------------"
  if [[ "$3" != "" ]]; then
    echo -e "Would open a browser with URL ${3}"
    echo -e "--------------------------------"
  fi
}
#
GO=true
#
# Banner done with https://manytools.org/hacker-tools/ascii-banner/, 'Slant Relief'
#
cat banner.txt
sleep 1
#
NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --http-port:${HTTP_PORT}"
#
while [[ "${GO}" == "true" ]]; do
	clear
	echo -e ">> Note ⚠️ : Optional Script Parameters : "
	echo -e "    starting the server, like ${0} --browser:[N]|Y --proxy:[N]|Y --option:1 --nohup:[N]|Y --http-port:9999 --cmd-verbose:[N]|Y --help -h help ?"
	echo -e "    --option:X will not prompt the user for his choice, it will go directly for it."
	echo -e "    --nohup:Y will launch some commands with nohup (see the script for details)"
	echo -e "+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+"
	echo -e "|               N A V   S E R V E R   -   D E M O   L A U N C H E R  🚀                                                                                                             |"
	echo -e "+-----------------------------------------------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}P${NC}. Launch proxy CLI, to visualize HTTP & REST traffic 🔎                               |                                                                                         |"
	echo -e "| ${RED}PG${NC}. Launch proxy GUI, to visualize HTTP & REST traffic 🕵️‍                                |                                                                                         |"
	echo -e "+------------------------------------+----------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}J${NC}. JConsole (JVM Monitoring) 📡   |  ${RED}JV${NC}. JVisualVM 📡                                  |                                                                                         |"
	echo -e "|                                    | - Note: for remote monitoring, jstatd must be      |                                                                                         |"
	echo -e "|                                    |         running on the remote machine.             |                                                                                         |"
	echo -e "|                                    |     Enter 'JVH' for some help.                     |                                                                                         |"
	echo -e "+------------------------------------+----------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}0${NC}. Pure ES6 Celestial Context. External (and demanding on the browser). Requires Internet Connection.                                                                            |"
	echo -e "+------------------------------------+----------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}1${NC}. Time simulated by a ZDA generator; HTTP Server, rich Web UI. Does not require a GPS |  ${RED}1a${NC}. Time from a TCP ZDA generator (port 7002), TCP Server, rich Web UI.                |"
	echo -e "|                                                                                         |             Does not require a GPS                                                      |"
	echo -e "|  ${RED}2${NC}. Interactive Time (user-set), HTTP Server, rich Web UI. Does not require a GPS       |  ${RED}3${NC}. Home Weather Station data                                                           |"
	echo -e "|  ${RED}4${NC}. With GPS and NMEA data, waits for the RMC sentence to be active to begin logging    |  ${RED}5${NC}. Like option '1', but with 'Sun Flower' option                                       |"
	echo -e "|                     (Check your GPS connection setting in nmea.mux.gps.properties file) |                                                                                         |"
	echo -e "|  ${RED}6${NC}. Replay logged kayak data (Drakes Estero)                                            |  ${RED}6b${NC}. Replay logged kayak data (Ria d'Etel. GPS - Satellites - PRMSL, Air Temp, Hum)     |"
	echo -e "|  ${RED}7${NC}. Replay logged driving data (with a Maps)                                            |  ${RED}8${NC}.  Replay logged kayak data, ANSI console display                                     |"
	echo -e "|  ${RED}9${NC}. Replay logged sailing data (Bora-Bora - Tongareva), TCP & GPSd forwarders (Big file)|  ${RED}9b${NC}. Replay logged sailing data (China Camp - Oyster Point),                            |"
	echo -e "|                                                                                         |             (there is some current in that one, it's in the SF Bay)                     |"
	echo -e "|  ${RED}9c${NC}. Replay logged sailing data (Nuku-Hiva - Rangiroa), ANSI console display (Big file) |  ${RED}9d${NC}. Replay logged sailing data (Oyster Point), heading back in.                        |"
	echo -e "|                                                                                         |             (requires a NodeJS WebSocket server to be running)                          |"
	echo -e "|  ${RED}9e${NC}. Replay logged sailing data (Bora-Bora - Tongareva), forwarders TCP, WS, GPSd       |                                                                                         |"
	echo -e "| ${RED}10${NC}. Full Nav Server Home Page. NMEA, Tides, Weather Wizard, Almanacs, etc. Data replay. | ${RED}11${NC}. Same as 10, with proxy.                                                             |"
	echo -e "|     - See or modify nmea.mux.properties for details.                                    |     - See or modify nmea.mux.properties for details.                                    |"
	echo -e "| ${RED}12${NC}. With 2 input serial ports.                                                          | ${RED}13${NC}. AIS Tests. With markers.                                                            |"
	echo -e "|     - See or modify nmea.mux.2.serial.yaml for details. Or try option H:12              | ${RED}13b${NC}. GPS, + AIS data from sinagot.net (demanding...).                                   |"
	echo -e "|                                                                                         | ${RED}13c${NC}. GPS, Chartless Map.                                                                |"
	echo -e "+-----------------------------------------------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "| ${RED}20${NC}.  Get Data Cache (curl)                                                              | ${RED}20b${NC}. Get REST operations list (curl)                                                    |"
	echo -e "+-----------------------------------------------------------------------------------------+-----------------------------------------------------------------------------------------+"
	# echo -e "| ${RED}21${NC}. Sample Python TCP Client                                                            |                                                                                         |"
	# echo -e "+-----------------------------------------------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}S${NC}. Show NavServer process(es) ⚙️                                                        | ${RED}SP${NC}. Show proxy process(es) ⚙️                                                            |"
	echo -e "|  ${RED}K${NC}. Kill all running Multiplexers                                                       |                                                                                         |"
	echo -e "+-----------------------------------------------------------------------------------------+-----------------------------------------------------------------------------------------+"
	echo -e "| >> Hint: use './killns.sh' to stop any running NavServer 💣                                                                                                                       |"
	echo -e "| >> Hint: use './killproxy.sh' to stop any running Proxy Server 💣                                                                                                                 |"
	echo -e "+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+"
	echo -e "|  >> ${BOLD_GREEN_BLINK}To get help on option X${NC}, type ${RED}H:X${NC} (like H:11, H:20b, etc)                                                                                                                     |"
	echo -e "+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+"
	echo -e "|  ${RED}Q${NC}. Quit ❎                                                                                                                                                                       |"
	echo -e "+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+"
	if [[ "${USER_OPTION}" != "" ]]; then
	  echo -e "------------------------------"
	  echo -e ">> Using option ${USER_OPTION}"
	  echo -e "------------------------------"
	  option=${USER_OPTION}
	  USER_OPTION=
	else
  	echo -en " ==> You choose: "
	  read option
	fi
	case "${option}" in
	  "PG" | "pg")
	    export HTTP_PROXY_PORT=9876
	    java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} utils.proxyguisample.ProxyGUI &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: ${HTTP_PROXY_PORT}"
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "P" | "p")
	    export HTTP_PROXY_PORT=9876
			JAVA_OPTIONS=
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=true"
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose.dump=true"
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.client.verbose=true"
			#
			# JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.util.logging.config.file=logging.properties"
			#
			java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} ${JAVA_OPTIONS} http.HTTPServer &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: ${HTTP_PROXY_PORT}"
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "J" | "j")
	    sudo jconsole &
	    ;;
	  "JV" | "jv")
	    jvisualvm &
	    ;;
	  "JVH" | "jvh")
	    echo "More here soon..."
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  H:*)   # Help on options below
	    # echo "Start with H: ${option}"
	    HELP_ON=${option#*:}
	    echo -e "Required help on option ${HELP_ON}"
	    case "${HELP_ON}" in
	      "0")
	        echo -e "All on Internet, nothing local."
	        ;;
	      "1")
	        PROP_FILE=mux-configs/nmea.mux.no.gps.yaml
	        displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_1_00}
	        ;;
	      "1a")
	        PROP_FILE=mux-configs/nmea.mux.tcp.zda.yaml
	        displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_1a}
	        ;;
	      "2")
	        PROP_FILE=mux-configs/nmea.mux.interactive.time.properties
	        displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_2}
	        ;;
	      "3")
	        PROP_FILE=mux-configs/nmea.mux.home.properties
	        displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_3}
	        ;;
	      "4")
	        PROP_FILE=mux-configs/nmea.mux.gps.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_4}
	        ;;
	      "5")
	        PROP_FILE=mux-configs/nmea.mux.no.gps.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_5}
	        ;;
	      "6")
	        PROP_FILE=mux-configs/nmea.mux.kayak.log.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_6}
	        ;;
	      "6b")
	        PROP_FILE=mux-configs/nmea.mux.kayak.etel.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_6b}
	        ;;
	      "7")
	        PROP_FILE=mux-configs/nmea.mux.driving.log.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_7}
	        ;;
	      "8")
	        PROP_FILE=mux-configs/nmea.mux.kayak.cc.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_8}
	        ;;
	      "9")
	        # PROP_FILE=mux-configs/nmea.mux.bora.cc.yaml
	        PROP_FILE=mux-configs/nmea.mux.bora.tgrva.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_9}
	        ;;
	      "9b")
	        PROP_FILE=mux-configs/nmea.mux.cc.op.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_9b}
	        ;;
	      "9c")
	        PROP_FILE=mux-configs/nmea.mux.nh.r.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_9c}
	        ;;
	      "9d")
	        PROP_FILE=mux-configs/nmea.mux.heading.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_9d}
	        ;;
	      "9e")
	        PROP_FILE=mux-configs/nmea.mux.bora.fwd.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_9e}
	        ;;
	      "10")
	        PROP_FILE=mux-configs/nmea.mux.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_10}
	        ;;
	      "11")
	        PROP_FILE=mux-configs/nmea.mux.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_11}
	        ;;
	      "12")
	        PROP_FILE=mux-configs/nmea.mux.2.serial.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_12}
	        ;;
	      "13")
	        PROP_FILE=mux-configs/nmea.mux.gps.ais.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_13}
	        ;;
	      "13b")
	        PROP_FILE=mux-configs/nmea.mux.gps.sinagot.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_13b}
	        ;;
	      "13c")
	        PROP_FILE=mux-configs/nmea.mux.replay.etel.groix.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE} ${URL_OPTION_13c}
	        ;;
	      "20")
	      	echo -e "Uses a 'curl' to display the current data cache, using REST"
	      	COMMAND="curl -X GET localhost:${HTTP_PORT}/mux/cache"
	      	echo -e "Command is "
	      	echo -e "\t${COMMAND}"
	        ;;
	      "20b")
	      	echo -e "Uses a 'curl' to display the REST operations list, using REST"
	        COMMAND="curl -X GET http://localhost:9999/oplist"
	      	echo -e "Command is "
	      	echo -e "\t${COMMAND}"
	        ;;
	      "21")
	        echo -e "--------------------------------"
	        echo -e "Requires a MUX to be running, "
	        echo -e "Starts a Python sample client."
	        echo -e "--------------------------------"
	        ;;
	      *)
	        echo -e "No help implemented (yet) for option ${HELP_ON}"
	        ;;
	    esac
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "0")
	    openBrowser "https://olivierld.github.io/web.stuff/astro/index_02.html"
	    # GO=false
	    ;;
	  "1")
      PROP_FILE=mux-configs/nmea.mux.no.gps.yaml
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
      if [[ "${option}" == "1" ]]; then
        if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
          # 2 Choices for that one
          URL_01=${URL_OPTION_1_00}
          echo -en "Open Console [1] (default) or Menu [2] > "
          read REPLY
          if [[ "${REPLY}" == "2" ]]; then
            URL_01=${URL_OPTION_1_01}
          fi
        fi
      fi
      echo -e "Launching Nav Server with ${PROP_FILE}"
      # QUESTION: a 'screen' option ?
      # screen -S navserver -dm "sleep 5; ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS}"
      # echo -e "A screen session 'navserver' was started"
      #
      # bash -c "exec -a ProcessName Command"
      if [[ "${CMD_VERBOSE}" == "Y" ]]; then
        echo -e "Running command: [${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &]"
      fi
      ${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
      if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
        echo -e ">>> Waiting for the server to start..."
        sleep 5  # Wait (5s) for the server to be operational
        openBrowser ${URL_01}
      else
        echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
      fi
      echo -e "Also try: curl -X GET http://localhost:${HTTP_PORT}/mux/cache | jq"
      GO=false
      ;;
	  "1a")
  	  PROP_FILE=mux-configs/nmea.mux.tcp.zda.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    # QUESTION: a 'screen' option ?
	    # screen -S navserver -dm "sleep 5; ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS}"
	    # echo -e "A screen session 'navserver' was started"
	    #
	    # bash -c "exec -a ProcessName Command"
	    if [[ "${CMD_VERBOSE}" == "Y" ]]; then
	      echo -e "Running command: [./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &]"
	    fi
	    # TODO ? Make sure the TCP server is started...
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5  # Wait (5s) for the server to be operational
		    openBrowser ${URL_OPTION_1a}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    echo -e "Also try: curl -X GET http://localhost:${HTTP_PORT}/mux/cache | jq"
	    GO=false
	    ;;
	  "2")
	    PROP_FILE=mux-configs/nmea.mux.interactive.time.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    echo -e "Use the 'Set Time' button in the Web UI"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_2}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "3")
	    PROP_FILE=mux-configs/nmea.mux.home.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	#   sleep 5 # Wait for the server to be operational
	#   openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    GO=false
	    ;;
	  "4")
	    PROP_FILE=mux-configs/nmea.mux.gps.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5   # Wait for the server to be operational
		    openBrowser ${URL_OPTION_4}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "5")
	    PROP_FILE=mux-configs/nmea.mux.no.gps.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date --sun-flower ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
		    openBrowser ${URL_OPTION_5}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "6")
	    PROP_FILE=mux-configs/nmea.mux.kayak.log.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_6}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "6b")
	    PROP_FILE=mux-configs/nmea.mux.kayak.etel.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_6b}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "7")
	    PROP_FILE=mux-configs/nmea.mux.driving.log.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
          LAUNCH_BROWSER=Y
          echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:${HTTP_PORT}/web/googlemaps.driving.html"
		    openBrowser ${URL_OPTION_7}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "8")
	    PROP_FILE=mux-configs/nmea.mux.kayak.cc.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_8}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "9")
	    # PROP_FILE=mux-configs/nmea.mux.bora.cc.yaml
	    PROP_FILE=mux-configs/nmea.mux.bora.tgrva.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_9}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "9b")
	    PROP_FILE=mux-configs/nmea.mux.cc.op.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_9b}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "9c")
	    PROP_FILE=mux-configs/nmea.mux.nh.r.yaml
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_9c}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "9d")
	    PROP_FILE=mux-configs/nmea.mux.heading.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_9d}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "9e")
	    PROP_FILE=mux-configs/nmea.mux.bora.fwd.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_9e}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "10")
	    PROP_FILE=mux-configs/nmea.mux.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    # NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO:2010-11"
	    NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_10}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "11")
	    PROP_FILE=mux-configs/nmea.mux.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --proxy --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_11}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "12")
  	  	PROP_FILE=mux-configs/nmea.mux.2.serial.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_12}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "13")
      # PROP_FILE=mux-configs/nmea.mux.ais.test.yaml
      # PROP_FILE=mux-configs/nmea.mux.ais.test.2.yaml
      PROP_FILE=mux-configs/nmea.mux.ais.test.3.yaml
      #	PROP_FILE=mux-configs/nmea.mux.gps.ais.yaml
        echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_13}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "13b")
  	  PROP_FILE=mux-configs/nmea.mux.gps.sinagot.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_13b}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "13c")
  	  PROP_FILE=mux-configs/nmea.mux.replay.etel.groix.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
      # Ask to launch a browser in interactive mode (and not provided already)
      # echo -e ">> Options: INTERACTIVE=[${INTERACTIVE}], LAUNCH_BROWSER=[${LAUNCH_BROWSER}], LNCH_BRWSR_PROVIDED=[${LNCH_BRWSR_PROVIDED}]"
      if [[ "${INTERACTIVE}" == "Y" ]] && [[ "${LAUNCH_BROWSER}" == "N" ]] && [[ "${LNCH_BRWSR_PROVIDED}" == "N" ]]; then
        echo -en "Launch a browser ? y|[n] > "
        read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
        LAUNCH_BROWSER=Y
        echo -e ">> Will launch a browser"
        fi
      fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]] || [[ "${LAUNCH_BROWSER}" == "y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser ${URL_OPTION_13c}
		  else
	    	echo -e "${RED}In a browser: http://localhost:${HTTP_PORT}/web/index.html${NC}"
	    fi
	    GO=false
	    ;;
	  "20")
	    COMMAND="curl -X GET localhost:${HTTP_PORT}/mux/cache"
	    if [[ "$(which jq)" != "" ]]; then
	      ${COMMAND} | jq
	    else
	      ${COMMAND}
	    fi
      echo -e "\nHit [Return]"
      read resp
	    ;;
	  "20b")
	    COMMAND="curl -X GET http://localhost:9999/oplist"
	    if [[ "$(which jq)" != "" ]]; then
	      ${COMMAND} | jq
	    else
	      ${COMMAND}
	    fi
		echo -e "\nHit [Return]"
		read resp
	    ;;
	  "21")
	    echo -e "This requires a Multiplexer to be running, and forwarding data on a TCP Port"
	    echo -en " ==> Enter Multiplexer machine name or IP (default 'localhost'): "
      read MACHINE_NAME
      if [[ "${MACHINE_NAME}" == "" ]]; then
        MACHINE_NAME=localhost
        echo -e "Defaulting machine name to ${MACHINE_NAME}"
      fi
      echo -en " ==> Enter Multiplexer TCP port (default 7001): "
      read TCP_PORT
      if [[ "${TCP_PORT}" == "" ]]; then
        TCP_PORT=7001
        echo -e "Defaulting TCP port to ${TCP_PORT}"
      fi
      echo -en " ==> With verbose option (default false): "
      read VERBOSE
      if [[ "${VERBOSE}" == "" ]]; then
        VERBOSE=false
        echo -e "Defaulting verbose to ${VERBOSE}"
      fi
      if [[ ${VERBOSE} =~ ^(yes|y|Y)$ ]]; then
        VERBOSE=true
      fi
      #
      pushd other-clients/python
        COMMAND="python3 tcp_mux_client.py --machine-name:${MACHINE_NAME} --port:${TCP_PORT} --verbose:${VERBOSE}"
        ${COMMAND}
        popd
      echo -e "\nHit [Return]"
      read resp
	    ;;
	# Others...
	    # ;;
	  "S" | "s")
	    echo -e "Nav Server processes:"
	    ps -ef | grep navrest.NavServer | grep -v grep
	    ps -ef | grep navrest.NavServer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
      NB_L=$(cat km | wc -l)
      if [[ ${NB_L} == 0 ]]; then
        echo -e "No NavServer process found."
      else
        echo -e "----------- NavServer HTTP Ports ---------"
        if [[ $(uname -a) == *Linux* ]]; then
          # Could use sudo below
          netstat -tunap | grep ${HTTP_PORT}
        else
          for pid in $(cat km); do
            netstat -vanp tcp | grep ${pid} | grep LISTEN
          done
        fi
        echo -e "------------------------------------------"
        rm km
      fi
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "SP" | "sp")
	    echo -e "Proxy processes:"
	    ps -ef | grep ProxyGUI | grep -v grep
	    ps -ef | grep HTTPServer | grep -v grep
	    #
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "K" | "k")
	    ./killns.sh
	    #
	    sleep 5  # Wait for the kill to be completed.
	    #
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "Q" | "q")
	    GO=false
	    ;;
	  *)
	    echo -e "What? Unknown option [${option}]"
	    echo -en "Hit [return]"
	    read ret
	    ;;
	esac
done
#######
echo -e "Bye now. See you ✋"
#
