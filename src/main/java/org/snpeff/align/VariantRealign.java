package org.snpeff.align;

import org.snpeff.binseq.GenomicSequences;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.MarkerSeq;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Re-align a variant towards the leftmost (rightmost) position
 *
 * Note: We perform a 'progressive' realignment, asking for more
 *       reference sequence as we need it
 *
 * @author pcingola
 */
public class VariantRealign {

	public static final int INITIAL_BASES_MULTIPLIER = 3;
	public static final int INITIAL_BASES_EXTRA = 10;

	public static final int PROGRESSIVE_BASES_MULTIPLIER = 2;
	public static final int PROGRESSIVE_BASES_EXTRA = 1;

	public static final int MAX_ITERATIONS = 100;

	boolean debug = false;
	boolean alignLeft = true; // By default, align to the left
	boolean realigned; // Was the variant realigned?
	boolean needMoreBasesLeft, needMoreBasesRight; // Do we need more bases to the left / right to improve current aligment?
	char basesRef[], basesAlt[];
	int basesTrimLeft, basesTrimRight;
	int basesAddedLeft, basesAddedRight; // Add some bases to add context to variant's sequence
	int maxBasesLeft, maxBasesRight; // Maximum number of bases we can add on each side before running out of sequence
	String sequenceRef, sequenceAlt;
	String refRealign, altRealign; // Ref and Alt after realignment
	GenomicSequences genSeqs; // Provides sequences
	Genome genome; // Reference genome
	Variant variant;
	Variant variantRealigned;

	public VariantRealign() {
	}

	public VariantRealign(Variant variant) {
		genome = variant.getGenome();
		genSeqs = genome.getGenomicSequences();
		this.variant = variant;
	}

	/**
	 * Calculate how many bases to add on each side of the sequence in order to
	 * give some 'anchor' or 'context' to the variant
	 */
	boolean basesToAdd(int addBasesLeft, int addBasesRight) {
		MarkerSeq ms = genSeqs.queryMarkerSequence(variant);
		if (ms == null) return false;

		// Minimum and maximum base number to request (we only have sequence within these positions)
		maxBasesLeft = variant.getStart() - ms.getStart();
		maxBasesRight = ms.getEndClosed() - variant.getEndClosed();

		// Calculate bases to left & right
		basesAddedLeft = variant.getStart() - (variant.getStart() - addBasesLeft);
		basesAddedRight = (variant.getEndClosed() + addBasesRight) - variant.getEndClosed();

		// Make sure we don't go over limit
		basesAddedLeft = Math.min(basesAddedLeft, maxBasesLeft);
		basesAddedRight = Math.min(basesAddedRight, maxBasesRight);

		return true;
	}

	/**
	 * Create alt sequence
	 */
	boolean createAltSeq() {
		// First sequence base is variant.start
		String seqPre = sequenceRef.substring(0, basesAddedLeft); // These bases do not change
		String seqVar = sequenceRef.substring(basesAddedLeft); // This is where the variant is

		// Remove 'ref' part
		String vref = variant.getReference().toLowerCase();
		if (!vref.isEmpty()) {
			// Sanity check
			if (!seqVar.startsWith(vref)) {
				if (debug) Log.debug("Variant not found in reference sequence. This should never happen!" //
						+ "\n\tSeq: '" + seqVar //
						+ "'\n\tVariant's ref: '" + vref + "'" //
				);
				return false;
			}

			seqVar = seqVar.substring(vref.length()); // Remove 'ref' part
		}

		// Combine 'alt' part
		sequenceAlt = seqPre + variant.getAlt().toLowerCase() + seqVar;
		return true;
	}

	/**
	 * Create a new variant reflecting the realignment
	 * @return true if a new variant is created
	 */
	boolean createRealignedVariant() {
		// Calculate new coordinates
		int start = variant.getStart() - basesAddedLeft + basesTrimLeft;
		int end = variant.getEndClosed() + basesAddedRight - basesTrimRight;
		if (end < start) end = start;

		// Do we need to create a new variant?
		if (start == variant.getStart() && end == variant.getEndClosed()) return false;

		// Create new variant
		variantRealigned = new Variant(variant.getParent(), start, refRealign, altRealign, variant.getId());
		variantRealigned.setGenotype(variant.getGenotype());

		return true;
	}

	/**
	 * Create reference sequence
	 */
	boolean createRefSeq() {
		Marker m = new Marker(variant.getChromosome(), variant.getStart() - basesAddedLeft, variant.getEndClosed() + basesAddedRight);
		sequenceRef = genSeqs.querySequence(m);
		return sequenceRef != null;
	}

	public String getAltRealign() {
		return altRealign;
	}

	public String getRefRealign() {
		return refRealign;
	}

	public Variant getVariantRealigned() {
		return variantRealigned;
	}

	/**
	 * Do we need more bases to the left or right?
	 * Sets 'needMoreBasesRight' to indicate that it might have trimmed more bases (we run out of sequence).
	 * Sets 'needMoreBasesLeft' to indicate that it might have trimmed more bases (we run out of sequence).
	 */
	boolean needMoreBases() {
		needMoreBasesLeft = (basesTrimLeft == 0);
		needMoreBasesRight = (basesTrimRight == 0);
		return needMoreBasesLeft || needMoreBasesRight;
	}

