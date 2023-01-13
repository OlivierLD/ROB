# Off we go!
### Aka "Get Started" for dummies

# Install required software, clone the repo, build and run a first module.

## You will need
- git
- Java (JDK 11)
- To access a GPS (and/or NMEA data) through a Serial Port, `librxtx-java`
- Python 3 (in some cases)

The modules of the project are built using `gradle`. It will be downloaded and installed automatically if not there yet.

All the commands described below are to be run from a terminal (unless mentioned otherwise).

## Install required software
- To know if `git` is available on your system:
```
$ which git
```
or 
```
$ git --version
```
- If missing, install git:
```
$ sudo apt-get install git-all
```

- To know if `Java` is available on your system:
```
$ which java
```
or
```
$ java -version
```
- If missing, install JDK 11:
```
$ sudo apt-get update 
$ sudo apt-get install openjdk-11-jdk
```
To install `librxtx-java`:
```
$ sudo apt-get install librxtx-java
```

> _**Note**_: To install the software above, you will need an Internet connection.

## Clone the git repo
From a directory of your choice, like a directory created for your git repositories, named below `repos`, created under youyr home directory by the following command:
```
$ cd
$ mkdir repos
```
then 
```
$ cd ~/repos
$ git clone https://github.com/OlivierLD/ROB.git
```
This will clone the repo (branch `master`) in to a directory named `ROB`.

> _**Note**_: Now your repository is cloned, at any time, to refresh it with its last modifications,
> from any folder under the root (`ROB` in this case), just do a
> ```
> $ git pull
> ```

> _**Note**_: To clone the repo, you need an Internet connection.
 

## Build the `NMEA-multiplexer` module
This step will validate all the required nuts and bolts.  
It requires an Internet connection, as it will need to pull some dependencies fromn somne `maven` repos.

If `gradle` is not on your machine yet, it will be downloaded (from the Internet) and installed (this happens only once).  
From your home directory, do a 
```
$ cd ~/repos/ROB
$ cd raspberry-sailor/NMEA-multiplexer
```
and then:
```
$ ../../gradlew shadowJar
```
This will compile all the required dependencies, and generate a jar (java-archive) named `./build/libs/NMEA-multiplexer-1.0-all.jar`.
It should finish with a 
```
. . .

BUILD SUCCESSFUL in 8s
13 actionable tasks: 1 executed, 12 up-to-date
$
```
If this is the case, try a
```
$ ./mux.sh  mux-configs/nmea.mux.replay.big.log.yaml 
```
This would replay an archived log file, and spit out its content on the terminal.  
Stop it with a `[Ctrl C]`.

If you reached this step without error messages, you are in good shape!

# Other modules
## `RESTNavServer`
. . .

# Customize
. . .

# Extra
. . .

---