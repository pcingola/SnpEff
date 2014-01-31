package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case where VCF entries has no sequence change (either REF=ALT or ALT=".") 
 * 
 * @author pcingola
 */
public class TestCasesNoChange extends TestCase {

	public TestCasesNoChange() {
		super();
	}

	public void test_01() {
		String args[] = { "testHg3766Chr1", "./tests/test.no_change.vcf" };
		vcfNoChange(args);
	}

	public void test_02() {
		String args[] = { "testHg3766Chr1", "./tests/test.no_change_02.vcf" };
		vcfNoChange(args);
	}

	void vcfNoChange(String args[]) {
		SnpEffCmdEff snpEffCmdEff = new SnpEffCmdEff();
		snpEffCmdEff.parseArgs(args);
		snpEffCmdEff.setVerbose(true);
		List<VcfEntry> vcfEntries = snpEffCmdEff.run(true);

		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve);
			for (VcfEffect veff : ve.parseEffects()) {
				EffectImpact imp = veff.getImpact();
				System.out.println("\t" + imp + "\t" + veff);
				Assert.assertEquals(EffectImpact.MODIFIER, imp);
			}
		}
	}
}
