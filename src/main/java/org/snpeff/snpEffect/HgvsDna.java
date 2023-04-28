package org.snpeff.snpEffect;

import org.snpeff.interval.CytoBands;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.VariantBnd;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Coding DNA reference sequence
 *
 * References http://www.hgvs.org/mutnomen/recs.html
 *
 * Nucleotide numbering:
 * 	- there is no nucleotide 0
 * 	- nucleotide 1 is the A of the ATG-translation initiation codon
 * 	- the nucleotide 5' of the ATG-translation initiation codon is -1, the previous -2, etc.
 * 	- the nucleotide 3' of the translation stop codon is *1, the next *2, etc.
 * 	- intronic nucleotides (coding DNA reference sequence only)
 * 		- beginning of the intron; the number of the last nucleotide of the preceding exon, a plus sign and the position in the intron, like c.77+1G, c.77+2T, ....
 * 		- end of the intron; the number of the first nucleotide of the following exon, a minus sign and the position upstream in the intron, like ..., c.78-2A, c.78-1G.
 * 		- in the middle of the intron, numbering changes from "c.77+.." to "c.78-.."; for introns with an uneven number of nucleotides the central nucleotide is the last described with a "+" (see Discussion)
 *
 * Genomic reference sequence
 * 		- nucleotide numbering starts with 1 at the first nucleotide of the sequence
 * 		  NOTE: the sequence should include all nucleotides covering the sequence (gene) of interest and should start well 5' of the promoter of a gene
 * 		- no +, - or other signs are used
 * 		- when the complete genomic sequence is not known, a coding DNA reference sequence should be used
 * 		- for all descriptions the most 3' position possible is arbitrarily assigned to have been changed (see Exception)
 */

public class HgvsDna extends Hgvs {

	public static boolean debug = false;

	public HgvsDna(VariantEffect variantEffect) {
		super(variantEffect);
	}

	protected String alt() {
		return variant.getAlt();
	}

	/**
	 * DNA level base changes
	 */
	protected String dnaBaseChange() {

		switch (variant.getVariantType()) {
		case SNP:
			if (strandPlus) return ref() + ">" + alt();
			return GprSeq.reverseWc(ref()) + ">" + GprSeq.reverseWc(alt());

		case MNP:
			String ref, alt;
			if (strandPlus) {
				ref = ref();
				alt = alt();
			} else {
				ref = GprSeq.reverseWc(ref());
				alt = GprSeq.reverseWc(alt());
			}
			return "del" + ref + "ins" + alt;

		case DEL:
		case DUP:
		case INS:
			if (variant.size() > MAX_SEQUENCE_LEN_HGVS) return "";
			String netChange = variant.netChange(false);
			if (strandPlus) return netChange;
			return GprSeq.reverseWc(netChange);

		case MIXED:
			if (strandPlus) return "del" + ref() + "ins" + alt();
			return "del" + GprSeq.reverseWc(ref()) + "ins" + GprSeq.reverseWc(alt());

		case INV:
			// Inversions are designated by "inv" after an indication of the
			// first and last nucleotides affected by the inversion.
			// Reference: http://www.hgvs.org/mutnomen/recs-DNA.html#inv
			// => No base changes are used
			return "";

		case INTERVAL:
			return "";

		case BND:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}
	}

	/**
	 * Is this position downstream?
	 */
	boolean isDownstream(int pos) {
		if (tr.isStrandPlus()) return tr.getEndClosed() < pos;
		return pos < tr.getStart();
	}

