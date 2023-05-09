#!/usr/bin/env python3
#
# Decimal, Octal, Hexa, Binary, etc
# Good doc at https://note.nkmk.me/en/python-bin-oct-hex-int-format/
#
# Sample data at https://github.com/canboat/canboat/blob/master/analyzer/tests/pgn-test.in
#   or in the SignalK repo.
#
# See https://opencpn.org/wiki/dokuwiki/doku.php?id=opencpn:supplementary_software:nmea2000
#
# List of NMEA2000 PGNs, at https://endige.com/2050/nmea-2000-pgns-deciphered/,
#  also at https://github.com/canboat/canboat/blob/master/analyzer/pgns.json
#
from typing import List, Dict

bin_num: int = 0b110111
oct_num: int = 0o1123
hex_num: int = 0x123EF

print(f"Bin: {bin_num}")
print(f"Hex: {hex_num}")
print(f"Oct: {oct_num}")

can_id: int = 0x11F80F00  # CAN Id
print("CAN Id:")
print(f"Original, in Hexa  : {hex(can_id)}")
print(f"          in Octal : {oct(can_id)}")
print(f"          in Binary: {bin(can_id)}")

#
# Can ID: 29 bits
#  3 bits: Priority
# 18 bits: NMEA2000 PGN
#  8 bits: Source address
# 3+18+8 = 29
#

# In binary
binary_can_id: str = bin(can_id)
print(f"Binary Can ID: [{binary_can_id}]")
print(f"    Bits only:   [{binary_can_id[2:]}]")

can_id_bits: str = binary_can_id[2:]
priority_bits: str = can_id_bits[0:3]
print(f"Binary priority: [{priority_bits}], in decimal {int(priority_bits, 2)}")
nmea2000_pgn: str = can_id_bits[3:21]  # 18 bits, like "011111100000001111" 
print(f"NMEA2000 PGN: [{nmea2000_pgn}], in Hexa: {hex(int(nmea2000_pgn, 2))}, decimal: {int(nmea2000_pgn, 2)}")
src_addr: str = can_id_bits[21:29]
print(f"Source Address: [{src_addr}]")
#
print(f"---------------------------------------------------------")
print(f"All bits: [{can_id_bits}]")
print( "           |  |                 |")
print(f"           |  |                [{src_addr}] (src addr)")
print(f"           | [{nmea2000_pgn}] (pgn)")
print(f"          [{priority_bits}] (priority)")
#
print(f"---------------------------------------------------------")


pgn_list: Dict[int, str] = {
    65311:	"Magnetic Variation (Raymarine Proprietary)",
    126992:	"System Time",
    127237:	"Heading/Track Control",
    127245:	"Rudder",
    127250:	"Vessel Heading",
    127251:	"Rate of Turn",
    127258:	"Magnetic Variation",
    127488:	"Engine Parameters, Rapid Update",
    128259:	"Speed, Water referenced",
    128267:	"Water Depth",
    128275:	"Distance Log",
    129025:	"Position, Rapid Update",
    129026:	"COG & SOG, Rapid Update",
    129029:	"GNSS Position Data",
    129033:	"Local Time Offset",
    129044:	"Datum",
    129283:	"Cross Track Error",
    129284:	"Navigation Data",
    129285:	"Navigation — Route/WP information",
    129291:	"Set & Drift, Rapid Update",
    129539:	"GNSS DOPs",
    129540:	"GNSS Sats in View",
    130066:	"Route and WP Service — Route/WP— List Attributes",
    130067:	"Route and WP Service — Route — WP Name & Position",
    130074:	"Route and WP Service — WP List — WP Name & Position",
    130306:	"Wind Data",
    130310:	"Environmental Parameters",
    130311:	"Environmental Parameters",
    130312:	"Temperature",
    130313:	"Humidity",
    130314:	"Actual Pressure",
    130316:	"Temperature, Extended Range",
    129038:	"AIS Class A Position Report",
    129039:	"AIS Class B Position Report",
    129040:	"AIS Class B Extended Position Report",
    129041:	"AIS Aids to Navigation (AtoN) Report",
    129793:	"AIS UTC and Date Report",
    129794:	"AIS Class A Static and Voyage Related Data",
    129798:	"AIS SAR Aircraft Position Report",
    129809:	"AIS Class B “CS” Static Data Report, Part A",
    129810:	"AIS Class B “CS” Static Data Report, Part B"
}

