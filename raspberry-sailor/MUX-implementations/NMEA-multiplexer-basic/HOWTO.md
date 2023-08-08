# From scratch
### An example: how to setup a new Raspberry Pi for a minimalist Nav Station.
- Use [Raspberry Pi imager](https://www.raspberrypi.com/software/) to flash a new SD Card
  - Make sure SSH is enabled.
- Use `./to.prod.sh` to package the current software
  - Make sure you package the python part as well, when prompted
  - This will prepare a `tar.gz` archive, called - for example - `nmea-dist.tar.gz`.
  - Send the archive to the new Raspberry Pi (change it's IP address at will)
    - ` scp nmea-dist.tar.gz pi@192.168.1.15:~`
- Prepare the new System. This will require an Internet connection.
  - Boot to CLI
  - Enable interfaces `SPI`, `I2C`, `Serial Port`
  - Install Java
  ```
  sudo apt-get update
  sudo apt-get install openjdk-11-jdk
  ```
  or (for some Raspberry Pi Zero)
  ```
  sudo apt-get install openjdk-8-jdk-headless
  ```
  - LibRxTx (optional)
  ```
  sudo apt-get install librxtx-java
  ```
  - Install required Python modules (if any), like for example
  ```
  sudo pip3 install adafruit-circuitpython-epd
  ```

- Setup Hotspot (if needed), as explained [here](./HOTSPOT.md).
- Expand the archive, using a command like `tar -xzvf nmea-dist.tar.gz`
- Modify the `/etc/rc.local` to start the server when the server boots
- Issue the required command (links, maps, stty, etc)
- Give it a try (see for example the script `start.all.sh`)
- Try `ssh` to make sure it's working as expected
- Try the web interface
  - From a browser on another machine (laptop, cell-phone, tablet, ...), connected on the Raspberry Pi's network, reach
    `http://192.168.50.10:9999/zip/index.html`, and see for yourself!

You're good to go!

### 3D Printed enclosures
STL files available [here](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/ProjectBoxRPiZeroBox.stl), and
[here](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/RPiDevBoards/ProjectBoxRPiZeroBoxTop.stl).

|                  In its box                   |          Connected (Power, GPS)          |
|:---------------------------------------------:|:----------------------------------------:|
 |   ![One](./doc_resources/01.in.the.box.jpg)   | ![One](./doc_resources/02.connected.jpg) | 
|                Up and Running                 |                  Closed                  |
| ![One](./doc_resources/03.up.and.running.jpg) |  ![One](./doc_resources/04.closed.jpg)   | 

---
