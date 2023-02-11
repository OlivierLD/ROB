# Some use-cases

First, make sure you've built the soft:
```
$ ../../../gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```

## Use-case 1 
### A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306 (using SPI)
> Suitable for hiking, kayaking, this kind of things.  
> Position is read from a Serial GPS, Atmospheric data from a [BME280](https://www.adafruit.com/product/2652).  
> Data are displayed on a [small OLED screen](https://www.adafruit.com/product/326), and logged into a file, to be replayed later. 

**We assume below that the address of the board you're running on is `192.168.1.103`.**

First start the Python servers, for the BME280 and the SSD1306.  
Make sure you use the right ports.
```
$ ../../RaspberryPythonServers/python/scripts/start.BME280.TCP.server.sh
~/repos/ROB/raspberry-sailor/NMEA-multiplexer ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
Working from /home/pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python
It worked: 192.168.1.103 
Enter Machine Name - Default [192.168.1.103] > 
Enter HTTP Port - Default [9999] > 8001
Verbose (true or false) ? - Default [false] > 
Running python3 ./TCP_BME280_server.py --machine-name:192.168.1.103 --port:8001 --verbose:false
Done
Use ./scripts/kill.python.tcp.sh to stop the server.

Usage is:
python3 /home/pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python/scripts/TCP_BME280_server.py [--machine-name:127.0.0.1] [--port:7001] [--verbose:true|false]
	where --machine-name: and --port: must match the context's settings.

Server is listening. [Ctrl-C] will stop the process.
. . .
```
The port above (`8001`) is the one defined in `nmea.mux.kayak.ssd1306.yaml`:
```yaml
channels:
  - . . .
  - type: tcp
    server: 192.168.1.103
    port: 8001
    verbose: false

```

```
$ ../../RaspberryPythonServers/python/scripts/start.SSD1306.REST.server.sh 
~/repos/ROB/raspberry-sailor/NMEA-multiplexer ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
Working from /home/pi/repos/ROB/raspberry-sailor/RaspberryPythonServers/python
It worked: 192.168.1.103 
Enter Machine Name - Default [192.168.1.103] > 
Enter HTTP Port - Default [9999] > 8080
Verbose (true or false) ? - Default [false] > 
Screen Height (32 or 64) ? - Default [32] > 64
Wiring Option (I2C or SPI) ? - Default [I2C] > SPI
Running python3 s./REST_SSD1306_server.py --machine-name:192.168.1.103 --port:8080 --verbose:false --height:64 --wiring:SPI
Done
Use ./scripts/kill.python.rest.sh to stop the server.
- Try curl -X PUT http://192.168.1.103:8080/ssd1306/nmea-data -d 'This is|a test.'
- Try curl -X GET http://192.168.1.103:8080/ssd1306/oplist
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
Using RESET D24
Using CS D8
Using DC D23
Starting SSD1306 server on port 8080
Try curl -X GET http://192.168.1.103:8080/ssd1306/oplist
or  curl -v -X VIEW http://192.168.1.103:8080/ssd1306 -H "Content-Length: 1" -d "1"
. . .

```
The port `8080` is the one defined in `REST.ssd1306.dg.properties`:
```properties
ssd1306.dg.protocol=http
ssd1306.dg.server-name=192.168.1.103
ssd1306.dg.port=8080
ssd1306.dg.verb=PUT
ssd1306.dg.resource=/ssd1306/nmea-data
ssd1306.dg.verbose=false
```

Now, sensors and actuator are in place, start the multiplexer:
```
$ ./mux.sh nmea.mux.kayak.ssd1306.yaml
Using properties file nmea.mux.kayak.ssd1306.yaml
Running sudo java  -Djava.library.path=/usr/lib/jni -Drmc.decl.only=true -Dscreen.verbose=true -Drest.feeder.verbose=true -Dparse.ais=true -Dmux.props.verbose=true -Dprocess.on.start=true -Dmux.properties=nmea.mux.kayak.ssd1306.yaml -Dno.ais=false -Dcalculate.solar.with.eot=true -Ddefault.mux.latitude=37.8218 -Ddefault.mux.longitude=-122.3112 -Dtry.to.speak=true -Djava.util.logging.config.file=./logging.properties   -cp ./build/libs/NMEA-multiplexer-basic-1.0-all.jar:/usr/share/java/RXTXcomm.jar nmea.mux.GenericNMEAMultiplexer 
{with.http.server=true, http.port=9999, init.cache=true, damping=30}
Definition Name: Logging Data from the Kayak
-- Description --
A GPS on /dev/ttyS80
A BME280 read from a TCP Server in Python, on port 8001
Display on an SSD1306 128x64
Log data in a file
-----------------
Log available in global, level INFO
Log file pattern null
- Start writing to nmea.forwarders.DataFileWriter, ./data.nmea 
Starting new http.HTTPServer (verbose false)
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
1,675,177,415,348 - Port open: 9999
1,675,177,415,351 - http.HTTPServer now accepting requests
1 client is now connected.
Connected by client <socket.socket fd=9, family=AddressFamily.AF_INET, type=SocketKind.SOCK_STREAM, proto=0, laddr=('192.168.1.103', 8001), raddr=('192.168.1.103', 39686)>
New client listener
Port Ownership of /dev/ttyS80 changed: type=1, Owned (Locked)
This is a serial port
Reading serial port...
/dev/ttyS80:4800  > Port is open...

. . .
```
> _**Note**_: if the Serial port of the GPS is giving you trouble, run the script
> `../RESTNavServer/mk.link.sh` ...

Data are read, displayed and logged.  
And you can see what's going on from a browser (machine name and port "may" change):
- <http://192.168.1.103:9999/web/index.html>

The Web UI available above is designed for all kinds of configurations. For example, 
in this case (as you have a GPS and a BME280 to feed the data), you might not be able to see the True Wind Speed 🤔...  
Again, the Web Pages presented here are to be seen as examples of the way to consume the data available from the cache of the Multiplexer, through REST services.  
> _**Note**_: The Raspberry Pi can very well emit its own network, onto which you can connect from other devices (laptops, tablets, cell-phones, smartwatches, ...). See
> [here](../../../HintsAndTips.md#ad-hoc--hotspot-networking).  
> Along the same lines, other devices can also `ssh` to the Raspberry Pi.

#### Details
Wiring, Schemas, Components, 3D printed enclosures...

On a [PiHAT](https://www.adafruit.com/product/2310) (HAT: **H**ardware **A**ttached on **T**op):

| Components moved down to show the wiring |
|:-----------------------------------------:|
| ![PiHAT](./doc_resources/perma-proto-pi-hat_bb_01.png) |
| Actual Components Position |
| ![PiHAT](./doc_resources/perma-proto-pi-hat_bb_02.png) |

3D Printed enclosure, available 
- [here](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/RPiA%2BLogger).
- [STL, animated](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiA%2BLogger/rpi.aplus.enclosure.full.stl).

| On the go |
|:---------:|
| ![Full Setting](./doc_resources/01.full.setting.jpg) |
| ![At work](./doc_resources/kayak.setting.jpg) |

---
