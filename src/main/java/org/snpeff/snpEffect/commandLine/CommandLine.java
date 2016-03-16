package org.snpeff.snpEffect.commandLine;

/**
 * Command line and arguments
 * 
 * The way to run a command from 'main' is usually:
 * 
 * 	public static void main(String[] args) {
 *		Command cmd = new Command();
 *		cmd.parseArgs(args);
 *		cmd.run();
 *	}
 * 
 * @author pcingola
 */
public interface CommandLine {

	public String[] getArgs();

	/**
	 * Parse command line arguments
	 */
	public void parseArgs(String[] args);

	/**
	 * Run the command
	 */
	public boolean run();

	/**
	 * Show 'usage' message and exit with an error code '-1'
	 */
	public void usage(String message);
}
