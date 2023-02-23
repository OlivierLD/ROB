# Use-case 3
### Deploy the config [Use-case 2](./USE_CASES_2.md), on a new Raspberry Pi image
We want to deploy the configuration described [before](./USE_CASES_2.md) on a newly flashed SD Card ([Raspberry Pi Imager](https://www.raspberrypi.com/software/) does the job).  
This config will include
- A Raspberry Pi A+, to host the NMEA-multiplexer
- A BME280 and a 128x64 oled SSD1306, with 2 push buttons to interact with it
- A GPS, with its USB socket

We want to log the data (from the GPS and BME280), so we can replay then, see them in GoogleMaps or Leaflet, etc.

We also want the Raspberry Pi to emit its own hotspot network, so other devices can connect to it and reach 
the multiplexer's web pages.  
And finally, we will need the multiplexer to start when the Raspberry Pi boots.

---
**_To be as clear as possible_**:
- We will build the required soft from a clone of the git repo, on `Machine A`.
- We will deploy the required artifacts on `Machine B` (the Raspberry Pi A+, the one that will do the job).
  - Configure whatever has to be configured on it
  - Take it for a hike!
---

#### Machine A
You need to have cloned the repo, and installed all the requirements for a build.  
> The script `start.from.scratch.sh` will help you if needed. Look into it for details.

`Machine A` can be a laptop, running Windows, Mac, Linux..., as well as a Raspberry Pi, strong enough to run the build.  
Assuming that you can run a terminal using `bash`, use `to.prod.sh` to generate the archive to deploy (from `ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic`).  
Make sure you say `y` when it says `Do we package the Python part ? >`.
```
$ ./to.prod.sh 
+----------------------------------------------------------------------------------------------------+
|                          P A C K A G E   f o r   D I S T R I B U T I O N                           |
+----------------------------------------------------------------------------------------------------+
| This is an EXAMPLE showing how to generate a 'production' version, without having the full github  |
| repo on the destination machine. We will deploy only what is needed to run the NMEA Multiplexer,   |
| possibly with several configurations - and its web clients.                                        |
+----------------------------------------------------------------------------------------------------+
| Now you may start a fresh build...                                                                 |
| Make sure the java version is compatible with your target. Current version:                        |
+----------------------------------------------------------------------------------------------------+
| java version "11.0.8" 2020-07-14 LTS
| Java(TM) SE Runtime Environment 18.9 (build 11.0.8+10-LTS)
| Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.8+10-LTS, mixed mode)
+----------------------------------------------------------------------------------------------------+
| Make sure the current Java version is compatible with the target one!!                             |
+----------------------------------------------------------------------------------------------------+

There is an existing jar-file:
147025906 78488 -rw-r--r--  1 olivierlediouris  staff    38M Feb 11 08:40 ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
With the following MANIFEST:
Found ./build/libs/NMEA-multiplexer-basic-1.0-all.jar. Moving on.
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/temp ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
-------- MANIFEST.MF --------
Manifest-Version: 1.0

-----------------------------
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
----------------------------
Do we re-build the Java part ? > y
Rebuilding from source (No Scala)...

> Configure project :
>> From task compileJava (in rob), using java version 11 
>> From task compileTestJava (in rob), using java version 11 

> Configure project :astro-computer:AstroComputer
>> From task compileJava (in AstroComputer), using java version 11 
>> From task compileTestJava (in AstroComputer), using java version 11 

> Configure project :astro-computer:AstroUtilities
>> From task compileJava (in AstroUtilities), using java version 11 
>> From task compileTestJava (in AstroUtilities), using java version 11 

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.6/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 9s
14 actionable tasks: 2 executed, 12 up-to-date
Which (non existent) folder should we create the distribution in ? > nmea-dist
Creating folder nmea-dist
Copying resources
  adding: admin.html (deflated 89%)
  adding: basic.html (deflated 59%)
  adding: console.html (deflated 79%)
  adding: css/ (stored 0%)
  adding: css/black.css (deflated 38%)
  adding: css/night.stylesheet.css (deflated 80%)
  adding: css/stylesheet.css (deflated 80%)
  adding: css/bground.jpg (stored 0%)
  adding: css/white.css (deflated 37%)
  adding: css/web-components.css (deflated 86%)
  adding: css/screen.css (deflated 46%)
  adding: css/graph.ux.01.css (deflated 63%)
  adding: css/graph.ux.02.css (deflated 64)
  . . .
  adding: widgets/TimeDisplay.js (deflated 70%)
  adding: widgets/AnalogDisplay.js (deflated 76%)
  adding: widgets/TWSEvolution.js (deflated 73%)
  adding: wsconsole.html (deflated 50%)
Do we package the Python part ? > y
Copying Python stuff into nmea-dist/python
Done with Python
a nmea-dist
a nmea-dist/nmea.mux.gps.tcp.yaml
a nmea-dist/nmea-to-text.properties
a nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
a nmea-dist/python
a nmea-dist/web.zip
a nmea-dist/nmea.mux.kayak.ssd1306.yaml
a nmea-dist/nmea.mux.replay.big.log.yaml
a nmea-dist/REST.ssd1306.dg.properties
a nmea-dist/nmea.mux.big.log.nmea-fwd.yaml
a nmea-dist/mux.sh
a nmea-dist/build
a nmea-dist/build/libs
a nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
a nmea-dist/python/checksum.py
. . .
a nmea-dist/python/scripts/start.Serial.TCP.server.sh
a nmea-dist/python/scripts/test.sh
a nmea-dist/python/scripts/start.BME280.REST.server.sh
+--------------------------------------------------------------------------------------------------+
 >> Archive /. . ./ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/nmea-dist.tar.gz ready for deployment.
+--------------------------------------------------------------------------------------------------+
| Send it to another machine, and un-archive it.                                                   |
| Use 'tar -xzvf nmea-dist.tar.gz' to un-archive.                                                   |
| External dependencies like librxtx-java may be needed if you intend to use a serial port,        |
| in which case you may need to run a 'sudo apt-get install librxtx-java' .                        |
| > For the python scripts, make sure you run (once) the script ./python/scripts/install.all.sh    |
| > after unzipping the archive on the target machine.                                             |
| The script to launch will be 'mux.sh'                                                            |
| It is your responsibility to use the right properties file, possibly modified to fit your needs. |
| For the runner/logger, use nmea.mux.gps.tcp.properties                                           |
| Use it - for example - like:                                                                     |
| $ cd nmea-dist                                                                                   
| $ nohup ./mux.sh nmea.mux.gps.tcp.yaml &                                                         |
|  >> Static web resources can be reached like http://<host>:<port>/zip/index.html                 |
+--------------------------------------------------------------------------------------------------+
$
```
The archive was generated.  
Now we need to configure `Machine B`, and send the newly generated archive to it.

#### Machine B
Flash a new SD card (see [here](https://www.raspberrypi.com/documentation/computers/getting-started.html), [Raspberry Pi Imager](https://www.raspberrypi.com/software/) does the job).  
Make sure you enable the `ssh`, `spi`, `i2c` interfaces (use `raspi-config`).  
This interface enablement can also be done through the settings (geared wheel) of the Raspberry Pi Imager.  
This new image may contain a Java Development Kit (aka JDK), and Python 3. Make sure it's right:
```
pi $ java -version
pi $ python3 -V
```
If java is not there (or not in the right version), install JDK 11:
```
pi $ sudo apt-get update
pi $ sudo apt-get install openjdk-11-jdk
```

Find the IP address of `Machine B` (I use [`fing`](https://www.fing.com/products/development-toolkit). Make sure you use the [`Fing CLI`](https://www.fing.com/products/development-toolkit) for your system, `dpkg --print-architecture` will tell you what to choose, `lscpu` too.).   
We assume it is `192.168.1.101`.  
From `Machine A`, send the archive to `Machine B`:
```
machine-a $ scp nmea-dist.tar.gz pi@192.168.1.101:~
pi@192.168.1.101's password: 
nmea-dist.tar.gz                                                                        100%   37MB 372.6KB/s   01:42    
machine-a $
```
We're done with `Machine A`.

Connect on `Machine B` (with `ssh` if you want):
```
machine-a $ ssh pi@192.168.1.101
```
and unarchive what was received before:
```
pi $ tar -xzvf nmea-dist.tar.gz
nmea-dist/
nmea-dist/nmea.mux.gps.tcp.yaml
nmea-dist/nmea-to-text.properties
nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
nmea-dist/python/
nmea-dist/web.zip
nmea-dist/nmea.mux.kayak.ssd1306.yaml
nmea-dist/nmea.mux.replay.big.log.yaml
nmea-dist/REST.ssd1306.dg.properties
nmea-dist/nmea.mux.big.log.nmea-fwd.yaml
nmea-dist/mux.sh
nmea-dist/build/
nmea-dist/build/libs/
nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
nmea-dist/python/checksum.py
. . .
nmea-dist/python/scripts/test.sh
nmea-dist/python/scripts/start.BME280.REST.server.sh
pi $
```
Make sure `librxtx-java` is installed:
```
pi $ sudo apt-get install librxtx-java
```

Let's move to the newly created directory:
```
pi $ cd nmea-dist
```
We now install the required Python modules:
```
pi $ ./python/scripts/install.all.sh
. . .
pi $
```
Then, modify the file `/etc/rc.local` (make sure you're super-user), to start the required pieces at boot. Add the following lines, at the end
of the file, _before_ the `exit` statement:
```
# Link the Serial Port
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# Start MUX and Co on startup
#
# Start the REST Server for BME280
# /home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:$(hostname -I) --port:9876 --verbose:false > /home/pi/bme280.python.log 2>&1
/home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:localhost --port:9876 --verbose:false > /home/pi/bme280.python.log 2>&1
sleep 10
# Start the REST Server for SSD1306 (v2)
# /home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:$(hostname -I) --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM > /home/pi/ssd1306.python.log 2>&1
/home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM > /home/pi/ssd1306.python.log 2>&1
sleep 10
# Start the MUX
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.sensor.nmea-fwd.yaml &
#
```
> Note: the mapping on `/dev/ttyS80` is used in `nmea.mux.gps.sensor.nmea-fwd.yaml`. Make sure it matches your config.


> Note: to _stop_ all the servers started above, run a script like:
> ```
> #!/bin/bash
> ps -ef | grep -Eiw 'java |python3 ' | grep -v grep | awk '{ print $2 }' > km
> NB_L=$(cat km | wc -l)
> if [[ ${NB_L} == 0 ]]; then
>   echo No process found.
> fi
> for pid in $(cat km); do
>   echo -e "Killing process ${pid}"
>   sudo kill -15 ${pid}
> done
> rm km
> ```

Finally, we can set up the hotspot network on the Raspberry Pi.  
Follow the instructions [here](../HOTSPOT.md).

Then you can stop the `Machine B` (the Raspberry Pi), plug in your GPS, and boot it.

Once it is re-started, you should see - from `Machine A` for example - a network named, as above, `NMEANetwork`.
Its password is `PassWord` (see in the [instructions](../HOTSPOT.md)).

Once connected on this new network, from this "other" machine (a cell-phone would work too, just connect from it to the new `NMEANetwork` network), try to reach <http://192.168.50.10:9999/zip/index.html>,
`192.168.50.10` being the address of the machine (Raspberry Pi) the multiplexer runs on.

---