def get_pgn_def(pgn: int) -> str:
    definition: str = "Not found"
    try:
        definition = pgn_list[pgn]
    except Exception as exception:
        definition = "Not Found"
    return definition


def parse_can_record(record: str) -> None:
    data_array: List[str] = record.split(",")
    # print(f"Type: {type(data_array)}")
    # for i in range(len(data_array)):
    #     print(f"{i} -> {data_array[i]}")

    # Assumptions...
    EPOCH: int = 0
    PRIORITY: int = 1
    PGN: int = 2
    SRC: int = 3
    DEST: int = 4
    DATA_BYTES: int = 5
    FIRST_DATA_BYTE: int = 6
    #
    print(f"Epoch: {data_array[EPOCH]}, priority {int(data_array[PRIORITY])}.")
    pgn: int = int(data_array[PGN])
    src: int = int(data_array[SRC])
    dest: int = int(data_array[DEST])
    print(f"Src: {src}, Dest: {dest}, PGN: {pgn} (hexa 0x{hex(pgn).upper()}), ({get_pgn_def(pgn)}).")
    data_bytes: int = int(data_array[DATA_BYTES])
    print(f"{data_bytes} data bytes.")
    if pgn == 127245:  # Rudder, for test
        for i in range(data_bytes):
            print(f"{i}: {data_array[i + FIRST_DATA_BYTE]}")
        # Data: 
        #   Order 1: 'instance'
        #   Order 2: 'directionOrder'
        #   Order 3: reserved
        #   Order 4: 'angleOrder'
        value: int = int(data_array[FIRST_DATA_BYTE + 0], 16)
        print(f"Instance: {value}")
        value = int(data_array[FIRST_DATA_BYTE + 1], 16)
        print(f"directionOrder: {value}")
        value = int(data_array[FIRST_DATA_BYTE + 2], 16)
        print(f"reserved: {value}")
        value = int(data_array[FIRST_DATA_BYTE + 3], 16)
        print(f"angleOrder: {value}")

#
# Sample: 2009-06-18Z09:46:01.129,2,127251,1,255,8,ff,e0,6c,fd,ff,ff,ff,ff
#
sample_record: str = "2009-06-18Z09:46:01.129,2,127251,1,255,8,ff,e0,6c,fd,ff,ff,ff,ff"
parse_can_record(sample_record)
print("----------------------------------")
sample_record_02: str = "2011-04-25-06:25:03.603,3,129029,36,255,43,e6,f1,3a,80,9c,c6,0d,00,12,38,aa,49,eb,51,07,00,0c,44,95,fb,15,b8,00,40,e1,33,00,00,00,00,00,13,fc,09,5a,00,8c,00,ff,ff,ff,7f,00"
parse_can_record(sample_record_02)
print("----------------------------------")
sample_record_03: str = "2022-09-10T12:07:29.542Z,4,129039,23,255,27,12,8a,e4,8d,0e,b4,c4,2a,03,22,d7,88,1f,77,09,75,b4,00,f8,08,00,ff,ff,00,f0,fe,ff"
parse_can_record(sample_record_03)
print("----------------------------------")
sample_record_04: str = "2017-03-13T01:00:00.146Z,2,127245,204,255,8,fc,f8,ff,7f,ff,7f,ff,ff"
parse_can_record(sample_record_04)
print("----------------------------------")
