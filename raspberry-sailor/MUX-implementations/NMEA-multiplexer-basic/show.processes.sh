#!/bin/bash
#
# Show server processes (MUX, Python-REST, Python-TCP)
#
echo -e "+-----------------------+"
echo -e "| NMEA Multiplexer(s) : |"
echo -e "+-----------------------+"
ps -ef | grep GenericNMEAMultiplexer | grep -v grep | grep -v killns
#
echo -e "+-----------------------+"
echo -e "| Python server(s) :    |"
echo -e "+-----------------------+"
ps -ef | grep python | grep REST_ | awk -v var="Launched:" '{ print var, $5, $7, "-", $8, $9 }'
ps -ef | grep python | grep REST_
#
ps -ef | grep python | grep TCP_ | awk -v var="Launched:" '{ print var, $5, $7, "-", $8, $9 }'
ps -ef | grep python | grep TCP_
#
echo -e "---- t h e   e n d ----"