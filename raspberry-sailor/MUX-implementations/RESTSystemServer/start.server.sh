#!/bin/bash

# CP=../build/libs/RESTNavServer-1.0-all.jar
CP=$(find . -name '*-all.jar')

JAVA_OPT="-Drest.verbose=true"

COMMAND="${SUDO}java -cp ${CP} ${JAVA_OPTS} systemrest.SystemServer"
if [[ "${CMD_VERBOSE}" == "Y" || 1 -eq 1 ]]; then    # Always true...
  echo -e "Running ${COMMAND}"
fi
#
${COMMAND}
#
echo -e "Bye now ✋"
#
