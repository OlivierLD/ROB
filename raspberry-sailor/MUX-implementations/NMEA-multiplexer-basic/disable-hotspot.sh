#!/bin/bash
nmcli device disconnect wlan0
nmcli device up wlan0
nmcli dev wifi show-password

