#!/usr/bin/env python3
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
#
# sudo pip3 install adafruit-circuitpython-epd
#
# That one receives the full cache (as JSON) and manages the display of the data by itself.
# It can also deal with 2 push-buttons (on the eInk2-13) for user's interaction, to choose the data to be displayed. (scroll up & down)
#
# Provides a ScreenSaving mode, see ENABLE_SCREEN_SAVER_AFTER and enable_screen_saver_after variables.
# -> Screen Saver displays a pelican (default) or a static text.
#
# CLI prms: Look for "# CLI prms" in the code below.
#
# Work In Progress !
# Do run a curl -X GET /eink2-13/oplist !
#
# See doc at <https://learn.adafruit.com/2-13-in-e-ink-bonnet?view=all>
#
# Code samples and doc: <https://learn.adafruit.com/2-13-in-e-ink-bonnet/usage>  
# EPD: <https://docs.circuitpython.org/projects/epd/en/latest/>
#      <https://docs.circuitpython.org/projects/epd/en/latest/api.html>
#
import json
import sys
import os
import signal
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Dict
from typing import List
import threading
import board
import busio
import digitalio
# from digitalio import DigitalInOut, Direction, Pull
import PIL
from PIL import Image, ImageDraw, ImageFont
from adafruit_epd.ssd1675 import Adafruit_SSD1675  # pylint: disable=unused-import
import time
import utils  # local script

# First define some color constants
WHITE = (0xFF, 0xFF, 0xFF)
BLACK = (0x00, 0x00, 0x00)
RED = (0xFF, 0x00, 0x00)

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

BORDER = 20
FONTSIZE = 16
BACKGROUND_COLOR = BLACK
FOREGROUND_COLOR = WHITE
TEXT_COLOR = BLACK

MONTHS = [
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
]

# create the spi device and pins we will need
spi = busio.SPI(board.SCK, MOSI=board.MOSI, MISO=board.MISO)
ecs = digitalio.DigitalInOut(board.CE0)
dc = digitalio.DigitalInOut(board.D22)
srcs = None
rst = digitalio.DigitalInOut(board.D27)
busy = digitalio.DigitalInOut(board.D17)

# REST prms
PATH_PREFIX = "/eink2-13"
server_port: int = 8080
verbose: bool = False
machine_name: str = "127.0.0.1"  # aka localhost

# CLI prms
MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"
HEIGHT_PREFIX: str = "--height:"
SCREEN_SAVER_MODE_PREFIX: str = "--screen-saver:"  # "on", or "off". Default "on"
SCREEN_SAVER_OPTION_PREFIX: str = "--screen-saver-option:"  # default "pelican", also available "sleep"
SCREEN_SAVER_AFTER_PREFIX: str = "--screen-saver-after:"  # default 30s

DATA_PREFIX: str = "--data:"  # Like "BSP,SOG,POS,..., etc"

# Supported data (see format_data method):
# BSP, POS, SOG, COG, NAV, ATM, ATP, PRS, HUM, WPT
# TODO: More data, and graphics (displays...)

eink = None
server_pid: int = os.getpid()

current_value: int = 0
keep_looping: bool = True
nmea_cache: Dict[str, object] = None

ENABLE_SCREEN_SAVER_AFTER: int = 30  # in seconds
enable_screen_saver_after: int = ENABLE_SCREEN_SAVER_AFTER  # default
screen_saver_timer: int = 0
screen_saver_on: bool = False
enable_screen_saver: bool = True
screen_saver_option: str = "pelican"  # Option are "pelican", "sleep"

# Default list
nmea_data: List[str] = [
    "BSP",  # Boat Speed
    "SOG",  # Speed Over Ground
    "COG",  # Course Over Ground
    "POS"   # Position
]

# The buttons
button_01 = digitalio.DigitalInOut(board.D6)  # D6 - Top
button_01.switch_to_input()
button_02 = digitalio.DigitalInOut(board.D5)  # D5 - Bottom
button_02.switch_to_input()

def reset_screen_saver() -> None:
    global screen_saver_on
    global screen_saver_timer
    if verbose:
        print("Reseting screen saver")
    screen_saver_on = False
    screen_saver_timer = 0
    

def button_listener(button, state) -> None:
    global current_value
    global nmea_data
    global button_01
    global button_02
    if verbose:
        print(f"Yo! Button { '1' if button == button_01 else '2' }, state {state}")
    if button == button_01 and state == True:  # Only on button down
        current_value += 1
    if button == button_02 and state == True:  # Only on button down
        current_value -= 1
    if current_value < 0:
        current_value = len(nmea_data) - 1
    if current_value >= len(nmea_data):
        current_value = 0
    # if state and verbose:
    if verbose:
        print(f"(button_listener) Current index in list is now {current_value}")


