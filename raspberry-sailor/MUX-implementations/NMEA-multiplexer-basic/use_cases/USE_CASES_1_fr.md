# Option 1

> _**Remarque**_ : cette page est très détaillée. Si vous maîtrisez déjà Git, Gradle, Java, etc., certains passages pourront vous sembler redondants 😜.

### Un multiplexeur (et son interface web) sur un Raspberry Pi, avec un GPS
Il s'agit d'une configuration simple. Nous allons compiler le projet sur une machine où le dépôt Git a été cloné.
Cette machine sera appelée « Machine A ». Depuis la « Machine A », nous générerons une archive qui sera transférée sur le Raspberry Pi (appelé « Machine B »).
Sur le Raspberry Pi, nous décompresserons cette archive, effectuerons une configuration minimale, et le système sera prêt à être utilisé en conditions réelles.

Le Raspberry Pi pourra, si nécessaire, diffuser son propre réseau Wi-Fi (point d'accès/HotSpot).

---
**_Pour être tout à fait clair_** :
- Nous compilerons les logiciels nécessaires à partir d'un clone du dépôt Git, sur la « Machine A ».
- Concrètement : nous générerons une archive `.gz` contenant des archives Java (fichiers JAR), des pages web, des scripts, des fichiers de configuration — tout ce qui est nécessaire au moment de l'exécution.
- Nous déploierons les éléments générés (l'archive `.gz` créée précédemment) sur la « Machine B » (le Raspberry Pi, qui assurera le fonctionnement).
- Décompression de l'archive `.gz`.
- Configuration des paramètres nécessaires.
- Et c'est parti ! Prêt pour l'aventure !

> _**Remarque importante !!**_ : La partie Java est compilée sur la « Machine A » puis transférée vers la « Machine B ».
> Il est indispensable de s'assurer que la compilation sur la « Machine A » utilise la même version du JDK que celle présente sur la « Machine B » lors de l'exécution.
> Le choix se porte soit sur le JDK 11, soit sur le JDK 8.
> Consultez la section ci-dessous concernant l'utilisation de la commande `lscpu`, [ici](#java-version).

---
Dans les commandes ci-dessous, les invites de commande (prompts) du terminal seront préfixées par le nom de la machine (ou une indication équivalente). Une invite de commande telle que `machine-a $` indique que la commande doit être saisie depuis la « Machine A » (votre ordinateur portable).
Une invite de commande telle que `pi $` indique que la commande doit être saisie depuis la « Machine B » (le Raspberry Pi).

## Démarrer de zéro, rapidement.
Vous devrez cloner le dépôt Git (appelé « repo » dans la suite du document).
Si vous n'êtes pas familier avec cette technique ou cet outil, voici une méthode simple pour y parvenir en quelques clics :

Nous supposons que vous travaillez depuis un ordinateur portable ou de bureau, via un terminal en ligne de commande compatible `bash` (Linux, Mac, etc.).
Nous allons télécharger un script depuis le dépôt Git, le rendre exécutable, puis l'exécuter.
Le script créera un nouveau dossier et y clonera le dépôt. Il s'assurera ensuite que la procédure de compilation (« build ») peut se dérouler sans accroc.

Une fois cela fait, vous serez prêt à suivre les étapes décrites dans la suite du document.

C'est parti ! Depuis le terminal de votre ordinateur, exécutez la commande suivante pour télécharger le script nécessaire :
```
machine-a $ wget https://github.com/OlivierLD/ROB/raw/master/start.from.scratch.sh
```
Ensuite, rendez-le exécutable :
```
machine-a $ chmod +x ./start.from.scratch.sh
```
Enfin, lancez le script :
```
machine-a $ ./start.from.scratch.sh
```
Le script clone le dépôt dans un dossier nommé `repos/ROB`, compile `RESTNavServer` et exécute un script de démonstration.
> _**Remarque**_ : Le script clone le dépôt, ce qui signifie qu'il en copie le contenu sur la « Machine A ». Comme cela représente un volume important de code et de ressources, une confirmation vous sera demandée avant le début de l'opération.
> Une fois le processus terminé (clonage, compilation et génération de l'archive de production), vous pourrez supprimer le dépôt cloné si vous le souhaitez. Mais vous pouvez également le conserver pour des compilations ultérieures, au cas où des modifications seraient apportées au dépôt...

Si tout se passe bien, vous pouvez vous rendre dans le répertoire `repos/ROB/MUX-implementations/NMEA-multiplexer-basic` et suivre la suite des instructions ci-dessous.
```
machine-a $ cd ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
machine-a $
```

---
#### Sur la machine A
Vous devez avoir cloné le dépôt et installé toutes les dépendances nécessaires à la compilation.

La « machine A » peut être un ordinateur portable (sous Windows, Mac, Linux, etc.) ou un Raspberry Pi, à condition qu'il soit suffisamment puissant pour effectuer la compilation.
En supposant que vous puissiez utiliser un terminal `bash`, exécutez `to.prod.sh` pour générer l'archive à déployer (depuis le répertoire `ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic`).
Répondez `n` lorsque le script affiche : `Do we package the Python part ? >`.

```
machine-a $ ./to.prod.sh
+----------------------------------------------------------------------------------------------------+
| P A C K ​​A G E   f o r   D I S T R I B U T I O N                           |
+----------------------------------------------------------------------------------------------------+
| This is an EXAMPLE showing how to generate a 'production' version, without having the full github  |
| repo on the destination machine. We will deploy only what is needed to run the NMEA Multiplexer,   |
| possibly with several configurations - and its web clients. |
+----------------------------------------------------------------------------------------------------+
| Now you may start a fresh build...                                                                 |
| Make sure the javala version est compatible avec votre cible. Version actuelle :                        |
+----------------------------------------------------------------------------------------------------+
| java version "11.0.8" 2020-07-14 LTS
| Java(TM) SE Runtime Environment 18.9 (build 11.0.8+10-LTS)
| Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.8+10-LTS, mixed mode)
+----------------------------------------------------------------------------------------------------+
| Assurez-vous que la version Java actuelle est compatible avec la version cible !! |
+----------------------------------------------------------------------------------------------------+

Un fichier JAR existe déjà :
147025906 78488 -rw-r--r--  1 olivierlediouris  staff    38M Feb 11 08:40 ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
Avec le MANIFEST suivant :
./build/libs/NMEA-multiplexer-basic-1.0-all.jar trouvé. On continue.
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/temp ~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
-------- MANIFEST.MF --------
Manifest-Version: 1.0

-----------------------------
~/repos/ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic
----------------------------
Doit-on reconstruire la partie Java ? > y
Reconstruction à partir du code source (sans Scala)...

> Configuration du projet :
>> Via la tâche compileJava (dans rob), utilisation de la version 11 de Java
>> Via la tâche compileTestJava (dans rob), utilisation de la version 11 de Java

> Configuration du projet :astro-computer:AstroComputer
>> Via la tâche compileJava (dans AstroComputer), utilisation de la version 11 de Java
>> Via la tâche compileTestJava (dans AstroComputer), utilisation de la version 11 de Java

> Configuration du projet :astro-computer:AstroUtilities
>> Via la tâche compileJava (dans AstroUtilities), utilisation de la version 11 de Java
>> Via la tâche compileTestJava (dans AstroUtilities), utilisation de la version 11 de Java

Des fonctionnalités obsolètes de Gradle ont été utilisées lors de cette construction, ce qui la rend incompatible avec Gradle 8.0.

Vous pouvez utiliser l'option « --warning-mode all » pour afficher les avertissements d'obsolescence détaillés et déterminer s'ils proviennent de vos propres scripts ou de plugins.

Consultez https://docs.gradle.org/7.6/userguide/command_line_interface.html#sec:command_line_warnings

CONSTRUCTION RÉUSSIE en 9 s
14 tâches traitées : 2 exécutées, 12 déjà à jour
Dans quel dossier (inexistant) devons-nous créer la distribution ? > nmea-dist
Création du dossier nmea-dist
Copie des ressources
ajout : admin.html (compressé à 89 %)
ajout : basic.html (compressé à 59 %)
ajout : console.html (compressé à 79 %)
ajout : css/ (stocké à 0 %)
. . . 
ajout : widgets/TWSEvolution.js (compressé à 73 %)
ajout : wsconsole.html (compressé à 50 %)
Doit-on inclure la partie Python dans le paquet ? > n
a nmea-dist
a nmea-dist/nmea.mux.gps.tcp.yaml
a nmea-dist/nmea-to-text.properties
a nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
a nmea-dist/web.zip
a nmea-dist/nmea.mux.kayak.ssd1306.yaml
a nmea-dist/nmea.mux.replay.big.log.yaml
a nmea-dist/REST.ssd1306.dg.properties
a nmea-dist/nmea.mux.big.log.nmea-fwd.yaml
a nmea-dist/mux.sh
a nmea-dist/build
a nmea-dist/build/libs
a nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
+--------------------------------------------------------------------------------------------------+
>> Archive /. . ./ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/nmea-dist.tar.gz prête pour le déploiement.
+--------------------------------------------------------------------------------------------------+
| Transférez-la vers une autre machine et décompressez-la. |
| Utilisez la commande « tar -xzvf nmea-dist.tar.gz » pour la décompresser. |
| Des dépendances externes, telles que « librxtx-java », peuvent être nécessaires si vous comptez utiliser un port série ; |
| dans ce cas, il faudra peut-être exécuter « sudo apt-get install librxtx-java ». |
| Le script à lancer est « mux.sh ».                                                                |
| Il vous appartient d'utiliser le fichier de propriétés approprié, éventuellement modifié selon vos besoins. |
| Pour l'exécution/l'enregistrement (logger), utilisez « nmea.mux.gps.tcp.properties ».              |
| Exemple d'utilisation :                                                                          |
| $ cd nmea-dist
| $ nohup ./mux.sh nmea.mux.gps.tcp.yaml &                                                         |
| >> Les ressources web statiques sont accessibles via une adresse telle que http://<hôte>:<port>/zip/index.html |
+--------------------------------------------------------------------------------------------------+
machine-a $
```
L'archive a été générée.
Il faut maintenant configurer la « Machine B » et lui transmettre l'archive qui vient d'être créée.

#### Machine B
Préparez une nouvelle carte SD (voir [ici](https://www.raspberrypi.com/documentation/computers/getting-started.html) ; l'outil [Raspberry Pi Imager](https://www.raspberrypi.com/software/) est tout indiqué).
Veillez à activer l'interface `ssh` (depuis la ligne de commande)....utilisez `raspi-config` ;
depuis le bureau graphique, allez dans `Menu` > `Préférences` > `Configuration de Raspberry Pi`, puis sélectionnez `Interfaces`).
L'activation de ces interfaces peut également être effectuée via les paramètres (icône en forme de roue dentée) de Raspberry Pi Imager.

##### Java Version
Cette nouvelle image peut contenir un kit de développement Java (JDK). Vérifiez qu'il s'agit de la bonne version :
```
pi $ java -version
```
Si Java est absent (ou s'il ne s'agit pas de la bonne version), installez le JDK 11 :
```
pi $ sudo apt-get update
pi $ sudo apt-get install openjdk-11-jdk
```
> _**Attention**_ : selon l'architecture de votre Raspberry Pi, il peut être préférable d'utiliser le JDK 8.
> Le JDK 11 ne semble pas être pris en charge sur les anciennes versions du Raspberry Pi.
> Vérifiez votre architecture en saisissant la commande suivante (sur le Raspberry Pi) :
> ```
> pi $ lscpu
> Architecture:          armv6l
> Byte Order:            Little Endian
> CPU(s):                1
> On-line CPU(s) list:   0
> Thread(s) per core:    1
> Core(s) per socket:    1
> Socket(s):             1
> ```
> Dans cet exemple, comme il s'agit de `armv6l`, vous devrez utiliser le JDK 8 ; le JDK 11 ne fonctionnerait pas. > Il faudrait procéder comme suit :
> ```
> pi $ sudo apt-get update
> pi $ sudo apt-get install openjdk-8-jdk
> ```
> **_Vous devez également vous assurer que la même version est utilisée_**
> _**sur la « Machine A » pour générer les archives exécutables.**_
> Si le Raspberry Pi (« Machine B ») utilise un JDK 8, alors la compilation sur la « Machine A » _**DOIT**_ impérativement être effectuée avec un JDK 8.
> Pour basculer entre différentes versions, n'oubliez pas d'utiliser la commande `sudo update-alternatives --config java[c]`.


Déterminez l'adresse IP de la « Machine B ».

- Si un écran et un clavier sont connectés au Raspberry Pi, utilisez simplement
  `ifconfig` ou `hostname -I`.
- Si vous souhaitez (ou devez) vous connecter à distance (depuis la « Machine A » vers la « Machine B »), vous pouvez utiliser `fing`, disponible [ici](https://www.fing.com/products/development-toolkit).
  L'archive que vous téléchargerez contient plusieurs installateurs ; vous devrez exécuter celui qui correspond à votre architecture.
  Assurez-vous d'utiliser la version de [`Fing CLI`](https://www.fing.com/products/development-toolkit) adaptée à votre système ; les commandes `dpkg --print-architecture` ou `lscpu` vous permettront de déterminer laquelle choisir.
  Une fois le logiciel installé sur la « Machine A », exécutez :
```
machine-a $ sudo fing
Password:
15:38:32 > Discovery profile: Default discovery profile
15:38:32 > Discovery class:   data-link (data-link layer)
15:38:32 > Discovery on:      192.168.1.0/24

15:38:32 > Discovery round starting.
15:38:36 > Progression de la découverte : 25 %
15:38:41 > Progression de la découverte : 50 %
15:38:32 > Hôte actif : 192.168.1.100
Adresse matérielle (MAC) : 3C:22:FB:B7:8C:5C

15:38:32 > Hôte actif : 192.168.1.1
Adresse matérielle (MAC) : F0:0F:EC:D2:44:F5
Nom d'hôte : flybox.home

15:38:34 > Hôte actif : 192.168.1.101
Adresse matérielle (MAC) : DC:A6:32:20:8E:27

. . .

15:38:34 > Hôte actif : 192.168.1.116
Adresse matérielle (MAC) : 00:22:43:0A:E8:92 (AzureWave Technology)

-------------------------------------------------------------------------------
| État  | Hôte                              | Adresse MAC       | Dernière modification |
|-----------------------------------------------------------------------------|
| UP   | 192.168.1.1                       | F0:0F:EC:D2:44:F5 | |
| UP   | 192.168.1.100                     | 3C:22:FB:B7:8C:5C | |
| UP   | 192.168.1.101                     | DC:A6:32:20:8E:27 | |
. . .
| UP   | 192.168.1.116                     | 00:22:43:0A:E8:92 | | -------------------------------------------------------------------------------

15:38:55 > Cycle de découverte terminé en 23,301 secondes.
15:38:55 > Réseau 192.168.1.0/24 : 7/7 hôtes sont actifs.

15:38:55 > Prochain cycle prévu à 15:39:32. Appuyez sur Ctrl+C pour quitter.
```


Nous supposons que l'adresse de la « Machine B » est `192.168.1.101`.

Vérifiez le nom d'utilisateur utilisé sur le Raspberry Pi à l'aide de la commande `whoami` :
```
pi $ whoami
pi
```
Dans ce cas, le nom d'utilisateur est `pi`, comme indiqué sur la deuxième ligne ci-dessus.

#### Sur la « Machine A »

De retour sur la « Machine A », envoyez l'archive vers la « Machine B » en utilisant son adresse IP :
```
machine-a $ scp nmea-dist.tar.gz pi@192.168.1.101:~
pi@192.168.1.101's password:
nmea-dist.tar.gz                                                                        100%   37MB 372.6KB/s   01:42
machine-a $
```
> _**Remarque**_ : dans la commande `scp` ci-dessus, au sein de `pi@192.168.1.101`, `pi` correspond au nom d'utilisateur identifié précédemment. Assurez-vous qu'il est bien adapté à votre situation.

Nous en avons terminé avec la « Machine A ».

#### Sur la machine B

Connectez-vous à la « machine B » (via `ssh` depuis la « machine A », si vous le souhaitez, ou directement sur la « machine B » si un écran et un clavier y sont branchés) :
```
machine-a $ ssh pi@192.168.1.101
```
puis décompressez — à l'aide de `tar` (Tape ARchiver) — le fichier reçu précédemment (nommé `nmea-dist.tar.gz` dans notre cas) :
```
pi $ tar -xzvf nmea-dist.tar.gz
nmea-dist/
nmea-dist/nmea.mux.gps.tcp.yaml
nmea-dist/nmea-to-text.properties
nmea-dist/nmea.mux.gps.sensor.nmea-fwd.yaml
nmea-dist/web.zip
nmea-dist/nmea.mux.kayak.ssd1306.yaml
nmea-dist/nmea.mux.replay.big.log.yaml
nmea-dist/REST.ssd1306.dg.properties
nmea
```-dist/nmea.mux.big.log.nmea-fwd.yaml
nmea-dist/mux.sh
nmea-dist/build/
nmea-dist/build/libs/
nmea-dist/build/libs/NMEA-multiplexer-basic-1.0-all.jar
pi $
```
Assurez-vous que `librxtx-java` est installé sur le Raspberry Pi :
```
pi $ sudo apt-get install librxtx-java
```

Rendez-vous dans le répertoire qui vient d'être créé (vous avez choisi ce nom lors de l'exécution du script `to.prod.sh`) :
```
pi $ cd nmea-dist
```

> _**Remarque**_ : En raison d'un bug (ou quelque chose de similaire) dans `librxtx-java`, le port série sur lequel le GPS est branché est identifié comme `/dev/ttyACM0`, mais il doit être lié à `/dev/ttyS80` — le port qui sera effectivement utilisé, comme indiqué dans le fichier de configuration `nmea.mux.gps.yaml` :
> ```yaml
> channels:
> - type: serial
>   port: /dev/ttyS80
>   baudrate: 4800
> ```
> Pour créer ce lien, utilisez la commande `sudo ln -s /dev/ttyACM0 /dev/ttyS80` avant toute tentative de lecture du port.
> Cette opération doit être répétée à chaque démarrage du Raspberry Pi. Il est également possible de l'automatiser, comme expliqué ci-dessous.


Ensuite, si vous souhaitez que le multiplexeur se lance automatiquement au démarrage du Raspberry Pi, vous pouvez modifier le fichier `/etc/rc.local` (en vous assurant de disposer des privilèges super-utilisateur) afin d'exécuter les composants nécessaires au démarrage. Ajoutez les lignes suivantes à la fin du fichier, _avant_ l'instruction `exit` :
```
# Création du lien vers le port série
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
# Lancement du multiplexeur (MUX)
cd /home/pi/nmea-dist
nohup ./mux.sh nmea.mux.gps.yaml &
#
```
> Remarque : le mappage sur `/dev/ttyS80` est utilisé dans `nmea.mux.gps.yaml`. Assurez-vous qu'il correspond bien à votre configuration.

> Remarque : pour _arrêter_ tous les serveurs lancés précédemment, exécutez un script tel que celui-ci :
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

##### Prêt !
Le multiplexeur est désormais opérationnel. Vous pouvez le lancer et accéder aux données qu'il génère depuis n'importe quel autre appareil connecté au même réseau
(y compris depuis le Raspberry Pi lui-même, bien entendu).
Pour démarrer le multiplexeur, depuis le répertoire `nmea-dist`, dans un terminal :
```
pi $ ./mux.sh nmea.mux.gps.yaml
```
S'il démarre comme prévu (vous verrez alors les informations s'afficher dans la console), essayez d'accéder à l'adresse
`http://192.168.1.101:9999/zip/index.html` via un navigateur sur n'importe quel appareil connecté au même réseau.
La combinaison de touches `[Ctrl + C]` dans la console permet d'arrêter le programme.

#### Pour finir
Enfin, nous pouvons configurer un point d'accès (hotspot) sur le Raspberry Pi.

En mer, vous ne disposerez peut-être pas de connexion Internet. Cependant, le Raspberry Pi peut diffuser son propre réseau,
permettant ainsi à d'autres appareils de s'y connecter, exactement comme vous le feriez avec votre réseau domestique...

Suivez les instructions [ici](../HOTSPOT.md).

Ensuite, vous pouvez éteindre la « Machine B » (le Raspberry Pi), brancher votre GPS, puis le démarrer.

Une fois le système redémarré, vous devriez voir apparaître — depuis la « Machine A », par exemple — un réseau nommé, comme indiqué précédemment, `NMEANetwork`.
Le mot de passe est `PassWord` (voir les [instructions](../HOTSPOT.md)).

Une fois connecté à ce nouveau réseau, depuis cette « autre » machine (un téléphone portable convient également ; il suffit de s'y connecter au réseau `NMEANetwork`), essayez d'accéder à l'adresse <http://192.168.50.10:9999/zip/index.html>,
`192.168.50.10` correspondant à l'adresse de la machine (le Raspberry Pi) sur laquelle le multiplexeur est exécuté.

### Résumé
Résumé graphique disponible [ici](https://htmlpreview.github.io/?https://github.com/OlivierLD/ROB/blob/master/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/use_cases/summary.html).

---