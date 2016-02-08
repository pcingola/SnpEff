package org.snpeff.snpEffect.testCases.integration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactory;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test case for GFF3 file parsing
 *
 * @author pcingola
 */
public class TestCasesIntegrationGff3 extends TestCasesIntegrationBase {

	int exonToStringVersionOri;

	public TestCasesIntegrationGff3() {
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

	@Test
	public void testCase_02_ExonIn() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonIn.gff3";
		String resultFile = "tests/exonIn.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_03_ExonOut() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/exonOut.gff3";
		String resultFile = "tests/exonOut.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_04_AthalianaTair10_AT5G66790() {
		Gpr.debug("Test");
		String genome = "athalianaTair10";
		String gff3File = "tests/AT5G66790.gff3";
		String resultFile = "tests/AT5G66790.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_05_PaeruPA14muccA() {
		Gpr.debug("Test");
		String genome = "paeru.PA14";
		String gff3File = "tests/paeru.PA14.muccA.gff";
		String resultFile = "tests/paeru.PA14.muccA.txt";
		SnpEffectPredictor sep = buildGff3AndCompare(genome, gff3File, resultFile, true, false);

		// Make sure no splice site is added
		Gene gene = sep.getGenome().getGenes().iterator().next();
		Transcript tr = gene.iterator().next();
		tr.createSpliceSites(SpliceSite.CORE_SPLICE_SITE_SIZE, 0, 0, 0);
		List<SpliceSite> spliceSites = tr.spliceSites();
		Assert.assertEquals(0, spliceSites.size());
	}

	@Test
	public void testCase_06_Ppersica() {
		Gpr.debug("Test");
		String genome = "ppersica139";
		String gff3File = "tests/ppersica_139.gff";
		String resultFile = "tests/ppersica_139.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, false, false);
	}

	@Test
	public void testCase_07_Rice5() {
		Gpr.debug("Test");
		String genome = "testRice5";
		String gff3File = "tests/Os03t0150600.gff";
		String resultFile = "tests/Os03t0150600.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, false, false);
	}

	@Test
	public void testCase_08_Vibrio() {
		Gpr.debug("Test");
		String genome = "vibrio";
		String gff3File = "tests/vibrio.gff3";
		String resultFile = "tests/vibrio.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_09() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/testAP_genes.gff.gz";
		String resultFile = "tests/testAP.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_09_AP() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/testAP_genes.gff.gz";
		String resultFile = "tests/testAP.txt";

		// Capture STDERR
		PrintStream errOri = System.err;
		final ByteArrayOutputStream myErr = new ByteArrayOutputStream();
		System.setErr(new PrintStream(myErr));

		// Test
		try {
			SnpEffPredictorFactory.MIN_TOTAL_FRAME_COUNT = 1; // Force warning even for one gene
			buildGff3AndCompare(genome, gff3File, resultFile, true, false);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Restore STDERR
			System.setErr(errOri);
		}

		// Show stderr and check message
		System.err.println("STDERR:\n" + myErr);
		Assert.assertTrue(myErr.toString().indexOf("WARNING: All frames are zero!") >= 0);
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
