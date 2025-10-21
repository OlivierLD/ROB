# A PHP REST Astro Server ?
This directory contains what could look like a `php` Astro REST server...
_But_ when you write php, you write a _page_, **not** a server... This could be a problem for path parameters (for example). Also, all the urls for the requests would contain a `php` extension, like below,
in `http://machine:8000/astro/bodies.php`.


### Operations List

- `curl -X GET http://machine:8000/astro/bodies.php`
- `curl -X GET http://machine:8000/astro/mps/pg.php?body=Sun&date=2025-09-26T03:15:00`
- `curl -X POST http://machine:8000/astro/mps/alt-and-z.php -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"hp":0.0,"sd":0.0,"gha":230.905951,"d":-1.313542}}'`
  - or just `curl -X POST http://machine:8000/astro/mps/alt-and-z.php -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"gha":230.905951,"d":-1.313542}}'`
- `curl -X POST http://machine:8000/astro/mps/cone.php -d '{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}'`
- `curl -X POST http://localhost:8000/astro/mps/compute-cones.php -d '[ {"bodyName" : "Mars","date" : "2025-10-07T15:36:00","gha" : null,"decl" : null,"obsAlt" : 21.942333333333334}, {"bodyName" : "Venus","date" : "2025-10-07T15:36:00","gha" : null,"decl" : null,"obsAlt" : 14.014}, {"bodyName" : "Altair","date" : "2025-10-07T15:36:00","gha" : null,"decl" : null,"obsAlt" : 32.47716666666667} ]'`
- ... and more to come !


---