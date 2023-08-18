# Some use-cases

> _**Note**_: If you're not familiar enough with the required commands, do take a look at
> the script named `start.from.scratch.sh`, located at the [root of the repo](../../../start.from.scratch.sh).

First, make sure the soft can be built smoothly, from the machine where the git repo was cloned:
```
machine-a [NMEA-multiplexer] $ ../../gradlew shadowJar -x :astro-computer:AstroComputer:compileScala
```
# Content
- Deploy for prod, [Raspberry Pi and GPS](#use-case-1)
- [A Raspberry Pi A+, with a GPS, a BME280, and a 128x64 SSD1306 (using SPI)](#use-case-2)
- Deploy for prod, [Raspberry Pi, GPS, BME280, SSD1306, push buttons](#use-case-3)
- Basic Weather Station, deploy for prod, [Raspberry Pi, BMP180](#use-case-4)

## Use-case 1
In a separate document, [here](./use_cases/USE_CASES_1.md).

## Use-case 2
In a separate document, [here](./use_cases/USE_CASES_2.md).

## Use-case 3
In a separate document, [here](./use_cases/USE_CASES_3.md).

## Use-case 4
In a separate document, [here](./use_cases/USE_CASES_4.md).

---
