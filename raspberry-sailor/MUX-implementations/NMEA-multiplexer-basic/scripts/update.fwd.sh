#!/bin/bash
#
# Example of forwarder update (enable/disable)
#
function nocase() {
  if [ "`echo $1 | tr [:lower:] [:upper:]`" = "`echo $2 | tr [:lower:] [:upper:]`" ]
  then
    return 0 # true
  else
    return 1 # false
  fi
}
#
echo -e "Getting forwarders list..."
curl -X GET http://localhost:9999/mux/forwarders | jq
#
echo -e "Updating a DataFileWriter..."
echo -e ""
echo -en "On or Off ? > "
read -r REPLY
if nocase "$REPLY" "ON"; then
  echo -e "Turning it on"
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
else  # OFF
  echo -e "Turning it off"
  curl -X PUT http://localhost:9999/mux/updateforwarder/off -d '{
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
fi
#
echo -e "Done."