package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * Test case
 */
public class TestCasesAnn extends TestCasesBase {

	public TestCasesAnn() {
		super();
	}

	/**
	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	 */
	@Test
	public void test_01_VcfRawAlt() {
		Gpr.debug("Test");

		// Create command
		String args[] = { "testHg3775Chr1", "tests/test_ann_integration_01.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Expected results
		Set<String> allelesExpected = new HashSet<String>();
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");

		// Find AA change for a genotype
		Set<String> allelesReal = new HashSet<String>();
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);

			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
				String allele = eff.getAllele();
				if (verbose) System.err.println("\t" + eff + "\n\t\tAllele: " + allele);

				Assert.assertTrue("Unexpected allele '" + allele + "'", allelesExpected.contains(allele));
				allelesReal.add(allele);
			}
		}

		Assert.assertEquals(allelesExpected, allelesReal);
	}

}
