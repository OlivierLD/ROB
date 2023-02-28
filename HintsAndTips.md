# Misc Hints and Tips for the Raspberry Pi
A batch of useful and other things...

## Useful soft
- Fing CLI: <https://www.fing.com/products/development-toolkit>
  - Doc at <https://get.fing.com/fing-business/devrecog/documentation/Fing_CLI.pdf>


## Find the Serial port of your GPS (or whatever)
- Run `raspberry-sailor/NMEA-multiplexer/find.port.sh`


## ad-hoc / hotspot networking:
See <https://pyshine.com/How-to-configure-Raspberry-Pi-in-Ad-hoc-wifi-mode/>

That one sort of works (from <https://forums.raspberrypi.com/viewtopic.php?t=24615>):
```
wpa_cli terminate
sudo ifconfig wlan0 down
sudo iwconfig wlan0 mode ad-hoc
sudo iwconfig wlan0 essid my-adhoc-name
# sudo iwconfig wlan0 essid my-adhoc-name key s:net-pwsd
# sudo iwconfig wlan0 key s:net-pwsd
sudo iwconfig wlan0 channel 1
sudo ifconfig wlan0 up
```
Or better: <https://www.raspberryconnect.com/projects/65-raspberrypi-hotspot-accesspoints/168-raspberry-pi-hotspot-access-point-dhcpcd-method>

To disable the HotSpot:
```
sudo systemctl disable dnsmasq
sudo systemctl disable hostapd
```
Then comment the lines at the bottom of `/etc/dhcpcd.conf`:  (do not forget the **c** in "dhcp**c**d.conf")
```
#
# Static HotSpot
# interface wlan0
# nohook wpa_supplicant
# static ip_address=192.168.50.10/24
# static routers=192.168.50.1
```
To re-enable the HotSpot:
Uncomment the 4 lines at the bottom of `/etc/dhcpcd.conf` (see above):
```
sudo systemctl unmask hostapd
sudo systemctl enable hostapd
```
Status:
```
sudo service hostapd status
sudo service dnsmasq status
```

## Backup / Restore SD card
See [here](https://pimylifeup.com/backup-raspberry-pi/).

On Mac OS:
```
$ diskutil list
```

```
$ sudo dd if=/dev/disk1 of=~/PiSDBackup.dmg
```

---
