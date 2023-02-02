#
# Warning!
# The button PINs are in this case connected on the 3.3V !! And not on the ground.
# That seems to work, states are inverted, but they react as anticipated.
#
import time
import board
from digitalio import DigitalInOut, Direction, Pull

# D20 is pin #
btn = DigitalInOut(board.D20)  # DigitalInOut(board.SWITCH)
btn.direction = Direction.OUTPUT
# btn.pull = Pull.DOWN

print("Press button connected on GPIO-20")

prev_state: bool = btn.value

keep_looping: bool = True
while keep_looping:
    try:
        cur_state: bool = btn.value
        if cur_state != prev_state:
            if not cur_state:
                print("BTN is UP")  # Broadcast wherever needed
            else:
                print("BTN is DOWN")    # Broadcast wherever needed
        prev_state = cur_state

        time.sleep(0.1)  # sleep for debounce
    except KeyboardInterrupt as oops:
        print("\nUser interrupted")
        keep_looping = False

print("Bye!")
