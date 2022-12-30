# Nautical Almanac primitives 
### also provides REST resources implementation

--- 

For the value of Delta T, see:
- http://maia.usno.navy.mil/
- http://maia.usno.navy.mil/ser7/deltat.data

> _Note_: the above stopped working, before 2020... We now have a way to _calculate_ `DeltaT`.

---

Designed to be part of other projects, see `RESTNavServer`, `RESTTideEngine`, ...

It also provides a way to generate the French `Tables 900` (Dieumegard & Bataille, see [here](http://navastro.free.fr/dieumegard1.htm)). Look into the `RESTNavServer` module,
in the `launchers/pub` folder, there is a `publish.tables.sh` script.

---

## Build it
To run it in standalone (which is by far not the only way), you need to build it first:
```
 $ ../../gradlew clean shadowJar
```
Then, you can give it a try:
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputer -help
```
or something like
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputer -type continuous -year 2019 -month 10 -day 28
```

---
