#!/bin/bash
#
# For a list of the kill signals:
# Try $> kill -l
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)
#
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
else
	echo Assuming Linux/Raspberry Pi
  # No sudo require if running as root, in Docker for example.
  if [[ "$(whoami)" != "root" ]]; then
    SUDO="sudo "
  fi
fi
#
ps -ef | grep 'ython.*http.server' | grep -v grep | awk '{ print $2 }' > km
NB_L=$(cat km | wc -l)
if [[ ${NB_L} -eq 0 ]]; then
  echo -e "No Python fax server process found."
else
  echo -e "Will kill ${NB_L} processes."
fi
for pid in $(cat km); do
  echo Killing process ${pid}
  ${SUDO} kill -15 ${pid}
done
rm km