def button_manager(button, callback) -> None:
    global keep_looping
    global screen_saver_on
    global button_01
    global button_02

    listener_name: str = 'Listener 1' if button == button_01 else 'Listener 2' 

    prev_state: bool = False
    # print(f"Button State is a {type(prev_state)}")
    while keep_looping:
        try:
            cur_state: bool = button.value
            if cur_state != prev_state:  # Button status has changed
                if cur_state:  # Button down
                    # print(f"Button down ({listener_name})")
                    if not screen_saver_on:
                        if verbose:
                            print(f">> NOT ScreenSaver case ({listener_name})")
                            print("BTN is DOWN")
                        callback(button, True)  # Broadcast wherever needed
                    else:
                        # print(">> ScreenSaver case")
                        pass
                    reset_screen_saver()  # Reset when button was clicked, anyway.
                else: 
                    if verbose:
                        print(f"Button up (pass) ({listener_name})")
                    pass   # Nothing when button released
            prev_state = cur_state
            time.sleep(0.1)  # sleep for debounce
        except Exception as oops:
            print(f"Error: {repr(oops)}")
        finally:
            if verbose and False:
                print("button_manager, finally.")
    print(f"Done with listener on button { '1' if button == button_01 else '2' }")


# There is a blank screen saver option... ("sleep")
def screen_saver_manager() -> None:
    global keep_looping
    global screen_saver_timer
    global screen_saver_on
    global screen_saver_option
    while keep_looping:
        screen_saver_timer += 1
        if verbose:
            print(f"screen_saver_manager >> Increasing screen_saver_timer to {screen_saver_timer}")
        if screen_saver_timer >= enable_screen_saver_after:   # and not screen_saver_on:
            if verbose:
                print(f"Turning screen saver ON, option {screen_saver_option}, screen_save_timer:{screen_saver_timer} / {enable_screen_saver_after} ")
            screen_saver_on = True
            if screen_saver_timer % enable_screen_saver_after == 0:
                if screen_saver_option == "sleep":
                    if screen_saver_timer <= enable_screen_saver_after:  # Just once
                        display_sleep_message()
                else:
                    # print(f"Pelican Screen Saver, {screen_saver_timer}")
                    display_image(screen_saver_timer)  # display the pelican as screen saver
        time.sleep(1.0)


#
# Change these to the right size for your display!
#
WIDTH: int = 250
HEIGHT: int = 122
BORDER: int = 5

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
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")
        if arg[:len(SCREEN_SAVER_MODE_PREFIX)] == SCREEN_SAVER_MODE_PREFIX:
            try:
                ss_mode_prm: str = arg[len(SCREEN_SAVER_MODE_PREFIX):]
                if ss_mode_prm == 'off':
                    enable_screen_saver = False
            except Exception as error:
                print(f"Screen Saver Mode error: {repr(error)}")
        if arg[:len(SCREEN_SAVER_OPTION_PREFIX)] == SCREEN_SAVER_OPTION_PREFIX:
            try:
                screen_saver_option = arg[len(SCREEN_SAVER_OPTION_PREFIX):]
                print(f"Screen Saver Option now {screen_saver_option}")
            except Exception as error:
                print(f"Screen Saver Option error: {repr(error)}")

        if arg[:len(SCREEN_SAVER_AFTER_PREFIX)] == SCREEN_SAVER_AFTER_PREFIX:
            try:
                screen_saver_after_str = arg[len(SCREEN_SAVER_AFTER_PREFIX):]
                print(f"Screen Saver After is now {screen_saver_after_str}")
                enable_screen_saver_after = int(screen_saver_after_str)
            except Exception as error:
                print(f"Screen Saver After error: {repr(error)}")

        if arg[:len(DATA_PREFIX)] == DATA_PREFIX:
            user_list = arg[len(DATA_PREFIX):].split(',')
            nmea_data = []  # reset
            for id in user_list:
                nmea_data.append(id.strip())
            if verbose:
                print("Data list:")
                print(nmea_data)

# initialize the eink screen
eink = Adafruit_SSD1675(122, 250,        # 2.13" HD mono display
                        spi,
                        cs_pin=ecs,
                        dc_pin=dc,
                        sramcs_pin=srcs,
                        rst_pin=rst,
                        busy_pin=busy)
