# Python NMEA Servers...
This directory contains _**EXAMPLES**_ of the way to have TCP servers written in Python,
that could be used to feed the NMEA-multiplexer.  
The code Python in this folder is usually a wrapper around the Python modules written by the sensor provider.
The code provided in this folder requires those modules to be installed first (with `pip3` or similar tools). This will be explained.

The Python code reads the sensor's data, and builds appropriate NMEA sentences to carry them.

- `ZDA`: Time & Date - UTC, day, month, year and local time zone 
  - that one does not need a sensor.
- `XDR`: Transducer Measurement. Can hold (among many others) Temperature, Pressure, Humidity
  - Produced by BMP180, BME280,
- `MTA`: Air Temperature, Celsius
  - Produced by BMP180, BME280
- `MMB`: Atm Pressure
    - Produced by BMP180, BME280
- `HDM`, `HDG`: Heading
  - Produced by LSM303, HCM5883L, LIS3DML

## Example 1, produce and consume ZDA Sentences
To start the Python ZDA server
```
$ pwd
.../ROB/raspberry-sailor/NMEA-multiplexer
$ python3 src/main/python/TCP_ZDA_server.py --port:7001
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
and you should get the following output, in the console, as expected:
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
To see if the Python server is running:
```
$ ps -ef | grep TCP_ZDA_server | grep -v grep
  501 29063  1322   0 11:58AM ttys011    0:00.07 /. . ./Python src/main/python/TCP_ZDA_server.py --port:7001 --verbose:true
$
```

#### Server interaction
You can interact with the server, using a TCP client.  
Look into the `Java-TCP-Python` module (in this project).  
You can change the ZDA sentence production frequency, and get the status of the server.

```
$ ./start.tcp.client.sh --port:7001
(tcp.clients.SimpleTCPClient) Port now set to 7001
(tcp.clients.SimpleTCPClient) Enter '.' at the prompt to stop. Any non-empty string otherwise.
User Request > status
>> Server responded {"source": "/Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py", "between-loops": 10.0, "connected-clients": 1, "python-version": "3.10.8", "system-utc-time": "2022-12-21T12:30:10.000Z"}
- Key: source, Value (java.lang.String): /Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py
- Key: between-loops, Value (java.lang.Double): 10.0
- Key: connected-clients, Value (java.lang.Integer): 1
- Key: python-version, Value (java.lang.String): 3.10.8
- Key: system-utc-time, Value (java.lang.String): 2022-12-21T12:30:10.000Z
User Request > loops:0.5
>> Server responded {"source": "/Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py", "between-loops": 0.5, "connected-clients": 1, "python-version": "3.10.8", "system-utc-time": "2022-12-21T12:30:30.000Z"}
- Key: source, Value (java.lang.String): /Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py
- Key: between-loops, Value (java.lang.Double): 0.5
- Key: connected-clients, Value (java.lang.Integer): 1
- Key: python-version, Value (java.lang.String): 3.10.8
- Key: system-utc-time, Value (java.lang.String): 2022-12-21T12:30:30.000Z
User Request > .
(tcp.clients.SimpleTCPClient) Client exiting
Done with dummy reader
User Request > .
(tcp.clients.SimpleTCPClient) Client exiting
$
```

---
## Misc links
- <https://docs.circuitpython.org/projects/lsm303/en/latest/_modules/adafruit_lsm303.html>
- 

---
