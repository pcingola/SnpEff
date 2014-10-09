package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCase {

	boolean debug = true;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		// Arguments
		ArrayList<String> args = new ArrayList<String>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	/**
	 * Test output order: Canonical first
	 */
	public void test_01_canonical() {
		Gpr.debug("Test");
		List<VcfEntry> vcfEntries = snpEffect("testHg3775Chr8", "tests/eff_sort_canon.vcf", null);

		// Only one entry in this file
		Assert.assertEquals(1, vcfEntries.size());

		VcfEntry ve = vcfEntries.get(0);
		VcfEffect veff = ve.parseEffects().get(0);

		Assert.assertEquals("ENST00000456015", veff.getTranscriptId());
	}

}
