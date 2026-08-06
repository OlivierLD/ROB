import time
import os
import signal

keep_looping: bool = True


def signal_term_handler(signal, frame):
    global keep_looping
    print('got SIGTERM')
    keep_looping = False
    # sys.exit(0)


def signal_kill_handler(signal, frame):
    global keep_looping
    print('got SIGKILL')
    keep_looping = False
    # sys.exit(0)


signal.signal(signal.SIGTERM, signal_term_handler)
# signal.signal(signal.SIGKILL, signal_kill_handler)

pid: int = os.getpid()  # process id

print("Starting process #{}...".format(pid))
print("To stop, do a Ctrl-C, or from a terminal, a kill -9 (or -15) {}".format(pid))


x = 1
while keep_looping:
    try:
        print(x)
        time.sleep(1)
        x += 1
    except KeyboardInterrupt:
        print("\nOops")
        keep_looping = False
        # sys.exit()

print("Outside the loop.")
print("Bye-bye!")
