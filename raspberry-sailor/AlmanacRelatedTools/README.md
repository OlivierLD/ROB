# Almanac Related Tools
## Mostly almanacs and tables publishing.

Uses Java to generate `XML` data files, and then `xsl-fo` to create `pdf` files from the `XML` data.  
The Java code can be (and is) reused from REST interfaces.

Look into the `pub` folder, the scripts will prompt you if needed.  
Try:

For the T900:
```
$ ./pub/publish.tables.sh
```
For the altitude correction table:
```
$ ./pub/corrections.sh
```
For a "perpetual" (aka long-term) almanac:
```
$ ./pub/perpetual.sh
```
For Nautical Amanac or Lunar Distances:
```
$ ./pub/almanac.sh
```
### Misc links and stuff...
- <https://www.crawfordnautical.com/2018/09/16/chart-projections/>

---