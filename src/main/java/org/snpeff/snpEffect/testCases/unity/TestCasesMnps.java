package org.snpeff.snpEffect.testCases.unity;

import java.io.Serializable;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

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
public class TestCasesMnps extends TestCasesBase {

	public static int N = 1000;

	String chromoNewSequence = "";
	String codonsOld = "";
	String codonsNew = "";
	int MAX_MNP_LEN = 10;

	public TestCasesMnps() {
		super();
	}

	void addIfDiff(char[] codonOld, char codonNew[]) {
		String cold = new String(codonOld);
		String cnew = new String(codonNew);
		if (!cold.equals(cnew)) {
			codonsOld += transcript.isStrandPlus() ? cold : GprSeq.wc(cold);
			codonsNew += transcript.isStrandPlus() ? cnew : GprSeq.wc(cnew);
		}
	}

	void analyze(int i, int pos, String ref, String mnp) {
		String codons = codons();

		Variant variant = new Variant(chromosome, pos, ref + "", mnp + "", "");

		//---
		// Calculate effects
		//---
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

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
			String effStr = effect.effect(true, true, true, false, false);

			if (codons.length() > 1) {
				String[] codonsExp = codons.split("/");

				boolean error = (!codonsExp[0].equalsIgnoreCase(effect.getCodonsRef()) //
						|| !codonsExp[1].equalsIgnoreCase(effect.getCodonsAlt()));

				if (error || debug) {
					Log.debug("Fatal error:"//
							+ "\n\tPos           : " + pos //
							+ "\n\tVariant       : " + variant //
							+ "\n\tCodon (exp)   : " + codons//
							+ "\n\tCodon (pred)  : " + effect.getCodonsRef().toUpperCase() + "/" + effect.getCodonsAlt().toUpperCase() //
							+ "\n\tEffect (pred) : " + effStr //
							+ "\n\tEffect (pred) : " + effect //
							+ "\n\tGene          : " + gene//
							+ "\n\tChromo        : " + chromoSequence//
					);
				}

				/*
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
			}
		}
	}

	String codons() {
		char[] seq = chromoSequence.toCharArray();
		char[] seqNew = chromoNewSequence.toCharArray();

		codonsOld = "";
		codonsNew = "";
		int codonIdx = 0;
		int i = 0;
		int step = transcript.isStrandPlus() ? 1 : -1;
		char[] codonOld = new char[3];
		char[] codonNew = new char[3];
		for (Exon ex : transcript.sortedStrand()) {
			int start = ex.isStrandPlus() ? ex.getStart() : ex.getEndClosed();
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
	 */
	String createMnp(int pos, int mnpLen) {
		char[] chSeq = chromoSequence.toCharArray();
		char[] chSeqNew = chromoSequence.toCharArray();

		StringBuilder mnp = new StringBuilder();
		for (int i = pos; i < (pos + mnpLen); i++) {
			chSeqNew[i] = snp(chSeq[i]);
			mnp.append(chSeqNew[i]);
		}

		chromoNewSequence = new String(chSeqNew);
		return mnp.toString();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20111222;
	}

	/**
	 * Create a different base
	 */
	char snp(char ref) {
		char snp = ref;
		while (snp == ref) {
			snp = Character.toUpperCase(GprSeq.randBase(rand));
		}
		return snp;
	}

	@Test
	public void test_01() {
		Log.debug("Test");

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();

			if (debug) System.out.println("MNP Test iteration: " + i + "\nChromo:\t" + chromoSequence + "\n" + transcript);
			else if (verbose) Log.info("MNP Test iteration: " + i + "\t" + (transcript.isStrandPlus() ? "+" : "-") + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);

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

		System.err.println();
	}

}
