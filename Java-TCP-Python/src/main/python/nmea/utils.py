#!/usr/bin/env python3

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
