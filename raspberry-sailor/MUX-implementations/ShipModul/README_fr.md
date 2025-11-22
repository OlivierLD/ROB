# Des notes à ne pas perdre...

MMSI La Rêveuse: `228210480`.

## Lecture d'un [ShipModul](http://www.shipmodul.com)

Le baud rate du ShipModul est `460800` ; c'est _**énorme**_, mais c'est vrai, et ça marche.

On dispose de plusieurs chaînes NMEA, qui nous donnent :
- Position
- Vitesse et route fond (SOG, COG)
- Température (de l'eau)
- Log (distance parcourue)
- Vitesse et route surface (BSP, HDG)
- Vitesse et direction du vent apparent (AWS, AWA)
- Assiette du bateau (tangage, gîte, état de la mer)
- AIS.

Analyse d'un log de 6 minutes 32.0 sec, réalisé au sec, à St Philibert :
```
Analyzing logged/shipmodul/2025-11-20_06-34-59_UTC_LOG.nmea.
Valid Strings:
VLW : 387 element(s) (Distance Traveled through Water)
RSA : 3,861 element(s) (un-managed)
VHW : 387 element(s) (Water speed and heading)
CJ, : 726 element(s) (un-managed)
XDR : 8,832 element(s) (Transducer Measurement)
RMC : 768 element(s) (Recommended Minimum Navigation Information, C)
HDG : 7,734 element(s) (Heading - Deviation & Variation)
VBW : 772 element(s) (un-managed)
MWV : 723 element(s) (Wind Speed and Angle)
MTW : 386 element(s) (Mean Temperature of Water)
ROT : 7,730 element(s) (un-managed)
MDN : 82 element(s) (un-managed)
VDM : 429 element(s) (AIS)
CJE : 727 element(s) (un-managed)

Valid Talker IDs:
II : 723 element(s)
VW : 774 element(s)
WI : 1,110 element(s)
PS : 82 element(s)
TI : 7,730 element(s)
AG : 3,861 element(s)
AI : 429 element(s)
GP : 768 element(s)
HC : 7,734 element(s)
PL : 1,453 element(s)
VD : 772 element(s)
YX : 8,108 element(s)
Started 20-Nov-2025 13:01:56 GMT, at N  47°35.66' / W 002°58.87' (IN87mo) (8CVVH2V9+PG)
Arrived 20-Nov-2025 13:08:28 GMT, at N  47°35.66' / W 002°58.87' (IN87mo) (8CVVH2V9+PG)
Used 768 record(s) out of 33,544. 
Total distance: 1.626 (1.623) nm, in 6 minute(s) 32.0 sec(s). Avg speed:14.935 kn
Max Speed (SOG): 0.090 kn
Min Speed (SOG): 0.000 kn
Top-Left    :N  47°35.66' / W 002°58.88' (IN87mo) (8CVVH2V9+QF) (47.594397 / -2.981277)
Top-Right   :N  47°35.66' / W 002°58.87' (IN87mo) (8CVVH2V9+QG) (47.594397 / -2.981163)
Bottom-Right:N  47°35.66' / W 002°58.87' (IN87mo) (8CVVH2V9+PG) (47.594322 / -2.981163)
Bottom-Left :N  47°35.66' / W 002°58.88' (IN87mo) (8CVVH2V9+PF) (47.594322 / -2.981277)
Min Lat (47°35.66'N) record idx (in logged/shipmodul/2025-11-20_06-34-59_UTC_LOG.nmea): 22732, at 20-Nov-2025 13:06:21 GMT
Max Lat (47°35.66'N) record idx (in logged/shipmodul/2025-11-20_06-34-59_UTC_LOG.nmea): 15324, at 20-Nov-2025 13:04:52 GMT
Min Lng (2°58.88'W) record idx (in logged/shipmodul/2025-11-20_06-34-59_UTC_LOG.nmea): 18636, at 20-Nov-2025 13:05:31 GMT
Max Lng (2°58.87'W) record idx (in logged/shipmodul/2025-11-20_06-34-59_UTC_LOG.nmea): 1336, at 20-Nov-2025 13:02:11 GMT

Max Calc Speed: 7.171 ms
Bottom-Left to top-right: 0.006 nm
Top-Left to bottom-right: 0.006 nm
Tooltip: idx 0, (rec #19)
Tooltip: idx 767, (rec #33530)

Bye!
Done
```

On dispose donc de toutes les données nécessaires pour élaborer les polaires du bateau, et faire du routage. Même en croisière, ça marche. L'objectif n'est pas forcément d'arriver le plus vite possible, on peut paramétrer le routage sur le mode "Je ne veux pas de vents de plus de 30 noeuds, et je ne veux pas être au près".

On va aussi pouvoir établir la courbe de déviation du bateau.

_Note_: On n'a ici ni `DBT`, ni `DPT` (Water Depth). Rien non plus sur `GGA`, `GSA`, `GSV` (Satellites GPS).

On remarque aussi qu'il existe plusieurs Talker IDs inhabituels, et des phrases NMEA non-standard.

---