package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Coding change in HGVS notation (amino acid changes)
 * References: http://www.hgvs.org/mutnomen/recs.html
 */

public class HgvsProtein extends Hgvs {

	int codonNum, aaPos;
	String aaNew3, aaOld3;

	// EffectType effectType;

	public HgvsProtein(VariantEffect variantEffect) {
		super(variantEffect);

		codonNum = variantEffect.getCodonNum();
		marker = variantEffect.getMarker();
		// effectType = variantEffect.getEffectType();

		// No marker? Nothing to do
		if (marker != null) {
			// Codon numbering
			// HGVS: the translation initiator Methionine is numbered as +1
			if (codonNum >= 0) aaPos = codonNum + 1;

			// Convert to 3 letter code
			// HGVS: the three-letter amino acid code is prefered (see Discussion), with "*" designating a translation
			// 		 termination codon; for clarity we this page describes changes using the three-letter amino acid
			CodonTable codonTable = marker.codonTable();

			String aaNew = variantEffect.getAaNew();
			String aaOld = variantEffect.getAaOld();

			if (aaNew == null || aaNew.isEmpty() || aaNew.equals("-")) aaNew3 = "";
			else aaNew3 = codonTable.aaThreeLetterCode(aaNew);

			if (aaOld == null || aaOld.isEmpty() || aaOld.equals("-")) aaOld3 = "";
			else aaOld3 = codonTable.aaThreeLetterCode(aaOld);
		} else {
			aaPos = -1;
			aaNew3 = aaOld3 = "";
		}
	}

	/**
	 * Deletions remove one or more amino acid residues from the protein and are described using "del"
	 * after an indication of the first and last amino acid(s) deleted separated by a "_" (underscore).
	 * Deletions remove either a small internal segment of the protein (in-frame deletion), part of
	 * the N-terminus of the protein (initiation codon change) or the entire C-terminal part of the
	 * protein (nonsense change). A nonsense change is a special type of deletion removing the entire
	 * C-terminal part of a protein starting at the site of the variant (specified 2013-03-16).
	 *
	 * 	1) in-frame deletions  -  are described using "del" after an indication of the first and last amino acid(s) deleted separated, by a "_" (underscore).
	 *
	 * 		p.Gln8del in the sequence MKMGHQQQCC denotes a Glutamine-8 (Gln, Q) deletion to MKMGHQQCC
	 *
	 * 		p.(Cys28_Met30del) denotes RNA nor protein was analysed but the predicted change is a deletion of three amino acids, from Cysteine-28 to Methionine-30
	 *
	 * 2) initiating methionine change (Met1) causing a N-terminal deletion  (see Discussion,  see Examples)
	 * NOTE:  changes extending the N-terminal protein sequence are described as an extension
	 * 		p.0  -  no protein is produced (experimental data should be available)
	 * 		NOTE: this change is not described as p.Met1_Leu833del, i.e. as a deletion removing the entire protein coding sequence
	 *
	 * 		p.Met1? -  denotes that amino acid Methionine-1 (translation initiation site) is changed and that it is unclear what the consequence of this change is
	 *
	 * 		p.Met1_Lys45del  -  a new translation initiation site is activated (at Met46)
	 *
	 * 3) nonsense variant  -  are a special type of amino acid deletion removing the entire C-terminal
	 * 	  part of a protein starting at the site of the variant. A nonsense change is described using
	 * 	  the format p.Trp26Ter (alternatively p.Trp26*). The description does not include the deletion
	 * 	  at protein level from the site of the change to the C-terminal end of the protein (stop codon)
	 * 	  like p.Trp26_Leu833del (the deletion of amino acid residue Trp26 to the last amino acid of
	 * 	  the protein Leu833).
	 *
	 * 		p.(Trp26Ter) indicates RNA nor protein was analysed but amino acid Tryptophan26 (Trp, W) is predicted to change to a stop codon (Ter) (alternatively p.(W26*) or p.(Trp26*))
	 */
	protected String del() {
		/**
		 * Frame shifts are a special type of amino acid deletion/insertion affecting an amino acid
		 * between the first (initiation, ATG) and last codon (termination, stop), replacing the
		 * normal C-terminal sequence with one encoded by another reading frame (specified 2013-10-11).
		 * A frame shift is described using "fs" after the first amino acid affected by the change.
		 * Descriptions either use a short ("fs") or long ("fsTer#") description
		 */
		if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return "fs";

		return "del";
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
	 */
	protected String ins() {
		// We cannot write frame shifts this way
		if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return "fs";

		return "ins" + aaNew3;
	}

	/**
	 * Mixed variants
	 * Deletion/insertions (indels) replace one or more amino acid residues with one or more other
	 * amino acid residues. Deletion/insertions are described using "delins" as a deletion followed
	 * by an insertion after an indication of the amino acid(s) flanking the site of the
	 * deletion/insertion separated by a "_" (underscore, see Discussion). Frame shifts are a special
	 * type of amino acid deletion/insertion affecting an amino acid between the first (initiation, ATG)
	 * and last codon (termination, stop), replacing the normal C-terminal sequence with one encoded
	 * by another reading frame (specified 2013-10-11). A frame shift is described using "fs" after
	 * the first amino acid affected by the change. Descriptions either use a short ("fs") or long
	 * ("fsTer#") description. The description of frame shifts does not include the deletion at
	 * protein level from the site of the frame shift to the natural end of the protein (stop codon).
	 * The inserted amino acid residues are not described, only the total length of the new shifted
	 * frame is given (i.e. including the first amino acid changed).
	 */
	protected String mixed() {
		if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return "fs";

		return "delins";
	}

	/**
	 * Protein coding position
	 */
	protected String pos() {
		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			return pos(codonNum);

		case INS:
			String p = pos(codonNum);
			if (p == null) return null;
			String pNext = pos(codonNum + 1);
			if (pNext == null) return null;
			return p + "_" + pNext;

		case DEL:
			p = pos(codonNum);
			if (p == null) return null;

			// Frame shifts ....are described using ... the change of the first amino acid affected
			// ... the description does not include a description of the deletion from the site of the change
			if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return p;

			String aaOld = variantEffect.getAaOld();
			if (aaOld == null || aaOld.isEmpty() || aaOld.equals("-")) return null;
			if (aaOld.length() == 1) return p; // Single AA deleted

			int end = codonNum + aaOld.length() - 1;
			pNext = pos(end);
			if (pNext == null) return null;

			return p + "_" + pNext;

		case MIXED:
			p = pos(codonNum);
			if (p == null) return null;

			// Frame shifts ....are described using ... the change of the first amino acid affected
			// ... the description does not include a description of the deletion from the site of the change
			if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return p;

			aaOld = variantEffect.getAaOld();
			if (aaOld == null || aaOld.isEmpty() || aaOld.equals("-")) aaOld = "";
			if (aaOld.length() == 1) return p; // Single AA deleted
			String aaNew = variantEffect.getAaNew();
			Gpr.debug(aaOld + "/" + aaNew);
			end = codonNum + aaOld.length();
			pNext = pos(end);
			if (pNext == null) return null;
			return p + "_" + pNext;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}
	}

