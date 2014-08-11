package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Test random DEL changes
 *
 * @author pcingola
 */
public class TestCasesDel extends TestCase {

	public static int N = 1000;

	boolean debug = false;
	boolean forcePositive = debug || false; // Force positive strand (used for debugging)
	static boolean verbose = false;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesDel() {
		super();
		init();
	}

	/**
	 * Calcualte codonsNew using a naive algrithm
	 */
	String codonsNew(Variant seqChange) {
		int cdsBaseNum = 0;
		String codonsNew = "";
		char currCodon[] = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				// Should we use this codon?
				if (seqChange.intersects(pos)) useCodon = true;
				else {
					// Should we use this base? We don't use the ones that intersect with 'seqChage' (because they are deleted)
					char base = chromoBases[pos];
					currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon
				}

				// Add it?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsNew += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsNew += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsNew);
	}

	/**
	 * Calculate codonsOld using a naive algorithm
	 */
	String codonsOld(Variant seqChange) {
		int cdsBaseNum = 0;
		String codonsOld = "";
		char currCodon[] = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				useCodon |= seqChange.intersects(pos); // Should we use this codon?
				char base = chromoBases[pos];
				currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon

				// Finished codon?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsOld += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsOld += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsOld);
	}

	void init() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(forcePositive);
		if (forcePositive) Gpr.debug("WARNING: Positive strand only tests!");

