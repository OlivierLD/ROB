#!/bin/bash
#
# try CLI prms: --help, -h, ?
#
# Colors available from https://gist.github.com/vratiu/9780109
RED='\033[0;31;1m'                 # Red and Bold
GREEN="\033[1;32m"                 # Green and bold
YELLOW="\033[1;33m"                # Yellow and bold
BLUE="\033[1;34m"                  # Blue and bold
CYAN="\033[1;36m"                  # Cyan and bold
BOLD_GREEN_BLINK='\033[0;32;1;5m'  # Green, bold, blink.
NC='\033[0m'                       # Back to No Color
#
display_help ( ) {
  echo -e "--------- H E L P -----------------------------------------------------"
  echo -e "Managed CLI parameters for ${0} are:"
  echo -e "${CYAN}--help, -h, ?${NC}"
  echo -e "  display this message, and exit"
  echo -e "${CYAN}EINK2-13${NC} (default)"
  echo -e "  starts the python server for an eInk2.13 screen, and a Multiplexer"
  echo -e "${CYAN}BME280-SSD${NC}"
  echo -e "  starts 2 python servers:"
  echo -e "  - one for a BME280 sensor"
  echo -e "  - one for an SSD1306 screen"
  echo -e "  and a Multiplexer"
  echo -e "${CYAN}SSD1306${NC}"
  echo -e "  starts the python server for an ssd1306 screen, and a Multiplexer"
  echo -e "${BOLD_GREEN_BLINK}Unmanaged CLI parameters will result in a exit.${NC}"
  echo -e "-----------------------------------------------------------------------"
}

