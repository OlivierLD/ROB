#!/usr/bin/env python3

"""
A TCP server.

Produces XDR, MTA and MMB Strings, from the data read from a BMP180,
on a regular basis, see the between_loops variable.
"""

import sys
import signal
import time
import socket
import threading
import traceback
from datetime import datetime, timezone
import NMEABuilder as NMEABuilder  # local script
from typing import List
import Adafruit_BMP.BMP085 as BMP085


keep_listening: bool = True
sensor: BMP085.BMP085

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost). Set to actual IP or name (from CLI) to make it reacheable from outside.
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
verbose: bool = True

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

NMEA_EOS: str = "\r\n"  # aka CR-LF


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    sys.exit()   # DTC


nb_clients: int = 0
between_loops: float = 2.0  # in seconds


def produce_nmea(connection: socket.socket, address: tuple,
                 mta_sentences: bool = True,
                 mmb_sentences: bool = True,
                 xdr_sentences: bool = True) -> None:
    global nb_clients
    global sensor
    print(f"Connected by client {connection}")
    while True:
        # data: bytes = conn.recv(1024)   # If receive from client is needed...
        temperature: float = sensor.read_temperature()  # Celsius
        pressure: float = sensor.read_pressure()        # Pa
        altitude: float = sensor.read_altitude()        # meters
        sea_level_pressure: float = sensor.read_sealevel_pressure()

        # OpenCPN expects the pressure in BARS from XDR, and air temperature from MTA !
        # XDR Temperature is not necessarily Air Temperature...
        nmea_mta: str = NMEABuilder.build_MTA(temperature) + NMEA_EOS
        nmea_mmb: str = NMEABuilder.build_MMB(pressure / 100) + NMEA_EOS
        nmea_xdr: str = NMEABuilder.build_XDR({"value": temperature, "type": "TEMPERATURE"},
                                              { "value": pressure, "type": "PRESSURE_P" },
                                              { "value": pressure / 100_000, "type": "PRESSURE_B" }) + NMEA_EOS

        if verbose:
            # Date formatting: https://docs.python.org/2/library/datetime.html#strftime-and-strptime-behavior
            print(f"-- At {datetime.now(timezone.utc).strftime('%d-%b-%Y %H:%M:%S') } --")
            if mta_sentences:
                print(f"Sending {nmea_mta.strip()}")
            if mmb_sentences:
                print(f"Sending {nmea_mmb.strip()}")
            if xdr_sentences:
                print(f"Sending {nmea_xdr.strip()}")
            print("---------------------------")

        try:
            # Send to the client
            if mta_sentences:
                connection.sendall(nmea_mta.encode())
            if mmb_sentences:
                connection.sendall(nmea_mmb.encode())
            if xdr_sentences:
                connection.sendall(nmea_xdr.encode())
            time.sleep(between_loops)
        except BrokenPipeError as bpe:
            print("Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("Oops!...")
            traceback.print_exc(file=sys.stdout)
            nb_clients -= 1
            break  # Client disconnected
    print(f"Done with request from {connection}")
    print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")


def main(args: List[str]) -> None:
    global HOST
    global PORT
    global verbose
    global nb_clients
    global sensor

    print("Usage is:")
    print(
        f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{HOST}] [{PORT_PRM_PREFIX}{PORT}] [{VERBOSE_PREFIX}true|false]")
    print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the context's settings.\n")

    if len(args) > 0:  # Script name + X args. > 1 should do the job.
        for arg in args:
            if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
                HOST = arg[len(MACHINE_NAME_PRM_PREFIX):]
            if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
                PORT = int(arg[len(PORT_PRM_PREFIX):])
            if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
                verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

    if verbose:
        print("-- Received from the command line: --")
        for arg in sys.argv:
            print(f"{arg}")
        print("-------------------------------------")

    signal.signal(signal.SIGINT, interrupt)  # callback, defined above.
    sensor = BMP085.BMP085(busnum=1)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        if verbose:
            print(f"Binding {HOST}:{PORT}...")
        s.bind((HOST, PORT))
        s.listen()
        print("Server is listening. [Ctrl-C] will stop the process.")
        while keep_listening:
            conn, addr = s.accept()
            print(f">> New accept: Conn is a {type(conn)}, addr is a {type(addr)}")
            nb_clients += 1
            print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")
            # Generate ZDA sentences for this client in its own thread.
            client_thread = threading.Thread(target=produce_nmea, args=(conn, addr, True, False, True,))
            client_thread.daemon = True  # Dies on exit
            client_thread.start()

    print("Exiting server")
    print("Bon. OK.")


if __name__ == '__main__':
    main(sys.argv)
