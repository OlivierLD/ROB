#!/bin/bash
# Find the port of a USB device, like a GPS
#
echo -e "Unplug the GPS from its USB port"
echo -en "Hit [Return] when done"
read USER_RESP
ls -l /dev/tty* > before.txt
# 
echo -e "Now, plug in you GPS"
echo -en "Hit [Return] when done"
read USER_RESP
ls -l /dev/tty* > after.txt
#
echo -e "The USB port of the device should be:"
diff before.txt after.txt
#
echo -e "Bye!"

