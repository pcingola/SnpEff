package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.SeqChangeTxtFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean verbose = true;
	boolean createOutputFile = false;
	Random rand;
	Config config;
	Genome genome;

	public TestCasesZzz() {
		super();
	}

	/** 
	 * Compare each result. If one matches, we consider it OK
	 * @param transcriptId
	 * @param seqChange
	 * @param resultsList
	 * @param useSimple
	 * @param resultsSoFar
	 * @return
	 */
	boolean anyResultMatches(String transcriptId, SeqChange seqChange, List<ChangeEffect> resultsList, boolean useShort) {
		boolean ok = false;
		for (ChangeEffect chEff : resultsList) {
			String resStr = chEff.toStringSimple(useShort);

			Transcript tr = chEff.getTranscript();
			if (tr != null) {
				if ((transcriptId == null) || (transcriptId.equals(tr.getId()))) {
					if (resStr.indexOf(seqChange.getId()) >= 0) return true; // Matches one result in this transcript
				}
			} else if (resStr.indexOf(seqChange.getId()) >= 0) return true; // Matches any result (out of a transcript)
		}
		return ok;
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		initSnpEffPredictor("testCase");
	}

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		config.setTreatAllAsProteinCoding(true); // For historical reasons we set this one to 'true'....
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Parse a SeqChange file and return a list
	 * 
	 * @param seqChangeFile
	 * @return
	 */
	public List<SeqChange> parseSnpEffectFile(String seqChangeFile) {
		ArrayList<SeqChange> seqChanges = new ArrayList<SeqChange>();

		int inOffset = 1;
		SeqChangeTxtFileIterator seqChangeFileIterator = new SeqChangeTxtFileIterator(seqChangeFile, config.getGenome(), inOffset);
		for (SeqChange sc : seqChangeFileIterator)
			seqChanges.add(sc);

		Collections.sort(seqChanges);
		return seqChanges;
	}

	/**
	 * Calculate snp effect for a list of snps
	 * @param snpEffFile
	 */
	public void snpEffect(List<SeqChange> seqChangeList, String transcriptId, boolean useShort, boolean negate) {
		int num = 1;
		// Predict each seqChange
		for (SeqChange seqChange : seqChangeList) {
			// Get results for each snp
			List<ChangeEffect> resultsList = config.getSnpEffectPredictor().seqChangeEffect(seqChange);

			String msg = "";
			msg += "Number : " + num + "\n";
			msg += "\tExpecting   : " + (negate ? "NOT " : "") + "'" + seqChange.getId() + "'\n";
			msg += "\tSeqChange   : " + seqChange + "\n";
			msg += "\tResultsList :\n";
			for (ChangeEffect res : resultsList)
				msg += "\t" + res + "\n";

			if (verbose) System.out.println(msg);

			// Compare each result. If one matches, we consider it OK
			// StringBuilder resultsSoFar = new StringBuilder();
			boolean ok = anyResultMatches(transcriptId, seqChange, resultsList, useShort);
			ok = negate ^ ok; // Negate? (i.e. when we are looking for effects that should NOT be matched)

			if (!ok) {
				if (createOutputFile) {
					for (ChangeEffect res : resultsList) {
						SeqChange sc = res.getSeqChange();
						System.out.println(sc.getChromosomeName() //
								+ "\t" + (sc.getStart() + 1) //
								+ "\t" + sc.getReference() //
								+ "\t" + sc.getChange() //
								+ "\t+\t0\t0" //
								+ "\t" + res.effect(true, true, true, false) //
						);
					}
				} else {
					Gpr.debug(msg);
					throw new RuntimeException(msg);
				}
			}
			num++;
		}
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure at least one effect matched the 'id' in the input TXT file
	 */
	public void snpEffect(String snpEffFile, String transcriptId, boolean useShort) {
		List<SeqChange> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, false); // Predict each snp
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure NOT A SINGLE effect matched the 'id' in the input TXT file, i.e. the opposite of snpEffect...) method.
	 */
	public void snpEffectNegate(String snpEffFile, String transcriptId, boolean useShort) {
		List<SeqChange> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, true); // Predict each snp
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_23_MNP_on_exon_edge() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_mnp_out_of_exon.txt", trId, true);
	}

}
