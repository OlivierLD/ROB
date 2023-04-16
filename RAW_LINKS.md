# Misc links, raw info, bulk data

- WiFi setup on Raspberry Pi <http://emery.claude.free.fr/wifi-raspberry.html>
- [HotSpot setup](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md)
- <https://pandoc.org/MANUAL.html>

- find the name of a Serial Port: `raspberry-sailor/NMEA-multiplexer/find.port.sh`
- To manage NMEA log-file:
    - to analyze them: `raspberry-sailor/NMEA-multiplexer/log.analyzer.sh`
    - to merge them: `raspberry-sailor/NMEA-multiplexer/log.merge.sh`
    - to shrink them: `raspberry-sailor/NMEA-multiplexer/log.shrinker.sh`
    - to filter them: `raspberry-sailor/NMEA-multiplexer/log.filter.sh`
    - to transform them 
      - in CVS: `raspberry-sailor/NMEA-multiplexer/log.to.csv.sh` (like a spreadsheet)
      - in GPX: `raspberry-sailor/NMEA-multiplexer/log.to.gpx.sh`
      - in KML: `raspberry-sailor/NMEA-multiplexer/log.to.kml.sh`
      - in JSON (for LeafLet): `raspberry-sailor/NMEA-multiplexer/log.to.json.sh`
      - in something else: `raspberry-sailor/NMEA-multiplexer/log.to.specific.sh` (an example...)
      - for polars calculation:`raspberry-sailor/NMEA-multiplexer/log.to.polars.sh`

---
