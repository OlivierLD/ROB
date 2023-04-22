# Case Study (WiP)
This document shows how web servers can be implemented, in several languages. They all give the same result (hopefully). This is just to give you an idea of the
effort it takes, and that will also give you the possibility to measure (later on) the memory and CPU it takes in each case.

> _**Here is the main requirement**_:  
> I want to run a simple HTTP server on my own local box,
> possibly managing REST requests.  
> How can I do that?  
> What language can I use?

## Here is an example use case

- On some remote server of yours, you have run some `gradle test`, and you want to see their result on their web page.
- From the directory you did the `gradle test` from, you want to see the test results, from a browser running on a laptop, which is not where the tests have been running.
- If the IP address of the _server_ where the tests have been running is `192.168.1.18`, you'd like to reach a url like <http://192.168.1.18:9876/build/reports/tests/test/index.html> ...

This requires some kind of small HTTP server, to serve this HTTP request.  
The server will have - obviously - to be running on the box the `gradle test` has been performed on,
the one with the IP address `192.168.1.18` here. 

Several options are available.


## Python
Port default value is 8000, if the variable `HTTP_PORT` (see below) is not set.
```text
$ HTTP_PORT=8000
$ python3 -m http.server ${HTTP_PORT}
```

## NodeJS
Requires a little bit of code, like this [`server.js`](./server.js).  
Then run `node server.js`. The port number is set in the code, look inti it.

## php
To know if a php server is installed, type
```
 $ which php
```
or
```
 $ php -v
   PHP 7.1.16 (cli) (built: Mar 31 2018 02:59:59) ( NTS )
   Copyright (c) 1997-2018 The PHP Group
   Zend Engine v3.1.0, Copyright (c) 1998-2018 Zend Technologies
```
If no php server is available, you can install one.
#### On Raspberry Pi
```
 $ sudo apt-get install php libapache2-mod-php
```

### Run a PHP server locally
```
$ HOSTNAME=$(hostname -I | awk '{ print $1 }' 2>/dev/null) || HOSTNAME=localhost
$ php -S ${HOSTNAME}:3000
```
Use the server's IP address if you want to reach it from another box, not `localhost`.

## Java (_this_ module 😎 !)
Aha! Now we're talking.  
Build the project (`../gradlew shadowJar`), and run the script `small.server.sh`.
Look into it for details.

## _Note_
Once started, all the servers above can be accessed in a language agnostic way,
like with `curl` or `wget`, or whatever piece of code that can act as an HTTP client.

From the client side:
```
HOST_NAME=192.168.1.18
HTTP_PORT=9876
$ curl -X GET http://${HOST_NAME}:${HTTP_PORT}/web/index.html
$ wget http://${HOST_NAME}:${HTTP_PORT}/web/index.html -o test.html
```
Of course, modify the environment variables `HOST_NAME` and `HTTP_PORT`.  
Depending on the various flavors, the ports are `8000`, `8080`, `3000`, `9876`. They all can be changed,
but just make sure you are using in the request the port you've set for the server.

And by the way, to get to the test results we started from, from a browser, you want
to reach <http://192.168.1.18:9876/build/reports/tests/test/index.html>. Make sure the port matches your server's settings, and make sure you've started the server
from the directory you ran the tests from.

---
