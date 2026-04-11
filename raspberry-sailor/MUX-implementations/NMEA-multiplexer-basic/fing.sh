#!/bin/bash
echo -e "Fing, pure bash! WiP..."


# Stolen from gradlew
oops ( ) {
  echo -e "host command is not available..."
  echo -e "try this: sudo apt install bind9-host"
  exit 1
}

oops2 ( ) {
  echo -e "dnslookup command is not available..."
  echo -e "try this: sudo apt install dnsutils"
  exit 1
}

# For 'host'
# sudo apt install bind9-host
ret=$(host 192.168.1.1) || oops

ret=$(nslookup 192.168.1.1) || oops2

VERBOSE=false

if [[ "$1" == "-v" ]]; then
  VERBOSE=true
fi
#
addr=$(ifconfig | grep 'inet 192' | awk '{ print $2 }')
radic=$(echo "${addr%.*}".)

# echo -e "Starting with radical ${radic} Scanning ${radic}1 to ${radic}254"
echo -e "Discovery on:      ${radic}0/254 \n"

for i in {1..254}; do
# for i in {1..50}; do
  toping=${radic}${i}
  if [[ "${VERBOSE}" == "true" ]]; then
    echo "...Pinging ${toping}"
  fi
  date=$(date '+%H:%M:%S')
  # response=$(ping -c1 -W 1 ${toping} | grep 'transmitted')
  response=$(ping -c1 -W 1 ${toping} | grep 'packet loss')
  if [[ "${VERBOSE}" == "true" ]]; then
    echo "...Got ${response}"
  fi
  if [[ "${response}" != *", 0 "*"received"* ]]; then
    # echo -e "${toping} is alive."
    hostname=$(host "${toping}" | awk '{ print $5 }')
    if [[ "${hostname}" == *"." ]]; then
      # echo -e "Transforming [${hostname}]..."
      # hostname=${hostname:0:-1}  # Remove last dot.
      hostname=${hostname%?}  # Remove last dot.
    fi
    ipv6=$(nslookup -query=hinfo ${toping} | grep 'Server:' | awk '{ print $2 }')
    echo -e "${date} > Host is up:   ${toping}"
    #        HH:MM:SS > H...
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