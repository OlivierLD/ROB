#!/bin/bash
#
# Dump serial port
#
stty -F /dev/ttyACM0 raw 4800 cs8 clocal
# while [[ 1 == 1 ]]; do echo "task $NB"; sleep 1; NB=$(expr $NB + 1) ; done
# cat /dev/ttyACM0
cat /dev/ttyACM0 | grep -e "RMC"