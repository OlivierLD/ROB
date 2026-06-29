#!/bin/bash 
#
echo -e "Creating hotspot..."
#
sleep 30
#
nmcli device disconnect wlan0
nmcli device wifi hotspot ssid RPiNetwork password PassWord
#
nmcli dev wifi show-password


