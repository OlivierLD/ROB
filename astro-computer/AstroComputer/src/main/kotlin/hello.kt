/**
 * Coming soon...
 * See http://kotlinlang.org/docs/reference/control-flow.html#when-expression
 *
 * From /src/main/kotlin
 * - Compile:
 * kotlinc hello.kt
 * - Run
 * kotlin [-cp ../../../build/classes/kotlin/main/] HelloKt FR Anakin
 */

fun main(args: Array<String>) {
    val language = if (args.isEmpty()) "EN" else args[0]
    val name     = if (args.size < 2) "" else (" " + args[1])
    println(when (language) {
        "EN" -> "Hello${name}!"
        "FR" -> "Salut${name}!"
        "ES" -> "\u00A1Hola${name}!"
        "IT" -> "Ciao${name}!"
        else -> "Sorry${name}, I can't greet you in $language yet"
    })
}
