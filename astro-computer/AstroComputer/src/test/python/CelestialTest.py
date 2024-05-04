#!/usr/bin/env python3

#
# This is a sample.
# More tests could be implemented.
#

import unittest

import os, sys, time
from datetime import datetime, timezone

# First, set the path
current_path = os.path.dirname(os.path.abspath(__file__))
print(f"Absolute current path ${current_path}")
sys.path.append(current_path)
sys.path.append(current_path + "/../../main/python/src/celestial_almanac")

print(f"SysPath: ${sys.path}")

# Now we can import thew required modules.
from long_term_almanac import LongTermAlmanac as lta

DELTA_T: float = 69.2201  # Default, overriden below.
deltaT: float = DELTA_T

# print("Default deltaT would be {} s.".format(deltaT))

# UTC, to be used for calculation
year: int = 2020
month: int = 3
day: int = 28
hours: int = 16
minutes: int = 50
seconds: int = 20


class MyTestCase(unittest.TestCase):
    def test_celest(self):
        before: int = int(round(time.time() * 1000))
        # default 2020-MAR-28 16:50:20 UTC
        lta.calculate(year, month, day, hours, minutes, seconds, deltaT)
        after: int = int(round(time.time() * 1000))
        # Display results
        print("----------------------------------------------")
        print(f"Calculations done for {year}-{month:02d}-{day:02d} {hours:02d}:{minutes:02d}:{seconds:02d} UTC")
        print("In {} ms".format(after - before))
        print("----------------------------------------------")

        print("Sideral Time: {}".format(lta.SidTm))

        # Sun
        fmtGHASun: str = lta.outHA(lta.GHASun)
        fmtRASun: str = lta.outRA(lta.RASun)
        fmtDECSun: str = lta.outDec(lta.DECSun)
        fmtSDSun: str = lta.outSdHp(lta.SDSun)
        fmtHPSun: str = lta.outSdHp(lta.HPSun)

        print("Sun: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHASun, fmtRASun, fmtDECSun, fmtSDSun, fmtHPSun))

        # Venus
        fmtGHAVenus: str = lta.outHA(lta.GHAVenus)
        fmtRAVenus: str = lta.outRA(lta.RAVenus)
        fmtDECVenus: str = lta.outDec(lta.DECVenus)
        fmtSDVenus: str = lta.outSdHp(lta.SDVenus)
        fmtHPVenus: str = lta.outSdHp(lta.HPVenus)

        print("Venus: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAVenus, fmtRAVenus, fmtDECVenus, fmtSDVenus, fmtHPVenus))

        # Mars
        fmtGHAMars: str = lta.outHA(lta.GHAMars)
        fmtRAMars: str = lta.outRA(lta.RAMars)
        fmtDECMars: str = lta.outDec(lta.DECMars)
        fmtSDMars: str = lta.outSdHp(lta.SDMars)
        fmtHPMars: str = lta.outSdHp(lta.HPMars)

        print("Mars: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAMars, fmtRAMars, fmtDECMars, fmtSDMars, fmtHPMars))

        # Jupiter
        fmtGHAJupiter: str = lta.outHA(lta.GHAJupiter)
        fmtRAJupiter: str = lta.outRA(lta.RAJupiter)
        fmtDECJupiter: str = lta.outDec(lta.DECJupiter)
        fmtSDJupiter: str = lta.outSdHp(lta.SDJupiter)
        fmtHPJupiter: str = lta.outSdHp(lta.HPJupiter)

        print("Jupiter: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAJupiter, fmtRAJupiter, fmtDECJupiter, fmtSDJupiter, fmtHPJupiter))

        # Saturn
        fmtGHASaturn: str = lta.outHA(lta.GHASaturn)
        fmtRASaturn: str = lta.outRA(lta.RASaturn)
        fmtDECSaturn: str = lta.outDec(lta.DECSaturn)
        fmtSDSaturn: str = lta.outSdHp(lta.SDSaturn)
        fmtHPSaturn: str = lta.outSdHp(lta.HPSaturn)

        print("Saturn: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHASaturn, fmtRASaturn, fmtDECSaturn, fmtSDSaturn, fmtHPSaturn))

        # Moon
        fmtGHAMoon: str = lta.outHA(lta.GHAMoon)
        fmtRAMoon: str = lta.outRA(lta.RAMoon)
        fmtDECMoon: str = lta.outDec(lta.DECMoon)
        fmtSDMoon: str = lta.outSdHp(lta.SDMoon)
        fmtHPMoon: str = lta.outSdHp(lta.HPMoon)

        print("Moon: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAMoon, fmtRAMoon, fmtDECMoon, fmtSDMoon, fmtHPMoon))
        print("\tMoon phase {} -> {}".format(lta.moonPhaseAngle, lta.moonPhase))

        # Polaris
        fmtGHAPolaris: str = lta.outHA(lta.GHAPol)
        fmtRAPolaris: str = lta.outRA(lta.RAPol)
        fmtDECPolaris: str = lta.outDec(lta.DECPol)

        print("Polaris: GHA {}, RA {}, DEC {}".format(fmtGHAPolaris, fmtRAPolaris, fmtDECPolaris))

        # Obliquity of Ecliptic
        OoE: str = lta.outECL(lta.eps0)
        tOoE: str = lta.outECL(lta.eps)

        print("Ecliptic: obliquity {}, true {}".format(OoE, tOoE))

        # Equation of time
        fmtEoT: str = lta.outEoT(lta.EoT)
        print("Equation of time {} ({} m)".format(fmtEoT, lta.EoT))
        tPass: float = (12.0 * 60) - lta.EoT  # In minutes. TODO Check the EoT sign...
        tPass /= 60.0  # In hours
        tPassHours: int = int(tPass)
        tPassMinutes: int = int((tPass*60) % 60)
        tPassSeconds: float = (tPass*3600) % 60
        print("TPass Sun: {}h {}m {:.2f}s".format(tPassHours, tPassMinutes, tPassSeconds))


        # Lunar Distance of Sun
        fmtLDist: str = lta.outHA(lta.LDist)
        print("Lunar Distance: {}".format(fmtLDist))

        print("Day of Week: {}".format(lta.DoW))
        print("Done with Python!")

        self.assertEqual(lta.DoW, "SAT")


if __name__ == '__main__':
    unittest.main()
