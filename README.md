# ROB (Raspberry-Pi On Board)
### Using the Raspberry Pi <u>at sea</u>.
| _**100% West Made. Fait 100% à l'ouest.**_ |
|:------------------------------------------:|
| ![A l'ouest, Britanny, California](./a.l.ouest.jpeg) |

Java and Python(3).

## Preamble
One _major_ requirement here is to be able to do everything _<u>at sea</u>_ - that means with _**NO**_ Internet access.
Some operations (like the build of the project 🙄...) would require an Internet connection. But definitely, we tried here to keep those requirements to a bare minimum. The runtime part can be done offline.  
Routing calculation (based on GRIBs and faxes you can receive with an SSB), almanac publication, all this can be done with on-board resources only.


## What this is
We intend here to deal with the data provided by an NMEA Station (Boat Speed, Wind data, ...), a GPS, and maybe additional boards (like Atmospheric sensors like BME280, very cheap).  
We want to be able to:
- Forward the read and calculated data to other softwares (like OpenCPN, SeaWi, ...)
- Calculate True Wind (Speed and Direction)
- Calculate current, in real time
- Calculate tides - and publish tide almanacs
- Calculate celestial data - and publish celestial almanacs
- Calculate routing - based on GRIB files (receivable at sea, yes. I did it)
- Log everything for future replay
- Provide all kinds of user interfaces, accessible from laptops, tablets, cell-phones...
- Provide the way to come up with your own extensions and custom User Interfaces.
- ...and all this kind of things.

All this, running on a Raspberry Pi.

---

This project came to light after [Raspberry Coffee](https://github.com/OlivierLD/raspberry-coffee).  
`Raspberry Coffee` is mainly written in Java (and other JVM languages), and can communicate with sensors and actuators using [PI4J](https://pi4j.com/), itself relying on [WiringPi](http://wiringpi.com/).

> In a navigation environment, NMEA data usually come through a Serial Port, which does not require much in term of additional software (we use here `librxtx-java`, easy to install and to use, and it seems not to present restrictions regarding the JDK version).
> Sensors can be welcome on board though, to add atmospheric data to the existing NMEA flow, like Pressure, Air Temperature, Humidity, etc.
> This is where extra Java frameworks could be needed, to deal with the Raspberry Pi's GPIO header the sensors are connected on.  

I have written many drivers for those boards (sensors and actuators), in Java, to enable native communication with the code.  

And then, Wiring Pi became deprecated, and subsequently, PI4J V1 as well. Too bad.  

Other frameworks are available for this kind of operations, [PI4J V2](https://github.com/Pi4J/pi4j-v2/) was released, [diozero](https://github.com/mattjlewis/diozero) is also available...
But those guys have restrictions on the version of the JDK, they seem to need at least a JDK 11, not supported on small Raspberry Pis like the `Raspberry Pi Zero W`. Too bad, again.

So, there is something smart to come up with here 🤔.

_**Here is a thing**_: for all the sensors and actuators, we usually have some code, written by the board provider - usually in Python. This is the code that has been re-written in Java, to enable a native communication with those boards, based on the frameworks mentioned before.
If a framework becomes obsolete, or if it needs upgrades, then so does the Java code...  
We want here to find a way to get rid of this kind of nasty occurrences.  
So, an idea would be to keep this code as it is - it's working in Python, and the board provider will take care of updates, if needed - and find a way to establish a communication between this Python code and Java...

_**We will give TCP a try**_, for this kind of Python-to-Java communication.  
This will also minimize the amount of dependencies to deal with (and eventually, the size of the archives).  
In short, we would wrap the Python code into some sort of TCP server, which itself can be accessed from Java, natively.
TCP is cool enough to be language agnostic.

And on top of that, several parts of the code deserved some cleanup, and this is also an opportunity to minimize
and consolidate the number of dependencies to external libraries. For example, for the Java-to-JSON part, only Jackson is now used.

The web pages and scripts _**do not rely on any framework**_ (_no_ JQuery, Underscore, etc). It's 100% organic.
It's using vanilla ES6 features, like Promises.

## How the repo is organized
At the root, we have some generic building blocks, like
- `http-tiny-server`
- `common-utils`
- `Serial-IO` and `SerialRxTx`

The `Java-TCP-Python` module gathers the different experiments done to establish
a bridge between Java and Python.

In the `astro-computer` directory, there are two modules for celestial calculations (in several
languages: Java, Scala, Python, C, go, ES6, and some Jupyter Notebooks).

All those things will come together in the directory `raspberry-sailor`.  
It contains REST wrappers (usable from the `http-tiny-server`) around the features we will need, like NMEA Parser,
Tide calculations, Routing (GRIB management), etc.  
The main building block in the `NMEA-multiplexer`. This one can run as it is, but it can also be enriched end extended.
> _**Note**_: The `NMEA-multiplexer` contains "some" Python TCP servers for sensor data access.

It provides REST access to the data it deals with, so all its features can be accessed through HTTP (from Services, and/or Web pages).   
Illustrations of the ways to put it to work are available under `MUX-implementations`.  

## Back end, front end
Many languages can take care of back-end computing...  
Front-end UI is more tricky. Swing is a Java option, Python has other possibilities, same for C..., but they're all different.  
_**So**_, to make everyone happy, we will here do all the back-end computing in whatever language you want, and make it REST-accessible, with - when required - a JSON payload.  
A web-enabled REST client (HTML5, ES6, CSS3) will be able to take care of displaying those data; all you need is a (recent) browser.
No need to deal with Android, iOS, Windows..., the goal here being to simplify the maintenance and portability.

## Misc notes
When building on small board, do not hesitate to exclude some demanding tasks, like
```
$ [...]/gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```

## Misc links to look at...
- <https://saillogger.com/>
- <https://github.com/itemir/rpi_boat_utils>
- <https://gpsd.gitlab.io/gpsd/>
- <https://celnav.de/> (my favorite)
- <http://www.tecepe.com.br/nav/>
- <http://navastro.free.fr/> (in French)

---
More to come...

