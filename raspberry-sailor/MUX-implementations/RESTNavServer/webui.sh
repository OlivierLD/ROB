#!/bin/bash
#
# Sample, showing how to launch Chromium in kiosk mode, on Debian/RasPi
#
# chromium --incognito --kiosk http://localhost:9999/web/
chromium-browser --incognito --kiosk http://localhost:9999/web/
#                                    |
#                                    All the tabs, separated by blanks
#
#
