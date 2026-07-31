#!/bin/bash
#
DARWIN=$(uname -a | grep Darwin)
#
# export PS1="\[\033[01;34m\]\w\[\033[33m\]\$(./get-ip-v4.sh)\[\033[00m\] $ "
#
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
  # export PS1="${PWD} [$(./get-ip-v4.sh)]"
  export PS1="\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[33m\] $(./get-ip-v4.sh)\[\033[00m\] $ "
else
	echo Assuming Linux/Raspberry Pi
  # export PS1="${PWD} [$(hostname -I)]"
  export PS1="\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[33m\] $(./get-ip-v4.sh)\[\033[00m\] $ "
fi