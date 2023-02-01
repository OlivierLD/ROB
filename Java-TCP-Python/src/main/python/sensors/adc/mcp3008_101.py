# From https://learn.adafruit.com/mcp3008-spi-adc/python-circuitpython
#
# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

import busio
import digitalio
import board
import adafruit_mcp3xxx.mcp3008 as MCP
from adafruit_mcp3xxx.analog_in import AnalogIn
import time

# create the spi bus
spi = busio.SPI(clock=board.SCK, MISO=board.MISO, MOSI=board.MOSI)

# create the cs (chip select)
cs = digitalio.DigitalInOut(board.D5)

# create the mcp object
mcp = MCP.MCP3008(spi, cs)

# create an analog input channel on pin 0
channel = AnalogIn(mcp, MCP.P0)

print("Raw ADC Value: ", channel.value)
print("ADC Voltage: " + str(channel.voltage) + "V")

while True:
    print('Raw ADC Value: ', channel.value)
    print('ADC Voltage: ' + str(channel.voltage) + 'V')
    time.sleep(0.5)
