#!/bin/bash
#
# For a list of the kill signals:
# Try $> kill -l
#
echo -e "Executing ${0}..."
#
# Kill all admin servers
#
SUDO="sudo "
#
ps -ef | grep SystemServer | grep -v grep | grep -v killserver | awk '{ print $2 }' > km
NB_L=$(cat km | wc -l)
if [[ ${NB_L} == 0 ]]; then
  echo No Server process found.
fi
for pid in $(cat km); do
  echo Killing Server process ${pid}
  ${SUDO} kill -15 ${pid}
done
rm km
#

