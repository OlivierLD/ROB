#!/usr/bin/env python3
#
# This is an example of the way to read GPS data from a Serial Port, and parse them.
#
# To read a serial port:
# http://www.elinux.org/Serial_port_programming
# sudo apt-get install python-serial
# or
# pip3 install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
# Read Serial port, parse NMEA Data.
#
# Supported CLI prms: --baud-rate:4800 --port-name:/dev/ttyS80 --proceed-with-parser:Y --verbose:Y|N
#       like:  python3 NMEASerialTest.py --port-name:/dev/tty.usbmodem142201
#
# To see below:
# - DEBUG
# - DEEP_DEBUG
# - DISPLAY_ALL
#
import serial
import sys
import os
from typing import Dict, List  # , Set, Tuple, Optional

# Local scripts
import NMEAParser
import checksum
import utils

__version__ = "0.0.1"
__repo__ = "https://github.com/OlivierLD/ROB"

DEBUG: bool = False
DEEP_DEBUG: bool = False
DISPLAY_ALL: bool = False

#
# Basic parsers, in NMEAParser.py
#
NMEA_PARSER_DICT: Dict = NMEAParser.NMEA_PARSER_DICT


# On mac, USB GPS on port /dev/tty.usbmodem14101 (or something close).
# Raspberry Pi, use /dev/ttyUSB0 or so.
port_name: str = "/dev/tty.usbmodem1414101"   # "/dev/tty.usbmodem141101"
baud_rate: int = 4800
# port_name = "/dev/ttyACM0"
# baud_rate = 115200

proceed_parser: bool = True


def display_help() -> None:
    print("Usage is:")
    print(f"python3 {os.path.basename(__file__)} --baud-rate:4800 --port-name:/dev/ttyS80 --proceed-with-parser:Y|N --verbose:Y|N --help")
    print("Default baud rate is 4800")
    print("Default port name is /dev/tty.usbmodem1414101 (do check in the code...)")
    print("Default proceed-with-parser is Y")
    print("Default verbose is N")
    print("Warning: this script has external (local) dependencies")


def read_nmea_sentence(serial_port: serial.serialposix.Serial) -> str:
    """
    Reads the serial port until a '\n' is met.
    :param serial_port: the port, as returned by serial.Serial
    :return: the full NMEA String, with its EOL '\r\n'
    """
    rv = []
    while True:
        ch = serial_port.read()
        if DEEP_DEBUG:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if DEEP_DEBUG:
                print("Returning {}".format(string))
            return string


def parse_nmea_sentence(sentence: str) -> Dict:
    nmea_dict: Dict = {}
    if sentence.startswith('$'):
        if sentence.endswith(NMEAParser.NMEA_EOS):
            sentence = sentence.strip()  # drops the \r\n
            members = sentence.split(',')
            # print("Split: {}".format(members))
            sentence_prefix = members[0]
            if len(sentence_prefix) == 6:
                # print("Sentence ID: {}".format(sentence_prefix))
                valid = checksum.valid_check_sum(sentence)
                if not valid:
                    raise Exception('Invalid checksum')
                else:
                    sentence_id: str = sentence_prefix[3:]
                    parser = None
                    for key in NMEA_PARSER_DICT:
                        if key == sentence_id:
                            parser = NMEA_PARSER_DICT[key]
                            break
                    if parser is None:
                        raise Exception("No parser exists for {}".format(sentence_id))
                    else:
                        if DEBUG:
                            print("Proceeding... {}".format(sentence_id))
                        nmea_dict = parser(sentence)
                        if DEBUG:
                            print("Parsed: {}".format(nmea_dict))
            else:
                raise Exception('Incorrect sentence prefix "{}". Should be 6 character long.'.format(sentence_prefix))
        else:
            raise Exception('Sentence should end with \\r\\n')
    else:
        raise Exception('Sentence should start with $')
    return nmea_dict


PORT_NAME_PREFIX: str = "--port-name:"
BAUD_RATE_PREFIX: str = "--baud-rate:"
PROCEED_WITH_PARSER: str = "--proceed-with-parser:"
VERBOSE_PREFIX: str = "--verbose:"
HELP_PREFIX: str = "--help"  # No value required.


