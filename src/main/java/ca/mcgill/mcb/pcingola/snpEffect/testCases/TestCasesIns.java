package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.IOException;
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
public class TestCasesIns extends TestCase {

	public static int N = 1000;

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public static void create_ENST00000268124_file() throws IOException {
		Config config = new Config("testENST00000268124", Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140129);
		StringBuilder out = new StringBuilder();

		int count = 0;
		for (Gene g : config.getGenome().getGenes()) {
			for (Transcript tr : g) {
				for (Exon e : tr) {
					for (int i = e.getStart(); i < e.getEnd(); i++) {
						if (rand.nextDouble() < 0.15) {

							// Insertion length
							int insLen = rand.nextInt(10) + 1;
							if (i + insLen > e.getEnd()) insLen = e.getEnd() - i;

							int idx = i - e.getStart();

							String ref = e.basesAt(idx, 1);
							String alt = ref + GprSeq.randSequence(rand, insLen);

							String line = e.getChromosomeName() + "\t" + i + "\t.\t" + ref + "\t" + alt + "\t.\t.\tAC=1\tGT\t0/1";
							System.out.println(line);
							out.append(line + "\n");
							count++;
						}
					}
				}
			}
		}

		System.err.println("Count:" + count);
		Gpr.toFile(Gpr.HOME + "/snpEff/testENST00000268124.vcf", out);
	}

	public TestCasesIns() {
		super();
		init();
	}

	/**
	 * Compare with results from ENSEMBL's VEP on transcript ENST00000268124
	 */
	public void compareVep(String genome, String vcf, String trId) {
		String args[] = { "-classic", genome, vcf };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		for (VcfEntry ve : vcfEnties) {

			StringBuilder msg = new StringBuilder();

			// Check effects
			boolean ok = false;
			for (VcfEffect veff : ve.parseEffects()) {
				// Find transcript
				if (veff.getTranscriptId().equals(trId)) {
					// Check that reported effect is the same
					String vep = ve.getInfo("EFF_V");
					String eff = veff.getEffectType().toString();

					if (vep.equals(eff)) ok = true;
					else {
						if (vep.equals("CODON_INSERTION") && eff.equals("CODON_CHANGE_PLUS_CODON_INSERTION")) ok = true; // OK. I consider these the same
						else if (vep.equals("STOP_GAINED,CODON_INSERTION") && eff.equals("STOP_GAINED")) ok = true; // OK. I consider these the same
						else if (eff.equals("SPLICE_SITE_REGION")) ok = true; // OK. I'm not checking these
						else {
							String line = "\n" + ve + "\n\tSnpEff:" + veff + "\n\tVEP   :" + ve.getInfo("EFF_V") + "\t" + ve.getInfo("AA") + "\t" + ve.getInfo("CODON") + "\n";
							msg.append(line);
						}
					}
				}
			}

			if (!ok) throw new RuntimeException(msg.toString());
		}
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

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// Create predictor
		snpEffectPredictor = sepf.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
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
			if (debug) System.out.println("INS Test iteration: " + i + "\n" + transcript);
			else if (verbose) System.out.println("INS Test iteration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);

			int cdsBaseNum = 0;

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				int step = exon.isStrandPlus() ? 1 : -1;
				int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

				// For each base in this exon...
				for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {

					// Get a random base different from 'refBase'
					int insLen = rand.nextInt(10) + 1;
					String insPlus = GprSeq.randSequence(rand, insLen); // Insertion (plus strand)
					String ins = insPlus;

					// Codon number
					int cdsCodonNum = cdsBaseNum / 3;
					int cdsCodonPos = cdsBaseNum % 3;

					int minCodonPos = cdsCodonNum * 3;
					int maxCodonPos = minCodonPos + 3;
					if (maxCodonPos < transcript.cds().length()) {
						String codonOld = transcript.cds().substring(minCodonPos, maxCodonPos);
						codonOld = codonOld.toUpperCase();
						String aaOld = codonTable.aa(codonOld);

						// Codon change
						String codonNew = "", aaNew = "";

						// Create a SeqChange
						if (exon.isStrandMinus()) ins = GprSeq.reverseWc(insPlus);
						Variant variant = new Variant(chromosome, pos, "", "+" + ins, "");

						// Is it an insertion?
						Assert.assertEquals(true, variant.isIns());

						// Codon change
						int idx = cdsCodonPos;
						if (transcript.isStrandMinus()) idx++; // Insert AFTER base (in negative strand)
						codonNew = codonOld.substring(0, idx) + insPlus + codonOld.substring(idx);
						aaNew = codonTable.aa(codonNew);

						// Expected Effect
						String effectExpected = "";
						String aaExpected = "";
						if (insLen % 3 != 0) {
							effectExpected = "FRAME_SHIFT";
							aaExpected = aaOld + "/" + aaNew;
						} else {
							if (cdsCodonPos == 0) {
								effectExpected = "CODON_INSERTION";
								aaExpected = aaOld + "/" + aaNew;
							} else {
								if (codonNew.startsWith(codonOld)) {
									effectExpected = "CODON_INSERTION";
									aaExpected = aaOld + "/" + aaNew;
								} else {
									effectExpected = "CODON_CHANGE_PLUS_CODON_INSERTION";
									aaExpected = aaOld + "/" + aaNew;
								}
							}

							if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonOld) && !codonTable.isStartFirst(codonNew)) {
								effectExpected = "START_LOST";
								aaExpected = aaOld + "/" + aaNew;
							} else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) {
								effectExpected = "STOP_LOST";
								aaExpected = aaOld + "/" + aaNew;
							} else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) {
								effectExpected = "STOP_GAINED";
								aaExpected = aaOld + "/" + aaNew;
							}
						}

