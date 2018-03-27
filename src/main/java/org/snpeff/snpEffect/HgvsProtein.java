package org.snpeff.snpEffect;

import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.VariantBnd;
import org.snpeff.util.Gpr;

/**
 * Coding change in HGVS notation (amino acid changes)
 * References: http://www.hgvs.org/mutnomen/recs.html
 */

public class HgvsProtein extends Hgvs {

	public static boolean debug = false;

	int codonNum, aaPos;
	String aaNew, aaOld;
	boolean hgvsOneLetterAa;
	boolean hgvsOld;
	int lettersPerAa;
	char stop;

	public HgvsProtein(VariantEffect variantEffect) {
		super(variantEffect);

		codonNum = variantEffect.getCodonNum();

		hgvsOneLetterAa = Config.get().isHgvs1LetterAA();
		hgvsOld = Config.get().isHgvsOld();
		stop = hgvsOld ? 'X' : '*';
		lettersPerAa = hgvsOneLetterAa ? 1 : 3;

		// No marker? Nothing to do
		if (marker != null) {
			// Codon numbering
			// HGVS: the translation initiator Methionine is numbered as +1
			if (codonNum >= 0) aaPos = codonNum + 1;

			aaNew = aaCode(variantEffect.getAaAlt());
			aaOld = aaCode(variantEffect.getAaRef());
		} else {
			aaPos = -1;
			aaNew = aaOld = "";
		}
	}

	protected String aaCode(char aa1Letter) {
		return aaCode(Character.toString(aa1Letter));
	}

