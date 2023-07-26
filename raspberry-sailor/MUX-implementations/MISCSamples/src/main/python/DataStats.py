#!/usr/bin/env python3

import json
from typing import Dict, List
from statistics import mean, median

with open('./data/tbi.max-leeway-10.json') as json_file:
    data:List[Dict]  = json.load(json_file)

print(f"Read {len(data)} objects")

rangeBottom: int = 0
rangeTop: int = 714

bsp_list:List[float] = []
for idx in range(rangeBottom, rangeTop):
    obj:Dict = data[idx]
    bsp: float = obj['bsp']
    if bsp > 0.0:
        bsp_list.append(bsp)
    # print(f"Object is a {type(obj)}, bsp {obj['bsp']}")

print(f"BSP => min:{min(bsp_list)} kn, max:{max(bsp_list)} kn, mean:{mean(bsp_list)} kn, median:{median(bsp_list)} kn")

print("Done")
