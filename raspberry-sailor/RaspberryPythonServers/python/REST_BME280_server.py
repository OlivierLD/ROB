#!/usr/bin/env python3
#
# Requires:
# ---------
# pip3 install http (already in python3.7, no need to install it)
# [sudo] pip3 install adafruit-circuitpython-bme280
#
# Provides REST access to the BME280 data, try GET http://localhost:8080/bme280/data
#
# For NMEA-multiplexer REST Channel (Consumer), consider looking at GET /bme280/nmea-data
#
# Start it with 
# $ python3 <...>/REST_BME280_server.py --machine-name:$(hostname -I) --port:9999 --verbose:false --simulate-when-missing:false
#
import json
import sys
import random
# import traceback
# import time
# import math
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Dict
import board
import busio
import NMEABuilder   # local script
import utils         # local script


from adafruit_bme280 import basic as adafruit_bme280  # pip3 install adafruit-circuitpython-bme280

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

PATH_PREFIX = "/bme280"
server_port: int = 8080
verbose: bool = False
machine_name: str = "127.0.0.1"
simulate_when_missing: bool = False

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"
SIMULATE_WHEN_MISSING_PREFIX: str = "--simulate-when-missing:"

sensor: adafruit_bme280.Adafruit_BME280_I2C

sample_data: Dict[str, str] = {  # Used for VIEW, and non-implemented operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}


i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
try:
    sensor = adafruit_bme280.Adafruit_BME280_I2C(i2c)
    sensor.sea_level_pressure = 1013.25  # Depends on your location
except:
    print("No BME280 was found...")
    sensor = None


def read_bme280() -> dict:
    """
    Reads the sensor, returns a JSON structure.
    """
    global sensor
    temperature: float = None  # Celsius
    humidity: float = None     # %
    pressure: float = None     # hPa
    status: str = None

    if sensor is not None:
        temperature = sensor.temperature     # Celsius
        humidity = sensor.relative_humidity  # %
        pressure = sensor.pressure           # hPa
        status = "OK"
    else:
        if simulate_when_missing:
            temperature = random.randrange(-100, 400) / 10
            humidity = random.randrange(0, 1_000) / 10
            pressure = random.randrange(9_500, 10_400) / 10
            status = "OK - Simulated"
        else:
            status = "No BME280 was found"
    return {
        "temperature": temperature,
        "humidity": humidity,
        "pressure": pressure,
        "status": status
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

        if path == PATH_PREFIX + "/data":
            if verbose:
                print("BME280 Value request")
            try:
                bme280_data: dict = read_bme280()
                # TODO Headers ?
                self.wfile.write(json.dumps(bme280_data).encode())
            except Exception as exception:
                error = {"message": "{}".format(exception)}
                self.wfile.write(json.dumps(error).encode())
                self.send_response(500)
        elif path == PATH_PREFIX + "/nmea-data":
            if verbose:
                print("BME280 NMEA-Value request")
            try:
                bme280_data: dict = read_bme280()
                # Transform sensor data to NMEA Strings
                nmea_data: dict
                if not bme280_data['status'].startswith('OK'):  # "OK", or "OK, simulated"
                    mess: str = NMEABuilder.build_MSG(bme280_data['status']) + NMEABuilder.NMEA_EOS
                    nmea_data = { "message": mess }
                else:
                    temperature: float = bme280_data['temperature']
                    pressure: float = bme280_data['pressure']
                    humidity: float = bme280_data['humidity']
                    dpt: float = utils.dew_point_temperature(humidity, temperature)   # This way, no Computer required on the mux

                    nmea_mta: str = NMEABuilder.build_MTA(temperature) + NMEABuilder.NMEA_EOS
                    nmea_mmb: str = NMEABuilder.build_MMB(pressure) + NMEABuilder.NMEA_EOS
                    nmea_xdr: str = NMEABuilder.build_XDR({"value": humidity, "type": "HUMIDITY"},
                                                          {"value": temperature, "type": "TEMPERATURE"},
                                                          {"value": dpt, "type": "TEMPERATURE", "extra": "DEWP"},
                                                          {"value": pressure * 100, "type": "PRESSURE_P"},
                                                          {"value": pressure / 1_000, "type": "PRESSURE_B"}) + NMEABuilder.NMEA_EOS
                    nmea_data = {
                        "01": nmea_mta,
                        "02": nmea_mmb,
                        "03": nmea_xdr
                    }
                self.wfile.write(json.dumps(nmea_data).encode())
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
                        "path": PATH_PREFIX + "/data",
                        "verb": "GET",
                        "description": "Get the BME280 data, in json format."
                    }, {
                        "path": PATH_PREFIX + "/nmea-data",
                        "verb": "GET",
                        "description": "Get the BME280 data, NMEA format."
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
            content_len = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            print("Content: {}".format(post_body))

            self.send_response(201)
            response = {"status": "OK"}
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
        if verbose:
            print("PUT request, {}".format(self.path))
        if self.path.startswith("/whatever/"):
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
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


if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")
        if arg[:len(SIMULATE_WHEN_MISSING_PREFIX)] == SIMULATE_WHEN_MISSING_PREFIX:
            simulate_when_missing = (arg[len(SIMULATE_WHEN_MISSING_PREFIX):].lower() == "true")

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
    print("\n\t\tUser interrupted (server.serve), exiting.")

print("Done with REST BME280 server.")
