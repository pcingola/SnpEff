package org.snpeff.osCmd;

import java.io.File;
import java.io.OutputStream;

import org.snpeff.util.Log;

/**
 * Launches an 'OS command' (e.g. "ls", "dir")
 *
 * Note: Launching a system command in Java is not trivial, we need to start 2 threads that read STDOUT and STDERR of
 * the process, otherwise it will block (actually it may even cause a deadlock)
 *
 * References: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=1
 *
 * @author pcingola
 */
public class ExecuteOsCommand extends Thread implements Progress {

	public static boolean debug = false;

	String commandArgs[]; // Command and arguments
	String error = ""; // Errors
	String pwd = null; // Path to command

	boolean quietStdout = false; // Be quite (i.e. do not copy to stdout )
	boolean quietStderr = false; // Be quite (i.e. do not copy to stderr )
	boolean saveStd = false; // Save lines to buffer
	boolean executing = false, started = false; // Command states
	boolean binaryStdout = false; // Is STDOUT binnary?
	boolean binaryStderr = false; // Is STDERR binnary? (this would be really odd)
	boolean showExceptions = true; // Show exceptions when running the program
	int progress = 0; // Any way to measure progress?
	int exitValue = 0; // Command exit value
	String redirectStdout = null; // Where to redirect STDOUT
	String redirectStderr = null; // Where to redirect STDERR
	Object objetcToNotify = null; // Notify this object when we are done
	OutputStream stdin = null; // We write to command's STDIN (so for us is an output stream)
	StreamGobbler stdErrGobbler = null, stdOutGobbler = null; // Gobblers for command's STDOUT and STDERR
	LineFilter stdOutFilter = null; // Line filter: Keep (and show) everything from STDOUT that matches this filter
	Process pr; // Java process (the one that actually executes our command)

	public ExecuteOsCommand(String args[]) {
		commandArgs = args;
	}

	public ExecuteOsCommand(String command) {
		commandArgs = new String[1];
		commandArgs[0] = command;
	}

	public int exec() {
		try {
			executing = true;
			ProcessBuilder pb = new ProcessBuilder(commandArgs);
			if (pwd != null) pb.directory(new File(pwd));

			if (debug) {
				Log.debug("PWD: " + pwd);
				for (String arg : commandArgs)
					Log.debug("ARGS: " + arg);
			}

			pr = pb.start();

			//---
			// Prepare & start stdout/sdterr reader processes
			//---
			stdErrGobbler = new StreamGobbler(pr.getErrorStream(), true); // StdErr
			stdOutGobbler = new StreamGobbler(pr.getInputStream(), false); // StdOut

			// Quiet? => Do not show
			if (quietStderr) stdErrGobbler.setQuietMode();
			if (quietStdout) stdOutGobbler.setQuietMode();

			// Keep a copy in memory?
			stdErrGobbler.setSaveLinesInMemory(saveStd);
			stdOutGobbler.setSaveLinesInMemory(saveStd);

			// Binary?
			stdErrGobbler.setBinary(binaryStderr);
			stdOutGobbler.setBinary(binaryStdout);

			// Redirect?
			if (redirectStderr != null) stdErrGobbler.setRedirectTo(redirectStderr);
			if (redirectStdout != null) stdOutGobbler.setRedirectTo(redirectStdout);

			// Filter stdout
			stdOutGobbler.setLineFilter(stdOutFilter);

			// Set this object as the progress monitor
			stdErrGobbler.setProgress(this);
			stdOutGobbler.setProgress(this);

			// Start gobblers
			stdErrGobbler.start();
			stdOutGobbler.start();

			// Assign StdIn
			stdin = pr.getOutputStream();

			//---
			// Start process & wait until completion
			//---
			started = true;

			// Wait for the process to finish and store exit value
			exitValue = pr.waitFor();

			// Wait for gobblers to finish processing the remaining of STDIN & STDERR
			while (stdOutGobbler.isRunning() || stdErrGobbler.isRunning()) {
				Thread.sleep(100);
			}

			if (debug && (exitValue != 0)) Log.debug("Exit value: " + exitValue);
		} catch (Exception e) {
			error = e.getMessage();
			exitValue = -1;
			if (showExceptions) e.printStackTrace();
		} finally {
			// We are done. Either process finished or an exception was raised.
			started = true;
			executing = false;
			if (objetcToNotify != null) {
				synchronized (objetcToNotify) {
					objetcToNotify.notify();
				}
			}
		}

		return exitValue;
	}

