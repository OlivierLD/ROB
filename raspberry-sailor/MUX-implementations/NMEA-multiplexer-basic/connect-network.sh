#!/bin/bash
echo -e "Disconnecting first..."
./disable-hotspot.sh
#
echo -e "Available network(s):"
nmcli dev wifi list
#
echo -en "Network to connect to: "
read NETWORK_NAME
echo -en "Password: "
read PSWD
#
nmcli dev wifi connect ${NETWORK_NAME} password ${PSWD}
#
nmcli dev wifi
nmcli dev wifi show-password