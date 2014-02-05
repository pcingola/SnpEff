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
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
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
public class TestCasesIns extends TestCase {

	boolean debug = false;
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
		String args[] = { genome, vcf };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		for (VcfEntry ve : vcfEnties) {

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.parseEffects();
			VcfEffect veff = null;
			for (VcfEffect v : veffs)
				if (v.getTranscriptId().equals(trId)) veff = v;

			//---
			// Check that reported effect is the same
			//---
			String vep = ve.getInfo("EFF_V");
			String eff = veff.getEffect().toString();
			if (!vep.equals(eff)) {
				if (vep.equals("CODON_INSERTION") && eff.equals("CODON_CHANGE_PLUS_CODON_INSERTION")) ; // OK. I consider these the same
				else if (vep.equals("STOP_GAINED,CODON_INSERTION") && eff.equals("STOP_GAINED")) ; // OK. I consider these the same
				else if (eff.equals("SPLICE_SITE_REGION")) ; // OK. I'm not checking these
				else {
					String msg = "\n" + ve + "\n\tSnpEff:" + veff + "\n\tVEP   :" + ve.getInfo("EFF_V") + "\t" + ve.getInfo("AA") + "\t" + ve.getInfo("CODON");
					Gpr.debug(msg);
					throw new RuntimeException(msg);
				}
			}

			//---
			// Check that AA is the same
			//---
			String aa = veff.getAa();
			String vepaa = ve.getInfo("AA");
			if (aa == null) {
				if (vepaa.equals("-")) ; // OK, test passed
				else {
					Gpr.debug(aa + "\t" + vepaa + "\t");
				}
			} else {
				String aas[] = aa.split("[0-9]+");
				String aav = aas[0] + "/" + (aas.length > 0 ? aas[1] : "");

				// Convert from 'Q/QLV' to '-/LV'
				String aav2 = "";
				if ((aas[0].length() == 1) && (aas[1].startsWith(aas[0]))) aav2 = "-/" + aas[1].substring(1);
				if ((aas[0].length() == 1) && (aas[1].endsWith(aas[0]))) aav2 = "-/" + aas[1].substring(0, aas[1].length() - 1);

				if (aav.equals(vepaa)) ; // OK, test passed
				else if (aav2.equals(vepaa)) ; // OK, test passed
				else if (aav.endsWith("?") && vepaa.equals("-")) ; // OK, test passed
				else {
					Gpr.debug(aa + " (" + aav + ")\t" + vepaa + "\t");
				}
			}

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
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, 1, rand, maxGeneLen, maxTranscripts, maxExons);

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
		int N = 1000;
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();
			if (debug) System.out.println("INS Test iteration: " + i + "\n" + transcript);
			else System.out.println("INS Test iteration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());

			int cdsBaseNum = 0;

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				int step = exon.getStrand() >= 0 ? 1 : -1;
				int beg = exon.getStrand() >= 0 ? exon.getStart() : exon.getEnd();

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
						// int seqChangeStrand = rand.nextBoolean() ? +1 : -1;
						int seqChangeStrand = 1;
						if (seqChangeStrand == -exon.getStrand()) ins = GprSeq.reverseWc(insPlus);
						SeqChange seqChange = new SeqChange(chromosome, pos, "", "+" + ins, seqChangeStrand, "", 1.0, 1);

						// Is it an insertion?
						Assert.assertEquals(true, seqChange.isIns());

						// Codon change
						int idx = cdsCodonPos;
						if (transcript.isStrandMinus()) idx++; // Insert AFTER base (in negative strand)
						codonNew = codonOld.substring(0, idx) + insPlus + codonOld.substring(idx);
						aaNew = codonTable.aa(codonNew);

						// Expected Effect
						String effectExpected = "";
						if (insLen % 3 != 0) {
							effectExpected = "FRAME_SHIFT(" + aaOld + "/" + aaNew + ")";
						} else {
							if (cdsCodonPos == 0) {
								effectExpected = "CODON_INSERTION(" + aaOld + "/" + aaNew + ")";
							} else {
								if (codonNew.startsWith(codonOld)) effectExpected = "CODON_INSERTION(" + aaOld + "/" + aaNew + ")";
								else effectExpected = "CODON_CHANGE_PLUS_CODON_INSERTION(" + aaOld + "/" + aaNew + ")";
							}

							if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonOld) && !codonTable.isStartFirst(codonNew)) effectExpected = "START_LOST(" + aaOld + "/" + aaNew + ")";
							else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) {
								effectExpected = "STOP_LOST(" + aaOld + "/" + aaNew + ")";
							} else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) effectExpected = "STOP_GAINED(" + aaOld + "/" + aaNew + ")";
						}

						// Calculate effects
						List<ChangeEffect> effects = snpEffectPredictor.seqChangeEffect(seqChange);

						// There should be only one effect
						Assert.assertEquals(1, effects.size());

						// Show
						ChangeEffect effect = effects.get(0);
						String effStr = effect.effect(true, true, true, false);
						if (debug) System.out.println("\tPos: " + pos //
								+ "\tCDS base num: " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]" //
								+ "\t" + seqChange + "\tstrand" + (seqChange.getStrand() >= 0 ? "+" : "-") //
								+ "\tCodon: " + codonOld + " -> " + codonNew //
								+ "\tAA: " + aaOld + " -> " + aaNew //
								+ "\tEffect: " + effStr //
								+ "\tEffect expected: " + effectExpected //
						);

						// Check effect
						Assert.assertEquals(effectExpected, effStr);
					}
				}
			}
		}
	}

	/**
	 * Insertion on minus strand
	 */
	public void test_02_InsOffByOne() {
		String args[] = { "testENST00000268124", "tests/ins_off_by_one.vcf" };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();

		List<VcfEntry> vcfEnties = snpeff.run(true);
		for (VcfEntry ve : vcfEnties) {

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.parseEffects();
			VcfEffect veff = veffs.get(0);

			Assert.assertEquals("Q53QQ", veff.getAa());
		}
	}

	public void test_03_InsVep() {
		compareVep("testENST00000268124", "tests/testENST00000268124_ins_vep.vcf", "ENST00000268124");
	}

	public void test_04_InsVep() {
		compareVep("testHg3770Chr22", "tests/testENST00000445220_ins_vep.vcf", "ENST00000445220");
	}

}
