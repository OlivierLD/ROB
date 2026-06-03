import adafruit_ssd1306
import math

#
# All drawing function have to be followed by
# display.show()
#

# Draw empty rectangle
def fill_rectangle(oled: adafruit_ssd1306.SSD1306_SPI, x: int, y: int, w: int, h: int):
    for i in range(w):
        for j in range(h):
            oled.pixel(x + i, y + j, 1)

#
# x, y : top left
# w : width
# h : height
#
def draw_rectangle(oled: adafruit_ssd1306.SSD1306_SPI, x: int, y: int, w: int, h: int):
    for i in range(w): # Top
        oled.pixel(x + i, y, 1)
    for i in range(w): # Bottom
        oled.pixel(x + i, y + h, 1)

    for j in range(h): # left
        oled.pixel(x, y + j, 1)
    for j in range(h): # right
        oled.pixel(x + w, y + j, 1)

def draw_line(oled: adafruit_ssd1306.SSD1306_SPI, from_x: int, from_y: int, to_x: int, to_y: int) -> None:
    oled.pixel(from_x, from_y, 1)  # first

    # Find interval
    width: int = to_x - from_x
    height: int = to_y - from_y
    the_range: int = max(abs(width), abs(height))
    step_x: float = width / the_range
    step_y: float = height / the_range
    # Let's loop
    # print(f"StepX:{step_x}, StepY:{step_y}")
    for i in range(the_range):
        x: float = from_x + (step_x * i)
        y: float = from_y + (step_y * i)
        oled.pixel(round(x), round(y), 1)
    oled.pixel(to_x, to_y, 1)            # last


def draw_circle(oled: adafruit_ssd1306.SSD1306_SPI, center_x: int, center_y: int, radius: int) -> None:
    for r in range(360):
        x: float = center_x + (radius * math.cos(math.radians(r)))
        y: float = center_y - (radius * math.sin(math.radians(r)))
        oled.pixel(round(x), round(y), 1)