						// Calculate effects
						VariantEffects effects = snpEffectPredictor.variantEffect(variant);

						// There should be at least one effect
						Assert.assertTrue(effects.size() > 0);

						// Show
						boolean ok = false;
						for (VariantEffect effect : effects) {
							String effFullStr = effect.effect(true, true, false, false);
							String effStr = effect.effect(true, false, false, false);
							String aaStr = effect.getAaChangeOld();

							if (debug) System.out.println("\tPos: " + pos //
									+ "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
									+ "\t" + variant + "\tstrand" + (variant.isStrandPlus() ? "+" : "-") //
									+ "\tCodon: " + codonOld + " -> " + codonNew //
									+ "\tAA: " + aaOld + " -> " + aaNew //
									+ "\n\t\tEffect          : '" + effStr + "'\t'" + effFullStr + "'" //
									+ "\n\t\tEffect expected : '" + effectExpected + "'" //
									+ "\n\t\tAA              : '" + aaStr + "'" //
									+ "\n\t\tAA expected     : '" + aaExpected + "'" //
							);

							// Check that there is a match
							for (String e : effStr.split("\\+"))
								if (e.equals(effectExpected) && aaStr.equals(aaExpected)) ok = true;
						}

						// Check effect
						Assert.assertTrue(ok);
					}
				}
			}
		}
	}

	//	/**
	//	 * Insertion on minus strand
	//	 */
	//	public void test_02_InsOffByOne() {
	//		Gpr.debug("Test");
	//		String args[] = { "-classic", "testENST00000268124", "tests/ins_off_by_one.vcf" };
	//
	//		SnpEff cmd = new SnpEff(args);
	//		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
	//		snpeff.setVerbose(verbose);
	//
	//		List<VcfEntry> vcfEnties = snpeff.run(true);
	//		for (VcfEntry ve : vcfEnties) {
	//
	//			// Get first effect (there should be only one)
	//			List<VcfEffect> veffs = ve.parseEffects();
	//			VcfEffect veff = veffs.get(0);
	//
	//			Assert.assertEquals("Q53QQ", veff.getAa());
	//		}
	//	}
	//
	//	public void test_03_InsVep() {
	//		Gpr.debug("Test");
	//		compareVep("testENST00000268124", "tests/testENST00000268124_ins_vep.vcf", "ENST00000268124");
	//	}
	//
	//	public void test_04_InsVep() {
	//		Gpr.debug("Test");
	//		compareVep("testHg3770Chr22", "tests/testENST00000445220_ins_vep.vcf", "ENST00000445220");
	//	}

}
