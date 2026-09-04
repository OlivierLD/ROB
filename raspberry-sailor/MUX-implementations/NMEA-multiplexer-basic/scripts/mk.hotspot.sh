#!/bin/bash
#
# Create WiFI HotSpot
#
NETWORK_NAME=RPiHotSpot
NETWORK_PSWD=PassWord
NETWORK_IFACE=wlan0
#
if [[ "$1" == "-h" || "$1" == "-help" || "$1" == "--help" || "$1" == "?" ]]; then
  echo -e "CLI parameters are:"
  echo -e "--iface:, like in --iface:wlan0"
  echo -e "--ssid:, like in --ssid:RPiHotSpot"
  echo -e "--pswd:, like in --pswd:PassWord"
  echo -e "-h, -help, --help, ? : you're on it!"
  exit 0
fi
#
for ARG in "$@"; do
  echo -e "Managing prm ${ARG}"
  if [[ "${ARG}" == "--iface:"* ]]; then
    NETWORK_IFACE=${ARG#*:}
  elif [[ "${ARG}" == "--ssid:"* ]]; then
    NETWORK_NAME=${ARG#*:}
  elif [[ "${ARG}" == "--pswd:"* ]]; then
    NETWORK_PSWD=${ARG#*:}
  else
    echo -e "Unmanaged CLI prm [${ARG}] ..."
  fi
done
#
echo -e "Processing HotSpot [${NETWORK_NAME}], pswd [${NETWORK_PSWD}], interface ${NETWORK_IFACE}"
#
nmcli con add type wifi ifname "${NETWORK_IFACE}" con-name "${NETWORK_NAME}" autoconnect yes ssid "${NETWORK_NAME}"
nmcli con modify "${NETWORK_NAME}" 802-11-wireless.mode ap 802-11-wireless.band bg ipv4.method shared
nmcli con modify "${NETWORK_NAME}" wifi-sec.key-mgmt wpa-psk
nmcli con modify "${NETWORK_NAME}" wifi-sec.psk "${NETWORK_PSWD}"
nmcli con up "${NETWORK_NAME}"
#