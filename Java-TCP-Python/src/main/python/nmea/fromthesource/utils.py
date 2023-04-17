#!/usr/bin/env python3
import math


def dew_point_temperature(hum: float, temp: float) -> float:
    c1: float = 6.10780
    c2: float = 17.08085 if (temp > 0) else 17.84362
    c3: float = 234.175 if (temp > 0) else 245.425

    pz: float = c1 * math.exp((c2 * temp) / (c3 + temp))
    pd: float = pz * (hum / 100)

    dew_point_temp: float = (- math.log(pd / c1) * c3) / (math.log(pd / c1) - c2)

    return dew_point_temp


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
    """
    Convert decimal value like 12.34 into sexagesimal format like N 12°20.40'
    :param value: decimal value to convert, like 12.34
    :param sign_type: the type, "NS" (for latitudes) or "EW" (for longitudes)
    :return the sexagesimal string
    """
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

    air_temp: float = 20.0
    rel_hum: float = 65.0

    dpt = dew_point_temperature(rel_hum, air_temp)
    print(f"Dew Point temp for Air {air_temp}\272C, Hum {rel_hum}% => {dpt}\272C")

    air_temp = 12.34
    rel_hum = 56.78
    dpt = dew_point_temperature(rel_hum, air_temp)
    print(f"Dew Point temp for Air {air_temp}\272C, Hum {rel_hum}% => {dpt}\272C")
