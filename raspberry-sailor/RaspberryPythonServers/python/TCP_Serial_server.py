#!/usr/bin/env python3
#
# That one reads a Serial port, and re-broadcasts NMEA strings to each connected client.
#
# It also understands input from the client: "STATUS", "EXIT" (not case sensitive), see client_listener.
#
# It starts TWO threads:
# - One to read the serial port and get NMEA strings
# - One to listen to possible client inputs.
#
# http://www.elinux.org/Serial_port_programming
# sudo apt-get install python-serial
# or
# pip3 install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
# Read Serial port, get NMEA Data, broadcast on TCP.
#
import serial
import sys
import os
import signal
import time
import socket
import threading
import traceback
import platform
import json
from datetime import datetime, timezone
import logging
from logging import info
from typing import List

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

keep_listening: bool = True

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
SERIAL_PORT: str = "/dev/ttyACM0"
BAUD_RATE: int = 4800
verbose: bool = False
DEBUG: bool = False

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
SERIAL_PORT_PREFIX: str = "--serial-port:"
BAUD_RATE_PREFIX: str = "--baud-rate:"
VERBOSE_PREFIX: str = "--verbose:"

CMD_STATUS: str = "STATUS"
CMD_EXIT: str = "EXIT"

NMEA_EOS: str = "\r\n"  # aka CR-LF


logging.basicConfig(level=logging.INFO, format='\n%(message)s')


def interrupt(sig: int, frame):
    # print(f"Signal: {type(sig)}, frame: {type(frame)}")
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    info(f'>> INFO: sigint_handler: Received signal {sig} on frame {frame}')
    # traceback.print_stack(frame)
    # sys.exit()   # DTC
    os._exit(1)  # Re-DTC


def read_nmea_sentence(serial_port: serial.serialposix.Serial) -> str:
    """
    Reads the serial port until a '\n' is met.
    :param serial_port: the port, as returned by serial.Serial
    :return: the full NMEA String, with its EOL '\r\n'
    """
    global DEBUG
    rv = []
    while True:
        ch = serial_port.read()
        if DEBUG:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if verbose:
                print("Returning {}".format(string))
            return string


def calculate_check_sum(sentence: str) -> int:
    cs = 0
    char_array = list(sentence)
    for c in range(len(sentence)):
        cs = cs ^ ord(char_array[c])  # This is an XOR
    return cs


def valid_check_sum(sentence: str) -> bool:
    star_index = -1
    try:
        star_index = sentence.index('*')
    except Exception:
        if verbose:
            print("No star was found")
        return False
    cs_key = sentence[-2:]
    # print("CS Key: {}".format(cs_key))
    try:
        csk = int(cs_key, 16)
    except Exception:
        print("Invalid Hex CS Key {}".format(cs_key))
        return False

    string_to_validate = sentence[1:-3]  # drop both ends, no $, no *CS
    # print("Key in HEX is {}, validating {}".format(csk, string_to_validate))
    calculated = calculate_check_sum(string_to_validate)
    if calculated != csk:
        if verbose:
            print("Invalid checksum. Expected {}, calculated {}".format(csk, calculated))
        return False
    elif verbose:
        print("Valid Checksum 0x{:02x}".format(calculated))

    return True


nb_clients: int = 0
producing_status: bool = False


def produce_status(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global producing_status
    global keep_listening
    message: dict = {
        "source": __file__,
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
    global keep_listening
    print("New client listener")
    while keep_listening:
        try:
            data: bytes = connection.recv(1_024)   # If receive from client is needed...
            if len(data) > 0:
                if verbose:
                    print(f"Received from client: {data}")
                client_mess: str = f"{data.decode('utf-8')}".strip().upper()  # Warning: upper
                if client_mess == CMD_STATUS:
                    produce_status(connection, address)
                elif client_mess == CMD_EXIT:
                    interrupt(None, None)
                # elif client_mess == "":
                #     pass  # ignore
                else:
                    print(f"Unknown or un-managed message [{client_mess}]")
                if len(client_mess) > 0:
                    print(f"Received {client_mess} request.")
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


def read_serial_port(connection: socket.socket, address: tuple, port: int) -> None:
    global nb_clients
    global producing_status
    global keep_listening
    print(f"Connected by client {connection}")
    while keep_listening:
        # data: bytes = conn.recv(1_024)   # If receive from client is needed...
        serial_nmea: str = read_nmea_sentence(port)

        try:
            if not producing_status:
                # if verbose:
                #    print(f"Read Serial Data and sending: {serial_nmea.strip()}")
                connection.sendall(serial_nmea.encode())  # Send to the client(s), broadcast.
            else:
                print("Waiting for the status to be completed.")
            time.sleep(0.25)   # between_loops)
        except BrokenPipeError as bpe:
            print("Serial Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("(Serial Producer) Oops!...")
            traceback.print_exc(file=sys.stdout)
            nb_clients -= 1
            break  # Client disconnected
    print(f"Exiting producer thread. Done with request(s) from {connection}.\nClosing.")
    connection.close()
    print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")


def main(args: List[str]) -> None:
    global HOST
    global PORT
    global BAUD_RATE
    global SERIAL_PORT
    global verbose
    global nb_clients
    global keep_listening

    print("Usage is:")
    print(
        f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{HOST}] [{PORT_PRM_PREFIX}{PORT}] [{SERIAL_PORT_PREFIX}{SERIAL_PORT}] [{BAUD_RATE_PREFIX}{BAUD_RATE}] [{VERBOSE_PREFIX}true|false]")
    print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the context's settings.\n")

    if len(args) > 0:  # Script name + X args. > 1 should do the job.
        for arg in args:
            if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
                HOST = arg[len(MACHINE_NAME_PRM_PREFIX):]
            if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
                PORT = int(arg[len(PORT_PRM_PREFIX):])
            if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
                verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")
            if arg[:len(SERIAL_PORT_PREFIX)] == SERIAL_PORT_PREFIX:
                SERIAL_PORT = arg[len(SERIAL_PORT_PREFIX):]
            if arg[:len(BAUD_RATE_PREFIX)] == BAUD_RATE_PREFIX:
                BAUD_RATE = int(arg[len(BAUD_RATE_PREFIX):])

    if verbose:
        print("-- Received from the command line: --")
        for arg in sys.argv:
            print(f"{arg}")
        print("-------------------------------------")

    # On mac, USB GPS on port /dev/tty.usbmodem14101,
    # Raspberry Pi, use /dev/ttyUSB0 or so.
    # port_name: str = "/dev/tty.usbmodem141101"
    baud_rate: int = BAUD_RATE
    port_name: str = SERIAL_PORT
    try:
        port: int = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
    except Exception as oops:
        print(f"Not such port {port_name}, exiting")
        sys.exit(1)

    signal.signal(signal.SIGINT, interrupt)  # callback, defined above.

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
                threading.Thread(target=read_serial_port, args=(conn, addr, port))  # Producer
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