eink.rotation = 1
image = Image.new("RGB", (eink.width, eink.height))

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)

# Load a TTF Font
font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", FONTSIZE)
bold_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", FONTSIZE)

FONT = bold_font

# Now, let's go
# Initialize buttons
print("Press button 01 to scroll up")
button_thread_01: threading.Thread = threading.Thread(target=button_manager, args=(button_01, button_listener))
# print(f"Thread is a {type(button_thread_01)}")
button_thread_01.daemon = True  # Dies on exit
button_thread_01.start()

print("Press button 02 to scroll down")
button_thread_02: threading.Thread = threading.Thread(target=button_manager, args=(button_02, button_listener))
button_thread_02.daemon = True  # Dies on exit
button_thread_02.start()

if enable_screen_saver:
    print("Starting screen saver thread")
    screen_saver_thread: threading.Thread = threading.Thread(target=screen_saver_manager)  # No args
    screen_saver_thread.daemon = True  # Dies on exit
    screen_saver_thread.start()

# Create blank image for drawing.
# Make sure to create image with mode '1' for 1-bit color.
image = Image.new("RGB", (eink.width, eink.height))
# print(f"Image is a {type(image)}")

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)
# print(f"Draw is a {type(draw)}")

# Clear display.
if eink is not None:
    draw.rectangle((0, 0, eink.width, eink.height), fill=FOREGROUND_COLOR)
    eink.image(image)
    eink.display()

# Draw a white background
# draw.rectangle((0, 0, eink.width, eink.height), fill=WHITE)

# Draw a smaller inner rectangle, in black
# draw.rectangle(
#     (BORDER, BORDER, eink.width - BORDER - 1, eink.height - BORDER - 1),
#     fill=BLACK,
# )

