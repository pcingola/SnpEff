package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = true;
	boolean verbose = false || debug;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesZzz() {
		super();
		init();
	}

	/**
	 * Distance to transcription start site (from a position before CDS start)
	 * @param bases
	 * @param pos
	 * @param direction
	 * @return
	 */
	int distToCodingBase(char bases[], int pos, int direction) {
		for (int count = 0; (pos >= 0) && (pos < bases.length); pos += direction) {
			if ((bases[pos] == '>') || (bases[pos] == '<')) return count;
			if (bases[pos] != '-') count++;
		}

		throw new RuntimeException("This should never happen!");
	}

	/**
	 * Count how many bases are there until the exon
	 * @param bases
	 * @param pos
	 * @return
	 */
	int distToExon(char bases[], int pos, int direction) {
		int countAfter = 0;
		for (int j = pos; (j >= 0) && (j < bases.length); countAfter++, j += direction)
			if (bases[j] != '-') break;

		int countBefore = 0;
		for (int j = pos; (j >= 0) && (j < bases.length); countBefore++, j -= direction)
			if (bases[j] != '-') break;

		if (countBefore <= countAfter) return countBefore;
		return -countAfter;
	}

	/**
	 * Distance to UTR
	 * @param bases
	 * @param pos
	 * @param direction
	 * @return
	 */
	int distToUtr5(char bases[], int pos, int direction) {
		int count = 0;
		for (; (pos >= 0) && (pos < bases.length); pos -= direction) {
			if (bases[pos] == '5') return count;
			if (bases[pos] != '-') count++;
		}
		return count;
	}

	void init() {
		initRand();
		initSnpEffPredictor(false);
	}

	void initRand() {
		rand = new Random(20130708);
	}

	/**
	 * Create a predictor
	 */
	void initSnpEffPredictor(boolean addUtrs) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Initialize factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(true); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
		sepf.setAddUtrs(addUtrs);

		// Create predictor
		snpEffectPredictor = sepf.create();

		// Update config
		config.setSnpEffectPredictor(snpEffectPredictor);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(0);

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	/**
	 * Run SnpEff on VCF file
	 * @param vcfFile
	 */
	public void snpEffect(String vcfFile, String genomeVer) {
		// Create command
		String args[] = { "-hgvs", "-ud", "0", genomeVer, vcfFile };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		// Run command
		List<VcfEntry> list = cmdEff.run(true);

		// Find HGVS in any 'EFF' field
		int entryNum = 1;
		for (VcfEntry vcfEntry : list) {
			boolean found = false;

			// Load hgvs expected annotations into set
			String hgvsStr = vcfEntry.getInfo("HGVS");
			HashSet<String> hgvsExpected = new HashSet<String>();
			for (String h : hgvsStr.split(",")) {
				if (h.indexOf(':') > 0) h = h.substring(h.indexOf(':') + 1);
				hgvsExpected.add(h);
			}

			if (debug) System.err.println(entryNum + "\t" + vcfEntry);

			// Find if HGVS predicted by SnpEff matches tha expected annotations
			for (VcfEffect eff : vcfEntry.parseEffects()) {
				String hgvsReal = eff.getAa();
				if (debug) System.err.println("\tHGVS: " + hgvsExpected.contains(hgvsReal) + "\t" + hgvsExpected + "\tAA: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff);
				if (hgvsExpected.contains(hgvsReal)) found = true;
			}

			// Not found? Error
			if (!found) throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
			entryNum++;
		}
	}

	public void test_05_intron() {
		int N = 100;

		int testIter = 142; // -1;
		int testPos = 768; // -1;

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int checked = 0, it = 1; checked < N; it++) {
			initSnpEffPredictor(true);
			//			if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
			//			else System.out.println("HGSV Test iteration: " + i + "\t" + (transcript.getStrand() >= 0 ? "+" : "-") + "\t" + transcript.cds());

			boolean tested = false;

			if (testIter >= 0 && it < testIter) {
				Gpr.debug("Skipping iteration: " + it);
				continue;
			}

			// No introns? Nothing to test
			if (transcript.introns().size() < 1) continue;

			// !!!!!!!!!!!!!!!!!!!!
			// CHECK NEGATIVE STRAND TRANSCRIPTS
			// !!!!!!!!!!!!!!!!!!!!
			if (transcript.isStrandMinus()) {
				Gpr.debug("!!!!!!!!!!!!!!!!!!!!");
				continue;
			}

			// Character representation
			String trstr = transcript.toStringAsciiArt();
			char bases[] = trstr.toCharArray();

			int cdsStart = transcript.getCdsStart();
			int cdsEnd = transcript.getCdsEnd();
			int cdsLeft = Math.min(cdsStart, cdsEnd);
			int cdsRight = Math.max(cdsStart, cdsEnd);

			// Show data
			System.out.println("Iteration:" + it + "\tChecked: " + checked);
			if (verbose) {
				System.out.println(trstr);
				System.out.println("Length   : " + transcript.size());
				System.out.println("CDS start: " + cdsStart);
				System.out.println("CDS end  : " + transcript.getCdsEnd());
				System.out.println(transcript);
			}

			// Check each intronic base
			for (int j = 0, pos = transcript.getStart(); pos < transcript.getEnd(); j++, pos++) {
				// Intron?
				if (bases[j] == '-') {
					tested = true;

					if (testPos >= 0 && pos < testPos) {
						Gpr.debug("\tSkipping\tpos: " + pos + " [" + j + "]");
						continue;
					}

					// Ref & Alt
					String refStr = "A";
					String altStr = "T";

					SeqChange sc = new SeqChange(transcript.getChromosome(), pos, refStr, altStr, 1, "", 0, 0);

					if (transcript.isStrandMinus()) {
						refStr = GprSeq.wc(refStr);
						altStr = GprSeq.wc(altStr);
					}

					/*
					 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					 * How it should be done:
					 * 	- Calculate the distance from 'pos' to 'closest_exon_base'
					 *  - If closest_exon_base < cdsStart : Calculate the distance from closest_exon_base to cdsStart (type "-")  
					 *  - If closest_exon_base > cdsEnd   : Calculate the distance from closest_exon_base to cdsEnd   (type "*")
					 *  - Else                            : Calculate the distance from closest_exon_base to cdsStart (type "")  
					 * 
					 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					 */

					// Distance from intron to exon boundary
					int distToExon = distToExon(bases, j, transcript.getStrand());

					// Distance from exon boundary to TSS
					int distCoding = Integer.MIN_VALUE;
					String distCodingStr = "";

					if ((cdsLeft <= pos) && (pos <= cdsRight)) {
						// Intron within coding region
						distCoding = distToUtr5(bases, j, transcript.getStrand());
						distCodingStr = "";

						if (distToExon < 0) distCoding++;
					} else if (transcript.isStrandPlus() && (pos < cdsStart)) {
						// Intron within 5'UTR
						distCoding = distToCodingBase(bases, j, transcript.getStrand());
						distCodingStr = "-";

						//  "the number of the last nucleotide of the preceding exon"
						if (distToExon >= 0) distCoding++;
					} else if (transcript.isStrandPlus() && (cdsEnd < pos)) {
						// Intron within 3'UTR
						distCoding = distToCodingBase(bases, j, -transcript.getStrand()) - 1; // Find first coding base opposite to transcript direction
						distCodingStr = "*";

						if (distCoding < 0) {
							// Last CDS base
							distCoding = distToUtr5(bases, j, transcript.getStrand());
							distCodingStr = "";
						} else if (distToExon > 0) distCoding++;
					}

					String hgsv = "c." + distCodingStr + distCoding + (distToExon >= 0 ? "+" : "") + distToExon + refStr + ">" + altStr;

					// Calculate effect and compare
					ChangeEffects ceffs = snpEffectPredictor.seqChangeEffect(sc);
					ChangeEffect ceff = ceffs.get();

					String hgsvEff = ceffs.get().getHgvs();
					if (debug) System.out.println("\tpos: " + pos + " [" + j + "]\thgsv: '" + hgsv + "'\tEff: '" + hgsvEff + "'\t" + ceff.getEffectType());

					// Is this an intron? (i.e. skip other effects, such as splice site)
					if (ceff.getEffectType() == EffectType.INTRON) Assert.assertEquals(hgsv, hgsvEff);
				}
			}

			if (tested) checked++;
		}
	}
}
