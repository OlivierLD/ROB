#!/usr/bin/env bash
#
# Standalone operation (no server)
#
CP=./build/libs/routing-1.0-all.jar
#
# Example:
# ./run.routing.sh --from-lat 37.122 --from-lng -122.5 --to-lat -9.75 --to-lng -139.10 --start-time "2017-10-16T07:00:00" --time-interval 24 --grib-file "./samples/GRIB_2017_10_16_07_31_47_PDT.grb" --polar-file "./samples/CheoyLee42.polar-coeff" --output-type "JSON" --speed-coeff 0.75 --verbose true
# ./run.routing.sh --from-lat 47.65 --from-lng -3.1 --to-lat 43.2 --to-lng -64.75 --start-time "2023-03-12T07:00:00" --time-interval 12 --grib-file "./samples/NorthAtlantic_2023_03_12_15_58_13_CET.grb" --polar-file "./samples/CheoyLee42.polar-coeff" --output-type "TXT" --speed-coeff 0.75 --verbose true
#
java -cp ${CP} gribprocessing.utils.BlindRouting $*
#
