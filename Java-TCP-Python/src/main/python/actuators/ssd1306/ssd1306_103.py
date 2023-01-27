# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

"""
This demo will fill the screen with white, draw a black box on top
and then print Hello World! in the center of the display

This example is for use on (Linux) computers that are using CPython with
Adafruit Blinka to support CircuitPython libraries. CircuitPython does
not support PIL/pillow (python imaging library)!
"""

import board
import digitalio
import PIL
from PIL import Image, ImageDraw, ImageFont
import adafruit_ssd1306
import time
import subprocess


# Define the Reset Pin
oled_reset = digitalio.DigitalInOut(board.D4)

# Change these
# to the right size for your display!
WIDTH: int = 128
HEIGHT: int = 32  # Change to 64 if needed
BORDER: int = 5

WHITE: int = 255
BLACK: int = 0

# Use for I2C.
i2c = board.I2C()  # uses board.SCL and board.SDA
# i2c = board.STEMMA_I2C()  # For using the built-in STEMMA QT connector on a microcontroller
oled: adafruit_ssd1306.SSD1306_I2C = adafruit_ssd1306.SSD1306_I2C(WIDTH, HEIGHT, i2c, addr=0x3C, reset=oled_reset)
# print(f"Oled is a {type(oled)}")


# Use for SPI
# spi = board.SPI()
# oled_cs = digitalio.DigitalInOut(board.D5)
# oled_dc = digitalio.DigitalInOut(board.D6)
# oled = adafruit_ssd1306.SSD1306_SPI(WIDTH, HEIGHT, spi, oled_dc, oled_reset, oled_cs)

# Clear display.
oled.fill(BLACK)
oled.show()

# Create blank image for drawing.
# Make sure to create image with mode '1' for 1-bit color.
image: PIL.Image.Image = Image.new("1", (oled.width, oled.height))
# print(f"Image is a {type(image)}")

# Get drawing object to draw on image.
draw: PIL.ImageDraw.ImageDraw = ImageDraw.Draw(image)
# print(f"Draw is a {type(draw)}")

# Draw a white background
draw.rectangle((0, 0, oled.width, oled.height), outline=WHITE, fill=WHITE)

# Draw a smaller inner rectangle, in black
draw.rectangle(
    (BORDER, BORDER, oled.width - BORDER - 1, oled.height - BORDER - 1),
    outline=BLACK,
    fill=BLACK,
)

# Load default font.
font: PIL.ImageFont.ImageFont = ImageFont.load_default()
# print(f"Font is a {type(font)}")

# Draw Some Text
text: str = "Hello SSD1306!"
(font_width, font_height) = font.getsize(text)
draw.text(
    (oled.width // 2 - font_width // 2, oled.height // 2 - font_height // 2),
    text,
    font=font,
    fill=WHITE,
)

# Display image
oled.image(image)
oled.show()

# Wait a bit
time.sleep(1)

# Draw some shapes.
# First define some constants to allow easy resizing of shapes.
padding: int = -2
top: int = padding
bottom: int = oled.height - padding
# Move left to right keeping track of the current x position for drawing shapes.
x: int = 0

print("Ctlr-C to exit")
keep_looping: bool = True
while keep_looping:
	try:
		# Draw a black filled box to clear the image.
		draw.rectangle((0, 0, oled.width, oled.height), outline=0, fill=BLACK)

		# Shell scripts for system monitoring from here : https://unix.stackexchange.com/questions/119126/command-to-display-memory-usage-disk-usage-and-cpu-load
		cmd = "hostname -I | cut -d\' \' -f1"
		IP = subprocess.check_output(cmd, shell=True )
		cmd = "top -bn1 | grep load | awk '{printf \"CPU Load: %.2f\", $(NF-2)}'"
		CPU = subprocess.check_output(cmd, shell=True )
		cmd = "free -m | awk 'NR==2{printf \"Mem: %s/%sMB %.2f%%\", $3,$2,$3*100/$2 }'"
		MemUsage = subprocess.check_output(cmd, shell=True )
		cmd = "df -h | awk '$NF==\"/\"{printf \"Disk: %d/%dGB %s\", $3,$2,$5}'"
		Disk = subprocess.check_output(cmd, shell=True )

		draw.text((x, top),      "IP: " + str(IP.decode('utf-8')),  font=font, fill=WHITE)
		draw.text((x, top + 8),  str(CPU.decode('utf-8')), font=font, fill=WHITE)
		draw.text((x, top + 16), str(MemUsage.decode('utf-8')),  font=font, fill=WHITE)
		draw.text((x, top + 24), str(Disk.decode('utf-8')),  font=font, fill=WHITE)
		# draw.text((x, top + 40),    "  shahrulnizam.com  ",font=font, fill=WHITE)

		# Display image.
		oled.image(image)
		# oled.display()
		oled.show()
		time.sleep(1)
	except KeyboardInterrupt:
		keep_looping = False
		print("Exiting at user's request")
	except Exception as ex:
		print("Oops! {}".format(ex))


oled.fill(BLACK)
oled.show()
print("Bye!")
