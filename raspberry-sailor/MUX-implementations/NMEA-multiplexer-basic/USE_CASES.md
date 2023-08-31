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
- Deploy for prod, [Raspberry Pi and GPS](#use-case-1)
- [A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306 (using SPI), push buttons](#use-case-2)
- Deploy for prod, [Raspberry Pi, GPS, BME280, SSD1306, push buttons](#use-case-3)
- Basic (and functional) Navigation Station, deploy for prod, [Raspberry Pi, BMP180](#use-case-4)

## Use-case 1
Generic example, showing how to deploy _your_ configuration to prod.  
In a separate document, [here](./use_cases/USE_CASES_1.md).

## Use-case 2
How to setup a A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306. Two push-buttons.  
In a separate document, [here](./use_cases/USE_CASES_2.md).

## Use-case 3
Deploy the config above fro prod.  
In a separate document, [here](./use_cases/USE_CASES_3.md).

## Use-case 4
A Barograph (Atmospheric Pressure recorder).    
In a separate document, [here](./use_cases/USE_CASES_4.md).


## Use-case 5
The smallest (and cheapest) Navigation Station. Raspberry Pi Zero W, GPS, optional e-ink bonnet.  
In a separate document, [here](./HOWTO.md).

---
