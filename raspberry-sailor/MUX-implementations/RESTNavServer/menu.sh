#!/bin/bash
#
echo -e "+----------- Top level menu  ------------+"
echo -e "| 1 - Multiplexer Demos                  |"
echo -e "+----------------------------------------+"
echo -e "| 2 - T900 (Dieumegard & Bataille)       | "
echo -e "| 3 - Celestial Almanacs publishing      |"
echo -e "| 4 - Long Term Almanacs publishing      |"
echo -e "| 5 - Tables de Corrections des Hauteurs |"
echo -e "+----------------------------------------+"
echo -e "| Q - Quit                               |"
echo -e "+----------------------------------------+"
echo -en "You choose > "
read RESP
case "${RESP}" in
  "1")
    clear
    ./launchers/demoLauncher.sh
    ;;
  "2")
    clear
    ./launchers/pub/tables/publish.tables.sh
    ;;
  "3")
    clear
    ./launchers/pub/almanacs/almanac.sh
    ;;
  "4")
    clear
    ./launchers/pub/almanacs/perpetual.sh
    ;;
  "5")
    clear
    ./launchers/pub/almanacs/corrections.sh
    ;;
  "Q" | "q")
    echo -e "Bye"
    exit 0
    ;;
  *)
    echo -e "Unknown option [${RESP}]"
    echo -e "Try again"
    ;;
esac
