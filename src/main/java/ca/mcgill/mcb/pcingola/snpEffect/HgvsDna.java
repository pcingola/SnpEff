package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteRegion;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

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

	/**
	 * Prefix for coding or non-coding sequences
	 */
	protected String codingPrefix() {
		return (tr.isProteinCoding() ? "c." : "n.");
	}

	/**
	 * DNA level base changes
	 */
	protected String dnaBaseChange() {

		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			if (strandPlus) return variant.getReference() + ">" + variant.getAlt();
			return GprSeq.reverseWc(variant.getReference()) + ">" + GprSeq.reverseWc(variant.getAlt());

		case INS:
		case DEL:
			if (variant.size() > MAX_SEQUENCE_LEN_HGVS) return "";
			String netChange = variant.netChange(false);
			if (strandPlus) return netChange;
			return GprSeq.reverseWc(netChange);

		case MIXED:
			if (strandPlus) return "del" + variant.getReference() + "ins" + variant.getAlt();
			return "del" + GprSeq.reverseWc(variant.getReference()) + "ins" + GprSeq.reverseWc(variant.getAlt());

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}
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
		Exon ex = variantEffect.getExon();
		if (ex != null) {
			// Get sequence at the 3'end of the variant
			int sstart, send;
			int len = variant.getAlt().length();

			if (strandPlus) {
				sstart = variant.getStart() - len;
				send = variant.getStart() - 1;
			} else {
				sstart = variant.getStart();
				send = sstart + (len - 1);
			}

			Marker m = new Marker(variant.getParent(), sstart, send, false, "");
			if (debug) Gpr.debug("variant: " + variant + "\n\tmarker: " + m.toStr() + "\tsstart:" + sstart + "\tsend: " + send + "\n\texon: " + ex + "\n\tstrand: " + (strandPlus ? "+" : "-"));
			seq = ex.getSequence(m);
			if (debug) Gpr.debug("SEQUENCE [ " + sstart + " , " + send + " ]: '" + seq + "'\talt: '" + variant.getAlt() + "'\tsequence (+ strand): " + (ex.isStrandPlus() ? ex.getSequence() : GprSeq.reverseWc(ex.getSequence())));
		}

		// Compare to ALT sequence
		if (seq == null) return false; // Cannot compare
		return seq.equalsIgnoreCase(variant.getAlt());
	}

	/**
	 * Calculate position
	 */
	protected String pos() {
		// Intron
		if (variantEffect.isIntron() //
				|| variantEffect.isSpliceSiteCore() //
				|| (variantEffect.isSpliceSiteRegion() && ((SpliceSiteRegion) variantEffect.getMarker()).isIntronPart()) //
				) return posIntron();

		return posExon();
	}

	/**
	 * Genomic position for exonic variants
	 */
	protected String posExon() {
		if (tr == null) return null;
		String posPrepend = "";

		// Exon position
		int posStart = -1, posEnd = -1;

		int variantPos = strandPlus ? variant.getStart() : variant.getEnd();
		if (variantEffect.isUtr3()) {
			// We are after stop codon, coordinates must be '*1', '*2', etc.
			int pos = tr.baseNumberPreMRna(variantPos);
			int posStop = tr.baseNumberPreMRna(tr.getCdsEnd());
			posStart = Math.abs(pos - posStop);
			posPrepend = "*";
		} else if (variantEffect.isUtr5()) {
			// We are before TSS, coordinates must be '-1', '-2', etc.
			int pos = tr.baseNumberPreMRna(variantPos);
			int posTss = tr.baseNumberPreMRna(tr.getCdsStart());
			posStart = Math.abs(pos - posTss);
			posPrepend = "-";
		} else {
			posStart = tr.baseNumberCds(variantPos, false) + 1;
		}

		// Could not find dna position in transcript?
		if (posStart <= 0) return null;

		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			return posPrepend + posStart;

		case INS:
			if (duplication) {
				// Duplication coordinates
				int lenAlt = variant.getAlt().length();
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
						//						posStart--;
						posEnd = posStart + lenAlt - 1;
					}
				}
			} else {
				// Other insertions must list both positions:
				// Reference: http://www.hgvs.org/mutnomen/disc.html#ins
				//            ...to prevent confusion, both flanking residues have to be listed.
				// Example: c.6_7dup (or c.6_7dupTG) denotes a TG duplication (TG insertion) in the sequence ACATGTGCC to ACATGTGTGCC
				posEnd = posStart + 1;
			}

			break;

		case DEL:
		case MIXED:
			posEnd = posStart + variant.size() - 1;
			break;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		if (posStart == posEnd) return posPrepend + posStart;
		return posPrepend + posStart + "_" + posPrepend + posEnd;

	}

	/**
	 * Genomic position for intronic variants
	 */
	protected String posIntron() {
		int start, end;

		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			return posIntron(variant.getStart());

		case INS:
			start = variant.getStart();

			if (duplication && variant.getAlt().length() == 1) {
				// One base duplications do not require end positions:
				// Reference: http://www.hgvs.org/mutnomen/disc.html#dupins
				// Example: c.7dupT (or c.7dup) denotes the duplication (insertion) of a T at position 7 in the sequence ACTTACTGCC to ACTTACTTGCC
				end = variant.getStart();
			} else {
				// Other insertions must list both positions:
				// Reference: http://www.hgvs.org/mutnomen/disc.html#ins
				//            ...to prevent confusion, both flanking residues have to be listed.
				// Example: c.6_7dup (or c.6_7dupTG) denotes a TG duplication (TG insertion) in the sequence ACATGTGCC to ACATGTGTGCC
				end = variant.getStart() + (strandPlus ? 1 : -1);
			}

			break;

		case DEL:
		case MIXED:
			start = variant.getStart();
			end = variant.getEnd();
			break;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		// Calculate positions and create string
		String posStart = posIntron(start);
		if (posStart == null) return null;

		String posEnd = posIntron(end);
		if (posEnd == null) return null;

		if (posStart.equals(posEnd)) return posStart;
		return strandPlus ? posStart + "_" + posEnd : posEnd + "_" + posStart;
	}

	/**
	 * Intronic position
	 */
	protected String posIntron(int pos) {
		Marker marker = variantEffect.getMarker();
		Intron intron = (Intron) marker.findParent(Intron.class);
		if (intron == null) {
			Gpr.debug("variantEffect: " + variantEffect //
					+ "\n\tMarker: " + variantEffect.getMarker() //
					);
			return null;
		}

		// Jump to closest exon position
		// Ref:
		//		beginning of the intron; the number of the last nucleotide of the preceding exon, a plus sign and the position in the intron, like c.77+1G, c.77+2T, etc.
		// 		end of the intron; the number of the first nucleotide of the following exon, a minus sign and the position upstream in the intron, like c.78-1G.
		int posExon = -1;
		String posExonStr = "";
		int distanceLeft = Math.max(0, pos - intron.getStart()) + 1;
		int distanceRight = Math.max(0, intron.getEnd() - pos) + 1;
		if (distanceLeft < distanceRight) {
			posExon = intron.getStart() - 1;
			posExonStr = (intron.isStrandPlus() ? "+" : "-");
		} else if (distanceRight < distanceLeft) {
			posExon = intron.getEnd() + 1;
			posExonStr = (intron.isStrandPlus() ? "-" : "+");
		} else {
			// Reference: in the middle of the intron, numbering changes from "c.77+.." to "c.78-.."; for introns with an uneven number of nucleotides the central nucleotide is the last described with a "+"
			posExonStr = "+";

			if (strandPlus) posExon = intron.getStart() - 1;
			else posExon = intron.getEnd() + 1;
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
		int cdnaPos = tr.baseNumberPreMRna(posExon);
		if (posExon < cdsLeft) {
			int cdnaStart = tr.baseNumberPreMRna(cdsLeft); // tr.getCdsStart());
			int utrDistance = Math.abs(cdnaStart - cdnaPos);
			String utrStr = strandPlus ? "-" : "*";
			return utrStr + utrDistance + (exonDistance > 0 ? posExonStr + exonDistance : "");
		}

		// Right side of coding part
		int cdnaEnd = tr.baseNumberPreMRna(cdsRight); // tr.getCdsEnd());
		int utrDistance = Math.abs(cdnaEnd - cdnaPos);
		String utrStr = strandPlus ? "*" : "-";
		return utrStr + utrDistance + (exonDistance > 0 ? posExonStr + exonDistance : "");
	}

	@Override
	public String toString() {
		if (variant == null) return null;

		// Is this a duplication?
		if (variant.isIns()) duplication = isDuplication();

		String pos = pos();
		if (pos == null) return null;

		String type = "";
		switch (variant.getVariantType()) {
		case INS:
			type = duplication ? "dup" : "ins";
			break;

		case DEL:
			type = "del";
			break;

		case MNP:
		case SNP:
		case MIXED:
		case INTERVAL:
			type = "";
			break;

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}

		return codingPrefix() + pos + type + dnaBaseChange();
	}

}
