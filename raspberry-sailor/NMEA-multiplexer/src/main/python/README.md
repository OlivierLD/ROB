# Python NMEA Servers...
This directory contains _**EXAMPLES**_ of the way to have TCP servers written in Python,
that could be used to feed the NMEA-multiplexer.

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
Client sending message: status
Server responded $PYZDA,104559.00,21,12,2022,00,00*76
User Request > loops:0.5
Client sending message: loops:0.5
Server responded $PYZDA,104600.00,21,12,2022,00,00*79
User Request > .
(tcp.clients.SimpleTCPClient) Client exiting
$
```
This produces on the server side:
```
Received from client: b'status\n'
Producing status: {'source': '/. . ./repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py', 'between-loops': 1.0, 'connected-clients': 2, 'python-version': '3.10.8', 'system-utc-time': '2022-12-21T10:46:19.000Z'}

Received STATUS request. Between Loop value: 1.0 s.
```
and
```
Received from client: b'loops:0.5\n'
Producing status: {'source': '/. . ./repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py', 'between-loops': 0.5, 'connected-clients': 2, 'python-version': '3.10.8', 'system-utc-time': '2022-12-21T10:46:30.000Z'}

Received LOOPS:0.5 request. Between Loop value: 0.5 s.
```

---
