package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

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
 * Test cases for HGVS notation
 */
public class TestCasesHgvsUpDownStream {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesHgvsUpDownStream() {
		super();
	}

	/**
	 * Calculate annotations for an input VCF file
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
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Check that there were no errors
		Assert.assertFalse("Annotation finished with errors", cmdEff.getTotalErrs() > 0);

		return list;
	}

	/**
	 * Test for HGVS.C notation on upstream variants
	 */
	@Test
	public void test_01_hgvs_upstream() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr2", "tests/hgvs_upstream.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (veff.getTranscriptId().equals("NM_000463.2")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test for HGVS.C notation on downstream variants
	 */
	@Test
	public void test_02_hgvs_downstream() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr2", "tests/hgvs_downstream.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (veff.getTranscriptId().equals("NM_000463.2")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test that CSV summary does not throw any error
	 */
	@Test
	public void test_03_hgvs_upstream_del() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg3765Chr22", "tests/hgvs_upstream_del.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (veff.getTranscriptId().equals("ENST00000404751")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
	 */
	@Test
	public void test_04_hgvs_upstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr17", "tests/hgvs_upstream_negative_strand.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (veff.getTranscriptId().equals("NM_000199.3")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

	/**
	 * Test HGVS.C upstream of a variant affecting a transcript on the negative strand
	 */
	@Test
	public void test_05_hgvs_downstream_negative_strand() {
		Gpr.debug("Test");
		List<VcfEntry> list = snpEffect("testHg19Chr17", "tests/hgvs_downstream_negative_strand.vcf", null);

		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (veff.getTranscriptId().equals("NM_000199.3")) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					String actualHgvsC = veff.getHgvsC();
					Assert.assertEquals(expectedHgvsC, actualHgvsC);
				}
			}
		}
	}

}
