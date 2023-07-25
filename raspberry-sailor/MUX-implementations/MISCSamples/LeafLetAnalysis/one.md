# How to
Reads and streams a json file, containing data processed by `log.to.leaflet.sh`, like in
```
 ./log.to.leaflet.sh --file-name:2010-07-10.tacking.back.in.nmea \
                     --archive-name:./sample-data/logged.data.zip \
                     --polar-file:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff \
                     --dev-curve:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv \
                     --max-leeway:10 \
                     --current-buffer-length:600000 \
                     --default-decl:14 \
                     --awa-offset:0 \
                     --hdg-offset:0 \
                     --aws-coeff:1 \
                     --bsp-coeff:1 \
                     --calc-tw-with-gps:true \
                     --output-file-name:tbi.max-leeway-10.json \
                     --awa-offset:-10
```
Result can be seen with `jq`:
```
cat ../data/tbi.max-leeway-10.json  | jq '.[294]'
{
  "lat": 37.66415,
  "lng": -122.35361666666667,
  "gridSquare": "CM87tp",
  "sog": 4.1,
  "cog": 286,
  "rmcDate": "2010-07-09 17:24:59 UTC",
  "hdt": 300.8515783113226,
  "hdc": 295,
  "cmg": 309.9870328877486,
  "leeway": 9.13545457642601,
  "decl": 15,
  "dev": -9.148421688677415,
  "bsp": 5.3,
  "mwt": 17,
  "awa": 326,
  "aws": 11.4,
  "twa": -11.559238419432859,
  "tws": 7.304335025825785,
  "twd": 289.29233989188975,
  "csp": 4.197722190939529,
  "cdr": 121.35583795195471,
  "perf": 0
}
```

Useful to elaborate several coefficients above (offsets & Co).

It can be used to see the calculated current, based - among others - on the `max-leeway`,
used to calculate the Course Made Good (CMG).

Same for all calculated data, like True Wind - Speed and Direction.


## TODO
- Draw the Boat on the Track
- Plot True Wind on the Track
- Plot Current on the Track

