package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.List;
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
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
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
 * Test random SNP changes 
 * 
 * @author pcingola
 */
public class TestCasesHgvs extends TestCase {

	boolean debug = false;
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

	public TestCasesHgvs() {
		super();
		init();
	}

	/**
	 * Count how many bases are there until the exon
	 * @param bases
	 * @param pos
	 * @return
	 */
	int exonBase(char bases[], int pos, int direction) {
		int countAfter = 0, countBefore = 0;
		int posBefore, posAfter;
		for (posAfter = pos; (posAfter >= 0) && (posAfter < bases.length); countAfter++, posAfter += direction)
			if (bases[posAfter] != '-') break;

		for (posBefore = pos; (posBefore >= 0) && (posBefore < bases.length); countBefore++, posBefore -= direction)
			if (bases[posBefore] != '-') break;

		if (countBefore <= countAfter) return posBefore;
		return posAfter;
	}

	void init() {
		initRand();
		initSnpEffPredictor(false, true);
	}

	void initRand() {
		rand = new Random(20130708);
	}

	/**
	 * Create a predictor
	 */
	void initSnpEffPredictor(boolean addUtrs, boolean onlyPlusStrand) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Initialize factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(onlyPlusStrand); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
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
	 * Intronic HGS notation
	 * 
	 * @param bases
	 * @param j
	 * @param pos
	 * @param refStr
	 * @param altStr
	 * @return
	 */
	String intronHgsv(char bases[], int j, int pos, String refStr, String altStr) {
		if (transcript.isStrandMinus()) {
			refStr = GprSeq.wc(refStr);
			altStr = GprSeq.wc(altStr);
		}

		// Closest exon base
		int exonBase = exonBase(bases, j, transcript.isStrandMinus() ? -1 : 1);
		int exonDist = (j - exonBase) * (transcript.isStrandMinus() ? -1 : 1);

		char type = bases[exonBase];
		String typeStr = "";
		int basesCount = 0;
		int step = transcript.isStrandPlus() ? 1 : -1;
		if (type == '5') {
			typeStr = "-";

			// Count UTR5 bases until TSS
			for (int i = exonBase; (i >= 0) && (i < bases.length); i += step) {
				if (bases[i] == type) basesCount++;
				else if (bases[i] != '-') break;
			}

		} else if (type == '3') {
			typeStr = "*";

			// Count UTR3 bases until end of coding 
			for (int i = exonBase; (i >= 0) && (i < bases.length); i -= step) {
				if (bases[i] == type) basesCount++;
				else if (bases[i] != '-') break;
			}
		} else if ((type == '>') || (type == '<')) {
			// Count coding bases until TSS
			for (int i = exonBase; (i >= 0) && (i < bases.length); i -= step) {
				if (bases[i] == type) basesCount++;
				else if ((bases[i] != '-') && (bases[i] != '>') && (bases[i] != '<')) break;
			}
		} else throw new RuntimeException("Unexpected base type '" + bases[exonBase] + "'");

		return "c." //
				+ typeStr //
				+ basesCount //
				+ (exonDist >= 0 ? "+" : "") + exonDist //
				+ refStr + ">" + altStr;
	}

	/**
	 * Run SnpEff on VCF file
	 * @param vcfFile
	 */
	public void snpEffect(String vcfFile, String genomeVer) {
		// Create command
		String args[] = { "-classic", "-hgvs", "-ud", "0", genomeVer, vcfFile };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		// Run command
		List<VcfEntry> list = cmdEff.run(true);

		// Find HGVS in any 'EFF' field
		int entryNum = 1;
		for (VcfEntry vcfEntry : list) {
			boolean found = false;

			// Load hgvs expexcted annotations into set
			String hgvsStr = vcfEntry.getInfo("HGVS");
			HashSet<String> hgvsExpected = new HashSet<String>();
			for (String h : hgvsStr.split(",")) {
				if (h.indexOf(':') > 0) h = h.substring(h.indexOf(':') + 1);
				hgvsExpected.add(h);
			}

			if (debug) System.err.println(entryNum + "\t" + vcfEntry);

			// Find if HGVS predicted by SnpEff matches expected annotations
			StringBuilder sb = new StringBuilder();
			for (VcfEffect eff : vcfEntry.parseEffects()) {
				String hgvsReal = eff.getAa();
				String line = "\tHGVS: " + hgvsExpected.contains(hgvsReal) + "\tExpected: " + hgvsExpected + "\tSnpEFf: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff;
				sb.append(line + "\n");
				if (debug) System.err.println(line);
				if (hgvsExpected.contains(hgvsReal)) found = true;
			}

			// Not found? Error
			if (!found) {
				System.err.println("HGVS not found in variant\n" + vcfEntry + "\n" + sb);
				// throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
			}
			entryNum++;
		}
	}

