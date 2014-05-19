package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Coding change in HGVS notation (amino acid changes)
 * References: http://www.hgvs.org/mutnomen/recs.html
 */

public class HgsvProtein extends Hgsv {

	int codonNum, aaPos;
	String aaNew3, aaOld3;
	EffectType effectType;

	public HgsvProtein(ChangeEffect changeEffect) {
		super(changeEffect);

		codonNum = changeEffect.getCodonNum();
		marker = changeEffect.getMarker();
		effectType = changeEffect.getEffectType();

		// No marker? Nothing to do
		if (marker != null) {
			// Codon numbering
			// HGVS: the translation initiator Methionine is numbered as +1
			aaPos = codonNum + 1;

			// Convert to 3 letter code
			// HGVS: the three-letter amino acid code is prefered (see Discussion), with "*" designating a translation
			// 		 termination codon; for clarity we this page describes changes using the three-letter amino acid
			CodonTable codonTable = marker.codonTable();
			aaNew3 = codonTable.aaThreeLetterCode(changeEffect.getAaNew());
			aaOld3 = codonTable.aaThreeLetterCode(changeEffect.getAaOld());
		} else {
			aaPos = -1;
			aaNew3 = aaOld3 = "";
		}

	}

	/**
	 * Insertions
	 * Insertions add one or more amino acid residues between two existing amino acids and this
	 * insertion is not a copy of a sequence immediately 5'-flanking (see Duplication). Insertions
	 * are described using "ins" after an indication of the amino acids flanking the insertion
	 * site, separated by a "_" (underscore) and followed by a description of the amino acid(s)
	 * inserted. Since for large insertions the amino acids can be derived from the DNA and/or RNA
	 * descriptions they need not to be described exactly but the total number may be given (like "ins17").
	 *
	 * Examples:
	 * 		1) p.Lys2_Met3insGlnSerLys denotes that the sequence GlnSerLys (QSK) was inserted between amino acids Lysine-2 (Lys, K) and Methionine-3 (Met, M), changing MKMGHQQQCC to MKQSKMGHQQQCC
	 * 		2) p.Trp182_Gln183ins17 describes a variant that inserts 17 amino acids between amino acids Trp182 and Gln183
	 *
	 * NOTE: it must be possible to deduce the 17 inserted amino acids from the description given at DNA or RNA level
	 *
	 * @return
	 */
	protected String ins() {
		throw new RuntimeException("CREATE THIS CODE");
	}

	@Override
	public String toString() {
		// No codon chage information? only codon number?
		if (changeEffect.getAaOld().isEmpty() && changeEffect.getAaNew().isEmpty()) {
			if (codonNum >= 0) return "" + (codonNum + 1);
			return null;
		}

		// Insertions
		if (changeEffect.getSeqChange().getChangeType() == ChangeType.INS) return ins();

		// Synonymous changes
		if ((effectType == EffectType.SYNONYMOUS_CODING) //
				|| (effectType == EffectType.SYNONYMOUS_STOP) //
				) {
			// HGVS: Description of so called "silent" changes in the format p.Leu54Leu (or p.L54L) is not allowed; descriptions
			// 		 should be given at DNA level, it is non-informative and not unequivocal (there are five possibilities
			// 		 at DNA level which may underlie p.Leu54Leu);  correct description has the format c.162C>G.
			return "p." + aaOld3 + aaPos + aaNew3;
		}

		// Start codon lost
		if ((effectType == EffectType.START_LOST) //
				|| (effectType == EffectType.SYNONYMOUS_START) //
				|| (effectType == EffectType.NON_SYNONYMOUS_START) //
				) {
			// Reference : http://www.hgvs.org/mutnomen/disc.html#Met
			// Currently, variants in the translation initiating Methionine (M1) are usually described as a substitution, e.g. p.Met1Val.
			// This is not correct. Either no protein is produced (p.0) or a new translation initiation site up- or downstream is used (e.g. p.Met1ValextMet-12 or p.Met1_Lys45del resp.).
			// Unless experimental proof is available, it is probably best to report the effect on protein level as "p.Met1?" (unknown).
			// When experimental data show that no protein is made, the description "p.0" is recommended (see Examples).
			//
			// We use the same for SYNONYMOUS_START since we cannot rally predict if the new start codon will actually be functioning as a start codon (since the Kozak sequence changed)
			// Ditto for NON_SYNONYMOUS_START
			return "p." + aaOld3 + "1?";
		}

		// Stop codon mutations
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#extp
		// A change affecting the translation termination codon (Ter/*) introducing a new downstream termination
		// codon extending the C-terminus of the encoded protein described using "extTer#" (alternatively "ext*#")
		// where "#" is the position of the new stop codon (Ter# / *#)
		// E.g.:
		// 		p.*327Argext*? (alternatively p.Ter327ArgextTer? or p.*327Rext*?) describes a variant in the stop
		//		codon (Ter/*) at position 327, changing it to a codon for Arginine (Arg, R) and adding a tail of
		//		new amino acids of unknown length since the shifted frame does not contain a new stop codon.
		if (effectType == EffectType.STOP_LOST) return "p." + aaOld3 + aaPos + aaNew3 + "ext*?";

		// Reference: 		http://www.hgvs.org/mutnomen/recs-prot.html#del
		// Nonsense variant are a special type of amino acid deletion removing the entire C-terminal part of a
		// protein starting at the site of the variant. A nonsense change is described using the format
		// p.Trp26Ter (alternatively p.Trp26*).
		if (effectType == EffectType.STOP_GAINED) return "p." + aaOld3 + aaPos + "*";

		return "p." + aaOld3 + aaPos + aaNew3;
	}
}
