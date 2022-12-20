# Python NMEA Servers...
This directory contains _**EXAMPLES**_ of the way to have TCP servers written in Python,
that could be used to feed the NMEA-multiplexer.

## Example 1, produce and consume ZDA Sentences

To start the Python ZDA server
```
python3 src/main/python/TCP_ZDA_server.py --port:7001
```
Then, start the NMEA-multiplexer with the following config file:
```yaml
#
# MUX definition.
# With Python TCP Server input, on port 7001
#
name: "With TCP server, in Python"
description:
  - Requires the Python server to be running
  - python3 src/main/python/TCP_ZDA_server.py --port:7001
context:
  with.http.server: true
  http.port: 9999
  init.cache: true
channels:
  # ZDA Data, as per the above.
  - type: tcp
    server: localhost
    port: 7001
    verbose: false
forwarders:
  - type: console
```
as in
```
./mux.sh mux-configs/nmea.mux.python.channel.yaml 
```
and you should bet the following output:
```
Running java  -Djava.library.path=/Library/Java/Extensions -Dscreen.verbose=true -Drest.feeder.verbose=true -Dparse.ais=true -Dmux.props.verbose=true -Dprocess.on.start=true -Dmux.properties=mux-configs/nmea.mux.python.channel.yaml -Dno.ais=false -Dcalculate.solar.with.eot=true -Ddefault.mux.latitude=37.8218 -Ddefault.mux.longitude=-122.3112 -Dtry.to.speak=true -Djava.util.logging.config.file=./logging.properties   -cp ./build/libs/NMEA-multiplexer-1.0-all.jar:../../Serial-IO/libs/RXTXcomm.jar nmea.mux.GenericNMEAMultiplexer 
{with.http.server=true, http.port=9999, init.cache=true}
Definition Name: With TCP server, in Python
-- Description --
Requires the Python server to be running
python3 src/main/python/TCP_ZDA_server.py --port:7001
-----------------
Log available in global, level INFO
Log file pattern mux.log
Starting new http.HTTPServer (verbose false)
1,671,534,091,318 - Port open: 9999
1,671,534,091,318 - http.HTTPServer now accepting requests
$PYZDA,110131.00,20,12,2022,00,00*78
$PYZDA,110132.00,20,12,2022,00,00*7B
$PYZDA,110133.00,20,12,2022,00,00*7A
$PYZDA,110134.00,20,12,2022,00,00*7D
$PYZDA,110135.00,20,12,2022,00,00*7C
$PYZDA,110136.00,20,12,2022,00,00*7F
. . .
```