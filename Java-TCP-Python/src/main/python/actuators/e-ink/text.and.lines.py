"""
From https://docs.circuitpython.org/projects/epd/en/latest/
"""
import digitalio
import busio
import board
from adafruit_epd.epd import Adafruit_EPD
# from adafruit_epd.il0373 import Adafruit_IL0373
from adafruit_epd.ssd1675 import Adafruit_SSD1675

from PIL import Image, ImageDraw, ImageFont
import math

# create the spi device and pins we will need
spi = busio.SPI(board.SCK, MOSI=board.MOSI, MISO=board.MISO)
ecs = digitalio.DigitalInOut(board.CE0)
dc = digitalio.DigitalInOut(board.D22)
rst = digitalio.DigitalInOut(board.D27)
busy = digitalio.DigitalInOut(board.D17)
srcs = None

# /usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf
medium_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 20)

# give them all to our driver
print("Creating display")
# display = Adafruit_IL0373(104, 212, spi,          # 2.13" Tri-color display
#                           cs_pin=ecs, dc_pin=dc, sramcs_pin=srcs,
#                           rst_pin=rst, busy_pin=busy)
display = Adafruit_SSD1675(122, 250, spi, 
                           cs_pin=ecs, dc_pin=dc, sramcs_pin=srcs,
                           rst_pin=rst, busy_pin=busy)

display.rotation = 1

# clear the buffer
print("Clear buffer")
display.fill(Adafruit_EPD.WHITE)

print("Pixel")
display.pixel(10, 100, Adafruit_EPD.BLACK)
for x in range(10, 50):
    display.pixel(x, 100, Adafruit_EPD.BLACK)

if True:
    print("Draw Rectangles")
    display.fill_rect(5, 5, 10, 10, Adafruit_EPD.BLACK)   # Was RED
    display.rect(0, 0, 20, 30, Adafruit_EPD.BLACK)

    print("Draw lines")
    display.line(0, 0, display.width-1, display.height-1, Adafruit_EPD.BLACK)
    display.line(0, display.height-1, display.width-1, 0, Adafruit_EPD.BLACK)

    print("Draw text")
    # text(string: str, x: int, y: int, color: int, *, font_name: str = 'font5x8.bin', size: int = 1)→ None
    display.text('hello world', 25, 10, Adafruit_EPD.BLACK)
    display.text('Akeu Coucou', 25, 40, Adafruit_EPD.BLACK, font=medium_font)  # , size=2)

    # A circle (test)
    center_x: int = 66
    center_y: int = 66
    radius: int = 50
    for alpha in range(0, 360):  # 0: center top
        x: int = center_x + math.floor(radius * math.sin(math.radians(alpha)))
        y: int = center_y + math.floor(radius * math.cos(math.radians(alpha)))
        display.pixel(x, y, Adafruit_EPD.BLACK)

display.display()

print("That's it")
