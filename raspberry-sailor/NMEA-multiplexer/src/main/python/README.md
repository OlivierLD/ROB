# Python NMEA Servers...
As said before, the code in this folder is here to try _not_ to have to re-write existing drivers.  
We will use the code provided by the sensor providers, as it is, which usually means in Python.
The idea here is _not_ to depend on Java frameworks (like PI4J, diozero), as we've experienced some frustration in the past, like
framework deprecation, restrictions based on the JDK version... Here is a try to get rid of those frustrations.  
> In short, those frameworks enable interaction with the GPIO header of the Raspberry Pi, and whatever you can plug on it.
> Vast topic indeed.

For now, this is more to be seen as a Proof Of Concept (POC) than anything else. 

The tricky point was to find a suitable way to establish a communication between Java and Python, and this without having to depend on external code. And ideally, a language agnostic method.  

After several trials, two main communication methods emerged:
- Transfer Control Protocol (TCP)
- Representational State Transfer (REST)
  - relies on HTTP, itself relying on TCP

### TCP and REST, in short
TCP implementations exist in the NMEA world (OpenCPN, SeaWi, ...). When a client connects to a TCP server, then the TCP server starts feeding the client with 
NMEA sentences, as long as the client remains connected to the server. This is indeed very convenient for data like Heading, Wind Speed, etc.  
TCP is a _connected_ protocol. The client remains connected to the server as long s he does not explicitly disconnect (or on error).

REST, as it is relying on HTTP, is not a connected protocol. A REST request goes like this:
- Client connects to the server
- Client sends a request
- Server produces a response, sent back to the client
- Connection is _closed_ (and cannot be reused)

In this context, this is convenient if you do not need to receive as many data as possible, but more like on
an on-demand basis. Like for Air Temperature, Atmospheric Pressure, etc. Those data do not vary
as fast as boat or wind speed.

So we are going to use TCP and REST, language agnostic protocol supported by many languages - including Java and Python, and the data going back and forth
will be encapsulated by JSON (JavaScript Object Notation), also natively supported by the languages we target.

_**A last detail**_: The servers (TCP and/or REST) do not need to run on the same machine as the NMEA-multiplexer. All they need is to see each other on the network.

## Implementations
This directory contains _**EXAMPLES**_ of the way to have TCP servers written in Python,
reading sensor data, that could be used to feed the NMEA-multiplexer.  
The Python (Python3) code in this folder is usually a wrapper around the Python modules written by the sensors' provider.
The code provided in this folder requires those modules to be installed first (with `pip3` or similar tools). This will be explained.

> _**Note**_: the sensors we talk about here are atmospheric and magnetic sensors, using I2C (or maybe SPI some day) protocol.
> We will _not_ use GPS here. GPS' are read using Serial communication. Look into the Java code for that.

