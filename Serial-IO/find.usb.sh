#!/bin/bash
# Find the address of a USB device
echo -e "Remove the device from its USB socket, hit [return] when done."
read a
li -lisa /dev/tty* > before.txt
echo -e "Plug the device back in its USB socket, hit [return] when done."
read a
li -lisa /dev/tty* > after.txt
#
echo -e "Device is:"
diff before.txt after.txt
#
rm before.txt after.txt

