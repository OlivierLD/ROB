#!/bin/bash
#
# FROM=$( dirname "${BASH_SOURCE[0]}" )
# FROM=$(echo -e "$(dirname -- "$0")")
FROM=${PWD}
DARWIN=$(uname -a | grep Darwin)
#
RUN_FROM=$(dirname $(find $HOME -name get-ip-v4.sh 2> /dev/null))
echo -e "To be run from ${RUN_FROM}"
if [[ "${FROM}" != "${RUN_FROM}" ]]; then
  echo -e "You're in the wrong directory (${FROM})..."
  # exit 1
fi
#
# export PS1="\[\033[01;34m\]\w\[\033[33m\]\$(./get-ip-v4.sh)\[\033[00m\] $ "
#
if [[ "$DARWIN" != "" ]]; then
	echo -e "Running on Mac (from ${FROM})"
  # export PS1="${PWD} [$(./get-ip-v4.sh)]"
  export PS1="\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[33m\] $(./get-ip-v4.sh)\[\033[00m\] $ "
else
	echo -e "Assuming Linux/Raspberry Pi (from ${FROM})"
  # export PS1="${PWD} [$(hostname -I)]"
  export PS1="\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[33m\] $(./get-ip-v4.sh)\[\033[00m\] $ "
fi