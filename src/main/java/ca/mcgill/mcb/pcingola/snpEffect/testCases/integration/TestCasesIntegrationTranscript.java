package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationTranscript {

	//	public static int N = 1000;
	boolean debug = false;
	boolean verbose = false || debug;

	@Test
	public void test_mRnaSequence() {
		Gpr.debug("Test");
		String genome = "testHg3766Chr1";
		Config config = new Config(genome);

		verbose = true;
		if (verbose) Timer.showStdErr("Loading genome " + genome);
		SnpEffectPredictor sep = config.loadSnpEffectPredictor();
		if (verbose) Timer.showStdErr("Building interval forest");
		sep.buildForest();
		if (verbose) Timer.showStdErr("Done");

		int count = 1;
		for (Gene gene : sep.getGenome().getGenes()) {
			for (Transcript tr : gene) {

				if (!tr.isProteinCoding()) continue;
				if (!tr.hasErrorOrWarning()) continue;

				String mRna = tr.mRna().toLowerCase();
				String cds = tr.cds().toLowerCase();

				// Get UTR sequence
				List<Utr5prime> utrs5 = tr.get5primeUtrs();
				if (utrs5.size() <= 0) continue;

				Gpr.showMark(count++, 1);

				Utr5prime utr5 = utrs5.get(0);
				String utr5Str = utr5.getSequence().toLowerCase();

				// Sanity check
				if (!mRna.startsWith(utr5Str)) throw new RuntimeException("ERROR mRna does not start with UTR5");
				if (!mRna.startsWith(utr5Str + cds)) throw new RuntimeException("ERROR mRna does not start with  UTR+CDS");
			}
		}
	}

}
