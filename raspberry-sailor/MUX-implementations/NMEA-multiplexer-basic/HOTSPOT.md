# Setup HotSpot on Raspberry Pi

Do as indicated on <https://www.raspberryconnect.com/projects/65-raspberrypi-hotspot-accesspoints/168-raspberry-pi-hotspot-access-point-dhcpcd-method>.

Connected on the Raspberry Pi (directly, or using `ssh`), do the following commands:

```
sudo apt update
sudo apt upgrade
```
To install `hostapd`, enter the command:
```
sudo apt install hostapd
```
enter `Y` when prompted.

To install `dnsmasq` enter the command:
```
sudo apt install dnsmasq
```
enter `Y` when prompted

The installers will have set up the program so they run when the pi is started and activated them. While we set the hotspot we should stop them running. This is done with the following commands:
```
sudo systemctl stop hostapd
sudo systemctl stop dnsmasq
```

Define the hotspot network name and passphrase in `/etc/hostapd/hostapd.conf` (see `ssid` and `wpa_passphrase` properties):
```
# country_code=FR
interface=wlan0
driver=nl80211
ssid=NMEANetwork   # <- This is the network name, choose your own.
hw_mode=g
channel=6
wmm_enabled=0
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=PassWord
wpa_key_mgmt=WPA-PSK
# wpa_pairwise=TKIP
rsn_pairwise=CCMP
```

Add the following lines at the bottom of `/etc/dnsmasq.conf`

```
# RPiHotspot config - No Intenet
interface=wlan0
# bind-dynamic     # Add this for Internet config 
domain-needed
bogus-priv
dhcp-range=192.168.50.150,192.168.50.200,255.255.255.0,12h
```

Add the following lines at the bottom of `/etc/dhcpcd.conf`:
```
#
# Static HotSpot
interface wlan0
nohook wpa_supplicant
static ip_address=192.168.50.10/24
static routers=192.168.50.1
```
Enable the `hostapd` service:
```
sudo systemctl unmask hostapd
sudo systemctl enable hostapd
```
See its status:
```
sudo service hostapd status
sudo service dnsmasq status
```
Notice above, the network name and password:
- Network name is `NMEANetwork`
- Network password is `PassWord`

Those values cane be change as you wish.  

Once the above is done, you can reboot the Raspberry Pi.

---
