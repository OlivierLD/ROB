#!/usr/bin/env python3
"""
That one produces ZDA Strings for each connected client.
It also understands input from the client: "STATUS", "SLOWER", "FASTER", or "TERMINATE" (not case sensitive), see client_listener.

Start it with
$ python src/main/python/nmea/TCP_ZDA_server.py --port:7002 --verbose:true
"""
import sys
import os
import json
import signal
import time
import socket
import threading
import traceback
import platform
from datetime import datetime, timezone
import logging
from logging import info
import NMEABuilder as NMEABuilder   # local script
from typing import List, Dict

keep_listening: bool = True

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
verbose: bool = True

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

CLIENT_CMD_FASTER: str = "FASTER"
CLIENT_CMD_SLOWER: str = "SLOWER"
CLIENT_CMD_STATUS: str = "STATUS"
CLIENT_CMD_TERMINATE: str = "TERMINATE"

NMEA_EOS: str = "\r\n"  # aka CR-LF


logging.basicConfig(level=logging.INFO, format='\n%(message)s')


def interrupt(sig: int, frame):
    # print(f"Signal: {type(sig)}, frame: {type(frame)}")
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    if sig is not None:
        info(f'sigint_handler: Received signal {sig} on frame {frame}')
    # traceback.print_stack(frame)
    sys.exit()   # DTC


nb_clients: int = 0
between_loops: float = 1.0  # For ALL the threads.
producing_status: bool = False


def produce_status(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    message: Dict = {
        "source": __file__,
        "between-loops": between_loops,
        "connected-clients": nb_clients,
        "python-version": platform.python_version(),
        "system-utc-time": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")
    }
    try:
        payload: str = str(json.dumps(message)) + NMEA_EOS
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
    Expects three possible inputs: "STATUS", "SLOWER", or "FASTER" (not case-sensitive).
    """
    global nb_clients
    global between_loops
    print("New client listener")
    while True:
        try:
            data: bytes = connection.recv(1024)   # If receive from client is needed...
            if verbose:
                print(f"Received from client: {data}")
            client_mess: str = f"{data.decode('utf-8')}".strip().upper()
            if  client_mess == CLIENT_CMD_FASTER:
                between_loops /= 2.0
            elif client_mess == CLIENT_CMD_SLOWER:
                between_loops *= 2.0
            elif client_mess == CLIENT_CMD_STATUS:
                produce_status(connection, address)
            elif client_mess == CLIENT_CMD_TERMINATE:
                # Send SIGINT Signal to main process
                os.kill(os.getpid(), signal.SIGINT)
                break
            elif client_mess == "":
                pass  # ignore
            else:
                print(f"Unknown or un-managed message [{client_mess}]")
            if len(client_mess) > 0:
                print(f"Received {client_mess} request. Between Loop value: {between_loops} s.")
        except BrokenPipeError as bpe:
            print("Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("Oops!...")
            traceback.print_exc(file=sys.stdout)
            break  # Client disconnected
    print("Exiting client listener")


def produce_zda(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global between_loops
    global producing_status
    print(f"Connected by client {connection}")
    while True:
        # data: bytes = conn.recv(1024)   # If receive from client is needed...
        nmea_zda: str = NMEABuilder.build_ZDA() + NMEA_EOS
        try:
            if not producing_status:
                if verbose:
                    print(f"Producing ZDA and sending: {nmea_zda}")
                connection.sendall(nmea_zda.encode())  # Send to the client
            else:
                print("Waiting for the status to be completed.")
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
        s.bind((HOST, PORT))
        s.listen()
        print("Server is listening. [Ctrl-C] will stop the process.")
        while keep_listening:
            conn, addr = s.accept()
            # print(f">> New accept: Conn is a {type(conn)}, addr is a {type(addr)}")
            nb_clients += 1
            print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")
            # Generate ZDA sentences for this client in its own thread.
            client_thread = threading.Thread(target=produce_zda, args=(conn, addr,))  # Producer
            client_thread.daemon = True  # Dies on exit
            client_thread.start()

            client_listener_thread = threading.Thread(target=client_listener, args=(conn, addr,))  # Listener
            client_listener_thread.daemon = True  # Dies on exit
            client_listener_thread.start()

    print("Exiting server")
    print("Bon. OK.")


if __name__ == '__main__':
    main(sys.argv)
