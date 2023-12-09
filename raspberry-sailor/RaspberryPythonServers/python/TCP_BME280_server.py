#!/usr/bin/env python3

"""
A TCP server.

To install the required packages:
https://learn.adafruit.com/adafruit-bme280-humidity-barometric-pressure-temperature-sensor-breakout/python-circuitpython-test

Use a sudo pip3 install adafruit-circuitpython-bme280

Produces XDR, MTA, MMB, from the data read from a BME280, (TODO: MBA ?, MDA (for the dew point))
on a regular basis, see the between_loops variable.

See the produce_nmea function.
See the special commands: CMD_STATUS, CMD_LOOPS, CMD_EXIT - To use from a TCP Client (See the module Java-TCP-Python for samples and code.)

Note: Default I2C address for a BME280 is 0x77 (one the sensor is connected, do a "sudo i2cdetect -y 1")
From some vendors (like AliBaba), it can sometime be 0x76, hence the --address: CLI parameter (see below).
"""

import sys
import os
import signal
import time
import socket
import threading
import traceback
import json
import platform
import board
from datetime import datetime, timezone
import logging
from logging import info
import NMEABuilder  # local script
import utils        # local script
from typing import List
import busio
# import math, yaml ?
from adafruit_bme280 import basic as adafruit_bme280   # pip3 install adafruit-circuitpython-bme280

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

keep_listening: bool = True
sensor: adafruit_bme280.Adafruit_BME280_I2C

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost). Set to actual IP or name (from CLI) to make it reacheable from outside.
PORT: int = 7001  # Port to listen on (non-privileged ports are > 1023)
verbose: bool = False
ADDRESS: int = 0x77

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"
ADDRESS_PREFIX: str = "--address:"

CMD_STATUS: str = "STATUS"
CMD_LOOP_PREFIX: str = "LOOPS:"
CMD_EXIT: str = "EXIT"

NMEA_EOS: str = "\r\n"  # aka CR-LF


def interrupt(sig: int, frame):
    # print(f"Signal: {type(sig)}, frame: {type(frame)}")
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    info(f'>> INFO: sigint_handler: Received signal {sig} on frame {frame}')
    # traceback.print_stack(frame)
    # sys.exit()  # DTC
    os._exit(1)


nb_clients: int = 0
between_loops: float = 1.0  # in seconds, between reads of the BME280
producing_status: bool = False


