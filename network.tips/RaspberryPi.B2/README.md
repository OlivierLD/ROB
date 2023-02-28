# Raspberry Pi B2, 512 Mb of memory.
To find the model, do a
```
$ cat /proc/cpuinfo | grep 'Revision' | awk '{print $3}' | sed 's/^1000//'
```
Then look into the matrix in `SystemUtils.java`, method `getRPiHardwareRevision`:
```java
matrix.put("000e", new String[]{"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Sony UK)"});
```

The network config (when connecting to an existing network) is in `/etc/wpa_supplicant/wpa_supplicant.conf`

### To change the Network Config
The files `interfaces*` are to be located in `/etc/network/`.  
The files `hostapd*` are to be located in `/etc/default/`.

You can use the script `./switch.sh`. 
