package ca.mcgill.mcb.pcingola.interval;

/**
 * Type of frame calculations
 * Internally, we use GFF style frame calculation for Exon / Transcript
 */
public enum FrameType {

	GFF, UCSC, UNKNOWN;

	public int convertFrame(int frame) {
		// If this is a GFF frame type, no conversion is required
		if (this == GFF) return frame;

		// Convert UCSC frame numbers to GFF frame numbers
		if (this == UCSC) {
			switch (frame) {
			case 0:
				return 0;

			case 1:
				return 2;

			case 2:
				return 1;

			default:
				return -1;
			}
		}

		// Other frame type?
		throw new RuntimeException("Unknown frame type '" + this + "'");
	}

	/**
	 * Calculate frame from cds length
	 */
	public int frameFromLength(int length) {
		return frameFromLengthGff(length);
	}

	/**
	 * Calculate frame (as specified in GTF / GFF) using sequence length
	 * References: http://mblab.wustl.edu/GTF22.html
	 *
	 * What frame means:
	 * 		'0' indicates that the specified region is in frame, i.e. that
	 * 			its first base corresponds to the first base of a codon.
	 *
	 * 		'1' indicates that there is one extra base, i.e. that the
	 * 			second base of the region corresponds to the first base of a codon
	 *
	 * 		'2' means that the third base of the region is the first base of a
	 * 			codon.
	 *
	 * If the strand is '-', then the first base of the region is value of 'end', because
	 * the corresponding coding region will run from <end> to <start> on the reverse strand.
	 *
	 * Frame is calculated as (3 - ((length-frame) mod 3)) mod 3:
	 * Here is why:
	 * 		(length-frame) is the length of the previous feature starting at the first whole codon (and thus the frame subtracted out).
	 * 		(length-frame) mod 3 is the number of bases on the 3' end beyond the last whole codon of the previous feature.
	 * 		3-((length-frame) mod 3) is the number of bases left in the codon after removing those that are represented at the 3' end of the feature.
	 * 		(3-((length-frame) mod 3)) mod 3 changes a 3 to a 0, since three bases makes a whole codon, and 1 and 2 are left unchanged.
	 *
	 */
	int frameFromLengthGff(int length) {
		return (3 - (length % 3)) % 3;
	}

	/**
	 * Calculate frame (as specified in RefSeq / UCSC) using sequence length
	 */
	int frameFromLengthUcsc(int length) {
		return length % 3;
	}

}
