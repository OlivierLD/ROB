# ROB (Raspberry-Pi On Board)

Java and Python.

| _**100% West Made. Fait 100% à l'ouest.**_ |
|:------------------------------------------:|
| ![A l'ouest](./a.l.ouest.jpeg) |


This project came to light after [Raspberry Coffee](https://github.com/OlivierLD/raspberry-coffee).  
`Raspberry Coffee` is mainly written in Java (and other JVM languages), and can communicate with sensors and actuators using [PI4J](https://pi4j.com/), itself relying on [WiringPi](http://wiringpi.com/).    

Many drivers for those boards (sensors and actuators) have been written in Java to enable native communication with the code.  

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

---
More to come...

