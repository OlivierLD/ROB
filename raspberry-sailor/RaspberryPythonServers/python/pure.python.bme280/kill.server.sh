#!/bin/bash
SUDO=""
# SUDO="sudo "
#
ps -ef | grep REST_and_WEB_BME280_server | grep -v grep | awk '{ print $2 }' > km
NB_L=$(cat km | wc -l)
if [[ ${NB_L} == 0 ]]; then
  echo No server process found.
fi
for pid in $(cat km); do
  echo Killing server process ${pid}
  ${SUDO} kill -15 ${pid}
done
rm km

