# Scripts to go to production ! WiP...
To prepare the software to deploy, run the script `to.prod.sh`.  
For all basic features:
```commandline
$ ./to.prod.sh [--basic]
```
or, if you want tides, almanacs publications and more:
```commandline
$ ./to.prod.sh --big
```

This can be used to create a brand new SD Card for the Raspberry Pi,
as explained [here](../NMEA-multiplexer-basic/HOWTO.md)

# To create a HotSpot for/from the Raspberry Pi
With `nmcli`, see [here](../../../HintsAndTips.md#nmcli).

The archive generated above comes with a folder called `scripts`, that contain scripts
that can be run from a `/etc/rc.local` to create the required HotSpot (see `mk-hotspot.sh`, or `mk.hotspot.sh`)
when the Raspberry Pi boots.  
See also the other scripts in the same folder

---