#!/bin/bash
DARWIN=$(uname -a | grep Darwin)
#
# export PS1="\[\033[01;34m\]\w\[\033[33m\]\$(./get-ip-v4.sh)\[\033[00m\] $ "
#
if [[ "$DARWIN" != "" ]]; then
  # To use on Mac
  echo -e "$(ifconfig | grep inet | grep 192 | awk '{ print $2 }')"
else
  echo -e "$(hostname -I | awk '{ print $1 }')"
fi