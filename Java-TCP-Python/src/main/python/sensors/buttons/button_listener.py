#!/usr/bin/env python3
#
import time
import board
from digitalio import DigitalInOut, Direction, Pull
import threading

VERBOSE: bool = False
keep_looping: bool = True
current_value: int = 0

pin_button_01 = board.D20  # pin #38
pin_button_02 = board.D21  # pin #40


def button_listener(pin, state) -> None:
    global current_value
    global pin_button_01
    global pin_button_02
    if VERBOSE:
        print(f"Yo! {pin}, state {state}")
    if pin == pin_button_01 and state == True:
        current_value += 1
    if pin == pin_button_02 and state == True:
        current_value -= 1
    if state:
        print(f"Current Value is now {current_value}")


def button_manager(pin, callback) -> None:
    global keep_looping
    btn: DigitalInOut = DigitalInOut(pin)
    # print(f"Button is a {type(btn)}")
    btn.direction = Direction.INPUT
    btn.pull = Pull.UP

    prev_state: bool = btn.value
    # print(f"Button State is a {type(prev_state)}")
    while keep_looping:
        try:
            cur_state: bool = btn.value
            if cur_state != prev_state:
                if not cur_state:
                    if VERBOSE:
                        print("BTN is down")
                    callback(pin, True)   # Broadcast wherever needed
                else:
                    if VERBOSE:
                        print("BTN is up")
                    callback(pin, False)  # Broadcast wherever needed
            prev_state = cur_state
            time.sleep(0.1) # sleep for debounce
        except Exception as oops:
            print(f"Error: {repr(oops)}")
    print(f"Done with button listener on pin {pin}")


print("Press button connected on GPIO-20 to add")
button_thread_01: threading.Thread = threading.Thread(target=button_manager, args=(pin_button_01, button_listener))
# print(f"Thead is a {type(button_thread_01)}")
button_thread_01.daemon = True  # Dies on exit
button_thread_01.start()

print("Press button connected on GPIO-21 to subtract")
button_thread_02: threading.Thread = threading.Thread(target=button_manager, args=(pin_button_02, button_listener))
button_thread_02.daemon = True  # Dies on exit
button_thread_02.start()

while keep_looping:
    try:
        # print("Boom")
        time.sleep(0.1)  # sleep for debounce
    except KeyboardInterrupt as oops:
        print("\nUser interrupted")
        keep_looping = False
        button_thread_01.join()
        button_thread_02.join()
        # time.sleep(0.5)

print("Bye!")
