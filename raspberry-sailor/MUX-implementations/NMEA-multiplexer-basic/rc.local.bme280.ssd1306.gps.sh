#!/bin/bash
#
# rc.local
#
# This script is executed at the end of each multiuser runlevel.
# Make sure that the script will "exit 0" on success or any other
# value on error.
#
# In order to enable or disable this script just change the execution
# bits.
#
# By default this script does nothing.

# Print the IP address
_IP=$(hostname -I) || true
if [ "$_IP" ]; then
  printf "My IP address is %s\n" "$_IP"
fi
#
nmcli device disconnect wlan0
nmcli device wifi hotspot ssid NMEANetwork password PassWord
#
pushd ~pi/nmea-dist
./start.all.sh BME280-SSD
popd
#

exit 0