	/**
	 * Use one letter / three letter AA codes
	 * Most times we want to vonvert to 3 letter code
	 * HGVS: the three-letter amino acid code is prefered (see Discussion), with "*" designating a translation
	 *       termination codon; for clarity we this page describes changes using the three-letter amino acid
	 */
	protected String aaCode(String aa1Letter) {
		if (aa1Letter == null || aa1Letter.isEmpty() || aa1Letter.equals("-")) return "";
		if (hgvsOld) return aa1Letter.replace('*', 'X');
		if (hgvsOneLetterAa) return aa1Letter;
		return marker.codonTable().aaThreeLetterCode(aa1Letter);
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
		return "del";
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
	protected String delins() {
		return "delins" + aaNew;
	}

	/**
	 * Duplications
	 */
	protected String dup() {
		return "dup";
	}

	/**
	 * Frame shifts are a special type of amino acid deletion/insertion affecting an amino acid
	 * between the first (initiation, ATG) and last codon (termination, stop), replacing the
	 * normal C-terminal sequence with one encoded by another reading frame (specified 2013-10-11).
	 * A frame shift is described using "fs" after the first amino acid affected by the change.
	 * Descriptions either use a short ("fs") or long ("fsTer#") description
	 */
	protected String fs() {
		return "fs";
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
		return "ins" + aaNew;
	}

	/**
	 * Is this a 'pure' deletion?
	 * E.g.:
	 * 		- A 'CODON_DELETION' is a pure deletion (form HGVS' perspective)
	 * 		- A 'CODON_CHANGE_PLUS_CODON_DELETION' is not a 'pure' deletion for HGVS (it's a 'delins')
	 */
	boolean isDel() {
		return !aaOld.isEmpty() && aaNew.isEmpty();
	}

	/**
	 * Is this variant a duplication
	 *
	 * Reference: http://www.hgvs.org/mutnomen/disc.html#dupins
	 * 		...the description "dup" (see Standards) may by definition only be used
	 * 		when the additional copy is directly 3'-flanking of the original copy (tandem
	 * 		duplication)
	 */
	protected boolean isDuplication() {

		//---
		// Simple duplications can be obtained by looking into AA.Ref / AA.Alt
		//---
		String aaRef = variantEffect.getAaRef().toUpperCase();
		String aaAlt = variantEffect.getAaAlt().toUpperCase();

		// Compare to ALT sequence
		String dupAaRef = aaRef + aaRef;
		if (debug) Gpr.debug("AA.Ref: '" + aaRef + "'\tAA.Alt: '" + aaAlt);
		if (aaAlt.equals(dupAaRef)) return true;

		//---
		// Duplications need to look into the protein sequence
		//---

		// Extract sequence from genomic coordinates before variant
		String protein = tr.protein();
		if (protein == null) return false; // Cannot calculate duplication

		// Calculate net amino acid change
		aaAlt = variantEffect.getAaNetChange();

		// Get previous AA sequence
		int aaEnd = variantEffect.getCodonNum();
		int aaStart = aaEnd - aaAlt.length();
		if (aaStart < 0 || aaEnd > protein.length()) return false;
		aaRef = protein.substring(aaStart, aaEnd);

		// Compare to ALT sequence
		boolean dup = aaRef.equalsIgnoreCase(aaAlt);
		if (debug) Gpr.debug("SEQUENCE [ " + aaStart + " , " + aaEnd + " ]: '" + aaRef + "'" //
				+ "\n\tAA Ref       : '" + variantEffect.getAaRef() + "'" //
				+ "\n\tAA Alt       : '" + variantEffect.getAaAlt() + "'" //
				+ "\n\tAA Alt (net) : '" + aaAlt + "'" //
				+ "\n\tDup?         : " + dup);

		return dup;
	}

	/**
	 * Is this a frameShift variant?
	 */
	boolean isFs() {
		return variantEffect.hasEffectType(EffectType.FRAME_SHIFT) //
				|| variantEffect.hasEffectType(EffectType.FRAME_SHIFT_BEFORE_CDS_START) //
				|| variantEffect.hasEffectType(EffectType.FRAME_SHIFT_AFTER_CDS_END) //
		;
	}

	/**
	 * Is this a 'pure' insertion?
	 * E.g.:
	 * 		- A 'CODON_INSERTION' is a pure insertion (form HGVS' perspective)
	 * 		- A 'CODON_CHANGE_PLUS_CODON_INSERTION' is not a 'pure' insertion for HGVS (it's a 'delins')
	 */
	boolean isIns() {
		return aaOld.isEmpty() && !aaNew.isEmpty();
	}

	/**
	 * Protein position
	 */
	protected String pos(int codonNum) {
		Transcript tr = variantEffect.getTranscript();
		return pos(tr, codonNum);
	}

	protected String pos(int start, int end) {
		Transcript tr = variantEffect.getTranscript();
		return pos(tr, start, end);
	}

	/**
	 * Protein position
	 */
	protected String pos(Transcript tr, int codonNum) {
		if (codonNum < 0 || tr == null) return null;

		// Sanity check: Longer than protein?
		String protSeq = tr.protein();
		if (codonNum >= protSeq.length()) return null;

		// Get AA code
		String aa = aaCode(protSeq.charAt(codonNum));

		return aa + (codonNum + 1);
	}

	/**
	 * Position string given two coordinates
	 */
	protected String pos(Transcript tr, int start, int end) {
		// Only one position needed?
		String posStart = pos(tr, start);
		if (posStart == null) return null;
		if (start == end) return posStart;

		// Both position needed
		String posEnd = pos(tr, end);
		if (posEnd == null) return null;
		return posStart + "_" + posEnd;
	}

	/**
	 * Position for deletions
	 */
	protected String posDel() {
		String posStart = pos(codonNum);
		if (posStart == null) return null;

		if (aaOld == null || aaOld.isEmpty() || aaOld.equals("-")) return null;
		if (aaOld.length() == lettersPerAa) return posStart; // Single AA deleted

		int end = codonNum + (aaOld.length() - aaNew.length()) / lettersPerAa - 1;
		String posEnd = pos(end);
		if (posEnd == null) return null;

		return posStart + "_" + posEnd;
	}

	/**
	 * Position for 'delins'
	 */
	protected String posDelIns() {
		String posStart = pos(codonNum);
		if (posStart == null) return null;

		// Frame shifts ....are described using ... the change of the first amino acid affected
		// ... the description does not include a description of the deletion from the site of the change
		if (variantEffect.hasEffectType(EffectType.FRAME_SHIFT)) return posStart;

		if (aaOld == null || aaOld.isEmpty() || aaOld.equals("-")) aaOld = "";
		if (aaOld.length() == lettersPerAa) return posStart; // Single AA deleted
		int end = codonNum + (aaOld.length() / lettersPerAa) - 1;

		String posEnd = pos(end);
		if (posEnd == null) return null;
		return posStart + "_" + posEnd;
	}

	/**
	 * Position for 'duplications' (a special kind of insertion)
	 */
	protected String posDup() {
		int start, end;

		int netLen = aaNew.length() / lettersPerAa;
		if (netLen == 1) {
			start = end = codonNum - 1;
		} else {
			end = codonNum - 1;
			start = end - (netLen - 1);
		}

		return pos(start, end);
	}

	/**
	 * Frame shifts ....are described using ... the change of the first amino acid affected
	 * ... the description does not include a description of the deletion from the site of the change
	 */
	protected String posFs() {
		if (codonNum < 0) return null;

		// Sanity check: Longer than protein?
		Transcript tr = variantEffect.getTranscript();
		String protSeq = tr.protein();
		if (codonNum > protSeq.length()) return null;
		if (codonNum == protSeq.length()) return aaCode(CodonTable.TERMINATION_CODON_1) + codonNum;

		// NOTE: the changes observed should be described on protein level and not try to
		//       incorporate any knowledge regarding the change at DNA-level.
		//       Thus, p.His150Hisfs*10 is not correct, but p.Gln151Thrfs*9 is.
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#del

		if (!variant.isMixed()) { // Apply is not supported on MIXED variants at the moment
			Transcript newTr = tr.apply(variant);
			String newProtSeq = newTr.protein();

			// Find the first difference in the protein sequences and use that
			// one as AA number
			int len = Math.min(protSeq.length(), newProtSeq.length());
			for (int cn = codonNum; cn < len; cn++) {
				char aa = protSeq.charAt(cn);
				char newAa = newProtSeq.charAt(cn);
				if (aa != newAa) return aaCode(aa) + (cn + 1);
			}
		}

		return aaCode(protSeq.charAt(codonNum)) + (codonNum + 1);
	}

	/**
	 * Position for insertions
	 */
	protected String posIns() {
		int start = codonNum - 1;
		int end = codonNum;
		return pos(start, end);
	}

	/**
	 * Position: SNP or NMP
	 */
	protected String posSnpOrMnp() {
		return pos(codonNum);
	}

	/**
	 * Can we simplify AAs?
	 */
	void simplifyAminoAcids() {
		simplifyAminoAcidsLeft();
		simplifyAminoAcidsRight();
	}

	/**
	 * Remove same AA from left side
	 */
	void simplifyAminoAcidsLeft() {
		// Can we simplify AAs?
		while (!aaOld.isEmpty() && !aaNew.isEmpty()) {
			// Get the first AA
			String ao = aaOld.substring(0, lettersPerAa);
			String an = aaNew.substring(0, lettersPerAa);

			// Both share the same AA? => Remove it
			if (ao.equals(an)) {
				aaOld = aaOld.substring(lettersPerAa);
				aaNew = aaNew.substring(lettersPerAa);
				codonNum++;
				aaPos++;
			} else return;
		}
	}

	/**
	 * Remove same AA form right side
	 */
	void simplifyAminoAcidsRight() {
		// Can we simplify AAs?
		while (!aaOld.isEmpty() && !aaNew.isEmpty()) {
			// Get the last AA
			int lastAaOldIdx = aaOld.length() - lettersPerAa;
			String ao = aaOld.substring(lastAaOldIdx);

			int lastAaNewIdx = aaNew.length() - lettersPerAa;
			String an = aaNew.substring(lastAaNewIdx);

			// Both share the same AA? => Remove it
			if (ao.equals(an)) {
				aaOld = aaOld.substring(0, lastAaOldIdx);
				aaNew = aaNew.substring(0, lastAaNewIdx);
			} else return;
		}
	}

	/**
	 * SNP or MNP changes
	 */
	protected String snpOrMnp() {
		// No codon change information? only codon number?
		if (variantEffect.getAaRef().isEmpty() && variantEffect.getAaAlt().isEmpty()) {
			if (codonNum >= 0) return "" + (codonNum + 1);
			return null;
		}

		// Stop gained
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#del
		// Nonsense variant are a special type of amino acid deletion removing the entire C-terminal part of a
		// protein starting at the site of the variant. A nonsense change is described using the format
		// p.Trp26Ter (alternatively p.Trp26*).
		if (variantEffect.hasEffectType(EffectType.STOP_GAINED)) return aaOld + aaPos + stop;

		// Stop codon mutations
		// Reference: http://www.hgvs.org/mutnomen/recs-prot.html#extp
		// A change affecting the translation termination codon (Ter/*) introducing a new downstream termination
		// codon extending the C-terminus of the encoded protein described using "extTer#" (alternatively "ext*#")
		// where "#" is the position of the new stop codon (Ter# / *#)
		// E.g.:
		// 		p.*327Argext*? (alternatively p.Ter327ArgextTer? or p.*327Rext*?) describes a variant in the stop
		//		codon (Ter/*) at position 327, changing it to a codon for Arginine (Arg, R) and adding a tail of
		//		new amino acids of unknown length since the shifted frame does not contain a new stop codon.
		if (variantEffect.hasEffectType(EffectType.STOP_LOST)) return aaOld + aaPos + aaNew + "ext" + stop + "?";

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
		) return aaOld + "1?";

		// Synonymous changes
		// Description of so called "silent" changes in the format p.Leu54Leu (or p.L54L) is not allowed; descriptions
		// should be given at DNA level, it is non-informative and not unequivocal (there are five possibilities
		// at DNA level which may underlie p.Leu54Leu);  correct description has the format c.162C>G.
		if ((variantEffect.hasEffectType(EffectType.SYNONYMOUS_CODING)) //
				|| (variantEffect.hasEffectType(EffectType.SYNONYMOUS_STOP)) //
		) return aaOld + aaPos + aaNew;

		return aaOld + aaPos + aaNew;
	}

