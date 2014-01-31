package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
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
public class TestCasesCancer extends TestCase {

	boolean verbose = true;
	boolean debug = false;

	public TestCasesCancer() {
		super();
	}

	/**
	 * Calculate snp effect for a list of snps
	 * @param snpEffFile
	 */
	public void snpEffect(String vcfFile, String txtFile, String aaHgsv, String genotype) {
		// Create command
		String argsVcf[] = { "-cancer", "-hgvs", "testHg3766Chr1", vcfFile };
		String argsTxt[] = { "-cancer", "-cancerSamples", txtFile, "-hgvs", "testHg3766Chr1", vcfFile };
		String args[] = (txtFile == null ? argsVcf : argsTxt);

		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.setVerbose(verbose);
		snpeff.parseArgs(args);

		// Run command
		List<VcfEntry> list = snpeff.run(true);

		// Find AA change for a genotype
		boolean found = false;
		for (VcfEntry vcfEntry : list) {
			for (VcfEffect eff : vcfEntry.parseEffects()) {
				if (genotype.equals(eff.getGenotype())) {
					Gpr.debug("AA: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff);
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
	public void test_01() {
		String file = "tests/test.cancer.snp.01.vcf";
		snpEffect(file, null, "p.Leu1?/c.1A>G", "2-1");
	}

	/**
	 * Test Somatic vs Germline (using TXT file)
	 */
	public void test_02() {
		String fileVcf = "tests/test.cancer_no_ped.vcf";
		String fileTxt = "tests/test.cancer_no_ped.txt";
		snpEffect(fileVcf, fileTxt, "p.Leu1?/c.1A>G", "2-1");
	}

}
