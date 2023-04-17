# Misc links, raw info, bulk data

- WiFi setup on Raspberry Pi <http://emery.claude.free.fr/wifi-raspberry.html>
- [HotSpot setup](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md)
- <https://pandoc.org/MANUAL.html>

- find the name of a Serial Port: `raspberry-sailor/NMEA-multiplexer/find.port.sh`
- To manage NMEA log-files:
    - to analyze them: `raspberry-sailor/NMEA-multiplexer/log.analyzer.sh`
    - to merge them: `raspberry-sailor/NMEA-multiplexer/log.merge.sh`
    - to shrink them: `raspberry-sailor/NMEA-multiplexer/log.shrinker.sh`
    - to filter them: `raspberry-sailor/NMEA-multiplexer/log.filter.sh`
    - to transform them 
      - into CVS: `raspberry-sailor/NMEA-multiplexer/log.to.csv.sh` (like for a spreadsheet)
      - into GPX: `raspberry-sailor/NMEA-multiplexer/log.to.gpx.sh`
      - into KML: `raspberry-sailor/NMEA-multiplexer/log.to.kml.sh`
      - into JSON (for LeafLet): `raspberry-sailor/NMEA-multiplexer/log.to.json.sh`
      - into something else: `raspberry-sailor/NMEA-multiplexer/log.to.specific.sh` (as an example...)
      - for polars calculation:`raspberry-sailor/NMEA-multiplexer/log.to.polars.sh`

---
