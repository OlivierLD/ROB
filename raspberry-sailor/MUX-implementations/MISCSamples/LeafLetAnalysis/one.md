# How to
Reads and streams a json file, containing data processed by `log.to.leaflet.sh`, like in
```
 ./log.to.leaflet.sh --file-name:2010-07-10.tacking.back.in.nmea \
                     --archive-name:./sample-data/logged.data.zip \
                     --polar-file:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff \
                     --dev-curve:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv \
                     --max-leeway:15 \
                     --current-buffer-length:600000 \
                     --default-decl:14 \
                     --awa-offset:0 \
                     --hdg-offset:0 \
                     --aws-coeff:1 \
                     --bsp-coeff:1 \
                     --calc-tw-with-gps:true \
                     --output-file-name:tbi.max-leeway-15.json
```

It can be used to see the calculated current, based - among others - on the `max-leeway`,
used to calculate the Course Made Good (CMG).

Same for all calculated data, like True Wind - Speed and Direction.


## TODO
- Draw the Boat on the Track
- Plot True Wind on the Track
- Plot Current on the Track

