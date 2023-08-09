# From scratch
### An example: how to setup a new Raspberry Pi for a minimalist Nav Station.
We will be setting up a Raspberry Pi Zero W with an [eInk 2.13" bonnet](https://learn.adafruit.com/2-13-in-e-ink-bonnet?view=all).  
The NMEA-multiplexer will:
- Read a GPS
- Log the data into a file
- Display the data on the EInk screen
  - The two buttons can be used to scroll through the available data
- Broadcast the data on TCP:7001

The server (aka Mux) will be automatically started when the Raspberry Pi boots.

- Use [Raspberry Pi imager](https://www.raspberrypi.com/software/) to flash a new SD Card
  - Make sure SSH is enabled.
  - Create a user named `pi` (this is the name we use below, choose your own if you want to)
- Use `./to.prod.sh` to package the current software
  - Make sure you package the python part as well, when prompted
  - This will prepare a `tar.gz` archive, called - for example - `nmea-dist.tar.gz`.
  - Send the archive to the new Raspberry Pi (change it's IP address at will)
    - ` scp nmea-dist.tar.gz pi@192.168.1.15:~`
- Prepare the new System. This will require an Internet connection.
  - Boot to CLI
  - Enable interfaces `SPI`, `I2C`, `Serial Port`
  - Install Java
  ```
  sudo apt-get update
  sudo apt-get install openjdk-11-jdk
  ```
  or (for some Raspberry Pi Zero)
  ```
  sudo apt-get install openjdk-8-jdk-headless
  ```
  - LibRxTx (optional)
  ```
  sudo apt-get install librxtx-java
  ```
  - Install required Python modules (if any), like for example
  ```
  sudo pip3 install adafruit-circuitpython-epd
  ```

- Setup Hotspot (if needed), as explained [here](./HOTSPOT.md).
- Expand the archive, using a command like `tar -xzvf nmea-dist.tar.gz`
- Modify the `/etc/rc.local` to start the server when the server boots
- Issue the required command (links, maps, stty, etc)
- Give it a try (see for example the script `start.all.sh`)
- Try `ssh` to make sure it's working as expected
- Try the web interface
  - From a browser on another machine (laptop, cell-phone, tablet, ...), connected on the Raspberry Pi's network, reach
    `http://192.168.50.10:9999/zip/index.html`, and see for yourself!

You're good to go!

Last but not least, we will update the `/etc/rc.local` script, to start the 
required components when the machine boots.  
Here are the lines to add to the file, _**before**_ the `exit` statement at the end:
```
#
# "Link" the Serial Port
stty -F /dev/ttyACM0 raw 4800 cs8 clocal
#
# Start MUX and Co on startup
#
# Start the REST Server for EINK2-13
/home/pi/nmea-dist/python/scripts/start.EINK2-13.REST.server.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --data:NAV,POS,SOG,COG --screen-saver:on > /home/pi/nmea-dist/eink2-13.python.log 2>&1
sleep 10
# Start the MUX
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.nmea-fwd.yaml &
#
```
As you can tell: it starts the Python server that takes care of the eInk display, and
starts the mux with the config file `nmea.mux.gps.nmea-fwd.yaml`, provided [here](nmea.mux.gps.nmea-fwd.yaml).

The system is now operational, and can be re-booted.

Warning: The data are logged into some log-files. Make sure you download and delete them from time to time...,
before the get too big. There is a Web page for that (in the embarked Web UI), called "Log Management".

### 3D Printed enclosures
STL files available [here](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/ProjectBoxRPiZeroBox.stl), and
[here](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/ProjectBoxRPiZeroBoxTop.stl).

|  |                                          |
|:----|:-----------------------------------------|
| ![One](./doc_resources/01.in.the.box.jpg) | ![Two](./doc_resources/02.connected.jpg) | 
| In its box | Connected (Power, GPS)                   |
| ![Three](./doc_resources/03.up.and.running.jpg)                                     | ![Four](./doc_resources/04.closed.jpg)              | 
| Up and Running                                                                      | Closed                                              |
| ![Five](./doc_resources/01.phone.UI.jpg)                                            | ![Six](./doc_resources/02.phone.UI.jpg)             | 
| Phone UI | Phone UI |


For the phone UI: the phone is connected to the RPi's network, URL in the browser is <http://192.168.50.10:9999/zip/index.html>  
_Note_: The phone does not need to have a SIM card.


Connected from OpenCPN, from a laptop:       
![Seven](./doc_resources/OpenCPN.png)

<!-- 
   TODO image with a solar panel
  -->

### Summary
So, you now have a system that starts at boot.    
The Raspberry emits its own network, so you can connect to it from other machines or devices,
using `ssh`, `scp`, or just `http` and `tcp`.

> _Note_: There is a network, but _**no**_ Internet.  
> This is no Cloud Computing..., maybe more like "Flake" Computing. No satellite needed, very low carbon footprint!

Current data are displayed on the eink screen (basic UI).  
It comes with a Web UI, to help you to manage the system, and/or visualize the data (using plain `http`, as mentioned above).  
It also broadcasts the data on `tcp`, port 7001, so other software can use them, like OpenCPN, SeaWi, etc.  
Data are logged into some files, so you can analyze or replay them.

### Supplies, BOM
- [eInk bonnet](https://www.adafruit.com/product/4687), $19.95 (Aug-2023)
- [Raspberry Pi Zero W](https://www.adafruit.com/product/3708), $16.00 (Aug-2023)
- [GPS Dongle](https://www.amazon.com/HiLetgo-G-Mouse-GLONASS-Receiver-Windows/dp/B01MTU9KTF/ref=sr_1_3?keywords=usb+gps+dongle&qid=1691564294&sprefix=USB+GPS%2Caps%2C153&sr=8-3), $11.99 (Aug 2023)

---
