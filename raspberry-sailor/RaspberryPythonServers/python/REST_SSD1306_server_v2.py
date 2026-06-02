#!/usr/bin/env python3
#
# One oled SSD1306 screen
# 2 push-buttons (external)
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
# pip3 install adafruit-circuitpython-ssd1306
#
# Different from the REST_SSD1306_server.py.
# That one receives full the cache (as JSON) and manages the display of the data by itself.
# It can also deal with 2 push-buttons for user's interaction, to choose the data to be displayed. (scroll up & down, and others)
#
# --- IMPORTANT ----------------------------------------------------------------------------------
# -> Warning: the buttons are wired on 3V3, not GND !!! See the Fritzing diagrams about that (in
#      ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/doc_resources/ and in
#      ROB/Java-TCP-Python/resources)
# ------------------------------------------------------------------------------------------------
#
# Provides a ScreenSaving mode, see ENABLE_SCREEN_SAVER_AFTER variable.
# To be used as an nmea-cache-publisher (see yaml files for details and examples)
#
# Work In Progress !
# Do run a "curl -X GET /ssd1306/oplist" !
#
# See https://readthedocs.org/projects/adafruit-circuitpython-ssd1306/
# For drawings: https://learn.adafruit.com/micropython-hardware-ssd1306-oled-display/circuitpython#drawing-2902524
#
# Runtime CLI parameters are:
# --wiring: "I2C" (default) or "SPI"
# --machine-name: The machine name or its IP address. Default "127.0.0.1"
# --port: default 8080
# --verbose: default false
# --height: 32 or 64. Default 32
# --screen-saver: "on", or "off". Default "on"
# --rotate: "true" or "false". Default "false". Rotate the screen by 180°.
#
# --data: Like BSP,SOG,POS,..., etc. The list of data to be displayed, in the order of the list.
#         Default "BSP,SOG,COG,POS,WPT". Managed in the code.
#         Supported data (see format_data method): BSP, POS, SOG, COG, NAV, ATM, ATP, PRS, HUM, WPT, NET
#
# Acts like a bus. All the data circulate (in the cache), you choose - see the dcata obove - what to display.
#
# See the script start.SSD1306.REST.server.v2.sh for examples of how to run this code, with different parameters.
#
# Managed:
# - click        (on both buttons, 1 & 2)
# - long click   (on button 1)
# - double click (available)
#
# Long press on button 1 will suggest a shutdown.
#
import json
import sys
import os
# import datetime
import time
import subprocess
import signal
import socket
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Dict
from typing import List
import threading
import board
import digitalio
from digitalio import DigitalInOut, Direction, Pull
import PIL
from PIL import Image, ImageDraw, ImageFont
import adafruit_ssd1306  # pip3 install adafruit-circuitpython-ssd1306
import utils  # local script

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

PATH_PREFIX = "/ssd1306"
server_port: int = 8080
verbose: bool = False
verbose_level2: bool = True
machine_name: str = "127.0.0.1"  # aka localhost

oled_wiring_option: str = "I2C"  # Default. Can be "I2C" or "SPI"

WIRING_PRM_PREFIX: str            = "--wiring:"
MACHINE_NAME_PRM_PREFIX: str      = "--machine-name:"
PORT_PRM_PREFIX: str              = "--port:"
VERBOSE_PRM_PREFIX: str           = "--verbose:"
VERBOSE_2_PRM_PREFIX: str         = "--verbose-2:"
HEIGHT_PRM_PREFIX: str            = "--height:"
SCREEN_SAVER_MODE_PRM_PREFIX: str = "--screen-saver:"  # "on", or "off". Default "on"
ROTATE_PRM_PREFIX: str            = "--rotate:"

DATA_PRM_PREFIX: str              = "--data:"  # Like "BSP,SOG,POS,..., etc". See below

# Supported data (see format_data method):
# BSP, POS, SOG, COG, NAV, ATM, ATP, PRS, HUM, WPT, NET
# TODO: More data, and graphics ?

oled = None
server_pid: int = os.getpid()


# Define the Reset Pin
reset_pin = board.D4  # Pin #7
oled_reset = digitalio.DigitalInOut(reset_pin)

current_value: int = 0
keep_looping: bool = True
nmea_cache: Dict[str, object] = None

ENABLE_SCREEN_SAVER_AFTER: int = 30  # in seconds

LONG_CLICK = 1000           # in ms, 1s
BETWEEN_DOUBLE_CLICK = 300  # in ms, 0.3s

screen_saver_timer: int = 0
screen_saver_on: bool = False
enable_screen_saver: bool = True

# Default list
nmea_data: List[str] = [
    "BSP",  # Boat Speed
    "SOG",  # Speed Over Ground
    "COG",  # Course Over Ground
    "POS",  # Position
    "WPT"   # Waypoint, distance and bearing
]

