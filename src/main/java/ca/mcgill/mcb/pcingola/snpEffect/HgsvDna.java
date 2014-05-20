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

public class HgsvDna extends Hgsv {

	public HgsvDna(ChangeEffect changeEffect) {
		super(changeEffect);
	}

	/**
	 * Prefix for coding or non-coding sequences
	 */
	protected String codingPrefix() {
		return (tr.isProteinCoding() ? "c." : "n.");
	}

	/**
	 * DNA level base changes
	 * @return
	 */
	protected String dnaBaseChange() {

		switch (seqChange.getChangeType()) {
		case SNP:
		case MNP:
			if (marker == null || marker.isStrandPlus()) return seqChange.getReference() + ">" + seqChange.getChange();
			return GprSeq.wc(seqChange.getReference()) + ">" + GprSeq.wc(seqChange.getChange());

		case INS:
		case DEL:
			return seqChange.netChange(1);

		default:
			return null;
		}
	}

	/**
	 * Calculate position
	 * @return
	 */
	protected String pos() {
		// Intron
		if (changeEffect.isIntron()) {
			switch (seqChange.getChangeType()) {
			case SNP:
			case MNP:
				return posIntron(seqChange.getStart());

			case INS:
				int next = seqChange.getStart() + (marker.isStrandPlus() ? 1 : -1);
				return posIntron(seqChange.getStart()) + "_" + posIntron(next);

			case DEL:

			default:
				return null;
			}
		}

		// Exon position
		int codonNum = changeEffect.getCodonNum();
		if (codonNum < 0) return null;
		int seqPos = codonNum * 3 + changeEffect.getCodonIndex() + 1;

		switch (seqChange.getChangeType()) {
		case SNP:
		case MNP:
			return "" + seqPos;

		case INS:
		case DEL:
			return seqPos + "_" + (seqPos + 1);

		default:
			return null;
		}
	}

	/**
	 * Intronic position
	 */
	protected String posIntron(int pos) {
		Intron intron = (Intron) changeEffect.getMarker();
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
		if (seqChange == null) return null;

		String pos = pos();
		if (pos == null) return null;

		String type = "";
		switch (seqChange.getChangeType()) {
		case INS:
			type = "ins";
			break;

		case DEL:
			type = "del";
			break;

		default:
			break;
		}

		return codingPrefix() + pos + type + dnaBaseChange();
	}

}
