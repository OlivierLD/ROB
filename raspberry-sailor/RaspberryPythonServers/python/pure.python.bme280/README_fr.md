# Un serveur BME280, tout en Python (uniquement)
(en [anglais](./README.md))  

Un barographe (comme [ceux-l&agrave;](https://www.naudet.com/barometre-enregistreur-c102x2375473)) est un instrument tr&egrave;s utile pour les pr&eacute;visions m&eacute;t&eacute;o.  
Mais &ccedil;a peut &ecirc;tre cher...  
On esp&egrave;re ici montrer comment en faire un, avec un Raspberry Pi Zero W, et un capteur bon march&eacute; comme le BME280.

---

Ce module est une impl&eacute;mentation d'un serveur en Python pour acc&eacute;der aux donn&eacute;es &eacute;mises par un BME280.

Ce qui fournit les fonctionnalit&eacute;s suivantes :
- Un serveur (en Python) qui 
  - g&egrave;re des requ&ecirc;tes REST et HTTP
  - lit reguli&egrave;rement le BME280
  - stocke jusqu'&agrave; une semaine de donn&eacute;es du BME280 (une donn&eacute;e tous les quarts d'heure)
- Une interface web, g&eacute;r&eacute;e par le serveur ci-dessus, pour afficher les donn&eacute;es d'une mani&egrave;re graphique et agr&eacute;able.

---

## En partant de z&eacute;ro
### Il vous faudra
- Un Raspberry Pi (le mod&egrave;le Zero W convient ici)
  - Si vous n'avez que le Raspberry Pi Zero, il faudra y ajouter un dongle WiFi.
- Une carte SD micro, pour le Raspberry Pi
- Une breadboard
- Un capteur BME280
  - Dispo sur des sites comme [celui-ci](https://www.aliexpress.com/p/tesla-landing/index.html?scenario=c_ppc_item_bridge&productId=4001027465901&_immersiveMode=true&withMainCard=true&src=google-language&aff_platform=true&isdl=y&src=google&albch=shopping&acnt=248-630-5778&isdl=y&slnk=&plac=&mtctp=&albbt=Google_7_shopping&aff_platform=google&aff_short_key=UneMJZVf&gclsrc=aw.ds&&albagn=888888&&ds_e_adid=&ds_e_matchtype=&ds_e_device=c&ds_e_network=x&ds_e_product_group_id=&ds_e_product_id=en4001027465901&ds_e_product_merchant_id=106630103&ds_e_product_country=ZZ&ds_e_product_language=en&ds_e_product_channel=online&ds_e_product_store_id=&ds_url_v=2&albcp=23109390367&albag=&isSmbAutoCall=false&needSmbHouyi=false&gad_source=1&gad_campaignid=23099403303&gbraid=0AAAAACWaBwdp0m6gB7HYpzmJF04LJFaR0&gclid=Cj0KCQiAhtvMBhDBARIsAL26pjH6AvrPvU8-iIFw3mKilyrSwii4EG_2QZ7wuuwDFxg9rmcNrmgYF8MaAvm_EALw_wcB), 
  [celui-ci](https://www.kubii.com/fr/modules-capteurs/4753-capteur-de-temperature-pression-et-humidite-bme280--3272496323773.html),
  [celui-ci](https://www.sparkfun.com/sparkfun-atmospheric-sensor-breakout-bme280-with-headers.html), 
  ou [celui-l&agrave;](https://www.adafruit.com/product/2652)... Les tarifs peuvent être disparates, n'h&eacute;sitez pas &agrave; chercher. 
- Des cables, jumpers
- Une alim pour le Raspberry Pi
- Un laptop (pour la configuration initiale)
- Un r&eacute;seau avec un acc&egrave;s Internet (celui que vous avez &agrave; la maison fait l'affaire)

> _**Attention**_: vous allez avoir &agrave; taper des commandes dans un terminal, comme indiqu&eacute; ci-dessous.  
> Il y en a qui sont un peu cryptiques, comme vous le verrez...  
> Notez bien que quand une ligne commence par `$`, c'est pour signifier que c'est une commande que _**vous**_ devez taper.  
> Ainsi, si vous voyez `$ mkdir BME280`, &ccedil;a veut dire que vous ne devez taper _que_ `mkdir BME280`.  
> Si une ligne ne commence _**pas**_ par `$`, &ccedil;a signifie qu'il s'agit du r&eacute;sultat de la commande que vous avez tap&eacute;e ; 
> et que vous devriez voir aussi (comme pour la commande `i2cdetect` que vous verrez ci-dessous, par exemple).

### Cr&eacute;ez une nouvelle carte SD
&Agrave; partir du laptop, utilisez le [Raspberry Pi Imager](https://www.raspberrypi.com/news/raspberry-pi-imager-imaging-utility/) pour configurer la carte SD destin&eacute;e au Raspberry Pi.  
N'oubliez pas
- D'activer SSH, I2C
- De d&eacute;finir les param&egrave;tres du r&eacute;seau, afin d'&ecirc;tre capable d'acc&eacute;der au Raspberry Pi avec SSH, lorsque la carte SD sera pr&ecirc;te.

### Cr&eacute;ez un r&eacute;pertoire sp&eacute;cifique sur le Raspberry Pi
Dans un terminal sur le laptop, en supposant que l'adresse IP du Raspberry Pi est `192.168.1.38` (&agrave; remplacer par la v&ocirc;tre)
```
$ ssh pi@192.168.1.38
```
Il va vous demander le mot de passe que **vous** avez cr&eacute;&eacute; en configurant la carte SD. 
Vous voil&agrave; connect&egrave; au Raspberry Pi.
```
$ mkdir BME280
```
### T&eacute;l&eacute;chargez les resources requises
Sur le laptop (ou sur le Raspberry Pi, en fait), ne clonez que la partie du repo qui nous int&eacute;resse :
```
$ mkdir BME280
$ cd BME280
$ git clone --depth 1 https://github.com/OlivierLD/ROB.git
$ cd ROB
$ git filter-branch --prune-empty --subdirectory-filter raspberry-sailor/RaspberryPythonServers/python/pure.python.bme280 HEAD
```
Le r&eacute;pertoire `ROB` doit maintenant contenir tout ce qu'il nous faut pour continuer.

Du laptop o&ugrave; le repo a &eacute;t&eacute; clon&eacute;, du r&eacute;pertoire `pure.python.bme280` (si vous avez clon&eacute; tout le repo) ou
du r&eacute;pertoire `ROB` (si vous avez ex&eacute;cut&eacute; la commande ci-dessus, pour ne cloner que ce qu'il faut):
```
$ scp -r . pi@192.168.1.38:~/BME280
```
### Cablage du BME280
En fonction du fournisseur de votre BME280, son aspect peut varier (Sparkfun ici, aussi disponible chez Adafruit, AliBaba, etc).
Mais le nom des contacts (`GND`, `3V3`, `SDA`, `SCL`) reste le m&ecirc;me.  
![Fritzing](doc/RPiZeroBME280_bb.png)

### V&eacute;rifiez l'adresse I2C
Assurez-vous d'avoir activ&eacute; l'interface I2C (avec `raspi-config`)  

Sur le Raspberry Pi:
```
$ sudo i2cdetect -y 1
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- -- 
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
70: -- -- -- -- -- -- 76 --                         
```
> Notez qu'ici l'adresse est `0x76`, comme sur un BME280 de chez AliBaba. Un capteur de chez Adafruit ou Sparkfun dirait `0x77`.

### &Agrave; faire la premi&egrave;re fois : installer les modules Python n&eacute;cessaires
Sur le Raspberry Pi :
```
$ sudo pip3 install adafruit-circuitpython-bme280
```
ou aussi
```
$ sudo pip3 install --upgrade --force-reinstall adafruit-circuitpython-bme280
```
En cas de probl&egrave;me "Externally Managed", see [here](https://www.makeuseof.com/fix-pip-error-externally-managed-environment-linux/)...

### D&eacute;marrer le serveur
```
$ python3 REST_and_WEB_BME280_server.py --machine-name:$(hostname -I | awk '{ print $1 }') --port:8080 --verbose:false [--address:0x76]
```
Ou si on veut qu'il tourne tout seul&nbsp;:
```
$ nohup python3 -u REST_and_WEB_BME280_server.py --machine-name:$(hostname -I | awk '{ print $1 }') --port:8080 --verbose:false [--address:0x76] > bmp.log 2>&1 &
```
Il y a aussi un param&egrave;tre `--store-restore`. Voir le code pour les d&eacute;tails, ainsi que l'op&eacute;ration REST `PUT /write`.
Ceci permet d'&eacute;crire dans des fichiers les donn&eacute;es contenues dans les maps &agrave; l'arr&ecirc;t du serveur, et/ou de les lire quand il d&eacute;marre. 

On peut aussi logger les donn&eacute;es dans une base sqlite.  
La base doit avoir &eacute;t&eacute; cr&eacute;&eacute;e avant de d&eacute;marrer le serveur :
```
$ sqlite3 weather.db < sql/create.db.sql
```
On utilise alors le param&egrave;tre `--log-db:true` au d&eacute;marrage du serveur. 
Ceci enregistrera 5 donn&eacute;es (pression atmosph&eacute;rique, humidit&eacute; relative, temp&eacute;rature de l'air, point de ros&eacute;e, humidit&eacute; absolue) toutes les 15 minutes.

### Et enfin
De n'importe o&ugrave; sur le m&ecirc;me r&eacute;seau, dans un navigateur, allez sur <http://192.168.1.38:8080/web/index.html>  
![WebUI](doc/web.ui.png)
M&ecirc;me avec un composant aussi petit que le Raspberry Pi Zero (W), on peut obtenir ce genre d'interface.  
Souvenez-vous que l'affichage d'une interface web est fait _sur et par le client_. Le Raspberry Pi se contente de fournir
au client les resources dont il a besoin pour cet affichage.

### Note
La page web utilise des requ&ecirc;tes REST pour obtenir les donn&eacute;es du serveur.    
On peut obtenir la liste des op&eacute;rations disponibles en tapant :
```
$ curl -X GET http://192.168.1.36:8080/bme280/oplist
```
On peut positionner la valeur de `verbose` &agrave; `true` ou `false` :
```
$ curl -X POST http://192.168.1.36:8080/bme280/verbose?value=false|true 
```

## Bonus
### Head-up display
Comme on le voit sur l'image ci-dessus, on a deux sliders en bas de la page.    
Ils sont l&agrave; pour Head-Up display (qu'on active en cliquant le bouton `Head Up` en haut a droite).  
Ainsi, on peut regarder l'&eacute;cran reflet&eacute; dans un pare-brise, par exemple.  

Pour avoir une id&eacute;e :

|              Ceci               |          Affiche cela          |
|:-------------------------------:|:------------------------------:|
| ![this](./doc/baro.head.up.png) | ![That](./doc/head.up.02.jpeg) |

### Le Raspberry Pi &eacute;met son propre r&eacute;seau
Imaginons que vous soyez en mer, loin de toute antenne 4G... Il n'y a pas d'Internet, _mais_ vous avez
la possibilit&eacute; de cr&eacute;er votre propre r&eacute;seau &agrave; partir du Raspberry Pi.  
Ainsi, tout p&eacute;rih&eacute;rique capable de rejoindre ce r&eacute;seau peut lire les pages web mises &agrave; disposition par le serveur
qui fonctionne sur le Raspberry Pi. &Ccedil;a peut &ecirc;tre un laptop, une tablette, un cell-phone. La seule
chose qui soit n&eacute;cessaire est un browser.

Pour que le Raspberry Pi &eacute;mette son propre reseau, on doit le configurer comme un HotSpot.    
Voir details [ici](https://github.com/OlivierLD/ROB/blob/master/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md)

Encore une fois, on peut avoir un r&eacute;seau, mais pas d'Internet. Il ne s’agit pas de Cloud Computing, mais plut&ocirc;t de Flake Computing (qu'on appelle aussi EZ - Entropy Zero)…

### Des bo&icirc;tes imprim&eacute;es en 3D pour prot&eacute;ger votre travail
- Voyez [ici](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/NavStations/README.md)
- Pour prot&egrave;ger le BME280 &agrave; l'ext&eacute;rieur :
  ![Wow](./doc/IMG_2409.JPG)  
  &agrave; retrouver sur <https://www.thingiverse.com/thing:1067700>

---