pin_button_01 = board.D20  # physical pin #38
pin_button_02 = board.D21  # physical pin #40

button_01_pressed_at: int = None
button_02_pressed_at: int = None
previous_button_01_pressed_at: int = None
previous_button_02_pressed_at: int = None

shutdown_suggested: bool = False
shutdown_suggested_at: int = None


def execute_system_command(cmd: str) -> None:
    command: str = cmd
    try:
        result: str = subprocess.check_output(command, shell=True, text=True)
    except Exception as oops:
        result = f"Oops: {repr(oops)}"
        pass
    print(f"Cmd: [{str}] -> {result}")


def get_network_name() -> str:
    command: str = "iwgetid -r"
    try:
        result: str = subprocess.check_output(command, shell=True, text=True)
    except Exception as oops:
        result = f"Oops: {repr(oops)}"
        command = "nmcli dev wifi show-password | grep SSID"
        try:
            result: str = subprocess.check_output(command, shell=True, text=True)
        except Exception as oops2:
            result = f"Oops: {repr(oops2)}"
            pass
        pass
    return result


def get_ip_address() -> str:
    command: str = "hostname -I | awk '{ print $1 }'"
    try:
        result: str = subprocess.check_output(command, shell=True, text=True)
    except Exception as oops:
        result = f"Oops: {repr(oops)}"
        pass
    return result


def reset_screen_saver() -> None:
    global screen_saver_on
    global screen_saver_timer
    if verbose or verbose_level2:
        print("- in reset_screen_saver: Reseting screen saver")
    screen_saver_on = False
    screen_saver_timer = 0


#
# state: True means ON (aka down)
#        False means (back) UP
#
def button_listener(pin, state) -> None:
    global current_value
    global nmea_data
    global screen_saver_on
    global pin_button_01
    global pin_button_02
    global button_01_pressed_at
    global button_02_pressed_at
    global shutdown_suggested
    global shutdown_suggested_at
    global previous_button_01_pressed_at
    global previous_button_02_pressed_at

    # nowms: int = int(datetime.datetime.now().timestamp() * 1000)  # Timestamp in ms
    nowms: int = int(time.time() * 1000)  # Timestamp in ms
    if verbose or verbose_level2:
        print(f">>> Yo! {pin}, state {state} at {nowms}, since previous click: {(nowms - button_01_pressed_at) if button_01_pressed_at is not None else 0} ms")
    if pin == pin_button_01 and state == False:  # Back Up !
        if button_01_pressed_at is None:
            print("1 - Should not happen !! Press longer!")
        diff_up_down_01 = nowms - (button_01_pressed_at if button_01_pressed_at is not None else 0)
        if verbose or verbose_level2:
            print(f"Released button 1: {diff_up_down_01} ms, screen saver on: {screen_saver_on}")
        if button_01_pressed_at is not None:
            if diff_up_down_01 > LONG_CLICK:  # more than 1 sec, long press
                if not screen_saver_on:
                    print(f"Long press on button 1: {diff_up_down_01} ms")  # Will do something sometime!
                    shutdown_suggested = True
                    shutdown_suggested_at = nowms
                else:
                    print(">>>> 1 - Long press, but screen saver was on.")
            elif button_01_pressed_at - (previous_button_01_pressed_at if previous_button_01_pressed_at is not None else 0) < BETWEEN_DOUBLE_CLICK:
                print("Double click button 1?")
            else:  # Short-Click
                if verbose or verbose_level2:
                    print(f"ShortPress button 1: {diff_up_down_01} ms")
                if shutdown_suggested:  # Replied YES to shutdown
                    # This is a shutdown!
                    cwd = os.getcwd()
                    print(f"Shutting down!! from {cwd}...")
                    if False:
                        cmd: str = f"curl -X PUT http://{machine_name}:{server_port}/ssd1306/bye-and-clear-screen"  # Self call
                        print(f"Executing [{cmd}] ...")
                        execute_system_command(cmd)
                    # Kill all, mux will kill the nmea-cache-publisher's
                    cmd: str = "../kill.all.sample.sh"
                    print(f"Executing [{cmd}] ...")
                    execute_system_command(cmd)
                    # Bye !
                else:
                    if verbose or verbose_level2:
                        print(f"... Increasing list index")
                    current_value += 1
        else:
            print("1bis - button_01_pressed_at was None...")
        previous_button_01_pressed_at = button_01_pressed_at

    if pin == pin_button_01 and state == True:
        button_01_pressed_at = nowms
        if verbose or verbose_level2:
            print(f"Button 1 is pressed DOWN at {nowms} ms!")

    if pin == pin_button_02 and state == False:  # Back Up !
        if button_02_pressed_at is None:
            print("2 - Should not happen !! Press longer!")
        diff_up_down_02 = nowms - (button_02_pressed_at if button_02_pressed_at is not None else 0)
        if verbose or verbose_level2:
            print(f"Released button 2: {diff_up_down_02} ms, screen saver on: {screen_saver_on}")
        if button_02_pressed_at is not None:
            if diff_up_down_02 > LONG_CLICK:  # more than 1 sec
                if not screen_saver_on:
                    print(f"Long press on button 2: {diff_up_down_02} ms")  # Will do something sometime!
                else:
                    print(">>>> 2 - Long press, but screen saver was on.")
            elif button_02_pressed_at - (previous_button_02_pressed_at if previous_button_02_pressed_at is not None else 0) < BETWEEN_DOUBLE_CLICK:
                print("Double click button 2?")
            else:  # Short-Click
                if verbose or verbose_level2:
                    print(f"ShortPress button 2: {diff_up_down_02} ms")
                if shutdown_suggested: # Replied NO
                    shutdown_suggested = False
                else:
                    if verbose or verbose_level2:
                        print(f"... Decreasing list index")
                    current_value -= 1
        else:
            print("2bis - button_02_pressed_at was None...")
        previous_button_02_pressed_at = button_02_pressed_at

    if pin == pin_button_02 and state == True:
        button_02_pressed_at = nowms
        if verbose or verbose_level2:
            print(f"Button 2 is pressed DOWN at {nowms} ms!")
    if current_value < 0:
        current_value = len(nmea_data) - 1
    if current_value >= len(nmea_data):
        current_value = 0
    if state and verbose:
        print(f"Current index in list is now {current_value}")


