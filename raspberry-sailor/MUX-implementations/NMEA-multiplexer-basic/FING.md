# fing = find + ping. WiP

Trying to come up with a script mimicking the `fing` utility...

```commandline
$ hostname
macbookpro.home

$ ifconfig | grep 192
inet 192.168.1.11 netmask 0xffffff00 broadcast 192.168.1.255
	
$ ifconfig | grep 'inet 192' | awk '{ print $2 }'
192.168.1.11


$ addr='192.168.1.11'
$ echo ${addr%.*}.
192.168.1.

$ host 192.168.1.1
1.1.168.192.in-addr.arpa domain name pointer livebox.home.

$ host 192.168.1.1 | awk '{ print $5 }'
livebox.home.

$ nslookup 192.168.1.1
Server:		2a01:cb08:86f0:2f00:a87:c6ff:fede:a9c0
Address:	2a01:cb08:86f0:2f00:a87:c6ff:fede:a9c0#53

$ toPing=192.168.1.1
$ arp -n ${toPing} | grep "${toPing}"
? (192.168.1.1) at 8:87:c6:de:a9:c0 on en0 ifscope [ethernet]

$ arp -n ${toPing} | grep "${toPing}" | awk '{ print $4 }'
8:87:c6:de:a9:c0

. . .
```

Current state of the art is in [fing.sh](fing.sh).

---