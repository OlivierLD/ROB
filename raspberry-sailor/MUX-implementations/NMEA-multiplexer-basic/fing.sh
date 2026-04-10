#!/bin/bash
echo -e "Fing, pure bash! WiP..."

VERBOSE=false

if [[ "$1" == "-v" ]]; then
  VERBOSE=true
fi
#
addr=$(ifconfig | grep 'inet 192' | awk '{ print $2 }')
radic=$(echo "${addr%.*}".)

# For 'host'
# sudo apt install bind9-host

echo -e "Starting with radical ${radic} Scanning ${radic}1 to ${radic}254"

for i in {1..254}; do
# for i in {1..50}; do
  toping=${radic}${i}
  if [[ "${VERBOSE}" == "true" ]]; then
    echo "...Pinging ${toping}"
  fi
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
    echo -e "${toping}, ${hostname} is alive."
  else
    if [[ "${VERBOSE}" == "true" ]]; then
      echo -e "Ooops: ${response} ..."
    fi
  fi
done
echo -e "Scan completed"
#