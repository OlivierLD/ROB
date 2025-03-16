# NMEA-multiplexer's Web Implementation Basics (and beyond)
This module is one illustration of what can be done from  Web and/or REST client, on top of the `NMEA-multiplexer`.  
This includes the way to start the Multiplexer (through its configuration file), and the way to reach the data to 
display on a web page.

This module does _**NOT**_ extend any Java class of the NMEA-multiplexer, and as you can see, there is just _**NO**_ extra Java code in this module. There is not even a `src` directory in this module.

The NMEA-multiplexer runs on its machine, and managed data can be access through several Web Interfaces, reachable from any device
with a browser, connected on the same network. This network does not need to have Internet access,
it can be emitted by a Raspberry Pi (like the one the NMEA-multiplexer runs on).

> _**Important Note**_: Some Python resources can be used.  
> Do make sure they are copied from their point-of-truth folders.  
> That is:
> - For parser related features: `NMEA-Multiplexer` module.
> - For sensors and actuators related features: `RaspberryPythonServers` module.

## Basics
It obviously requires the NMEA-multiplexer to be running.  
Data are fetched from the `NMEA Data Cache` using REST Requests.
> For an HTTP server to be instantiated when the Multiplexer starts, you 
> have to set (in the mux's config file)  the property `with.http.server` to `true`.  
> **In _addition_**, for the Data Cache to be available, populated, and reachable, 
> you need to set the property `init.cache` to `true`.

### Get Started, now!
Build, run, watch:
- `../../../gradlew shadowJar`
- `./mux.sh nmea.mux.replay.big.log.yaml`
- then reach <http://localhost:8080/web/basic.html> in your browser.
- and also <http://localhost:8080/web/index.html>, and <http://localhost:8080/web/admin.html>.

> Notice the config file `nmea.mux.replay.big.log.yaml`, look into it.

Also try (this is for lazy-dummies)
```
$ ./mux.sh --interactive-config
```
This previous one was developed for tests, it is a work in progress, it requires some polishing, several values are hard-coded..., 
as you would see in the code (`GenericNMEAMultiplexer.interactiveConfig()`). 

## How it works (in short)
As it depends on the `http-tiny-server` module, the `NMEA-multiplexer` can also act as an HTTP Server (not only WebServer). When the configuration property `with.http.server` is set to `true`, the multiplexer can also serve:
- static Web pages
    - See the system properties `static.docs` and `static.zip.docs` (and its associated `web.archive`).
- REST requests
    - Look into the class `nmea.mux.RESTImplementation`, in the `NMEA-multiplexer` module itself.

> _**Note**_: _Any_ REST client can reach the features available in the `NMEA-multiplexer` (or the classes extending it).
> Web pages can do it - obviously - but also utilities like `curl`, `wget`, and devices like Arduino, ESP32 & friends, M5 sticks, etc.  
> Examples will be linked from here (soon).

## About NMEA-multiplexer's REST operations
As you would see in `nmea.mux.RESTImplementation` where all operations are defined, there is a `/mux/oplist` resource, that will list
all its siblings (and child services):
```text
$ curl -X GET http://192.168.1.102:8080/mux/oplist
```
```json
[
  {
    "verb": "GET",
    "path": "/mux/oplist",
    "description": "List of all available operations, on NMEA request manager.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/terminate",
    "description": "Hard stop, shutdown. VERY unusual REST resource...",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/serial-ports",
    "description": "Get the list of the available serial ports.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/channels",
    "description": "Get the list of the input channels",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/forwarders",
    "description": "Get the list of the output channels",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/computers",
    "description": "Get the list of the computers",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/mux-config",
    "description": "Get the full mux config, channels, forwarders, and computers",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/forwarders/{id}",
    "description": "Delete an output channel",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/channels/{id}",
    "description": "Delete an input channel",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/computers/{id}",
    "description": "Delete a computer",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/forwarders",
    "description": "Creates an output channel",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/channels",
    "description": "Creates an input channel",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/computers",
    "description": "Creates computer",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/channels/{id}",
    "description": "Update channel",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/forwarders/{id}",
    "description": "Update forwarder",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/computers/{id}",
    "description": "Update computer",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/mux-verbose/{state}",
    "description": "Update Multiplexer verbose",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/mux-process/{state}",
    "description": "Update Multiplexer processing status. Aka enable/disable logging.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/mux-process",
    "description": "Get the mux process status (on/off)",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/cache",
    "description": "Get ALL the data in the cache. QS prm: option=tiny|txt",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/cache",
    "description": "Reset the cache",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/dev-curve",
    "description": "Get the deviation curve as a json object",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/position",
    "description": "Get position from the cache",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/position",
    "description": "Set position in the cache",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/distance",
    "description": "Get distance traveled since last reset",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/delta-alt",
    "description": "Get delta altitude since last reset",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/nmea-volume",
    "description": "Get the time elapsed and the NMEA volume managed so far",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/sog-cog",
    "description": "Get Speed and Course Over Ground",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/sog-cog",
    "description": "Set Speed and Course Over Ground",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/run-data",
    "description": "Get Speed and Course Over Ground, distance, and delta-altitude, in one shot.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/log-files",
    "description": "Download the log files list",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/system-time",
    "description": "Get the system time as a long. Optional QS prm 'fmt': date | duration",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/log-files/{log-file}",
    "description": "Download the log file",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/log-files/{log-file}",
    "description": "Delete a given log file",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/events/{topic}",
    "description": "Broadcast event (payload in the body) on specific topic. The {topic} can be a regex.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/custom-protocol/{content}",
    "description": "Manage custom protocol",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/utc",
    "description": "Set 'current' UTC Date.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/last-sentence",
    "description": "Get the last available inbound sentence",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/nmea-sentence",
    "description": "Push NMEA or AIS Sentence to cache, after parsing it. NMEA Sentence as text/plain in the body.",
    "fn": {}
  }
]
```
See how to add `http.RESTRequestManager`(s) to an `http.HTTPServer`, with the `addRequestManager` method.

# Other samples
Many other Web UI examples are available. Just look.  
You will see that some examples need an Internet connection (for Google Maps and Leaflets), some others can run without Internet.  
Some are using WebComponents, some vanilla JavaScript components (directly drawing on HTML5 canvases).  
Your imagination is the limit, use it!  
There is also a [USE_CASES](./USE_CASES.md) document.  
> The WebComponents presented here live in their own repo, at <https://github.com/OlivierLD/WebComponents>. 
> They can be seen live at <https://olivierld.github.io/webcomponents/gallery.html> (it's a biiiiig file).

Examples of Web Admin features will also be presented (in other modules, like [RESTNavServer](../RESTNavServer/README.md)).

# Build and package for production
See the script `to.prod.sh`. It will generate an archive that can be deployed on
a target machine, un-archived, and run.  
To run it:
```
$ ./to.prod.sh
```

Explanations on how to deploy the generated archive are given interactively by the script.  
⚠️ _**DO READ THEM !!!**_ ⚠️   
And _do not hesitate_ to mess with the code !! 🪠

You will notice that this archive will zip all the HTML/CSS/JavaScript/etc resources into a zip file that will be used
at runtime when a Web Interface is required. The `http-tiny-server` can get the resources it needs from a zip, to minimize its footprint.  
In the production environment, what used to be reached from a URL like <http://localhost:8080/web/index.html> should now be <http://localhost:8080/zip/index.html>.
The http port is assigned in the config yaml file used start the multiplexer.

> _**Note**_: This `to.prod.sh` script takes care of the deployment of the Java and Web parts (http server).  
> If your config requires some sensor data to be reached, the corresponding Python
> scripts should be available on your production machine too.  
> See how to do this is [their folder's README.md](../../RaspberryPythonServers/README.md#tcp).

Here is an example of the execution of the `to.prod.sh` script.  
Notice below when the script prompts the user for his input:
- to rebuild the jar file (based on the current sources)
- for the name of the archive to generate (`nmea-dist` here)
- to include Python scripts in the archive
- to drop the directory the archive has been done from

_Example:_
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
| openjdk version "11.0.16" 2022-07-19
| OpenJDK Runtime Environment (build 11.0.16+8-post-Raspbian-1deb11u1)
| OpenJDK Server VM (build 11.0.16+8-post-Raspbian-1deb11u1, mixed mode)
+----------------------------------------------------------------------------------------------------+
| Make sure the current Java version is compatible with the target one!!                             |
+----------------------------------------------------------------------------------------------------+

There is an existing jar-file:
1421050 40M -rw-r--r-- 1 pi pi 40M May 30 09:43 ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
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
Starting a Gradle Daemon (subsequent builds will be faster)

> Configure project :
>> From task compileJava (in rob), using java version 11 
>> From task compileTestJava (in rob), using java version 11 

> Configure project :astro-computer:AstroComputer
+------ Warning: Java version (JAVA_HOME) possible mismatch -----
| In task compileJava (AstroComputer), the java version used 11 is not the expected version 1.8.
+----------------------------------------------------------------
+------ Warning: Java version (JAVA_HOME) possible mismatch -----
| In task compileTestJava (AstroComputer), the java version used 11 is not the expected version 1.8.
+----------------------------------------------------------------

> Configure project :astro-computer:AstroUtilities
+------ Warning: Java version (JAVA_HOME) possible mismatch -----
| In task compileJava (AstroUtilities), the java version used 11 is not the expected version 1.8.
+----------------------------------------------------------------
+------ Warning: Java version (JAVA_HOME) possible mismatch -----
| In task compileTestJava (AstroUtilities), the java version used 11 is not the expected version 1.8.
+----------------------------------------------------------------

> Task :raspberry-sailor:MUX-implementations:NMEA-multiplexer-basic:clean
> Task :common-utils:compileJava UP-TO-DATE
> Task :common-utils:processResources NO-SOURCE
> Task :common-utils:classes UP-TO-DATE
> Task :common-utils:jar UP-TO-DATE
> Task :http-tiny-server:compileJava UP-TO-DATE
> Task :http-tiny-server:processResources NO-SOURCE
> Task :http-tiny-server:classes UP-TO-DATE
> Task :http-tiny-server:jar UP-TO-DATE
> Task :astro-computer:AstroUtilities:compileJava UP-TO-DATE
. . .
> Task :raspberry-sailor:NMEA-multiplexer:jar
> Task :raspberry-sailor:routing:jar UP-TO-DATE
> Task :raspberry-sailor:MUX-implementations:NMEA-multiplexer-basic:compileJava NO-SOURCE
> Task :raspberry-sailor:MUX-implementations:NMEA-multiplexer-basic:processResources NO-SOURCE
> Task :raspberry-sailor:MUX-implementations:NMEA-multiplexer-basic:classes UP-TO-DATE
> Task :raspberry-sailor:MUX-implementations:NMEA-multiplexer-basic:shadowJar

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.6/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 3m 6s
24 actionable tasks: 8 executed, 16 up-to-date
You will need to provide a name for your archive, that will also be the name of the folder where the runtime components will live.
A name like 'nmea-dist' would do the job...
Which (non existent) folder should we create the distribution in ? > nmea-dist
Creating folder nmea-dist
Copying resources
  adding: admin.html (deflated 86%)
  adding: basic.html (deflated 59%)
  adding: console.html (deflated 79%)
  adding: css/ (stored 0%)
  adding: css/night.stylesheet.css (deflated 80%)
  adding: css/graph.ux.03.css (deflated 64%)
  adding: css/screen.css (deflated 45%)
  adding: css/graph.ux.01.css (deflated 63%)
  adding: css/web-components.css (deflated 86%)
  adding: css/rocker.css (deflated 66%)
  adding: css/graph.ux.02.css (deflated 64%)
  adding: css/bground.jpg (stored 0%)
  adding: css/stylesheet.css (deflated 80%)
  adding: css/white.css (deflated 37%)
  adding: css/black.css (deflated 38%)
  adding: examples/ (stored 0%)
  . . .
  adding: widgets/Thermometer.js (deflated 76%)
  adding: widgets/Direction.js (deflated 80%)
  adding: widgets/AnalogDisplay.js (deflated 76%)
  adding: widgets/TrackMap.js (deflated 70%)
  adding: widgets/TimeDisplay.js (deflated 70%)
  adding: widgets/worldmap.js (deflated 67%)
  adding: widgets/JumboDisplay.js (deflated 73%)
  adding: widgets/TWSEvolution.js (deflated 72%)
  adding: widgets/AISMap.js (deflated 72%)
  adding: widgets/SatellitesPlotter.js (deflated 71%)
  adding: widgets/README.md (deflated 9%)
  adding: wsconsole.html (deflated 50%)
Do we package the Python part ? > y
Copying Python stuff from RaspberryPythonServers/python into nmea-dist/python
Done with Python
nmea-dist/
nmea-dist/nmea.mux.ais.test.3.yaml
nmea-dist/nmea.mux.gps.yaml
nmea-dist/kill.all.sample.sh
nmea-dist/mux.sh
nmea-dist/REST.ssd1306.dg.properties
nmea-dist/python/
nmea-dist/python/python/
nmea-dist/python/python/TCP_HTU21DF_server.py
nmea-dist/python/python/pull.python.sh
nmea-dist/python/python/utils.py
nmea-dist/python/python/sample_cache.py
nmea-dist/python/python/REST_BasicCacheForwarder_server.py
nmea-dist/python/python/REST_SSD1306_server_v2.py
nmea-dist/python/python/REST_ZDA_server.py
. . .
nmea-dist/web.zip
nmea-dist/nmea.mux.replay.big.log.yaml
nmea-dist/nmea.mux.gps.tcp.yaml
nmea-dist/ais.mgr.properties
nmea-dist/nmea.mux.zero.yaml
nmea-dist/markers.yaml
nmea-dist/nmea.mux.ais.test.1.yaml
nmea-dist/nmea.mux.kayak.ssd1306.yaml
nmea-dist/build/
nmea-dist/build/libs/
nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
Can we drop the nmea-dist directory ? > y
Ok, moving on.
+--------------------------------------------------------------------------------------------------+
 >> Archive /home/pi/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/nmea-dist.tar.gz ready for deployment.
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

The script above is generating an archive named `nmea-dist.tar.gz`, its name comes from user's input.  
Once the script is completed, transfer the archive on your target machine (using `scp` for example), un-archive it (as shown above, with a `tar -xzvf`), and you are good to go.

If an _update_ is necessary, it is even easier.  
Just run the script `to.prod.sh` again, and _**do not drop**_ the directory at the end.  
The structure of the `nmea-dist` folder (or whatever _**you**_ named it) looks like this:
```
$ tree nmea-dist
nmea-dist
├── ais.mgr.properties
├── build
│   └── libs
│       └── NMEA-multiplexer-basic-1.0-all.jar
├── kill.all.sample.sh
├── markers.yaml
├── mux.sh
. . .
├── nmea.mux.ais.test.1.yaml
├── nmea.mux.ais.test.3.yaml
├── nmea.mux.big.log.nmea-fwd.yaml
├── nmea.mux.gps.sensor.nmea-fwd.yaml
├── nmea.mux.gps.tcp.yaml
├── nmea.mux.gps.yaml
├── nmea.mux.kayak.ssd1306.yaml
├── nmea.mux.replay.big.log.yaml
├── nmea.mux.zero.yaml
. . .
├── nmea-to-text.properties
├── REST.ssd1306.dg.properties
. . .
└── web.zip

2 directories, 17 files
```
This is also the structure generated on the target machine, strictly identical.  
All you need to do this  time is to transfer the jar `NMEA-multiplexer-basic-1.0-all.jar` where it already exists, and - if needed - 
the `web.zip`.

### JDK Versions
You need to make sure that the version you use to package the soft is compatible
with the machine you plan to deploy it on.  
For example, some Raspberry Pi Zero would not support JDK 11. 

#### Switch JDK on Mac:
```
$ /usr/libexec/java_home -V
Matching Java Virtual Machines (2):
9.0.1, x86_64:"Java SE 9.0.1"/Library/Java/JavaVirtualMachines/jdk-9.0.1.jdk/Contents/Home
1.8.0_144, x86_64:"Java SE 8"/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home
```
Then:
```
$ export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_144`
$ export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_211`
$ export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_252`
$ export JAVA_HOME=`/usr/libexec/java_home -v 9.0.1`
$ export JAVA_HOME=`/usr/libexec/java_home -v 11.0.6`
```
or
```
$ export JAVA_HOME=$(/usr/libexec/java_home -v 11.0.6)
$ export JAVA_HOME=$(/usr/libexec/java_home -v 1.8.0_252)
$ export JAVA_HOME=$(/usr/libexec/java_home -v 11.0.7)
```
```
$ export JAVA_HOME=~/graalvm-ce-19.1.1/Contents/Home
$ export PATH=${JAVA_HOME}/bin:${PATH}
```

#### Switch JDK on Debian
```
$ sudo update-alternatives --config java
$ sudo update-alternatives --config javac
```

#### Get the Java Class version
<!--```
$ javap -cp /usr/local/Cellar/opencv/4.3.0_5/share/java/opencv4/opencv-430.jar -verbose org.opencv.core.Core | grep "major"
```-->
```
$ javap -verbose -cp build/libs/NMEA-multiplexer-basic-1.0-all.jar nmea.mux.GenericNMEAMultiplexer | grep "major"
  major version: 52
```
or
```
$ javap -verbose -cp build/libs/NMEA-multiplexer-basic-1.0-all.jar nmea.mux.GenericNMEAMultiplexer | head
Classfile jar:file:///Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/build/libs/NMEA-multiplexer-basic-1.0-all.jar!/nmea/mux/GenericNMEAMultiplexer.class
  Last modified Mar 15, 2025; size 18466 bytes
  MD5 checksum 7526912d8821d89502bcb23d8459d291
  Compiled from "GenericNMEAMultiplexer.java"
public class nmea.mux.GenericNMEAMultiplexer implements http.RESTRequestManager,nmea.api.Multiplexer
  minor version: 0
  major version: 52
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #228                        // nmea/mux/GenericNMEAMultiplexer
  super_class: #69                        // java/lang/Object
```

## A Full example
See [this doc](./HOWTO.md).

---
