#!/bin/bash
#
# Kill all servers (MUX, Python-REST, Python-TCP)
#
SUDO="sudo "
#
ps -ef | grep GenericNMEAMultiplexer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
NB_L=$(cat km | wc -l)
if [[ ${NB_L} == 0 ]]; then
  echo No MUX process found.
fi
for pid in $(cat km); do
  echo Killing MUX process ${pid}
  ${SUDO} kill -15 ${pid}
done
rm km
#
ps -ef | grep python | grep REST | awk '{ print $2 }' > server.id.txt
#
for pid in $(cat server.id.txt); do
  sleep 5  # Give time to shutdown
  echo Killing REST process ${pid}
  sudo kill -9 ${pid}
done
rm server.id.txt

ps -ef | grep python | grep TCP_ | awk '{ print $2 }' > server.id.txt
#
for pid in $(cat server.id.txt); do
  echo Killing TCP process ${pid}
  sudo kill -9 ${pid}
done
rm server.id.txt

