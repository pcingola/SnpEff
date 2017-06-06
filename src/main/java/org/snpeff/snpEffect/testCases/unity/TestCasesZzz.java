package org.snpeff.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test multiple variants affecting one codon
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public static int N = 1000;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Check that RAW alt fields are kept in 'Allele/Genotype'
	 */
	@Test
	public void test_01_VcfRawAlt() {
		Gpr.debug("Test");
		verbose = true;

		// Create command
		String args[] = { "testHg3775Chr1", "tests/test_ann_integration_01.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Expected results
		Set<String> allelesExpected = new HashSet<>();
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACACACACAC");
		allelesExpected.add("AACACACACACACACACACACACACACACACACACAC");

		// Find AA change for a genotype
		Set<String> allelesReal = new HashSet<>();
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
