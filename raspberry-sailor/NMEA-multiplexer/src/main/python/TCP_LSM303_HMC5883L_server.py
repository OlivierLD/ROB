#!/usr/bin/env python3

"""
A TCP server.

To install the required packages:
https://learn.adafruit.com/lsm303-accelerometer-slash-compass-breakout/python-circuitpython

https://docs.circuitpython.org/projects/lsm303/en/latest/_modules/adafruit_lsm303.html

Produces HDG (or HDM) Strings, from the data read from a LSM303 or a HMC5883L,
on a regular basis, see the between_loops variable.
"""

import sys
import signal
import time
import socket
import threading
import traceback
import json
import platform
import board
from datetime import datetime, timezone
import NMEABuilder   # local script
from typing import List
import busio
import adafruit_lsm303dlh_mag

keep_listening: bool = True
sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost). Set to actual IP or name (from CLI) to make it reacheable from outside.
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
verbose: bool = True

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

NMEA_EOS: str = "\r\n"  # aka CR-LF
DATA_EOS: str = "\r\n"  # aka CR-LF


def interrupt(sig: int, frame):
    # print(f"Signal: {type(sig)}, frame: {type(frame)}")
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    info(f'>> INFO: sigint_handler: Received signal {sig} on frame {frame}')
    # traceback.print_stack(frame)
    sys.exit()   # DTC


nb_clients: int = 0
between_loops: float = 1.0  # in seconds
producing_status: bool = False


def produce_MAG_Data(sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag) -> str:
    mag_x, mag_y, mag_z = sensor.magnetic
    data: dict = {
        "mag_x": mag_x,
        "mag_y": mag_y,
        "mag_z": mag_z
    }
    data_str: str = json.dumps(data) + DATA_EOS  # DATA_EOS is important, the client does a readLine !
    return data_str


def produce_status(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    global keep_listening
    message: Dict = {
        "source": __file__,
        "between-loops": between_loops,
        "connected-clients": nb_clients,
        "python-version": platform.python_version(),
        "system-utc-time": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")
    }
    try:
        payload: str = json.dumps(message) + NMEA_EOS  # str(message) + NMEA_EOS
        if verbose:
            print(f"Producing status: {payload}")
        producing_status = True
        connection.sendall(payload.encode())  # This one does not go through...
        producing_status = False
    except Exception:
        print("Oops!...")
        traceback.print_exc(file=sys.stdout)


def client_listener(connection: socket.socket, address: tuple) -> None:
    """
    Expects several possible inputs: "STATUS", "LOOPS:x.xx" (not case-sensitive).
    """
    global nb_clients
    global between_loops
    print("New client listener")
    while keep_listening:
        try:
            data: bytes = connection.recv(1024)   # If receive from client is needed...
            if len(data) > 0:
                if verbose:
                    print(f"Received from client: {data}")
                client_mess: str = f"{data.decode('utf-8')}".strip().upper()  # Warning: upper
                if  client_mess[:len(CMD_LOOP_PREFIX)] == CMD_LOOP_PREFIX:
                    try:
                        between_loops = float(client_mess[len(CMD_LOOP_PREFIX):])
                    except ValueError as ex:
                        print("Bad number, oops!...")
                        traceback.print_exc(file=sys.stdout)
                    produce_status(connection, address)
                elif client_mess == CMD_STATUS:
                    produce_status(connection, address)
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
                 hdg_sentences: bool = True,
                 hdm_sentences: bool = True,
                 xdr_sentences: bool = True) -> None:
    global nb_clients
    global sensor
    print(f"Connected by client {connection}")
    while True:
        # data: bytes = conn.recv(1024)   # If receive from client is needed...
        data_str: str = produce_MAG_Data(sensor)
        #
        # TODO Introduce calibration parameters. Calculate data.
        hdg: float = 0.0   # Degrees
        ptch: float = 0.0  # Degrees
        roll: float = 0.0  # Degrees

        nmea_hdg: str = NMEABuilder.build_HDG(hdg) + NMEA_EOS
        nmea_hdm: str = NMEABuilder.build_HDM(hdg) + NMEA_EOS
        # like "$IIXDR,A,180,D,PTCH,A,-154,D,ROLL*78"
        nmea_xdr: str = NMEABuilder.build_XDR({ "value": ptch, "type": "ANGULAR_DISPLACEMENT" },
                                              { "value": roll, "type": "ANGULAR_DISPLACEMENT" }) + NMEA_EOS

        if verbose:
            # Date formatting: https://docs.python.org/2/library/datetime.html#strftime-and-strptime-behavior
            print(f"-- At {datetime.now(timezone.utc).strftime('%d-%b-%Y %H:%M:%S') } --")
            if hdg_sentences:
                print(f"Sending {nmea_hdg.strip()}")
            if hdm_sentences:
                print(f"Sending {nmea_hdm.strip()}")
            if xdr_sentences:
                print(f"Sending {nmea_xdr.strip()}")
            print("---------------------------")

        try:
            # Send to the client
            if hdg_sentences:
                connection.sendall(nmea_hdg.encode())
            if hdm_sentences:
                connection.sendall(nmea_hdm.encode())
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
    i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
    sensor = adafruit_lsm303dlh_mag.LSM303DLH_Mag(i2c)

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
        while keep_listening:   # Listen for new connections
            conn, addr = s.accept()
            # print(f">> New accept: Conn is a {type(conn)}, addr is a {type(addr)}")
            nb_clients += 1
            print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")
            # Generate ZDA sentences for this client in its own thread. Producer thread
            client_thread: threading.Thread = \
                threading.Thread(target=produce_nmea, args=(conn, addr, True, True, True,))  # Producer
            # print(f"Thread is a {type(client_thread)}")
            client_thread.daemon = True  # Dies on exit
            client_thread.start()

            # Listener thread
            client_listener_thread: threading.Thread = \
                threading.Thread(target=client_listener, args=(conn, addr,))  # Listener
            client_listener_thread.daemon = True  # Dies on exit
            client_listener_thread.start()

    print("Exiting server")
    print("Bon. OK.")


if __name__ == '__main__':
    main(sys.argv)