# Draw Some Text, at startup.
text: str = "Init eInk2.13"   # TODO Add an image ;)
(font_width, font_height) = FONT.getsize(text)
draw.text(
    (eink.width // 2 - font_width // 2, eink.height // 2 - font_height // 2),
    text,
    font=FONT,
    fill=TEXT_COLOR,
)

# Display image
eink.image(image)
eink.display()
# Wait a bit to show the init screen
time.sleep(1)  # Optional

# First define some constants to allow easy resizing of shapes.
padding: int = -2
top: int = padding
bottom: int = eink.height - padding
# Move left to right keeping track of the current x position for drawing shapes.
x: int = 0


def display(display_data: List[str]) -> None:
    global eink
    global FONT
    global image
    global draw
    global x
    global screen_saver_on

    display_dots = False

    try:
        # Clear Screen.
        # draw.rectangle((0, 0, eink.width, eink.height), fill=FOREGROUND_COLOR)  # Moved below (if screen saver is off)

        if not screen_saver_on:
            # Clear Screen.
            draw.rectangle((0, 0, eink.width, eink.height), fill=FOREGROUND_COLOR)  # TODO a clear() instead ?
            y: int = top
            # Now draw the required text
            for line in display_data:
                draw.text((x, y), line, font=FONT, fill=TEXT_COLOR)
                y = y + FONTSIZE

            # draw.text((x, top), display_data, font=font, fill=WHITE)
            # draw.text((x, top + 8), str(CPU.decode('utf-8')), font=font, fill=WHITE)
            # draw.text((x, top + 16), str(MemUsage.decode('utf-8')), font=font, fill=WHITE)
            # draw.text((x, top + 24), str(Disk.decode('utf-8')), font=font, fill=WHITE)
        else:
            # Blink dots... Removed (see display_dots). This is not displayed if screen saver is on
            if verbose:
                print(f"screen_saver_timer  {screen_saver_timer}")   # For the log
            if display_dots:
                if screen_saver_timer % 4 == 1:
                    if verbose:
                        print("pixel ON .")
                    # Draw '.' on top left
                    draw.text((x, top), ".", font=FONT, fill=TEXT_COLOR)
                elif screen_saver_timer % 4 == 2:
                    if verbose:
                        print("pixel ON ..")
                    # Draw '..' on top left
                    draw.text((x, top), "..", font=FONT, fill=TEXT_COLOR)
                elif screen_saver_timer % 4 == 3:
                    if verbose:
                        print("pixel ON ...")
                    # Draw '...' on top left
                    draw.text((x, top), "...", font=FONT, fill=TEXT_COLOR)
        if not screen_saver_on:  # display if screen saver is on
            # Display image.
            eink.image(image)
            eink.display()
    except Exception as error:
        print(f"Error: {repr(error)}")


def clear() -> None:
    global eink
    global draw
    global image
    global draw
    try:
        # Draw a black filled box to clear the image.
        draw.rectangle((0, 0, eink.width, eink.height), fill=FOREGROUND_COLOR)
        # Display image.
        eink.image(image)
        eink.display()
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

    # To silence the HTTP logger
    @staticmethod
    def log_message(fmt, *args):
        if verbose:
            print(fmt % args)
        return

    # GET Method Definition
    def do_GET(self):
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
                print("EINK2-13 Value request")
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
                    "description": "Receive the cache, in JSON format."
                }, {
                    "path": PATH_PREFIX + "/clear-screen",
                    "verb": "PUT",
                    "description": "Clean the screen."
                }, {
                    "path": PATH_PREFIX + "/bye-and-clear-screen",
                    "verb": "PUT",
                    "description": "Says bye, then clean the screen."
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
            response_content = json.dumps(response).encode()
            self.send_response(200)
            # defining the response headers
            self.send_header('Content-Type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(response_content)
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
        if verbose:
            print("POST request, {}".format(self.path))
        if self.path.startswith("/whatever/"):  # Dummy POST
            content_len: int = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            if verbose:
                print("Content: {}".format(post_body))
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
        elif self.path.startswith("/exit"):
            content_len: int = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            if verbose:
                print("Content: {}".format(post_body))
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
            os.kill(server_pid, signal.SIGINT)
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
        if verbose:
            print("PUT request, {}".format(self.path))
        if self.path == PATH_PREFIX + "/nmea-data":  # Receive the full NMEA Cache, as a JSON Object
            content_len: int = int(self.headers.get('Content-Length'))
            body: str = self.rfile.read(content_len).decode('utf-8')
            if verbose:
                print("Content: {}".format(body))
            try:
                # Parse it
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
                time.sleep(1.5)
                # Clear screen. Say Bye for 1 second before clearing the screen.
                clear()
                text: str = "Bye EINK2-13"
                (font_width, font_height) = FONT.getsize(text)
                draw.text(
                    (eink.width // 2 - font_width // 2, eink.height // 2 - font_height // 2),
                    text,
                    font=FONT,
                    fill=WHITE,
                )
                # Display image
                eink.image(image)
                eink.display()
                time.sleep(1)  # Give time to read the screen.
                clear()
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = {"status": "OK"}
                self.wfile.write(json.dumps(response).encode())
                # Kill the server
                print(">> Now killing the server")
                os.kill(server_pid, signal.SIGINT)
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
            sog = nmea_cache[id]["speed"]
            formatted = ["SOG", f"{sog} kts"]
        elif id == "COG":
            cog = 0
            try:
                cog = nmea_cache["COG"]["angle"]
            except Exception as oops:
                pass
            formatted = ["COG", f"{cog}°"]
        elif id == "POS":
            position: Dict = nmea_cache["Position"]
            latitude: float = position["lat"]
            longitude: float = position["lng"]
            grid: str = position["gridSquare"]
            formatted = [id, utils.dec_to_sex(latitude, "NS"), utils.dec_to_sex(longitude, "EW"), grid]
        elif id == "NAV":
            # Warning: 6 lines (or more)
            position: Dict = nmea_cache["Position"]
            latitude: float = position["lat"]
            longitude: float = position["lng"]
            grid: str = position["gridSquare"]
            sog = nmea_cache["SOG"]["speed"]
            cog = 0
            gps: str = "-"
            try:
                gps = f"{nmea_cache['GPS Time']['fmtDate']['year']}-" + \
                      f"{MONTHS[nmea_cache['GPS Time']['fmtDate']['month'] - 1]}-" + \
                      f"{nmea_cache['GPS Time']['fmtDate']['day']:02} " + \
                      f"{nmea_cache['GPS Time']['fmtDate']['hour']:02}:" + \
                      f"{nmea_cache['GPS Time']['fmtDate']['min']:02}:" + \
                      f"{nmea_cache['GPS Time']['fmtDate']['sec']:02}"
            except Exception as oops:
                pass
            try:
                cog = nmea_cache["COG"]["angle"]
            except Exception as oops:
                pass
            formatted = [
                "NAV",
                f"LAT: {utils.dec_to_sex(latitude, 'NS')}",
                f"LNG: {utils.dec_to_sex(longitude, 'EW')}",
                f"GRID: {grid}",
                f"COG: {cog}°",
                f"SOG: {sog} kts",
                f"UTC: {gps}"]
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
        elif id == "WPT": # Waypoint direction and distance
            # pass
            wpid: string = nmea_cache["To Waypoint"]
            b2wp: float = nmea_cache["Bearing to WP"]["value"]
            d2wp: float = nmea_cache["Distance to WP"]["value"]
            formatted = [
                f"To waypoint {wpid}",
                f"{d2wp:.02f} nm",
                f"{b2wp:.01f}°"
            ]
        else:
            formatted = [id, "Not implemented"]
    except TypeError as te:
        formatted = [id, "Not in Cache (yet)"]
    except Exception as oops:
        print(f"{id}:{repr(oops)}")
        formatted = [f"{id}:{repr(oops)}"]
    return formatted

def display_image(offset: int) -> None:
    global eink

    clear()
    # An image on Bye screen...
    pelican = Image.open("pelican.bw.png")
    # Scale the image to the smaller screen dimension

    h_ratio: float = eink.height / pelican.height
    w_ratio: float = eink.width / pelican.width
    scale_ratio: float = min(h_ratio, w_ratio)
    scaled_width: int = round((pelican.width * 0.9) * scale_ratio)
    scaled_height: int = round((pelican.height * 0.9) * scale_ratio)
    pelican = pelican.resize((scaled_width, scaled_height), Image.BICUBIC)
    # Crop and h-center, w-center the image
    x = (offset % eink.width) - scaled_width // 2 - eink.width // 2
    # x = scaled_width - eink.width // 2
    y = scaled_height // 2 - eink.height // 2
    pelican = pelican.crop((x, y, x + eink.width, y + eink.height))
    # adding dithering for monochrome displays
    pelican = pelican.convert("1").convert("L")
    eink.image(pelican)
    if verbose:
        print(f">> Displaying pelican, offset {offset}, x {x}, eWidth {eink.width}, pWidth {scaled_width}")
    eink.display()

def display_sleep_message() -> None:
    #
    # Two line text.
    #
    global eink
    global FONT
    global draw
    global image

    text_1: str = "Server running,"
    text_2: str = "Screen sleeping..."
    text_3: str = "Hit a button to resume"
    clear()
    if verbose:
        print("Displaying sleep message.")

    (font_width, font_height) = FONT.getsize(text_1)
    draw.text(
        (eink.width // 2 - font_width // 2, eink.height // 2 - font_height),  # (x, y)
        text_1,
        font=FONT,
        fill=TEXT_COLOR,
    )
    (font_width, font_height) = FONT.getsize(text_2)
    draw.text(
        (eink.width // 2 - font_width // 2, eink.height // 2),
        text_2,
        font=FONT,
        fill=TEXT_COLOR,
    )
    (font_width, font_height) = FONT.getsize(text_3)
    draw.text(
        (eink.width // 2 - font_width // 2, eink.height // 2 + font_height),  # (x, y)
        text_3,
        font=FONT,
        fill=TEXT_COLOR,
    )
    eink.image(image)
    eink.display()

# Manage what goes on, on the display
def display_manager() -> None:
    global current_value
    global keep_looping
    while keep_looping:
        to_display: List[str] = format_data(nmea_data[current_value])  # Format the data to display
        display(to_display)
        # display([f"{current_value} -> {nmea_data[current_value]}"])
        time.sleep(1.0)
    print("Done with display thread")


# Initialize the display thread
display_thread: threading.Thread = threading.Thread(target=display_manager, args=())
display_thread.daemon = True  # Dies on exit
display_thread.start()

# Server Initialization
port_number: int = server_port
print("Starting EINK2-13 server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
# For dev. Requires import sample_cache
# nmea_cache = json.loads(sample_cache.sample_json)

print(f"Server ({type(server)})ready for duty.")
print("Try curl -X GET http://{}:{}{}/oplist".format(machine_name, port_number, PATH_PREFIX))
print("or  curl -v -X VIEW http://{}:{}{} -H \"Content-Length: 1\" -d \"1\"".format(machine_name, port_number,
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

# After all
if eink is not None:
    clear()
    text: str = "Bye-bye eInk 2.13 server!.."
    (font_width, font_height) = FONT.getsize(text)
    draw.text(
        (eink.width // 2 - font_width // 2, eink.height // 2 - font_height // 2),
        text,
        font=FONT,
        fill=BLACK,
    )
    # Display image
    eink.image(image)
    eink.display()
    time.sleep(2)
    clear()
print("Done with REST EINK2.13 server.")