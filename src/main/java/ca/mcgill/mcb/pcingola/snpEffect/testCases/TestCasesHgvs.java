package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test random SNP changes 
 * 
 * @author pcingola
 */
public class TestCasesHgvs extends TestCase {

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

	public TestCasesHgvs() {
		super();
		init();
	}

	void init() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(20130708);
	}

	/**
	 * Create a predictor
	 */
	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);

		// Create predictor
		sepf.setForcePositive(true); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
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
			String hgvs = vcfEntry.getInfo("HGVS");

			if (debug) System.err.println(entryNum + "\t" + vcfEntry);

			for (VcfEffect eff : vcfEntry.parseEffects()) {
				if (debug) System.err.println("\tHGVS: " + hgvs + "\tAA: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff);
				if (hgvs.equals(eff.getAa())) {
					if (debug) System.err.println("\tFOUND!");
					found = true;
				}
			}

			// Not found? Error
			if (!found) throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
			entryNum++;
		}
	}

	//	public void test_01_coding() {
	//		int N = 250;
	//		CodonTable codonTable = genome.codonTable();
	//
	//		// Test N times
	//		//	- Create a random gene transcript, exons
	//		//	- Change each base in the exon
	//		//	- Calculate effect
	//		for (int i = 0; i < N; i++) {
	//			initSnpEffPredictor();
	//			if (debug) System.out.println("HGSV Test iteration: " + i + "\n" + transcript);
	//			else System.out.println("HGSV Test iteration: " + i + "\t" + (transcript.getStrand() >= 0 ? "+" : "-") + "\t" + transcript.cds());
	//
	//			int cdsBaseNum = 0;
	//
	//			// For each exon...
	//			for (Exon exon : transcript.sortedStrand()) {
	//				// For each base in this exon...
	//				for (int pos = exon.getStart(); (exon.getStart() <= pos) && (pos <= exon.getEnd()); pos++, cdsBaseNum++) {
	//
	//					// Reference base
	//					char refBase = chromoBases[pos]; // exon.basesAt(pos - exon.getStart(), 1).charAt(0);
	//					refBase = Character.toUpperCase(refBase);
	//					// Codon number
	//					int cdsCodonNum = cdsBaseNum / 3;
	//					int cdsCodonPos = cdsBaseNum % 3;
	//
	//					int minCodonPos = cdsCodonNum * 3;
	//					int maxCodonPos = minCodonPos + 3;
	//					if (maxCodonPos < transcript.cds().length()) {
	//						String codon = transcript.cds().substring(minCodonPos, maxCodonPos);
	//						codon = codon.toUpperCase();
	//						String aa = codonTable.aaThreeLetterCode(codonTable.aa(codon));
	//
	//						// Get a random base different from 'refBase'
	//						char snp = refBase;
	//						while (snp == refBase)
	//							snp = Character.toUpperCase(GprSeq.randBase(rand));
	//
	//						// Codon change
	//						String newCodon = codon.substring(0, cdsCodonPos) + snp + codon.substring(cdsCodonPos + 1);
	//						String newAa = codonTable.aaThreeLetterCode(codonTable.aa(newCodon));
	//						String protHgvs = "";
	//						String dnaHgvs = "c." + (cdsBaseNum + 1) + refBase + ">" + snp;
	//
	//						// Effect
	//						if (newAa.equals(aa)) {
	//							if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
	//								if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
	//								else protHgvs = "p." + aa + "1?";
	//							} else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
	//						} else {
	//							if ((cdsCodonNum == 0) && (codonTable.isStart(codon))) {
	//								if (codonTable.isStart(newCodon)) protHgvs = "p." + aa + "1?";
	//								else protHgvs = "p." + aa + "1?";
	//							} else if (codonTable.isStop(codon)) protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa + "ext*?";
	//							else if (codonTable.isStop(newCodon)) protHgvs = "p." + aa + (cdsCodonNum + 1) + "*";
	//							else protHgvs = "p." + aa + (cdsCodonNum + 1) + newAa;
	//						}
	//
	//						String effectExpected = protHgvs + "/" + dnaHgvs;
	//
	//						// Create a SeqChange
	//						SeqChange seqChange = new SeqChange(chromosome, pos, refBase + "", snp + "", 1, "", 1.0, 1);
	//
	//						if (!seqChange.isChange()) protHgvs = "EXON";
	//
	//						// Calculate effects
	//						ChangeEffects effects = snpEffectPredictor.seqChangeEffect(seqChange);
	//
	//						// There should be only one effect
	//						Assert.assertEquals(true, effects.size() <= 1);
	//
	//						// Show
	//						if (effects.size() == 1) {
	//							ChangeEffect effect = effects.get();
	//							String effStr = effect.getHgvs();
	//
	//							if (debug) System.out.println("\tPos: " + pos //
	//									+ "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
	//									+ "\t" + seqChange + (seqChange.getStrand() >= 0 ? "+" : "-") //
	//									+ "\tCodon: " + codon + " -> " + newCodon //
	//									+ "\tAA: " + aa + " -> " + newAa //
	//									+ "\tEffect expected: " + effectExpected //
	//									+ "\tEffect: " + effStr);
	//
	//							// Check effect
	//							Assert.assertEquals(effectExpected, effStr);
	//
	//							// Check that effect can be inserted into a VCF field
	//							if (!VcfEntry.isValidInfoValue(effStr)) {
	//								String err = "No white-space, semi-colons, or equals-signs are permitted in INFO field. Value:\"" + effStr + "\"";
	//								System.err.println(err);
	//								throw new RuntimeException(err);
	//							}
	//
	//						}
	//					}
	//				}
	//			}
	//		}
	//	}
	//
	//	public void test_02() {
	//		snpEffect("tests/hgvs_1.vcf", "testHg3766Chr1");
	//	}

	public void test_023_intron() {
		snpEffect("tests/ensembl_hgvs_intron.vcf", "testHg3771Chr1");
		//		snpEffect("tests/ensembl_hgvs_intron.1.vcf", "testHg3771Chr1");
	}

}
