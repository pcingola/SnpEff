package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.Serializable;
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
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
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

class Save implements Serializable {

	private static final long serialVersionUID = 3888380698995710933L;

	public SnpEffectPredictor snpEffectPredictor;
	public String chromoSequence;
	public String chromoNewSequence;
	public String ref;
	public String mnp;

	public int pos;
	public int i;
}

/**
 * Test random SNP changes 
 * 
 * @author pcingola
 */
public class TestCasesMnp extends TestCase {

	static boolean debug = false;
	static int MAX_MNP_LEN = 10;

	// Create factory
	int maxGeneLen = 1000;
	int maxTranscripts = 1;
	int maxExons = 5;

	Random rand;
	Config config;
	Genome genome;
	CodonTable codonTable;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript, transcriptNew;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	String chromoNewSequence = "";
	char chromoBases[];

	String codonsOld = "";
	String codonsNew = "";

	public TestCasesMnp() {
		super();
		init();
	}

	void addIfDiff(char codonOld[], char codonNew[]) {
		String cold = new String(codonOld);
		String cnew = new String(codonNew);
		if (!cold.equals(cnew)) {
			codonsOld += transcript.isStrandPlus() ? cold : GprSeq.wc(cold);
			codonsNew += transcript.isStrandPlus() ? cnew : GprSeq.wc(cnew);
		}
	}

	void analyze(int i, int pos, String ref, String mnp) {
		String codons = codons();

		// Create a SeqChange
		int seqChangeStrand = +1;
		Variant seqChange = new Variant(chromosome, pos, ref + "", mnp + "", "");

		//---
		// Calculate effects
		//---
		VariantEffects effects = snpEffectPredictor.variantEffect(seqChange);

		// Show
		VariantEffect effect = null;
		if (effects.size() > 1) { // Usually there is only one effect
			for (VariantEffect ce : effects) {
				if ((ce.getEffectType() != EffectType.SPLICE_SITE_ACCEPTOR) //
						&& (ce.getEffectType() != EffectType.SPLICE_SITE_DONOR) //
						&& (ce.getEffectType() != EffectType.INTRON) //
						&& (ce.getEffectType() != EffectType.INTERGENIC) //
				) //
					effect = ce;
			}
		} else effect = effects.get();

		if (effect != null) {
			String effStr = effect.effect(true, true, true, false);

			if (codons.length() > 1) {
				String codonsExp[] = codons.split("/");

				boolean error = (!codonsExp[0].toUpperCase().equals(effect.getCodonsOld().toUpperCase()) //
				|| !codonsExp[1].toUpperCase().equals(effect.getCodonsNew().toUpperCase()));

				if (error || debug) {
					Gpr.debug("Fatal error:"//
							+ "\n\tPos           : " + pos //
							+ "\n\tSeqChange     : " + seqChange + (seqChange.isStrandPlus() ? "+" : "-") //
							+ "\n\tCodon (exp)   : " + codons//
							+ "\n\tCodon (pred)  : " + effect.getCodonsOld().toUpperCase() + "/" + effect.getCodonsNew().toUpperCase() //
							+ "\n\tEffect (pred) : " + effStr //
							+ "\n\tEffect (pred) : " + effect //
							+ "\n\tGene          : " + gene//
							+ "\n\tChromo        : " + chromoSequence//
					);
				}

				/**
				 * Error? Dump so we can debug...
				 */
				if (error) {
					System.err.println("Error. Dumping data");
					Save save = new Save();
					save.snpEffectPredictor = snpEffectPredictor;
					save.chromoSequence = chromoSequence;
					save.chromoNewSequence = chromoNewSequence;
					save.ref = ref;
					save.pos = pos;
					save.mnp = mnp;
					String outFile = "/tmp/sep_" + i + "_" + pos + ".bin";
					Gpr.toFileSerialize(outFile, save);
					throw new RuntimeException("Codons do not match!\n\tData dumped: '" + outFile + "'");
				}

				// Check warnings
				//				if (!effect.getWarning().isEmpty()) Gpr.debug("WARN:" + effect.getWarning() + "\t" + seqChange + "\t" + seqChangeStrand);
				//				Assert.assertEquals(true, effect.getWarning().isEmpty());
			}
		}
	}

