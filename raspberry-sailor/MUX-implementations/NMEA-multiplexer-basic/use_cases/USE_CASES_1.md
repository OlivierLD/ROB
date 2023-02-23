# Use-case 1

### A Multiplexer (and its web interface) on a Raspberry Pi, with a GPS
This is a simple configuration. We will build the project on a machine where the git repo has been cloned.
This machine will be called `Machine A`. From `Machine A`, we will generate an archive that will be pushed on the Raspberry Pi (called `Machine B`).
On the Raspberry Pi, we will expand the archive mentioned before, do a minimal setup, and we will be ready for the real world.

The Raspberry Pi will possibly emit its own HotSpot network.

---
**_To be as clear as possible_**:
- We will build the required soft from a clone of the git repo, on `Machine A`.
  - Technically: we are going to generate a `gz` archive containing Java ARchives (aka JARs), web pages, scripts, configuration files, everything you'll need at runtime.
- We will deploy the required artifacts (the `gz` archive generated previously) on `Machine B` (the Raspberry Pi, the one that will do the job).
  - Expand the `gz` archive
  - Configure whatever has to be configured on it
  - And go! Take it for a hike!

---
In the commands below, we will prefix the terminal prompts with the machine name (or so).  
A prompt like `machine-a $` means that the command is to be issued from `Machine A`, the laptop.  
A prompt like `pi $` means that the command is to be issued omn the `Machine B`, the Raspberry Pi.

