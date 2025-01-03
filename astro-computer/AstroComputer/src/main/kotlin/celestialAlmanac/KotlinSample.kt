package celestialAlmanac

import calc.GeomUtil
import calc.calculation.AstroComputerV2
import calc.calculation.SightReductionUtil
import utils.StringUtils
import utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

object KotlinSample {
    private val SDF_UTC = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'")

    init {
        SDF_UTC.timeZone = TimeZone.getTimeZone("Etc/UTC")
    }

    /**
     *
     * @param value in seconds of arc
     * @return
     */
    private fun renderSdHp(value: Double): String {
        var formatted = ""
        val minutes = Math.floor(value / 60.0).toInt()
        val seconds = value - minutes * 60
        formatted = if (minutes > 0) {
            String.format("%d'%05.02f\"", minutes, seconds)
        } else {
            String.format("%05.02f\"", seconds)
        }
        return formatted
    }

    private fun renderRA(ra: Double): String {
        var formatted = ""
        val t = ra / 15.0
        val raH = Math.floor(t).toInt()
        val raMin = Math.floor(60 * (t - raH)).toInt()
        val raSec = Math.round(10.0 * (3600.0 * (t - raH - raMin / 60.0))).toFloat() / 10
        formatted = String.format("%02dh %02dm %05.02fs", raH, raMin, raSec)
        return formatted
    }

    /**
     *
     * @param eot in minutes (of time)
     * @return
     */
    private fun renderEoT(eot: Double): String {
        var formatted = ""
        val dEoT = Math.abs(eot)
        val eotMin = Math.floor(dEoT).toInt()
        val eotSec = Math.round(600 * (dEoT - eotMin)) / 10.0
        formatted = if (eotMin == 0) { // Less than 1 minute
            String.format("%s %04.01fs", if (eot > 0) "+" else "-", eotSec)
        } else {
            String.format("%s %02dm %04.01fs", if (eot > 0) "+" else "-", eotMin, eotSec)
        }
        return formatted
    }

    /**
     *
     * @param args use --now to get current data. Otherwise, 2020-Mar-28 16:50:20 UTC will be used.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val now = Arrays.stream(args).filter { arg: String -> "--now" == arg }.findFirst().isPresent
        val date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")) // Now
        if (!now) { // Hard coded date
            date[Calendar.YEAR] = 2020
            date[Calendar.MONTH] = Calendar.MARCH
            date[Calendar.DAY_OF_MONTH] = 28
            date[Calendar.HOUR_OF_DAY] = 16 // and not just Calendar.HOUR !!!!
            date[Calendar.MINUTE] = 50
            date[Calendar.SECOND] = 20
        }
        println(String.format("Calculations for %s (%s)", SDF_UTC.format(date.time), if (now) "now" else "not now"))
        val acv2 = AstroComputerV2()
        val defaultDeltaT = acv2.deltaT  // Default one, not to use.
        System.out.printf("Using deltaT: %f\n", defaultDeltaT)
        for (i in 0..9) { // Any variation across the time?
            val before = System.currentTimeMillis()
            // Recalculate DeltaT
            val deltaT = TimeUtil.getDeltaT(date[Calendar.YEAR], date[Calendar.MONTH] + 1)
            System.out.printf(">> deltaT: %f s\n", deltaT)
            acv2.deltaT = deltaT

            // All calculations here
            acv2.calculate(
                    date[Calendar.YEAR],
                    date[Calendar.MONTH] + 1,  // Jan: 1, Dec: 12.
                    date[Calendar.DAY_OF_MONTH],
                    date[Calendar.HOUR_OF_DAY],  // and not just Calendar.HOUR !!!!
                    date[Calendar.MINUTE],
                    date[Calendar.SECOND])
            val after = System.currentTimeMillis()

            // Done with calculations, now display
            println(String.format(">>> Calculations done for %s, in %d ms <<<", SDF_UTC.format(date.time), after - before))
        }
        println(String.format("Sun data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.sunDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.sunGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.sunRA),
                StringUtils.lpad(renderSdHp(acv2.sunSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.sunHp), 9, " ")))
        println(String.format("Moon data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.moonDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.moonGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.moonRA),
                StringUtils.lpad(renderSdHp(acv2.moonSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.moonHp), 9, " ")))
        println(String.format("\tMoon phase: %s, %s",
                GeomUtil.decToSex(acv2.moonPhase, GeomUtil.SWING, GeomUtil.NONE),
                acv2.moonPhaseStr))
        println(String.format("\tMoon illumination %.04f%%", acv2.moonIllum))
        println(String.format("Venus data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.venusDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.venusGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.venusRA),
                StringUtils.lpad(renderSdHp(acv2.venusSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.venusHp), 9, " ")))
        println(String.format("Mars data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.marsDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.marsGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.marsRA),
                StringUtils.lpad(renderSdHp(acv2.marsSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.marsHp), 9, " ")))
        println(String.format("Jupiter data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.jupiterDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.jupiterGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.jupiterRA),
                StringUtils.lpad(renderSdHp(acv2.jupiterSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.jupiterHp), 9, " ")))
        println(String.format("Saturn data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.saturnDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.saturnGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.saturnRA),
                StringUtils.lpad(renderSdHp(acv2.saturnSd), 9, " "),
                StringUtils.lpad(renderSdHp(acv2.saturnHp), 9, " ")))
        println()
        println(String.format("Polaris data:\tDecl.: %s, GHA: %s, RA: %s",
                StringUtils.lpad(GeomUtil.decToSex(acv2.polarisDecl, GeomUtil.SWING, GeomUtil.NS), 10, " "),
                StringUtils.lpad(GeomUtil.decToSex(acv2.polarisGHA, GeomUtil.SWING, GeomUtil.NONE), 11, " "),
                renderRA(acv2.polarisRA)))
        println(String.format("Equation of time: %s", renderEoT(acv2.eoT)))
        println(String.format("Lunar Distance: %s", StringUtils.lpad(GeomUtil.decToSex(acv2.lDist, GeomUtil.SWING, GeomUtil.NONE), 10, " ")))
        println(String.format("Day of Week: %s", acv2.weekDay))

        // Extra: calculate Sun's apparent position from a given position
        val sru = SightReductionUtil()
        val userLatitude = 47.677667 // Belz
        val userLongitude = -3.135667 // Belz
        sru.calculate(userLatitude, userLongitude, acv2.sunGHA, acv2.sunDecl)
        val estimatedAltitude = sru.he
        val z = sru.z
        System.out.printf("Sun, from %s / %s : Alt: (%f) %s, Z: %f\n",
                GeomUtil.decToSex(userLatitude, GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(userLongitude, GeomUtil.SWING, GeomUtil.EW),
                estimatedAltitude,
                GeomUtil.decToSex(estimatedAltitude, GeomUtil.SWING, GeomUtil.NONE),
                z)
        println("Done with Java test run, from Kotlin!")
    }
}