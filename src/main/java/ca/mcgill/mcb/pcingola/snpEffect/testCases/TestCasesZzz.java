package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;

/**
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	public TestCasesZzz() {
		super();
	}

	@Test
	public void test_01_Allele() {
		throw new RuntimeException("ANN: Check Allele & parsed correctly");
	}

	@Test
	public void test_02_Allele_Cancer() {
		throw new RuntimeException("ANN: Check Allele in cancer sample & parsed correctly");
	}

	@Test
	public void test_03_GeneName_Intergenic() {
		throw new RuntimeException("ANN: Check that closest gene name is correctly added & parsed correctly");
	}

	@Test
	public void test_04_FeatureType() {
		throw new RuntimeException("ANN: Check feature type 'transcript' & parsed correctly");
	}

	@Test
	public void test_05_FeatureType() {
		throw new RuntimeException("ANN: Check feature type 'custom' & parsed correctly");
	}

	@Test
	public void test_06_FeatureType() {
		throw new RuntimeException("ANN: Check feature type 'regulation', check that 'cell_type' is added & parsed correctly");
	}

	@Test
	public void test_07_cDnaPos_cDnaLen() {
		throw new RuntimeException("ANN: Check that cDna pos / len are added & parse correctly");
	}

	@Test
	public void test_08_CDS_CdsLen() {
		throw new RuntimeException("ANN: Check that CDS pos / len are added & parse correctly");
	}

	@Test
	public void test_09_ProteinPos_ProteinLen() {
		throw new RuntimeException("ANN: Check that protein pos / len are added & parse correctly");
	}

	@Test
	public void test_10_ProteinPos_ProteinLen() {
		throw new RuntimeException("ANN: Check -o GATK works OK and no '&' are added into effect field");
	}

}
