package org.snpeff.osCmd;

import java.io.File;

import org.snpeff.util.Gpr;

/**
 * Run an OS command as a thread
 * 
 * @author pcingola
 */
public class OsCmdRunner extends Thread {

	boolean executing = false, started = false;
	int exitValue = 0;
	long defaultWaitTime = 100; // Default time to use in 'wait' calls when initialting the command
	long defaultLoopWaitTime = 1000; // Default time to use in 'wait' calls
	String jobId = "";
	String head = ""; // Output's head
	String headStderr = ""; // Stderr's head
	String error; // Latest error message
	String stdout = "", stderr = "";
	ExecuteOsCommand osCmd = null;

	/**
	* Run an OS command only if the output files does not exists.
	* 
	* 	opts[0] 			: OS Command 
	* 	opts[1] ... opts[N]	: Command line options
	* 	outputFile			: Where the results are stored (if the file exists, the command is NOT run)
	* 	redirect			: If 'redirect=true' then run "command > outputFile" (i.e. redirect STDOUT to 'outputFile'). Output is assumed to be binary.
	* 
	* @param opts
	* @param outputFile
	* @param redirectToOutput
	* @return true if command executed OK or outputFile exists
	*/
	public static boolean runIfNotExists(String[] opts, String outputFile, boolean redirectToOutput) {
		if (Gpr.exists(outputFile)) return true; // File exists? => Don't run
		String id = opts[0];
		OsCmdRunner cmd = new OsCmdRunner(id, opts);
		cmd.getOsCmd().setQuiet(false, false);
		return cmd.runIfNotExists(outputFile, redirectToOutput);
	}

	public OsCmdRunner(String jobId, String osCmdStr[]) {
		super();
		this.jobId = jobId;
		osCmd = new ExecuteOsCommand(osCmdStr);
	}

	/**
	 * Close (kill) command
	 */
	public synchronized void close() {
		if (osCmd != null) {
			osCmd.kill();
			head = osCmd.getHead();
			headStderr = osCmd.getHeadStderr();

			// Note: this only get values if osCmd.setSaveStd(true) is invoked
			stdout = osCmd.getStdout();
			stderr = osCmd.getStderr();
		}
		osCmd = null;
	}

	/**
	 * Stop execution of this thread
	 */
	public synchronized void finish() {
		executing = false; // Set run to false and wake up from 'wait'. See run() method
		notify();
	}

	public long getDefaultWaitTime() {
		return defaultWaitTime;
	}

	public String getError() {
		return error;
	}

	public int getExitValue() {
		return exitValue;
	}

	public String getHead() {
		return head;
	}

	public String getHeadStderr() {
		return headStderr;
	}

	public String getJobId() {
		return jobId;
	}

	public ExecuteOsCommand getOsCmd() {
		return osCmd;
	}

	public int getProgress() {
		return (osCmd == null ? 0 : osCmd.getProgress());
	}

	public String getStderr() {
		if (osCmd != null) return osCmd.getStderr();
		return stderr;
	}

	public String getStdout() {
		if (osCmd != null) return osCmd.getStdout();
		return stdout;
	}

	/**
	 * Has this runner finished?
	 * @return
	 */
	public boolean isDone() {
		return started && !executing; // In order to finish, it has to be started and not running any more
	}

	public boolean isExecuting() {
		if (osCmd == null) return false;
		return executing && osCmd.isExecuting();
	}

	@Override
	public void run() {
		try {
			executing = true; // OK, we are running
			osCmd.start();

			Thread.sleep(defaultWaitTime); // Allow some time for the thread to start

			// Wait for command to start 
			while (!osCmd.isStarted() && isExecuting())
				Thread.sleep(defaultWaitTime);

			// Wait for stdin to became available
			while ((osCmd.getStdin() == null) && isExecuting())
				Thread.sleep(defaultWaitTime);

			// Now the command started executing
			started = true;

			synchronized (this) {
				osCmd.setObjetcToNotify(this); // Notify me when done (i.e. the command finished)
				while (isExecuting())
					wait(defaultLoopWaitTime);
			}

		} catch (Throwable t) {
			error = t.toString();
			t.printStackTrace(); // Something happened? => Stop this thread
		} finally {
			exitValue = osCmd.getExitValue();
			close();
			executing = false;
			started = true;
		}
	}

	/**
	 * Run a command only if 'outputFile' does not exist
	 * @param outputFile
	 * @param redirectToOutput
	 * @return
	 */
	public boolean runIfNotExists(String outputFile, boolean redirectToOutput) {
		// Already done?
		if (Gpr.exists(outputFile)) return true;

		try {
			// Have to redirect?
			if (redirectToOutput) {
				getOsCmd().setBinaryStdout(true);
				getOsCmd().setRedirectStdout(outputFile);
			}

			run();
		} catch (Throwable t) {
			t.printStackTrace();

			// Command failed => Try to delete output file
			if ((outputFile != null) && (!outputFile.isEmpty())) {
				try {
					(new File(outputFile)).delete();
				} catch (Exception e) {
					// Nothing to do
				}
			}

			return false;
		}

		return true;

	}

	public void setDefaultWaitTime(long defaultWaitTime) {
		this.defaultWaitTime = defaultWaitTime;
	}

	public void setSaveStd(boolean save) {
		osCmd.setSaveStd(save);
	}

	@Override
	public String toString() {
		return super.toString() + "\t" + (osCmd != null ? osCmd.toString() : "null");
	}

}
