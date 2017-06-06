package org.snpeff.snpEffect.testCases.integration;

import org.junit.Test;
import org.snpeff.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnpEnsembl {

	public TestCasesIntegrationSnpEnsembl() {
		super();
	}

	@Test
	public void test_01_ENST00000369219() {
		Gpr.debug("Test");
		String trName = "ENST00000369219";
		String ensemblFile = "./tests/integration/snpEnsembl/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	@Test
	public void test_01_ENST00000369368() {
		Gpr.debug("Test");
		String trName = "ENST00000369368";
		String ensemblFile = "./tests/integration/snpEnsembl/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	@Test
	public void test_01_ENST00000415551() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/integration/snpEnsembl/ENST00000415551_ensembl.txt";
		String trName = "ENST00000415551";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	@Test
	public void test_01_ENST00000434489() {
		Gpr.debug("Test");
		String trName = "ENST00000434489";
		String ensemblFile = "./tests/integration/snpEnsembl/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	@Test
	public void test_02() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/integration/snpEnsembl/ENST00000430575_ensembl.txt";
		String trName = "ENST00000430575";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	@Test
	public void test_03() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/integration/snpEnsembl/ENST00000382896_ensembl.txt";
		String trName = "ENST00000382896";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}
}
