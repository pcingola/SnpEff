package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test cases for cancer effect (difference betwee somatic an germline tissue)
 *
 * @author pcingola
 */
public class TestCasesCancer {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesCancer() {
		super();
	}

	/**
	 * Calculate snp effect for a list of snps
	 */
	public void snpEffect(String vcfFile, String txtFile, String aaHgsv, String genotype) {
		// Create command
		String argsVcf[] = { "-classic", "-cancer", "-hgvs", "testHg3766Chr1", vcfFile };
		String argsTxt[] = { "-classic", "-cancer", "-cancerSamples", txtFile, "-hgvs", "testHg3766Chr1", vcfFile };
		String args[] = (txtFile == null ? argsVcf : argsTxt);

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);

		// Find AA change for a genotype
		boolean found = false;
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);
			for (VcfEffect eff : vcfEntry.parseEffects()) {
				if (debug) System.err.println("\t" + eff + "\n\t\tAA : " + eff.getAa() + "\n\t\tGenotype: " + eff.getGenotype());
				if (genotype.equals(eff.getGenotype())) {
					Assert.assertEquals(aaHgsv, eff.getAa());
					found = true;
				}
			}
		}

		// Not found? Error
		if (!found) throw new RuntimeException("Genotype '" + genotype + "' not found.");
	}

	/**
	 * Test Somatic vs Germline
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String file = "tests/test.cancer.snp.01.vcf";
		snpEffect(file, null, "p.Leu1?/c.1A>G", "G-C");
	}

	/**
	 * Test Somatic vs Germline (using TXT file)
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");
		String fileVcf = "tests/test.cancer_no_ped.vcf";
		String fileTxt = "tests/test.cancer_no_ped.txt";
		snpEffect(fileVcf, fileTxt, "p.Leu1?/c.1A>G", "G-C");
	}

}