### TCP
The Python code reads the sensor's data, and builds appropriate NMEA sentence(s) to carry them around.
The code acts as a TCP server, so any TCP client can receive the produced NMEA sentences.  
> _**Note**_: TCP is one of the inputs the NMEA-multiplexer expects, but any TCP/NMEA savvy client can use it.
> Like [OpenCPN](https://www.opencpn.org/), [SeaWi](http://www.seawimarine.net/), etc.

We provide here TCP servers reading the following sensors:
- [BMP180](https://learn.adafruit.com/using-the-bmp085-with-raspberry-pi/using-the-adafruit-bmp-python-library). Temperature, Pressure.
- [BME280](https://learn.adafruit.com/adafruit-bmp280-barometric-pressure-plus-temperature-sensor-breakout). Temperature, Pressure, Relative Humidity.
- [HTUDF21D](https://learn.adafruit.com/adafruit-htu21d-f-temperature-humidity-sensor).  Temperature, Relative Humidity.
- [LSM303](https://learn.adafruit.com/lsm303-accelerometer-slash-compass-breakout). 3-axis Magnetometer, Accelerometer.
- [LIS3MDL](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer). 3-axis Magnetometer.

_**NMEA Sentences examples:**_
- `ZDA`: Time & Date - UTC, day, month, year and local time zone 
  - that one does _**not**_ need a sensor (it's based on the system's time). It has a Java equivalent.
- `XDR`: Transducer Measurement. Can convey (among many others) Temperature, Pressure, Humidity, Angular Displacement (like pitch and roll)
  - Produced by BMP180, BME280, Magnetometers
- `MTA`: Air Temperature, Celsius
  - Produced by BMP180, BME280
- `MMB`: Atm Pressure
    - Produced by BMP180, BME280
- `HDM`, `HDG`: Heading
  - Produced by LSM303, HCM5883L, LIS3DML

> _**Important Note**_: the Python code may require some modules to be installed. This must be done
> _with an <u>Internet connection</u>_.  
> Like 
> ````
> pip3 install pyyaml
> pip3 install board
> . . .
> pip3 install adafruit-circuitpython-htu21d
> ````
> After that, you may hop off the Internet.


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

## Example 2, produce Mag Data
```
$ python3 src/main/python/TCP_LSM303_HCM5883L_server.py --port:7001 --cal-props:cal.sample.yaml
```
> Notice the calibration properties yaml file

## Example 3, produce Atmospheric Data
```
$ python3 src/main/python/TCP_BMP180_server.py --port:7001
```
```
$ python3 src/main/python/TCP_BME280_server.py --port:7001
```

## Example 4, produce all kinds of data, and consume them in one place
Start all the required Python TCP servers
```
$ python3 src/main/python/TCP_LSM303_HCM5883L_server.py --port:7001 --cal-props:cal.sample.yaml
```
```
$ python3 src/main/python/TCP_BMP180_server.py --port:7002
```
Consume them from an NMEA-multiplexer, with a config file like
```yaml
#
# MUX definition.
# With Python TCP Server input, on port 7001
#
name: "With TCP server, in Python"
description:
  - Requires the Python servers to be running, ports 7001 and 7002
context:
  with.http.server: true
  http.port: 9999
  init.cache: true
channels:
  # Mag Data
  - type: tcp
    server: localhost
    port: 7001
    verbose: false
  # Atm Data
  - type: tcp
    server: localhost
    port: 7002
    verbose: false
forwarders:
  - type: console
```
Then you can get to the data.  
Do a
```
$ curl -X GET http://localhost:9999/mux/oplist
```
and then a 
```
$ curl -X GET http://localhost:9999/mux/cache
```
> Notice the way the different TCP ports are mentioned in the yaml. 

## Server interaction
The TCP code presented here allows you to interact with the server, using a TCP client.  
Look into the `Java-TCP-Python` module (in this project, make sure you've built it).  
You can change the NMEA sentences production frequency, and get the status of the server.

```
$ ./start.tcp.client.sh --port:7001
(tcp.clients.SimpleTCPClient) Port now set to 7001
(tcp.clients.SimpleTCPClient) Enter '/exit' at the prompt to stop. Any non-empty string otherwise.
User Request > status
>> Server responded {"source": "<. . .>/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py", "between-loops": 10.0, "connected-clients": 1, "python-version": "3.10.8", "system-utc-time": "2022-12-21T12:30:10.000Z"}
- Key: source, Value (java.lang.String): <. . .>/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py
- Key: between-loops, Value (java.lang.Double): 10.0
- Key: connected-clients, Value (java.lang.Integer): 1
- Key: python-version, Value (java.lang.String): 3.10.8
- Key: system-utc-time, Value (java.lang.String): 2022-12-21T12:30:10.000Z
User Request > loops:0.5
>> Server responded {"source": "<. . .>/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py", "between-loops": 0.5, "connected-clients": 1, "python-version": "3.10.8", "system-utc-time": "2022-12-21T12:30:30.000Z"}
- Key: source, Value (java.lang.String): <. . .>/repos/ROB/raspberry-sailor/NMEA-multiplexer/src/main/python/TCP_ZDA_server.py
- Key: between-loops, Value (java.lang.Double): 0.5
- Key: connected-clients, Value (java.lang.Integer): 1
- Key: python-version, Value (java.lang.String): 3.10.8
- Key: system-utc-time, Value (java.lang.String): 2022-12-21T12:30:30.000Z
User Request > /exit
(tcp.clients.SimpleTCPClient) Client exiting
Done with dummy reader
$
```

### REST
There is a REST channel that comes with the NMEA-multiplexer. Let's use it.

_Server Example_: `REST_BME280_server.py`  
Start it like in
```
$ python3 src/main/python/REST_BME280_server.py --machine-name:192.168.1.105 --simulate-when-missing:true
```

> _**Important**_: As it is now, it's the server responsibility to respond with _valid_ NMEA strings. 
> See the NMEA-multiplexer Consumer's doc for details.

. . .

---
## Misc links and stuff
- <https://docs.circuitpython.org/projects/lsm303/en/latest/_modules/adafruit_lsm303.html>
- <https://learn.adafruit.com/adafruit-htu21d-f-temperature-humidity-sensor/python-circuitpython>
- <https://github.com/adafruit/Adafruit_CircuitPython_HTU21D>
- <https://pypi.org/project/adafruit-circuitpython-HTU21D/>
- <https://docs.circuitpython.org/projects/htu21d/en/latest/>
- <https://learn.adafruit.com/using-the-bmp085-with-raspberry-pi/using-the-adafruit-bmp-python-library>
- <https://learn.adafruit.com/adafruit-bme280-humidity-barometric-pressure-temperature-sensor-breakout/python-circuitpython-test>
- <https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer/python-circuitpython>
- A shield for atmospheric sensors: <https://www.thingiverse.com/thing:1067700>
- [Magnetometer (and accelerometer) Calibration](https://github.com/OlivierLD/raspberry-coffee/blob/master/raspberry-io-pi4j/I2C-SPI/lsm303.calibration/README.md)

Put the calibration parameters in a `yaml` file:
```yaml
#
MAG_X_COEFF: 1.0
MAG_Y_COEFF: 1.05
MAG_Z_COEFF: 1.0
#
# Offsets in microTesla
#
MAG_X_OFFSET: 12
MAG_Y_OFFSET: -15
MAG_Z_OFFSET: -5
```
and use it like in
```
$ python3 src/main/python/TCP_LSM303_HMC8553L_server.py \
          --port:7001 \
          --cal-props:src/main/python/cal.sample.yaml
```

# To Try next

- Docker image?

---
