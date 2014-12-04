package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Re-align a variant towards the leftmost (rightmost) position
 *
 * @author pcingola
 */
public class VariantRealign {

	public static final int MIN_BASES_EXTRA = 10;
	public static final int BASES_EXTRA_MULTIPLIER = 5;

	boolean alignLeft = true; // By default, align to the left
	boolean warningReachedEndOfSequence; // Did we reached the end of the sequence (i.e. GenomicSequences could not provide sequences to continue realignment)
	boolean realigned; // Was the variant realigned?
	char basesRef[], basesAlt[];
	int basesLeft, basesRight;
	String sequenceRef, sequenceAlt;
	String refRealign, altRealign; // Ref and Alt after realignment
	GenomicSequences genSeqs; // Provides sequences
	Variant variant;
	Variant variantRealigned;

	public VariantRealign() {
	}

	public VariantRealign(GenomicSequences genSeqs, Variant variant) {
		this.genSeqs = genSeqs;
		this.variant = variant;
	}

	/**
	 * Calculate 'left' indexes by removing identical bases from the left end
	 */
	int basesLeft() {
		int bases = 0;
		for (int refIdx = 0, altIdx = 0; refIdx < sequenceRef.length() && altIdx < sequenceAlt.length(); refIdx++, altIdx++, bases++)
			if (basesRef[refIdx] != basesAlt[altIdx]) return bases;

		return bases;
	}

	/**
	 * Calculate 'right' indexes by removing identical bases from the right end
	 */
	int basesRight() {
		int bases = 0;
		for (int refIdx = basesRef.length - 1, altIdx = basesAlt.length - 1; refIdx >= basesLeft && altIdx >= basesLeft; refIdx--, altIdx--, bases++) {
			Gpr.debug("bases: " + bases + "\t" + basesRef[refIdx] + "\t" + basesAlt[altIdx]);
			if (basesRef[refIdx] != basesAlt[altIdx]) return bases;
		}

		return bases;
	}

	/**
	 * Create alt sequence
	 */
	boolean createAltSeq(int addBases) {
		// First sequence base is variant.start
		String seqPre = sequenceRef.substring(0, addBases); // These bases do not change
		String seqVar = sequenceRef.substring(addBases); // This is where the variant is

		// Remove 'ref' part
		String vref = variant.getReference();
		if (!vref.isEmpty()) {
			// Saniti check
			if (!sequenceRef.startsWith(vref)) throw new RuntimeException("Variant not founr in reference sequence. This should never happen!");

			seqVar = seqVar.substring(vref.length() - 1); // Remove 'ref' part
		}

		// Combine 'alt' part
		sequenceAlt = seqPre + variant.getAlt() + seqVar;
		return true;
	}

	/**
	 * Create reference sequence
	 */
	boolean createRefSeq(int addBases) {
		Marker m = new Marker(variant.getChromosome(), variant.getStart() - addBases, variant.getEnd() + addBases);
		sequenceRef = genSeqs.getSequence(m);
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
	 * Realign variant
	 * @return	true if variant was realigned and a new variant (different than
	 * 			the original one) was created. false if it wasn't realigned or
	 * 			there was an error
	 */
	public boolean realign() {
		int maxVarLen = BASES_EXTRA_MULTIPLIER * Math.max(variant.getReference().length(), variant.getAlt().length());
		int addBases = Math.min(maxVarLen, MIN_BASES_EXTRA);

		if (!createRefSeq(addBases)) return false;
		if (!createAltSeq(addBases)) return false;

		realigned = realignSeqs();

		return realigned;
	}

	/**
	 * Realignment
	 */
	public boolean realignSeqs() {
		// Initialize
		basesLeft = basesRight = 0;

		// Create ref and alt bases
		basesRef = sequenceRef.toCharArray();
		basesAlt = sequenceAlt.toCharArray();

		// Calculate how many bases to remove form each end
		if (alignLeft) {
			basesLeft = basesLeft();
			basesRight = basesRight();
		} else {
			basesRight = basesRight();
			basesLeft = basesLeft();
		}

		if (basesLeft < 0 || basesRight < 0) return false;

		// Calculate new 'ref' and 'alt'
		Gpr.debug(this);
		refRealign = trimedSequence(sequenceRef);
		altRealign = trimedSequence(sequenceAlt);

		return false;
	}

	public void setAlignLeft() {
		alignLeft = true;
	}

	public void setAlignRight() {
		alignLeft = false;
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
		sb.append("\tVariant (nrealinged) : " + variantRealigned + "\n");
		sb.append("\tReference sequence   : '" + sequenceRef + "'\n");
		sb.append("\tAlternative sequence : '" + sequenceAlt + "'\n");
		sb.append("\tIndexes              : left: " + basesLeft + ", right: " + basesRight + "\n");
		sb.append("\tRef (after realign)  : '" + refRealign + "'\n");
		sb.append("\tAlt (after realign)  : '" + altRealign + "'\n");
		if (warningReachedEndOfSequence) sb.append("\tWARNING: End of genomic sequences. Unable to realign further.\n");
		return sb.toString();
	}

	String trimedSequence(String seq) {
		int end = seq.length() - basesRight;
		if (basesLeft <= end) return seq.substring(basesLeft, end);
		return "";
	}
}
