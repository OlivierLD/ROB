# Almanac Related Tools
## Mostly almanacs and tables publishing.

Uses Java to generate `XML` data files, and then `xsl-fo` to create `pdf` files from the `XML` data.  
The Java code can be (and is) reused from REST interfaces.

Look into the `pub` folder, the scripts will prompt you if needed.  
Try:

### For the T900 (Dieumegard & Bataille):
```
$ ./pub/publish.tables.sh
```
### For the altitude correction tables:
```
$ ./pub/corrections.sh
```
### For a "perpetual" (aka long-term) almanac:
```
$ ./pub/perpetual.sh
```
The main interest of the perpetual almanac is its concision.  
It gives ephemeris data for the Sun and Aries, this requires more interpolations
than the "regular" Nautical Almanac. The idea is to put one copy in the bag
you grab when something goes _very_ wrong. This is more for emergency navigation.

### For Nautical Almanacs or Lunar Distances:
```
$ ./pub/almanac.sh
```

### Mercator Canvases, Plotting Sheets, etc.
For now, look into the `ChartComponents` module (under `Swing`),
[here](../Swing/ChartComponents).  
From the module's folder, do a 
```
$ ../../../gradlew shadowJar
$ ./run.samples.sh 
+--------------------------------------------------------------------------------+
|                            S W I N G   S A M P L E S                           |
+---------------------------+-------------------------+--------------------------+
|  1 - Satellites           |  2 - Atlantic           |  3 - Big Width           |
|  4 - Case Study           |  5 - Conic Equidistant  |  6 - Globe               |
|  7 - Increasing Latitude  |  8 - Lambert            |  9 - Mercator Canvas     |
| 10 - Mercator Scale       | 11 - Mid Atlantic       | 12 - Viking Sun Compass  |
| 13 - Night and Day        | 14 - North America      | 15 - Pacific             |
| 16 - Plotting Sheet (1)   | 17 - Plotting Sheet (2) | 18 - Polar Stereo        |
| 19 - Polar Stereo (south) | 20 - SF Bay             | 21 - Shore Detection     |
| 22 - Stereographic        | 23 - The trip           | 24 - Two Globes          |
| 25 - World                |                         |                          |
+---------------------------+-------------------------+--------------------------+
| Q - Quit                                                                       |
+--------------------------------------------------------------------------------+
You choose > 
```

### Misc links and stuff...
- <https://www.crawfordnautical.com/2018/09/16/chart-projections/>

### More tables
- Also look into <https://olivierld.github.io/web.stuff/nse/index.html>

---