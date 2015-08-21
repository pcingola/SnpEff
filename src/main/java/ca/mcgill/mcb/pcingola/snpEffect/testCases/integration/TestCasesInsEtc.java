package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesInsEtc {

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	/**
	 * Insertion on minus strand
	 */
	@Test
	public void test_02_InsOffByOne() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testENST00000268124", "tests/ins_off_by_one.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		for (VcfEntry ve : vcfEnties) {

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.getVcfEffects();
			VcfEffect veff = veffs.get(0);

			Assert.assertEquals("Q53QQ", veff.getAa());
		}
	}

}