def produce_status(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    global keep_listening
    global verbose
    message: dict = {
        "source": __file__,
        "between-loops": between_loops,
        "connected-clients": nb_clients,
        "python-version": platform.python_version(),
        "system-utc-time": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")
    }
    try:
        payload: str = json.dumps(message) + NMEA_EOS  # str(message) + NMEA_EOS
        producing_status = True
        if verbose:
            print(f"Sending status to client: {payload}")
        connection.sendall(payload.encode())  # FIXME This one does not go through...
        producing_status = False
    except Exception:
        print("Oops!...")
        traceback.print_exc(file=sys.stdout)


def client_listener(connection: socket.socket, address: tuple) -> None:
    """
    Expects several possible inputs: "STATUS", "LOOPS:x.xx", "EXIT" (not case-sensitive).
    """
    global nb_clients
    global between_loops
    global keep_listening
    global verbose

    print("New client listener")
    while keep_listening:
        try:
            data: bytes = connection.recv(1_024)  # If receive from client is needed...
            if len(data) > 0:
                if verbose:
                    print(f"Received from client: {data}")
                client_mess: str = f"{data.decode('utf-8')}".strip().upper()  # Warning: upper
                if client_mess[:len(CMD_LOOP_PREFIX)] == CMD_LOOP_PREFIX:
                    try:
                        between_loops = float(client_mess[len(CMD_LOOP_PREFIX):])
                    except ValueError as ex:
                        print("Bad number, oops!...")
                        traceback.print_exc(file=sys.stdout)
                    produce_status(connection, address)
                elif client_mess == CMD_STATUS:
                    produce_status(connection, address)
                elif client_mess == CMD_EXIT:
                    print(">> TCP BME280 received an EXIT message.")
                    interrupt(None, None)
                # elif client_mess == "":
                #     pass  # ignore
                else:
                    print(f"Unknown or un-managed message [{client_mess}]")
                if len(client_mess) > 0:
                    print(f"Received {client_mess} request. Between Loop value: {between_loops} s.")
        except ConnectionResetError as cre:
            print("ClientListener disconnected")
            # nb_clients -= 1
            break
        except BrokenPipeError as bpe:
            print("ClientListener disconnected")
            # nb_clients -= 1
            break
        except Exception as ex:
            print("(ClientListener) Oops!...")
            traceback.print_exc(file=sys.stdout)
            break  # Client disconnected
    print("Exiting client listener thread")


def produce_nmea(connection: socket.socket, address: tuple,
                 mta_sentences: bool = True,
                 mmb_sentences: bool = True,
                 xdr_sentences: bool = True) -> None:
    global verbose
    global nb_clients
    global between_loops
    global producing_status
    global keep_listening
    global sensor
    print(f"Connected by client {connection}")
    while keep_listening:
        # data: bytes = conn.recv(1_024)   # If receive from client is needed...
        if sensor is not None:
            temperature: float = sensor.temperature  # Celsius
            humidity: float = sensor.relative_humidity  # %
            pressure: float = sensor.pressure  # hPa
            dpt: float = utils.dew_point_temperature(humidity, temperature)   # This way, no Computer required on the mux

            nmea_mta: str = NMEABuilder.build_MTA(temperature) + NMEA_EOS
            nmea_mmb: str = NMEABuilder.build_MMB(pressure) + NMEA_EOS
            nmea_xdr: str = NMEABuilder.build_XDR({"value": humidity, "type": "HUMIDITY"},
                                                  {"value": temperature, "type": "TEMPERATURE"},
                                                  {"value": dpt, "type": "TEMPERATURE", "extra": "DEWP"},
                                                  {"value": pressure * 100, "type": "PRESSURE_P"},
                                                  {"value": pressure / 1_000, "type": "PRESSURE_B"}) + NMEA_EOS

            if verbose:
                # Date formatting: https://docs.python.org/2/library/datetime.html#strftime-and-strptime-behavior
                print(f"-- At {datetime.now(timezone.utc).strftime('%d-%b-%Y %H:%M:%S')} --")
                if mta_sentences:
                    print(f"Sending {nmea_mta.strip()}")
                if mmb_sentences:
                    print(f"Sending {nmea_mmb.strip()}")
                if xdr_sentences:
                    print(f"Sending {nmea_xdr.strip()}")
                print("---------------------------")
        else:
            dummy_str: str = NMEABuilder.build_MSG("No BME280 was found") + NMEA_EOS

        try:
            # Send to the client
            if sensor is not None:
                if not producing_status:
                    if mta_sentences:
                        connection.sendall(nmea_mta.encode())
                    if mmb_sentences:
                        connection.sendall(nmea_mmb.encode())
                    if xdr_sentences:
                        connection.sendall(nmea_xdr.encode())
                else:
                    print("Waiting for the status production to be completed.")
            else:
                print(f"No Sensor: {dummy_str.strip()}")
                if not producing_status:
                    connection.sendall(dummy_str.encode())
                else:
                    print("Producing status, wait.")
            if verbose:
                print(f"\tSleeping between loops for {between_loops} sec.")
            time.sleep(between_loops)  # Wait between loops
        except BrokenPipeError as bpe:
            print("Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("Oops!...")
            traceback.print_exc(file=sys.stdout)
            nb_clients -= 1
            break  # Client disconnected
    print(f"Exiting NMEA producer thread. Done with request from {connection}.\nClosing.")
    connection.close()
    print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")


def main(args: List[str]) -> None:
    global HOST
    global PORT
    global ADDRESS
    global verbose
    global nb_clients
    global sensor
    global keep_listening

    print("Usage is:")
    print(
        f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{HOST}] [{PORT_PRM_PREFIX}{PORT}] [{VERBOSE_PREFIX}true|false] [{ADDRESS_PREFIX}0x77]")
    print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the context's settings.\n")

    if len(args) > 0:  # Script name + X args. > 1 should do the job.
        for arg in args:
            if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
                HOST = arg[len(MACHINE_NAME_PRM_PREFIX):]
            if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
                PORT = int(arg[len(PORT_PRM_PREFIX):])
            if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
                verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")
            if arg[:len(ADDRESS_PREFIX)] == ADDRESS_PREFIX:
                ADDRESS = int(arg[len(ADDRESS_PREFIX):], 16)  # Expect hex number

    if verbose:
        print("-- Received from the command line: --")
        for arg in sys.argv:
            print(f"{arg}")
        print("-------------------------------------")

    signal.signal(signal.SIGINT, interrupt)  # callback, defined above.
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

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        if verbose:
            print(f"Binding {HOST}:{PORT}...")
        try:
            s.bind((HOST, PORT))
        except OSError:
            traceback.print_exc(file=sys.stdout)
            print("Exiting.")
            sys.exit(1)
        s.listen()
        print("Server is listening. [Ctrl-C] will stop the process.")
        while keep_listening:
            conn, addr = s.accept()
            # print(f">> New accept: Conn is a {type(conn)}, addr is a {type(addr)}")
            nb_clients += 1
            print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")
            # Generate NMEA sentences for this client in its own thread. Producer thread
            client_thread: threading.Thread = \
                threading.Thread(target=produce_nmea, args=(conn, addr, True, True, True,))  # Producer
            # print(f"Thread is a {type(client_thread)}")
            client_thread.daemon = True  # Dies on exit
            client_thread.start()

            # Listener thread (client special requests)
            client_listener_thread: threading.Thread = \
                threading.Thread(target=client_listener, args=(conn, addr,))  # Listener
            client_listener_thread.daemon = True  # Dies on exit
            client_listener_thread.start()

    print("Exiting server")
    print("Bon. OK.")


if __name__ == '__main__':
    main(sys.argv)
