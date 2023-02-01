
import time
import board
from digitalio import DigitalInOut, Direction, Pull

# D20 is pin #
btn = DigitalInOut(board.D20)  # DigitalInOut(board.SWITCH)
btn.direction = Direction.INPUT
btn.pull = Pull.UP

while True:
    if not btn.value:
        print("BTN is down")
    else:
        #print("BTN is up")
        pass

    time.sleep(0.1) # sleep for debounce

