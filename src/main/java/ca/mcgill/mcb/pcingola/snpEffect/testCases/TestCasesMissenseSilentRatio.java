package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;

/**
 * Calculate missense over silent ratio 
 * 
 * @author pcingola
 */
public class TestCasesMissenseSilentRatio extends TestCase {

	public TestCasesMissenseSilentRatio() {
		super();
	}

	public void test_01() {
		String args[] = { "-i", "vcf" //
				, "-noOut" //
				, "-useLocalTemplate" //
				, "testHg3765Chr22" //
				, "./tests/missenseSilent.chr22.vcf.gz" //
		};

		SnpEffCmdEff snpEffCmdEff = new SnpEffCmdEff();
		snpEffCmdEff.parseArgs(args);
		snpEffCmdEff.setVerbose(true);
		snpEffCmdEff.run();

		double silentRatio = snpEffCmdEff.getChangeEffectResutStats().getSilentRatio();
		System.err.println("Missense / Silent ratio: " + silentRatio);

		Assert.assertEquals(1.19, silentRatio, 0.1);
	}
}
