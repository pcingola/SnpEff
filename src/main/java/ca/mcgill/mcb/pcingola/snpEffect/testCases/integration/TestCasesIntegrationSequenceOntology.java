package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.io.IOException;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * Test case for sequence ontology
 *
 * @author pcingola
 */
public class TestCasesIntegrationSequenceOntology extends TestCasesIntegrationBase {

	public TestCasesIntegrationSequenceOntology() {
		super();
	}

	@Test
	public void test_01_Vep() throws IOException {
		Gpr.debug("Test");
		// create_SNP_file("testENST00000268124","./tests/testENST00000268124.SNP.ORI.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.SNP.vcf", "ENST00000268124");
	}

	@Test
	public void test_02_Vep() throws IOException {
		Gpr.debug("Test");
		// create_SNP_file("testENST00000268124","./tests/testENST00000268124.SNP.ORI.02.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.SNP.02.vcf", "ENST00000268124");
	}

	@Test
	public void test_03_Vep() throws IOException {
		Gpr.debug("Test");
		//		create_Ins_file("testENST00000268124", "./tests/testENST00000268124.Ins.ORI.03.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.Ins.03.vcf", "ENST00000268124");
	}

	@Test
	public void test_04_Vep() throws IOException {
		Gpr.debug("Test");
		//		create_Ins_file("testENST00000398332", "./tests/testENST00000398332.Ins.ORI.04.vcf", 0.95);
		compareVepSO("testENST00000398332", "tests/testENST00000398332.Ins.04.vcf", "ENST00000398332");
	}

	@Test
	public void test_05_Vep() throws IOException {
		Gpr.debug("Test");
		//		createDelFile("testENST00000268124", "./tests/testENST00000268124.Del.ORI.05.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.Del.05.vcf", "ENST00000268124");
	}

	@Test
	public void test_06_Vep() throws IOException {
		Gpr.debug("Test");
		// createMnpFile("testENST00000268124", "./tests/testENST00000268124.Mnp.ORI.06.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.Mnp.06.vcf", "ENST00000268124");
	}

}
