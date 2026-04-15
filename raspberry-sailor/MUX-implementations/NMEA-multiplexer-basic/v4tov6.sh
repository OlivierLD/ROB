#!/bin/bash -e

IP=$1
echo -e "Pinging ${IP} ..."
ping -c 1 ${IP} > /dev/null 2>&1
# was -an below ...
arp -n ${IP}
MAC=$(arp -n ${IP} | awk '{ print $4 }')
IFACE=$(arp -n ${IP} | awk '{ print $7 }')

# requires a pip3 install netaddr
python3 -c "
from netaddr import IPAddress
from netaddr.eui import EUI
mac = EUI(\"$MAC\")
ip = mac.ipv6(IPAddress('fe80::'))
print('{ip}%{iface}'.format(ip=ip, iface=\"$IFACE\"))"