	String codons() {
		char seq[] = chromoSequence.toCharArray();
		char seqNew[] = chromoNewSequence.toCharArray();

		codonsOld = "";
		codonsNew = "";
		int codonIdx = 0;
		int i = 0;
		int step = transcript.isStrandPlus() ? 1 : -1;
		char codonOld[] = new char[3];
		char codonNew[] = new char[3];
		for (Exon ex : transcript.sortedStrand()) {
			int start = ex.isStrandPlus() ? ex.getStart() : ex.getEnd();
			for (i = start; ex.intersects(i); i += step, codonIdx = (codonIdx + 1) % 3) {
				codonOld[codonIdx] = seq[i];
				codonNew[codonIdx] = seqNew[i];
				if (codonIdx == 2) addIfDiff(codonOld, codonNew);
			}
		}

		for (; codonIdx != 0; i += step, codonIdx = (codonIdx + 1) % 3) {
			codonOld[codonIdx] = 'N';
			codonNew[codonIdx] = 'N';
			if (codonIdx == 2) addIfDiff(codonOld, codonNew);
		}

		return codonsOld + "/" + codonsNew;
	}

	/**
	 * Create a MNP
	 * @param pos
	 * @param mnpLen
	 */
	String createMnp(int pos, int mnpLen) {
		char chSeq[] = chromoSequence.toCharArray();
		char chSeqNew[] = chromoSequence.toCharArray();

		String mnp = "";
		for (int i = pos; i < (pos + mnpLen); i++) {
			chSeqNew[i] = snp(chSeq[i]);
			mnp += chSeqNew[i];
		}

		chromoNewSequence = new String(chSeqNew);
		return mnp;
	}

	void init() {
		initRand();
		initSnpEffPredictorRand();
	}

	void initRand() {
		rand = new Random(20111113);
		rand = new Random(20111222);
	}

	void initSnpEffPredictorRand() {
		initSnpEffPredictorRand("testCase");
	}

	void initSnpEffPredictorRand(String genomeVer) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);

		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);

		// Create predictor
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
		codonTable = genome.codonTable();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	/**
	 * Create a different base
	 * @param ref
	 * @return
	 */
	char snp(char ref) {
		char snp = ref;
		while (snp == ref) {
			snp = Character.toUpperCase(GprSeq.randBase(rand));
		}
		return snp;
	}

	public void test() {
		int N = 1000;

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictorRand();

			if (debug) System.out.println("MNP Test iteration: " + i + "\nChromo:\t" + chromoSequence + "\n" + transcript);
			else System.out.println("MNP Test iteration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());

			if (debug) {
				for (Exon exon : transcript.sortedStrand())
					System.out.println("\tExon: " + exon + "\tSEQ:\t" + (exon.isStrandMinus() ? GprSeq.reverseWc(exon.getSequence()) : exon.getSequence()).toUpperCase());
			}

			// For each base in this exon...
			for (int pos = 0; pos < chromoSequence.length() - 2; pos++) {
				// MNP length
				int mnpLen = rand.nextInt(MAX_MNP_LEN) + 2;
				int maxMnpLen = chromoSequence.length() - pos;
				mnpLen = Math.min(mnpLen, maxMnpLen);

				String ref = chromoSequence.substring(pos, pos + mnpLen);
				String mnp = createMnp(pos, mnpLen);

				analyze(i, pos, ref, mnp);
			}
		}
	}

	public void test_01() {
		// Run
		String args[] = { "-classic", "-ud", "0", "testHg3766Chr1", "./tests/test.mnp.01.vcf" };
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.snpEffCmd();
		List<VcfEntry> results = snpeff.run(true);

		// Check
		Assert.assertEquals(1, results.size());
		VcfEntry result = results.get(0);

		for (VcfEffect eff : result.parseEffects()) {
			String aa = eff.getAa();
			String aaNumStr = aa.substring(1, aa.length() - 1);
			int aanum = Gpr.parseIntSafe(aaNumStr);
			System.out.println(eff.getAa() + "\t" + aaNumStr + "\t" + eff);

			if (aanum <= 0) throw new RuntimeException("Missing AA number!");
		}

	}
}
