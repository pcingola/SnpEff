package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case where VCF entries are huge (e.g. half chromosome deleted)
 *
 * @author pcingola
 */
public class TestCasesHugeDeletions {

	boolean verbose = false;

	public TestCasesHugeDeletions() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "-noOut", "testHg3766Chr1", "./tests/huge_deletion_DEL.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getEnd() + "\tsize:" + ve.size());

			boolean ok = false;
			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println(veff);
				ok |= (veff.getEffectType() == EffectType.CHROMOSOME_LARGE_DELETION);
			}

			if (!ok) Assert.assertTrue("Expecting 'CHROMOSOME_LARGE_DELETION', not found", ok);
		}
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
