#!/bin/bash
CP=./build/libs/workbench-1.0-all.jar
#
REBUILD=false
if [[ "${REBUILD}" == "true" ]]; then
  java -cp ${CP} utils.CSVtoJSON
fi
#
node server.js --port:1234 &
NODE_PID=$!
#
open http://localhost:1234/web/email.sender.html
#
echo -e "Do not forget to stop the node server..."
echo -e "To kill it, do a: kill -9 ${NODE_PID}"
