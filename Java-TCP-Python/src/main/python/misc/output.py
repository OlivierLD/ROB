# Test stdout, in sync, in batch, etc.
#
# python3 output.py                <== OK
# python3 output.py > output.log   <== OK
# python3 output.py > output.log &  <== with print, that one produces no output in output.log. Idem with stdout.
# To get the output in the log file, use
# python3 -u output.py > output.log &
#
# Another solution would be to flush the stdout after each write.
#

import time
# import sys

between_loops: int = 1
loop_num: int = 0

# out = sys.stdout

try:
    while True:
        print(f"...Loop #{loop_num}")
        # out.write(f"...Loop #{loop_num}\n")
        loop_num += 1
        time.sleep(between_loops)
except KeyboardInterrupt:
    keep_looping = False
    print("\n\t\tUser interrupted (server.serve), exiting...")
    time.sleep(between_loops * 2)
    print("\n\t\tOver and out!")

print("Done with REST and Web server.")
