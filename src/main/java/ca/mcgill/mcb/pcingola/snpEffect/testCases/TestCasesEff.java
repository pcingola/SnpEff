package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test cases for cancer effect (difference between somatic an germline tissue)
 *
 * @author pcingola
 */
public class TestCasesEff extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesEff() {
		super();
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		// Arguments
		ArrayList<String> args = new ArrayList<String>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	/**
	 * Test output order
	 */
	public void test_01() {
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", null);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			EffectImpact impPrev = EffectImpact.HIGH;
			for (VcfEffect veff : ve.parseEffects()) {
				EffectImpact imp = veff.getImpact();

				if (verbose) System.out.println("\t" + imp + "\t" + impPrev + "\t" + imp.compareTo(impPrev) + "\t" + veff);
				Assert.assertTrue(impPrev.compareTo(imp) <= 0); // Higher impact go first
				impPrev = imp;
			}
		}
	}

	/**
	 * Test GATK option: At most one effect per VCF entry
	 */
	public void test_02() {
		String args[] = { "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect("testHg3770Chr22", "tests/eff_sort.vcf", args);

		for (VcfEntry ve : vcfEntries) {
			int numEffs = ve.parseEffects().size();
			if (verbose) System.out.println("Num effects:" + numEffs + "\t" + ve);
			Assert.assertTrue(numEffs <= 1);
		}
	}

	/**
	 * Make sure that empty VCF does not trigger an exception when creating the summary
	 */
	public void test_03_EmptyVcf() {
		String args[] = { "eff", "-noLog", "testHg3770Chr22", "tests/empty_only_header.vcf" };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setVerbose(verbose);
		snpEff.setSupressOutput(!verbose);
		boolean ok = snpEff.run();
		Assert.assertTrue(ok);
	}

}
