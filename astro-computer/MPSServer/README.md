# A MPS REST Server

Defines and implements REST operations to be invoked from a Web Browser, or any REST client.  
Based on the `http.HTTPServer`, aka `http-tiny-server`, in this project.  

WiP...  

# Status
- Infrastructure in place. 
- No operation is defined yet.

### Operations to be defined
- For a given date (UTC), for a given body, get GHA and D
- For a given position on Earth, with a given GHA and D, get ObsAlt and Z
- For a given ObsAlt, with GHA and D, get the Cone Definition
- For 2 Cone Definitions, get Intersections
- Process Intersections

## To build it
From this module's root:
```
$ ../../gradlew clean shadowJar
```

## To invoke it
Whatever REST client can do it.
```
$ curl ....
```


---