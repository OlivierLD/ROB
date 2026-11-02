#!/bin/bash
#
# Show visible networks.
#
echo -e "----- Available Networks -----"
nmcli dev wifi
echo -e "----- Current Network -----"
# Show the current one (if any):
nmcli dev wifi show-password