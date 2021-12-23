/*
 * Created on Nov 24, 2003
 */
package org.snpeff.util;

import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Logging
 */
public class Log {

    public static final int MAX_WARNINGS = 25; // Report a warnings no more than X times
    public static final int MAX_ERRORS = 10; // Report an error no more than X times
    protected static Map<ErrorWarningType, Integer> warnCount = new HashMap<>(); // Count warnings
    private static Timer timer = new Timer(); // Keep track of time (since first class instantiation)
    private static FatalErrorBehabiour fatalErrorBehabiour = FatalErrorBehabiour.EXIT; // How do we exit in case of a fatal error (for testing, we use 'EXCEPTION')
    private static Set<ErrorWarningType> silenceWarning = new HashSet<>();

    /**
     * Prints a debug message (prints class name, method and line number)
     */
    public static void debug(Object obj) {
        debug(obj, 1, true);
    }

    /**
     * Prints a debug message (prints class name, method and line number)
     */
    public static void debug(Object obj, int offset) {
        debug(obj, offset, true);
    }

    /**
     * Prints a debug message (prints class name, method and line number)
     */
    public static void debug(Object obj, int offset, boolean newLine) {
        StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
        String steStr = ste.getClassName();
        int ind = steStr.lastIndexOf('.');
        steStr = steStr.substring(ind + 1);
        steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "):\t" + (obj == null ? null : obj.toString());
        if (newLine) System.err.println(steStr);
        else System.err.print(steStr);
    }

    /**
     * Show a warning message (up to MAX_ERRORS times)
     */
    public static void error(String msg) {
        System.err.println("ERROR: " + msg);
    }

    public static void error(Throwable e, String message) {
        if (Config.get().isVerbose() && (e != null)) e.printStackTrace();
        System.err.println("ERROR: " + message);
    }

    /**
     * Show an error message and exit
     */
    public static void fatalError(String message) {
        fatalError(null, message);
    }

    public static void fatalError(Throwable e, String message) {
        if (fatalErrorBehabiour != FatalErrorBehabiour.EXCEPTION_QUIET) {
            System.err.println("FATAL ERROR: " + message);
            if (e != null) e.printStackTrace();
        }

        switch (fatalErrorBehabiour) {
            case EXIT:
                System.exit(-1);
                break;

            case EXCEPTION:
            case EXCEPTION_QUIET:
                throw new RuntimeException(message, e);

            default:
                System.err.println("WARNINGN: Unknown fatalErrorBehabiour '" + fatalErrorBehabiour + "'");
                System.exit(-1);
                break;
        }
    }

    public static Map<ErrorWarningType, Integer> getWarnCount() {
        return warnCount;
    }

    /**
     * Show absolute timer value and a message on STDERR
     */
    public static void info(Object msg) {
        System.err.println(timer + " " + (msg == null ? "null" : msg.toString()));
    }

    /**
     * Show absolute timer value and a message on STDERR (prepend a newline)
     */
    public static void infoln(Object msg) {
        System.err.println("\n" + timer + " " + (msg == null ? "null" : msg.toString()));
    }

    /**
     * Reset all parameters, warning counters, silenced warningis, etc.
     */
    public static void reset() {
        warnCount = new HashMap<>(); // Count warningns
        timer = new Timer(); // Keep track of time (since first class instantiation)
        fatalErrorBehabiour = FatalErrorBehabiour.EXIT; // How do we exit in case of a fatal error (for testing, we use 'EXCEPTION')
        silenceWarning = new HashSet<>();
    }

    public static void setFatalErrorBehabiour(FatalErrorBehabiour fatalErrorBehabiour) {
        Log.fatalErrorBehabiour = fatalErrorBehabiour;
    }

    public static void silenceWarning(ErrorWarningType warningType) {
        silenceWarning.add(warningType);
    }

    public static void warning(String msg) {
        System.err.println("\nWARNING: " + msg);
    }

    public static void warningln(String msg) {
        System.err.println("WARNING: " + msg);
    }

    /**
     * Show a warning message (up to MAX_ERRORS times)
     */
    public static void warning(ErrorWarningType warnType, String msg) {
        if (warnType != null) {
            if (!warnCount.containsKey(warnType)) warnCount.put(warnType, 0);

            int count = warnCount.get(warnType);
            warnCount.put(warnType, count + 1);

            if (silenceWarning.contains(warnType)) {
                // Ignore this warning
            } else if (count < MAX_WARNINGS) {
                System.err.println(warnType + ": " + msg);
            } else if (count == MAX_WARNINGS) {
                System.err.println(warnType + ": " + msg);
                System.err.println(warnType + ": Too many '" + warnType + "' warnings, no further warnings will be shown.");
            }
        } else {
            System.err.println("WARNING: " + msg);
        }
    }

    public enum FatalErrorBehabiour {
        EXIT // Use System.exit()
        , EXCEPTION // Throw an exception
        , EXCEPTION_QUIET // Silence the output and throw an exception (used in test cases)
    }

}
