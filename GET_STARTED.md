# Off we go!
### Aka "Get Started" for dummies

This document should help anyone who's not familiar with programming, build, and such technologies.
If that is not the case, please do let me know, log a bug or a request.

From this page, you will learn how to install the required software to build and run the whole stuff, from the git repo. 

There will be a dedicated section, describing the way to _build and deploy_ the result to another machine, without having to deal with the repository, which is obviously not required
at runtime.

> _**Note**_:   
> - This document is written for RaspiOS, Debian, Ubuntu, and such clones.  
> - The scripts in this repo are written for Bash Shell (`bash`).

# Install required software, clone the repo, build and run a first module.

## You will need:
- `git`
- `Java` (JDK 11)
- To access a GPS (and/or NMEA data) through a Serial Port, `librxtx-java`
- `Python 3` (in some cases)

> _**Note**_: The modules of the project are built using `gradle`. It will be downloaded and installed automatically if not there yet.  
> So, you do _NOT_ need to worry about it.

All the commands described below are to be run from a terminal (unless mentioned otherwise).

> _**Note**_: in the following instructions, make sure you respect the provided syntax.  
> When you see something like 
> ```
> ../../../gradlew shadowJar
> ```
> _**do enter**_ the command's leading `../../../` ! (Thanks to Captain K)  
> 
> Also, when you see a leading `$` in the commands below, this is the console's prompt. Do _**not**_ type it.  
> When it says `$ which git`, what you actually need to type is just `which git`.  
> Depending on your config (in `~/.bashrc`, `~/.bash-profile`, or so), this prompt may vary. It can also give you your current directory
> or other environment data.  
> Cheers!

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
From a directory of your choice, like a directory created for your git repositories, named below `repos`, created under your home directory by the following command:
```
$ cd
$ mkdir repos
```
then 
```
$ cd ~/repos
$ git clone https://github.com/OlivierLD/ROB.git
```
This will clone the repo (branch `master`) into a directory named `ROB`.

> _**Note**_: Now your repository is cloned, at any time, to refresh it with its last modifications,
> from any folder under the root (`ROB` in this case), just do a
> ```
> $ git pull
> ```

> _**Note**_: To clone the repo, you need an Internet connection.
 

## Build the `NMEA-multiplexer` module
This step will validate all the required nuts and bolts.  
It requires an Internet connection, as it will need to pull some dependencies from some `maven` repos.

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

You will notice a bunch of other scripts (`log.analyzer.sh`, `log.merge.sh`, `log.shrinker.sh`, `log.to.csv.sh`, 
`log.to.gpx.sh`, `log.to.json.sh`, `log.to.kml.sh`, `log.to.polars.sh`, `mk.link.sh`, ...). We'll come back to them later.

# Other modules
## `RESTNavServer`
This `RESTNavServer` module gather a bunch of examples of what can be done from the NMEA-multiplexer and related 
modules, from the smallest implementation showing how to log data when hiking or kayaking (but with a Web UI!), to a bigger
one, with admin features, celestial almanacs and tide tables publication, GRIB and faxes management, etc.

### Build it
From the repo's root:
```
$ cd ~/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer
```
do a 
```
$ ../../../gradlew shadowJar

> Configure project :
>> From task compileJava (in rob), using java version 11 
>> From task compileTestJava (in rob), using java version 11 

> Configure project :astro-computer:AstroComputer
>> From task compileJava (in AstroComputer), using java version 11 
>> From task compileTestJava (in AstroComputer), using java version 11 

> Configure project :astro-computer:AstroUtilities
>> From task compileJava (in AstroUtilities), using java version 11 
>> From task compileTestJava (in AstroUtilities), using java version 11 

. . .

BUILD SUCCESSFUL in 8s
33 actionable tasks: 1 executed, 32 up-to-date
```
If no error message show up, you can proceed to the next step.

### Run it
There are a lot of examples in this module. To facilitate the access to those examples, there is a demo script, `demoLauncher.sh`,
to be - like many others - started from a terminal. This script offers the possibility to start a browser
when appropriate. To use this feature, you obviously need to be in a Graphical Desktop Environment; you will then start
a the `demoLauncher.sh` from a terminal open in the desktop.

#### Get to it, fast
After doing the build as explained above, do a
```
$ cd launchers
```
and then, try the following command, to start the server (with a specific config), and open a browser:
```
$ ./demoLauncher.sh --option:1 --nohup:N --browser:Y
```
This will open the browser with a URL like <http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n>, with arbitrary position, 
but using the system current time.  

![Console](./images/01.console.png)

> _**Note**_: to kill the server, use the script `./killns.sh`

Now, try going to <http://localhost:9999/web/index.html>. You would see a page with a hamburger (&#9776;) at the top left, from which you can access to a menu.

| Menu collapsed | Menu opened |
|:----------------------------------:|:-----------------------------:|
| ![Hamburger](./images/01.menu.png) | ![Menu](./images/02.menu.png) |

To try:
- In the Weather Wizard section, try "2 - Operations" (this requires an Internet connection, for now)
![Weather Wizard](./images/01.ww.png)
  - As per the request definition (the drop-down list at the top left part, saying "North Atlantic, current analysis (fine, 2-day GRIB)),
    this shows a GRIB and 4 faxes on the North Atlantic.
  - The orange display show the GRIB data at the position of the mouse on the chart,
    True Wind Speed and Direction, Air Temperature, Atmospheric Pressure, Precipitation Rate.
  - GRIB and faxes can be shown or hidden at will, by using checkboxes on the right pane.
- Various NMEA Consoles
  - This is a bunch of console examples, that can be displayed on several kinds of devices (laptop, tablets, cell-phone, smartwatches) 
- Tides / Select Tide Station
  ![Tides Port-Tudy](./images/01.tides.png)
- Tides / Publish tide almanacs

#### Explore the menu
Just type 
```
$ ./demoLauncher.sh
```
... and see for yourself !

The same Web pages can be accessed from other devices on the same network (other laptops, tablets, cell-phones).
To facilitate the URL entry, go to "Nav Menu" (from `http://<your-ip>:9999/web/index.html`), and choose the `QR Codes` entry.  
Make sure you use the server's IP address or name (not "localhost"), and then you can flash
the generated QR Code from any other device.

![QR Code](./images/QRCodes.png)

> _**Note**_: to kill the server, run the script `./killns.sh`, or
> use the `K` option in the `demoLauncher.sh`.


# Customize
. . .

# Extra
. . .

---