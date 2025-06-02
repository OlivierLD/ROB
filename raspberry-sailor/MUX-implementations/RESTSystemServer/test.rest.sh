#!/bin/bash
#
# Test several REST requests
HTTP_PORT=1234
# HOST=localhost
HOST=192.168.50.10
#
echo -e "OpList"
curl -X GET http://${HOST}:${HTTP_PORT}/oplist | jq
#
echo -e "Generic Get"
curl -X GET http://${HOST}:${HTTP_PORT}/system/generic-get | jq
echo -e "Networks"
curl -X GET http://${HOST}:${HTTP_PORT}/system/networks | jq
echo -e "IP Addresses"
curl -X GET http://${HOST}:${HTTP_PORT}/system/ip-address # | jq
echo -e ""
echo -e "CPU Load"
curl -X GET http://${HOST}:${HTTP_PORT}/system/cpu-load # | jq
echo -e ""
echo -e "CPU Temperature"
curl -X GET http://${HOST}:${HTTP_PORT}/system/cpu-temperature # | jq
echo -e ""
echo -e "Disk Usage"
curl -X GET http://${HOST}:${HTTP_PORT}/system/disk-usage
echo -e ""
echo -e "Memory Usage"
curl -X GET http://${HOST}:${HTTP_PORT}/system/memory-usage # | jq
echo -e ""
echo -e "System Data"
curl -X GET http://${HOST}:${HTTP_PORT}/system/system-data | jq
echo -e "Addresses"
curl -X GET http://${HOST}:${HTTP_PORT}/system/addresses | jq
echo -e "System Date"
curl -X GET http://${HOST}:${HTTP_PORT}/system/system-date | jq
# curl -X POST http://${HOST}:${HTTP_PORT}/system/system-date
echo -e "MUX Status"
curl -X GET http://${HOST}:${HTTP_PORT}/system/mux-stat
echo -e ""
# curl -X POST http://${HOST}:${HTTP_PORT}/system/start-mux
# curl -X POST http://${HOST}:${HTTP_PORT}/system/stop-mux
#
# Date like "19 APR 2012 11:14:00"
# DATE=$(date '+%d %m %Y %H:%M:%S')
DATE=$(date '+%d %b %Y %H:%M:%S' | tr '[:lower:]' '[:upper:]')
echo -e "Setting Date to ${DATE}"
curl -X POST http://${HOST}:${HTTP_PORT}/system/system-date --data "${DATE}" # or -d @data.txt
echo -e ""
#
echo -e "Bye now ✋"
#
