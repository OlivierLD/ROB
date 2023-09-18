# Some use-cases

> _**Note**_: If you're not familiar enough with the required commands, do take a look at
> the script named `start.from.scratch.sh`, located at the [root of the repo](../../../start.from.scratch.sh).

First, make sure the soft can be built smoothly, from the machine where the git repo was cloned:
```
machine-a [NMEA-multiplexer] $ ../../gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```

> _**Warning**_: This page summarizes a list of _**examples**_. The deployment processes go in two steps:
> - You choose your config (Raspberry Pi model, sensors, actuators, etc).
>   - This config may involve Web pages, customizable at will. Some are provided here, you're obviously free to develop your own.
>   - This will be summarized by a `yaml` file, used to drive the multiplexer at runtime. 
> - You deploy it for prod.

# Content
- Deploy for prod, [Raspberry Pi and GPS]  
  Generic example, showing how to deploy _your_ configuration to prod.  
  In a separate document, [here](./use_cases/USE_CASES_1.md).
- [A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306 (using SPI), push buttons]  
  How to setup a A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306. Two push-buttons.  
  In a separate document, [here](./use_cases/USE_CASES_2.md).
- Deploy for prod, [Raspberry Pi, GPS, BME280, SSD1306, push buttons]  
  Deploy the config above fro prod.  
  In a separate document, [here](./use_cases/USE_CASES_3.md).
- Basic (and functional) data recorder, deploy for prod, [Raspberry Pi, BMP180]  
  A Barograph (Atmospheric Pressure recorder).    
  In a separate document, [here](./use_cases/USE_CASES_4.md).
- The smallest (and cheapest) Navigation Station. Raspberry Pi Zero W, GPS, optional e-ink bonnet.  
  In a separate document, [here](./HOWTO.md).

---
