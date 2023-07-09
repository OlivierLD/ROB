#!/bin/bash
#
# Show server processes (MUX, Python-REST, Python-TCP)
#
ps -ef | grep GenericNMEAMultiplexer | grep -v grep | grep -v killns
#
ps -ef | grep python | grep REST 
#
ps -ef | grep python | grep TCP_ 

