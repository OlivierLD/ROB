#!/usr/bin/env python3

import checksum  # local script
import utils  # local script
import json
from typing import Dict  # , List, Set, Tuple, Optional

NMEA_EOS: str = '\r\n'
DEBUG: bool = False


# TODO XDR, GSA, GGA, VTG, GSV ...


def mda_parser(sentence: str) -> Dict[str, Dict]:
    """
    MDA Meteorological Composite
    Structure is
    $--MDA,x.x,I,x.x,B,x.x,C,x.x,C,x.x,x.x,x.x,C,x.x,T,x.x,M,x.x,N,x.x,M*hh<CR><LF>
           |     |     |     |     |   |   |     |     |     |     |
           |     |     |     |     |   |   |     |     |     |     19-Wind speed, m/s
           |     |     |     |     |   |   |     |     |     17-Wind speed, knots
           |     |     |     |     |   |   |     |     15-Wind dir Mag
           |     |     |     |     |   |   |     13-Wind dir, True
           |     |     |     |     |   |   11-Dew Point C
           |     |     |     |     |   10-Absolute hum %
           |     |     |     |     9-Relative hum %
           |     |     |     7-Water temp in Celsius
           |     |     5-Air Temp in Celsius  |
           |     3-Pressure in Bars
           1-Pressure in inches
    Example: $WIMDA,29.4473,I,0.9972,B,17.2,C,,,,,,,,,,,,,,*3E
    """
    PRESS_INCH: int = 1
    PRESS_BAR: int = 3
    AIR_T: int = 5
    WATER_T: int = 7
    REL_HUM: int = 9
    ABS_HUM: int = 10
    DEW_P_C: int = 11
    WD_T: int = 13
    WD_M: int = 15
    WS_KNOTS: int = 17
    WS_MS: int = 19
    mda_dict: Dict = {"source": sentence}
    sentence = sentence.strip()  # drops the \r\n
    members: list = sentence[:sentence.index("*")].split(',')

    for idx in range(1, len(members)):
        if len(members[idx].strip()) > 0:
            if idx == PRESS_INCH:
                val: float = float(members[idx].strip())
                mda_dict["press-inch"] = val
            elif idx == PRESS_BAR:
                val: float = float(members[idx].strip())
                mda_dict["press-bar"] = val
            elif idx == AIR_T:
                val: float = float(members[idx].strip())
                mda_dict["air-temp"] = val
            elif idx == WATER_T:
                val: float = float(members[idx].strip())
                mda_dict["water-temp"] = val
            elif idx == REL_HUM:
                val: float = float(members[idx].strip())
                mda_dict["rel-hum"] = val
            elif idx == ABS_HUM:
                val: float = float(members[idx].strip())
                mda_dict["abs-hum"] = val
            elif idx == DEW_P_C:
                val: float = float(members[idx].strip())
                mda_dict["dew-point-c"] = val
            elif idx == WD_T:
                val: float = float(members[idx].strip())
                mda_dict["wind-dir-true"] = val
            elif idx == WD_M:
                val: float = float(members[idx].strip())
                mda_dict["wind-dir-mag"] = val
            elif idx == WS_KNOTS:
                val: float = float(members[idx].strip())
                mda_dict["wind-speed-knots"] = val
            elif idx == WS_MS:
                val: float = float(members[idx].strip())
                mda_dict["wind-speed-ms"] = val
    return {"mda": mda_dict}


