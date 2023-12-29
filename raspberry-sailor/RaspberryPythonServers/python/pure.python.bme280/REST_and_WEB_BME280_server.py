#!/usr/bin/env python3
#
# REST and Web server.
#
# Requires:
# ---------
# pip3 install http (already in python3.7+, no need to install it)
# https://learn.adafruit.com/adafruit-bme280-humidity-barometric-pressure-temperature-sensor-breakout/python-circuitpython-test
# Use a sudo pip3 install adafruit-circuitpython-bme280
#
# Provides REST access to the JSON data, try GET http://localhost:8080/json-data/data
# Acts as a sensor reader.
#
# For a REST Channel (Consumer), consider looking at GET /json-data/data
#
# Start it with 
# $ python3 <...>/REST_and_WEB_BME280_server.py --machine-name:$(hostname -I | awk '{ print $1 }') --port:8080 --verbose:false [--address:0x76]
#
# Note: Default I2C address for a BME280 is 0x77 (one the sensor is connected, do a "sudo i2cdetect -y 1")
# From some vendors (like AliBaba), it can sometime be 0x76, hence the --address: CLI parameter (see below).
#
#
import json
import signal
import sys
import os
import traceback
import time
import math
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Dict
from datetime import datetime, timezone
import board
import busio
from adafruit_bme280 import basic as adafruit_bme280   # pip3 install adafruit-circuitpython-bme280

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

PATH_PREFIX: str = "/json-data"
STATIC_PATH_PREFIX: str = "/web"        # Whatever starts with /web is managed as static resource. See below.
# TODO zip prefix ? That'd be kewl...
server_port: int = 8080
verbose: bool = False
machine_name: str = "127.0.0.1"
ADDRESS: int = 0x77     # Default. We've seen some 0x76... Hence this parameter.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"
ADDRESS_PREFIX: str = "--address:"

keep_looping: bool = True
between_loops: int = 1            # 1 sec
between_big_loops: int = 15 * 60  # 15 minutes

