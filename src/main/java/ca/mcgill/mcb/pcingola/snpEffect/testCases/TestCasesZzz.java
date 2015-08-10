package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Check HGVS annotations
	 */
	public void checkHgvs(String genome, String vcfFile, int minCheck) {
		List<VcfEntry> list = snpEffect(genome, vcfFile, null);

		int countCheck = 0;
		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			String transcriptId = ve.getInfo("TR");
			for (VcfEffect veff : ve.parseEffects()) {

				if (veff.getTranscriptId().equals(transcriptId)) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.p: " + veff.getHgvsP() + "\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					if (expectedHgvsC != null) {
						String actualHgvsC = veff.getHgvsC();
						Assert.assertEquals(expectedHgvsC, actualHgvsC);
						countCheck++;
					}

					String expectedHgvsP = ve.getInfo("HGVSP");
					if (expectedHgvsP != null) {
						String actualHgvsP = veff.getHgvsP();
						Assert.assertEquals(expectedHgvsP, actualHgvsP);
						countCheck++;
					}
				}
			}
		}

		Assert.assertTrue("Too few variants checked: " + countCheck, countCheck >= minCheck);
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
		cmdEff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	/**
	 * Test HGVS.P in a synonimous frame shift
	 */
	@Test
	public void test_01_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr1", "tests/hgvs_frameshifts_syn_chr1.vcf", 4);
	}

	/**
	 * Test HGVS.P in a synonimous frame shift
	 */
	@Test
	public void test_02_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr4", "tests/hgvs_frameshifts_syn_chr4.vcf", 2);
	}

	/**
	 * Test HGVS.P in a synonimous frame shift
	 */
	@Test
	public void test_03_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr10", "tests/hgvs_frameshifts_syn_chr10.vcf", 2);
	}

	/**
	 * Test HGVS.P in a synonimous frame shift
	 */
	@Test
	public void test_04_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr17", "tests/hgvs_frameshifts_syn_chr17.vcf", 2);
	}

	/**
	 * Test HGVS.P in a synonimous frame shift
	 */
	@Test
	public void test_05_hgvs_frameshift() {
		Gpr.debug("Test");
		checkHgvs("testHg19Chr19", "tests/hgvs_frameshifts_syn_chr19.vcf", 2);
	}

}
