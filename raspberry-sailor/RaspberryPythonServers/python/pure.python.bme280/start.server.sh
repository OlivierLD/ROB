#!/bin/bash
IP_ADDR=$(hostname -I | awk '{ print $1 }')
#
I2C_ADDR=0x76
VERBOSE=false
STORE_RESTORE=true
#
echo -e "Starting server with I2C addr ${I2C_ADDR}, on ${IP_ADDR}, verbose=${VERBOSE}"
#
# Do not omit the '-u' to get the output in the log file !!
#
nohup python3 -u REST_and_WEB_BME280_server.py --address:${I2C_ADDR} --verbose:${VERBOSE} --machine-name:${IP_ADDR} --store-restore:${STORE_RESTORE} > bmp.log 2>&1 &
# nohup -u python3 REST_and_WEB_BME280_server.py --address:${I2C_ADDR} --verbose:${VERBOSE} --machine-name:${IP_ADDR} &
PID=$!
echo -e "Process [${PID}] on its way, on address ${IP_ADDR} !"
