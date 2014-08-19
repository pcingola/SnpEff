package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Intron;
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
			if (marker == null || marker.isStrandPlus()) return variant.getReference() + ">" + variant.getAlt();
			return GprSeq.reverseWc(variant.getReference()) + ">" + GprSeq.reverseWc(variant.getAlt());

		case INS:
		case DEL:
			String netChange = variant.netChange(false);
			if (marker == null || marker.isStrandPlus()) return netChange;
			return GprSeq.reverseWc(netChange);

		case MIXED:
			if (marker == null || marker.isStrandPlus()) return "del" + variant.getReference() + "ins" + variant.getAlt();
			return "del" + GprSeq.reverseWc(variant.getReference()) + "ins" + GprSeq.reverseWc(variant.getAlt());

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}
	}

	/**
	 * Calculate position
	 */
	protected String pos() {
		// Intron
		if (variantEffect.isIntron()) {
			switch (variant.getVariantType()) {
			case SNP:
			case MNP:
				return posIntron(variant.getStart());

			case INS:
				String p = posIntron(variant.getStart());
				if (p == null) return null;
				int next = variant.getStart() + (marker.isStrandPlus() ? 1 : -1);
				String pNext = posIntron(next);
				if (pNext == null) return null;
				return p + "_" + pNext;

			case DEL:
				p = posIntron(variant.getStart());
				if (p == null) return null;
				pNext = posIntron(variant.getEnd());
				if (pNext == null) return null;
				return p + "_" + pNext;

			case MIXED:
				p = posIntron(variant.getStart());
				if (p == null) return null;
				pNext = posIntron(variant.getEnd());
				if (pNext == null) return null;
				return p + "_" + pNext;

			case INTERVAL:
				return "";

			default:
				throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
			}
		}

		String posPrepend = "";

		// Exon position
		int codonNum = variantEffect.getCodonNum();
		int posStart = -1, posEnd = -1;
		if (codonNum >= 0) posStart = codonNum * 3 + variantEffect.getCodonIndex() + 1;
		else {
			if (tr == null) return null;

			int variantPos = tr.isStrandPlus() ? variant.getStart() : variant.getEnd();
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
				// posStart = tr.baseNumberPreMRna(variant.getStart()) + 1;
			}
		}

		// Could not find dna position in transcript?
		if (posStart <= 0) return null;

		switch (variant.getVariantType()) {
		case SNP:
		case MNP:
			return posPrepend + posStart;

		case INS:
			posEnd = posStart + 1;
			return posPrepend + posStart + "_" + posPrepend + posEnd;

		case DEL:
		case MIXED:
			posEnd = posStart + variant.size() - 1;
			return posPrepend + posStart + "_" + posPrepend + posEnd;

		case INTERVAL:
			return "";

		default:
			throw new RuntimeException("Unimplemented method for variant type " + variant.getVariantType());
		}
	}

	/**
	 * Intronic position
	 */
	protected String posIntron(int pos) {
		Intron intron = (Intron) variantEffect.getMarker();
		if (intron == null) return null;

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

			if (tr.isStrandPlus()) posExon = intron.getStart() - 1;
			else posExon = intron.getEnd() + 1;
		}

		// Distance to closest exonic base
		int exonDistance = Math.abs(posExon - pos);

		// Closest exonic base within coding region?
		int cdsLeft = Math.min(tr.getCdsStart(), tr.getCdsEnd());
		int cdsRight = Math.max(tr.getCdsStart(), tr.getCdsEnd());
		if ((posExon >= cdsLeft) && (posExon <= cdsRight)) {
			int distExonBase = tr.baseNumberCds(posExon, false) + 1;
			return distExonBase + posExonStr + exonDistance;
		}

		// Left side of coding part
		int cdnaPos = tr.baseNumberPreMRna(posExon);
		if (posExon < cdsLeft) {
			int cdnaStart = tr.baseNumberPreMRna(cdsLeft); // tr.getCdsStart());
			int utrDistance = Math.abs(cdnaStart - cdnaPos);
			String utrStr = tr.isStrandPlus() ? "-" : "*";
			return utrStr + utrDistance + posExonStr + exonDistance;
		}

		// Right side of coding part
		int cdnaEnd = tr.baseNumberPreMRna(cdsRight); // tr.getCdsEnd());
		int utrDistance = Math.abs(cdnaEnd - cdnaPos);
		String utrStr = tr.isStrandPlus() ? "*" : "-";
		return utrStr + utrDistance + posExonStr + exonDistance;
	}

	@Override
	public String toString() {
		if (variant == null) return null;

		String pos = pos();
		if (pos == null) return null;

		String type = "";
		switch (variant.getVariantType()) {
		case INS:
			type = "ins";
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
