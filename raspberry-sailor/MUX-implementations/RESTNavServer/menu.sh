#!/bin/bash
#
echo -e "+------------ T O P   L E V E L   M E N U -------------+"
echo -e "| 1 - Multiplexer Use-Cases and other demos            |"
echo -e "+------------------------------------------------------+"
echo -e "| 2 - T900 (Dieumegard & Bataille)                     |"
echo -e "| 3 - Celestial Almanacs publishing                    |"
echo -e "|     - Can also be published from some Web Interfaces |"
echo -e "| 4 - Long Term Almanacs publishing                    |"
echo -e "| 5 - Tables de Corrections des Hauteurs               |"
echo -e "+------------------------------------------------------+"
echo -e "| 6 - Tide tables (samples).                           |"
echo -e "|     - Look in the code for details...                |"
echo -e "+------------------------------------------------------+"
echo -e "| Q - Quit                                             |"
echo -e "+------------------------------------------------------+"
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
    ./launchers/xsl/almanac.sh
    ;;
  "4")
    clear
    ./launchers/xsl/perpetual.sh
    ;;
  "5")
    clear
    ./launchers/xsl/corrections.sh
    ;;
  "6")
    clear
    ./launchers/xsl/tidePublishTest.sh
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
