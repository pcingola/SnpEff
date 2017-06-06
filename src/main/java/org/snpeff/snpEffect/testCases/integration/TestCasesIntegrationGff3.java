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
		String gff3File = "tests/integration/gff3/exonSimple.gff3";
		String resultFile = "tests/integration/gff3/exonSimple.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_02_ExonIn() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/integration/gff3/exonIn.gff3";
		String resultFile = "tests/integration/gff3/exonIn.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_03_ExonOut() {
		Gpr.debug("Test");
		String genome = "testCase";
		String gff3File = "tests/integration/gff3/exonOut.gff3";
		String resultFile = "tests/integration/gff3/exonOut.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_04_AthalianaTair10_AT5G66790() {
		Gpr.debug("Test");
		Exon.ToStringVersion = exonToStringVersionOri;
		String genome = "testAthalianaTair10"; //"athalianaTair10";
		String gff3File = "tests/integration/gff3/AT5G66790.gff3";
		String resultFile = "tests/integration/gff3/AT5G66790.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_05_PaeruPA14muccA() {
		Gpr.debug("Test");
		Exon.ToStringVersion = exonToStringVersionOri;
		String genome = "testPaeru.PA14";
		String gff3File = "tests/integration/gff3/paeru.PA14.muccA.gff";
		String resultFile = "tests/integration/gff3/paeru.PA14.muccA.txt";
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
		Exon.ToStringVersion = exonToStringVersionOri;
		String genome = "testPpersica139";
		String gff3File = "tests/integration/gff3/ppersica_139.gff";
		String resultFile = "tests/integration/gff3/ppersica_139.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, false, false);
	}

	@Test
	public void testCase_07_Rice5() {
		Gpr.debug("Test");
		String genome = "testRice5";
		String gff3File = "tests/integration/gff3/Os03t0150600.gff";
		String resultFile = "tests/integration/gff3/Os03t0150600.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, false, false);
	}

	@Test
	public void testCase_08_Vibrio() {
		Gpr.debug("Test");
		Exon.ToStringVersion = exonToStringVersionOri;
		String genome = "testVibrio";
		String gff3File = "tests/integration/gff3/vibrio.gff3";
		String resultFile = "tests/integration/gff3/vibrio.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_09() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/integration/gff3/testAP_genes.gff.gz";
		String resultFile = "tests/integration/gff3/testAP.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	@Test
	public void testCase_09_AP() {
		Gpr.debug("Test");
		String genome = "testAP";
		String gff3File = "tests/integration/gff3/testAP_genes.gff.gz";
		String resultFile = "tests/integration/gff3/testAP.txt";

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
		String gff3File = "tests/integration/gff3/testMaizeZmB73.gff3";
		String resultFile = "tests/integration/gff3/testMaizeZmB73.txt";
		buildGff3AndCompare(genome, gff3File, resultFile, true, false);
	}

	/**
	 * Test for bug: Infinite loop when looking up "Parent ID" in some GFF3 files
	 */
	@Test
	public void testCase_11_mita() {
		Gpr.debug("Test");
		String genome = "testMita";
		String gff3File = "tests/integration/gff3/testMita.gff";
		String resultFile = null; // We only check that there is no "Out of memory" error due to infinite loop
		buildGff3AndCompare(genome, gff3File, resultFile, false, false);
	}

}
