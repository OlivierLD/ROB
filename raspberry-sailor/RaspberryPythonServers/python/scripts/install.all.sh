#!/bin/bash
#
# Required dependencies for the TCP and REST servers
# Needs Internet access.
# Run with sudo, so scripts can be run as root.
#
echo -e "For the BMP180, look into https://learn.adafruit.com/using-the-bmp085-with-raspberry-pi/using-the-adafruit-bmp-python-library"
#
sudo pip3 install adafruit-circuitpython-htu21d
sudo pip3 install adafruit-circuitpython-bme280
sudo pip3 install adafruit-circuitpython-lis3mdl
sudo pip3 install pyyaml
sudo pip3 install adafruit-circuitpython-lsm303dlh-mag
#
sudo pip3 install adafruit-circuitpython-ssd1306