	public void test_01_coding() {
		Gpr.debug("Test");
		int N = 250;
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor(false, true);
			if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
			else System.out.println("HGSV Coding\titeration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());

			int cdsBaseNum = 0;

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				// For each base in this exon...
				for (int pos = exon.getStart(); (exon.getStart() <= pos) && (pos <= exon.getEnd()); pos++, cdsBaseNum++) {

					// Reference base
					char refBase = chromoBases[pos]; // exon.basesAt(pos - exon.getStart(), 1).charAt(0);
					refBase = Character.toUpperCase(refBase);
					// Codon number
					int cdsCodonNum = cdsBaseNum / 3;
					int cdsCodonPos = cdsBaseNum % 3;

					int minCodonPos = cdsCodonNum * 3;
					int maxCodonPos = minCodonPos + 3;
					if (maxCodonPos < transcript.cds().length()) {
						String codon = transcript.cds().substring(minCodonPos, maxCodonPos);
						codon = codon.toUpperCase();
						String aa = codonTable.aaThreeLetterCode(codonTable.aa(codon));

						// Get a random base different from 'refBase'
						char snp = refBase;
						while (snp == refBase)
							snp = Character.toUpperCase(GprSeq.randBase(rand));

						// Codon change
						String newCodon = codon.substring(0, cdsCodonPos) + snp + codon.substring(cdsCodonPos + 1);
						String newAa = codonTable.aaThreeLetterCode(codonTable.aa(newCodon));
						String protHgvs = "";
						String dnaHgvs = "c." + (cdsBaseNum + 1) + refBase + ">" + snp;

						// Effect
						if (newAa.equals(aa)) {
							if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
								if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
								else protHgvs = "p." + aa + "1?";
							} else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
						} else {
							if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
								if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
								else protHgvs = "p." + aa + "1?";
							} else if (codonTable.isStop(codon)) protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa + "ext*?";
							else if (codonTable.isStop(newCodon)) protHgvs = "p." + aa + (cdsCodonNum + 1) + "*";
							else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
						}

						String effectExpected = protHgvs + "/" + dnaHgvs;

						// Create a SeqChange
						Variant seqChange = new Variant(chromosome, pos, refBase + "", snp + "", "");

						if (!seqChange.isVariant()) protHgvs = "EXON";

						// Calculate effects
						VariantEffects effects = snpEffectPredictor.variantEffect(seqChange);

						// There should be only one effect
						Assert.assertEquals(true, effects.size() <= 1);

						// Show
						if (effects.size() == 1) {
							VariantEffect effect = effects.get();
							String effStr = effect.getHgvs();

							if (debug) System.out.println("\tPos: " + pos //
									+ "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
									+ "\t" + seqChange + (seqChange.isStrandPlus() ? "+" : "-") //
									+ "\tCodon: " + codon + " -> " + newCodon //
									+ "\tAA: " + aa + " -> " + newAa //
									+ "\tEffect expected: " + effectExpected //
									+ "\tEffect: " + effStr);

							// Check effect
							Assert.assertEquals(effectExpected, effStr);

							// Check that effect can be inserted into a VCF field
							if (!VcfEntry.isValidInfoValue(effStr)) {
								String err = "No white-space, semi-colons, or equals-signs are permitted in INFO field. Value:\"" + effStr + "\"";
								System.err.println(err);
								throw new RuntimeException(err);
							}

						}
					}
				}
			}
		}
	}

	public void test_02() {
		Gpr.debug("Test");
		snpEffect("tests/hgvs_1.vcf", "testHg3766Chr1");
	}

	public void test_03_intron_withinCds() {
		Gpr.debug("Test");
		snpEffect("tests/ensembl_hgvs_intron.within_cds.vcf", "testHg3775Chr1");
	}

	public void test_04_intron_outsideCds() {
		Gpr.debug("Test");
		snpEffect("tests/ensembl_hgvs_intron.outsideCds.vcf", "testHg3775Chr1");
	}

	public void test_05_intron() {
		Gpr.debug("Test");
		int N = 250;

		int testIter = -1;
		int testPos = -1;

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int checked = 0, it = 1; checked < N; it++) {
			initSnpEffPredictor(true, false);
			boolean tested = false;

			// Skip test?
			if (testIter >= 0 && it < testIter) {
				Gpr.debug("Skipping iteration: " + it);
				continue;
			}

			// No introns? Nothing to test
			if (transcript.introns().size() < 1) continue;

			// Character representation
			String trstr = transcript.toStringAsciiArt();
			char bases[] = trstr.toCharArray();

			// Show data
			System.out.println("HGSV Intron\titeration:" + checked + "\t" + (transcript.isStrandPlus() ? "+" : "-"));
			if (verbose) {
				System.out.println(trstr);
				System.out.println("Length   : " + transcript.size());
				System.out.println("CDS start: " + transcript.getCdsStart());
				System.out.println("CDS end  : " + transcript.getCdsEnd());
				System.out.println(transcript);
			}

			// Check each intronic base
			for (int j = 0, pos = transcript.getStart(); pos < transcript.getEnd(); j++, pos++) {
				// Intron?
				if (bases[j] == '-') {
					tested = true;

					// Skip base?
					if (testPos >= 0 && pos < testPos) {
						Gpr.debug("\tSkipping\tpos: " + pos + " [" + j + "]");
						continue;
					}

					// Ref & Alt
					String refStr = "A", altStr = "T";

					// Calculate expected hgsv string 
					String hgsv = intronHgsv(bases, j, pos, refStr, altStr);

					// Calculate effect and compare to expected
					Variant sc = new Variant(transcript.getChromosome(), pos, refStr, altStr, "");
					VariantEffects ceffs = snpEffectPredictor.variantEffect(sc);
					VariantEffect ceff = ceffs.get();
					String hgsvEff = ceffs.get().getHgvs();
					if (debug) System.out.println("\tpos: " + pos + " [" + j + "]\thgsv: '" + hgsv + "'\tEff: '" + hgsvEff + "'\t" + ceff.getEffectType());

					// Is this an intron? (i.e. skip other effects, such as splice site)
					// Compare expected to real HGSV strings
					if (ceff.getEffectType() == EffectType.INTRON) Assert.assertEquals(hgsv, hgsvEff);
				}
			}

			if (tested) checked++;
		}
	}
}
