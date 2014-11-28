package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case where VCF entries has no sequence change (either REF=ALT or ALT=".")
 *
 * @author pcingola
 */
public class TestCasesNoChange extends TestCase {

	boolean verbose = false;

	public TestCasesNoChange() {
		super();
	}

	public void test_01() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3766Chr1", "./tests/test.no_change.vcf" };
		vcfNoChange(args);
	}

	public void test_02() {
		Gpr.debug("Test");
		String args[] = { "-classic", "testHg3766Chr1", "./tests/test.no_change_02.vcf" };
		vcfNoChange(args);
	}

	void vcfNoChange(String args[]) {
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = snpeff.run(true);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.parseEffects()) {
				EffectImpact imp = veff.getImpact();
				if (verbose) System.out.println("\t" + imp + "\t" + veff);
				Assert.assertEquals(EffectImpact.MODIFIER, imp);
			}
		}
	}
}