def rmc_parser(sentence: str) -> Dict[str, Dict]:
    """
    Recommended Minimum Navigation Information, C
    RMC Structure is
                                                                         12
              1      2 3        4 5         6 7     8     9      10    11
       $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,T*6A
              |      | |        | |         | |     |     |      |     | |
              |      | |        | |         | |     |     |      |     | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator. Not mandatory
              |      | |        | |         | |     |     |      |     Variation sign
              |      | |        | |         | |     |     |      Variation value
              |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
              |      | |        | |         | |     COG
              |      | |        | |         | SOG
              |      | |        | |         Longitude Sign
              |      | |        | Longitude Value
              |      | |        Latitude Sign
              |      | Latitude value
              |      Active or Void
              UTC
    
    :param sentence: Original sentence, source
    :return: A Dict containing parsed elements
    """
    RMC_UTC: int = 1
    RMC_ACTIVE_VOID: int = 2
    RMC_LATITUDE_VALUE: int = 3
    RMC_LATITUDE_SIGN: int = 4
    RMC_LONGITUDE_VALUE: int = 5
    RMC_LONGITUDE_SIGN: int = 6
    RMC_SOG: int = 7
    RMC_COG: int = 8
    RMC_DDMMYY: int = 9
    RMC_VARIATION_VALUE: int = 10
    RMC_VARIATION_SIGN: int = 11
    RMC_TYPE: int = 12

    rmc_dict: Dict = {"source": sentence}
    sentence = sentence.strip()  # drops the \r\n
    members: list = sentence[:sentence.index("*")].split(',')

    if len(members[RMC_ACTIVE_VOID]) > 0 and members[RMC_ACTIVE_VOID] == 'A':
        if len(members[RMC_UTC]) > 0 and len(members[RMC_DDMMYY]) > 0:
            year: int = int(members[RMC_DDMMYY][4:])
            if year < 50:
                year = 2000 + year
            else:
                year = 1900 + year
            month: int = int(members[RMC_DDMMYY][2:4])
            day: int = int(members[RMC_DDMMYY][0:2])
            hour: int = int(members[RMC_UTC][0:2])
            minute: int = int(members[RMC_UTC][2:4])
            second: float = float(members[RMC_UTC][4:])
            rmc_dict["utc"] = {
                "year": year,
                "month": month,
                "day": day,
                "hour": hour,
                "minute": minute,
                "second": second
            }
        if len(members[RMC_LATITUDE_VALUE]) > 0 and len(members[RMC_LATITUDE_SIGN]) > 0 and len(
                members[RMC_LONGITUDE_VALUE]) > 0 and len(members[RMC_LONGITUDE_SIGN]) > 0:
            deg: str = members[RMC_LATITUDE_VALUE][0:2]
            min: str = members[RMC_LATITUDE_VALUE][2:]
            lat: float = utils.sex_to_dec(deg, min)
            if members[RMC_LATITUDE_SIGN] == 'S':
                lat = -lat
            deg = members[RMC_LONGITUDE_VALUE][0:3]
            min = members[RMC_LONGITUDE_VALUE][3:]
            lng: float = utils.sex_to_dec(deg, min)
            if members[RMC_LONGITUDE_SIGN] == 'W':
                lng = -lng
            rmc_dict["pos"] = {
                "latitude": lat,
                "longitude": lng
            }
        if len(members[RMC_SOG]) > 0:
            sog: float = float(members[RMC_SOG])
            rmc_dict["sog"] = sog
        if len(members[RMC_COG]) > 0:
            cog: float = float(members[RMC_COG])
            rmc_dict["cog"] = cog
        if len(members[RMC_VARIATION_VALUE]) > 0:
            variation: float = float(members[RMC_VARIATION_VALUE])
            if members[RMC_VARIATION_SIGN] == 'W':
                variation = -variation
            rmc_dict["variation"] = variation
        if len(members) > RMC_TYPE:
            rmc_type: str = members[RMC_TYPE]
            type_val: str = ""
            # print(f"Type->{rmc_type}")
            if rmc_type is not None:
                if rmc_type == 'A':
                    type_val = "autonomous"
                elif rmc_type == 'D':
                    rmc_type = "differential"
                elif rmc_type == 'E':
                    type_val = "estimated"
                elif rmc_type == 'N':
                    type_val = "not valid"
                elif rmc_type == 'S':
                    type_val = "simulator"
                else:
                    type_val = f"unknown [{rmc_type}]"
                rmc_dict['type'] = type_val

    else:
        rmc_dict["status"] = "void"

    return {"rmc": rmc_dict}


def gsa_parser(sentence: str) -> Dict[str, str]:
    return {"gsa": sentence}


