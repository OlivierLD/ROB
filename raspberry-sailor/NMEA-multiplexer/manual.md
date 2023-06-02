# NMEA Multiplexer - Technical Manual

[<< Back](./README.md)

The following sections will make references to several resources (Java classes, properties files, etc).
They are all part of this project, so you can refer to them if needed for more details.

The program to start is `nmea.mux.GenericNMEAMultiplexer`, it is driven by a `properties` or `yaml` file,
describing the features required by an instance of the Multiplexer (channels, forwarders, computers, http server, etc).

- [Properties](#properties)
- [Pre-defined channel types](#pre-defined-channel-types)
- [Forwarders](#forwarders)
- [Pre-defined forwarder types](#pre-defined-forwarder-types)
- [Pre-defined computer type(s)](#pre-defined-computer-types)
- [Other properties](#other-properties)
- [Example](#example)
- [Minimal](#minimal)
- [And then...](#and-then)
- [To summarize](#to-summarize)
- [Other resources](#other-resources)


### Properties
Here is a brief description of the properties managed by the `nmea.mux.GenericNMEAMultiplexer`, the ones
present in the file `nmea.mux.properties`, or in the file named as set in the System variable `mux.properties`.

Property names of channels, forwarders and computers follow this pattern:
```
 [element-type].[index].[attribute]
```

Element types can take three values: `mux`, `forward`, or `computer`:

- Whatever begins with `mux.` is a channel
- Whatever begins with `forward.` is a forwarder
- Whatever begins with `computer.` is a computer

For the three categories above, the second item is the index of the element.
Indexes are numbers, mentioned on two digits. Indexes _must_ start at `01` and be
after that incremented by `1`.

For example, `mux.01.xxx`, followed by `mux.02.yyy`.

> _Quick explanation_: To find the first channel, the program looks for a `mux.01.*`.
> If no such entry is found, that would mean for the program that there is no channel to deal with.
> After finding and evaluating `mux.01.xxx`, the program looks for `mux.02.*`. If no
> such channel is found, the program understands that the list of the channels is terminated.
> This is the same for channels, forwarders and computers.

The third part of the property name (the `type` in `mux.0X.type` for example) is the attribute.
_**ALL**_ elements _have_ a mandatory `type` attribute, the other attributes depend on this `type`.

> _**Exception**_: if an element does _not_ have a `type` attribute, then it is a custom element, it _must_ have a _cls_ attribute
> containing the name of the Java `class` to load dynamically, with a `Class.forName`.
> For example, a line like that one
```properties
 forward.02.class=nmea.forwarders.LedBlinker
```
> would tell the Multiplexer to load a forwarder defined in the class `nmea.forwarders.LedBlinker`.
> If the loaded class does not extend the right `superclass` or implement the right `interface`, an error
> will be raised.

#### YAML
> _Note_: Since October 2019, a `yaml` format is also supported for the properties of the multiplexer.
> It's a bit easier than the `properties` syntax.
>
> Example:
```yaml
 #
 # This is an example of the way a MUX could be defined with YAML.
 #
 name: "NMEA with GPS, BME280, LSM303 through TCP"
 description:
   - "Part 1"
   - "Part 2"
   - "Etc..."
 context:
   with.http.server: true
   http.port: 5678
   init.cache: true
   default.declination: 14
   deviation.file.name: dp_2011_04_15.csv
   # Leeway = max.leeway * cos(awa)
   max.leeway: 10
   bsp.factor: 1.0
   aws.factor: 1.0
   awa.offset: 0
   hdg.offset: 0
   damping: 30
   markers: markers.yaml
 channels:
   - type: serial
     port: /dev/ttyS80
     baudrate: 4800
     verbose: false
   - type: tcp
     port: 7001
     verbose: false
   - type: tcp
     port: 7002
     verbose: false
     sentence.filters: HDM,XDR
     heading.offset: 0
 forwarders:
   - type: file
     timebase.filename: true
     filename.suffix: _LOG
     log.dir: logged
     split: hour
   - type: tcp
     port: 8001
 computers:
   - type: tw-current
     prefix: CC
     time.buffer.length: 30, 60, 600
```

Channel `properties` like: 
```properties                                       
mux.01.type=tcp           
mux.01.verbose=false
mux.01.sentence.filters=HDM,XDR
```
are equivalent to `yaml` like
```yaml
channels:
 - type: tcp           
   verbose: false
   sentence.filters: HDM,XDR
```  
> Notice that the yaml does not require `mux.01.xxx` and `mux.02.xxx` as the `properties` do. That 
> makes it a bit more convenient and flexible to use.

#### Pre-defined channel types
> Note: There is a default REST channel type available when `with.http.server` is set to `true`. See below.

- `serial`
    - Serial port input.
    ```properties
    mux.01.type=serial
    mux.01.port=/dev/ttyUSB0
    mux.01.baudrate=4800
    mux.01.verbose=false
    mux.01.reset.interval=60000
    ```
    > The optional `reset.interval` is in milliseconds. It closes and re-opens the Serial port as mentionned.
    > It happens to be useful on some devices...
- `tcp`
    - TCP input
    ```properties
    mux.01.type=tcp
    mux.01.server=ais.exploratorium.edu
    mux.01.port=80
    mux.01.verbose=false
    ```
- `file`
    - Log file replay
    ```properties
    mux.01.type=file
    mux.01.filename=./sample.data/archived.logged.data.zip
    mux.01.zip=true
    mux.01.path.in.zip=2010-11-08.Nuku-Hiva-Tuamotu.nmea
    mux.01.loop=true             # Default true
    mux.01.between-records=500   # Default 500ms
    ```
    > The `filename` can be an archive (zip) or a text file (containing NMEA sentences)
    >
    > If the file is a zip, you set the `zip` property to `true` (default is `false`), and possibly 
    > the `path.in.zip`. If `path.in.zip` is null or invalid, the first entry in the archive will be used.
    > This can be usefull, as the archive may very well contain several log files.
- `ws`
    - WebSocket input. This is acting as a WebSocket _client_. 
    ```properties
    mux.02.type=ws
    mux.02.wsuri=ws://192.168.1.136:9876/
    ```
    > See the examples of WebSocket servers suitable for this channel (like `wsnmea.js`, running on NodeJS).  
      The server is designed to push to every connected client the NMEA data pushed to it (see the `ws` forwarder about that).
- `rnd`
    - Random data generator (for debug)
    ```properties
    mux.04.type=rnd
    ```
- `zda`
    - ZDA Sentence generator (UTC day, month, and year, and local time zone offset)
    ```properties
    mux.01.type=zda
    ```
- `rest`
  - Work in Progress
  - The `jqs` parameter below intends to work like the `jq` utility, on a returned JSON payload 
    - See the [JQ](https://stedolan.github.io/jq/) repo 
    - For JQ (**J**SON **Q**uery) Syntax, see <https://lzone.de/cheat-sheet/jq>, and <https://github.com/eiiches/jackson-jq>
  - For `GET` queries only (...for now)  
    ```properties
    mux.01.type=rest
    mux.01.protocol=http
    mux.01.machine-name=192.168.1.102
    mux.01.http-port=8080
    mux.01.query-path=/mux/cache
    mux.01.query-string=?query=string  # Must include the leading ?, and subsequent &...
    mux.01.jqs=".NMEA_AS_IS | { RMC, GLL }"  # jq-like expression
    mux.01.between-loops=1000   # Default is 1000ms
    mux.01.verbose=false
    ```
    or in `yaml`:
    ```yaml
    channels:
    . . .
    - type: rest
      protocol: http
      machine-name: 192.168.1.105
      http-port: 8080
      query-path: /mux/cache    # /bme280/nmea-data
      query-string: ?query=string  # Must include the leading ?, and subsequent &...
      jqs: ".NMEA_AS_IS | { RMC, GLL }"  # null
      between-loops: 2000  # in ms (default 1000)
      verbose: false
    ```
    <!-- This one is more designed to be extended. -->  
    Look into the repo for more examples.    
    The tricky point is that this has to generate a _valid_ NMEA String, and that requires
    a knowledge of the structure of the payload returned by the service, if not some post-processing.
  
    As it is now, we can deal with REST services returning in the response's payload:
    - A JSON Map
    - A plain object (as a String in `plain/text`)
    
    If it is a map like `{ "one": "$IDAAA,aaaaa*FF", "two": "$IDBBB,bbbbb*FF" }`, the Consumer will
    assume that the values `"$IDAAA,aaaaa*FF"` and `"$IDBBB,bbbbb*FF"` are NMEA 
    valid strings, and will be managed as such. If invalid, they'd be lost (See in `nmea.api.NMEAParser`, method `interesting()`).  
    If it is a plain object, it will be managed as usual, if NMEA-valid.
---

- _**Note**_: there is an "Implicit" REST input (to feed the cache)
    - Like a `rest` input channel (consumer)
    - If the `with.http.server` is running, then there is REST resource
    ```
    POST /mux/nmea-sentence -h "Content-Type: plain/text" -d "$GPRMC,....."
    ```
    - This can be used to feed the cache "by hand".
    - Use system variable `-Drest.feeder.verbose=true` to see the output.

You can also define your own channels (extending `NMEAClient` and with a `reader` attribute in the properties, like `mux.02.reader=nmea.consumers.reader.ReaderSkeleton`).  
Look for `mux.01.class=nmea.consumers.client.WeatherStationWSClient`.  
Do also take a look at the class `nmea.consumers.dynamic.TXTExample`, it shows how to implement your own custom Consumer, along
wit a `yaml` using it, `nmea.mux.dyn.consumer.yaml`.

Channels can use those three attributes: `properties`, `device.filters`, `sentence.filters`:
```properties
mux.01.properties=weather.station.properties
mux.01.device.filters=II, GP
mux.01.sentence.filters=MMB, XDR, MDA
```
`device.filters` and `sentence.filters` both support positive (inclusive) and negative (exclusive) filters.
Negative filters are prefixed with `~`.

A line like
```properties
mux.01.sentence.filters=~MMB, ~GGA, ~GSV
```
means "_everything, but no MMB, no GGA, no GSV_".

A line like
```properties
mux.01.sentence.filters=RMC, XDR, MDA
```
means "_only RMC, XDR or MDA_".

> _Note_: a line like `~RMC, MDA` does not mean much, as it would mean [`no RMC` and `just MDA`]. 
> A line like `MDA` would mean the same thing.

##### A Convention
If a channel definition has a `properties` member, and if there is a `janitor` member in those properties like this:
```yaml
channels:
  - type: zda
    properties: mux-configs/dummy.zda.properties
. . .
```
and
```properties
janitor=default
dummy=stuff
whatever=itizz
```
then:  
If `janitor` is set to `default`, then the default `nmea.consumers.client.Janitor.executeOnClose()` will be executed, with the properties as parameter of the method.  
Otherwise, the `janitor` will be dynamically loaded, and its `executeOnClose(Properties prop)` method will be executed, with the properties as parameters.  
All you need to do is to implement your own `Janitor`, and have it in the classpath. It could then
be mentioned like this:
```properties
janitor=my.own.CustomJanitor
dummy=stuff
whatever=itizz
```
The property values required at runtime (on close) by the `Janitor` would here be the values of `dummy` and `whatever`.



#### Forwarders

_**ALL**_ forwarders can use 2 _optional_ attributes, `subclass` and `properties`:
```properties
forward.XX.type=file
forward.XX.subclass=nmea.forwarders.ExtendedDataFileWriter
forward.XX.properties=validlogger.properties
. . .
```
The lines above means that:
- The `nmea.forwarders.ExtendedDataFileWriter` is a `file` Forwarder (it extends `DataFileWriter`)
- Required extra properties are in a file named `validlogger.properties`.

> _Note_: if `ExtendedDataFileWriter` happens not to extend the class anticipated by the `type`, a runtime error will be raised.

> _**Dynamic loading versus sub-classing**_:
> We've seen before that you have the possibility - using a `class` attribute - to define your own
> elements (Channel, Forwarder or Computer) and dynamically load it at runtime. Here we see the possibility to `extend` a given element type.
> A dynamically loaded element gives the programmer more flexibility and room for invention, but it _cannot_
> be managed by the `admin` web page. A sub-class of a given type of element can be much lighter to write,
> and _is_ manageable by the `admin` web page.

> See `ExtendedDataFileWriter.java` for details.
 
Several "dynamic" forwarders are provided, as examples. See - among others
- `CharacterConsoleWrite.java`
- `SQLitePublisher.java`
- . . .

#### Pre-defined forwarder types

- `serial`
    - Write to a serial port
    ```properties
    forward.02.type=serial
    forward.02.port=/dev/ttyS88
    forward.02.baudrate=9600
    ```
- `tcp`
    - TCP Server
    ```properties
    forward.01.type=tcp
    forward.01.port=7001
    ```
  > _Note_: using `forward.XX.properties`, you can refer to a properties file containing a `with.ais` properties.
  > Default value is `true`, it can be set to `false` to prevent the AIS strings to be re-broadcasted.
  >
  > Along the same lines, there is a `put.ais.in.cache` property defaulted to `true`. Set it to `false` if you do not need the 
  > parsed AIS data to end up in the NMEA Cache. 
  >
- `rest`
    - Forward the NMEA data (string by string) to a REST endpoint (WiP).
    - Can be used - for example - to push data to the implicit REST channel (see above)
    ```properties
    forward.02.server.name=192.168.42.6
    forward.02.server.port=8080
    forward.02.rest.resource=/mux/nmea-sentence
    forward.02.rest.verb=POST
    forward.02.rest.protocol=http
    forward.02.http.headers=Content-Type:plain/text,Whatever:whateverYouLike
    ```                                                                                                      
- `gpsd`
    - GPSD Server
    ```properties
    forward.06.type=gpsd
    forward.06.port=2947
    ```
- `file`
    - Log file output
    ```properties
    forward.XX.type=file
    # Option 1
    forward.XX.filename=./data.nmea
    # Option 2
    forward.XX.timebase.filename=true
    forward.XX.filename.suffix=_LOG
    forward.XX.log.dir=logged
    forward.XX.split=min|hour|day|week|month|year
    #
    forward.XX.append=true
    forward.XX.flush=true
    ```
    > _Explanations_:
    > - `timebase.filename` default value is `false`
    > - if `timebase.filename` is `false` then a `filename` is expected to be provided (default is `data.nmea`).
    > - if `timebase.filename` is `true` then log file name will be based on the time the logging was started, like `2018-10-22_20:04:00_UTC.nmea`.
    >   - if `filename.suffix` exists (default is an empty string) like `LOG_`, then log file name will be `LOG_2018-10-22_20:04:00_UTC.nmea`.
    >   - if `log.dir` exists (default is `.`) log files will be generated in this directory (located under the working directory).
    >   - if `split` exists (default is `null`) and one of `min`, `hour`, `day`, `week`, `month`, `year`, then a new log file will generated every `min`, `hour`, `day`, `week`, `month`, or `year`.
    > - `flush` will write to the disk every time a record is written. Default is `false`.
    
- `ws`
    <!-- TODO: See https://www.baeldung.com/java-websockets -->
    - WebSocket server. 
    > Pushes NMEA data to an _**external**_ WebSocket server (NodeJS for example, does the job). Unlike for `tcp` where the NMEA-multiplexer acts as a TCP server,
    the NMEA-multiplexer is _**not**_ acting as a WebSocket server.  
    For a NodeJS server example, see `wsnmea.js`. Can be started with an `npm start`.
    ```properties
    forward.07.type=ws
    forward.07.wsuri=ws://localhost:9876/
    ```
- `wsp`
    - WebSocket Processor.  
    > This is more like an example of a transformer.    
    To be used with other apps, like the pebble one.  
    The transformer turns the content of the NMEA Cache into the expected format.    
    _May require some customization!!!_    
    See <https://github.com/OlivierLD/SmartWatches/tree/master/pebble/NMEA>
    ```properties
    forward.07.type=wsp
    forward.07.wsuri=ws://localhost:9876/
    ```
- `console`
    - Console output
    ```properties
    forward.01.type=console
    ```
- `rmi`
    - RMI Server
    ```properties
    forward.05.type=rmi
    forward.05.port=1099
    forward.05.name=RMI-NMEA
    ```
- `nmea-cache-publisher`
  - Can be used to PUT/POST the full cache (in JSON format) to a REST server.
  ```yaml
  - type: nmea-cache-publisher
    between-loops: 1  # in seconds
    rest.protocol: http
    rest.machine-name: 192.168.1.103
    rest.port: 8080
    rest.resource: /ssd1306/nmea-data
    rest.verb: PUT
    # rest.query.string: ""
    verbose: true
  ```
  - The config above tells the multiplexer to `PUT` the cache (JSON-formatted, with a `COntent-Type: application/json` header) to `http://192.168.1.103:8080/ssd1306/nmea-data` every 1 second.
  Then it is the server's (the one running on `192.168.1.103:8080` here) job to do what has to be done with the data.  
  See such an example in `REST_SSD1306_server_v2.py`, it is a REST server written in Python, displaying data on an SSD1306 oled screen.

You can also implement your own forwarder (implementing the `Forwarder` interface).

Look for `forward.02.class=nmea.forwarders.RESTIoTPublisher`

##### An interesting forwarder use-case
There is in the code a `nmea.forwarders.NMEAtoTextProcessor`, that is not to be mentioned by its type, but as a custom Forwarder, like in
```yaml
forwarders:
  - class: nmea.forwarders.NMEAtoTextProcessor   # An interesting use-case.
    properties: mux-configs/nmea-to-text.properties
```
As you would see in the code, this forwarder translates the NMEA content stored in the cache in to
human-readable strings, to be displayed "somewhere".  
The strings to display are defined in the properties file attached to the forwarder,
`mux-configs/nmea-to-text.properties` in the above:
```properties
#
# display.time is the amount of seconds a given value remains displayed before switching to the next one.
#
display.time=10
screen.verbose=false
#
# The DelegateConsumer
data.consumer=nmea.forwarders.delegate.SSD1306RESTConsumer
consumer.properties=mux-configs/REST.ssd1306.dg.properties
#
# Available data:  See in NMEAtoTextProcessor, DisplayOptions.
#
to.display=POS,AWS,TWS,BSP,BSP_KMH,SOG,HDG,COG,GPS
```
This tells the forwarder to display strings named  as in the property `to.display`.  
The default behavior for `nmea.forwarders.NMEAtoTextProcessor` is to display the required text in the console:
```
Definition Name: Replay big log file.
-- Description --
Forward data to "NMEA to Text"
Work in Progress
-----------------
Log available in global, level INFO
Log file pattern mux.log
nmea.computers.current.LongTimeCurrentCalculator is starting...
nmea.computers.current.LongTimeCurrentCalculator is starting...
nmea.computers.current.LongTimeCurrentCalculator is starting...
Starting new http.HTTPServer (verbose false)
POSITION:
RMC not ready yet!
---------------
1,674,916,654,786 - Port open: 8080
1,674,916,654,786 - http.HTTPServer now accepting requests
POSITION
9°06.46'S
140°12.52'W
---------------
POSITION
9°06.46'S
140°12.52'W
---------------
POSITION
9°06.46'S
140°12.53'W
---------------
POSITION
9°06.47'S
140°12.54'W
---------------
. . .
```
The displayed data changes every 10 seconds, as defined in the property `display.time`.  
This default behavior is coded in a `Consumer<List<Stgring>>`, defaulted in the code to
```java
    // Default Consumer.
    private final Consumer<List<String>> DEFAULT_DISPLAY_CONSUMER = (dataList) -> {
        dataList.forEach(line -> System.out.println(line));
        System.out.println("---------------");
    };

    private Consumer<List<String>> displayConsumer = DEFAULT_DISPLAY_CONSUMER;
```
This behavior can be overridden, by supplying the name of a `DelegateConsumer` in the properties file, as done above in
the properties `data.consumer`, optionally `consumer.properties`.  
The `data.consumer` mentions `nmea.forwarders.delegate.SSD1306RESTConsumer`, available in this repository.  
Look into it for details.  
This class is using properties described in `consumer.properties`, `mux-configs/REST.ssd1306.dg.properties`:
```properties
ssd1306.dg.protocol=http
ssd1306.dg.server-name=192.168.1.101
ssd1306.dg.port=8080
ssd1306.dg.verb=PUT
ssd1306.dg.resource=/ssd1306/nmea-data
ssd1306.dg.verbose=false
```
This `DelegateConsumer` is doing a REST PUT request to some REST server, connected to an oled screen SSD1306, that will
display the required data. This REST server is in this case written in Python,
the code is in `REST_SSD1306_server.py`, in this repository.

| POS | AWS | BSP |
|:---:|:---:|:---:|
| ![POS](./docimages/SSD1306_POS.jpg) | ![AWS](./docimages/SSD1306_AWS.jpg) | ![BSP](./docimages/SSD1306_BSP.jpg) | 


#### Pre-defined computer type(s)

- `tw-current`
  - One computer, to calculate both True Wind and Current (GPS Based, with possibly several time buffers).
- `dew-point-computer`
  - Calculate the dew point temperature if relative humidity and air temperature are available. 

> _Important_: Computers may need data coming from the various channels. Those data will
be stored in a cache _if the property `init.cache` is set to `true`_. See below.

You can also define your own computers (extending `Computer`).

Look for `computer.02.class=nmea.computers.ComputerSkeleton`

See also the computer `nmea.computers.AISManager`. It is a computer to load as in (`yaml` version)
```yaml
computers:
  - class: nmea.computers.AISManager
    properties: ais.mgr.properties
    verbose: false
```
It's an example/WiP of the way to use AIS data to detect collision threats.
> The properties file (`properties` member in the `yaml` above) can also include a `collision.threat.callback` property.
> - If it is missing, nothing will be done when a collision threat is detected.
> - If it is set to `default`, the warning message will be displayed in the console
> - If it is set to a class name, like in:  
> ```
> collision.threat.callback=nmea.computers.SpeakingCallback
> ```
> the class name _must_ implement a `Consumer<String>`.
> - If the class is not found, then nothing happens.
> - Same if the class is not a `Consumer<String>`.
> - See a valid _example_ in `nmea.computers.SpeakingCallback.java`, that will <u>speak out loud</u> the warning message.

#### Other properties

```properties
name=Basic MUX configuration.
description.01=Basic MUX Config
description.02=HTTP port is 9999
with.http.server=yes
http.port=9999
#
init.cache=true
deviation.file.name=dp_2011_04_15.csv
# Leeway = max.leeway * cos(awa)
max.leeway=10
#
bsp.factor=1.0
aws.factor=1.0
awa.offset=0
hdg.offset=0
#
default.declination=14
damping=30
markers=markers.yaml
```

`with.http.server` is set to `no` by default. `yes` means that you will have access to
some `REST` services, for admin and other purposes.

If `with.http.server` is set to `yes` (or `true`), the default http port is `9999`. It can be overridden by `http.port` if needed.

> _Note_: with `with.http.server` set to `true` comes a REST channel: `POST /mux/nmea-sentence -d '$GPRMC,165007.445,A,3806.5201,N,12251.7777,W,001.7,192.6,200818,,,A*7B'`
> 
> This allows you to insert NMEA or IAS Data in the cache through a REST endpoint. 

`init.cache` is set to `false` by default. A cache - accessible by `Computers` will be initialized if
`init.cache` is set to `true`.
The cache is a `Map<String, Object>`, see `context.NMEADataCache` for details.

> _Note_: If the property `with.http.server` is set to `true`, then the cache might also be accessible through a REST interface, as a `json` object reflecting the `Map` above
> (also see in the `RESTNavServer` for details).
```
 GET /mux/cache
```

If `init.cache` is set to `true` or `yes`, the following parameters will be taken in account when inserting data in the cache:
- `bsp.factor` Boat Speed Factor, number, `0` to `n`
- `aws.factor` Apparent Wind Speed Factor, number, `0` to `n`
- `awa.offset` Apparent Wind Angle offset in degrees, from `-180` to `180`
- `hdg.offset` Heading offset in degrees, from `-180` to `180`

`default.declination` will be used if not returned by the GPS (as it could, depends on your GPS). `E` is `+`, `W` is `-`.

`max.leeway` is used to calculate the leeway. The formula used here is:
```
 if awa > 90 and awa < 270 (i.e. cos(awa) < 0) then leeway = 0
 otherwise, leeway = max.leeway * cos(awa)
```
can also be more simply expressed as
```
 leeway = max.leeway * max(0, cos(awa))
```

`damping` (default is `1`) unused for now (Aug-2018), but will be.

`deviation.file.name` mentions the name of a CSV file, like - for example - `dp_2011_04_15.csv`. The
default value is `zero-deviation.csv`.

The format of this Comma-Separated-Values (CSV) file is the following one:
```csv
0.0,-0.9830777902388692
5.0,-0.011026572256005562
10.0,0.9376226337606713
15.0,1.8481417760529473
20.0,2.706968419259063
25.0,3.502010498068172
...
```
Each line contains two fields, the first one is the **Compass** Heading, the second one is the corresponding deviation.
Such a file can be rendered like this:

<img src="./docimages/deviation.curve.png" title="deviation curve" width="318" height="440">

`markers` (default null) can be used to mention the name of a `yaml` file containing user-defined markers. Those markers
will be stored in the cache (like the deviation curve data for example), and can be referred to by some other components (like Map UIs).  
The structure of this file is the following one:
```yaml
markers:
  - latitude: 47.705 
    longitude: -3.105 
    label: Locoal
  - latitude: 47.661667 
    longitude: -2.758167
    label: Vannes
  - latitude: 47.677667
    longitude: -3.135667
    label: Belz
  - latitude: 49.293167 
    longitude: -0.098833
    label: Dives
  - latitude: 37.7489 
    longitude: -122.5070
    label: SF
  . . .
```
`markers` is an array of objects containing 3 members:
- `latitude` as a number
- `longitude` as a number
- `label` as a string


### Example
Here is an example of a simple properties file driving the Multiplexer:
```properties
#
#  MUX definition.
#
with.http.server=yes
http.port=9999
#
# Channels (input)
#
mux.01.type=serial
mux.01.port=/dev/ttyUSB0
mux.01.baudrate=4800
mux.01.verbose=false
#
# Forwarders
#
forward.01.type=tcp
forward.01.port=7001
#
forward.02.type=file
forward.02.filename=./data.nmea
#
init.cache=true
```
This file tells the Multiplexer to:
- Read the Serial port `/dev/ttyUSB0` with a baud rate of `4800` bauds
- Forward the NMEA data on `tcp` port `7001`
- Log the data into a file named `data.nmea`.

As `with.http.server` is set to `yes`, an `admin` web page will be available on port `9999` (at http://localhost:9999/web/admin.html).

And `init.cache` is set to `true`, meaning that a Computer (not mentioned here) would be able to pick up data from the NMEA Cache.

#### Minimal
_Note_: Two system variables can be used to set the default position (in case no GPS is available) at runtime:
```
...
JAVA_OPTS="$JAVA_OPTS -Ddefault.mux.latitude=37.7489 -Ddefault.mux.longitude=-122.5070" # SF.
java -cp $CP $JAVA_OPTS navrest.NavServer
```
> `navrest.NavServer` is an example - part of this project - involving the `NMEA-Multiplexer`.

A ZDA Sentence can be generated from the system time. A multiplexer driven by
this minimal properties file:
```properties
with.http.server=yes
http.port=9999
#
# Channels (input)
#
mux.01.type=zda
#
#
# No Forwarders
#
#############################################################
#
init.cache=true
#
```
can be accessed from a Web UI (http://localhost:9999/web/webcomponents/console.gps.html?style=orange&bg=black&border=n&boat-data=n), to render astronomical data.

![Web UI](./docimages/minimal.png)

In this case:
- there is only one channel (`zda`) providing the date and time (UT) (`mux.01.type=zda`)
- the position is provided by the user at runtime (`-Ddefault.mux.latitude=37.7489 -Ddefault.mux.longitude=-122.5070`)
- the data are pushed to the cache (`init.cache=true`)
- the cache is accessed from the Web UI through REST services, see in the WebPage code the statements like
```js
function getNMEAData() {
  return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'GET', 200, null, false);
}
```

All you need to know in this case is your location, all the rest is taken care of.

##### Bonus
You can even have _no_ `channel` at all.
In this case, you have the possibility to provide the UTC time to the cache using a service like
```
 PUT /mux/utc { epoch: 1535491435769 }
```
In this case, the cache will reflect this `epoch`, and all celestial data will be computed accordingly, from the position
supplied as above. This allows you to see what the celestial configuration looks like
at any point in time.

### And then...
An example of an implementation of the `NMEA-Multiplexer` along with several chunks of REST Services can be found
in the module `RESTNavServer`, part of this project.

This is typically what makes a Web page like the above available.
The data like `position` and `time` can come from an NMEA data source (they can be emulated, as we've seen), and the astronomical data
would be computed by REST services, based on the time and position coming from the NMEA cache.

Along the same lines, you can have (and you _have_ in the `RESTNavServer`) services to
- compute tide curves
- publish nautical almanacs
- compute astronomical data
- compute routing
- etc...

Once again, the idea (if not the goal) here is to _totally_ de-couple data from their rendition.
NMEA Multiplexer and REST Services provide all the server side data and abstraction,
all the Graphical User Interface (GUI) can then be provided by Web Pages, using technologies like HTML5, CSS3, WebComponents, WebGL, etc, all of those allowing
_local_ as well as _remote_ access from laptops, tablets, smartphones, character consoles, etc.
The _only_ requirement for a client is a network access (with obvious fluency with protocols like HTTP or TCP).

> Note: See an example of a simple standalone TCP Client in the test directory, at `nmea.consumers.reader.TCPReaderTest`

#### To summarize
The NMEA-Multiplexer pulls data from `channels`, possibly computes some using `computers`, and forwards (broadcasts) them through `forwarders`.

All those data are represented using the NMEA format.

In addition, those data can be injected in a cache (as they are - NMEA - and in a `json` format, to avoid re-parsing on the client side), so they can be accessed through REST services. The
HTTP server serving the REST requests can also serve HTML requests, and behave like a web server.

`channels`, `computers` and `forwarders` are defined by some configuration (`properties`) files used when starting the Multiplexer, and they can also be later on added, modified or removed when the Multiplexer is running, through some admin REST services. Along the exact same lines, there is an Admin Web UI also available.

#### Other resources
- [Case Study](./casestudy.md)

-----------------
See the code for more details. _This_ is Open Source 😜
