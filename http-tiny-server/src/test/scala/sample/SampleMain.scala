package sample

import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}

object SampleMain {

  private val SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'")
  SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"))

  def main(args: Array[String]): Unit = {
    val now = args.exists(arg => arg.equals("--now"))
    val date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")) // Now
    if (!now) { // Hard coded date
      date.set(Calendar.YEAR, 2020)
      date.set(Calendar.MONTH, Calendar.MARCH)
      date.set(Calendar.DAY_OF_MONTH, 28)
      date.set(Calendar.HOUR_OF_DAY, 16) // and not just Calendar.HOUR !!!!
      date.set(Calendar.MINUTE, 50)
      date.set(Calendar.SECOND, 20)
      date.set(Calendar.MILLISECOND, 0)
    }
    println(String.format("Calculations for %s (%s)", SDF_UTC.format(date.getTime), if (now) "now" else "not now"))
    // Take time here, JVM is loaded, etc...
    // val before = System.currentTimeMillis

    println("Done with Scala!")
  }
}