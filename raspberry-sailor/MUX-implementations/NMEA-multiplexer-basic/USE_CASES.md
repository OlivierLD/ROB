# Some use-cases

First, make sure you've built the soft:
```
$ ../../../gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```

### With a GPS, a BME280, and a 128x64 SSD1306 (using SPI)
First start the Python servers, for the BME280 and the SSD1306.  
Make sure you use the right ports.
```
$ ../../NMEA-multiplexer/scripts/start.BME280.TCP.server.sh


$ ../../NMEA-multiplexer/scripts/start.SSD1306.REST.server.sh 

```

```
$ ./mux.sh nmea.mux.kayak.ssd1306.yaml
```

#### Details
. . .

---
