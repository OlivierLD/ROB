#!/bin/bash
pushd $(dirname $0)
CP=./build/libs/ChartComponents-1.0-all.jar
#
KEEP_LOOPING=true
while [[ "${KEEP_LOOPING}" == "true" ]]; do
  clear
  echo -e "+--------------------------------------------------------------------------------+"
  echo -e "|                            S W I N G   S A M P L E S                           |"
  echo -e "+---------------------------+-------------------------+--------------------------+"
  echo -e "|  1 - Satellites           |  2 - Atlantic           |  3 - Big Width           |"
  echo -e "|  4 - Case Study           |  5 - Conic Equidistant  |  6 - Globe               |"
  echo -e "|  7 - Increasing Latitude  |  8 - Lambert            |  9 - Mercator Canvas     |"
  echo -e "| 10 - Mercator Scale       | 11 - Mid Atlantic       | 12 - Viking Sun Compass  |"
  echo -e "| 13 - Night and Day        | 14 - North America      | 15 - Pacific             |"
  echo -e "| 16 - Plotting Sheet (1)   | 17 - Plotting Sheet (2) | 18 - Polar Stereo        |"
  echo -e "| 19 - Polar Stereo (south) | 20 - SF Bay             | 21 - Shore Detection     |"
  echo -e "| 22 - Stereographic        | 23 - The trip           | 24 - Two Globes          |"
  echo -e "| 25 - World                |                         |                          |"
  echo -e "+---------------------------+-------------------------+--------------------------+"
  echo -e "| Q - Quit                                                                       |"
  echo -e "+--------------------------------------------------------------------------------+"
  echo -en "You choose > "
  read RESP
  case "${RESP}" in
    "1")
      java -cp ${CP} examples.satellite.Main4Tests
      ;;
    "2")
      java -cp ${CP} examples.atlantic.Main4Tests
      ;;
    "3")
      java -cp ${CP} examples.bigwidth.Main4Tests
      ;;
    "4")
      java -cp ${CP} examples.casestudy.Main4Tests
      ;;
    "5")
      java -cp ${CP} examples.conic_equidistant.Main4Tests
      ;;
    "6")
      java -cp ${CP} examples.globe.Main4Tests
      ;;
    "7")
      java -cp ${CP} examples.incrlat.Main4Tests
      ;;
    "8")
      java -cp ${CP} examples.lambert.Main4Tests
      ;;
    "9")
      java -cp ${CP} examples.mercatorcanvas.Main4Tests
      ;;
    "10")
      java -cp ${CP} examples.mercatorscale.Main4Tests
      ;;
    "11")
      java -cp ${CP} examples.midatlantic.Main4Tests
      ;;
    "12")
      java -cp ${CP} examples.misc.VikingSunCompass
      ;;
    "13")
      java -cp ${CP} examples.nightnday.Main4Tests
      ;;
    "14")
      java -cp ${CP} examples.northamerica.Main4Tests
      ;;
    "15")
      java -cp ${CP} examples.pacific.Main4Tests
      ;;
    "16")
      java -cp ${CP} examples.plottingsheet.one.Main4Tests
      ;;
    "17")
      java -cp ${CP} examples.plottingsheet.two.Main4Tests
      ;;
    "18")
      java -cp ${CP} examples.polarstereographic.Main4Tests
      ;;
    "19")
      java -cp ${CP} examples.polarstereographicsouth.Main4Tests
      ;;
    "20")
      java -cp ${CP} examples.sfbay.Main4Tests
      ;;
    "21")
      java -cp ${CP} examples.shoredetection.Main4Tests
      ;;
    "22")
      java -cp ${CP} examples.stereographic.Main4Tests
      ;;
    "23")
      java -cp ${CP} examples.trip.Main4Tests
      ;;
    "24")
      java -cp ${CP} examples.twoglobe.Main4Tests
      ;;
    "25")
      java -cp ${CP} examples.world.Main4Tests
      ;;
    "Q" | "q")
      KEEP_LOOPING=false
      ;;
    *)
      echo -e "Unknown option ${RESP}"
      echo -en "Hit [return]"
      read RESP
      ;;
  esac
done
#
popd
echo -e "Bye !"