	/**
	 * Realign variant
	 *
	 * @return	true if variant was realigned and a new variant (different than
	 * 			the original one) was created. false if it wasn't realigned or
	 * 			there was an error
	 */
	public boolean realign() {
		int basesAddedLeftPrev = 0, basesAddedRightPrev = 0;

		// Progressive realignment
		// Require more bases to the right or left if needed
		boolean needMoreBases = true;
		for (int i = 0; (i < MAX_ITERATIONS) && needMoreBases; i++) {
			//---
			// Calculate how many bases to add
			//---
			if (i == 0) {
				// First iteration? Initialize using a 'guess' and let basesToAdd() method make a proper calculation
				int maxVarLen = INITIAL_BASES_MULTIPLIER * Math.max(variant.getReference().length(), variant.getAlt().length());
				basesAddedLeft = basesAddedRight = Math.max(maxVarLen, INITIAL_BASES_EXTRA);
			} else {
				// Increment values
				basesAddedLeft = PROGRESSIVE_BASES_MULTIPLIER * basesAddedLeft + PROGRESSIVE_BASES_EXTRA;
				basesAddedRight = PROGRESSIVE_BASES_MULTIPLIER * basesAddedRight + PROGRESSIVE_BASES_EXTRA;
			}

			if (debug) Log.debug("Bases\tleft: " + basesAddedLeft + (needMoreBasesLeft ? " [more]" : "") + "\tright: " + basesAddedRight + (needMoreBasesRight ? " [more]" : ""));
			// Can we add those many bases?
			if (!basesToAdd(basesAddedLeft, basesAddedRight)) return false;

			// Did we add more bases since last iteration? Otherwise we are not making any progress
			if (needMoreBasesLeft && basesAddedLeftPrev == basesAddedLeft) break;
			if (needMoreBasesRight && basesAddedRightPrev == basesAddedRight) break;

			//---
			// Align
			//---
			// Create ref and alt sequences
			if (!createRefSeq()) return false;
			if (!createAltSeq()) return false;

			// Realign
			realignSeqs();

			// Prepare for next iteration
			needMoreBases = needMoreBases();
			basesAddedLeftPrev = basesAddedLeft;
			basesAddedRightPrev = basesAddedRight;
		}

		// Create new variant
		realigned = createRealignedVariant();
		if (debug) Log.debug("Realign:\n" + this);
		return realigned;
	}

	/**
	 * Realignment
	 */
	public void realignSeqs() {
		// Initialize
		basesTrimLeft = basesTrimRight = 0;

		// Create ref and alt bases
		basesRef = sequenceRef.toCharArray();
		basesAlt = sequenceAlt.toCharArray();

		// Calculate how many bases to remove form each end
		if (alignLeft) {
			basesTrimLeft = trimBasesLeft();
			basesTrimRight = trimBasesRight();
		} else {
			basesTrimRight = trimBasesRight();
			basesTrimLeft = trimBasesLeft();
		}

		// Calculate new 'ref' and 'alt'
		refRealign = trimedSequence(sequenceRef).toUpperCase();
		altRealign = trimedSequence(sequenceAlt).toUpperCase();
	}

	public void setAlignLeft() {
		alignLeft = true;
	}

	public void setAlignRight() {
		alignLeft = false;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setSequenceAlt(String sequenceAlt) {
		this.sequenceAlt = sequenceAlt;
	}

	public void setSequenceRef(String sequenceRef) {
		this.sequenceRef = sequenceRef;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Realigned: " + (realigned ? "Yes" : "No") + "\n");
		sb.append("\tVariant (original)   : " + variant + "\n");
		sb.append("\tVariant (realinged)  : " + variantRealigned + "\n");
		sb.append("\tReference sequence   : '" + sequenceRef + "'\tlen: " + sequenceRef.length() + "\n");
		sb.append("\tAlternative sequence : '" + sequenceAlt + "'\tlen: " + sequenceAlt.length() + "\n");
		sb.append("\tRef (after realign)  : '" + refRealign + "'\n");
		sb.append("\tAlt (after realign)  : '" + altRealign + "'\n");
		sb.append("\tBases added          : left: " + basesAddedLeft + ", right: " + basesAddedRight + "\n");
		sb.append("\tIndexes              : left: " + basesTrimLeft + ", right: " + basesTrimRight + "\n");
		if (needMoreBasesLeft) sb.append("\tWARNING: Needs more bases to the left.\n");
		if (needMoreBasesRight) sb.append("\tWARNING: Needs more bases to the right.\n");
		return sb.toString();
	}

	/**
	 * Calculate 'left' indexes by removing identical bases from the left end
	 *
	 * @return	Positive number to indicate the number of bases trimmed.
	 */
	int trimBasesLeft() {
		int bases = 0;
		for (int refIdx = 0, altIdx = 0; refIdx < sequenceRef.length() && altIdx < sequenceAlt.length(); refIdx++, altIdx++, bases++)
			if (basesRef[refIdx] != basesAlt[altIdx]) return bases;
		return bases;
	}

	/**
	 * Calculate 'right' indexes by removing identical bases from the right end
	 *
	 * @return	Positive number to indicate the number of bases trimmed.
	 */
	int trimBasesRight() {
		int bases = 0;
		for (int refIdx = basesRef.length - 1, altIdx = basesAlt.length - 1; refIdx >= basesTrimLeft && altIdx >= basesTrimLeft; refIdx--, altIdx--, bases++)
			if (basesRef[refIdx] != basesAlt[altIdx]) return bases;
		return bases;
	}

	String trimedSequence(String seq) {
		int end = seq.length() - basesTrimRight;
		if (basesTrimLeft <= end) return seq.substring(basesTrimLeft, end);
		return "";
	}
}