	@Override
	public String toString() {
		if (variant == null || marker == null) return null;

		// Deleted transcript produces no protein.
		if (variantEffect.getEffectType() == EffectType.TRANSCRIPT_DELETED) return typeOfReference() + "0?";

		// Can we simplify amino acids in aaNew/aaOld?
		// if (!variant.isSnp() && !variant.isMnp()) simplifyAminoAcids();
		if (!variant.isSnp()) simplifyAminoAcids();

		String pos = "", protChange = "", prefix = "", suffix = "";

		switch (variant.getVariantType()) {

		case INTERVAL:
			// Not a variant? Nothing to do
			return "";

		case SNP:
		case MNP:
			// SNP or MNP
			protChange = snpOrMnp();
			pos = "";
			break;

		case INV:
			// Inversion description not used at protein level
			// Reference: http://www.hgvs.org/mutnomen/examplesAA.html
			return "";

		case BND:
			return translocation();

		default:
			if (isFs()) {
				// Frame shifts
				protChange = fs();
				pos = posFs();
			} else if (isIns()) {
				// This is a 'pure' insertion
				duplication = isDuplication(); // Is it a duplication?

				if (duplication) {
					protChange = dup();
					pos = posDup();
				} else {
					protChange = ins();
					pos = posIns();
				}
			} else if (isDel()) {
				// A deletion
				protChange = del();
				pos = posDel();
			} else {
				// A mixture of insertion and deletion
				protChange = delins();
				pos = posDelIns();
			}
		}

		if (protChange == null || pos == null) return null;

		return prefix + typeOfReference() + pos + protChange + suffix;
	}

