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
Also look at [this](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md), in the same repo.

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
Also see the doc [here](./raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/HOTSPOT.md)

### Option 4
See this script: <https://gist.github.com/atlury/fe0ea8b91a981c103df7>

## Backup / Restore SD card
See [here](https://pimylifeup.com/backup-raspberry-pi/).

On Mac OS:
```
$ diskutil list
```
Then once the drive is identified
```
$ sudo dd if=/dev/disk3 of=~/PiSDBackup.dmg  [bs=1m]
```
Or
```
$ sudo dd if=/dev/disk3 | gzip -c > backup.raspian.img.gz   << Preferred
```
it can take time (1630 seconds here)... :
```
$ sudo time dd if=/dev/disk4 | gzip -c > raspi.backup.img.gz
Password:
31291392+0 records in
31291392+0 records out
16021192704 bytes transferred in 1629.770212 secs (9830338 bytes/sec)
     1629.78 real        35.11 user       219.39 sys
```
then
```
$ diskutil eject /dev/disk3
```

### Same thing, another way
See at https://www.raspberrypi.org/documentation/linux/filesystem/backup.md  
Also:
https://thepihut.com/blogs/raspberry-pi-tutorials/17789160-backing-up-and-restoring-your-raspberry-pis-sd-card

```
$ diskutil list
/dev/disk0 (internal, physical):
#:                       TYPE NAME                    SIZE       IDENTIFIER
0:      GUID_partition_scheme                        *500.3 GB   disk0
1:                        EFI EFI                     209.7 MB   disk0s1
2:          Apple_CoreStorage Macintosh HD            499.4 GB   disk0s2
3:                 Apple_Boot Recovery HD             650.0 MB   disk0s3
/dev/disk1 (internal, virtual):
#:                       TYPE NAME                    SIZE       IDENTIFIER
0:                  Apple_HFS Macintosh HD           +499.1 GB   disk1
Logical Volume on disk0s2
CED0CEAE-78CD-419D-A636-9EFF60EDBB93
Unlocked Encrypted
/dev/disk2 (internal, physical):
#:                       TYPE NAME                    SIZE       IDENTIFIER
0:     FDisk_partition_scheme                        *15.5 GB    disk2
1:             Windows_FAT_16 RECOVERY                1.2 GB     disk2s1
2:                      Linux                         33.6 MB    disk2s5
3:             Windows_FAT_32 boot                    66.1 MB    disk2s6
4:                      Linux                         14.3 GB    disk2s7
```

```
$ sudo dd if=/dev/disk2 | gzip -c > backup.raspian.img.gz   << Preferred
```
Or
```
$ sudo dd if=/dev/disk1 of=~/SDCardBackup.img
```
then
```
$ diskutil eject /dev/disk1
```
Restore backup:
```
$ diskutil list
$ diskutil unmountDisk /dev/disk2
$ sudo dd if=backup.my.sdcard-18-oct-2015.img.gz of=/dev/disk2
### Restores compressed image and write /dev/disk2 ###
$ sudo sh -c 'gunzip -c backup.disk.img.dd.gz | dd of=/dev/disk2’
```
Or use Etcher (recommended).   `<< Preferred (use the gz file)`

Backup:  
From the Raspberry Pi Graphical Desktop, use the SD Card Copier. Just make sure the card is not locked (write protected).

## Similar and interesting soft

- <https://kingtidesailing.blogspot.com/2016/04/make-wireless-nmea-0183-multiplexer.html>
- <https://opensource.com/article/23/3/build-raspberry-pi-dashboard-appsmith>





---
