package memory

import java.text.NumberFormat

object GCTestScala {

  private val BOLD_RED = "\u001b[0;31;1m"            // Red and Bold
  private val GREEN = "\u001b[92m"                   // Green
  private val RED = "\u001b[91m"                     // Red
  private val BOLD_GREEN_BLINK = "\u001b[0;32;1;5m"  // Green, bold, blink.
  private val BOLD_RED_BLINK = "\u001b[0;31;1;5m"    // Red, bold, blink.
  private val NC = "\u001b[0m"                       // Back to No Color

  def runGC(): Unit = {
    val runtime = Runtime.getRuntime
    val memoryMax = runtime.maxMemory()
    println(f"The maximum memory: ${NumberFormat.getInstance.format(memoryMax)} bytes (${NumberFormat.getInstance.format(memoryMax / (1024 * 1024))} Mb, ${NumberFormat.getInstance.format(memoryMax / (1024 * 1024 * 1024))} Gb)")
    val memoryUsed = runtime.totalMemory() - runtime.freeMemory()
    val memoryUsedPercent = (memoryUsed * 100.0) / memoryMax
    // println(f"The memory used by program (in percent): $RED%.02f $memoryUsedPercent%.02f %% $NC")
    println(f"The memory used by program (in percent): $RED $memoryUsedPercent%.02f %% $NC")
    if (memoryUsedPercent > 90.0) {
      System.gc()
    }
    val testString = "this a test"

    println(s"$RED $testString $NC")
    println(s"$GREEN $testString $NC")
    println(s"$BOLD_RED $testString $NC")
    println(s"$BOLD_GREEN_BLINK $testString $NC")
    println(s"$BOLD_RED_BLINK $testString $NC")
  }

  def main(args: Array[String]): Unit = {
    runGC()
  }
}