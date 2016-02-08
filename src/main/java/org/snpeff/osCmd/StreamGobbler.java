package org.snpeff.osCmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read the contents of a stream in a separate thread
 * This class is used when executing OS commands in order to read STDOUT / STDERR and prevent process blocking
 * It can alert an AlertListener when a given string is in the stream
 * 
 * @author pcingola
 */
public class StreamGobbler extends Thread {

	public static int HEAD_SIZE = 100;
	public static int BUFFER_SIZE = 100 * 1024;

	InputStream is;
	boolean binary = false; // Is this a binary stream?
	boolean alertDone = false;
	boolean sendToStderr = false;
	boolean sendToStdout = false;
	boolean saveLinesInMemory = false;
	boolean running = false;
	StringBuffer allLines = new StringBuffer();
	String alert = null;
	Object alertNotify = null;
	StringBuffer head = null;
	Progress progress = null;
	String redirectTo = null;
	BufferedReader lineInputReader = null;
	LineFilter lineFilter;

	StreamGobbler(InputStream is, boolean stderr) {
		this.is = is;
		sendToStdout = !stderr;
		sendToStderr = stderr;
		allLines = new StringBuffer();
		head = new StringBuffer();
	}

	/**
	 * CLose streams
	 */
	private void close() {
		try {
			if (lineInputReader != null) lineInputReader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public String getAlert() {
		return alert;
	}

	public Object getAlertNotify() {
		return alertNotify;
	}

	public String getAllLines() {
		return allLines.toString();
	}

	/**
	 * Head: First HEAD_SIZE lines of this output
	 * @return
	 */
	public String getHead() {
		return head.toString();
	}

	public String getRedirectTo() {
		return redirectTo;
	}

	public boolean isAlertDone() {
		return alertDone;
	}

	public boolean isBinary() {
		return binary;
	}

	public boolean isRunning() {
		return running;
	}

	public void resetBuffer() {
		allLines = new StringBuffer();
	}

	@Override
	public void run() {
		if (binary) {
			// Sanity check
			if (lineFilter != null) throw new RuntimeException("Cannot apply line filter to binary output.");
			runBinGobbler();
		} else {
			runLineGobbler();
		}
	}

	/**
	 * Run in 'binary' mode: This is used when the output is non-text and you want to ignore it or redirect it to a file
	 * This mode does not provide 'head', 'alert' or 'save' functionalities 
	 */
	void runBinGobbler() {
		FileOutputStream redirectWriter = null;
		running = true;
		try {
			// Create buffer
			byte buffer[] = new byte[BUFFER_SIZE];
			int num = 0;

			// Create redirect file (if any)
			if (redirectTo != null) redirectWriter = new FileOutputStream(redirectTo);

			// Read input
			while ((num = is.read(buffer)) >= 0) {
				// Redirect
				if (redirectWriter != null) redirectWriter.write(buffer, 0, num);

				// Report progress
				if (progress != null) progress.progress();
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			close();

			// Close redirect
			try {
				if (redirectWriter != null) redirectWriter.close();
			} catch (IOException ioe) {
				running = false;
				throw new RuntimeException(ioe);
			}

			running = false;
		}
	}

	/**
	 * Run in 'line' mode: This is the standard way to execute commands
	 */
	void runLineGobbler() {
		BufferedWriter redirectWriter = null;
		lineInputReader = new BufferedReader(new InputStreamReader(is));
		running = true;
		try {
			// Create redirect file (if any)
			if (redirectTo != null) redirectWriter = new BufferedWriter(new FileWriter(redirectTo));

			String line = null;
			for (int lineNum = 0; (line = lineInputReader.readLine()) != null; lineNum++) {
				// Does this line pass the filter?
				if (lineFilter != null) {
					line = lineFilter.filter(line); // Filter might change line
					if (line == null) continue; // line did not pass the filter
				}

				if (sendToStderr) System.err.println(line);
				if (sendToStdout) System.out.println(line);

				// Redirect
				if (redirectWriter != null) redirectWriter.write(line + "\n");

				// Keep in memory copy?
				if (saveLinesInMemory) allLines.append(line + "\n");
				if (lineNum < HEAD_SIZE) head.append(line + "\n");

				// Report progress
				if (progress != null) progress.progress();

				// Alert to 'alert listener'?
				if ((alert != null) && (line.indexOf(alert) >= 0)) {
					alertDone = true;
					// Do we need to notify? 
					if (alertNotify != null) {
						synchronized (alertNotify) {
							alertNotify.notify();
						}
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			close();

			// Close redirect
			try {
				if (redirectWriter != null) redirectWriter.close();
			} catch (IOException ioe) {
				running = false;
				throw new RuntimeException(ioe);
			}

			running = false;
		}
	}

	public void setAlert(String alert) {
		alertDone = false;
		this.alert = alert;
	}

	public void setAlertNotify(Object alertNotify) {
		this.alertNotify = alertNotify;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public void setLineFilter(LineFilter lineFilter) {
		this.lineFilter = lineFilter;
	}

	public void setProgress(Progress progress) {
		this.progress = progress;
	}

	public void setQuietMode() {
		sendToStderr = false;
		sendToStdout = false;
	}

	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}

	public void setSaveLinesInMemory(boolean saveLinesInMemory) {
		this.saveLinesInMemory = saveLinesInMemory;
	}
}
