# Option 3
### Déployer la configuration de l'[option 2](./USE_CASES_2_fr.md) sur une nouvelle image Raspberry Pi
Nous souhaitons déployer la configuration décrite [précédemment](./USE_CASES_2.md) sur une carte SD fraîchement préparée (l'outil [Raspberry Pi Imager](https://www.raspberrypi.com/software/) est parfait pour cela).
Cette configuration comprendra :
- Un Raspberry Pi A+, qui hébergera le multiplexeur NMEA.
- Un capteur BME280 et un écran OLED SSD1306 (128x64), assorti de deux boutons-poussoirs pour interagir avec le système.
- Un module GPS, connecté via un port USB.

Nous voulons enregistrer les données (provenant du GPS et du BME280) afin de pouvoir les rejouer ultérieurement, les visualiser sur Google Maps ou Leaflet, etc.

Nous souhaitons également que le Raspberry Pi génère son propre point d'accès Wi-Fi (hotspot), permettant à d'autres appareils de s'y connecter et d'accéder aux pages web du multiplexeur.
Enfin, le multiplexeur devra démarrer automatiquement lors de la mise sous tension du Raspberry Pi.

---
**_Pour être tout à fait clair_** :
- Nous compilerons le logiciel nécessaire à partir d'un clone du dépôt Git, sur une « Machine A ».
- Nous déploierons les éléments nécessaires sur une « Machine B » (le Raspberry Pi A+, qui assurera le fonctionnement du système).
- Effectuer les configurations requises sur cette machine.
- Et c'est parti pour l'aventure !
---

#### Machine A
Vous devez avoir cloné le dépôt et installé toutes les dépendances nécessaires à la compilation.
> Le script `start.from.scratch.sh` peut vous aider si besoin ; consultez-le pour plus de détails.

La « Machine A » peut être un ordinateur portable (sous Windows, Mac, Linux, etc.) ou même un autre Raspberry Pi, à condition qu'il soit suffisamment puissant pour effectuer la compilation. En supposant que vous puissiez utiliser un terminal avec `bash`, exécutez `to.prod.sh` pour générer l'archive à déployer (depuis `ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic`).
Veillez à répondre `y` lorsque le script affiche : `Do we package the Python part ? >`.
```
$ ./to.prod.sh
+----------------------------------------------------------------------------------------------------+
| P A C K ​​A G E   f o r   D I S T R I B U T I O N                           |
+----------------------------------------------------------------------------------------------------+
| Ceci est un EXEMPLE montrant comment générer une version « de production », sans avoir besoin de     |
| copier l'intégralité du dépôt GitHub sur la machine de destination. Nous ne déploierons que le      |
| nécessaire pour faire fonctionner le multiplexeur NMEA — éventuellement avec plusieurs               |
| configurations — ainsi que ses clients web. |
+----------------------------------------------------------------------------------------------------+
| Vous pouvez maintenant lancer une nouvelle construction (build)...                                 |
| Assurez-vous que la version de Java est compatible avec votre cible. Version actuelle :             |
+----------------------------------------------------------------------------------------------------+
| java version "11.0.8" 2020-07-14 LTS
| Java(TM) SE Runtime Environment 18.9 (build 11.0.8+10-LTS)
| Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.8+10-LTS, mixed mode)
+----------------------------------------------------------------------------------------------------+
| Vérifiez bien que la version actuelle de Java est compatible avec celle requise pour la cible !! |
+----------------------------------------------------------------------------------------------------+

Un fichier JAR existe déjà :
147025906 78488 -rw-r--r--  1 olivierlediouris  staff    38M 11 févr. 08:40 ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
Avec le MANIFEST suivant :
./build/libs/NMEA-multiplexer-basic-1.0-all.jar trouvé. On continue.
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/temp ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
-------- MANIFEST.MF --------
Manifest-Version: 1.0

-----------------------------
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
----------------------------
Faut-il reconstruire la partie Java ? > y
Reconstruction à partir des sources (sans Scala)...

> Configuration du projet :
>> Via la tâche compileJava (dans rob), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans rob), utilisation de la version Java 11

> Configuration du projet :astro-computer:AstroComputer
>> Via la tâche compileJava (dans AstroComputer), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans AstroComputer), utilisation de la version Java 11

> Configuration du projet :astro-computer:AstroUtilities
>> Via la tâche compileJava (dans AstroUtilities), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans AstroUtilities), utilisation de la version Java 11

Des fonctionnalités obsolètes de Gradle ont été utilisées lors de cette construction, ce qui la rend incompatible avec Gradle 8.0.

Vous pouvez utiliser l'option « --warning-mode all » pour afficher les avertissements individuels concernant ces fonctionnalités obsolètes et déterminer s'ils proviennent de vos propres scripts ou de plugins. Voir https://docs.gradle.org/7.6/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 9s
14 tâches traitables : 2 exécutées, 12 à jour
Dans quel dossier (inexistant) devons-nous créer la distribution ? > nmea-dist
Création du dossier nmea-dist
Copie des ressources
ajout : admin.html (compressé à 89 %)
ajout : basic.html (compressé à 59 %)
ajout : console.html (compressé# Cas d'utilisation 3
```

### Déployer la configuration de l'[option 2](./USE_CASES_2_fr.md) sur une nouvelle image Raspberry Pi
Nous souhaitons déployer la configuration décrite [précédemment](./USE_CASES_2.md) sur une carte SD fraîchement préparée (l'outil [Raspberry Pi Imager](https://www.raspberrypi.com/software/) est parfait pour cela).
Cette configuration comprendra :
- Un Raspberry Pi A+, qui hébergera le multiplexeur NMEA.
- Un capteur BME280 et un écran OLED SSD1306 (128x64), accompagnés de deux boutons-poussoirs pour interagir avec le système.
- Un module GPS, connecté via un port USB.

Nous voulons enregistrer les données (provenant du GPS et du BME280) afin de pouvoir les rejouer ultérieurement, les visualiser sur Google Maps ou Leaflet, etc.

Nous souhaitons également que le Raspberry Pi génère son propre point d'accès Wi-Fi (hotspot), permettant à d'autres appareils de s'y connecter et d'accéder aux pages web du multiplexeur.
Enfin, le multiplexeur devra démarrer automatiquement lors de la mise sous tension du Raspberry Pi.

---
**_Pour être tout à fait clair_** :
- Nous compilerons le logiciel nécessaire à partir d'un clone du dépôt Git, sur une « Machine A ».
- Nous déploierons les éléments nécessaires sur une « Machine B » (le Raspberry Pi A+, qui assurera le fonctionnement du système). 
- Effectuer les configurations requises sur cette machine.
- Et c'est parti pour l'aventure !

---

#### Machine A
Vous devez avoir cloné le dépôt et installé toutes les dépendances nécessaires à la compilation.
> Le script `start.from.scratch.sh` peut vous aider si besoin ; consultez-le pour plus de détails.

La « Machine A » peut être un ordinateur portable (sous Windows, Mac, Linux, etc.) ou même un autre Raspberry Pi, à condition qu'il soit suffisamment puissant pour effectuer la compilation. En supposant que vous puissiez utiliser un terminal avec `bash`, exécutez `to.prod.sh` pour générer l'archive à déployer (depuis `ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic`).
Veillez à répondre `y` lorsque le script affiche : `Do we package the Python part ? >`.
```
$ ./to.prod.sh
+----------------------------------------------------------------------------------------------------+
| P A C K ​​A G E   f o r   D I S T R I B U T I O N                           |
+----------------------------------------------------------------------------------------------------+
| Ceci est un EXEMPLE montrant comment générer une version « de production », sans avoir besoin de     |
| copier l'intégralité du dépôt GitHub sur la machine de destination. Nous ne déploierons que le      |
| nécessaire pour faire fonctionner le multiplexeur NMEA — éventuellement avec plusieurs               |
| configurations — ainsi que ses clients web. |
+----------------------------------------------------------------------------------------------------+
| Vous pouvez maintenant lancer une nouvelle construction (build)...                                 |
| Assurez-vous que la version de Java est compatible avec votre cible. Version actuelle :             |
+----------------------------------------------------------------------------------------------------+
| java version "11.0.8" 2020-07-14 LTS
| Java(TM) SE Runtime Environment 18.9 (build 11.0.8+10-LTS)
| Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.8+10-LTS, mixed mode)
+----------------------------------------------------------------------------------------------------+
| Vérifiez bien que la version actuelle de Java est compatible avec celle requise pour la cible !! |
+----------------------------------------------------------------------------------------------------+

Un fichier JAR existe déjà :
147025906 78488 -rw-r--r--  1 olivierlediouris  staff    38M 11 févr. 08:40 ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
Avec le MANIFEST suivant :
./build/libs/NMEA-multiplexer-basic-1.0-all.jar trouvé. On continue.
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/temp ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
-------- MANIFEST.MF --------
Manifest-Version: 1.0

-----------------------------
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
----------------------------
Faut-il reconstruire la partie Java ? > y
Reconstruction à partir des sources (sans Scala)...

> Configuration du projet :
>> Via la tâche compileJava (dans rob), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans rob), utilisation de la version Java 11

> Configuration du projet :astro-computer:AstroComputer
>> Via la tâche compileJava (dans AstroComputer), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans AstroComputer), utilisation de la version Java 11

> Configuration du projet :astro-computer:AstroUtilities
>> Via la tâche compileJava (dans AstroUtilities), utilisation de la version Java 11
>> Via la tâche compileTestJava (dans AstroUtilities), utilisation de la version Java 11

Des fonctionnalités obsolètes de Gradle ont été utilisées lors de cette construction, ce qui la rend incompatible avec Gradle 8.0.

Vous pouvez utiliser l'option « --warning-mode all » pour afficher les avertissements individuels concernant ces fonctionnalités obsolètes et déterminer s'ils proviennent de vos propres scripts ou de plugins. Voir https://docs.gradle.org/7.6/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 9s
14 tâches traitables : 2 exécutées, 12 à jour
Dans quel dossier (inexistant) devons-nous créer la distribution ? > nmea-dist
Création du dossier nmea-dist
Copie des ressources
ajout : admin.html (compressé à 89 %)
ajout : basic.html (compressé à 59 %)
ajout : console.html (compresséd/
nmea-dist/build/libs/
nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
nmea-dist/python/checksum.py
. . .
nmea-dist/python/scripts/test.sh
nmea-dist/python/scripts/start.BME280.REST.server.sh
pi $
```
Assurez-vous que `librxtx-java` est installé :
```
pi $ sudo apt-get install librxtx-java
```

Déplaçons-nous dans le répertoire nouvellement créé :
```
pi $ cd nmea-dist
```
Installons maintenant les modules Python requis :
```
pi $ ./python/scripts/install.all.sh
. . .
pi $
```
Ensuite, modifiez le fichier `/etc/rc.local` (assurez-vous de disposer des privilèges de superutilisateur) afin de lancer les composants nécessaires au démarrage du système. Ajoutez les lignes suivantes à la fin
du fichier, _avant_ l'instruction `exit` :
```
# Créer un lien vers le port série
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# Lancer le MUX et autres services au démarrage
#
# Lancer le serveur REST pour le BME280
# /home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:$(hostname -I) --port:9876 --verbose:false > /home/pi/bme280.python.log 2>&1
/home/pi/nmea-dist/python/scripts/start.BME280.REST.server.sh --interactive:false  --machine-name:localhost --port:9876 --verbose:false > /home/pi/bme280.python.log 2>&1
sleep 10
# Lancer le serveur REST pour le SSD1306 (v2)
# /home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:$(hostname -I) --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM > /home/pi/ssd1306.python.log 2>&1
/home/pi/nmea-dist/python/scripts/start.SSD1306.REST.server.v2.sh --interactive:false  --machine-name:localhost --port:8080 --verbose:false --height:64 --wiring:SPI --data:NAV,POS,SOG,COG,ATM > /home/pi/ssd1306.python.log 2>&1
sleep 10
# Lancer le MUX
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.sensor.nmea-fwd.yaml &
#
```
> Remarque : le mappage sur `/dev/ttyS80` est utilisé dans `nmea.mux.gps.sensor.nmea-fwd.yaml`. Assurez-vous qu'il correspond bien à votre configuration.


> Remarque : pour _arrêter_ tous les serveurs lancés précédemment, exécutez un script tel que :
> ```
> #!/bin/bash
> ps -ef | grep -Eiw 'java |python3 ' | grep -v grep | awk '{ print $2 }' > km
> NB_L=$(cat km | wc -l)
> if [[ ${NB_L} == 0 ]]; then
>   echo Aucun processus trouvé.
> fi
> for pid in $(cat km); do
>   echo -e "Arrêt du processus ${pid}"
>   sudo kill -15 ${pid}
> done
> rm km
> ```

Enfin, nous pouvons configurer le réseau point d'accès (hotspot) sur le Raspberry Pi.
Suivez les instructions disponibles [ici](../HOTSPOT.md).

Ensuite, vous pouvez éteindre la « Machine B » (le Raspberry Pi), brancher votre GPS, puis le démarrer.

Une fois le système redémarré, vous devriez voir — depuis la « Machine A », par exemple — un réseau nommé, comme indiqué plus haut, `NMEANetwork`.
Le mot de passe est `PassWord` (voir les [instructions](../HOTSPOT.md)).

Une fois connecté à ce nouveau réseau, depuis cette « autre » machine (un téléphone portable convient également ; il suffit de le connecter au réseau `NMEANetwork`), essayez d'accéder à l'adresse <http://192.168.50.10:9999/zip/index.html>,
`192.168.50.10` étant l'adresse de la machine (le Raspberry Pi) sur laquelle le multiplexeur est en cours d'exécution.

---