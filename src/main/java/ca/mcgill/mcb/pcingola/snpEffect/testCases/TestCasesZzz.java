package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 *
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	int exonToStringVersionOri;

	public TestCasesZzz() {
		super();
	}

	@After
	public void after() {
		Exon.ToStringVersion = exonToStringVersionOri;
	}

	@Before
	public void before() {
		exonToStringVersionOri = Exon.ToStringVersion;
		Exon.ToStringVersion = 1; // Set "toString()" version
	}

	@Test
	public void testCase_01_Exon_Simple() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonSimple.gff3";
		String resultFile = "tests/exonSimple.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

}
