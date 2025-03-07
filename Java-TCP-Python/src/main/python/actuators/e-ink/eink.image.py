"""
Image resizing and drawing using the Pillow Library. For the image, check out the
associated Adafruit Learn guide at:
https://learn.adafruit.com/adafruit-eink-display-breakouts/python-code

Written by Melissa LeBlanc-Williams for Adafruit Industries
"""

import digitalio
import busio
import board
from PIL import Image
from adafruit_epd.il0373 import Adafruit_IL0373
from adafruit_epd.il91874 import Adafruit_IL91874  # pylint: disable=unused-import
from adafruit_epd.il0398 import Adafruit_IL0398  # pylint: disable=unused-import
from adafruit_epd.ssd1608 import Adafruit_SSD1608  # pylint: disable=unused-import
from adafruit_epd.ssd1675 import Adafruit_SSD1675  # pylint: disable=unused-import

# create the spi device and pins we will need
spi = busio.SPI(board.SCK, MOSI=board.MOSI, MISO=board.MISO)
ecs = digitalio.DigitalInOut(board.CE0)
dc = digitalio.DigitalInOut(board.D22)
srcs = None
rst = digitalio.DigitalInOut(board.D27)
busy = digitalio.DigitalInOut(board.D17)

# give them all to our driver
# display = Adafruit_SSD1608(200, 200,        # 1.54" HD mono display
display = Adafruit_SSD1675(122, 250,  # 2.13" HD mono display
# display = Adafruit_IL91874(176, 264,        # 2.7" Tri-color display
# display = Adafruit_IL0373(152, 152,         # 1.54" Tri-color display
# display = Adafruit_IL0373(128, 296,         # 2.9" Tri-color display
# display = Adafruit_IL0398(400, 300,         # 4.2" Tri-color display
# display = Adafruit_IL0373(104, 212,         # 2.13" Tri-color display
                           spi,
                           cs_pin=ecs,
                           dc_pin=dc,
                           sramcs_pin=srcs,
                           rst_pin=rst,
                           busy_pin=busy)

# IF YOU HAVE A FLEXIBLE DISPLAY (2.13" or 2.9") uncomment these lines!
# display.set_black_buffer(1, False)
# display.set_color_buffer(1, False)

display.rotation = 1

# image_name: str = "blinka.png"
image_name: str = "pelican.bw.png"
image = Image.open(image_name)

print(f"Screen definition: w:{display.width} x h:{display.height}")
print(f"Image definition: w:{image.width} x h:{image.height}")
# Scale the image to the smaller screen dimension
image_ratio = image.width / image.height
screen_ratio = display.width / display.height

print(f"Image ratio: {image_ratio}")
print(f"Screen ratio: {screen_ratio}")

h_ratio: float = display.height / image.height
w_ratio: float = display.width / image.width
print(f"H ratio: {h_ratio}, W ratio: {w_ratio}")
scale_ratio = min(h_ratio, w_ratio)

# if screen_ratio < image_ratio:
#     scaled_width = image.width * display.height // image.height
#     scaled_height = display.height
# else:
#     scaled_width = display.width
#     scaled_height = image.height * display.width // image.width

scaled_width = round(image.width * scale_ratio)
scaled_height = round(image.height * scale_ratio)

print(f"Scaled Image definition: w:{scaled_width} x h:{scaled_height}")
image = image.resize((scaled_width, scaled_height), Image.BICUBIC)

# Crop and center the image
# x = scaled_width // 2 - display.width // 2
x = scaled_width - display.width // 2
# x = display.width - scaled_width
y = scaled_height // 2 - display.height // 2
print(f"x:{x}, y:{y}")
image = image.crop((x, y, x + display.width, y + display.height))

# adding dithering for monochrome displays
image = image.convert("1").convert("L")
# Display image.
display.image(image)
display.display()
