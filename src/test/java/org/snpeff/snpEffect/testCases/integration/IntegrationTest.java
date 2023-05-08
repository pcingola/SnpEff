package org.snpeff.snpEffect.testCases.integration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.snpeff.snpEffect.commandLine.CommandLine;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Base class for integration tests
 *
 * @author pcingola
 */
public class IntegrationTest extends TestCasesIntegrationBase {

	public static final int MAX_LINES_DIFF = 20;
	public final int BUFFER_SIZE = 10 * 1024 * 1024;

	public IntegrationTest() {
		super();
	}

	/**
	 * Run a 'command' and return everything printed to stdout
	 */
	public String command(CommandLine command) {
		PrintStream oldOut = System.out;
		String standardOutput = "";
		ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
		try {
			// Capture STDOUT
			System.setOut(new PrintStream(output));

			// Run command
			command.run();

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Get output
			standardOutput = output.toString();

			// Restore old output
			System.setOut(oldOut);
		}

		return standardOutput;
	}

	/**
	 * Run a command and compare to an expected output (form a file)
	 */
	public void command(CommandLine command, String expectedOutputFile) {
		if (verbose) Log.info("Executing command '" + showCommand(command) + "'");
		String actualOutput = command(command);
		actualOutput = removeVcfHeader(actualOutput);

		if (verbose) Log.info("Reading results file '" + expectedOutputFile + "'");
		String expectedOutput = Gpr.readFile(expectedOutputFile);
		expectedOutput = removeVcfHeader(expectedOutput);

		// Count lines
		int expectedOutputCountLines = expectedOutput.split("\n").length;
		int actualOutputCountLines = actualOutput.split("\n").length;

		// Show differences (if any)
		if (verbose) Log.info("Comparing outputs\t\tExpected size: " + expectedOutput.length() + " (" + expectedOutputCountLines + " lines)\t\tActual size: " + actualOutput.length() + " (" + actualOutputCountLines + " lines)");
		if (!expectedOutput.equals(actualOutput)) {
			String msg = "Outputs differ!\n\tFile    : '" + expectedOutputFile + "'\n\tCommand : '" + showCommand(command) + "'";
			System.err.println(msg);
			System.err.println(showDiff(expectedOutput, actualOutput));
			throw new RuntimeException(msg);
		}
	}

	String removeVcfHeader(String lines) {
		StringBuilder sb = new StringBuilder();

		for (String l : lines.split("\n"))
			if (!l.startsWith("#")) sb.append(l + "\n");

		return sb.toString();

	}

	/**
	 * Show command (as a command line)
	 */
	public String showCommand(CommandLine command) {
		StringBuilder cmd = new StringBuilder();
		cmd.append(command.getClass().getSimpleName() + " ");
		for (String arg : command.getArgs())
			cmd.append(arg + " ");
		return cmd.toString();
	}

	/**
	 * Show the difference between who string (multi-line strings)
	 */
	public String showDiff(String s1, String s2) {
		StringBuilder diff = new StringBuilder();
		String lines1[] = s1.split("\n");
		String lines2[] = s2.split("\n");

		diff.append("Number of lines: " + lines1.length + " vs " + lines2.length + "\n");

		int min = Math.min(lines1.length, lines2.length);
		int countLinesDiff = 0;
		for (int i = 0; i < min; i++) {
			if (!lines1[i].equals(lines2[i])) {
				countLinesDiff++;

				// Show difference between lines
				diff.append(String.format("%10d\t|%s|\n", (i + 1), lines1[i]));
				diff.append(String.format("          \t|%s|\n", lines2[i]));
				diff.append(String.format("          \t|%s|\n\n", showDiffLine(lines1[i], lines2[i])));
			}

			if (countLinesDiff > MAX_LINES_DIFF) break;
		}

		return diff.toString();
	}

	/**
	 * Show difference between two lines
	 */
	public String showDiffLine(String l1, String l2) {
		int max = Math.max(l1.length(), l2.length());
		char d[] = new char[max];

		for (int i = 0; i < max; i++) {
			if ((i >= l1.length()) || (i >= l2.length())) d[i] = '|';
			else if (l1.charAt(i) != l2.charAt(i)) d[i] = '|';
			else {
				if (l1.charAt(i) == '\t') d[i] = '\t';
				else d[i] = ' ';
			}
		}

		return new String(d);
	}
}