	/**
	 * Is this a duplication?
	 */
	protected boolean isDuplication() {
		// Only insertion cause duplications
		// Reference: Here is a discussion of a possible new term ('los') as an analogous
		//            to 'dup' for deletions:
		//                http://www.hgvs.org/mutnomen/disc.html#loss
		//            So, it's still not decided if there is an analogous 'dup' term
		//            for deletions.
		if (!variant.isIns()) return false;

		// Extract sequence from genomic coordinates before variant
		String seq = null;

		// Get sequence at the 3'end of the variant
		int sstart, send;
		int len = alt().length();

		if (strandPlus) {
			sstart = Math.max(0, variant.getStart() - len);
			send = variant.getStart() - 1;
		} else {
			sstart = variant.getStart();
			send = sstart + (len - 1);
		}

		// Maybe we can just use exonic sequences (it's faster)
		// Create a marker and check that is comprised within exon boundaries
		Marker m = new Marker(variant.getParent(), sstart, send, false, "");

		Exon ex = variantEffect.getExon();
		if (ex != null && ex.includes(m)) {
			if (debug) Log.debug("Variant: " + variant + "\n\tmarker: " + m.toStr() + "\tsstart:" + sstart + "\tsend: " + send + "\n\texon: " + ex + "\n\tstrand: " + (strandPlus ? "+" : "-"));
			seq = ex.getSequence(m);
			if (debug) Log.debug("Sequence (Exon)  [ " + sstart + " , " + send + " ]: '" + seq + "'\talt: '" + alt() + "'\tsequence (+ strand): " + (ex.isStrandPlus() ? ex.getSequence() : GprSeq.reverseWc(ex.getSequence())));
		}

		// May be it is not completely in the exon. Use genomic sequences
		if (seq == null) {
			seq = genome.getGenomicSequences().querySequence(m);
			if (debug) Log.debug("Sequence (Genome) [ " + sstart + " , " + send + " ]: '" + seq + "'\talt: '" + alt() + "'\tsequence (+ strand): " + seq);
		}

		// Compare to ALT sequence
		if (seq == null) return false; // Cannot compare

		return seq.equalsIgnoreCase(alt());
	}

	/**
	 * Is this position upstream?
	 */
	boolean isUpstream(int pos) {
		if (tr.isStrandPlus()) return pos < tr.getStart();
		return tr.getEndClosed() < pos;
	}

