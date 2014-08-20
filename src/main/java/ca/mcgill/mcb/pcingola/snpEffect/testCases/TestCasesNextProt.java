package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test NextProt databases
 *
 * @author pcingola
 */
public class TestCasesNextProt extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = true;
	public static int SHOW_EVERY = 10;

	public TestCasesNextProt() {
		super();
	}

	void checkNextProt(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact) {
		String args[] = { "-classic", "-nextProt", genomeVer, vcfFile };
		SnpEff cmd = new SnpEff(args);
		cmd.setVerbose(verbose);
		cmd.setSupressOutput(!verbose);

		// Run
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Check results
		int numNextProt = 0;
		for (VcfEntry ve : vcfEntries) {
			for (VcfEffect veff : ve.parseEffects()) {

				if ((veff.hasEffectType(EffectType.NEXT_PROT)) // Is it nextProt?
						&& effectDetails.equals(veff.getEffectDetails()) // Are details OK?
						&& (impact == veff.getImpact())) // Is impact OK?
					numNextProt++;

				if (verbose) //
					System.out.println("\t" + veff //
							+ "\n\t\tEffect type       : " + veff.getEffectType() //
							+ "\n\t\tEffect details    : '" + veff.getEffectDetails() + "'" //
							+ "\n\t\tEffect impact     : '" + veff.getImpact() + "'" //
							+ "\n\t\tExpected details  : '" + effectDetails + "'" //
							+ "\n\t\tExpected impact   : '" + impact + "'" //
							+ "\n\t\tCount matches     : " + numNextProt //
							+ "\thasEffectType : " + veff.hasEffectType(EffectType.NEXT_PROT) //
							+ "\tmatch details : " + effectDetails.equals(veff.getEffectDetails()) //
							+ "\tmatch impact: " + (impact == veff.getImpact()) //
					);
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	//	public void test_01_build() {
	//		Gpr.debug("Test");
	//		String args[] = { "buildNextProt", "testHg3770Chr22", "tests/nextProt" };
	//		SnpEff snpEff = new SnpEff(args);
	//		snpEff.setVerbose(verbose);
	//		snpEff.setSupressOutput(!verbose);
	//		boolean ok = snpEff.run();
	//		Assert.assertEquals(true, ok);
	//	}

	public void test_02_eff() {
		Gpr.debug("Test");
		// Note: Normally this EffectImpact should be 'HIGH' impact, but since the database we build in test_01_build is small, there are not enough stats.
		checkNextProt("testHg3770Chr22", "tests/test_nextProt_02.vcf", "amino_acid_modification:N-acetylglycine", EffectImpact.LOW);
	}

	//	public void test_03_eff() {
	//		Gpr.debug("Test");
	//		// Note: Normally this EffectImpact should be 'MODERATE' impact, but since the database we build in test_01_build is small, there are not enough stats.
	//		checkNextProt("testHg3770Chr22", "tests/test_nextProt_03.vcf", "amino_acid_modification:Phosphoserine", EffectImpact.MODERATE);
	//	}
	//
	//	public void test_04_parse() {
	//		Gpr.debug("Test");
	//		String vcfFile = "tests/test.nextProt_paren.vcf";
	//		int count = 0;
	//		for (VcfEntry ve : new VcfFileIterator(vcfFile)) {
	//			for (VcfEffect eff : ve.parseEffects()) {
	//				if (verbose) System.out.println(eff);
	//				if (eff.hasEffectType(EffectType.NEXT_PROT)) count++;
	//			}
	//		}
	//
	//		if (verbose) System.out.println("Count: " + count);
	//		Assert.assertTrue(count > 0);
	//
	//	}
}
