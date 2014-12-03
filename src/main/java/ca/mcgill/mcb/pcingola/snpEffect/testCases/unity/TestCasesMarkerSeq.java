package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 *
 * Test case
 */
public class TestCasesMarkerSeq {

	protected boolean debug = false;
	protected boolean verbose = false || debug;
	protected int randSeed = 20141128;
	protected Random rand;

	public TestCasesMarkerSeq() {
		super();
	}

	protected void initRand() {
		rand = new Random(randSeed);
	}

	/**
	 * Test markerSeq.getSequence(marker) method
	 * Compare to trivial implementation
	 */
	@Test
	public void test_zzz() {
		Gpr.debug("Test");
		initRand();
		int maxSize = 20; // Max subsequence size to test

		String genome = "testHg19Chr17";
		String vcf = "tests/hgvs_dup.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// The problem appears when splice site is large (in this example)
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		snpeff.load();
		Config config = snpeff.getConfig();
		int count = 0;
		for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes())
			for (Transcript tr : g) {
				for (Exon ex : tr) {
					Gpr.showMark(count++, 100);

					String sequence = ex.getSequence();
					if (ex.isStrandMinus()) sequence = GprSeq.reverseWc(sequence);
					if (verbose) Gpr.debug("Transcript: " + tr.getId() + "\tExon: " + ex.getId() + ", strand: " + (ex.isStrandPlus() ? "+" : "-") + "\tSequence: " + sequence);

					for (int i = 0, pos = ex.getStart(); pos <= ex.getEnd(); i++, pos++) {
						// Random size
						int size = rand.nextInt(maxSize);
						if (size > (ex.getEnd() - pos)) size = 0;

						// Get subsequence
						String seqExpected = sequence.substring(i, i + size + 1);

						// Retrieve same sequence using a marker query
						Marker m = new Marker(ex.getChromosome(), pos, pos + size, false, "");
						String seq = ex.getSequence(m);

						// Check that sequences match
						if (!seq.equalsIgnoreCase(seqExpected)) {
							String msg = "Expecting '" + seqExpected + "', got '" + seq + "'";
							System.err.println("ERROR:\t" + msg);
							throw new RuntimeException(msg);
						}
					}
				}
			}
	}
}
