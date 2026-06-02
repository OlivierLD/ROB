import time

tm1 = 0.25 # short press is less than 0.25s
tm2 = 0.5 # time to wait after press ended before deciding it's end of sequence
gpio_state = False

before_last_on_off = 0.0
last_off_on = 10.0
last_on_off = 20.0

actioned = True

while True:
  pin = GPIO.input(X) # however you get input, much better with callback function on change
  if not (pin == gpio_state):
    now = time.time()
    if pin and not gpio_state: # off to on change
      last_off_on = now
    else: # must be on to off change
      before_last_on_off = last_on_off
      last_on_off = now
    gpio_state = pin
    actioned = False
  if (now - last_on_off) > tm2 and not actioned:
    if (before_last_on_off - last_off_on) < tm2:
      # do double press actions
      print("Double-click")
    elif (last_on_off - last_off_on) < tm1:
      # do short press actions
      print("Short-click")
    elif (last_on_off - last_off_on) > tm1: # probably could be else
      # do long press actions
      print("Long-click")
    actioned = True
  time.sleep(0.01)
