package org.snpeff.logStatsServer;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 *
 * Log basic usage information to a server (for feedback and stats)
 * This information an always be suppressed (no info sent at all)
 *
 */
public class LogStats extends Thread {

	public enum RequestResult {
		OK, ERROR, NOINFO;

		/** A request is "Completed" if the communication worked */
		public boolean completed() {
			return this != ERROR && this != NOINFO;
		}
	}

	// Parameters for LOG thread (a thread that logs information to a server)
	public static final int LOG_THREAD_WAIT_TIME = 1000; // 1 Second
	public static final int LOG_THREAD_WAIT_TIME_REPEAT = 5;
	public static boolean debug = false; // Debug mode?

	// Log server parameters
	private static final String URL_WWW = "http://www.dnaminer.com";
	private static final String URL_ROOT = URL_WWW + "/recuse.php";
	private static final String HTTP_CHARSET = "ISO-8859-1";
	private static final int HTTP_CONNECT_TIMEOUT_MSECS = 22000;
	private static final int HTTP_READ_TIMEOUT_MSECS = 23000;

	// Class variables
	public StringBuilder msg = new StringBuilder(); // info for the user
	private final String versionFull;
	private final String programName;
	private RequestResult res = RequestResult.NOINFO;
	private long duration; // Time to complete the request, in msecs - successful or not
	protected boolean log = true; // Log to server (statistics)
	protected boolean verbose = false; // Be verbose
	HashMap<String, String> values; // Values to report

	/**
	 * Report stats to server
	 * @param versionFull : Program name and version
	 * @param ok : Did the program finished OK?
	 * @param verbose : Be verbose while reporting
	 * @param args : Program's command line arguments
	 * @param errorMessage : Error messages (if any)
	 * @param reportValues : A hash containing <name, value> pairs to report
	 */
	public static LogStats report(String software, String versionShort, String versionFull, boolean ok, boolean verbose, String args[], String errorMessage, HashMap<String, String> reportValues) {
		//---
		// Create logStats & add data
		//---
		LogStats logStats = new LogStats(software, versionShort, versionFull);

		//---
		// Add command line arguments
		//---
		if (args != null) {
			for (int i = 0; i < args.length; i++)
				logStats.add("args_" + i, args[i]);
		}

		//---
		// Add run status info
		//---
		logStats.add("Finished_OK", Boolean.toString(ok));
		if ((errorMessage != null) && !errorMessage.isEmpty()) logStats.add("Error", errorMessage);

		//---
		// Add system info
		//---
		// What kind of systems do users run this program on?
		String properties[] = { "user.name", "os.name", "os.version", "os.arch" };
		for (String prop : properties) {
			try {
				logStats.add(prop, System.getProperty(prop));
			} catch (Exception e) {
				; // Do nothing, just skip the values
			} ;
		}

		// What kind of computers are users using?
		try {
			logStats.add("num.cores", Gpr.NUM_CORES + "");
			logStats.add("total.mem", Runtime.getRuntime().totalMemory() + "");
		} catch (Exception e) {
			; // Do nothing, just skip the values
		} ;

		//---
		// Add custom values
		//---
		for (String name : reportValues.keySet())
			logStats.add(name, reportValues.get(name));

		//---
		// Run thread
		//---
		logStats.start();

		// Finish up
		if (verbose) Log.info("Logging");
		for (int i = 0; i < LOG_THREAD_WAIT_TIME_REPEAT; i++) {
			if (!logStats.isAlive()) break;
			try {
				Thread.sleep(LOG_THREAD_WAIT_TIME); // Sleep some time
			} catch (InterruptedException e) {
				; // Nothing to do
			}
		}

		// Interrupt if not done?
		if (logStats.isAlive() && !logStats.isInterrupted()) logStats.interrupt();

		return logStats;
	}

	/**
	 * Constructor
	 */
	public LogStats(String software, String versionShort, String versionFull) {
		programName = software;
		this.versionFull = versionFull;
		values = new HashMap<String, String>();
	}

	/**
	 * Add a 'name=value' pair
	 */
	public void add(String name, String value) {
		// Replace '\n' char by (literally) a "\n" string. Same for '\t'
		value = value.replaceAll("\\n", "\\\\n");
		value = value.replaceAll("\\t", "\\\\t");
		value = value.replaceAll("\\r", ""); // Remove '\r'

		values.put(name, value); // Now we can store values
	}

	/**
	 * Create URL
	 */
	private URL buildUrl() throws MalformedURLException {
		StringBuilder urlsb = new StringBuilder();
		urlsb.append(URL_ROOT).append("?");

		// Add program and version
		//		urlsb.append("program=").append(encode2url(SnpEff.class.getSimpleName()));
		urlsb.append("program=").append(encode2url(programName));
		urlsb.append("&version=").append(encode2url(versionFull));

		// Add all other 'name=value' pairs in alphabetical order
		ArrayList<String> names = new ArrayList<String>();
		names.addAll(values.keySet());
		Collections.sort(names);
		for (String name : names)
			urlsb.append("&" + name + "=").append(encode2url(values.get(name)));

		if (debug) Log.debug("URL: " + urlsb);
		return new URL(urlsb.toString());
	}

	/** Unencripted */
	private URL buildUrlWrap() {
		try {
			return buildUrl();
		} catch (MalformedURLException e) {
			msg.append(e.getMessage());
			return null;
		}
	}

	/**
	 * Connect to server
	 */
	public void connect() {
		// Step 0 : Build URL
		int step = 0;
		long t0 = System.currentTimeMillis();
		if (debug) Log.debug("Connect Step = " + step);
		try {
			URL url = buildUrlWrap();

			// Step 1: Open connection
			step = 1;
			if (debug) Log.debug("Connect Step = " + step);
			URLConnection httpConnection = url.openConnection();

			// Step 2: Set parameters
			step = 2;
			if (debug) Log.debug("Connect Step = " + step);
			httpConnection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MSECS);
			httpConnection.setReadTimeout(HTTP_READ_TIMEOUT_MSECS);

			// Step 3: Connect to server
			step = 3;
			if (debug) Log.debug("Connect Step = " + step);

			// Step 4: Parse results (nothing done here)
			step = 4;
			String responseStr = Gpr.read(httpConnection.getInputStream());
			if (debug) Log.debug("Server response: " + responseStr);
			// parseResponse(responseStr);

			res = RequestResult.OK;
		} catch (Exception e) {
			if (debug) e.printStackTrace();
			msg.append(step > 3 ? "Bad response" : "Error in connection. ").append(" Step " + step).append("(").append(e.toString()).append(")");
			res = RequestResult.ERROR;
		} finally {
			duration = System.currentTimeMillis() - t0;
			if (debug && !res.completed()) Log.debug("Error in connection: " + res + " step=" + step + " duration(msecs)=" + duration + " " + msg);
		}

		if (debug) Log.debug("Connect done!");
	}

	/**
	 * Encode data to URL format
	 */
	private String encode2url(String data) {
		try {
			return URLEncoder.encode(data, HTTP_CHARSET);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public RequestResult getRes() {
		return res;
	}

	/**
	 * Run thread in background
	 */
	@Override
	public void run() {
		try {
			if (debug) Log.debug("Running thread");
			connect();
			if (debug) Log.debug("Thread finished");
		} catch (Throwable t) {
			if (debug) t.printStackTrace();; // Do nothing if it fails
		}

	}
}