def gll_parser(sentence: str) -> Dict[str, Dict]:
    """
    Geographical Lat & Long

    Structure is
           1       2 3       4 5         6 7
    $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A,D*hh
           |       | |       | |         | |
           |       | |       | |         | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator (not always there)
           |       | |       | |         A:data valid (Active), V: void
           |       | |       | UTC of position
           |       | |       Long sign :E/W
           |       | Longitude
           |       Lat sign :N/S
           Latitude
    
    :param sentence: Original sentence, source
    :return: A Dict containing parsed elements
    """
    GLL_LATITUDE_VALUE: int = 1
    GLL_LATITUDE_SIGN: int = 2
    GLL_LONGITUDE_VALUE: int = 3
    GLL_LONGITUDE_SIGN: int = 4
    GLL_UTC: int = 5
    GLL_ACTIVE_VOID: int = 6
    GLL_TYPE: int = 7

    gll_dict: Dict = {"source": sentence}
    sentence = sentence.strip()  # drops the \r\n
    members: list = sentence[:sentence.index("*")].split(',')

    if len(members[GLL_ACTIVE_VOID]) > 0 and members[GLL_ACTIVE_VOID] == 'A':
        if len(members[GLL_UTC]) > 0:
            hour: int = int(members[GLL_UTC][0:2])
            minute: int = int(members[GLL_UTC][2:4])
            second: float = float(members[GLL_UTC][4:])
            gll_dict["utc"] = {
                "hour": hour,
                "minute": minute,
                "second": second
            }
        if len(members[GLL_LATITUDE_VALUE]) > 0 and len(members[GLL_LATITUDE_SIGN]) > 0 and len(
                members[GLL_LONGITUDE_VALUE]) > 0 and len(members[GLL_LONGITUDE_SIGN]) > 0:
            deg: str = members[GLL_LATITUDE_VALUE][0:2]
            min: str = members[GLL_LATITUDE_VALUE][2:]
            lat: float = utils.sex_to_dec(deg, min)
            if members[GLL_LATITUDE_SIGN] == 'S':
                lat = -lat
            deg = members[GLL_LONGITUDE_VALUE][0:3]
            min = members[GLL_LONGITUDE_VALUE][3:]
            lng: float = utils.sex_to_dec(deg, min)
            if members[GLL_LONGITUDE_SIGN] == 'W':
                lng = -lng
            gll_dict["pos"] = {
                "latitude": lat,
                "longitude": lng
            }
        if len(members) > GLL_TYPE:
            gll_type: str = members[GLL_TYPE]
            type_val: str = ""
            # print(f"Type->{gll_type}")
            if gll_type is not None:
                if gll_type == 'A':
                    type_val = "autonomous"
                elif gll_type == 'D':
                    type_val = "differential"
                elif gll_type == 'E':
                    type_val = "estimated"
                elif gll_type == 'N':
                    type_val = "not valid"
                elif gll_type == 'S':
                    type_val = "simulator"
                else:
                    type_val = f"unknown [{gll_type}]"
                gll_dict['type'] = type_val
    else:
        gll_dict["status"] = "void"

    return {"gll": gll_dict}


def gga_parser(sentence: str) -> Dict[str, str]:
    return {"gga": sentence}


def vtg_parser(sentence: str) -> Dict[str, str]:
    return {"vtg": sentence}


def gsv_parser(sentence: str) -> Dict[str, str]:
    return {"gsv": sentence}


