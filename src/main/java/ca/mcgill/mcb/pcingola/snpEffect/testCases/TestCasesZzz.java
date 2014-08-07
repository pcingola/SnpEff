package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

/**
 *
 * Test case
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	Config config;
	Genome genome;

	public void test_01_EmptyVcf() {
		String args[] = { "eff", "-noLog", "testHg3770Chr22", "tests/empty_only_header.vcf" };
		SnpEff snpEff = new SnpEff(args);
		boolean ok = snpEff.run();
		Assert.assertTrue(ok);
	}

}
