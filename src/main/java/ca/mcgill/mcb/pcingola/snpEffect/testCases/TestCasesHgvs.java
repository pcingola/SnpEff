package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Assert;

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

	boolean debug = true;
	boolean verbose = true || debug;
	boolean skipLong = false;

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
	 */
	public void snpEffect(String genomeVer, String vcfFile) {
		// Create command
		String args[] = { "-classic", "-hgvs", "-ud", "0", genomeVer, vcfFile };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);

		// Find HGVS in any 'EFF' field
		int entryNum = 1;
		for (VcfEntry vcfEntry : list) {
			boolean found = false;

			// Load hgvs expexcted annotations into set
			String hgvsStr = vcfEntry.getInfo("HGVS");
			String trId = vcfEntry.getInfo("TR");
			HashSet<String> hgvsExpected = new HashSet<String>();
			for (String h : hgvsStr.split(",")) {
				if (h.indexOf(':') > 0) h = h.substring(h.indexOf(':') + 1);
				hgvsExpected.add(h);
			}

			if (debug) System.err.println(entryNum + "\t" + vcfEntry);

			// Find if HGVS predicted by SnpEff matches expected annotations
			StringBuilder sb = new StringBuilder();
			for (VcfEffect eff : vcfEntry.parseEffects()) {
				if (trId != null && !trId.isEmpty() && trId.equals(eff.getTranscriptId())) {
					String hgvsReal = eff.getAa();
					String line = "\tHGVS: " + hgvsExpected.contains(hgvsReal) + "\tExpected: " + hgvsExpected + "\tSnpEFf: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff;
					sb.append(line + "\n");

					if (debug) System.err.println(line);
					if (hgvsExpected.contains(hgvsReal)) found = true;
				}
			}

			// Not found? Error
			if (!found) {
				System.err.println("HGVS not found in variant\n" + vcfEntry + "\n" + sb);
				throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
			}
			entryNum++;
		}
	}

	public void test_01_coding() {
		Gpr.debug("Test");
		int N = 250;
		CodonTable codonTable = genome.codonTable();

		if (skipLong) throw new RuntimeException("Test skipped!");

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor(false, true);
			if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
			else if (verbose) System.out.println("HGSV Coding\titeration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);

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
		System.err.println("");
	}

	public void test_01_intron() {
		Gpr.debug("Test");
		int N = 250;

		int testIter = -1;
		int testPos = -1;

		if (skipLong) throw new RuntimeException("Test skipped!");

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
			if (verbose) {
				System.out.println("HGSV Intron\titeration:" + checked + "\t" + (transcript.isStrandPlus() ? "+" : "-"));
				System.out.println(trstr);
				System.out.println("Length   : " + transcript.size());
				System.out.println("CDS start: " + transcript.getCdsStart());
				System.out.println("CDS end  : " + transcript.getCdsEnd());
				System.out.println(transcript);
			} else Gpr.showMark(it, 1);

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
		System.err.println("");
	}

	public void test_02() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/hgvs_1.vep.vcf";
		CompareToVep comp = new CompareToVep(genomeName, verbose);
		comp.setCompareHgvs();
		//		comp.setStrict(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_03() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/ensembl_hgvs_intron.1.vep.vcf";
		CompareToVep comp = new CompareToVep(genomeName, verbose);
		comp.setCompareHgvs();
		comp.setStrict(true);
		comp.setOnlyProtein(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_04() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/ensembl_hgvs_intron.outsideCds.vep.vcf";
		CompareToVep comp = new CompareToVep(genomeName, verbose);
		comp.setCompareHgvs();
		comp.setStrict(true);
		comp.setOnlyProtein(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_05() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/ensembl_hgvs_intron.vep.vcf";
		CompareToVep comp = new CompareToVep(genomeName, verbose);
		comp.setCompareHgvs();
		comp.setStrict(true);
		comp.setOnlyProtein(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_06() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/ensembl_hgvs_intron.within_cds.vep.vcf";
		CompareToVep comp = new CompareToVep(genomeName, verbose);
		comp.setCompareHgvs();
		comp.setStrict(true);
		comp.setOnlyProtein(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_10_MixedVep_HGVS() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr1";
		String vcf = "tests/mixed_10_hgvs.vep.vcf";
		CompareToVep comp = new CompareToVep(genome, verbose);
		comp.setCompareHgvs();
		comp.setOnlyProtein(true);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	public void test_11_Hg19Hgvs() {
		Gpr.debug("Test");
		String genome = "testHg19Hgvs";
		String vcf = "tests/hgvs_counsyl.vcf";
		CompareToVep comp = new CompareToVep(genome, verbose);
		comp.setCompareHgvs();
		comp.setCompareHgvsProt(false);
		comp.compareVep(vcf);
		System.out.println(comp);
		Assert.assertTrue("No comparissons were made!", comp.checkComapred());
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_12_BRCA_Splice_15_Hgvs() {
		Gpr.debug("Test");
		int spliceSize = 15;
		String genome = "test_BRCA";
		String vcf = "tests/test_BRCA_splice_15.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// The problem appears when splice site is large (in this example)
		snpeff.setSpliceSiteSize(spliceSize);
		snpeff.setUpDownStreamLength(0);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure the spleice site is annotatted as "c.1909+12delT" (instead of "c.1910delT")
		boolean ok = false;
		for (VcfEffect veff : ve.parseEffects())
			ok |= veff.getTranscriptId().equals("ENST00000544455") && veff.getHgvsDna().equals("c.1909+12delT");

		Assert.assertTrue(ok);
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_13_large_Del_Hgvs() {
		Gpr.debug("Test");
		String genome = "testHg3775Chr22";
		String vcf = "tests/test_large_del_hgvs_13.vcf";

		// Create SnpEff
		String args[] = { genome, vcf };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure HGVS string is not so long
		for (VcfEffect veff : ve.parseEffects()) {
			System.out.println(veff);

			System.out.println("\tAA change    : " + veff.getAa());
			Assert.assertTrue(veff.getAa() == null || veff.getAa().length() < 100);

			System.out.println("\tCodon change : " + veff.getCodon());
			Assert.assertTrue(veff.getCodon() == null || veff.getCodon().length() < 100);

		}
	}

	/**
	 * Using non-standard splice size (15 instead of 2)
	 * may cause some HGVS annotations issues
	 */
	public void test_14_splice_region_Hgvs() {
		Gpr.debug("Test");
		String genome = "testHg19Chr1";
		String vcf = "tests/hgvs_splice_region.vcf";

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
		List<VcfEntry> results = snpeff.run(true);
		VcfEntry ve = results.get(0);

		// Make sure the spleice site is annotatted as "c.1909+12delT" (instead of "c.1910delT")
		boolean ok = false;
		for (VcfEffect veff : ve.parseEffects()) {
			if (verbose) System.out.println("\t" + veff + "\t" + veff.getEffectsStr() + "\t" + veff.getHgvsDna());
			ok |= veff.getEffectsStr().equals("SPLICE_SITE_REGION") //
					&& veff.getTranscriptId().equals("NM_001232.3") //
					&& veff.getHgvsDna().equals("c.420+6T>C");
		}

		Assert.assertTrue(ok);
	}

}
