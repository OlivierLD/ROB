_**Note**_: the links below are just examples, provided to have an idea of the cost of the different components. Components are available from several providers, and prices may vary.

---

# Full Stack

- [Raspberry Pi 4B, 4Go](https://www.kubii.com/fr/cartes-nano-ordinateurs/2772-raspberry-pi-4-modele-b-4gb-5056561800349.html)
- [In a box](https://www.kubii.com/fr/boitiers-fixations/2681-boitier-officiel-pour-raspberry-pi-4-3272496298583.html)
- A Screen (power supply 12v, or USB), with an HDMI cord. Like [this](https://www.amazon.fr/Yodoit-Portable-Moniteur-B%C3%A9quille-Haut-parleurs/dp/B0F82M3GHX/ref=asc_df_B0F82M3GHX?mcid=b898da6d55c23e2897ce0722a272fa60&tag=googshopfr-21&linkCode=df0&hvadid=701511851432&hvpos=&hvnetw=g&hvrand=13635281505204187963&hvpone=&hvptwo=&hvqmt=&hvdev=c&hvdvcmdl=&hvlocint=&hvlocphy=9109278&hvtargid=pla-2451665989584&hvocijid=13635281505204187963-B0F82M3GHX-&hvexpln=0&th=1).
- [Keyboard](https://www.pearl.fr/article/TG1541/clavier-usb-lumineux-azerty-avec-pave-numerique) (QWERTY or AZERTY)
- Mouse, Trackball, or track pad (many options to choose from!)

## AIS Receiver Option
- [Wegmatt](https://wegmatt.com/).
- [Get started](https://wegmatt.com/files/dAISy%20HAT%20AIS%20Receiver%20Quickstart.pdf)
- [Raspberry Pi AIS-HAT, WegMatt](https://shop.wegmatt.com/products/daisy-hat-ais-receiver)
- [Raspberry Pi AIS-HAT, OpenMarine](https://shop.openmarine.net/home/14-daisy-hat-ais-receiver.html)

## Extra
- A [BME280](https://www.adafruit.com/product/2652) or [BMP180](https://www.alibaba.com/pla/BMP180-GY-68-33V-5V-BMP-180-Temperature_1601258226008.html?mark=google_shopping&biz=pla&searchText=electronic+components&product_id=1601258226008&pcy=fr_fr&src=sem_ggl&field=UG&from=sem_ggl&cmpgn=22889997309&adgrp=184184039735&fditm=&tgt=pla-2426194943436&locintrst=&locphyscl=9109278&mtchtyp=&ntwrk=g&device=c&dvcmdl=&creative=769301584071&plcmnt=&plcmntcat=&aceid=&position=&gad_source=1&gad_campaignid=22889997309&gbraid=0AAAAAD8m77prknzKuHOPnobJhLRJhaNn3&gclid=CjwKCAjwoaLWBhAWEiwAnyituwUAFi1j_D2e30QtmCTB03nXi-rumaNeVdon_UWhQrmVl2fDIs2BDRoC9ygQAvD_BwE) can be useful to get the atmospheric pressure and air temperature.   
  _This one needs not to be too close to the CPU of the Raspberry Pi, as the temperature of the CPU
  might impact your measures._

---

### Context
The Raspberry Pi emits its own network (yes, you can have a network, and no Internet...), and is connected to the boat's sensors (ShipModul or so).  
Data can be visualized on the screen connected to the Raspberry Pi, or from any network-aware device, with a (recent, or at least not too old) browser.  
The Raspberry Pi is strong enough to host programs like [OpenCPN](https://opencpn.org/).  
The Multiplexer (options to be defined, technical doc [here](https://github.com/OlivierLD/ROB/blob/master/raspberry-sailor/NMEA-multiplexer/manual.md) - don't be scared, we'll help you) is also running on the Raspberry Pi.  
An OS like [Twister OS](https://twisteros.com/) is a very good option.

Adapters (power supply, HDMI sockets, serial/USB ports) and SD Card have to be provided as well.

---