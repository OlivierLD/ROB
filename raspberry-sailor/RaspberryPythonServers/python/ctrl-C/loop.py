import time
import sys
import os

pid: int = os.getpid()  # process id

print("Starting process #{}...".format(pid))
print("To stop, do a Ctrl-C, or from a terminal, a kill -9 {}".format(pid))

x = 1
while True:
    try:
        print(x)
        time.sleep(1)
        x += 1
    except KeyboardInterrupt:
        print("Bye")
        sys.exit()
