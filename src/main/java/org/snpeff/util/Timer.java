package org.snpeff.util;

import java.util.Date;

public class Timer {

	private static Timer timer = new Timer(); // Keep track of time (since first class instantiation)

	boolean useMiliSecs = false;
	Date start;
	Date end;

	/**
	 * Show absolute timer value and a message
	 * @param msg
	 */
	public static void show(Object msg) {
		System.out.println(timer + "\t" + (msg == null ? "null" : msg.toString()));
	}

	/**
	 * Show absolute timer value and a message on STDERR
	 * @param msg
	 */
	public static void showStdErr(Object msg) {
		System.err.println(timer + "\t" + (msg == null ? "null" : msg.toString()));
	}

	public static String toString(long elapsedMs, boolean useMiliSecs) {
		long delta = elapsedMs;
		long days = delta / (24 * 60 * 60 * 1000);
		long hours = (delta % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
		long mins = (delta % (60 * 60 * 1000)) / (60 * 1000);
		long secs = (delta % (60 * 1000)) / (1000);
		long ms = (delta % 1000);

		if (days > 0) {
			if (useMiliSecs) return String.format("%d days %02d:%02d:%02d.%03d", days, hours, mins, secs, ms);
			return String.format("%d days %02d:%02d:%02d", days, hours, mins, secs);
		}

		if (useMiliSecs) return String.format("%02d:%02d:%02d.%03d", hours, mins, secs, ms);
		return String.format("%02d:%02d:%02d", hours, mins, secs);
	}

	public Timer() {
		start = new Date();
	}

	/**
	 * Elapsed time in milliseconds
	 */
	public long elapsed() {
		if (end != null) return end.getTime() - start.getTime();
		Date now = new Date();
		return now.getTime() - start.getTime();
	}

	public void end() {
		end = new Date();
	}

	public void setUseMiliSecs(boolean useMiliSecs) {
		this.useMiliSecs = useMiliSecs;
	}

	public void start() {
		start = new Date();
		end = null;
	}

	@Override
	public String toString() {
		return toString(elapsed(), useMiliSecs);
	}

}
