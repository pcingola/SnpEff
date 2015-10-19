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

	/**
	 * Exon.frameCorrection: Exon too short (size: 1), cannot correct frame!
	 */
	@Test
	public void testCase_10_MaizeZmB73() {
		Gpr.debug("Test");
		String genome = "testMaizeZmB73";
		String gff3File = "tests/testMaizeZmB73.gff3";
		String resultFile = "tests/testMaizeZmB73.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

}
