# ROB (Raspberry-Pi On Board)

Java and Python(3).

| _**100% West Made. Fait 100% à l'ouest.**_ |
|:------------------------------------------:|
| ![A l'ouest](./a.l.ouest.jpeg) |


This project came to light after [Raspberry Coffee](https://github.com/OlivierLD/raspberry-coffee).  
`Raspberry Coffee` is mainly written in Java (and other JVM languages), and can communicate with sensors and actuators using [PI4J](https://pi4j.com/), itself relying on [WiringPi](http://wiringpi.com/).

> In a navigation environment, NMEA data usually come through a Serial Port, which does not require much in term of additional software (we use here `librxtx-java`, easy to install and to use, and it seems not to present restrictions regarding the JDK version).
> Sensors can be welcome though, to add atmospheric data to the existing NMEA flow, like Pressure, Air Temperature, Humidity, etc.
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
the number of dependencies to external libraries.

## How the repo is organized
At the root, we have some generic building blocks, like
- `http-tiny-server`
- `common -utils`
- `Serial-IO` and `SerialRxTx`

The `Java-TCP-Python` module gathers the different experiment done to establish
a bridge between Java and Python.

In the `astro-computer` directory, there are two modules for celestial calculations (in several
languages: Java, Scala, Python, C, go, ES6, and some Jupyter Notebooks).

All those things will come together in the directory `raspberry-sailor`.  
It contains REST wrapper (usable from the `http-tiny-server`) around the features we will need, like NMEA Parser,
Tide calculations, Routing (GRIB management), etc.  
The main building block in the `NMEA-multiplexer`. This one can run as it is, but it can also be enriched end extended.
It provides REST access to the data it deals with, so all its features can be accessed through HTTP (from Services, and/or Web pages).   
Illustrations of the ways to put it to work are available under `MUX-implementations`.  


---
More to come...

