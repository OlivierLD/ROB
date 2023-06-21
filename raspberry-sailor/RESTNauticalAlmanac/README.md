# Nautical Almanac primitives usage 
### Also provides REST resources implementation, for Almanac and misc tables (T900 , Dieumegard & Bataille, etc) publishing.
Allows an HTTP Client to consume (some) features of the `AstroComputer` and `AlmanacRelatedTools` modules.  
See how it can be put to work in `RESTNavServer`.

--- 

For the value of Delta T, see:
- http://maia.usno.navy.mil/
- http://maia.usno.navy.mil/ser7/deltat.data

> _Note_: the above stopped working, before 2020... We now have a way to _calculate_ `DeltaT`.

---

Designed to be part of other projects, see `RESTNavServer`, `RESTTideEngine`, ...

It also provides a way to generate the French `Tables 900` (Dieumegard & Bataille, see [here](http://navastro.free.fr/dieumegard1.htm)). Look into the `RESTNavServer` module,
in the `launchers/pub` folder, there is a `publish.tables.sh` script.  
And for the fun, we also have a couple of spreadsheets, see [here](spreadsheets/README.md).

---

## Build it
To run it in standalone (which is by far not the only way), you need to build it first:
```
 $ ../../gradlew clean shadowJar
```
Then, you can give it a try:
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputerImpl -help
```
or something like
```
 $ java -cp ./build/libs/RESTNauticalAlmanac-1.0-all.jar implementation.almanac.AlmanacComputerImpl -type continuous -year 2019 -month 10 -day 28
```

---
