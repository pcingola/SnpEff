package org.snpeff.osCmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * A queue of commands to be run.
 * They are run in multiple threads (default number of threads = number of CPUs in the computer)
 *
 * @author pcingola
 */
public class OsCmdQueue implements Iterable<OsCmdRunner> {

	public static boolean debug = false;

	ArrayList<OsCmdRunner> commands;
	ArrayList<OsCmdRunner> commandsToRun;
	HashSet<OsCmdRunner> commandsDone, commandsRunning;
	HashMap<OsCmdRunner, String> outputFiles;
	boolean verbose = false;
	boolean throwExceptionOnError = false;
	boolean redirectToOutput = true;
	int numThreads = Gpr.NUM_CORES;
	int sleepTime = 1000; // Default sleep time, 1 sec

	public OsCmdQueue() {
		commands = new ArrayList<OsCmdRunner>();
		outputFiles = new HashMap<OsCmdRunner, String>();
	}

	/**
	 * Add command to be executed
	 * @param cmd
	 */
	public void add(OsCmdRunner cmd) {
		commands.add(cmd);
	}

	/**
	 * Add command to be executed, only if 'outputFile' does not exist
	 * @param cmd
	 * @param outputFile
	 */
	public void add(OsCmdRunner cmd, String outputFile) {
		commands.add(cmd);
		outputFiles.put(cmd, outputFile);
	}

	/**
	 * Wait for commands to finish
	 */
	void doneCommands() {
		if (commandsRunning.isEmpty()) return;

		// Commands to delete
		LinkedList<OsCmdRunner> toDelete = new LinkedList<OsCmdRunner>();

		for (OsCmdRunner cmd : commandsRunning) {
			if (cmd.isDone()) {
				toDelete.add(cmd);

				// Any error executing command?
				if (cmd.getExitValue() > 0) {
					String message = "Error executing command: " //
							+ "Command: " + cmd + "\n"//
							+ "\n---------- STDOUT: Start ----------\n" //
							+ cmd.getStdout() //
							+ "\n---------- STDOUT: End ----------\n" //
							+ "\n---------- STDERR: Start ----------\n" //
							+ cmd.getStderr() //
							+ "\n---------- STDERR: End ----------\n" //
					;

					// Should we throw an exception or just show a message?
					if (throwExceptionOnError) throw new RuntimeException(message);
					else System.err.println(message);
				}
			}
		}

		// Move from running list to done
		for (OsCmdRunner cmd : toDelete) {
			if (verbose) Log.info("Finished command: " + cmd);
			commandsRunning.remove(cmd);
			commandsDone.add(cmd);
		}
	}

	@Override
	public Iterator<OsCmdRunner> iterator() {
		return commands.iterator();
	}

	/**
	 * Kill all commands
	 */
	public void kill() {
		for (OsCmdRunner cmd : commands) {
			Log.debug("Queue failed. Killing command: " + cmd);
			cmd.finish();
		}
	}

	/**
	 * Run commands
	 */
	public void run() {
		if (verbose) Log.info("Starting " + this);

		// Sanity check (has this queue been run before?
		if (commandsDone != null) throw new RuntimeException("Cannot re-run a queue! Please create a new one.");

		try {
			// Create commands list
			commandsToRun = new ArrayList<OsCmdRunner>();
			commandsToRun.addAll(commands);
			commandsDone = new HashSet<OsCmdRunner>();
			commandsRunning = new HashSet<OsCmdRunner>();

			// Are there any more commands to run?
			while (!commandsToRun.isEmpty() || !commandsRunning.isEmpty()) {
				// Can we run one more command?
				if ((commandsRunning.size() < numThreads) && (!commandsToRun.isEmpty())) {
					// Get next command and run it
					OsCmdRunner cmdNext = commandsToRun.remove(0);
					run(cmdNext);
				}

				doneCommands(); // Have commands finished?
				sleep();

				if (debug) Log.debug("Queue processes:\tPending : " + commandsToRun.size() + "\tRunning: " + commandsRunning.size() + "\tDone: " + commandsDone.size());
			}
		} catch (Throwable t) {
			// Kill all commands
			kill();
			throw new RuntimeException("Queue aborted due to exception.", t);
		}
	}

	/**
	 * Run a command
	 */
	void run(OsCmdRunner cmd) {
		// Run only if outFile exists?
		String outputFile = outputFiles.get(cmd);

		if (verbose) Log.info("Running command: '" + cmd + "'");

		if (outputFile == null) {
			// No 'outFile'? => Always run
			commandsRunning.add(cmd); // Add to commands running
			cmd.start();
		} else {
			// Start cmd only if 'outputFile' does not exist
			if (!Gpr.exists(outputFile)) {
				commandsRunning.add(cmd);

				if (redirectToOutput) {
					cmd.getOsCmd().setBinaryStdout(true);
					cmd.getOsCmd().setRedirectStdout(outputFile);
				}

				cmd.start();
			} else commandsDone.add(cmd); // Otherwise, we are already done
		}
	}

	/**
	 * Run some commands
	 */
	void runCommands() {

	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public void setRedirectToOutput(boolean redirectToOutput) {
		this.redirectToOutput = redirectToOutput;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public int size() {
		return commands.size();
	}

	/**
	 * Sleep some time
	 */
	void sleep() {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		int done = 0, executing = 0;
		for (OsCmdRunner cmd : commands) {
			if (cmd.isDone()) done++;
			if (cmd.isExecuting()) executing++;
		}

		sb.append("Queue:\n");
		sb.append("\tSize       : " + commands.size() + "\n");
		sb.append("\tExecuting  : " + executing + "\n");
		sb.append("\tDone       : " + done + "\n");
		sb.append("\tCPUs       : " + numThreads + "\n");
		sb.append("\tSleep time : " + sleepTime + "\n");

		for (OsCmdRunner cmd : commands) {
			String status = "Not started";
			if (cmd.isDone()) status = "Done";
			else if (cmd.isExecuting()) status = "Executing";

			sb.append(String.format("\t[%12s]\t%s\n", status, cmd));
		}

		return sb.toString();
	}
}
