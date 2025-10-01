# A MPS REST Server

Defines and implements REST operations to be invoked from a Web Browser, or any REST client.  
Based on the `http.HTTPServer`, aka `http-tiny-server`, in this project.  

WiP...  

# Status
- Infrastructure in place. 
- All operations are implemented.
- TODO: Sample web page invoking the services.

### Operations to be defined for MPS
- Get the body list ✅ `curl -X GET http://localhost:9999/mps/bodies`
- For a given date (UTC), for a given body, get GHA and D. ✅ `curl -X GET http://localhost:9999/mps/pg/Sun/2025-09-26T03:15:00`
- For a given position on Earth, with a given GHA and D, get ObsAlt and Z. ✅ `curl -X POST http://localhost:9999/mps/alt-and-z -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"hp":0.0,"sd":0.0,"gha":230.905951,"d":-1.313542}}'`, or just `curl -X POST http://localhost:9999/mps/alt-and-z -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"gha":230.905951,"d":-1.313542}}'`  
- For a given ObsAlt, with GHA and D, get the Cone Definition. ✅ `curl -X POST http://localhost:9999/mps/cone -d '{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}'`
- For 2 Cone Definitions, get Intersections, using `MPSToolBox.resolve2Cones`. ✅ 
  - `curl -X POST http://localhost:9999/mps/2-cones-intersections -d '[{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023},{"bodyName":"Jupiter","obsAlt":33.994908,"gha":300.336092,"d":21.678212}]'`
  - `curl -X POST http://localhost:9999/mps/2-cones-intersections -d '[{"bodyName":"Rigel","obsAlt":28.856483,"gha":334.991105,"d":-8.168236},{"bodyName":"Jupiter","obsAlt":33.994908,"gha":300.336092,"d":21.678212}]'`
- Process Intersections. ✅
  - Each intersection returns 4 points (2 on each circle)
  - `curl -X POST http://localhost:9999/mps/process-intersections -d '[{"bodyOneName":"Saturn","bodyTwoName":"Jupiter","coneOneIntersectionOne":{"latitude":47.677643,"longitude":-3.13567},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-10.904689,"longitude":13.240187},"coneTwoIntersectionTwo":{"latitude":-10.904689,"longitude":13.240187}},{"bodyOneName":"Saturn","bodyTwoName":"Rigel","coneOneIntersectionOne":{"latitude":47.677643,"longitude":-3.13567},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-63.20844,"longitude":-12.106294},"coneTwoIntersectionTwo":{"latitude":-63.20844,"longitude":-12.106294}}]'`

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
  . . .
]
```

```
$ curl -X GET http://localhost:9999/mps/bodies
```

```json
[
  "Sun",
  "Moon",
  "Aries",
  "Venus",
  "Mars",
  "Jupiter",
  "Saturn",
  "Acamar",
  "Achenar",
  "Acrux",
  "Adhara",
  "Aldebaran",
  . . .
  "Shaula",
  "Sirius",
  "Spica",
  "Suhail",
  "Vega",
  "Zubenelgenubi"
]
```

```
$ curl -X GET http://localhost:9999/mps/pg/Sun/2025-09-26T03:15:00
```

```json
{
  "hp": 0.002436097482257691,
  "sd": 0.26583491322480646,
  "gha": 140.8845418176559,
  "d": -1.2161652494865915
}
```

```
$ curl -X POST http://localhost:9999/mps/alt-and-z -d '{"pos":{"latitude":47.677667,"longitude":-3.135667},"pg":{"gha":230.905951,"d":-1.313542}}'
```

```json
{
  "alt": -27.99267688201456,
  "z": 56.96535412553529
}
```

```
$ curl -X POST http://localhost:9999/mps/cone -d '{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023}'
```

```json
{
  "pg": {
    "latitude": -3.048023,
    "longitude": -54.653345
  },
  "obsAlt": 22.276078,
  "earthCenterToConeSummit": 9054.74636065408,
  "bodyName": "Saturn",
  "observationTime": "2025-Oct-01 01:42:13 UTC",
  "circle": [
    {
      "point": {
        "latitude": 64.67589899999999,
        "longitude": -54.65334500000001
      },
      "z": 0
    },
    {
      "point": {
        "latitude": 64.65705352112906,
        "longitude": -52.491045430213255
      },
      "z": 1
    },
    {
      "point": {
        "latitude": 64.60060116656845,
        "longitude": -50.335316840711485
      },
      "z": 2
    },
    {
      "point": {
        "latitude": 64.50679232209163,
        "longitude": -48.19261416843048
      },
      "z": 3
    },
    {
      "point": {
        "latitude": 64.3760381912995,
        "longitude": -46.06916534911687
      },
      "z": 4
    },
    {
      "point": {
        "latitude": 64.20890196130344,
        "longitude": -43.97087011951062
      },
      "z": 5
    },
    {
      "point": {
        "latitude": 64.00608705409687,
        "longitude": -41.90321248467024
      },
      "z": 6
    },
    . . .
    {
      "point": {
        "latitude": 64.50679232209163,
        "longitude": -61.11407583156959
      },
      "z": 357
    },
    {
      "point": {
        "latitude": 64.60060116656845,
        "longitude": -58.971373159288504
      },
      "z": 358
    },
    {
      "point": {
        "latitude": 64.65705352112906,
        "longitude": -56.81564456978676
      },
      "z": 359
    }
  ]
}
```

```
$ curl -X POST http://localhost:9999/mps/2-cones-intersections -d '[{"bodyName":"Saturn","obsAlt":22.276078,"gha":54.653345,"d":-3.048023},{"bodyName":"Jupiter","obsAlt":33.994908,"gha":300.336092,"d":21.678212}]'
```

```json
[
  {
    "latitude": 47.67766804891214,
    "longitude": -3.135666894012773
  },
  {
    "latitude": 47.67766807844463,
    "longitude": -3.135666341694059
  },
  {
    "latitude": -10.904707499551664,
    "longitude": 13.240186639763486
  },
  {
    "latitude": -10.904708058980065,
    "longitude": 13.240187004313771
  }
]
```

```
$ curl -X POST http://localhost:9999/mps/process-intersections -d '[{"bodyOneName":"Saturn","bodyT{"latitude":47.677643,"longitude":-3.13567},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-10.904689,"longitude":13.240187},"coneTwoIntersectionTwo":{"latitude":-10.904689,"longitude":13.240187}},{"bodyOneName":"Saturn","bodyTwoName":"Rigel","coneOneIntersectionOne":{"latitude":47.677643,"longitude":-3.13567},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-63.20844,"longitude":-12.106294},"coneTwoIntersectionTwo":{"latitude":-63.20844,"longitude":-12.106294}}]' | jq
```

```json
{
  "latitude": 47.677643,
  "longitude": -3.13567
}
```

---

Next, we'll provide Web Pages invoking those REST services. That's going to be interesting.

---