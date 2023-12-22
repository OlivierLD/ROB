#!/bin/bash
nohup python3 REST_and_WEB_BME280_server.py --address:0x76 --verbose:false --machine-name:192.168.1.38 > bmp.log 2>&1 &
echo -e "On its way !"

