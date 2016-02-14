package org.snpeff.interval.codonChange;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;

/**
 * Calculate codon changes produced by a duplication
 *
 * @author pcingola
 */
public abstract class CodonChangeStructural extends CodonChange {

	public static boolean debug = false;

	protected boolean coding;
	protected int exonFull, exonPartial;

	public CodonChangeStructural(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
		coding = transcript.isProteinCoding() || Config.get().isTreatAllAsProteinCoding();
		countAffectedExons();
	}

	/**
	 * Differences between two CDSs after removing equal codons from
	 * the beginning and from the end of both strings
	 */
	protected void cdsDiff(String cdsRef, String cdsAlt) {
		int min = Math.min(cdsRef.length(), cdsAlt.length()) / 3;

		// Removing form the beginning
		codonStartNum = 0;
		codonStartIndex = 0;
		for (int i = 0; i <= min; i++) {
			codonStartNum = i;
			if (debug) Gpr.debug("cdsDiff Start\tcodonEquals(" + i + " , " + i + "): " + codonEquals(cdsRef, cdsAlt, i, i) //
					+ "\n\tcodonsRef [" + codonStartNum + "]: " + codons(cdsRef, codonStartNum, -1) //
					+ "\n\tcodonsAlt [" + codonStartNum + "]: " + codons(cdsAlt, codonStartNum, -1) //
			);
			if (!codonEquals(cdsRef, cdsAlt, i, i)) break;
		}

		// Removing trailing codons
		int codonNumEndRef = cdsRef.length() / 3; //+ (cdsRef.length() % 3 == 0 ? 0 : 1);
		int codonNumEndAlt = cdsAlt.length() / 3; //+ (cdsAlt.length() % 3 == 0 ? 0 : 1);

		for (; codonNumEndRef >= codonStartNum && codonNumEndAlt >= codonStartNum; codonNumEndRef--, codonNumEndAlt--) {
			if (debug) Gpr.debug("cdsDiff End\tcodonEquals(" + codonNumEndRef + " , " + codonNumEndAlt + "): " + codonEquals(cdsRef, cdsAlt, codonNumEndRef, codonNumEndAlt) //
					+ "\n\tcodonsRef [" + codonStartNum + " , " + codonNumEndRef + "]: " + codons(cdsRef, codonStartNum, codonNumEndRef) //
					+ "\n\tcodonsAlt [" + codonStartNum + " , " + codonNumEndAlt + "]: " + codons(cdsAlt, codonStartNum, codonNumEndAlt) //
			);
			if (!codonEquals(cdsRef, cdsAlt, codonNumEndRef, codonNumEndAlt)) break;
		}

		// Codons
		codonsRef = codons(cdsRef, codonStartNum, codonNumEndRef);
		codonsAlt = codons(cdsAlt, codonStartNum, codonNumEndAlt);

		// No codon difference found?
		if (codonsRef.isEmpty() && codonsAlt.isEmpty()) codonStartNum = codonStartIndex = -1;
	}

	@Override
	public void codonChange() {
		if (variant.includes(transcript)) {
			// Whole transcript affected?
			effectTranscript();
		} else {
			// Does the variant affect any exons?
			if ((exonFull > 0 || exonPartial > 0)) exons();
			else intron();
		}
	}

	/**
	 * Compare codons from cdsRef[codonNumRef] and cdsAlt[codonNumAlt]
	 */
	private boolean codonEquals(String cdsRef, String cdsAlt, int codonNumRef, int codonNumAlt) {
		for (int h = 0, i = 3 * codonNumRef, j = 3 * codonNumAlt; h < 3; i++, j++, h++) {
			if ((i >= cdsRef.length()) || (j >= cdsAlt.length())) // Premature end of sequence? (i.e. sequence ends before codon end)
				return (i >= cdsRef.length()) && (j >= cdsAlt.length()); // We consider them equal only if both sequences reached the end at the same time

			// Same base?
			if (cdsRef.charAt(i) != cdsAlt.charAt(j)) return false;
		}

		return true;
	}

	/**
	 * Get codons from CDS
	 */
	private String codons(String cds, int codonNumStart, int codonNumEnd) {
		if (codonNumEnd >= 0 && codonNumEnd < codonNumStart) return "";
		int endBase = codonNumEnd < 0 ? cds.length() : 3 * (codonNumEnd + 1);
		endBase = Math.min(cds.length(), endBase);
		int startBase = Math.max(0, 3 * codonNumStart);
		return cds.substring(startBase, endBase);
	}

	/**
	 * How many full / partial exons does the variant affect?
	 */
	protected void countAffectedExons() {
		exonFull = exonPartial = 0;

		for (Exon ex : transcript) {
			if (variant.includes(ex)) exonFull++;
			else if (variant.intersects(ex)) exonPartial++;
		}
	}

	protected abstract void effectTranscript();

	/**
	 * Variant affect one or more exons
	 */
	protected abstract void exons();

	/**
	 * Variant affect one or more coding exons
	 */
	protected abstract void exonsCoding();

	/**
	 * Variant affect one or more non-coding exons
	 */
	protected abstract void exonsNoncoding();

	/**
	 * Variant affect one intron
	 */
	protected abstract void intron();

}
