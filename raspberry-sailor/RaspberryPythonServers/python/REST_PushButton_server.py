#!/usr/bin/env python3
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
#
# It can also deal with 2 push-buttons for user's interaction,
# to choose the data to be displayed - or whatever. (scroll up & down)
#
# -> Warning: the buttons are wired on 3V3, not GND !!! See the Fritzing diagrams about that (in
#      ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/doc_resources/ and in
#      ROB/Java-TCP-Python/resources)
#
#
# Work In Progress !
# Do run a curl -X GET /pushbutton/oplist !
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
import digitalio
from digitalio import DigitalInOut, Direction, Pull
import time
import utils  # local script

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

PATH_PREFIX = "/pushbutton"
server_port: int = 8080
verbose: bool = False
machine_name: str = "127.0.0.1"  # aka localhost

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

server_pid: int = os.getpid()

current_value: int = 0
keep_looping: bool = True
nmea_cache: Dict[str, object] = None

# Default list
nmea_data: List[str] = [
    "BSP",  # Boat Speed
    "SOG",  # Speed Over Ground
    "COG",  # Course Over Ground
    "POS",  # Position
    "WPT"   # Waypoint, distance and bearing
]

pin_button_01 = board.D20  # pin #38
pin_button_02 = board.D21  # pin #40

def button_listener(pin, state) -> None:
    global current_value
    global nmea_data
    global pin_button_01
    global pin_button_02
    if verbose:
        print(f"Yo! {pin}, state {state}")
    if pin == pin_button_01 and state == True:
        current_value += 1
    if pin == pin_button_02 and state == True:
        current_value -= 1
    if current_value < 0:
        current_value = len(nmea_data) - 1
    if current_value >= len(nmea_data):
        current_value = 0
    if state and verbose:
        print(f"Current index in list is now {current_value}")


def button_manager(pin, callback) -> None:
    global keep_looping
    global screen_saver_on
    btn: DigitalInOut = DigitalInOut(pin)
    # print(f"Button is a {type(btn)}")
    #
    # Warning: Button wired on the 3V3, and not on GND !!!
    #
    btn.direction = Direction.OUTPUT
    # btn.pull = Pull.UP

    prev_state: bool = btn.value
    # print(f"Button State is a {type(prev_state)}")
    while keep_looping:
        try:
            cur_state: bool = btn.value
            if cur_state != prev_state:  # Button status has changed
                if not cur_state:
                    if verbose:
                        print("BTN is UP")
                    callback(pin, False)  # Broadcast wherever needed
                else:
                    if verbose:
                        print("BTN is DOWN")
                    callback(pin, True)  # Broadcast wherever needed
            prev_state = cur_state
            time.sleep(0.1)  # sleep for debounce
        except Exception as oops:
            print(f"Error: {repr(oops)}")
        finally:
            if verbose and False:
                print("button_manager, finally.")
    print(f"Done with button listener on pin {pin}")


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
                print("DUH Value request")
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
        else:  # Not supported, path not recognized
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
        elif self.path == PATH_PREFIX + "/bye-and-clear-screen":
            try:
                global keep_looping
                keep_looping = False
                time.sleep(1.5)
                # Clear screen. Say Bye for 1 second before clearing the screen.
                # clear()
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


# Server Initialization
port_number: int = server_port
print("Starting PushButton server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
# For dev. Requires import sample_cache
# nmea_cache = json.loads(sample_cache.sample_json)

print("Server ready for duty.")
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

# After all
print("Done with REST PushButton server.")
