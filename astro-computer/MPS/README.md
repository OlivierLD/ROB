# M&eacute;thode du Plan des Sommets (MPS)
M&eacute;thode &eacute;labor&eacute;e par Yves Robin-Jouan, en 1995-96.

Ceci est un chantier... On veut voir si cette m&eacute;thode est applicable manuellememt - sans recours &agrave; l'informatique.  
&Agrave; la diff&eacute;rence de la methode des droites de hauteur (Marcq Saint-Hilaire, 1875), cette m&eacute;thode pr&eacute;sente l'avantage de ne pas avoir &agrave; recourir &agrave; une position estim&eacute;e. L'inconv&eacute;nient potentiel pourrait &ecirc;tre la quantit&eacute; de calculs &agrave; mettre en &oelig;uvre...  
C'est ce qu'on se propose de voir ici.

## Rappels

$$
He = \arcsin \left( (\sin(L).\sin(D)) + (\cos(L).\cos(D).\cos(AHL)) \right)
$$

$$
Z = \arctan \left( \dfrac{\sin(AHL)}{(\cos(L).\tan(D)) - (\sin(L).\cos(AHL))}\right)
$$

---

- Voir [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)
- Voir [Formule de Haversine](https://fr.wikipedia.org/wiki/Formule_de_haversine)
- Voir [ce document](https://www.aftopo.org/download.php?type=pdf&matricule=aHR0cHM6Ly93d3cuYWZ0b3BvLm9yZy93cC1jb250ZW50L3VwbG9hZHMvYXJ0aWNsZXMvcGRmL2FydGljbGUxNzYwNy5wZGY=)
- Voir [Navigation aux Astres et aux Satellites](https://navastro.fr/index.html?p659.html)
- Voir [ici](https://les-mathematiques.net/vanilla/discussion/59651/astronomie-plan-des-sommets)

![Context](img.png)

Pour le c&ocirc;ne 1 (le rouge), l'angle en O est `(90° - h1)`.  
La distance (grand cercle) `F - Pied1` est donc `(90° - h1) * 60.0` nm.

---

## Le principe  
- On est capable de d&eacute;terminer les points d'un cercle d&eacute;fini par les points qui voient un astre &agrave; la m&ecirc;me hauteur, &agrave; un instant donn&eacute;.
- Avec plusieurs observations (plusieurs astres), l'observateur se trouve &agrave; l'intersection de ces cercles.
- Pour chaque astre, les param&egrave;tres de l'&eacute;quation sont :
  - La hauteur de l'astre observ&eacute;
  - L'heure de l'observation
- On en d&eacute;duit :
  - Avec les &eacute;ph&eacute;m&eacute;rides, la position du point Pg de l'astre (D&eacute;clinaison et AHG), le centre du cercle.
  - Par le calcul, le lieu des points (un cercle centr&eacute; sur ce Pg) qui voient l'astre &agrave; la m&ecirc;me hauteur (celle qu'on a observ&eacute;e).

Avec plus d'un astre, la position de l'observateur - celle qu'on cherche - est &agrave; l'intersection de ces cercles.

![Context](01.png)

Voici la m&ecirc;me figure, d'un autre point de vue.  
On note que les g&eacute;n&eacute;ratrices des c&ocirc;nes tangentent la Terre &agrave; la base du c&ocirc;ne, _sur_ le cercle d'&eacute;gales hauteurs.  

![Context](02.png)

Les cercles se croisent &agrave; la position de l'observateur. Ainsi, la position de l'observateur et
toutes celles des sommets des c&ocirc;nes sont dans le m&ecirc;me plan.

![Context](02.bis.png)

L'&eacute;chelle des cartes pose ici un premier probl&egrave;me. Voici le contexte ci-dessus repr&eacute;sent&eacute; sur une carte 
Mercator.  
Deux premiers &eacute;l&eacute;ments sont &agrave; noter :
- Les cercles de hauteur ne sont pas ronds sur cette projection.
- Ils peuvent &ecirc;tre &eacute;normes.

Ils ne sont ronds ni sur une carte Mercator :
![Context Mercator](03.png)

Ni sur une carte Anaximandre :
![Context Mercator](04.png)

En fait, ils ne sont ronds que sur un globe.

## Demo (comme ci-dessus)

### 1 - G&eacute;n&eacute;rer les data
&Agrave; partir du r&eacute;pertoire `MPS` :
```
$ ./generate.cones.sh
```
Ce script execute le code Java de la classe `mps.Context01.java`. Ceux que ce int&eacute;resse iront voir le code.   
Ceci g&eacute;n&egrave;re les fichiers `.json` dans le r&eacute;pertoire `web/json`, qui seront utilis&eacute;s &agrave; partir d'une page HTML.

### 2 - Affichage des diagrammes
&Agrave; partir du r&eacute;pertoire `MPS/web` :
````
$ npm start
````
Ceci suppose que `nodeJS` est disponible sur le syst&egrave;me.  
Puis &agrave; partir d'un browser, acc&eacute;der &agrave; `http://localhost:8080/index.html`.  

Les diagrammes ci-dessus sont affich&eacute;s lorsqu'on utilise le bouton `SET POSITION`, visible 
sous la rubrique `Position on Earth`.

## En pratique
Vaste sujet... &Ccedil;a vient !

#### Une remarque &agrave; propos du point par droites de hauteurs, de Marcq Saint-Hilaire
Pour mettre cette m&eacute;thode en &oelig;uvre, on mesure la hauteur d'un astre au sextant, qu'on compare &agrave; ce qu'on
devrait observer si on &eacute;tait l&agrave; o&ugrave; l'estime nous situe, cette hauteur "&eacute;stim&eacute;e" est calcul&eacute;e gr&acirc;ce aux &eacute;ph&eacute;m&eacute;rides et aux tables de Dieumegard, pour l'heure (exacte)
de l'observation.  
La droite de hauteur obtenue est ensuite port&eacute;e sur la carte, perpendiculairement &agrave; l'azimut de 
l'astre observ&eacute;, lequel est _**calcul&eacute;**_ (par les tables de Bataille), _**et non pas observ&eacute;**_ !  
Ceci justifie - entre autres - la raison pour laquelle un intercept de plus de 15 miles est consid&eacute;r&eacute; comme suspect. Le
calcul de l'azimut se faisant &agrave; partir de la position estim&eacute;e, une "mauvaise" estime donne lieu
&agrave; un azimut potentiellement &eacute;rron&eacute;.

### Une premi&egrave;re approche...
On va partir - pour l'instant - d'une position estim&eacute;e, d'o&ugrave; on pourra calculer l'azimut des astres observ&eacute;s &agrave; l'aide des tables de Bataille.  
On pourra alors calculer le point du cercle d'&eacute;gales hauteur (de cet astre) pour cet azimut.

#### Exemple
```
On 2025-Aug-20 10:40:31 UTC: 
the Sun Decl 12º16.80'N, GHA 339º17.40', from 47º40.66'N / 3º08.14'W.
Seeing the Sun at altitude 49º22.52', in the 142.65º
```

### Une approche graphique ?
Le challenge ici est donc de trouver pour _**tous**_ les cercles le (ou les) point(s) communs.  
On peut essayer graphiquement, pour la latitude, puis pour la longitude...

Mais le calcul des coordonn&eacute;es de chaque cercle (ou c&ocirc;ne) requiert des ressources consid&eacute;rables (si on fait &ccedil;a manuellement)...

Si on part d'une position estim&eacute;e, on revient &agrave; un point par droites de hauteurs classique (Saint-Hilaire).
Si on a trois astres, on a trois droites, et on est &agrave; leur intersection...

D'o&ugrave; la question : _Quel est alors dans ce contexte l'int&eacute;ret de la m&eacute;thode du plan des sommets ?_


---