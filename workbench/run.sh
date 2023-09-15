#!/bin/bash
CP=./build/libs/workbench-1.0-all.jar
#
java -cp ${CP} utils.CSVtoJSON
#
node server.js &
#
open http://localhost:8080/web/email.sender.html
#
echo -e "Do not forget to stop the node server..."
