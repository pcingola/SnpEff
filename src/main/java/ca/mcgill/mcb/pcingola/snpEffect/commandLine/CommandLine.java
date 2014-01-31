package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

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

	/**
	 * Parse command line arguments
	 * @param args
	 */
	public void parseArgs(String[] args);

	/**
	 * Run the command
	 */
	public boolean run();

	/**
	 * Show 'usage' message and exit with an error code '-1'
	 * @param message
	 */
	public void usage(String message);
}