# Loops forever
def button_manager(pin, callback) -> None:
    global verbose
    global keep_looping
    global screen_saver_on
    global button_01_pressed_at
    global button_02_pressed_at

    btn: DigitalInOut = DigitalInOut(pin)
    # print(f"Button is a {type(btn)}")
    #
    # Warning: Button wired on the 3V3, and not on GND !!!
    #
    btn.direction = Direction.OUTPUT
    # btn.pull = Pull.UP

    prev_state: bool = btn.value  # on or off
    # print(f"Button State is a {type(prev_state)}")

    while keep_looping:
        try:
            button_down: bool = btn.value  # True means ON (aka down)
            if button_down != prev_state:  # Button status has changed
                if button_down:  # on button-down
                    if pin == pin_button_01:
                        button_01_pressed_at = int(time.time() * 1000)  # Timestamp in ms
                        if verbose_level2 or verbose:
                            print(f"Button 1 reset_pressed_at {button_01_pressed_at}")
                    elif pin == pin_button_02:
                        button_02_pressed_at = int(time.time() * 1000)  # Timestamp in ms
                        if verbose_level2 or verbose:
                            print(f"Button 2 reset_pressed_at {button_02_pressed_at}")
                if screen_saver_on:  # and button_down:  # Screen Saver on, and Button DOWN. Wake up !
                    if not button_down:  # button Up. TODO To be removed ?
                        if pin == pin_button_01:
                            button_01_pressed_at = int(time.time() * 1000)  # Timestamp in ms
                            if verbose_level2 or verbose:
                                print(f"Button 1 reset_pressed_at {button_01_pressed_at}")
                        elif pin == pin_button_02:
                            button_02_pressed_at = int(time.time() * 1000)  # Timestamp in ms
                            if verbose_level2 or verbose:
                                print(f"Button 2 reset_pressed_at {button_02_pressed_at}")
                else:
                    if not button_down:
                        if verbose or verbose_level2:
                            print("                                           >> BTN is UP")
                        callback(pin, False)  # Broadcast wherever needed

                    else:
                        if verbose or verbose_level2:
                            print("                                           >> BTN is DOWN")
                        callback(pin, True)  # Broadcast wherever needed

            if button_down and button_down != prev_state: # To avoid repeated resets on long-clicks
                if verbose_level2 or verbose:
                    print("ButtonManager: Reseting screen saver.")
                reset_screen_saver()

            prev_state = button_down
            time.sleep(0.01)  # sleep for debounce. Tricky.
        except Exception as oops:
            print(f"Error: {repr(oops)}")
        finally:
            if verbose and False:
                print("button_manager, finally.")
    print(f"Done with button listener on pin {pin}")


def screen_saver_manager() -> None:
    global keep_looping
    global screen_saver_timer
    global screen_saver_on
    while keep_looping:
        screen_saver_timer += 1
        if screen_saver_timer > ENABLE_SCREEN_SAVER_AFTER and not screen_saver_on:
            if verbose or verbose_level2:
                print(f"Turning screen saver ON, after {screen_saver_timer} seconds")
            screen_saver_on = True
        else:
            if verbose or verbose_level2:
                print(f"... ScreenSaverTime is now {screen_saver_timer}")
        time.sleep(1.0)


#
# Change these to the right size for your display!
#
WIDTH: int = 128
HEIGHT: int = 32  # Change to 64 if needed. It is also a CLI prm (See HEIGHT_PRM_PREFIX)
BORDER: int = 5
ROTATE: bool = False


