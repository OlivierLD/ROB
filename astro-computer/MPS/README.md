# M&eacute;thode du Plan des Sommets (MPS)
Yves Robin-Jouan,1995-96

This is a workbench... We want to see if this can be done manually.  
Some investigations need to be done.

- See [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)
- See [Navigation aux Astres et aux Satellites](https://navastro.fr/index.html?p659.html)
- See [here](https://les-mathematiques.net/vanilla/discussion/59651/astronomie-plan-des-sommets)

![Context](img.png)

Pour le c&ocirc;ne 1 (le rouge), l'angle en O est `(90&deg; - h1)`.  
La distance (grand cercle) `F - Pied1` est donc `(90&deg; - h1) * 60.0`.

---

## Le principe  
- On est capable de d&eacute;terminer les points d'un cercle d&eacute;fini par les points qui voient un astre &agrave; la m&ecirc;me hauteur - &agrave; un instant donn&eacute;.
- Avec plusieurs observations (plusieurs astres), l'observateur se trouve &agrave; l';'intersection de ces cercles.
- Les param&egrave;tres de l'&eacute;quation sont :
  - La hauteur de l'astre observ&eacute;
  - L'heure de l'observation
- On en d&eacute;duit :
  - La position du point Pg de l'astre (D&eacute;clinaison et AHG)
  - Le lieu des points (un cercle) qui voient l'astre &agrave; la m&ecirc;me hauteur (celle qu'on a observ&eacute;e)

Avec plus d'un astre, la position de l'observateur - celle qu'on cherche - est &agrave; l'intersection de ces cercles.

![Context](01.png)

---