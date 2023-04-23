<table width="100%">
  <tr>
<td valign="top">
<h1>ROB (Raspberry-Pi On Board)</h1>
<h3>Using the Raspberry Pi, <u>at sea</u>.</h3>
</td>
<td valign="top">
<img src="./a.l.ouest.jpeg" alt="A l'ouest, Britanny, California" title="A l'ouest, Britanny, California">
</td>
  </tr>
</table>
Java and Python(3).

#### Warning 1
The scripts presented in this repo are written for `bash` shell.  
As such, they run on Linux, Mac OS, and Windows 10+ (I was told so).

#### Warning 2
To keep this project compatible with as many Raspberry Pi models as possible, we kept the Java code compatible with
Java 8. Some syntax improvements would be possible, if Java 11 was the only one in the picture, but they're - for now - commented.  
This is done intentionally.

---
#### Some links
- [Get Started](./GET_STARTED.md), fast
- Package for Prod [example](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/USE_CASES.md)
- [Sample implementation and demos](./raspberry-sailor/MUX-implementations/RESTNavServer/README.md), REST services, Web Interfaces, and more.
---

## Preliminary note
There is in this repo a `repository` branch, that can be used as a Maven repo. See [here](./MAVEN-REPO.md) for details.

## Preamble
The goal here is to be able to fulfil the computing needs of a navigator with a Raspberry Pi (we'll discuss the different models later).
The Raspberry Pi (see <https://www.raspberrypi.org/> and <https://www.raspberrypi.com/>) is a small and _cheap_ single-board computer, running a clone of Linux, it's hard drive is an SD card, its power consumption is low, and
it comes with a GPIO header that can be used to read sensors, or feed actuators.  
The more I use it, the more I like it.

One _major_ requirement here is to be able to do everything _<u>at sea</u>_ - that means with _**NO**_ Internet access, nothing in the cloud.  
There can be a network on board, emitted by one machine (...like the Raspberry Pi), with several other machines connected on it to visualize or manage the data going back and forth,
but definitely _**NO**_ Internet access. We could call this "flake" computing 😉.   
Some operations (like the build of the project 🙄...) would require an Internet connection. But definitely, we tried here to keep those requirements to a bare minimum. The runtime part can indeed happen offline.  
NMEA Data management, routing calculation (based on GRIBs and faxes you can receive with an SSB), almanac publication, all this can be done with on-board resources only.  
At sea, the Raspberry Pi can run 24x7, and its power consumption remains ridiculous.

This project is opening several possibilities, from simple data logging to celestial almanac computation and publication. Depending on what you want or what you need,
a Raspberry Pi Zero might be enough. I've tested it, the basic `NMEA-multiplexer` runs fine on it.  
Now, if you want to fully featured navigation server (as in the module `RESTNavServer`), then you might want to beef-up your config, with a Raspberry Pi Model 4...

When we will be using extra components (like Atmospheric data sensors, small screens), you will find links to some providers of such devices, this will
give an idea of the budget you'll need (it's usually ridiculous).

And another cool thing is that we will provide a Web interface for data rendering, as well as for the administration of the system.  
Those will mostly rely on some REST services, illustrated by many examples.

As it is now, you might need to like playing with bits and pieces of code to get to where you want to be... I'm working on it, I'm trying to bring it to a wider audience.

## What this is
We intend here to deal with the data provided by an NMEA Station (Boat Speed, Wind data, ...), a GPS, and maybe additional boards (like Atmospheric sensors like BME280, very cheap).  
We want to be able to:
- Forward the read and calculated data to other software programs (like OpenCPN, SeaWi, ...)
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

> _**Note**_: for now, we deal _only_ with `NMEA0183`, as - as far as I know - no GPS deals (yet) with `NMEA2000`.
> Working on it.

---

## Background

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

> The reason why I originally went for Java is because Java is a programming language, as opposed to Python, which sounds more - to me - like a scripting language.  
> I suspect that this last sentence may cause some eyebrows to raise, but I still believe it.  
> Java is designed to scale, it has amazing debug capabilities (including _remote_ debugging), it scales, it can be 
> distributed, it is **_natively_** object-oriented, it's recursive (Java knows about Java), it's compiled - and not interpreted, it can deal with concurrent accesses, etc.  
> Python is great, and keeps getting more and more popular, for good reasons. One of them being that it may look simpler than Java,
> and closer to the mindset of a scientist (like a mathematician).  
> If you look at those charts ranking the different languages popularity, you would see Python's popularity keeping rising, and - for example - C's popularity stagnating at 
> bottom of the scale... But still, at least 90% of the Operating Systems are written in C. There must be a reason.  
> Popularity and efficiency might not be always in sync... Riding a cab and building a car are two different things.   
> There is obviously a lot to say about that, but I'll stop here; this could turn into an endless conversation (ping me if you want).  
> But now, I want to bridge a gap between Java and Python. Let's be positive.

_**We will give TCP and REST a try**_, for this kind of Python-to-Java communication.  
This will also minimize the amount of dependencies to deal with (and eventually, the size of the archives).  
In short, we would wrap the Python code into some sort of TCP or REST server, which itself can be accessed from Java, natively.
TCP and REST are cool enough to be language agnostic.

And on top of that, several parts of the code deserved some cleanup, and this is also an opportunity to minimize
and consolidate the number of dependencies to external libraries. For example, for the Java-to-JSON part, only Jackson is now used.

The web pages and scripts _**do not rely on any external framework**_ (_no_ JQuery, Underscore, etc). It's 100% organic.
It's using vanilla ES6 features, like Promises. <!-- https://kinsta.com/blog/javascript-libraries/ -->
> _**Note**_: Those JavaScript frameworks (JQuery, Underscore, React.js, etc) **are** great tools. The goal here is to minimize the dependencies,
> as well as the final volume of the code and archives.

And we will try to implement Consumers, Forwarders and Computers as pluggable components, to facilitate the required customizations.

## How the repo is organized
At the root, we have some generic building blocks, like
- `http-tiny-server`
- `common-utils` and other similar modules
- `Serial-IO` and `SerialRxTx`

The `Java-TCP-Python` module gathers the different experiments done to establish
a bridge between Java and Python. It's more like a playground.

In the `astro-computer` directory, there are two modules for celestial calculations (in several
languages: Java, Scala, Python, C, go, ES6, and some Jupyter Notebooks).

All those things will come together in the directory `raspberry-sailor`.  
It contains REST wrappers (usable from and by the `http-tiny-server`) around the features we will need, like NMEA Parser,
Tide calculations, Routing (and GRIB management), etc.  
The main building block is the `NMEA-multiplexer`. This one can run as it is, but it can also be enriched end extended.
> _**Note**_: The `RaspberryPythonServer` contains "some" Python TCP and REST servers for sensors and actuators data access.

It provides REST access to the data it deals with, so all its features can be accessed through HTTP (from Services, and/or Web pages).   
Illustrations of the ways to put it to work are available under `MUX-implementations`.  

## Back end, front end
Many languages can take care of back-end computing...  
Front-end UI is more tricky. Swing is a Java option, Python has other possibilities, same for C..., but they're all different.  
_**So**_, to make everyone happy, we will here do all the back-end computing in whatever language you want, and make it REST-accessible, with - when required - a JSON payload.  
A web-enabled REST client (HTML5, ES6, CSS3) will be able to take care of displaying those data; all you need is a (recent) browser.
No need to deal with Android, iOS, Windows..., **_the goal here being to simplify the data access, maintenance and portability_**.  
> _**Note**_: We will see later what can be done with Docker.  

## Build Techniques
Programming languages used here are Java, and in some cases Python. The Web UI is done in pure HTML5/ES6/CSS3, without any external framework.  
The build is done using [`gradle`](https://gradle.org/).  
If you're not familiar with those tools and techniques, check out the [GET_STARTED](./GET_STARTED.md) page.

## Misc notes
When building on small boards, do not hesitate to exclude some demanding tasks, like
```
$ [...]/gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```

You might also want to exclude some tests (look into their code for details)
```
$ [...]/gradlew clean build -x :raspberry-sailor:NMEA-multiplexer:test
```

## Highlights
- [Read NMEA data, log, forward, process](./raspberry-sailor/NMEA-multiplexer/README.md)
- Notebooks, [how NMEA Parsers work](./raspberry-sailor/NMEA-Parser/notebooks/HowItWorks.ipynb)
- [Build your own deviation curve](./raspberry-sailor/Swing/Deviation-Tool/README.md)
- [Build your own polars](./raspberry-sailor/PolarSmoother/README.md) (in progress)
- [Read and render GRIB files](./raspberry-sailor/RESTRouting/README.md) (in progress)
- Celestial Almanacs publication
- Tide Tables publication
- Implementation and customization examples:
  - [Basic](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/README.md)
  - [RESTNavServer](./raspberry-sailor/MUX-implementations/RESTNavServer/README.md), with many examples

## Misc links to look at...
- <https://saillogger.com/>
- <https://github.com/itemir/rpi_boat_utils>
- <https://gpsd.gitlab.io/gpsd/>
- <https://celnav.de/> (my favorite)
- <http://www.tecepe.com.br/nav/>
- <http://navastro.free.fr/> (in French)
- Get Started: [For dummies](./GET_STARTED.md)
- Hints and Tips [here](./HintsAndTips.md)

## Customize, Build, and Deploy
This is a vast topic.  
Again, we are targeting here all kinds of machines, from big ones, to the smallest Raspberry Pi Zero - which can indeed take care of the job.  
The thing is that a Raspberry Pi Zero might not be abe to take care a routing, almanac publishing, and such demanding tasks.  
This is why we do not provide here the biggest possible config with all possible options, as it might not be suitable for your targeted environment.
Each case is a particular case, there is no "one-size-fits-all" config here...  
We will see later if Docker could be a way to approach this problem, by preparing several Docker images, suitable for different hardware configurations. More to come. 

- WiP, look into [to.prod.sh](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/to.prod.sh), for now,
and the [USE_CASE](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/USE_CASES.md#use-case-2) page.

Also, by using TCP and REST, all sensors and actuators using those protocols can be _distributed_, they do not have to be located on the same board.
You can very well have the GPS and NMEA data read from one Raspberry Pi somewhere, the Magnetometer (like an LSM303) somewhere else, and all those guys
feeding the NMEA-multiplexer, located on another board.

## TODO next
Wow! Vast topic too...

- Docker Images ? For now, see [here](https://github.com/OlivierLD/raspberry-coffee/tree/master/docker).
  - Docker can run on a Raspberry Pi, this would simplify deployment and installation steps.
- Jupyter Notebooks to see how components work.  
  _Jupyter Notebooks_ were originally developed for Python, but they're now also available for pretty much any language that comes with a REPL (Read Execute Print Loop), like Java (9+), Scala, NodeJS, ...
  - [Spencer Park / IJava](https://github.com/SpencerPark/IJava)
  - [Almond, for Scala](https://almond.sh/)
  - [IJavaScript](https://github.com/n-riesco/ijavascript)
  - . . .
  
Some notebooks are available in the `NMEA-Parser` project. More to come.

---
Check out the number of lines of code, per categories
```
$ ./line.counter.sh
```

---
And more to come...

---

