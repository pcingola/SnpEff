package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test Loss of Function prediction
 * 
 * @author pcingola
 */
public class TestCasesCutsomIntervals extends TestCase {

	public static boolean debug = false;
	public static final int NUM_DEL_TEST = 10; // number of random test per transcript

	Config config;
	Random random = new Random(20131220);

	public TestCasesCutsomIntervals() {
		super();
	}

	public void test_01() {
		// Load database
		String[] args = { "-interval", "tests/custom_intervals_01.gff", "testHg3770Chr22", "tests/custom_intervals_01.vcf" };
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		// Run
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Check propper annotations
		VcfEntry ve = vcfEntries.get(0);
		Assert.assertEquals("R02837:N/A", ve.getInfo("custom_intervals_01_type"));
		Assert.assertEquals("TRANSFAC_site", ve.getInfo("custom_intervals_01_source"));
		Assert.assertEquals("R02837", ve.getInfo("custom_intervals_01_siteAcc"));
	}
}
