package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for GTF22 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationGtf22 extends TestCasesIntegrationBase {

	int exonToStringVersionOri;

	public TestCasesIntegrationGtf22() {
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
	public void testCaseMm37_61_ENSMUST00000070533() {
		Gpr.debug("Test");
		String genome = "testMm37.61";
		String gtfFile = "tests/ENSMUST00000070533.gtf";
		String resultFile = "tests/ENSMUST00000070533.txt";
		buildGtfAndCompare(genome, gtfFile, null, resultFile);
	}

}
