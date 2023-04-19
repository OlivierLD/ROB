#!/bin/bash
#
# Good resource at https://packaging.python.org/en/latest/tutorials/packaging-projects/
#
echo -e "Packaging the NMEA-Parser for Python (WiP)"
echo -e "------------------------------------------"
#
pushd $(dirname $0)/src/main/python
#
echo -e "-- Upgrading build, and building module --"
python3 -m pip install --upgrade build
python3 -m build
# Local deployment:
echo -e "-- Local deployment --"
pip install -e ./   # Seems not to work outside venv...
#
popd
echo -e "-- Done for now --"
