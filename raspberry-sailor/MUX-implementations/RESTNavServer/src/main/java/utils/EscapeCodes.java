package utils;

/**
 * Suitable for bash shell
 */
public class EscapeCodes {
    public final static String BOLD_RED = "\033[0;31;1m";            // Red and Bold
    public final static String GREEN = "\033[92m";                   // Green
    public final static String RED = "\033[91m";                     // Red
    public final static String BOLD_GREEN_BLINK = "\033[0;32;1;5m";  // Green, bold, blink.
    public final static String BOLD_RED_BLINK = "\033[0;31;1;5m";    // Red, bold, blink.
    public final static String NC = "\033[0m";                       // Back to No Color

}