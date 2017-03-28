package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test mixed variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationMixedVariants {

	boolean debug = false;
	boolean verbose = false || debug;

	/**
	 * Compare with results from ENSEMBL's VEP to SnpEff
	 * Use VCF having VEP's results
	 */
	public void compareVep(String genome, String vcf) {
		CompareToVep comp = new CompareToVep(genome, verbose);
		comp.compareVep(vcf);
		if (verbose) System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	/**
	 * Make sure we can read VCF and parse variants without producing any exception
	 */
	@Test
	public void test_01_MixedVep() {
		Gpr.debug("Test");
		String vcfFile = "tests/mixed_01.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var);
			}
		}
	}

	@Test
	public void test_02_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr22", "tests/mixed_chr22.vcf");
	}

	@Test
	public void test_03_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr14", "tests/mixed_chr14.vcf");
	}

	@Test
	public void test_04_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr12", "tests/mixed_chr12.vcf");
	}

	@Test
	public void test_05_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr22", "tests/mixed_chr22.vcf");
	}

	@Test
	public void test_06_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr7", "tests/mixed_chr7.vcf");
	}

	@Test
	public void test_07_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr6", "tests/mixed_chr6.vcf");
	}

	@Test
	public void test_08_MixedVep() {
		Gpr.debug("Test");
		compareVep("testHg3775Chr1", "tests/mixed_chr1.vcf");
	}

	@Test
	public void test_09_MixedVep() {
		Gpr.debug("Test");

		String vcfFileName = "tests/mixed_09.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			for (Variant v : ve.variants()) {
				if (verbose) System.out.println("\t\t" + v);
				Assert.assertTrue("Variant is not MIXED", v.getVariantType() == VariantType.MIXED);
			}
		}
	}

	@Test
	public void test_11_ExonRank() {
		Gpr.debug("Test");

		String vcfFileName = "tests/mixed_11.vcf";
		String args[] = { "testHg19Chr20", vcfFileName };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
		snpeff.setSupressOutput(!verbose);
		snpeff.setVerbose(verbose);

		List<VcfEntry> vcfEnties = snpeff.run(true);
		VcfEntry ve = vcfEnties.get(0);

		// Get first effect (there should be only one)
		List<VcfEffect> veffs = ve.getVcfEffects();
		VcfEffect veff = veffs.get(0);

		Assert.assertEquals("Exon rank does not match", 12, veff.getRank());

	}

}
