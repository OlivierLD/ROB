# Misc Hints and Tips for the Raspberry Pi
A batch of useful and other things...

## Useful soft
- Fing CLI: <https://www.fing.com/products/development-toolkit>
  - Doc at <https://get.fing.com/fing-business/devrecog/documentation/Fing_CLI.pdf>
- Many useful hints: <https://www.raspberrypi.com/documentation/computers/raspberry-pi.html>

## Find the Serial port of your GPS (or whatever)
- Run `raspberry-sailor/NMEA-multiplexer/find.port.sh`


## ad-hoc / hotspot networking:
### Option 1
See <https://pyshine.com/How-to-configure-Raspberry-Pi-in-Ad-hoc-wifi-mode/>

### Option 2
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

### Option 3 - My preferred one
To know what release you're running on:
```
$ lsb_release -a
```
See <https://www.raspberryconnect.com/projects/65-raspberrypi-hotspot-accesspoints/168-raspberry-pi-hotspot-access-point-dhcpcd-method>, that one uses 
`hostapd` and `dnsmasq`. Works fine.  
May need, to begin with: 
```
$ sudo apt-get update
$ sudo apt-get upgrade
$ sudo apt-get dist-upgrade
```
This way, it works even on a Raspberry Pi A+.  
> Note: in `/etc/dnsmasq.conf`:
> ```
> # RPiHotspot config - No Internet
> interface=wlan0
> bind-dynamic
> domain-needed
> bogus-priv
> dhcp-range=192.168.50.150,192.168.50.200,255.255.255.0,12h
> ```

---
To _disable_ the HotSpot (and get back to a regular Internet config):
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
---
To _re-enable_ the HotSpot:
Uncomment the 4 lines at the bottom of `/etc/dhcpcd.conf` (see above):
```
sudo systemctl unmask hostapd
sudo systemctl enable hostapd
#
sudo systemctl unmask dnsmasq
sudo systemctl enable dnsmasq
```
Status:
```
sudo service hostapd status
sudo service dnsmasq status
```

### Option 4
See this script: <https://gist.github.com/atlury/fe0ea8b91a981c103df7>

## Backup / Restore SD card
See [here](https://pimylifeup.com/backup-raspberry-pi/).

On Mac OS:
```
$ diskutil list
```

```
$ sudo dd if=/dev/disk3 of=~/PiSDBackup.dmg
```

## Similar and interesting soft
- <https://kingtidesailing.blogspot.com/2016/04/make-wireless-nmea-0183-multiplexer.html>
- <https://opensource.com/article/23/3/build-raspberry-pi-dashboard-appsmith>

---