WHITE: int = 255
BLACK: int = 0

sample_data: Dict[str, str] = {  # Used for VIEW, and non-implemented operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}

if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PRM_PREFIX)] == VERBOSE_PRM_PREFIX:
            verbose = (arg[len(VERBOSE_PRM_PREFIX):].lower() == "true")
        if arg[:len(VERBOSE_2_PRM_PREFIX)] == VERBOSE_2_PRM_PREFIX:
            verbose_level2 = (arg[len(VERBOSE_2_PRM_PREFIX):].lower() == "true")
        if arg[:len(ROTATE_PRM_PREFIX)] == ROTATE_PRM_PREFIX:
            ROTATE = arg[len(ROTATE_PRM_PREFIX):].lower() == "true"
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
        if arg[:len(SCREEN_SAVER_MODE_PRM_PREFIX)] == SCREEN_SAVER_MODE_PRM_PREFIX:
            try:
                ss_mode_prm: str = arg[len(SCREEN_SAVER_MODE_PRM_PREFIX):]
                if ss_mode_prm == 'off':
                    enable_screen_saver = False
            except Exception as error:
                print(f"Screen Saver Mode error: {repr(error)}")

        if arg[:len(DATA_PRM_PREFIX)] == DATA_PRM_PREFIX:
            user_list = arg[len(DATA_PRM_PREFIX):].split(',')
            nmea_data = []  # reset
            for id in user_list:
                nmea_data.append(id.strip())
            if verbose:
                print("Data list:")
                print(nmea_data)

# Summarize all options
print(f"Running process (id {server_pid}) with config:\n" +
      f"- Server Port {server_port}\n" +
      f"- verbose {verbose}\n" +
      f"- machine name {machine_name}\n" +
      f"- wiring {oled_wiring_option}\n" +
      f"- screen height {HEIGHT}\n" +
      f"- data {nmea_data} ")

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
else:
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

# Now, let's go
# Initialize buttons
print("Press button connected on GPIO-20 to scroll up")
button_thread_01: threading.Thread = threading.Thread(target=button_manager, args=(pin_button_01, button_listener))
# print(f"Thread is a {type(button_thread_01)}")
button_thread_01.daemon = True  # Dies on exit
button_thread_01.start()

print("Press button connected on GPIO-21 to scroll down")
button_thread_02: threading.Thread = threading.Thread(target=button_manager, args=(pin_button_02, button_listener))
button_thread_02.daemon = True  # Dies on exit
button_thread_02.start()

if enable_screen_saver:
    print("Starting screen saver thread")
    screen_saver_thread: threading.Thread = threading.Thread(target=screen_saver_manager)  # No args
    screen_saver_thread.daemon = True  # Dies on exit
    screen_saver_thread.start()

# Initialize OLED screen.
# Clear display.
if oled is not None:
    oled.rotate(ROTATE)
    oled.fill(BLACK)
    oled.show()

# Create blank image for drawing.
# Make sure to create image with mode '1' for 1-bit color.
image: PIL.Image.Image = Image.new("1", (oled.width, oled.height))
# print(f"Image is a {type(image)}")

# Get drawing object to draw on image.
draw: PIL.ImageDraw.ImageDraw = ImageDraw.Draw(image)
# print(f"Draw is a {type(draw)}")

# Draw a white background
draw.rectangle((0, 0, oled.width, oled.height), outline=WHITE, fill=WHITE)

# Draw a smaller inner rectangle, in black
draw.rectangle(
    (BORDER, BORDER, oled.width - BORDER - 1, oled.height - BORDER - 1),
    outline=BLACK,
    fill=BLACK,
)

# Load default font.
font: PIL.ImageFont.ImageFont = ImageFont.load_default()
# print(f"Font is a {type(font)}")

# Draw Some Text, at startup.
text: str = "Init SSD1306"
# Get IP address and network name ?
hostname: str = socket.gethostname()
IPAddr: str = get_ip_address()   # socket.gethostbyname(hostname)