	/**
	 * Translocation nomenclature.
	 * From HGVS:
	 * 	Translocations at protein level occur when a translocation at DNA level
	 * 	leads to the production of a fusion protein, joining the N-terminal
	 * 	end of the protein on one chromosome to the C-terminal end of the protein
	 * 	on the other chromosome (and vice versa). No recommendations have been
	 * 	made sofar to describe protein translocations.
	 *
	 * 		t(X;17)(DMD:p.Met1_Val1506; SGCA:p.Val250_*387)
	 * 	describes a fusion protein resulting from a translocation between the
	 * chromosomes X and 17; the fusion protein contains an N-terminal segment
	 * of DMD (dystrophin, amino acids Methionine-1 to Valine-1506), and a
	 * C-terminal segment of SGCA (alpha-sarcoglycan, amino acids Valine-250
	 * to the stop codon at 387)
	 */
	protected String translocation() {
		if (!(variantEffect instanceof VariantEffectFusion)) return "";
		VariantBnd vtr = (VariantBnd) variant;
		VariantEffectFusion veffFusion = (VariantEffectFusion) variantEffect;

		if (veffFusion.getTrLeft() == null && veffFusion.getTrRight() == null) return "";

		// Chromosome part
		String chrCoords = "(" //
				+ vtr.getChromosomeName() //
				+ ";" //
				+ vtr.getEndPoint().getChromosomeName() //
				+ ")" //
		;

		// Left transcript coordinates
		String trLeftStr = "";
		if (veffFusion.getTrLeft() != null) {
			trLeftStr = veffFusion.getTrLeft().getId() //
					+ ":" //
					+ pos(veffFusion.getTrLeft(), veffFusion.getAaNumLeftStart(), veffFusion.getAaNumLeftEnd());
		}

		// Right transcript coordinates
		String trRightStr = "";
		if (veffFusion.getTrRight() != null) {
			trRightStr = veffFusion.getTrRight().getId() //
					+ ":" //
					+ pos(veffFusion.getTrRight(), veffFusion.getAaNumRightStart(), veffFusion.getAaNumRightEnd());
		}

		return "t" + chrCoords + "(" + trLeftStr + ";" + trRightStr + ")";
	}

	/**
	 * Return "p." string with/without transcript ID, according to user command line options.
	 */
	protected String typeOfReference() {
		if (!hgvsTrId || tr == null) return "p.";

		String ver = tr.getVersion();
		return tr.getId() + (ver.isEmpty() ? "" : "." + ver) + ":p.";
	}
}
