# A pure Python BME280 server
## A use-case
(switch to [French](./README_fr.md))  

A barograph (like [this](https://www.naudet.com/barometre-enregistreur-c102x2375473)) is a very useful instrument if you're interested in weather forecast.  
But it can be pretty expensive...  
Here we want to show how to implement one with a Raspberry Pi Zero W, and an inexpensive sensor like a BME280.

---

This module shows the implementation of a pure Python server to get to the data emitted by a BME280.

It provides
- A server (python part) that 
  - serves REST and HTTP requests
  - reads the BME280 on a regular basis
  - stores BME280 data up to one week (one data set every 15 minutes)
- Web resources, served by the server above, to display the data in a user-friendly way.

---

## From Scratch
### You will need
- A Raspberry Pi (a Zero W would fit, here)
  - If you only have a Raspberry Pi Zero, you will need a WiFi dongle.
- A micro SD card for the Raspberry Pi
- A breadboard
- A BME280 sensor
- Jumper wires
- A power supply for the Raspberry Pi
- A laptop (for the initial configuration)
- A network with Internet access (your home WiFi would do)

> _**A Note**_: you will have to type some commands in a terminal, as shown below.  
> Some are quite cryptic, as you'll see...  
> Be aware that whatever starts with a `$` tells you that this is something _**you**_ have to type.  
> If you see `$ mkdir BME280`, it tells you to type _only_ `mkdir BME280`.  
> If a line does **_not_** begin with a `$`, it means it is the output of the command you've typed; 
> this is what you should see if all goes well (like for the `i2cdetect` command you'll see below, for example).

### Create a fresh SD Card
From the laptop, use the [Raspberry Pi Imager](https://www.raspberrypi.com/news/raspberry-pi-imager-imaging-utility/) to configure the Raspberry Pi sd card.  
Do not forget to
- Enable SSH, I2C
- Setup the network you're on, to be able to reach the Raspberry Pi when the SD card is ready.

### Create a dedicated folder on the Raspberry Pi
From the laptop terminal, assuming that the address of the Raspberry Pi is `192.168.1.38` (replace with the actual one)
```
$ ssh pi@192.168.1.38
```
It will prompt you for the password **you** created when flashing the SD Card. You're now connected on the Raspberry Pi.
```
$ mkdir BME280
```
### Upload the required resources
On the laptop (or on the Raspberry Pi, actually), clone just the part of the repo you're interested in:
```
$ mkdir BME280
$ cd BME280
$ git clone --depth 1 https://github.com/OlivierLD/ROB.git 
$ cd ROB
$ git filter-branch --prune-empty --subdirectory-filter raspberry-sailor/RaspberryPythonServers/python/pure.python.bme280 HEAD
```
The `ROB` folder should now contain at least all we need to move ahead.

From the laptop where the repo was cloned, from the `pure.python.bme280` (if you've cloned the full repo) of from the
`ROB` folder (if you've done the above, to clone only what we need here):
```
$ scp -r . pi@192.168.1.38:~/BME280
```
### Wiring of the BME280
Depending on where you got the BME280 from, its shape may vary (Sparkfun here, also available Adafruit, AliBaba, etc).
But the contact names (`GND`, `3V3`, `SDA`, `SCL`) remain the same.  
![Fritzing](doc/RPiZeroBME280_bb.png)

### Check the I2C availability
Make sure you've activated the I2C interface (with `raspi-config`)

On the Raspberry Pi:
```
$ sudo i2cdetect -y 1
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- -- 
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
70: -- -- -- -- -- -- 76 --                         
```
> Note: Here the address is `0x76`, as on the AliBaba BME280 I got. An Adafruit or Sparkfun would say `0x77`.

### To do once: install required python modules
On the Raspberry Pi:
```
$ sudo pip3 install adafruit-circuitpython-bme280
```
In case you see an "Externally Managed" message, see [here](https://www.makeuseof.com/fix-pip-error-externally-managed-environment-linux/)...  
Also, in case of some "name" issue, try `sudo pip install --upgrade setuptools`... 


### Run the server app
```
$ python3 REST_and_WEB_BME280_server.py --machine-name:$(hostname -I | awk '{ print $1 }') --port:8080 --verbose:false [--address:0x76]
```
Or if you want the server to run on its own:
```
$ nohup python3 -u REST_and_WEB_BME280_server.py --machine-name:$(hostname -I | awk '{ print $1 }') --port:8080 --verbose:false [--address:0x76] > bmp.log 2>&1 &
```
There is also a `--store-restore` CLI parameter. See the code for details, along with the `PUT /write` REST operation.
This allows you to write the maps content to a file when the server exits, and/or restore them at startup.

Data can be logged into a sqlite database.  
You need to create it before starting the server:
```
$ sqlite3 weather.db < sql/create.db.sql
```
Then use the `--log-db:true` CLI parameter at startup. This will log 5 data (pressure, relative humidity, air temperature, dew point, absolute humidity) every 15 minutes.

### Finally
From anywhere on the same network, you can reach, in a browser, <http://192.168.1.38:8080/web/index.html>  
![WebUI](doc/web.ui.png)
Even on such a small board as the Raspberry Pi Zero (W), you can have such a rich Web UI.  
Remember that the rendering of a Web UI is done _on the client side_. The Raspberry Pi only serves the
resources required on the client.

### Note
The web UI is using REST requests to ping the server.  
You can get the operation list by using
```
$ curl -X GET http://192.168.1.36:8080/bme280/oplist
```
You can set the `verbose` value to `true` or `false`
```
$ curl -X POST http://192.168.1.36:8080/bme280/verbose?value=false|true 
```

## Bonuses
### Head-up display
As you would notice in the picture above, we have two sliders at the bottom of the page.  
This is for a Head-Up display (enabled when you hit the `Head Up` button at the top right).  
When activated, this allows you to see the screen, reflected on a windshield for example.  

To have an idea:

|              This               |       Would display that       |
|:-------------------------------:|:------------------------------:|
| ![this](./doc/baro.head.up.png) | ![That](./doc/head.up.02.jpeg) |

### The Raspberry Pi emits its own network
Let's say you're at sea, far away from any 4G antenna... There is no Internet, _but_ you may be
able to create your own boat's network.  
This way, any device capable of joining this network can read the web pages made available 
by the server running on the Raspberry Pi. 
It can be a laptop, a tablet, a cell phone. The only requirement is a browser.

For the Raspberry Pi to emit it's own network, you need to set it up as a HotSpot.  
See details [here](https://github.com/OlivierLD/ROB/blob/master/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md)

Again, we can have a network, but no Internet. This is _**not**_ Cloud Computing, but more like Flake Computing...

### 3D printed enclosure(s) for your work
- See [here](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/NavStations/README.md)

---