# (font_width, font_height) = font.getsize(text)
left, top, right, bottom = font.getbbox(text)
(font_width, font_height) = right - left, bottom - top
draw.text(
    (oled.width // 2 - font_width // 2, (oled.height // 2 - font_height // 2) - font_height),
    text,
    font=font,
    fill=WHITE,
)
text = "IP: " + IPAddr
left, top, right, bottom = font.getbbox(text)
(font_width, font_height) = right - left, bottom - top
draw.text(
    (oled.width // 2 - font_width // 2, font_height + 1 + (oled.height // 2 - font_height // 2)),
    text,
    font=font,
    fill=WHITE,
)

# Display image
oled.image(image)
oled.show()
# Wait a bit to show the init screen
time.sleep(5)  # Optional. Just long enough to read the screen above.

# First define some constants to allow easy resizing of shapes.
padding: int = -2
top: int = padding
bottom: int = oled.height - padding
# Move left to right keeping track of the current x position for drawing shapes.
x: int = 0


def display(display_data: List[str]) -> None:
    global oled
    global font
    global image
    global draw
    global x
    global screen_saver_on
    global shutdown_suggested
    global shutdown_suggested_at
    global verbose
    global verbose_level2

    try:
        # Clear Screen. Draw a black filled box to clear the image.
        draw.rectangle((0, 0, oled.width, oled.height), outline=BLACK, fill=BLACK)  # cls

        if not screen_saver_on:
            nowms: int = int(time.time() * 1000)  # Timestamp in ms
            if verbose or verbose_level2:
                print(f">> Screen saver NOT on, shutdown_suggested:{shutdown_suggested}, suggested at:{shutdown_suggested_at}, nowms:{nowms} (diff: {(nowms - shutdown_suggested_at) if shutdown_suggested_at is not None else '-'})")
                if shutdown_suggested_at is not None and shutdown_suggested and (nowms - shutdown_suggested_at) < 5000:
                    print("   Displaying shutdown options")
                else:
                    print("   NOT Displaying shutdown options")
            if shutdown_suggested_at is not None and shutdown_suggested and (nowms - shutdown_suggested_at) < 5000:
                if verbose or verbose_level2:
                    print("Tossion! ShutdownScreen displayed !")
                options: List[str] = ["Really? Shutdown?",
                                      "",
                                      "   - Yes: button 1",
                                      "",
                                      "   - No:  button 2"]
                y: int = top
                for line in options:
                    draw.text((x, y), line, font=font, fill=WHITE)
                    y = y + 8
            else:
                if verbose or verbose_level2:
                    print("Regular data display")
                if shutdown_suggested:
                    if verbose or verbose_level2:
                        print(f"Reseting shutdown...")
                    shutdown_suggested = False    # Reset
                    shutdown_suggested_at = None
                y: int = top
                # Now draw the required text
                for line in display_data:
                    draw.text((x, y), line, font=font, fill=WHITE)
                    y = y + 8
                # draw.text((x, top), display_data, font=font, fill=WHITE)
                # draw.text((x, top + 8), str(CPU.decode('utf-8')), font=font, fill=WHITE)
                # draw.text((x, top + 16), str(MemUsage.decode('utf-8')), font=font, fill=WHITE)
                # draw.text((x, top + 24), str(Disk.decode('utf-8')), font=font, fill=WHITE)
        else:  # Screen saver management
            # Blink dots...
            if verbose:
                print(f"screen_saver_timer  {screen_saver_timer}")
            if screen_saver_timer % 4 == 1:
                if verbose:
                    print("pixel ON .")
                # Draw '.' on top left
                draw.text((x, top), ".", font=font, fill=WHITE)
            elif screen_saver_timer % 4 == 2:
                if verbose:
                    print("pixel ON ..")
                # Draw '..' on top left
                draw.text((x, top), "..", font=font, fill=WHITE)
            elif screen_saver_timer % 4 == 3:
                if verbose:
                    print("pixel ON ...")
                # Draw '...' on top left
                draw.text((x, top), "...", font=font, fill=WHITE)
        # Display image.
        oled.image(image)
        oled.show()
    except Exception as error:
        print(f"Error: {repr(error)}")


def clear() -> None:
    global oled
    global draw
    try:
        # Draw a black filled box to clear the image.
        draw.rectangle((0, 0, oled.width, oled.height), outline=BLACK, fill=BLACK)
        # Display image.
        oled.image(image)
        oled.show()
    except Exception as error:
        print(f"Error: {repr(error)}")


# Defining an HTTP request Handler class, for REST requests
class ServiceHandler(BaseHTTPRequestHandler):
    # sets basic headers for the server
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        # reads the length of the Headers
        length = int(self.headers['Content-Length'])
        # reads the contents of the request
        content = self.rfile.read(length)
        temp = str(content).strip('b\'')
        self.end_headers()
        return temp

    # To silence the HTTP logger. See README.md
    @staticmethod
    def log_message(fmt, *args):
        if verbose:
            print(fmt % args)
        return

    # GET Method Definition
    def do_GET(self):
        global verbose

        if verbose:
            print("GET methods")
        # defining all the headers
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        #
        full_path = self.path
        split = full_path.split('?')
        path = split[0]
        qs = None
        if len(split) > 1:
            qs = split[1]
        # The parameters into a map
        prm_map = {}
        if qs is not None:
            qs_prms = qs.split('&')
            for qs_prm in qs_prms:
                nv_pair = qs_prm.split('=')
                if len(nv_pair) == 2:
                    prm_map[nv_pair[0]] = nv_pair[1]
                else:
                    print("oops, no equal sign in {}".format(qs_prm))

        if path == PATH_PREFIX + "/duh":
            if verbose:
                print("SSD1306 Value request")
            try:
                dummy_data: dict = {"dummy": "empty"}
                self.wfile.write(json.dumps(dummy_data).encode())
            except Exception as exception:
                error = {"message": "{}".format(exception)}
                self.wfile.write(json.dumps(error).encode())
                self.send_response(500)
        elif path == PATH_PREFIX + "/oplist":
            response = {
                "oplist": [{
                    "path": PATH_PREFIX + "/oplist",
                    "verb": "GET",
                    "description": "Get the available operation list."
                }, {
                    "path": PATH_PREFIX + "/duh",
                    "verb": "GET",
                    "description": "Placeholder, in json format."
                }, {
                    "path": PATH_PREFIX + "/nmea-data",
                    "verb": "PUT",
                    "description": "Client put s data in the cache, in JSON format."
                }, {
                    "path": PATH_PREFIX + "/clear-screen",
                    "verb": "PUT",
                    "description": "Clean the screen."
                }, {
                    "path": PATH_PREFIX + "/bye-and-clear-screen",
                    "verb": "PUT",
                    "description": "Says bye, then clean the screen."
                }, {
                    "path": PATH_PREFIX + "/verbose/{value}",
                    "verb": "PUT",
                    "description": "Set the verbose value, true or false."
                }, {
                    "path": PATH_PREFIX + "/verbose-2/{value}",
                    "verb": "PUT",
                    "description": "Set the verbose-level-2 value, true or false."
                }, {
                    "path": PATH_PREFIX + "/exit",
                    "verb": "POST",
                    "description": "Careful: terminate the server process."
                }, {
                    "path": PATH_PREFIX + "/whatever",
                    "verb": "POST",
                    "description": "Placeholder."
                }]
            }
            self.wfile.write(json.dumps(response).encode())  # takes care of response code and stuff...
        else:
            if verbose:
                print("GET on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(400)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # VIEW method definition. Uncommon...
    def do_VIEW(self):
        # dict var. for pretty print
        display = {}
        temp = self._set_headers()
        # check if the key is present in the sample_data dictionary
        if temp in sample_data:
            display[temp] = sample_data[temp]
            # print the keys required from the json file
            self.wfile.write(json.dumps(display).encode())
        else:
            error = "{} Not found in sample_data\n".format(temp)
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # POST method definition
    def do_POST(self):
        global verbose

        if verbose:
            print("POST request, {}".format(self.path))
        if self.path.startswith(PATH_PREFIX + "/whatever/"):  # Dummy POST
            content_len: int = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            if verbose:
                print("Content: {}".format(post_body))
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
        elif self.path.startswith(PATH_PREFIX + "/exit"):
            if verbose:
                print("Exiting!...")
            # content_len: int = int(self.headers.get('Content-Length'))
            # post_body = self.rfile.read(content_len).decode('utf-8')
            # if verbose:
            #    print("Content: {}".format(post_body))
            self.send_response(201)
            response = {"status": "OK"}
            # self.wfile.write(json.dumps(response).encode())
            try:
                print("Killing {}...".format(server_pid))
                os.kill(server_pid, signal.SIGINT)
                # server.shutdown()
                # server.server_close()
            except Exception as oops:
                print (f"Error: {repr(oops)}")
                response = {(f"Error: {repr(oops)}")}
            self.wfile.write(json.dumps(response).encode())
        else:
            if verbose:
                print("POST on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # self.wfile.write(json.dumps(data[str(index)]).encode())

    # PUT method Definition
    def do_PUT(self):
        global nmea_cache
        global verbose
        global verbose_level2

        if verbose:
            print("PUT request, {}".format(self.path))
        if self.path == PATH_PREFIX + "/nmea-data":  # Receive the full NMEA Cache, as a JSON Object
            content_len: int = int(self.headers.get('Content-Length'))
            body: str = self.rfile.read(content_len).decode('utf-8')
            if verbose:
                print(f"{PATH_PREFIX}/nmea-data Received Content: {body}")
            try:
                # Parse it, load it in nmea-cache.
                try:
                    nmea_cache = json.loads(body)
                    if verbose:
                        print(">>> Cache parsed OK")
                except Exception as whatzat:
                    print(f">>> Parsing cache -> Oops: {repr(whatzat)}")
                # display("Cache was received")  # Temp
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = {"status": "OK"}
                self.wfile.write(json.dumps(response).encode())
            except Exception as error:
                error: str = f"Exception {repr(error)}\n"
                self.send_response(404)
                self.send_header('Content-Type', 'text/plain')
                content_len = len(error)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(bytes(error, 'utf-8'))
        elif self.path == PATH_PREFIX + "/clear-screen":
            try:
                clear()
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = {"status": "OK"}
                self.wfile.write(json.dumps(response).encode())
            except Exception as error:
                error: str = f"Exception {repr(error)}\n"
                self.send_response(404)
                self.send_header('Content-Type', 'text/plain')
                content_len = len(error)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(bytes(error, 'utf-8'))
        elif self.path == PATH_PREFIX + "/bye-and-clear-screen":
            try:
                global keep_looping
                keep_looping = False
                # Clear screen. Say Bye for 1.5 second before clearing the screen.
                time.sleep(1.5)
                clear()
                # Draw a white background
                draw.rectangle((0, 0, oled.width, oled.height), outline=WHITE, fill=WHITE)

                # Draw a smaller inner rectangle, in black
                draw.rectangle(
                    (BORDER, BORDER, oled.width - BORDER - 1, oled.height - BORDER - 1),
                    outline=BLACK,
                    fill=BLACK,
                )
                text: str = "Bye SSD1306"
                # (font_width, font_height) = font.getsize(text)
                left, top, right, bottom = font.getbbox(text)
                (font_width, font_height) = right - left, bottom - top
                draw.text(
                    (oled.width // 2 - font_width // 2, oled.height // 2 - font_height // 2),
                    text,
                    font=font,
                    fill=WHITE,
                )
                # Display image
                oled.image(image)
                oled.show()
                time.sleep(2)  # Give time to read the screen.
                clear()
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = {"status": "OK"}
                self.wfile.write(json.dumps(response).encode())
            except Exception as error:
                error: str = f"Exception {repr(error)}\n"
                self.send_response(404)
                self.send_header('Content-Type', 'text/plain')
                content_len = len(error)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(bytes(error, 'utf-8'))
        elif self.path.startswith(PATH_PREFIX + "/verbose/"):
            try:
                item_list = self.path.split('/')
                verboseValue: str = item_list[len(item_list) - 1]
                verbose = verboseValue == 'true'
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = {"status": "OK"}
                self.wfile.write(json.dumps(response).encode())
            except Exception as error:
                error: str = f"Exception {repr(error)}\n"
                self.send_response(404)
                self.send_header('Content-Type', 'text/plain')
                content_len = len(error)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(bytes(error, 'utf-8'))
        elif self.path.startswith(PATH_PREFIX + "/verbose-2/"):
                try:
                    item_list = self.path.split('/')
                    verboseValue: str = item_list[len(item_list) - 1]
                    verbose_level2 = verboseValue == 'true'
                    self.send_response(201)
                    self.send_header('Content-Type', 'application/json')
                    self.end_headers()
                    response = {"status": "OK"}
                    self.wfile.write(json.dumps(response).encode())
                except Exception as error:
                    error: str = f"Exception {repr(error)}\n"
                    self.send_response(404)
                    self.send_header('Content-Type', 'text/plain')
                    content_len = len(error)
                    self.send_header('Content-Length', str(content_len))
                    self.end_headers()
                    self.wfile.write(bytes(error, 'utf-8'))
        else:
            if verbose:
                print("PUT on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # DELETE method definition
    def do_DELETE(self):
        if verbose:
            print("DELETE on {} not managed".format(self.path))
        error = "NOT FOUND!"
        self.send_response(400)
        self.send_header('Content-Type', 'text/plain')
        content_len = len(error)
        self.send_header('Content-Length', str(content_len))
        self.end_headers()
        self.wfile.write(bytes(error, 'utf-8'))


#
# That one is THE display manager.
# Whatever is displayed, the way it is displayed,
# this is done here. It takes the values from the nmea_cache.
#
def format_data(id: str) -> List[str]:
    global nmea_cache

    formatted: List[str] = None  # Init

    try:
        if id == "BSP":
            bsp = nmea_cache[id]["speed"]
            formatted = ["BSP", f"{bsp} kts"]
        elif id == "SOG":
            try:
                sog = nmea_cache["SOG"]["speed"]
            except TypeError as te:
                sog = "-"
                pass
            formatted = ["SOG", f"{sog} kts"]
        elif id == "COG":
            try:
                cog = nmea_cache[id]["angle"]
            except (TypeError, KeyError) as te:
                cog = "-"
                pass
            formatted = ["COG", f"{cog}°"]
        elif id == "POS":
            position: Dict = nmea_cache["Position"]
            latitude: float = position["lat"]
            longitude: float = position["lng"]
            grid: str = position["gridSquare"]
            formatted = [id, utils.dec_to_sex(latitude, "NS"), utils.dec_to_sex(longitude, "EW"), grid]
        elif id == "NAV":
            # Warning: 5 lines, too many lines for a 128x32
            position: Dict = nmea_cache["Position"]
            latitude: float = position["lat"]
            longitude: float = position["lng"]
            grid: str = position["gridSquare"]
            try:
                sog = nmea_cache["SOG"]["speed"]
            except (TypeError, KeyError) as te:
                sog = "-"
                pass
            try:
                cog = nmea_cache["COG"]["angle"]
            except (TypeError, KeyError) as te:
                cog = "-"
                pass
            try:
                fmt_date: Dict = nmea_cache["GPS Date & Time"]["fmtDate"]
                str_date: str = f"{fmt_date['year']}-{fmt_date['month']:02d}-{fmt_date['day']:02d} {fmt_date['hour']:02d}:{fmt_date['min']:02d}:{fmt_date['sec']:02d}"
            except (TypeError, KeyError) as te:
                str_date = "-"
                pass

            formatted = [
                f"POS: {utils.dec_to_sex(latitude, 'NS')}",
                f"     {utils.dec_to_sex(longitude, 'EW')}",
                f"     {grid}",
                f"COG: {cog}°",
                f"SOG: {sog} kts",
                "Date UTC:",
                f"{str_date}" ]
        elif id == "ATP":
            atp: float = nmea_cache["Air Temperature"]["value"]
            formatted = [ "AIR", f"{atp:.01f}°C" ]
        elif id == "PRM":
            prmsl: float = nmea_cache["Barometric Pressure"]["value"]
            formatted = [ "PRMSL", f"{prmsl:.01f} hPa" ]
        elif id == "HUM":
            hum: float = nmea_cache["Relative Humidity"]
            formatted = [ id, f"{hum:.01f}%" ]
        elif id == "ATM":  # ATP, PRS, HUM
            atp: float = nmea_cache["Air Temperature"]["value"]
            prmsl: float = nmea_cache["Barometric Pressure"]["value"]
            hum: float = nmea_cache["Relative Humidity"]
            dew: float = None
            try:
                dew = nmea_cache["dewpoint"]
            except Exception as oops:
                pass
            formatted = [
                f"PRMSL: {prmsl:.01f} hPa",
                f"AIR  : {atp:.01f}°C",
                f"HUM  : {hum:.01f} %",
                f"DEW  : {dew:.01f}°C"
            ]
        elif id == "NET":  # Network info. Not from the cache.
            host: str = socket.gethostname()
            ip_addr: str = get_ip_address()   # socket.gethostbyname(hostname)
            network_name: str = get_network_name()
            # nmcli dev wifi show-password | grep SSID
            formatted = [
                "Hostname:", f"  {host}",
                "IP:", f"  {ip_addr}",
                "Network:", f"  {network_name}"
            ]
        else:
            formatted = [id, "Not implemented"]
    except TypeError as te:
        formatted = [id, "Not in Cache (yet)"]
    except Exception as oops:
        print(f"{id}:{repr(oops)}")
        formatted = [f"{id}:{repr(oops)}"]
    return formatted


# Manage what goes on, on the display
def display_manager() -> None:
    global current_value
    global keep_looping
    while keep_looping:
        to_display: List[str] = format_data(nmea_data[current_value])  # Format the data to display
        display(to_display)
        # display([f"{current_value} -> {nmea_data[current_value]}"])
        time.sleep(0.5)   # 1.0)  # Time between refresh
    print("Done with display thread")


# Initialize the display thread
display_thread: threading.Thread = threading.Thread(target=display_manager, args=())
display_thread.daemon = True  # Dies on exit
display_thread.start()

# Server Initialization
port_number: int = server_port
print("Starting SSD1306 server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
# server = HTTPServer(('', port_number), ServiceHandler)  # localhost, not IP address
#
# For dev. Requires import sample_cache
# nmea_cache = json.loads(sample_cache.sample_json)

print("Server ready for duty.")
print("Try curl -X GET http://{}:{}{}/oplist".format(machine_name,
                                                     port_number,
                                                     PATH_PREFIX))
print("or  curl -v -X VIEW http://{}:{}{} -H \"Content-Length: 1\" -d \"1\"".format(machine_name,
                                                                                    port_number,
                                                                                    PATH_PREFIX))
#
# Main part.
# Run the server, until Ctrl-C is hit.
#
try:
    server.serve_forever()
except KeyboardInterrupt:
    print("\n\t\tUser interrupted (server.serve), exiting.")
    keep_looping = False
    button_thread_01.join()
    button_thread_02.join()
    display_thread.join()
    if enable_screen_saver:
        screen_saver_thread.join()

print("Out of the server loop")

# After all, cleanup.
if oled is not None:
    clear()
    text: str = "Bye-bye..."
    # (font_width, font_height) = font.getsize(text)
    left, top, right, bottom = font.getbbox(text)
    (font_width, font_height) = right - left, bottom - top
    draw.text(
        (oled.width // 2 - font_width // 2, oled.height // 2 - font_height // 2),
        text,
        font=font,
        fill=WHITE,
    )
    # Display image
    oled.image(image)
    oled.show()
    time.sleep(2)
    clear()
print("Done with REST SSD1306 server.")