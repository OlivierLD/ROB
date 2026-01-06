package utils;

/**
 * Suitable for bash shell.
 * See https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797#color-codes
 *
 * Color Name	Foreground Color Code	Background Color Code
 * Black        30	                       40
 * Red	        31	                       41
 * Green	    32	                       42
 * Yellow	    33	                       43
 * Blue	        34	                       44
 * Magenta	    35	                       45
 * Cyan	        36	                       46
 * White	    37	                       47
 * Default	    39	                       49
 *
 * Bright Black	    90	                   100
 * Bright Red	    91	                   101
 * Bright Green     92	                   102
 * Bright Yellow    93	                   103
 * Bright Blue	    94	                   104
 * Bright Magenta   95	                   105
 * Bright Cyan	    96	                   106
 * Bright White	    97	                   107
 */
public class EscapeCodes {
    public final static String GREEN = "\033[92m";                   // Green
    public final static String RED = "\033[91m";                     // Red
    public final static String YELLOW = "\033[93m";                  // Yellow
    public final static String BLUE = "\033[94m";                    // Blue
    public final static String WHITE = "\033[97m";                   // White
    public final static String BOLD_RED = "\033[0;31;1m";            // Red and Bold
    public final static String BOLD_GREEN_BLINK = "\033[0;32;1;5m";  // Green, bold, blink.
    public final static String BOLD_RED_BLINK = "\033[0;31;1;5m";    // Red, bold, blink.
    public final static String NC = "\033[0m";                       // Back to No Color

}