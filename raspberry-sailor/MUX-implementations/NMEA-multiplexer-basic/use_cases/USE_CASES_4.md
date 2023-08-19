# Use-case 4. Basic Weather Station
### A Raspberry Pi, with a BMP180 (WiP)

#### Read the BMP180 Data
```
$ ../../RaspberryPythonServers/python/scripts/start.BMP180.TCP.server.sh
~/repos/ROB/raspberry-sailor/NMEA-multiplexer ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
Working from /home/pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python
It worked: 192.168.1.103 
Enter Machine Name - Default [192.168.1.103] > 127.0.0.1
Enter HTTP Port - Default [9999] > 8001
Verbose (true or false) ? - Default [false] > 
Running python3 ./TCP_BMP180_server.py --machine-name:127.0.0.1 --port:8001 --verbose:false
Done
Use ./scripts/kill.python.tcp.sh to stop the server.

Usage is:
python3 /home/pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python/scripts/TCP_BMP180_server.py [--machine-name:127.0.0.1] [--port:7001] [--verbose:true|false]
    where --machine-name: and --port: must match the context's settings.

Server is listening. [Ctrl-C] will stop the process.
. . .
```
The port above (`8001`) is the one defined in `nmea.mux.bmp180.yaml`:
```yaml
channels:
  - . . .
  - type: tcp
    server: 127.0.0.1
    port: 8001
    verbose: false
```

#### Specific Requirement: Long Term Data
We want here to be able to display a curve, like the one produce by a barograph, like this one:

![Barograph](../doc_resources/barograph.png)

It displays data on one week (or more). We would need some server-side component to store those data,
and restore them on demand.  
V1 available. See in the `yaml` file.


#### Details
Wiring, Schemas, Components, 3D printed enclosures...

|        Wiring (VIN, GND, SLC, SDA)        |
|:-----------------------------------------:|
| ![BMP180](../doc_resources/BMP180_bb.png) |

3D Printed enclosure, available (somewhere...)

#### Running
```
../../RaspberryPythonServers/python/scripts/start.BMP180.TCP.server.sh
Enter Machine Name > 127.0.0.1
Enter TCP POrt > 8001
Verbose > false  
. . .
./mux.sh nmea.mux.bmp180.yaml
```

---
