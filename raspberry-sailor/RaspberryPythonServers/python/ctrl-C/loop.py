import time
import sys
import os

pid: int = os.getpid()  # process id

print("Starting process #{}...".format(pid))
print("To stop, do a Ctrl-C, or from a terminal, a kill -9 {}".format(pid))

x = 1
keep_looping: bool = True
while keep_looping:
    try:
        print(x)
        time.sleep(1)
        x += 1
    except KeyboardInterrupt:
        print("\nBye")
        keep_looping = False
        # sys.exit()

print("Outside the loop.")
print("Bye-bye!")