sensor: adafruit_bme280.Adafruit_BME280_I2C

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
        global verbose
        if verbose:
            print("GET methods")
        #
        full_path: str = self.path
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
                full_data: Dict[str, object] = {}
                full_data["instant"] = instant_data
                full_data["pressure-map"] = PRESSURE_MAP
                full_data["temperature-map"] = TEMPERATURE_MAP
                json_data: str = json.dumps(full_data)
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
                        "path": PATH_PREFIX + "/verbose[?value=true|false]",
                        "verb": "POST",
                        "description": "Set verbose to true (default) or false."
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
            else:
                if static_resource.endswith("/"):  # Assuming index.html
                    static_resource += "index.html"
                if verbose:
                    print(f"un-managed ststic_resource type for {static_resource}, assuming html.")
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
        global verbose
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
        if self.path.startswith(PATH_PREFIX + "/exit"):
            print(">>>>> Server received POST /exit")
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
            print(f">>> Killing Server process ({server_pid}).")
            os.kill(server_pid, signal.SIGKILL)
        elif self.path.startswith(PATH_PREFIX + "/verbose"):
            print(">>>>> Server received POST /verbose")
            # Find true or false
            full_path: str = self.path
            status: str = 'true'
            if full_path.index("?") > 0:
                query_string = full_path[full_path.index("?") + 1:]
                # print(f"Full QueryString: {query_string}")
                qs_params: list = query_string.split('&')
                # Look for a 'value'
                for qs_prm in qs_params:
                    # print(f"Managing {qs_prm}")
                    nv_pair = qs_prm.split('=')
                    if len(nv_pair) == 2:
                        # print(f"We have {nv_pair[0]} = {nv_pair[1]}")
                        if nv_pair[0] == 'value':
                            if nv_pair[1] != 'true' and nv_pair[1] != 'false':
                                print(f"Unsupported value {nv_pair[1]} for 'value' parameter. Defaulting to 'true'")
                            else:
                                status = nv_pair[1]
                        else:
                            print(f"Unsupported parameter named {nv_pair[0]}")
                    else:
                        print("oops, no equal sign in {}".format(qs_prm))
            print(f"Setting verbose: {status}")
            verbose = (status == 'true')

            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            self.send_response(201)
            self.send_header('Content-Type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(response_content)
        else:
            print("POST on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # PUT method Definition
    def do_PUT(self):
        global verbose
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
        global verbose
        if verbose:
            print("DELETE on {} not managed".format(self.path))
        error = "NOT FOUND!"
        self.send_response(400)
        self.send_header('Content-Type', 'text/plain')
        content_len = len(error)
        self.send_header('Content-Length', str(content_len))
        self.end_headers()
        self.wfile.write(bytes(error, 'utf-8'))


PRESSURE_MAP: Dict[str, float] = {}
TEMPERATURE_MAP: Dict[str, float] = {}

MAP_MAX_LENGTH: int = 672  # 672 * 15 minutes: 7 days
instant_data: Dict[str, float] = {}


def dew_point_temperature(hum: float, temp: float) -> float:
    c1: float = 6.10780
    c2: float = 17.08085 if (temp > 0) else 17.84362
    c3: float = 234.175 if (temp > 0) else 245.425

    pz: float = c1 * math.exp((c2 * temp) / (c3 + temp))
    pd: float = pz * (hum / 100)

    dew_point_temp: float = (- math.log(pd / c1) * c3) / (math.log(pd / c1) - c2)

    return dew_point_temp


# THE long storage data producer. To be customized...
def long_storage_data(dummy_prm: str) -> None:
    global keep_looping
    global between_loops
    global verbose
    global DATA_ARRAY
    global MAP_MAX_LENGTH

    print(f"Long Storage thread")
    ping: int = 0
    while keep_looping:
        if ping % between_big_loops == 0:
            ping = 0  # Reset counter, to avoid overflow
            # Generating data
            utc_ms = datetime.now(timezone.utc).timestamp() * 1_000  # System "UTC epoch" in ms
            dt_object = datetime.fromtimestamp(utc_ms / 1_000, tz=timezone.utc)  # <- Aha !!
            # Duration: YYYY-MM-DDTHH:MI:SS.sss
            # duration_date_time: str = dt_object.strftime("%H%M%S.00,%d,%m,%Y")
            duration_date_time: str = dt_object.strftime("%Y-%m-%dT%H:%M:%S")
            # 1 - Pressure
            all_good: bool = True
            try:
                pressure_data: float = instant_data["pressure"]
                if verbose:
                    print(f"New element {{ 'key':{duration_date_time}, 'value':{pressure_data} }}")
                data: Dict = {}
                data[duration_date_time] = pressure_data
                PRESSURE_MAP.update(data)
                # Trim if too long
                while len(PRESSURE_MAP) > MAP_MAX_LENGTH:
                    key: str = list(PRESSURE_MAP.keys())[0]
                    if verbose:
                        print(f"Dropping {key}, {PRESSURE_MAP.get(key)}")
                    PRESSURE_MAP.pop(key)
            except KeyError as key_error:
                print(f"Oops: no {key_error} yet...")
                all_good = False
            # 2 - Temperature
            try:
                temperature_data: float = instant_data["temperature"]
                if verbose:
                    print(f"New element {{ 'key':{duration_date_time}, 'value':{temperature_data} }}")
                # data: Dict = {}
                data[duration_date_time] = temperature_data
                TEMPERATURE_MAP.update(data)
                # Trim if too long
                while len(TEMPERATURE_MAP) > MAP_MAX_LENGTH:
                    key: str = list(TEMPERATURE_MAP.keys())[0]
                    if verbose:
                        print(f"Dropping {key}, {TEMPERATURE_MAP.get(key)}")
                    TEMPERATURE_MAP.pop(key)
            except KeyError as key_error:
                print(f"Oops: no {key_error} yet...")
                all_good = False
        if all_good:
            ping += 1
            if verbose:
                print(f"\t=> ping {ping}/{between_big_loops}")
        if verbose:
            print(f"\t(Big Loop) Sleeping between loops for {between_loops} sec.")
        time.sleep(between_loops)  # Wait between loops
    print("\tDone with long storage data thread")


# Reads the BME280
def produce_data(dummy_prm: str) -> None:
    global verbose
    global between_loops
    global keep_looping
    global sensor

    while keep_looping:
        if sensor is not None:
            temperature: float = sensor.temperature  # Celsius
            humidity: float = sensor.relative_humidity  # %
            pressure: float = sensor.pressure  # hPa
            dpt: float = dew_point_temperature(humidity, temperature) # Celsius
        else:
            print("No BME280 was found")

        try:
            # Send to the client
            if sensor is not None:
                data: Dict = {}
                data["temperature"] = temperature
                data["humidity"] = humidity
                data["pressure"] = pressure
                data["dew-point"] = dpt
                instant_data.update(data)
            if verbose:
                print(f"\t(Instant loop) Sleeping between loops for {between_loops} sec. Data is {json.dumps(instant_data)}")
            time.sleep(between_loops)  # Wait between loops
        except Exception as ex:
            print("Oops!...")
            traceback.print_exc(file=sys.stdout)
            break  # Client disconnected
    print(f"Exiting data producer thread.\nClosing.")


if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")
        if arg[:len(ADDRESS_PREFIX)] == ADDRESS_PREFIX:
            ADDRESS = int(arg[len(ADDRESS_PREFIX):], 16)  # Expect hex number
if verbose:
    print("-- Received from the command line: --")
    for arg in sys.argv:
        print(f"{arg}")
    print("-------------------------------------")

i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
try:
    # Sensor I2C address may change..., address=0x76
    sensor = adafruit_bme280.Adafruit_BME280_I2C(i2c, address=ADDRESS)
    sensor.sea_level_pressure = 1013.25  # Depends on your location
    if verbose:
        print("BME280 was found and initialized...")
except:
    print("No BME280 was found...")
    sensor = None


# Start data thread
if True:
    long_storage_thread: threading.Thread = \
                    threading.Thread(target=long_storage_data, args=("Parameter...",))  # Long Storage Producer
    # print(f"Thread is a {type(client_thread)}")
    long_storage_thread.daemon = True  # Dies on exit
    long_storage_thread.start()

data_thread: threading.Thread = \
                threading.Thread(target=produce_data, args=("Parameter...",))  # Data Producer
# print(f"Thread is a {type(client_thread)}")
data_thread.daemon = True  # Dies on exit
data_thread.start()

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
