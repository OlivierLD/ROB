#
import time
import board
from digitalio import DigitalInOut, Direction, Pull

# D20 is pin #38
btn: DigitalInOut = DigitalInOut(board.D20)  # DigitalInOut(board.SWITCH)
# print(f"Button is a {type(btn)}")
btn.direction = Direction.INPUT
btn.pull = Pull.UP

print("Press button connected on GPIO-20")

prev_state: bool = btn.value
# print(f"Button State is a {type(prev_state)}")

keep_looping: bool = True
while keep_looping:
    try:
        cur_state: bool = btn.value
        if cur_state != prev_state:
            if not cur_state:
                print("BTN is down")  # Broadcast wherever needed
            else:
                print("BTN is up")    # Broadcast wherever needed
        prev_state = cur_state

        time.sleep(0.1) # sleep for debounce
    except KeyboardInterrupt as oops:
        print("\nUser interrupted")
        keep_looping = False

print("Bye!")
