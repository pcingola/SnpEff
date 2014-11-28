package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesSnpEnsembl extends TestCase {

	public TestCasesSnpEnsembl() {
		super();
	}

	public void test_01_ENST00000369219() {
		Gpr.debug("Test");
		String trName = "ENST00000369219";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000369368() {
		Gpr.debug("Test");
		String trName = "ENST00000369368";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000415551() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/ENST00000415551_ensembl.txt";
		String trName = "ENST00000415551";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000434489() {
		Gpr.debug("Test");
		String trName = "ENST00000434489";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_02() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/ENST00000430575_ensembl.txt";
		String trName = "ENST00000430575";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_03() {
		Gpr.debug("Test");
		String ensemblFile = "./tests/ENST00000382896_ensembl.txt";
		String trName = "ENST00000382896";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}
}