def zda_parser(sentence: str) -> Dict[str, Dict]:
    """
    UTC DCate and Time

    Structure is
    $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
           1         2  3  4
    $GPZDA,201530.00,04,07,2002,00,00*60
           |         |  |  |    |  |
           |         |  |  |    |  local zone minutes 0..59
           |         |  |  |    local zone hours -13..13
           |         |  |  year
           |         |  month
           |         day
           HrMinSec(UTC)

    :param sentence: Original sentence, source
    :return: A Dict containing parsed elements
    """
    ZDA_UTC: int = 1
    ZDA_DAY: int = 2
    ZDA_MONTH: int = 3
    ZDA_YEAR: int = 4
    ZDA_LOCAL_ZONE_HOURS: int = 5
    ZDA_LOCAL_ZONE_MINUTES: int = 6

    zda_dict: Dict = {"source": sentence}
    sentence = sentence.strip()  # drops the \r\n
    members: list = sentence.split(',')

    if len(members[ZDA_UTC]) > 0:
        hour: int = int(members[ZDA_UTC][0:2])
        minute: int = int(members[ZDA_UTC][2:4])
        second: float = float(members[ZDA_UTC][4:])
        zda_dict["utc"] = {
            "hour": hour,
            "minute": minute,
            "second": second
        }
    if len(members[ZDA_DAY]) > 0:
        day: int = int(members[ZDA_DAY])
        zda_dict["utc"]["day"] = day
    if len(members[ZDA_MONTH]) > 0:
        month: int = int(members[ZDA_MONTH])
        zda_dict["utc"]["month"] = month
    if len(members[ZDA_YEAR]) > 0:
        year: int = int(members[ZDA_YEAR])
        zda_dict["utc"]["year"] = year

    return {"zda": zda_dict}


NMEA_PARSER_DICT: Dict = {
    "GLL": gll_parser,
    "GSA": gsa_parser,
    "RMC": rmc_parser,
    "GGA": gga_parser,
    "VTG": vtg_parser,
    "GSV": gsv_parser,
    "ZDA": zda_parser,
    "MDA": mda_parser
}


def parse_nmea_sentence(sentence: str) -> Dict:
    nmea_dict: Dict = {}
    if sentence.startswith('$'):
        if sentence.endswith(NMEA_EOS):
            sentence = sentence.strip()  # drops the \r\n
            members: list = sentence.split(',')
            # print(f"members is a {type(members)}")
            if DEBUG:
                print("Split: {}".format(members))
            sentence_prefix: str = members[0]  # $TTIII
            if len(sentence_prefix) == 6:
                # print("Sentence ID: {}".format(sentence_prefix))
                valid: bool = checksum.valid_check_sum(sentence, True)
                if not valid:
                    raise Exception('Invalid checksum')
                else:
                    sentence_id: str = sentence_prefix[3:]
                    parser = None
                    for key in NMEA_PARSER_DICT:
                        if key == sentence_id:
                            parser = NMEA_PARSER_DICT[key]  # TODO type 'function'
                            # print(f"parser is a {type(parser)}")
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


# This is for tests
if __name__ == '__main__':
    nmea: str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"
    # nmea: str = "$GPRMC,170000.00,A,3744.79693,N,12223.30420,W,0.052,,200621,,,D*62"
    parsed: Dict = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed RMC: {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    nmea: str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,S*15"
    # nmea: str = "$GPRMC,170000.00,A,3744.79693,N,12223.30420,W,0.052,,200621,,,D*62"
    parsed: Dict = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed RMC (2): {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    nmea = "$IIGLL,3739.854,N,12222.812,W,014003,A,A*49"
    parsed = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed GLL: {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    nmea = "$IIGLL,3739.854,N,12222.812,W,014003,A*24"
    parsed = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed GLL (2): {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    nmea = "$GPZDA,201530.00,04,07,2002,00,00*60"
    parsed = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed ZDA: {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    nmea = "$WSMDA,30.029,I,1.017,B,16.6,C,,,66.0,,,,12,T,,,3.1,N,1.6,M*6B"
    parsed = parse_nmea_sentence(nmea + NMEA_EOS)
    print(f"Parsed MDA: {parsed}")
    print(f"Beautified:\n{json.dumps(parsed, sort_keys=False, indent=2)}")

    print("---------------")

    try:
        nmea = "GPXXX,AKEU,COUCOU*12"
        parsed = parse_nmea_sentence(nmea + NMEA_EOS)
        print(f"Parsed Data: {parsed}")
    except Exception as ex:
        print(f"Exception: {ex}")

    print("---------------")

    try:
        nmea = "$GPXXX,AKEU,COUCOU*12"
        parsed = parse_nmea_sentence(nmea + NMEA_EOS)
        print(f"Parsed Data: {parsed}")
    except Exception as ex:
        print(f"Exception: {ex}")
