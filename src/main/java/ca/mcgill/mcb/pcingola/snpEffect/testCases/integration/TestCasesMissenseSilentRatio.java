package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Calculate missense over silent ratio
 *
 * @author pcingola
 */
public class TestCasesMissenseSilentRatio extends TestCase {

	boolean verbose = false;

	public TestCasesMissenseSilentRatio() {
		super();
	}

	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-i", "vcf" //
				, "-classic" //
				, "-useLocalTemplate" //
				, "testHg3765Chr22" //
				, "./tests/missenseSilent.chr22.vcf.gz" //
		};

		SnpEff cmd = new SnpEff(args);
		cmd.setVerbose(verbose);
		cmd.setSupressOutput(!verbose);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();

		snpeff.run();

		double silentRatio = snpeff.getChangeEffectResutStats().getSilentRatio();
		if (verbose) System.err.println("Missense / Silent ratio: " + silentRatio);

		Assert.assertEquals(1.19, silentRatio, 0.1);
	}
}
