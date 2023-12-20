#!/usr/bin/env python3
#
# An example (WiP), showing how to have a REST and Web server.
# A Skeleton for further dev.
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
#
# Provides REST access to the JSON data, try GET http://localhost:8080/json-data/data
# Acts as a sensor reader.
#
# For a REST Channel (Consumer), consider looking at GET /json-data/data
#
# Start it with 
# $ python3 <...>/REST_and_WEB_server.py --machine-name:$(hostname -I) --port:9999 --verbose:false
#
import json
import signal
import sys
import os
# import traceback
import time
import random
# import math
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Dict
from datetime import datetime, timezone
# import NMEABuilder   # local script
# import utils         # local script

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

PATH_PREFIX: str = "/json-data"
STATIC_PATH_PREFIX: str = "/web"        # Whatever starts with /web is managed as static resource
# TODO zip prefix ? That'd be kewl...
server_port: int = 8080
verbose: bool = False
machine_name: str = "127.0.0.1"

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

keep_looping: bool = True
between_loops: int = 1

server_pid: int = os.getpid()  # Used to kill the process. Bam.

sample_data: Dict[str, str] = {  # Used for VIEW (and non-implemented) operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}


# Defining an HTTP request Handler class
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

        if path == PATH_PREFIX + "/data":
            if verbose:
                print("JSON Array Value request")
            try:
                json_data: str = json.dumps(DATA_ARRAY)
                # defining all the headers
                self.send_response(200)
                # self.send_header('Content-Type', 'text/plain')
                self.send_header('Content-Type', 'application/json')
                content_len = len(json_data)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(json_data.encode())
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
                        "path": PATH_PREFIX + "/exit",
                        "verb": "POST",
                        "description": "Careful: terminate the server process."
                    }, {
                        "path": PATH_PREFIX + "/data",
                        "verb": "GET",
                        "description": "Get the JSON data, in JSON format."
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
        elif path.startswith(STATIC_PATH_PREFIX):
            if verbose:
                print(f"Static path: {path}")
            static_resource: str = path[len(STATIC_PATH_PREFIX):]
            if verbose:
                print(f"Loading static resource [{static_resource}]")

            content_type: str = "text/html"
            binary: bool = False
            if static_resource.endswith(".css"):
                content_type = "text/css"
            elif static_resource.endswith(".js"):
                content_type = "text/javascript"
            elif static_resource.endswith(".png"):
                content_type = "image/png"
                binary = True
            elif static_resource.endswith(".ico"):
                content_type = "image/ico"
                binary = True
            # TODO more cases. jpg, gif, svg, ttf, pdf, wav, etc.

            # Content type based on file extension
            if not binary:
                with open("web" + static_resource) as f:
                    content = f.read()
            else:
                with open("web" + static_resource, "rb") as image:
                    content = image.read()

            if verbose:
                print(f"Data type: {type(content)}, content:\n{content}")
            self.send_response(200)
            # defining the response headers
            self.send_header('Content-Type', content_type)
            content_len = len(content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            if not binary:
                self.wfile.write(content.encode())
            else:
                self.wfile.write(content)
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
            print("POST on {} not managed".format(self.path))
        if self.path.startswith(PATH_PREFIX + "/exit"):
            print(">>>>> ZDA server received POST /exit")
            # content_len: int = int(self.headers.get('Content-Length'))
            # post_body = self.rfile.read(content_len).decode('utf-8')
            # if verbose:
            #    print("Content: {}".format(post_body))
            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            self.send_response(201)
            self.send_header('Content-Type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(response_content)
            time.sleep(2)  # Wait for response to be received
            print(f">>> Killing ZDA server process ({server_pid}).")
            os.kill(server_pid, signal.SIGKILL)
        else:
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # PUT method Definition
    def do_PUT(self):
        if verbose:
            print("PUT request, {}".format(self.path))
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


DATA_ARRAY: Dict[str, int] = {
}
MAP_MAX_LENGTH: int = 10

# THE data producer. To be customized...
def ping_data(dummy_prm: str) -> None:
    global keep_looping
    global between_loops
    global verbose
    global DATA_ARRAY
    global MAP_MAX_LENGTH

    print(f"Data thread")
    while keep_looping:
        # Do you job here
        # print("Pinging data...")
        # Generating data
        utc_ms = datetime.now(timezone.utc).timestamp() * 1_000  # System "UTC epoch" in ms
        dt_object = datetime.fromtimestamp(utc_ms / 1_000, tz=timezone.utc)  # <- Aha !!
        # Duration: YYYY-MM-DDTHH:MI:SS.sss
        # duration_date_time: str = dt_object.strftime("%H%M%S.00,%d,%m,%Y")
        duration_date_time: str = dt_object.strftime("%Y-%m-%dT%H:%M:%S")
        dummy_data: float = random.randrange(-100, 400) / 10
        print(f"New element {{ 'key':{duration_date_time}, 'value':{dummy_data} }}")
        data: Dict = {}
        data[duration_date_time] = dummy_data
        DATA_ARRAY.update(data)
        # Trim if too long
        while len(DATA_ARRAY) > MAP_MAX_LENGTH:
            key: str = list(DATA_ARRAY.keys())[0]
            print(f"Dropping {key}, {DATA_ARRAY.get(key)}")
            DATA_ARRAY.pop(key)

        if verbose:
            print(f"\tSleeping between loops for {between_loops} sec.")
        time.sleep(between_loops)  # Wait between loops
    print("Done with data thread")


if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

# Start data thread
client_thread: threading.Thread = \
                threading.Thread(target=ping_data, args=("Parameter...",))  # Producer
# print(f"Thread is a {type(client_thread)}")
client_thread.daemon = True  # Dies on exit
client_thread.start()

# Server Initialization
port_number: int = server_port
print("Starting server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
print("Try curl -X GET http://{}:{}{}/oplist".format(machine_name, port_number, PATH_PREFIX))
print("or  curl -v -X VIEW http://{}:{}{} -H \"Content-Length: 1\" -d \"1\"".format(machine_name, port_number, PATH_PREFIX))
#
try:
    server.serve_forever()
except KeyboardInterrupt:
    keep_looping = False
    print("\n\t\tUser interrupted (server.serve), exiting...")
    time.sleep(between_loops * 2)
    print("\n\t\tOver and out!")

print("Done with REST and Web server.")