		// Create predictor
		snpEffectPredictor = sepf.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		// Data
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();
		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	/**
	 * Remove white spaces from a string.
	 * @param str
	 * @return A string without any white spaces. '-' resulting string is empty
	 */
	String removeWhiteSpaces(String str) {
		String strNoWs = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != ' ') strNoWs += str.charAt(i);
		}

		return strNoWs;
	}

	public void test_01() {
		Gpr.debug("Test");
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		for (int i = 0; i < N; i++) {

			initSnpEffPredictor();
			if (debug) System.out.println("DEL Test iteration: " + i + "\n" + transcript);
			else if (verbose) System.out.println("DEL Test iteration: " + i + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);
			int cdsBaseNum = 0;

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				int step = exon.isStrandPlus() ? 1 : -1;
				int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

				// For each base in this exon...
				for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
					//---
					// Create seqChange
					//---
					// Get a random base different from 'refBase'
					int delLen = rand.nextInt(10) + 1;

					int start = pos;
					int end = pos + delLen;
					if (transcript.isStrandMinus()) {
						start = pos - delLen;
						end = pos;
					}

					if (start < 0) start = 0;
					if (end > chromosome.getEnd()) end = chromosome.getEnd();
					delLen = end - start + 1;

					String delPlus = chromoSequence.substring(start, end + 1); // Deletion (plus strand)
					String del = delPlus;

					// Codon number
					int cdsCodonNum = cdsBaseNum / 3;
					int cdsCodonPos = cdsBaseNum % 3;

					// Create a SeqChange
					Variant variant = new Variant(chromosome, start, "", "-" + del, "");

					// Sanity checks
					Assert.assertEquals(true, variant.isDel()); // Is it a deletion?
					Assert.assertEquals(del.length(), variant.size()); // Does seqChange have the correct size?

					//---
					// Expected Effect
					//---
					String effectExpected = "";
					String codonsOld = codonsOld(variant);
					codonsOld = codonsOld.toUpperCase();
					String aaOld = codonTable.aa(codonsOld);

					String codonsNew = codonsNew(variant);
					// String aaNew = codonTable.aa(codonsNew.length() < 3 ? "" : codonsNew);
					String aaNew = codonTable.aa(codonsNew);

					// Net change
					String netChange = "";
					for (Exon ex : transcript.sortedStrand())
						netChange += variant.netChange(ex);

					// Replace empty by '-'
					if (codonsOld.isEmpty()) codonsOld = "-";
					if (codonsNew.isEmpty()) codonsNew = "-";
					if (aaOld.isEmpty()) aaOld = "-";
					if (aaNew.isEmpty()) aaNew = "-";

					if (variant.includes(exon)) effectExpected = "EXON_DELETED";
					else if (netChange.length() % 3 != 0) effectExpected = "FRAME_SHIFT(" + aaOld + "/" + "-" + ")";
					else {
						if (cdsCodonPos == 0) effectExpected = "CODON_DELETION(" + aaOld + "/-)";
						else {
							if (codonsOld.startsWith(codonsNew) || (codonsNew.equals("-"))) effectExpected = "CODON_DELETION(" + aaOld + "/" + aaNew + ")";
							else effectExpected = "CODON_CHANGE_PLUS_CODON_DELETION(" + aaOld + "/" + aaNew + ")";
						}

						if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonsOld) && !codonTable.isStartFirst(codonsNew)) effectExpected = "START_LOST(" + aaOld + "/" + aaNew + ")";
						else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) effectExpected = "STOP_LOST(" + aaOld + "/" + aaNew + ")";
						else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) effectExpected = "STOP_GAINED(" + aaOld + "/" + aaNew + ")";
					}

					//---
					// Calculate effects
					//---
					VariantEffects effectsAll = snpEffectPredictor.variantEffect(variant);
					VariantEffects effects = new VariantEffects(variant);
					for (VariantEffect eff : effectsAll) {
						boolean copy = true;

						if (eff.getEffectType() == EffectType.SPLICE_SITE_ACCEPTOR) copy = false;
						if (eff.getEffectType() == EffectType.SPLICE_SITE_DONOR) copy = false;
						if (eff.getEffectType() == EffectType.INTRON) copy = false;

						if (copy) {
							Gpr.debug("COPY:" + eff);
							effects.effect(eff.getMarker(), eff.getEffectType(), "");
						}
					}

					// There should be only one effect in most cases
					Assert.assertEquals(false, effects.isEmpty()); // There should be at least one effect
					if (debug && (effects.size() > 1)) {
						System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
						System.out.println("\tEffects: ");
						for (VariantEffect eff : effects)
							System.out.println("\t" + eff);
					}

					// Show

					//---
					// Check effect
					//---
					boolean ok = false;
					for (VariantEffect effect : effects) {
						String effStr = effect.effect(true, true, true, false);
						if (debug) Gpr.debug("\tIteration: " + i + "\tPos: " + pos + "\tExpected: '" + effectExpected + "'\tEffect: '" + effStr + "'");

						if (effectExpected.equals(effStr)) {
							ok = true;
							// Check codons
							if ((effect.getEffectType() != EffectType.FRAME_SHIFT) // No codons in 'FRAME_SHIFT'
									&& (effect.getEffectType() != EffectType.EXON_DELETED) // No codons in 'EXON_DELETED'
									&& (effect.getEffectType() != EffectType.SPLICE_SITE_REGION) // No codons in 'SPLICE_SITE_REGION'
									&& (effect.getEffectType() != EffectType.INTERGENIC) // No codons in 'INTERGENIC'
							) {

								if (codonsNew.equals("-")) codonsNew = "";
								String codonsNewEff = effect.getCodonsNew().toUpperCase();
								if (codonsNewEff.equals("-")) codonsNewEff = "";

								if (debug //
										|| !codonsOld.equals(effect.getCodonsOld().toUpperCase()) //
										|| !codonsNew.equals(codonsNewEff)) {
									System.out.println("\tIteration: " + i //
											+ "\tPos: " + pos //
											+ "\n\t\tCDS base [codon] : " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
											+ "\n\t\tSeqChange        : " + variant + "\tsize: " + variant.size() + "\tdelPlus: " + delPlus//
											+ "\n\t\tNetCdsChange     : " + netChange //
											+ "\n\t\tExpected         : " + effectExpected //
											+ "\n\t\tEffect           : " + effStr //
											+ "\n\t\tAA               : '" + aaOld + "' / '" + aaNew + "'" //
											+ "\n\t\tAA (eff)         : '" + effect.getAaOld() + "' / '" + effect.getAaNew() + "'" //
											+ "\n\t\tCodon            : '" + codonsOld + "' / '" + codonsNew + "'" //
											+ "\n\t\tCodons(eff)      : '" + effect.getCodonsOld().toUpperCase() + "' / '" + effect.getCodonsNew().toUpperCase() + "'" //
											+ "\n" //
									);
								}

								Assert.assertEquals(codonsOld, effect.getCodonsOld().toUpperCase()); // Check codons old
								Assert.assertEquals(codonsNew, codonsNewEff); // Check codons new
							}
						}
					}
					Assert.assertEquals(true, ok);
				}
			}
		}
	}
}
