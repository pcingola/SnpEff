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
public class TestCasesIntegrationMnp {

	static boolean debug = false;
	static boolean verbose = false || debug;

	@Test
	public void test_02() {
		Gpr.debug("Test");
		// Run
		String args[] = { "-classic", "-ud", "0", "testHg3766Chr1", "./tests/test.mnp.01.vcf" };
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setDebug(debug);
		List<VcfEntry> results = snpeff.run(true);

		// Check
		Assert.assertEquals(1, results.size());
		VcfEntry result = results.get(0);

		for (VcfEffect eff : result.getVcfEffects()) {
			String aa = eff.getAa();
			String aaNumStr = aa.substring(1, aa.length() - 1);
			int aanum = Gpr.parseIntSafe(aaNumStr);
			if (verbose) System.out.println("AA: '" + eff.getAa() + "'\tAA Num Str: '" + aaNumStr + "'\teff: " + eff);

			if (aanum <= 0) throw new RuntimeException("Missing AA number!");
		}
	}

}
