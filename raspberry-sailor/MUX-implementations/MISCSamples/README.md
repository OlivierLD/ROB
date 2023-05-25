# Miscellaneous Examples
Web Interfaces, REST services, Shell Scripts, etc.

## Leaflet Analysis
A Simple Web UI to visualize data logged when sailing, using [LeafLet.js](http://leafletjs.com/) and WebComponents.

Logged data (NMEA-0183) are transformed using `NMEAtoJSONPosPlus.java`, launched by the script 
`log.to.leaflet.sh`, with possible CLI parameters:
```
$ ./log.to.leaflet.sh --file-name:2010-07-10.tacking.back.in.nmea \
                      --archive-name:~/repos/ROB/raspberry-sailor/NMEA-multiplexer/sample-data/logged.data.zip \
                      --dev-curve:~/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv \
                      --polar-file:~/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff \
                      --current-buffer-length:600000  \
                      --output-file-name:~/repos/ROB/raspberry-sailor/MUX-implementations/MISCSamples/LeafLetAnalysis/2010-07-10.tacking.back.in.nmea.json \ 
                      --max-leeway:12 \
                      --aws-coeff:1.0
```
Explanation about the various parameters can be found in the code.

To define a track, LeafLet expects a JSON structure like
```javascript
let latlngs = [
          [45.51, -122.68],
          [37.77, -122.43],
          [34.04, -118.2]
];
```
or
```javascript
let latlngs = [
          { "lat": 45.51, "lng": -122.68 },
          { "lat": 37.77, "lng": -122.43 },
          { "lat": 34.04, "lng": -118.2 }
];
```

The cool thing is that you can add other members to the elements of those arrays.  
And this is what `NMEAtoJSONPosPlus.java` is doing.  
Look into `MISCSamples/LeafLetAnalysis/2010-07-10.tacking.back.in.nmea.json` for an example.

To see the Web UI, start a NodeJS server, using `npm start` (from the `MISCSamples` directory),
and reach <http://localhost:8080/LeafLetAnalysis/one.html> from a browser.

The UI should be clear enough for you to deal with it!

---

