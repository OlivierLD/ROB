# A MPS REST Server

Defines and implements REST operations to be invoked from a Web Browser, or any REST client.  
Based on the `http.HTTPServer`, aka `http-tiny-server`, in this project.  

WiP...  

# Status
- Infrastructure in place. 
- Not all operations are defined yet.

### Operations to be defined
- Get the body list ✅ `GET /mps/bodies`
- For a given date (UTC), for a given body, get GHA and D. See `/sight-reduction` and `/reverse-sight`
- For a given position on Earth, with a given GHA and D, get ObsAlt and Z. See `/sight-reduction` and `/reverse-sight`
- For a given ObsAlt, with GHA and D, get the Cone Definition
- For 2 Cone Definitions, get Intersections
- Process Intersections

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
For example
```
$ curl -X GET http://localhost:9999/mps/oplist
```
or, if `jq` is available
```
$ curl -X GET http://localhost:9999/mps/oplist | jq
```



---