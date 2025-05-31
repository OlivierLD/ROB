#!/bin/bash
#
# Show server processes (MUX, Python-REST, Python-TCP)
#
echo -e "+-----------------------+"
echo -e "| NMEA Multiplexer(s) : |"
echo -e "+-----------------------+"
ps -ef | grep GenericNMEAMultiplexer | grep -v grep | grep -v killns
#
echo -e "+------------------------+"
echo -e "| Python server(s) :     |"
echo -e "+------------------------+"
ps -ef | grep python | grep REST
#
ps -ef | grep python | grep TCP_ 
#
echo -e "---- t h e   e n d ----"
