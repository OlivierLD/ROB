# TODOs, ideas, and so.

### Sun Path Web Component
- Extended to _**any**_ celestial body, any hemisphere, tropical zone(s).
- Constellations &#9989;

### Rhumblines and Great Circles on the WorldMap
- Great Circle (with `doAfter` callback on the WorldMap WebComponent) &#9989;

### A logger like the runner - for Kayaking.
- Transparent top
- External GPS
- Larger screen (like Nokia)
- eInk and Papirus
- 2/4-button interface: Back, Yes, Select, No.

### Lightweight TCP client
- Like a _really_ smart watch, no Bluetooth required, no phone required.
- Possibly ESP8266 based, with an OLED screen and a LiPo battery.
- WIP, more to come.

### Elaborate a deviation curve
- From a Web/JavaScript UI, maybe? DONE. &#9989;
  - See `Project.Trunk/Deviation.Tool`

### Elaborate polars
- See module `PolarSmoother`, Swing UI, and doc. &#9989;

### Routing
- In progress. Best route OK
  - _TODO_: GRIB version 2.
- Get all isochronals, _TODO_

### Document the way to use `screen`
See <https://linuxize.com/post/how-to-use-linux-screen/>
```
$ sudo apt update
$ sudo apt install screen
```

```
$ screen -S sleepy -dm sleep 60
$ screen -S sleepy -X quit
$ screen -XS <id> quit
$ screen -S navserver -dm bash -c 'sleep 5; exec sh'
```