	/**
	 * Genomic position for exonic variants
	 */
	protected String pos() {
		int posStart = -1, posEnd = -1;

		int variantPosStart = strandPlus ? variant.getStart() : variant.getEndClosed();

		switch (variant.getVariantType()) {
		case SNP:
			posStart = posEnd = variantPosStart;
			break;

		case MNP:
			posStart = variantPosStart;
			posEnd = posStart + (strandPlus ? 1 : -1) * (variant.size() - 1);
			break;

		case INS:
			posStart = variantPosStart;
			if (duplication) {
				// Duplication coordinates
				int lenAlt = alt().length();
				if (lenAlt == 1) {
					// One base duplications do not require end positions:
					// Reference: http://www.hgvs.org/mutnomen/disc.html#dupins
					// Example: c.7dupT (or c.7dup) denotes the duplication (insertion) of a T at position 7 in the sequence ACTTACTGCC to ACTTACTTGCC
					posStart += strandPlus ? -1 : 0; // The 'previous base' is duplicated, we have to decrement the position
					posEnd = posStart;
				} else {
					// Duplication coordinates
					if (strandPlus) {
						posEnd = posStart - 1;
						posStart -= lenAlt;
					} else {
						// Insert is 'before' variant position, so we must shift one base (compared to plus strand)
						posEnd = posStart;
						posStart += lenAlt - 1;
					}
				}
			} else {
				// Other insertions must list both positions:
				// Reference: http://www.hgvs.org/mutnomen/disc.html#ins
				//            ...to prevent confusion, both flanking residues have to be listed.
				// Example: c.6_7dup (or c.6_7dupTG) denotes a TG duplication (TG insertion) in the sequence ACATGTGCC to ACATGTGTGCC
				if (strandPlus) {
					// Insert before current posStart
					posEnd = posStart;
					posStart -= 1;
				} else {
					// Insert before current posStart (negative strand)
					posEnd = posStart - 1;
				}
			}
			break;

		case BND:
		case DEL:
		case DUP:
		case INV:
		case MIXED:
			if (strandPlus) {
				posStart = variant.getStart();
				posEnd = variant.getEndClosed();
			} else {
				posStart = variant.getEndClosed();
				posEnd = variant.getStart();
			}
			break;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		// Single base
		if (posStart == posEnd) return pos(posStart);

		// Base range
		String ps = pos(posStart);
		String pe = pos(posEnd);
		if (ps == null || pe == null) return null;
		return ps + "_" + pe;
	}

	/**
	 * HGVS position base on genomic coordinates (chr is assumed to be the same as in transcript/marker).
	 */
	protected String pos(int pos) {
		// Cannot do much if there is no transcript
		if (tr == null) return Integer.toString(pos + 1);

		// Are we in an exon?
		// Note: This may come from an intron-exon boundary variant (intron side, walked in a duplication).
		//       In that case, the exon marker won't be available from 'variantEffect.marker'.
		Exon ex = tr.findExon(pos);
		if (ex != null) return posExon(pos);

		Intron intron = tr.findIntron(pos);
		if (intron != null) return posIntron(pos, intron);

		if (isDownstream(pos)) return posDownstream(pos);
		if (isUpstream(pos)) return posUpstream(pos);

		if (debug) Log.debug("Unknown HGVS position " + pos + ", transcript " + tr);
		return null;
	}

	//	/**
	//	 * Position downstream of the transcript
	//	 */
	//	protected String posDownstream(int pos) {
	//		int baseNumCdsEnd = tr.getCdsEnd();
	//		int idx = Math.abs(pos - baseNumCdsEnd);
	//
	//		return "*" + idx; // We are after stop codon, coordinates must be '*1', '*2', etc.
	//	}

	/**
	 * Position downstream of the transcript
	 */
	protected String posDownstream(int pos) {
		int trEnd = tr.isStrandPlus() ? tr.getEndClosed() : tr.getStart();
		int baseNumTrEnd = tr.baseNumber2MRnaPos(trEnd);
		int baseNumCdsEnd = tr.baseNumber2MRnaPos(tr.getCdsEnd());
		int basesFromCdsEndToTrEnd = Math.abs(baseNumTrEnd - baseNumCdsEnd);

		int idx = basesFromCdsEndToTrEnd + Math.abs(pos - trEnd);
		return "*" + idx; // We are after stop codon, coordinates must be '*1', '*2', etc.
	}

	/**
	 * Convert genomic position to HGVS compatible (DNA) position
	 */
	protected String posExon(int pos) {
		if (tr.isUtr3(pos)) return posUtr3(pos);
		if (tr.isUtr5(pos)) return posUtr5(pos);

		int idx = tr.baseNumberCds(pos, false) + 1; // Coding Exon: just use CDS position

		// Could not find dna position in transcript?
		if (idx <= 0) return null;
		return "" + idx;
	}

	/**
	 * Intronic position
	 */
	protected String posIntron(int pos, Intron intron) {
		// Jump to closest exon position
		// Ref:
		//		Beginning of the intron; the number of the last nucleotide of the
		//      preceding exon, a plus sign and the position in the intron, like
		//      c.77+1G, c.77+2T, etc.
		// 		End of the intron; the number of the first nucleotide of the following
		//      exon, a minus sign and the position upstream in the intron, like c.78-1G.
		int posExon = -1;
		String posExonStr = "";
		int distanceLeft = Math.max(0, pos - intron.getStart()) + 1;
		int distanceRight = Math.max(0, intron.getEndClosed() - pos) + 1;
		if (distanceLeft < distanceRight) {
			posExon = intron.getStart() - 1;
			posExonStr = (intron.isStrandPlus() ? "+" : "-");
		} else if (distanceRight < distanceLeft) {
			posExon = intron.getEndClosed() + 1;
			posExonStr = (intron.isStrandPlus() ? "-" : "+");
		} else {
			// Reference: in the middle of the intron, numbering changes from "c.77+.." to "c.78-.."; for introns with an uneven number of nucleotides the central nucleotide is the last described with a "+"
			posExonStr = "+";

			if (strandPlus) posExon = intron.getStart() - 1;
			else posExon = intron.getEndClosed() + 1;
		}

		// Distance to closest exonic base
		int exonDistance = Math.abs(posExon - pos);

		// Closest exonic base within coding region?
		int cdsLeft = Math.min(tr.getCdsStart(), tr.getCdsEnd());
		int cdsRight = Math.max(tr.getCdsStart(), tr.getCdsEnd());
		if ((posExon >= cdsLeft) && (posExon <= cdsRight)) {
			int distExonBase = tr.baseNumberCds(posExon, false) + 1;
			return distExonBase + (exonDistance > 0 ? posExonStr + exonDistance : "");
		}

		// Left side of coding part
		int cdnaPos = tr.baseNumber2MRnaPos(posExon);
		if (posExon < cdsLeft) {
			int cdnaStart = tr.baseNumber2MRnaPos(cdsLeft); // tr.getCdsStart());
			int utrDistance = Math.abs(cdnaStart - cdnaPos);
			String utrStr = strandPlus ? "-" : "*";
			return utrStr + utrDistance + (exonDistance > 0 ? posExonStr + exonDistance : "");
		}

		// Right side of coding part
		int cdnaEnd = tr.baseNumber2MRnaPos(cdsRight); // tr.getCdsEnd());
		int utrDistance = Math.abs(cdnaEnd - cdnaPos);
		String utrStr = strandPlus ? "*" : "-";
		return utrStr + utrDistance + (exonDistance > 0 ? posExonStr + exonDistance : "");
	}

	//	/**
	//	 * Position upstream of the transcript
	//	 */
	//	protected String posUpstream(int pos) {
	//		int tss = tr.getCdsStart();
	//		int idx = Math.abs(pos - tss);
	//
	//		if (idx <= 0) return null;
	//		return "-" + idx; // 5'UTR: We are before TSS, coordinates must be '-1', '-2', etc.
	//	}

	/**
	 * Position upstream of the transcript
	 *
	 * Note: How to calculate Upstream position:
	 * If strand is '-' as for NM_016176.3, "genomicTxStart" being the rightmost tx coord:
	 *     cDotUpstream = -(cdsStart + variantPos - genomicTxStart)
	 *
	 * Instead of "-(variantPos - genomicCdsStart)":
	 *
	 * The method that stays in transcript space until extending beyond the transcript is
	 * correct because of these statements on http://varnomen.hgvs.org/bg-material/numbering/:
	 *
	 *     * nucleotides upstream (5') of the ATG-translation initiation
	 *       codon (start) are marked with a "-" (minus) and numbered c.-1,
	 *       c.-2, c.-3, etc. (i.e. going further upstream)
	 *
	 *     * Question: When the ATG translation initiation codon is in
	 *       exon 2, and we find a variant in exon 1, should we include
	 *       intron 1 (upstream of c.-14) in nucleotide
	 *       numbering? (Isabelle Touitou, Montpellier, France)
	 *
	 *       Answer: Nucleotides in introns 5' of the ATG translation
	 *       initiation codon (i.e. in the 5'UTR) are numbered as
	 *       introns in the protein coding sequence (see coding DNA
	 *       numbering). In your example, based on a coding DNA
	 *       reference sequence, the intron is present between
	 *       nucleotides c.-15 and c.-14. The nucleotides for this
	 *       intron are numbered as c.-15+1, c.-15+2, c.-15+3, ....,
	 *       c.-14-3, c.-14-2, c.-14-1. Consequently, regarding the
	 *       question, when a coding DNA reference sequence is used,
	 *       the intronic nucleotides are not counted.
	 *
	 */
	protected String posUpstream(int pos) {
		int baseNumTss = tr.baseNumber2MRnaPos(tr.getCdsStart());
		int trStart = tr.isStrandPlus() ? tr.getStart() : tr.getEndClosed();
		int idx = baseNumTss + Math.abs(pos - trStart);

		if (idx <= 0) return null;
		return "-" + idx; // 5'UTR: We are before TSS, coordinates must be '-1', '-2', etc.
	}

	/**
	 * Position within 3'UTR
	 */
	protected String posUtr3(int pos) {
		int baseNum = tr.baseNumber2MRnaPos(pos);
		int baseNumCdsEnd = tr.baseNumber2MRnaPos(tr.getCdsEnd());
		int idx = Math.abs(baseNum - baseNumCdsEnd);

		if (idx <= 0) return null;
		return "*" + idx; // 3'UTR: We are after stop codon, coordinates must be '*1', '*2', etc.
	}

	/**
	 * Position within 5'UTR
	 */
	protected String posUtr5(int pos) {
		int baseNum = tr.baseNumber2MRnaPos(pos);
		int baseNumTss = tr.baseNumber2MRnaPos(tr.getCdsStart());
		int idx = Math.abs(baseNum - baseNumTss);

		if (idx <= 0) return null;
		return "-" + idx; // 5'UTR: We are before TSS, coordinates must be '-1', '-2', etc.
	}

	/**
	 * Translocation nomenclature.
	 * From HGVS:
	 * 		Translocations are described at the molecular level using the
	 * 		format "t(X;4)(p21.2;q34)", followed by the usual numbering, indicating
	 * 		the position translocation breakpoint. The sequences of the translocation
	 * 		breakpoints need to be submitted to a sequence database (Genbank, EMBL,
	 * 		DDJB) and the accession.version numbers should be given (see Discussion).
	 * 		E.g.:
	 * 			t(X;4)(p21.2;q35)(c.857+101_857+102) denotes a translocation breakpoint
	 * 			in the intron between coding DNA nucleotides 857+101 and 857+102, joining
	 * 			chromosome bands Xp21.2 and 4q34
	 */
	protected String prefixTranslocation() {
		VariantBnd vtr = (VariantBnd) variant;

		// Chromosome part
		String chrCoords = "(" //
				+ vtr.getChromosomeName() //
				+ ";" //
				+ vtr.getEndPoint().getChromosomeName() //
				+ ")" //
		;

		// Get cytobands
		String band1 = "";
		CytoBands cytoBands = genome.getCytoBands();
		Markers bands1 = cytoBands.query(vtr);
		if (!bands1.isEmpty()) band1 = bands1.get(0).getId(); // Get first match

		String band2 = "";
		Markers bands2 = cytoBands.query(vtr.getEndPoint());
		if (!bands2.isEmpty()) band2 = bands2.get(0).getId(); // Get first match

		String bands = "(" + band1 + ";" + band2 + ")";

		return "t" + chrCoords + bands + "(";
	}

	protected String ref() {
		return variant.getReference();
	}

	@Override
	public String toString() {
		if (variant == null || genome == null) return null;

		// Is this a duplication?
		if (variant.isIns()) duplication = isDuplication();

		String type = "", prefix = "", suffix = "";
		switch (variant.getVariantType()) {
		case INS:
			type = duplication ? "dup" : "ins";
			break;

		case DEL:
			type = "del";
			break;

		case MNP:
			type = "";
			break;

		case SNP:
		case MIXED:
		case INTERVAL:
			type = "";
			break;

		case INV:
			type = "inv";
			break;

		case DUP:
			type = "dup";
			break;

		case BND:
			prefix = prefixTranslocation();
			type = "";
			suffix = ")";
			break;

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		// HGVS formatted Position
		String pos = pos();
		if (pos == null) return null;

		// SNPs using old HGVS notation?
		if (Config.get().isHgvsOld() && type.isEmpty()) {
			String ref, alt;
			if (strandPlus) {
				ref = ref();
				alt = alt();
			} else {
				ref = GprSeq.reverseWc(ref());
				alt = GprSeq.reverseWc(alt());
			}

			// Use 'c.G123T' instead of 'c.123G>T'
			return prefix + typeOfReference() + ref + pos + alt + suffix;
		}

		return prefix + typeOfReference() + pos + type + dnaBaseChange() + suffix;
	}

	/**
	 * Prefix for coding or non-coding sequences
	 */
	protected String typeOfReference() {
		if (tr == null) return "n.";

		// Is the transcript protein coding?
		String prefix = tr.isProteinCoding() ? "c." : "n.";

		// Not using transcript ID?
		if (!hgvsTrId) return prefix;

		StringBuilder sb = new StringBuilder();
		sb.append(tr.getId());

		String ver = tr.getVersion();
		if (!ver.isEmpty()) sb.append("." + ver);

		sb.append(':');
		sb.append(prefix);

		return sb.toString();
	}

}
