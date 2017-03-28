package org.snpeff.snpEffect.testCases.integration;

import junit.framework.Assert;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;

/**
 * Calculate missense over silent ratio
 *
 * @author pcingola
 */
public class TestCasesIntegrationMissenseSilentRatio {

	boolean verbose = false;

	public TestCasesIntegrationMissenseSilentRatio() {
		super();
	}

	@Test
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
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();

		snpeff.run();

		double silentRatio = snpeff.getChangeEffectResutStats().getSilentRatio();
		if (verbose) System.err.println("Missense / Silent ratio: " + silentRatio);

		Assert.assertEquals(1.19, silentRatio, 0.1);
	}
}