	/**
	 * Protein position
	 */
	protected String pos(int codonNum) {
		if (codonNum < 0) return null;
		Transcript tr = variantEffect.getTranscript();
		String protSeq = tr.protein();
		if (codonNum >= protSeq.length()) return null;
		CodonTable codonTable = marker.codonTable();
		return codonTable.aaThreeLetterCode(protSeq.charAt(codonNum)) + codonNum;
	}

	/**
	 * SNP or MNP changes
	 */
	protected String snpOrMnp() {
		// No codon change information? only codon number?
		if (variantEffect.getAaOld().isEmpty() && variantEffect.getAaNew().isEmpty()) {
			if (codonNum >= 0) return "" + (codonNum + 1);
			return null;
		}

		// Stop gained
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#del
		// Nonsense variant are a special type of amino acid deletion removing the entire C-terminal part of a
		// protein starting at the site of the variant. A nonsense change is described using the format
		// p.Trp26Ter (alternatively p.Trp26*).
		if (variantEffect.hasEffectType(EffectType.STOP_GAINED)) return "p." + aaOld3 + aaPos + "*";

		// Stop codon mutations
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#extp
		// A change affecting the translation termination codon (Ter/*) introducing a new downstream termination
		// codon extending the C-terminus of the encoded protein described using "extTer#" (alternatively "ext*#")
		// where "#" is the position of the new stop codon (Ter# / *#)
		// E.g.:
		// 		p.*327Argext*? (alternatively p.Ter327ArgextTer? or p.*327Rext*?) describes a variant in the stop
		//		codon (Ter/*) at position 327, changing it to a codon for Arginine (Arg, R) and adding a tail of
		//		new amino acids of unknown length since the shifted frame does not contain a new stop codon.
		if (variantEffect.hasEffectType(EffectType.STOP_LOST)) return "p." + aaOld3 + aaPos + aaNew3 + "ext*?";

		// Start codon lost
		// Reference : http://www.hgvs.org/mutnomen/disc.html#Met
		// Currently, variants in the translation initiating Methionine (M1) are usually described as a substitution, e.g. p.Met1Val.
		// This is not correct. Either no protein is produced (p.0) or a new translation initiation site up- or downstream is used (e.g. p.Met1ValextMet-12 or p.Met1_Lys45del resp.).
		// Unless experimental proof is available, it is probably best to report the effect on protein level as "p.Met1?" (unknown).
		// When experimental data show that no protein is made, the description "p.0" is recommended (see Examples).
		//
		// We use the same for SYNONYMOUS_START since we cannot rally predict if the new start codon will actually be functioning as a start codon (since the Kozak sequence changed)
		// Ditto for NON_SYNONYMOUS_START
		if (variantEffect.hasEffectType(EffectType.START_LOST) //
				|| variantEffect.hasEffectType(EffectType.SYNONYMOUS_START) //
				|| variantEffect.hasEffectType(EffectType.NON_SYNONYMOUS_START) //
		) return "p." + aaOld3 + "1?";

		// Synonymous changes
		// Description of so called "silent" changes in the format p.Leu54Leu (or p.L54L) is not allowed; descriptions
		// should be given at DNA level, it is non-informative and not unequivocal (there are five possibilities
		// at DNA level which may underlie p.Leu54Leu);  correct description has the format c.162C>G.
		if ((variantEffect.hasEffectType(EffectType.SYNONYMOUS_CODING)) //
				|| (variantEffect.hasEffectType(EffectType.SYNONYMOUS_STOP)) //
		) return "p." + aaOld3 + aaPos + aaNew3;

		return "p." + aaOld3 + aaPos + aaNew3;
	}

	@Override
	public String toString() {
		if (variant == null || marker == null) return null;

		String protChange = "";
		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			return snpOrMnp();

		case INS:
			protChange = ins();
			break;

		case DEL:
			protChange = del();
			break;

		case MIXED:
			protChange = mixed();
			break;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		String pos = pos();
		if (pos == null) return null;
		return "p." + pos + protChange;
	}
}