display_usage ( ) {
  SERVER_IP=$(hostname -I | awk '{ print $1 }')
  echo -e "${CYAN}If mux is started, try curl to check the REST available MUX operations: curl -X GET on http://${SERVER_IP}:${SERVER_HTTP_PORT}/mux/oplist${NC}"
  echo -e "${CYAN}                            to check the REST available SSD1306 operations: curl -X GET on http://${SERVER_IP}:8080/ssd1306/oplist${NC}"
  echo -e "${CYAN}                            to check the REST available BME280 operations: curl -X GET on http://${SERVER_IP}:9876/bme280/oplist${NC}"
  echo -e "${CYAN}                   Web server available, see http://${SERVER_IP}:${SERVER_HTTP_PORT}/web/index.html or http://${SERVER_IP}:${SERVER_HTTP_PORT}/zip/index.html${NC}"
  echo -e "${YELLOW}Try curl -X GET on http://${SERVER_IP}:${SERVER_HTTP_PORT}/mux/cache | jq '.\"Solar time\"'${NC}"
  echo -e "${YELLOW}Try curl -X GET on http://${SERVER_IP}:${SERVER_HTTP_PORT}/mux/cache | jq '.NMEA_AS_IS'${NC}"
  echo -e "Use ./show.processes.sh to see what's running."
  echo -e "Script ${0} completed."
}
#
echo -e "${RED}Warning !! Make sure this is matching the one in rc.local !!${NC}"
echo -e "           Assuming we're working from ~pi/nmea-dist"
#
# Can be EINK2-13, SSD1306, or BME280-SSD (BME280 + SSD1306)
OPTION=EINK2-13        # Default value
if [[ $# -gt 0 ]]; then
  # echo -e "CLI prm: $1"
  if [[ "$1" == "--help" ]] || [[ "$1" == "-h" ]] || [[ "$1" == "?" ]]; then
    display_help
    exit 0
  else
    OPTION=$1
  fi
fi
#
MAP_SERIAL_PORT=false  # File or sym-link
# Required if serial port is read as a file
if [[ "${MAP_SERIAL_PORT}" == "true" ]]; then
  echo -e "Mapping /dev/ttyACM0 to a file."
  stty -F /dev/ttyACM0 raw 4800 cs8 clocal
else
  if [[ ! -L /dev/ttyS80 ]]; then
    echo -e "Linking /dev/ttyACM0 to /dev/ttyS80"
    sudo ln -s /dev/ttyACM0 /dev/ttyS80
  else
    echo -e "SymLink exists"
  fi
fi
#
# Start MUX and Co on startup
#
echo -e ">>> Using option ${OPTION}"
# MACHINE_NAME=$(hostname -I | awk '{ print $1 }')  # IP address
MACHINE_NAME=localhost      # For the kill to work, from the MUX. prop rest.onclose.resource !!!!!
echo -e "Step 1 - Starting the Python server(s), on ${MACHINE_NAME}"
#
if [[ "${OPTION}" == "BME280-SSD" ]]; then
  # Start the REST Server for BME280
  echo -e "1 - Starting the BME280 REST server, port 9876, log is bme280.python.log"
  ~pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:${MACHINE_NAME} --port:9876 --verbose:false > ~pi/nmea-dist/bme280.python.log 2>&1
  sleep 10
  # Start the REST Server for SSD1306 (v2)
  echo -e "2 - Starting the SSD1306 (v2) REST server, port 8080, log is ssd1306.python.log"
  ~pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:${MACHINE_NAME} --port:8080 --verbose:false --verbose-2:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM,ATP,PRM,HUM,NET,COG_G --screen-saver:on --rotate:false > ~pi/nmea-dist/ssd1306.python.log 2>&1
  sleep 10
elif [[ "${OPTION}" == "EINK2-13" ]]; then
  # Start the REST Server for EINK2-13
  echo -e "1 - Starting the EINK2-13 REST server, port 8080, log is eink2-13.python.log"
  ~pi/nmea-dist/python/scripts/start.EINK2-13.REST.server.sh --interactive:false  --machine-name:${MACHINE_NAME} --port:8080 --verbose:false --data:NAV,POS,SOG,COG --screen-saver:on > ~pi/nmea-dist/eink2-13.python.log 2>&1
  sleep 10
elif [[ "${OPTION}" == "SSD1306" ]]; then
  # Start the REST Server for SSD1306 (v2)
  echo -e "1 - Starting the SSD1306 (v2) REST server, port 8080, log is ssd1306.python.log"
  ~pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:${MACHINE_NAME} --port:8080 --verbose:false --verbose-2:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,NET,COG_G --screen-saver:on --rotate:true > ~pi/nmea-dist/ssd1306.python.log 2>&1
  sleep 10
else
  echo -e ">> Unmanaged OPTION ${OPTION}"
  display_help
  echo -e "Aborting"
  exit 1
fi
# Start the MUX
echo -e "Step 2 - Starting the MUX"
cd ~pi/nmea-dist
rm nohup.out 2> /dev/null
if [[ "${OPTION}" == "BME280-SSD" ]]; then
  # PROP_FILE=nmea.mux.gps.sensor.2.nmea-fwd.yaml
  PROP_FILE=nmea.mux.gps.sensor.nmea-fwd.yaml
  SERVER_HTTP_PORT=$(cat ${PROP_FILE} | grep http.port | head -1 | awk '{ print $2 }')
  echo -e "Starting Mux, ${SERVER_HTTP_PORT}"
  nohup ./mux.sh ${PROP_FILE} &
elif [[ "${OPTION}" == "EINK2-13" ]]; then
  PROP_FILE=nmea.mux.gps.nmea-fwd.yaml
  SERVER_HTTP_PORT=$(cat ${PROP_FILE} | grep http.port | head -1 | awk '{ print $2 }')
  echo -e "Starting Mux, ${SERVER_HTTP_PORT}"
  nohup ./mux.sh ${PROP_FILE} &
elif [[ "${OPTION}" == "SSD1306" ]]; then
  PROP_FILE=nmea.mux.gps.nmea-fwd-ssd.yaml
  SERVER_HTTP_PORT=$(cat ${PROP_FILE} | grep http.port | head -1 | awk '{ print $2 }')
  echo -e "Starting Mux, port ${SERVER_HTTP_PORT}"
  nohup ./mux.sh ${PROP_FILE} &
else
  # This should have been taken of previously
  echo -e ">> Unmanaged OPTION ${OPTION}"
  display_help
  echo -e "Aborting"
  exit 1
fi
#
display_usage
#