	public String[] getCommandArgs() {
		return commandArgs;
	}

	public int getExitValue() {
		return exitValue;
	}

	/**
	 * First lines of stdout
	 */
	public String getHead() {
		if (stdOutGobbler != null) return stdOutGobbler.getHead();
		return "";
	}

	/**
	 * First lines of stderr
	 */
	public String getHeadStderr() {
		if (stdErrGobbler != null) return stdErrGobbler.getHead();
		return "";
	}

	@Override
	public int getProgress() {
		return progress;
	}

	public String getPwd() {
		return pwd;
	}

	public String getRedirectStderr() {
		return redirectStderr;
	}

	public String getRedirectStdout() {
		return redirectStdout;
	}

	public String getStderr() {
		return stdErrGobbler == null ? "" : stdErrGobbler.getAllLines();
	}

	public OutputStream getStdin() {
		return stdin;
	}

	public String getStdout() {
		return stdOutGobbler == null ? "" : stdOutGobbler.getAllLines();
	}

	public LineFilter getStdOutFilter() {
		return stdOutFilter;
	}

	public boolean isAlertDone() {
		return stdOutGobbler.isAlertDone();
	}

	public boolean isBinaryStderr() {
		return binaryStderr;
	}

	public boolean isBinaryStdout() {
		return binaryStdout;
	}

	public boolean isExecuting() {
		return executing;
	}

	public boolean isQuiet() {
		return quietStdout;
	}

	public boolean isSaveStd() {
		return saveStd;
	}

	public boolean isStarted() {
		return started;
	}

	public void kill() {
		if (pr != null) pr.destroy();
		if (debug) Log.debug("Process was killed");
	}

	/**
	 * Report progress
	 */
	@Override
	public void progress() {
		progress++;
	}

	public void resetBuffers() {
		stdOutGobbler.resetBuffer();
		stdErrGobbler.resetBuffer();
	}

	@Override
	public void run() {
		if (debug) Log.debug("Running ExecOsCommand thread");
		exec();
	}

	public void setBinaryStderr(boolean binaryStderr) {
		this.binaryStderr = binaryStderr;
	}

	public void setBinaryStdout(boolean binaryStdout) {
		this.binaryStdout = binaryStdout;
	}

	public void setCommandArgs(String[] commandArgs) {
		this.commandArgs = commandArgs;
	}

	public void setObjetcToNotify(Object objetcToNotify) {
		this.objetcToNotify = objetcToNotify;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setQuiet(boolean quietStdout, boolean quietStderr) {
		this.quietStdout = quietStdout;
		this.quietStderr = quietStderr;
	}

	public void setRedirectStderr(String redirectStderr) {
		this.redirectStderr = redirectStderr;
	}

	public void setRedirectStdout(String redirectStdout) {
		this.redirectStdout = redirectStdout;
	}

	public void setSaveStd(boolean saveStd) {
		this.saveStd = saveStd;
	}

	public void setShowExceptions(boolean showExceptions) {
		this.showExceptions = showExceptions;
	}

	public void setStdoutAlert(String alert) {
		stdOutGobbler.setAlert(alert);
	}

	public void setStdoutAlertNotify(Object toBeNotified) {
		stdOutGobbler.setAlertNotify(toBeNotified);
	}

	public void setStdOutFilter(LineFilter stdOutFilter) {
		this.stdOutFilter = stdOutFilter;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String c : commandArgs)
			sb.append(c + " ");
		return sb.toString();
	}
}
