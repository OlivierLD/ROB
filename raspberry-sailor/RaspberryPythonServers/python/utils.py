#!/usr/bin/env python3
import math


def sex_to_dec(degrees: str, minutes: str) -> float:
    """
    Convert a sexagesimal number (degrees, decimal minutes) into a decimal number
    :param degrees: degrees, usually an int, entered as a string, like "37".
    :param minutes: decimal minutes, entered as a string, like "12.34"
    :return: the converted value, as a float, like 37.205666
    """
    try:
        deg: float = float(degrees)
        min: float = float(minutes)
        min *= (10.0 / 6.0)
        ret = deg + (min / 100.0)
        return ret
    except Exception as error:
        raise Exception(f"Cannot convert {degrees} {minutes}, {repr(error)}")


#
# sign_type is "NS" or "EW". Default is NS
#
def dec_to_sex(value: float, sign_type: str) -> str:
    abs_val = abs(value)
    int_value: float = math.floor(abs_val)
    dec: float = abs_val - int_value
    i: int = int(int_value)
    dec *= 60
    converted: str = f"{i}°{dec:02.2f}'"
    if value < 0:
        if sign_type is None or sign_type == "NS":
            converted = "S " + converted
        elif sign_type == "EW":
            converted = "W " + converted
    else:
        if sign_type is None or sign_type == "NS":
            converted = "N " + converted
        elif sign_type == "EW":
            converted = "E " + converted
    return converted


# This is for tests
if __name__ == '__main__':
    deg: str = "37"
    min: str = "12.34"
    print(f"{deg} {min} => {sex_to_dec(deg, min)}")
    min = "72.34"
    print(f"{deg} {min} => {sex_to_dec(deg, min)}")
    deg = "Oops"
    try:
        print(f"{deg} {min} => {sex_to_dec(deg, min)}")
    except Exception as error:
        print(f"{repr(error)}")

    try:
        dec: float = 37.20566666
        print(f"{dec} => {dec_to_sex(dec, 'NS')}")
    except Exception as oops:
        print(f"{repr(oops)}")
