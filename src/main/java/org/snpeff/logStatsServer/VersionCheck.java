package org.snpeff.logStatsServer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.snpeff.util.Log;

/**
 * Check is a new version is available
 */
public class VersionCheck extends Thread {

	// Parameters for LOG thread (a thread that logs information to a server)
	public static final int LOG_THREAD_WAIT_TIME = 1000; // 1 Second
	public static final int LOG_THREAD_WAIT_TIME_REPEAT = 3;
	public static boolean debug = false; // Debug mode?

	String url;
	boolean newVersion = false; // Is there a new version available?
	String software;
	String latestVersion, latestUrl, latestReleaseDate;

	/**
	 * Get version data
	 */
	public static VersionCheck version(String software, String latestVersion, String url, boolean verbose) {
		// Run thread
		VersionCheck versionCheck = new VersionCheck(software, latestVersion, url);
		versionCheck.start();

		// Finish up
		if (verbose) Log.info("Checking for updates...");
		for (int i = 0; i < LOG_THREAD_WAIT_TIME_REPEAT; i++) {
			if (!versionCheck.isAlive()) break;
			try {
				Thread.sleep(LOG_THREAD_WAIT_TIME); // Sleep some time
			} catch (InterruptedException e) {
				; // Nothing to do
			}
		}

		// Interrupt if not done?
		if (versionCheck.isAlive() && !versionCheck.isInterrupted()) versionCheck.interrupt();

		return versionCheck;
	}

	public VersionCheck(String software, String latestVersion, String url) {
		this.software = software.toUpperCase();
		this.latestVersion = latestVersion.toUpperCase();
		this.url = url;
	}

	/**
	 * Get page as string
	 */
	protected String getData() throws Exception {
		if ((url == null) || url.isEmpty()) return "";

		StringBuffer text = new StringBuffer();
		URL page = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) page.openConnection();
		conn.connect();
		InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());

		BufferedReader buff = new BufferedReader(in);
		String line = buff.readLine();
		while (line != null) {
			text.append(line + "\n");
			line = buff.readLine();
		}

		if (debug) Log.debug("Downloaded data:\n" + text.toString());
		return text.toString();
	}

	public String getLatestReleaseDate() {
		return latestReleaseDate;
	}

	public String getLatestUrl() {
		return latestUrl;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public boolean isNewVersion() {
		return newVersion;
	}

	/**
	 * Parse versions page
	 * Format: One entry per line
	 * Line format: "softwareName \t version \t date \t URL \n"
	 */
	protected void parse(String text) {
		String lines[] = text.split("\n");
		for (String line : lines) {
			if (debug) Log.debug("Parse line: " + line);
			if (line.startsWith("#")) {
				// Ignore comments
			} else if (line.length() < 1) {
				// Ignore empty lines
			} else {
				String recs[] = line.split("\t");

				if (debug) {
					for (int i = 0; i < recs.length; i++)
						Log.debug("RECS[" + i + "]:" + recs[i]);
				}

				if (recs.length > 3) {
					String softwareName = recs[0].toUpperCase(); // Use upper-case
					String version = recs[1].toUpperCase(); // Use upper-case
					String date = recs[2];
					String url = recs[3];

					if (debug) Log.debug("VERSION CHECK: " + softwareName.equals(software) + "\t" + version + " cmp " + latestVersion + " : " + (version.compareTo(latestVersion) > 0));

					// Update latest
					if (softwareName.toUpperCase().equals(software.toUpperCase()) && version.compareTo(latestVersion) > 0) {
						latestVersion = version;
						latestReleaseDate = date;
						latestUrl = url;
						newVersion = true;
						if (debug) Log.debug("Found new release:\t" + latestVersion + "\t" + latestReleaseDate + "\t" + latestUrl);
					}
				}
			}
		}
	}

	/**
	 * Run thread in background
	 */
	@Override
	public void run() {
		try {
			if (debug) Log.debug("Running thread");
			String page = getData();
			parse(page);
			if (debug) Log.debug("Thread finished");
		} catch (Throwable t) {
			if (debug) t.printStackTrace();; // Do nothing if it fails
		}

	}
}