def main(args: List[str]) -> None:
    global port_name
    global baud_rate
    global proceed_parser
    global DEBUG

    if len(args) > 0:  # Script name + X args
        for arg in args:
            if arg[:len(PORT_NAME_PREFIX)] == PORT_NAME_PREFIX:
                port_name = arg[len(PORT_NAME_PREFIX):]
            if arg[:len(BAUD_RATE_PREFIX)] == BAUD_RATE_PREFIX:
                baud_rate = int(arg[len(BAUD_RATE_PREFIX):])
            if arg[:len(PROCEED_WITH_PARSER)] == PROCEED_WITH_PARSER:
                proceed_flag = arg[len(PROCEED_WITH_PARSER):]
                if proceed_flag.upper() == 'N' or proceed_flag.upper() == 'NO' or proceed_flag.upper() == 'FALSE':
                    proceed_parser = False
            if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
                DEBUG = (arg[len(VERBOSE_PREFIX):].upper() == 'Y')
            if arg[:len(HELP_PREFIX)] == HELP_PREFIX:
                # Display help and exit
                display_help()
                exit(0)

    print(f"Will read {port_name}:{baud_rate}...")
    try:
        port: int = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
    except FileNotFoundError as fnfe:
        print(f">> No such port {port_name}. Exiting.")
        sys.exit(1)
    except serial.serialutil.SerialException as se:
        print(f">> No such port {port_name}. Exiting.")
        sys.exit(1)

    print("Let's go. Hit Ctrl+C to stop")
    keep_looping: bool = True
    while keep_looping:
        try:
            rcv: str = read_nmea_sentence(port)
            if DEBUG or not proceed_parser:
                print("\tReceived raw:" + repr(rcv))  # repr: displays also non-printable characters between quotes.
            if proceed_parser:
                try:
                    nmea_obj: dict = parse_nmea_sentence(rcv)
                    sentence_id: str = rcv[3:6]
                    # print(f"Raw: {repr(rcv)}\n\tParsed: {nmea_obj}")
                    if sentence_id == 'GLL':
                        # {'gll': {'source': '$GNGLL,4740.66861,N,00308.13866,W,141439.00,A,A*66', 'utc': {'hour': 14, 'minute': 14, 'second': 39.0}, 'pos': {'latitude': 47.67781016666667, 'longitude': -3.1356443333333335}}}
                        print("-- GLL --")
                        print(f"Position: {utils.dec_to_sex(nmea_obj['gll']['pos']['latitude'], 'NS')} / {utils.dec_to_sex(nmea_obj['gll']['pos']['longitude'], 'EW')}")
                        print(f"UTC Time: {nmea_obj['gll']['utc']['hour']}:{nmea_obj['gll']['utc']['minute']}:{nmea_obj['gll']['utc']['second']}")
                    elif sentence_id == 'RMC':
                        # {'rmc': {'source': '$GNRMC,141440.00,A,4740.66813,N,00308.13792,W,0.211,,280323,,,A*7A', 'utc': {'year': 2023, 'month': 3, 'day': 28, 'hour': 14, 'minute': 14, 'second': 40.0}, 'pos': {'latitude': 47.677802166666666, 'longitude': -3.135632}, 'sog': 0.211}}
                        print("-- RMC --")
                        print(f"Position: {utils.dec_to_sex(nmea_obj['rmc']['pos']['latitude'], 'NS')} / {utils.dec_to_sex(nmea_obj['rmc']['pos']['longitude'], 'EW')}")
                        print(f"UTC DateTime: {nmea_obj['rmc']['utc']['year']}-{nmea_obj['rmc']['utc']['month']:02d}-{nmea_obj['rmc']['utc']['day']:02d} {nmea_obj['rmc']['utc']['hour']:02d}:{nmea_obj['rmc']['utc']['minute']:02d}:{nmea_obj['rmc']['utc']['second']}")
                        print(f"Speed & Course: SOG: {nmea_obj['rmc']['sog']}\272 - COG {nmea_obj['rmc']['cog'] if 'cog' in nmea_obj['rmc'].keys() else '-'} kt")
                    elif DISPLAY_ALL:
                        print("---------")
                        print(f"Raw: {repr(rcv)}\n\tParsed: {nmea_obj}")
                except KeyboardInterrupt:  # Ctrl-C
                    keep_looping = False
                    print("\nExiting at user's request")
                    pass
                except Exception as ex:
                    print("Oops! {}".format(ex))
        except KeyboardInterrupt:  # Ctrl-C
            keep_looping = False
            print("\nExiting at user's request")
            pass

    print("\nBye!")


if __name__ == '__main__':
    main(sys.argv)
