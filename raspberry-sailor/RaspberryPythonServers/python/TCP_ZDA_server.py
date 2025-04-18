#!/usr/bin/env python3
#
# That one produces ZDA Strings for each connected client.
# This server does not need to read a sensor, it takes the time from the system.
#
#
# Start it with
# $ python src/main/python/nmea/TCP_ZDA_server.py --port:7002 --verbose:true --machine-name:my-box.home

# It also understands input from the client: "STATUS", "LOOPS:x.xx", "EXIT" (not case sensitive), see client_listener.
# LOOPS:xxx will produce a between_loops = x.xx (in seconds), like LOOPS:1.0
#
# It starts TWO threads:
# - One to produce the ZDA strings
# - One to listen to possible client inputs.
#
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
import NMEABuilder              # local script
from typing import List

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

keep_listening: bool = True

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
verbose: bool = False

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

CMD_STATUS: str = "STATUS"
CMD_LOOP_PREFIX: str = "LOOPS:"
CMD_EXIT: str = "EXIT"

NMEA_EOS: str = "\r\n"  # aka CR-LF


logging.basicConfig(level=logging.INFO, format='\n%(message)s')


def interrupt(sig: int, frame):
    # print(f"Signal: {type(sig)}, frame: {type(frame)}")
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("+---------- From Python ---------+")
    print("| Python TCP ZDA Server Exiting. |")
    print("+---------- From Python ---------+")
    info(f'>> INFO: sigint_handler: Received signal {sig} on frame {frame}')
    time.sleep(2)  # Give time to the client to recover...
    # traceback.print_stack(frame)
    # sys.exit()   # DTC
    os._exit(1)  # Re-DTC


nb_clients: int = 0
between_loops: float = 1.0  # For ALL the threads.
producing_status: bool = False


def produce_status(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    global keep_listening
    message: dict = {
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
        connection.sendall(payload.encode())
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
    print("New TCP client listener")
    while keep_listening:
        try:
            data: bytes = connection.recv(1_024)   # If receive from client is needed...
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
                    print(">> TCP ZDA received an EXIT message.")
                    # interrupt(None, None)
                    # Send SIGINT Signal to main process
                    os.kill(os.getpid(), signal.SIGINT)
                    # break
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


def produce_zda(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    global keep_listening
    print(f"Connected by client {connection}")
    while keep_listening:
        # data: bytes = conn.recv(1_024)   # If receive from client is needed...
        nmea_zda: str = NMEABuilder.build_ZDA() + NMEA_EOS
        try:
            if not producing_status:
                if verbose:
                    print(f"Producing ZDA and sending: {nmea_zda.strip()}")
                connection.sendall(nmea_zda.encode())  # Send to the client(s), broadcast.
            else:
                print("Waiting for the status production to be completed.")
            time.sleep(between_loops)
        except BrokenPipeError as bpe:
            print("ZDA Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("(ZDA Producer) Oops!...")
            traceback.print_exc(file=sys.stdout)
            nb_clients -= 1
            break  # Client disconnected
    print(f"Exiting producer thread. Done with request(s) from {connection}.\nClosing.")
    connection.close()
    print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")


def main(args: List[str]) -> None:
    global HOST
    global PORT
    global verbose
    global nb_clients
    global keep_listening

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
                threading.Thread(target=produce_zda, args=(conn, addr,))  # Producer
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
