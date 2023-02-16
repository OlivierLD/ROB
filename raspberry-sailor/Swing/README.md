# Swing Components
We will gather here several Swing Components and related utilities, to be run from a Desktop UI.

> _**Warning**_: These are demanding components. This might be a bit tough for small boards...

## Deviation Tool
Look at [this](./Deviation-Tool/README.md).  
```
$ ./process.log.sh
```

## WeatherWizard
Requires the Swing `ChartComponents` module.

#### Build it
```
$ cd <...>/Swing/WeatherWizard
$ ../../../gradlew shadowJar
```
#### Run it
```
$ ./run-ww.sh
```

### WeatherWizard user-exits
User-exits are graphical features implemented at will..., they're not strictly part of the product,
this is mostly experimental.  
They're used in the script `run-ww.sh`, modify it if needed.

> TODO: How to manage them from the Weather-Wizard

