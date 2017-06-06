package org.snpeff.snpEffect.testCases.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snpeff.interval.Exon;
import org.snpeff.util.Gpr;

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
	public void testCaseHg37_61_ENST00000250838() {
		Gpr.debug("Test");
		String genome = "testHg37.61";
		String gtfFile = "tests/integration/gtf22/ENST00000250838.gtf";
		String fastaFile = "tests/integration/gtf22/chrY.fa.gz";
		String resultFile = "tests/integration/gtf22/ENST00000250838.txt";
		buildGtfAndCompare(genome, gtfFile, fastaFile, resultFile);
	}

	@Test
	public void testCaseHg37_61_ENST00000331397() {
		Gpr.debug("Test");
		String genome = "testHg37.61";
		String gtfFile = "tests/integration/gtf22/ENST00000331397.gtf22";
		String fastaFile = "tests/integration/gtf22/chrY.fa.gz";
		String resultFile = "tests/integration/gtf22/ENST00000331397.txt";
		buildGtfAndCompare(genome, gtfFile, fastaFile, resultFile);
	}

	@Test
	public void testCaseMm37_61_ENSMUSG00000051951() {
		Gpr.debug("Test");
		String genome = "testMm37.61";
		String gtfFile = "tests/integration/gtf22/ENSMUSG00000051951.gtf";
		String resultFile = "tests/integration/gtf22/ENSMUSG00000051951.txt";
		buildGtfAndCompare(genome, gtfFile, null, resultFile);
	}

	@Test
	public void testCaseMm37_61_ENSMUST00000070533() {
		Gpr.debug("Test");
		String genome = "testMm37.61";
		String gtfFile = "tests/integration/gtf22/ENSMUST00000070533.gtf";
		String resultFile = "tests/integration/gtf22/ENSMUST00000070533.txt";
		buildGtfAndCompare(genome, gtfFile, null, resultFile);
	}

}
