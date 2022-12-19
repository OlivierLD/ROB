# ROB (Raspberry-Pi On Board)

Java and Python

This project came to light after [Raspberry Coffee](https://github.com/OlivierLD/raspberry-coffee).  
`Raspberry Coffee` is mainly written in Java (and other JVM languages) can communicate with sensors and actuators using [PI4J](https://pi4j.com/), itself relying on [WiringPi](http://wiringpi.com/).    

Many drivers for those boards (sensors and actuators) have been written in Java to enable communication with the code.  

And then, Wiring Pi became deprecated, and subsequently, PI4J V1 as well. Too bad.  

Other framework are available for this kind of operations, [PI4J V2](https://github.com/Pi4J/pi4j-v2/) was released, [diozero](https://github.com/mattjlewis/diozero) is also available...
But those guys have restriction on the JDK, they seem to need at least a JDK 11, not supported on small Raspberry Pis like the `Raspberry Pi Zero W`. Too bad, again.

So, there is something smart to come up with here 🤔.

_**Here is a thing**_: for all the sensors and actuators, we usually have some code, written by the board provider - usually in Python (which is why this code has been re-written in Java).  
So, an idea would be to keep this code as it is - it's working, and the borad provider will take care of updates, if needed - and find a way to establish a communication between this Python code and Java...



