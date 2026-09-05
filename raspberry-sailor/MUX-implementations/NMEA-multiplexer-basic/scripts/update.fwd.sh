#!/bin/bash
#
# Example of forwarder update (enable/disable)
#
echo -e "Getting forwarders list..."
curl -X GET http://localhost:9999/mux/forwarders | jq
#
echo -e "Updating a DataFileWriter..."
curl -X PUT http://localhost:9999/mux/updateforwarder/on -d '{
    "cls": "nmea.forwarders.DataFileWriter",
    "log": "logged/2026-09-04_08-53-15/2026-09-04_09-00-00_UTC_LOG.nmea",
    "append": false,
    "timeBased": true,
    "radix": "_LOG",
    "dir": "logged/2026-09-04_08-53-15",
    "split": "hour",
    "flush": false,
    "zipped": false,
    "active": false,
    "filters": [
        "~GGA",
        "~GSA",
        "~GSV"
    ],
    "type": "file"
}'
#
echo -e "Done."