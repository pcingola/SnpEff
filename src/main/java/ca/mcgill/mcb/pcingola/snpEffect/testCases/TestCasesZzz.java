package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	public void test_codonInsBug() {
		String vcf = "tests/athaliana_bug.vcf";
		throw new RuntimeException("A. Thaliana bug. File: " + vcf);

		//		java.lang.StringIndexOutOfBoundsException: String index out of range: 1
		//		at java.lang.String.substring(String.java:1950)
		//		at ca.mcgill.mcb.pcingola.interval.codonChange.CodonChangeIns.codonsNew(CodonChangeIns.java:88)
		//		at ca.mcgill.mcb.pcingola.interval.codonChange.CodonChangeIns.codonChangeSingle(CodonChangeIns.java:29)
		//		at ca.mcgill.mcb.pcingola.interval.codonChange.CodonChange.codonChange(CodonChange.java:179)
		//		at ca.mcgill.mcb.pcingola.interval.codonChange.CodonChangeMixed.codonChange(CodonChangeMixed.java:63)
		//		at ca.mcgill.mcb.pcingola.interval.Transcript.variantEffect(Transcript.java:1598)
		//		at ca.mcgill.mcb.pcingola.interval.Gene.variantEffect(Gene.java:413)
		//		at ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor.variantEffect(SnpEffectPredictor.java:602)
		//		at ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor.variantEffect(SnpEffectPredictor.java:557)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff.iterateVcf(SnpEffCmdEff.java:289)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff.runAnalysis(SnpEffCmdEff.java:791)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff.run(SnpEffCmdEff.java:711)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff.run(SnpEffCmdEff.java:663)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff.run(SnpEff.java:734)
		//		at ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff.main(SnpEff.java:123)

	}

}
