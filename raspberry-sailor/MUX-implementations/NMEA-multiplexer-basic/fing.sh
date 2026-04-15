#!/bin/bash
echo -e "Fing, pure bash! WiP..."

# Stolen from gradlew
oops ( ) {
  echo -e "host command is not available..."
  echo -e "On Linux, try this: sudo apt install bind9-host"
  exit 1
}

oops2 ( ) {
  echo -e "dnslookup command is not available..."
  echo -e "On Linux, try this: sudo apt install dnsutils"
  exit 1
}

# For 'host': sudo apt install bind9-host
ret=$(host 192.168.1.1) || oops
# For 'nslookup': sudo apt install dnsutils
# ret=$(nslookup 192.168.1.1) || oops2

VERBOSE=false

if [[ "$1" == "-v" ]]; then
  VERBOSE=true
fi
# '192' below might need to be changed...
addr=$(ifconfig | grep 'inet 192' | awk '{ print $2 }')
radic=$(echo "${addr%.*}".)

echo -e "From ${addr}:"
# echo -e "Starting with radical ${radic} Scanning ${radic}1 to ${radic}254"
echo -e "Discovery on:      ${radic}0/254 \n"

for i in {0..254}; do
# for i in {1..50}; do
  toPing=${radic}${i}
  if [[ "${VERBOSE}" == "true" ]]; then
    echo "...Pinging ${toPing}"
  fi
  date=$(date '+%H:%M:%S')
  # response=$(ping -c1 -W 1 ${toPing} | grep 'transmitted')
  response=$(ping -c1 -W 1 ${toPing} | grep 'packet loss')
  if [[ "${VERBOSE}" == "true" ]]; then
    echo "...Got ${response}"
  fi
  if [[ "${response}" != *", 0 "*"received"* ]]; then
    # echo -e "${toPing} is alive."
    hostname=$(host "${toPing}" | awk '{ print $5 }')
    if [[ "${hostname}" == *"." ]]; then
      # echo -e "Transforming [${hostname}]..."
      # hostname=${hostname:0:-1}  # Remove last dot.
      hostname=${hostname%?}  # Remove last dot.
    fi
    # ipv6=$(nslookup -query=hinfo ${toPing} | grep 'Server:' | awk '{ print $2 }')
    arpOutput=$(arp -n ${toPing} | grep "${toPing}")
    # echo -e "Arp, step 1: ${arpOutput}"
    if [[ "${arpOutput}" == "? "* ]]; then
      # echo -e "Cutting..."
      # arpOutput=${arpOutput:2:}
      arpOutput=$(awk '{print substr($0, 2)}' <<< "${arpOutput}")
      # echo -e "Arp, step 2: ${arpOutput}"
    fi
    ipv6=$(echo "${arpOutput}" | awk '{ print $3 }')
    echo -e "${date} > Host is up:   ${toPing}"
    #        HH:MM:SS > Host ...
    echo -e "           HW Address:   ${ipv6}"
    echo -e "           Hostname:     ${hostname}"
    echo -e ""
  else
    if [[ "${VERBOSE}" == "true" ]]; then
      echo -e "Ooops: ${response} ..."
    fi
  fi
done
echo -e "\nScan completed"
#