#!/bin/bash
# Kill MPS server
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
ps -ef | grep MPSServer | grep -v grep | grep -v kill | awk '{ print $2 }' > km
NB_L=`cat km | wc -l | xargs`
# echo Found ${NB_L} lines
if [[ ${NB_L} == 0 ]]; then
  echo No MPSServer process found.
else
  echo -e "Will kill ${NB_L} process(es)"
fi
for pid in `cat km`; do
  echo Killing process ${pid}
  ${SUDO} kill -15 ${pid}
done
rm km