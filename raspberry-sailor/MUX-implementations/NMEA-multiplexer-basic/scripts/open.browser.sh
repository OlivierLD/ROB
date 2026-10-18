#!/bin/bash
if [[ "${SERVER_HTTP_PORT}" == "" ]]; then
  SERVER_HTTP_PORT=8888
fi
open http://localhost:${SERVER_HTTP_PORT}/zip/index.html
echo -e "Browser should be open. Port is ${SERVER_HTTP_PORT}"