#!/bin/bash
#
# Read a GPS
#
CP=./build/libs/Serial-IO-1.0-all.jar
CP=${CP}:/usr/share/java/RXTXcomm.jar
echo -e "Make sure the GPS is connected through its USB socket."
#
SERIAL_PORT=/dev/ttyACM0 # RPi
# SERIAL_PORT=/dev/ttyUSB0 # RPi
# SERIAL_PORT=/dev/ttyS80 # RPi
# SERIAL_PORT=/dev/tty.Bluetooth-Incoming-Port # Mac
# SERIAL_PORT=/dev/tty.usbmodem14201 # Mac
# SERIAL_PORT=/dev/tty.usbserial # Mac
#
if [[ "${SERIAL_PORT}" == "/dev/ttyACM0" ]]; then
  echo -s "Note: There is some bug in libRxTx-java regarding the access to /dev/ttyACM0"
  echo -s "If this is your case, try creating a symbolic link on the port, and access it through its link:"
  echo -s " $ sudo ln -s /dev/ttyACM0 /dev/ttyS80"
  echo -s "Then try reading or writing on /dev/ttyS80"
fi
#
BAUD_RATE=4800
#
JAVA_OPTS="-Dserial.port=$SERIAL_PORT -Dbaud.rate=$BAUD_RATE"
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)
#
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions"  # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni"              # RPi
  SUDO="sudo "
fi
#
# Sentence filter? like RMC,GLL,GSA
if [[ "$1" != "" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -Dfilters=$1"
fi
#
COMMAND="${SUDO}java ${JAVA_OPTS} -cp ${CP} sample.GPSReader"
echo -e "Executing ${COMMAND} ..."
echo -e "Enter [Return]"
read a
${COMMAND}
