#!/bin/bash
IP_ADDR=$(hostname -I | awk '{ print $1 }')
#
I2C_ADDR=0x76
VERBOSE=true
#
# Do not omit the '-u' to get the output in the log file !!
#
nohup python3 -u REST_and_WEB_BME280_server.py --address:${I2C_ADDR} --verbose:${VERBOSE} --machine-name:${IP_ADDR} > bmp.log 2>&1 &
# nohup -u python3 REST_and_WEB_BME280_server.py --address:${I2C_ADDR} --verbose:${VERBOSE} --machine-name:${IP_ADDR} &
echo -e "On its way, on address ${IP_ADDR} !"

