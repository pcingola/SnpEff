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
	boolean verbose = false;

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

	int distToTss(char bases[], int j, int direction) {
		for (int count = 0; (j >= 0) && (j < bases.length); j += direction) {
			if ((bases[j] == '>') || (bases[j] == '<')) return count;
			if (bases[j] != '-') count++;
		}

		throw new RuntimeException("This should never happen!");
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

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);

		// Create predictor
		sepf.setForcePositive(true); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
		sepf.setAddUtrs(addUtrs);
		snpEffectPredictor = sepf.create();
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
		int N = 10;

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N;) {
			initSnpEffPredictor(true);
			//			if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
			//			else System.out.println("HGSV Test iteration: " + i + "\t" + (transcript.getStrand() >= 0 ? "+" : "-") + "\t" + transcript.cds());

			boolean tested = false;

			// No introns? Nothing to test
			if (transcript.introns().size() < 1) continue;

			if (transcript.isStrandMinus()) {
				Gpr.debug("!!!!!!!!!!!!!!!!!!!!");
				continue;
			}

			// Character representation
			String trstr = transcript.toStringAsciiArt();
			char bases[] = trstr.toCharArray();

			int cdsStart = transcript.getCdsStart();
			System.out.println(trstr);
			System.out.println("Length   : " + transcript.size());
			System.out.println("CDS start: " + cdsStart);
			System.out.println("CDS end  : " + transcript.getCdsEnd());
			System.out.println(transcript);

			for (int j = 0, pos = transcript.getStart(); pos < cdsStart; j++, pos++) {
				// Intron?
				if (bases[j] == '-') {

					if (transcript.cds().equals("accgtgctcaggggggtttcatgatcatctcacggaaatgtggggttagggacaagaacttatgcggtcta")) //
						Gpr.debug("!!!!!!!!!!!!!!!");

					tested = true;

					// Ref & Alt
					String refStr = "A";
					String altStr = "T";

					SeqChange sc = new SeqChange(transcript.getChromosome(), pos, refStr, altStr, 1, "", 0, 0);

					if (transcript.isStrandMinus()) {
						refStr = GprSeq.wc(refStr);
						altStr = GprSeq.wc(altStr);
					}

					// Find distances
					int distToExon = distToExon(bases, j, transcript.getStrand());
					int distToTss = distToTss(bases, j, transcript.getStrand());

					//  "the number of the last nucleotide of the preceding exon"
					if (distToExon >= 0) distToTss++;

					String hgsv = "c.-" + distToTss + (distToExon >= 0 ? "+" : "") + distToExon + refStr + ">" + altStr;

					// Calculate effect and compare
					ChangeEffects ceffs = snpEffectPredictor.seqChangeEffect(sc);
					ChangeEffect ceff = ceffs.get();

					String hgsvEff = ceffs.get().getHgvs();
					System.out.println("\t" + pos + " [" + j + "]\tdistToExon: " + distToExon + "\tdistToTss: " + distToTss + "\thgsv: '" + hgsv + "'\tEffs: '" + hgsvEff + "'");

					// Is this an intron? (i.e. skip other effects, such as splice site)
					if (ceff.getEffectType() == EffectType.INTRON) Assert.assertEquals(hgsv, hgsvEff);
				}
			}

			if (tested) i++;
		}
	}
}
