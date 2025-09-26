#!/bin/bash

export CP=./build/libs/MPS-1.0-all.jar
export CLI_PRM='--time-1:2025-08-20T10:40:31 --gha-1:339º17.40 --decl-1:N12º16.80 --alt-1:49º22.52 --time-2:2025-08-20T10:40:31 --alt-2:66º33.85 --gha-2:13º41.85 --decl-2:N25º46.13 --verbose:false'
#
SYSTEM_STUFF="-Dprm.verbose=false"
# echo -e "With CLI PRM ${CLI_PRM}"
java -classpath ${CP} ${SYSTEM_STUFF} mps.pg.PlayGround07 ${CLI_PRM}
echo -e "Done"