package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdProtein;

/**
 * Protein translation test case
 * 
 * @author pcingola
 */
public class TestCasesProtein extends TestCase {

	public void test_01() throws IOException {
		String args[] = { "testHg3763ChrY", "./tests/proteins_testHg3763ChrY.txt" };

		SnpEffCmdProtein cmd = new SnpEffCmdProtein();
		cmd.parseArgs(args);
		cmd.run();

		// Check that it is OK
		Assert.assertEquals(0, cmd.getTotalErrors());
		Assert.assertEquals(true, cmd.getTotalOk() >= 167);
	}
}
