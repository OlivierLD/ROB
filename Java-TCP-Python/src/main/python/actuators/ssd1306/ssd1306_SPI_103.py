"""
I2C or SPI Version, see below

This demo will fill the screen with white, draw a black box on top
and then print Hello World! in the center of the display

This example is for use on (Linux) computers that are using CPython with
Adafruit Blinka to support CircuitPython libraries. CircuitPython does
not support PIL/pillow (python imaging library)!
"""

# https://docs.micropython.org/en/latest/esp8266/tutorial/ssd1306.html?highlight=ssd1306
# https://learn.adafruit.com/circuitpython-on-raspberrypi-linux?view=all

import board
import digitalio
import PIL
from PIL import Image, ImageDraw, ImageFont
import adafruit_ssd1306
import time
# import math

import ssd1306Utils

# Define the Reset Pin
# oled_reset = digitalio.DigitalInOut(board.D4)  # GPIO 4, Pin #7
oled_reset = digitalio.DigitalInOut(board.D24)  # GPIO 24, Pin #18

# Change these
# to the right size for your display!
WIDTH: int = 128
HEIGHT: int = 64 # 32  # Change to 64/32 if needed
BORDER: int = 5

WHITE: int = 255
BLACK: int = 0

display: None

# Use for I2C.
if False:
    i2c = board.I2C()  # uses board.SCL and board.SDA
    # i2c = board.STEMMA_I2C()  # For using the built-in STEMMA QT connector on a microcontroller
    display: adafruit_ssd1306.SSD1306_I2C = adafruit_ssd1306.SSD1306_I2C(WIDTH, HEIGHT, i2c, addr=0x3C, reset=oled_reset)
    # print(f"Oled is a {type(oled)}")

# Use for SPI
if True:
    # spi: busio.SPI = board.SPI()
    spi = board.SPI()
    print(f"SPI board type: {type(spi)}")
    # oled_cs = digitalio.DigitalInOut(board.D5)
    oled_cs = digitalio.DigitalInOut(board.D8)  # Pin #24
    # oled_dc = digitalio.DigitalInOut(board.D6)
    oled_dc = digitalio.DigitalInOut(board.D23)  # Pin #16
    display: adafruit_ssd1306.SSD1306_SPI = adafruit_ssd1306.SSD1306_SPI(WIDTH, HEIGHT, spi, oled_dc, oled_reset, oled_cs)


# Load default font.
font: PIL.ImageFont.ImageFont = ImageFont.load_default()
print(f"Font is a {type(font)}")

# Create blank image for drawing.
# Make sure to create image with mode '1' for 1-bit color.
image: PIL.Image.Image = Image.new("1", (display.width, display.height))
print(f"Image is a {type(image)}")

# Get drawing object to draw on image.
draw: PIL.ImageDraw.ImageDraw = ImageDraw.Draw(image)
print(f"Draw is a {type(draw)}")


display.fill(BLACK)  # cls
# Display a string -------
print("Display string, in the middle")
text: str = "Et Paf !"
left, top, right, bottom = font.getbbox(text)
(font_width, font_height) = right - left, bottom - top
draw.text(
    (display.width // 2 - font_width // 2, display.height // 2 - font_height // 2),
    text, font=font, fill=WHITE)
display.image(image)
display.show()
# ------------------------

time.sleep(2)

print("CLS...")
# draw.rectangle((0, 0, oled.width, oled.height), outline=BLACK, fill=BLACK)
# # Display image.
# oled.image(image)
# oled.show()

# display.fill(BLACK)  # cls
draw.rectangle((0, 0, display.width, display.height), outline=BLACK, fill=BLACK)
display.image(image)
display.show()
time.sleep(0.01)
# A rectangle
print("Display Rectangles and circle")
draw.text((1, 1), "Pouet", font=font, fill=WHITE)  # Ignored, without display.image(image)...
display.image(image)
# time.sleep(1)
ssd1306Utils.draw_rectangle(display, 10, 20, 12, 12)
ssd1306Utils.fill_rectangle(display, 20, 30, 12, 12)
ssd1306Utils.draw_circle(display, 80, 30, 15)
# Display
# display.image(image)
display.show()
# -------------------------

time.sleep(5)

display.fill(BLACK)  # cls
# A rectangle
print("Draw Line")
ssd1306Utils.draw_line(display, 10, 5, 100, 50)
# Display
display.show()
# -------------------------

time.sleep(5)

display.fill(BLACK)

# plot shapes and pixels
print("Display Shapes and Pixels, no Utils...")
# Set a pixel in the origin [0,0] position.
for i in range(6):
    for j in range(6):
        display.pixel(i, j, 1)
# Set a pixel in the middle [64, 16] position.
display.pixel(64, 16, 1)
# Set a pixel in the opposite [127, 31] position.
display.pixel(127, 31, 1)

#  cls
# oled.fill(BLACK)
display.show()

time.sleep(5)

# Clean and exit
display.fill(BLACK)
display.show()
print("Bye!")