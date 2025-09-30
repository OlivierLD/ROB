# A MPS REST Server

Defines and implements REST operations to be invoked from a Web Browser, or any REST client.  
Based on the `http.HTTPServer`, aka `http-tiny-server`, in this project.  

WiP...  

# Status
- Infrastructure in place. 
- Not all operations are implemented yet.
- TODO: Unify the error codes

### Operations to be defined for MPS
- Get the body list ✅ `curl -X GET http://localhost:9999/mps/bodies`
- For a given date (UTC), for a given body, get GHA and D. ✅ `curl -X GET http://localhost:9999/mps/pg/Sun/2025-09-26T03:15:00`
- For a given position on Earth, with a given GHA and D, get ObsAlt and Z. ✅ `curl -X POST http://localhost:9999/mps/alt-and-z -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"hp":0.0,"sd":0.0,"gha":230.905951,"d":-1.313542}}'`, or just `curl -X POST http://localhost:9999/mps/alt-and-z -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"gha":230.905951,"d":-1.313542}}'`  
- For a given ObsAlt, with GHA and D, get the Cone Definition. ✅ `curl -X POST http://localhost:9999/mps/cone -d '{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}'`
- For 2 Cone Definitions, get Intersections, using `MPSToolBox.resolve2Cones`. ✅ 
  - `curl -X POST http://localhost:9999/mps/2-cones-intersections -d '[{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023},{"bodyName":"Jupiter","obsAlt":33.994908,"gha":300.336092,"d":21.678212}]'`
  - `curl -X POST http://localhost:9999/mps/2-cones-intersections -d '[{"bodyName":"Rigel","obsAlt":28.856483,"gha":334.991105,"d":-8.168236},{"bodyName":"Jupiter","obsAlt":33.994908,"gha":300.336092,"d":21.678212}]'`
- Process Intersections
  - Each intersection returns 4 points (2 on each circle)

## To build it
From this module's root:
```
$ ../../gradlew clean shadowJar
```

## To run it
```
$ ./runMPSServer.sh [--http-port:1234]
```

## To invoke it
Whatever REST client can do it.  
For example (`9999` being the default port)
```
$ curl -X GET http://localhost:9999/mps/oplist
```
or, if `jq` is available
```
$ curl -X GET http://localhost:9999/mps/oplist | jq
```

```json
[
  {
    "verb": "GET",
    "path": "/mps/oplist",
    "description": "List of all available operations, on astro request manager.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mps/bodies",
    "description": "Get the list of the bodies available here.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mps/pg/{body}/{utc-date}",
    "description": "Get the GHA and D for a given {body} at a given {utc duration}",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/alt-and-z",
    "description": "For a given user position, a given Pg (GHA, D, ...), get Observed Altitude and Azimut.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/cone",
    "description": "For a given GHD, D, and ObsAlt, get the Cone definition (MPSToolBox.ConeDefinition).",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/2-cones-intersections",
    "description": "Get the intersections of two MPSToolBox.ConeDefinition.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/process-intersections",
    "description": "Process the intersections of two MPSToolBox.ConeDefinition.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mps/positions-in-the-sky",
    "description": "Get the Sun's and Moon's position (D & GHA) for an UTC date passed as QS prm named 'at', in DURATION Format. Optional: 'fromL' and 'fromG', 'wandering' (true|[false]), 'stars' (true|[false]), 'constellations' (true|[false]).",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/sun-now",
    "description": "Create a request for Sun data now. Requires body payload (GeoPoint)",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/sun-path-today",
    "description": "Create a request for Sun path today. Requires body payload (GeoPoint & step)",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/declination",
    "description": "Get declination of one or more bodies between two UTC dates",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/sun-between-dates",
    "description": "Create a request for Sun data between 2 dates. Requires body payload (GeoPoint), and 3 queryString prm : from and to, in DURATION Format, and tz, the timezone name.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/sun-moon-dec-alt",
    "description": "Create a request for Sun data between 2 dates. Requires body payload (GeoPoint), and 2 to 3 queryString prm : from and to, in DURATION Format, and optional tz, the timezone name.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mps/utc",
    "description": "Get current UTC Date. Will return UTC time, system time, and optionally, the time(s) at the time zone(s) passed in QS prm 'tz', UTF-8 encoded, comma separated.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/publish/almanac",
    "description": "Generates nautical almanac document (pdf)",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/publish/lunar",
    "description": "Generates lunar distances document (pdf)",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/publish/perpetual",
    "description": "Generates perpetual nautical almanac document (pdf)",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mps/sight-reduction",
    "description": "Sight reduction user data sample (for development, to get the shape of the returned object)",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/sight-reduction",
    "description": "Sight reduction",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mps/reverse-sight",
    "description": "Reverse Sight reduction",
    "fn": {}
  }
]
```


---