## Get started from scratch, fast.
You will need to clone the git repository (we'll say "repo" in the rest of the document).  
If you are not familiar with this technique and tool, here is a way for you to do it in a couple of clicks:

We assume that you start from a laptop or desktop, from a command line terminal understanding `bash` (Linux, Mac, ...).  
We are going to download a script from the git repo, make it runnable, and execute it.
The script will create a new folder, and clone the repo in it. Next, it will make sure that the build can be done smoothly.  

After that, you will be good to follow the steps described in the rest of the document.

So, let's go. From your laptop terminal, execute the following command to download the required script:
```
machine-a $ wget https://github.com/OlivierLD/ROB/raw/master/start.from.scratch.sh
```
Next, make it executable:
```
machine-a $ chmod +x ./start.from.scratch.sh
```
Finally, run the script:
```
machine-a $ ./start.from.scratch.sh
```
The script clones the repo in a `repos/ROB` folder, builds the `RESTNavServer`, and runs a demo script.

If this goes well, then you can move to the folder `repos/ROB/MUX-implementations/NMEA-multiplexer-basic`, and follow the rest of the instructions below.
```
machine-a $ cd ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
machine-a $
```

---
#### On Machine A
You need to have cloned the repo, and installed all the requirements for a build.

`Machine A` can be a laptop, running Windows, Mac, Linux..., as well as a Raspberry Pi, strong enough to run the build.  
Assuming that you can run a terminal using `bash`, use `to.prod.sh` to generate the archive to deploy (from `ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic`).  
Say `n` when it says `Do we package the Python part ? >`.

```
machine-a $ ./to.prod.sh 
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
  . . .
  adding: widgets/TWSEvolution.js (deflated 73%)
  adding: wsconsole.html (deflated 50%)
Do we package the Python part ? > n
a nmea-dist
a nmea-dist/nmea.mux.gps.tcp.yaml
a nmea-dist/nmea-to-text.properties
a nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
a nmea-dist/web.zip
a nmea-dist/nmea.mux.kayak.ssd1306.yaml
a nmea-dist/nmea.mux.replay.big.log.yaml
a nmea-dist/REST.ssd1306.dg.properties
a nmea-dist/nmea.mux.big.log.nmea-fwd.yaml
a nmea-dist/mux.sh
a nmea-dist/build
a nmea-dist/build/libs
a nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
+--------------------------------------------------------------------------------------------------+
 >> Archive /. . ./ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/nmea-dist.tar.gz ready for deployment.
+--------------------------------------------------------------------------------------------------+
| Send it to another machine, and un-archive it.                                                   |
| Use 'tar -xzvf nmea-dist.tar.gz' to un-archive.                                                   |
| External dependencies like librxtx-java may be needed if you intend to use a serial port,        |
| in which case you may need to run a 'sudo apt-get install librxtx-java' .                        |
| The script to launch will be 'mux.sh'                                                            |
| It is your responsibility to use the right properties file, possibly modified to fit your needs. |
| For the runner/logger, use nmea.mux.gps.tcp.properties                                           |
| Use it - for example - like:                                                                     |
| $ cd nmea-dist                                                                                   
| $ nohup ./mux.sh nmea.mux.gps.tcp.yaml &                                                         |
|  >> Static web resources can be reached like http://<host>:<port>/zip/index.html                 |
+--------------------------------------------------------------------------------------------------+
machine-a $
```
The archive was generated.  
Now we need to configure `Machine B`, and send the newly generated archive to it.

#### Machine B
Flash a new SD card (see [here](https://www.raspberrypi.com/documentation/computers/getting-started.html), [Raspberry Pi Imager](https://www.raspberrypi.com/software/) does the job).  
Make sure you enable the `ssh` interface (from the command line, use `raspi-config`,
from the Graphical Desktop, use `Menu` > `Preferences` > `Raspberry Pi Configuration`, and then `Interfaces`).    
This interface enablement can also be done through the settings (geared wheel) of the Raspberry Pi Imager.    
This new image may contain a Java Development Kit (aka JDK). Make sure it's right:
```
pi $ java -version
```
If java is not there (or not in the right version), install JDK 11:
```
pi $ sudo apt-get update
pi $ sudo apt-get install openjdk-11-jdk
```

Find the IP address of `Machine B`.

- If you have a screen and a keyboard connected on the Raspberry Pi, just use 
`ifconfig`, or `hostname -I`.
- If you want (or need) to remotely connect (from `Machine A` to `Machine B`), you can use `fing`, available [here](https://www.fing.com/products/development-toolkit). 
The archive you'll download contains several installers, you will need to run the right one, based on your architecture;
make sure you use the [`Fing CLI`](https://www.fing.com/products/development-toolkit) for your system, `dpkg --print-architecture` will tell you what to choose, `lscpu` too.  
Once installed on `Machine A`, run a
```
machine-a $ sudo fing
Password:
15:38:32 > Discovery profile: Default discovery profile
15:38:32 > Discovery class:   data-link (data-link layer)
15:38:32 > Discovery on:      192.168.1.0/24

15:38:32 > Discovery round starting.
15:38:36 > Discovery progress 25%
15:38:41 > Discovery progress 50%
15:38:32 > Host is up:   192.168.1.100
           HW Address:   3C:22:FB:B7:8C:5C

15:38:32 > Host is up:   192.168.1.1
           HW Address:   F0:0F:EC:D2:44:F5
           Hostname:     flybox.home

15:38:34 > Host is up:   192.168.1.101
           HW Address:   DC:A6:32:20:8E:27

. . .

15:38:34 > Host is up:   192.168.1.116
           HW Address:   00:22:43:0A:E8:92 (AzureWave Technology)

-------------------------------------------------------------------------------
| State | Host                              | MAC Address       | Last change |
|-----------------------------------------------------------------------------|
|  UP   | 192.168.1.1                       | F0:0F:EC:D2:44:F5 |             |
|  UP   | 192.168.1.100                     | 3C:22:FB:B7:8C:5C |             |
|  UP   | 192.168.1.101                     | DC:A6:32:20:8E:27 |             |
. . .
|  UP   | 192.168.1.116                     | 00:22:43:0A:E8:92 |             |
-------------------------------------------------------------------------------

15:38:55 > Discovery round completed in 23.301 seconds.
15:38:55 > Network 192.168.1.0/24 has 7/7 hosts up.

15:38:55 > Next round starting at 15:39:32. Press Ctrl^C to exit.
```


We assume that the address of `Machine B` is `192.168.1.101`.  

Make sure you know the username used on the Raspberry Pi, use `whoami`:
```
pi $ whoami
pi
```
In this case, the username is `pi`, as seen on the second line above.

#### On Machine A

Back on `Machine A`, send the archive to `Machine B`, using its IP address:
```
machine-a $ scp nmea-dist.tar.gz pi@192.168.1.101:~
pi@192.168.1.101's password: 
nmea-dist.tar.gz                                                                        100%   37MB 372.6KB/s   01:42    
machine-a $
```
> _**Note**_: in the `scp` command above, in `pi@192.168.1.101`, `pi` is the username found above. Make sure it matches your context.

We're done with `Machine A`.

#### On Machine B

Connect on `Machine B` (with `ssh` from `Machine A`, if you want, or directly on `Machine B`, if you have a screen and a keyboard connected to it)
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
nmea-dist/web.zip
nmea-dist/nmea.mux.kayak.ssd1306.yaml
nmea-dist/nmea.mux.replay.big.log.yaml
nmea-dist/REST.ssd1306.dg.properties
nmea-dist/nmea.mux.big.log.nmea-fwd.yaml
nmea-dist/mux.sh
nmea-dist/build/
nmea-dist/build/libs/
nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
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
Then, modify the file `/etc/rc.local` (make sure you're super-user), to start the required pieces at boot. Add the following lines, at the end
of the file, _before_ the `exit` statement:
```
# Link the Serial Port
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# Start the MUX
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.yaml &
#
```
> Note: the mapping on `/dev/ttyS80` is used in `nmea.mux.gps.yaml`. Make sure it matches your config.

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
