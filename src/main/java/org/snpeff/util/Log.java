/*
 * Created on Nov 24, 2003
 */
package org.snpeff.util;

import java.util.HashMap;
import java.util.Map;

import org.snpeff.snpEffect.Config;

/**
 * Logging
 */
public class Log {

	public static final int MAX_WARNINGS = 25; // Report a warningi no more than X times
	public static final int MAX_ERRORS = 10; // Report an error no more than X times

	protected static Map<String, Integer> warnCount = new HashMap<>();

	private static Timer timer = new Timer(); // Keep track of time (since first class instantiation)

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
	protected static void error(String msg) {
		System.err.println("Error: " + msg);
	}

	public static void error(Throwable e, String message) {
		if (Config.get().isVerbose() && (e != null)) e.printStackTrace();
		System.err.println("Error: " + message);
	}

	/**
	 * Show an error message and exit
	 */
	public static void fatalError(String message) {
		System.err.println("FATAL ERROR" + message);
		System.exit(-1);
	}

	public static void fatalError(Throwable e, String message) {
		System.err.println("FATAL ERROR" + message);
		e.printStackTrace();
		System.exit(-1);
	}

	/**
	 * Show absolute timer value and a message on STDERR
	 * @param msg
	 */
	public static void info(Object msg) {
		System.err.println(timer + " " + (msg == null ? "null" : msg.toString()));
	}

	public static void warning(String msg) {
		System.err.println("WARNING: " + msg);
	}

	/**
	 * Show a warning message (up to MAX_ERRORS times)
	 */
	public static void warning(String warnType, String msg) {
		if (!warnCount.containsKey(warnType)) warnCount.put(warnType, 0);

		int count = warnCount.get(warnType);
		warnCount.put(warnType, count + 1);

		if (count < MAX_WARNINGS) System.err.println("WARNING: " + msg);
	}

}
