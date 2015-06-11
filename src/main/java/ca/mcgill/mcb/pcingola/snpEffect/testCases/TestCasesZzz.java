package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", "./tests/huge_deletion.vcf.gz" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());
			Assert.assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));
		}
	}

}
