package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;

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
		String trName = "ENST00000369219";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000369368() {
		String trName = "ENST00000369368";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000415551() {
		String ensemblFile = "./tests/ENST00000415551_ensembl.txt";
		String trName = "ENST00000415551";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_01_ENST00000434489() {
		String trName = "ENST00000434489";
		String ensemblFile = "./tests/" + trName + "_ensembl.txt";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763Chr1", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_02() {
		String ensemblFile = "./tests/ENST00000430575_ensembl.txt";
		String trName = "ENST00000430575";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}

	public void test_03() {
		String ensemblFile = "./tests/ENST00000382896_ensembl.txt";
		String trName = "ENST00000382896";

		CompareToEnsembl compareToEnsembl = new CompareToEnsembl("testHg3763ChrY", false);
		compareToEnsembl.compareEnsembl(ensemblFile, trName);
	}
}
