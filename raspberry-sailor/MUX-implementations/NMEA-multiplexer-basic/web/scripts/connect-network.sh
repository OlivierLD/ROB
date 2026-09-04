#!/bin/bash
#
# NOT to be run with . ./connect-network.sh
#
pushd $(dirname $0) > /dev/null
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
#
# . ./change-prompt.sh
echo -e "Use . ./change-prompt.sh to update the prompt with new IP address."
popd > /dev/null