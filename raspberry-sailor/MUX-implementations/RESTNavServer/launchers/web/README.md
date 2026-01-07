# Preamble
The files contained in this folder do not intend to be more than examples!
Again: _**examples**_. Bring them to prod at your own risks.
This is open source. You need to know what you're doing. You would not give a gun to an idiot, would you?

# Some features (WiP, the list  is long)
## Build and start the server
### To build the server  
> From the folder `ROB/raspberry-sailor/MUX-implementations/RESTNavServer`:
> ```
> $ ../../../gradlew shadowJar
> ```

### To start the server
There is a (big) script that will let you choose among many options.  
From the folder `ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers`:
```
$./demoLauncher.sh
```


## Weather Wizard
A Web Interface for some server side features... That one requires an Internet connection.  
From the script `demoLauncher.sh` (see above), choose option `1`, and then reach 
<http://localhost:9999/web/weather.wizard/ww.2.html> from your browser.
![Weather Wizard](../../docimg/ww-at-work.png)

## And more to come!
This doc is lagging behind... The script `demoLauncher.sh` provides a lot to choose from.

## TODO
- Web-Component console, with mirror/starwars effect for HeadsUp display, with at least (to begin with):
    - TWS, TWD
    - BSP (+ log), HDG
    - GPS Time (watch)
    - World Map
    - Analog AW

Done. See in the WebComponents folder. &#9989;

----

Expose the "Smooth-Move" (Also see previous `animate`) parameters at the component level for
- Analog Diplay
- Direction Display
- Compass Display
- Compass Rose
- Wind Angle Display

Add current and tws evolution in `webcomponents/console.html` (as in `web/console.html`).  
Upgrade the check boxes to rocking toggles. (&#9989;   Done in the Weather Wizard).  
Move to arrow functions where possible.

-------------------------