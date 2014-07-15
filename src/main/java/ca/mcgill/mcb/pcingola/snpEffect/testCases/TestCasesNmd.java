package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.LinkedList;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.LossOfFunction;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Nonsense mediated decay prediction
 * 
 * @author pcingola
 */
public class TestCasesNmd extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false;
	public static int SHOW_EVERY = 10;

	Config config;
	int countTest = 1;

	public TestCasesNmd() {
		super();
	}

	/**
	 * Check that NMD works for a given transcript
	 * @param gene
	 * @param tr
	 */
	void checkNmd(Gene gene, Transcript tr) {
		if (verbose) System.err.print("\tTranscript " + tr.getId() + " " + (tr.isStrandPlus() ? '+' : '-') + " :");
		else Gpr.showMark(countTest++, SHOW_EVERY);

		int pos = 0;
		boolean isNmd[] = new boolean[tr.cds().length()];
		HashSet<Exon> codingExons = new HashSet<Exon>();

		StringBuilder nmdStr = new StringBuilder();
		StringBuilder nmdStrSimple = new StringBuilder();
		for (Exon exon : tr.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int from = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int expos = from; (exon.getStart() <= expos) && (expos <= exon.getEnd()); expos += step) {
				// Not in UTR? => Test
				if (!tr.isUtr(expos)) {
					codingExons.add(exon);

					// Create a seqChange
					//					SeqChange seqChange = new SeqChange(tr.getChromosome(), expos, expos, "");
					Variant seqChange = new Variant(tr.getChromosome(), expos, "A", "C"); // Create a seqChange

					// Create a STOP_GAIN effect
					VariantEffect changeEffect = new VariantEffect(seqChange);
					changeEffect.set(exon, EffectType.STOP_GAINED, "");
					LinkedList<VariantEffect> changeEffects = new LinkedList<VariantEffect>();
					changeEffects.add(changeEffect);

					// Create a LOF object and analyze the effect
					LossOfFunction lof = new LossOfFunction(config, changeEffects);
					isNmd[pos] = lof.isNmd();

					nmdStr.append(isNmd[pos] ? '+' : '.');
					nmdStrSimple.append(isNmd[pos] ? '+' : '.');
					pos++;
				} else nmdStr.append('U');
			}
			nmdStr.append('\t');
			nmdStrSimple.append('\t');
		}

		// Show string
		if (verbose) System.err.println(nmdStr);
		if (debug) System.err.println("\tCoding Exons:" + codingExons.size());

		//---
		// Check that NMP prediction is 'correct'
		//---
		// We need a splice event in the coding part 
		if (codingExons.size() > 1) {
			// Use the 'simple' string to check
			StringBuilder sb = new StringBuilder();
			String ex[] = nmdStrSimple.toString().split("\t");
			for (int i = 0; i < (ex.length - 1); i++)
				sb.append(ex[i]);

			// Check that last 50 bases are '.'
			String simpleNoLast = sb.toString();
			int lastNmd = Math.max(0, simpleNoLast.length() - LossOfFunction.MND_BASES_BEFORE_LAST_JUNCTION);
			String points = simpleNoLast.substring(lastNmd) + ex[ex.length - 1];
			String plus = simpleNoLast.substring(0, lastNmd);

			if (debug) System.err.println("\tPoints: " + points + "\n\tPlus :" + plus);

			// Check
			Assert.assertEquals(0, points.replace('.', ' ').trim().length());
			Assert.assertEquals(0, plus.replace('+', ' ').trim().length());
		}
	}

	public void test_01() {
		// Load database
		String genomeVer = "testHg3766Chr1";
		Gpr.debug("Loading database '" + genomeVer + "'");
		config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
		config.setTreatAllAsProteinCoding(true); // For historical reasons...
		config.loadSnpEffectPredictor();

		// For each gene, transcript, check that NMD works
		for (Gene gene : config.getGenome().getGenes()) {
			if (verbose) System.err.println("NMD test\tGene ID:" + gene.getId());
			for (Transcript tr : gene) {
				if (debug) System.err.println(tr);
				checkNmd(gene, tr);
			}
		}
	}
}
