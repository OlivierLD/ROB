#!/usr/bin/env python3
#
# One oled SSD1306 screen
# Clear it
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
# pip3 install adafruit-circuitpython-ssd1306
#
# See https://readthedocs.org/projects/adafruit-circuitpython-ssd1306/
# For drawings: https://learn.adafruit.com/micropython-hardware-ssd1306-oled-display/circuitpython#drawing-2902524
# Doc at https://www.tutorialspoint.com/python_pillow/python_pillow_imagedraw_module.htm
#        https://pillow.readthedocs.io/en/stable/reference/ImageDraw.html
#
# Runtime CLI parameters are: (see below for more details)
# --help
# --wiring: "I2C" (default) or "SPI"
# --verbose: default false
# --height: 32 or 64. Default 32
#
import sys
import os
from typing import Dict
import board
import digitalio
from digitalio import DigitalInOut, Direction, Pull   # for the push-buttons
import PIL
from PIL import Image, ImageDraw, ImageFont
import adafruit_ssd1306  # pip3 install adafruit-circuitpython-ssd1306

__version__ = "0.0.2"
__repo__ = "https://github.com/OlivierLD/ROB"

verbose: bool = False
oled_wiring_option: str = "I2C"   # Default. Can be "I2C" or "SPI"


HELP_PRM_PREFIX: str              = "--help"           # See the help_display method for more details
WIRING_PRM_PREFIX: str            = "--wiring:"
VERBOSE_PRM_PREFIX: str           = "--verbose:"
HEIGHT_PRM_PREFIX: str            = "--height:"

#
# Change these to the right size for your display!
#
WIDTH: int   = 128
HEIGHT: int  = 64                 # Change to 32 or 64 if needed. It is also a CLI prm (See HEIGHT_PRM_PREFIX)
BORDER: int  = 5

WHITE: int   = 255
BLACK: int   = 0

board_type = os.uname().machine
print(f"Board: {board_type}")

oled = None
server_pid: int = os.getpid()  # process id


# Define the Reset Pin
reset_pin = board.D4  # Pin #7
oled_reset = digitalio.DigitalInOut(reset_pin)

current_value: int = 0
keep_looping: bool = True
nmea_cache: Dict[str, object] = None


def display_help() -> None:
    """
    Guess what !
    """
    print("Available CLI parameters are:")
    print(f"{HELP_PRM_PREFIX} - Display help and exit")
    print(f"{WIRING_PRM_PREFIX} - Screen wiring. SPI or I2C")
    print(f"{VERBOSE_PRM_PREFIX} - Verbose level 1. true or false, default false")
    print(f"{HEIGHT_PRM_PREFIX} - Screen Height (32 or 64), default 32")
    return


# Manage what goes on, on the display
print("Starting process #{}...".format(server_pid))
print("To stop, do a Ctrl-C, or from a terminal, a kill -15 {}".format(server_pid))

#
# Main part.
#
if __name__ == '__main__':

    if len(sys.argv) > 0:  # Script name + X args
        for arg in sys.argv:
            if arg[:len(HELP_PRM_PREFIX)] == HELP_PRM_PREFIX:
                print("Display Help and exit")
                display_help()
                exit(0)
            if arg[:len(VERBOSE_PRM_PREFIX)] == VERBOSE_PRM_PREFIX:
                verbose = (arg[len(VERBOSE_PRM_PREFIX):].lower() == "true")
            if arg[:len(WIRING_PRM_PREFIX)] == WIRING_PRM_PREFIX:
                wiring_option = arg[len(WIRING_PRM_PREFIX):]
                if wiring_option != "SPI" and wiring_option != "I2C":
                    print(f"Wiring Option must be SPI or I2C, not {wiring_option}. Keeping {oled_wiring_option}.")
                else:
                    oled_wiring_option = wiring_option
            if arg[:len(HEIGHT_PRM_PREFIX)] == HEIGHT_PRM_PREFIX:
                try:
                    user_height = int(arg[len(HEIGHT_PRM_PREFIX):])
                    if user_height == 32 or user_height == 64:
                        HEIGHT = user_height
                    else:
                        print(f"Height must be 32 or 64, not {user_height}")
                except Exception as error:
                    print(f"Height error: {repr(error)}")

    # Summarize all options
    print(f"Running process (id {server_pid}) with config:\n" +
          f"- verbose {verbose}\n" +
          f"- wiring {oled_wiring_option}\n" +
          f"- screen height {HEIGHT}")

    # initialize the oled screen
    if oled_wiring_option == "I2C":
        # Use for I2C.
        i2c = board.I2C()  # uses board.SCL and board.SDA
        # i2c = board.STEMMA_I2C()  # For using the built-in STEMMA QT connector on a microcontroller
        print(f"Using RESET {reset_pin}")
        try:
            oled: adafruit_ssd1306.SSD1306_I2C = adafruit_ssd1306.SSD1306_I2C(WIDTH, HEIGHT, i2c, addr=0x3C,
                                                                              reset=oled_reset)
        except:
            print("No I2C SSD1306 was found...")
            oled = None
    else: # Means SPI
        # Use for SPI
        spi = board.SPI()
        reset_pin = board.D24  # pin #18
        oled_reset = digitalio.DigitalInOut(reset_pin)  # GPIO 24, Pin #18
        # oled_cs = digitalio.DigitalInOut(board.D5)
        cs_pin = board.D8  # Pin #24
        oled_cs = digitalio.DigitalInOut(cs_pin)  # Pin #24
        # oled_dc = digitalio.DigitalInOut(board.D6)
        dc_pin = board.D23  # Pin #16
        oled_dc = digitalio.DigitalInOut(dc_pin)  # Pin #16
        print(f"Using RESET {reset_pin}")
        print(f"Using CS {cs_pin}")
        print(f"Using DC {dc_pin}")

        try:
            oled: adafruit_ssd1306.SSD1306_SPI = adafruit_ssd1306.SSD1306_SPI(WIDTH, HEIGHT, spi, oled_dc, oled_reset,
                                                                              oled_cs)
            # print(f"SSD1306 is a {type(oled)}")
        except:
            print("No SPI SSD1306 was found...")
            oled = None

    # Initialize OLED screen.
    # Clear display.
    if oled is not None:
        oled.fill(BLACK)
        oled.show()

    print("Cleaned SSD1306.")

