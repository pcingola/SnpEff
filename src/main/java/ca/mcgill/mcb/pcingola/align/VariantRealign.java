package ca.mcgill.mcb.pcingola.align;

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
	int basesTrimLeft, basesTrimRight;
	int basesAddedLeft, basesAddedRight; // Add some bases to add context to variant's sequence
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
		for (int refIdx = basesRef.length - 1, altIdx = basesAlt.length - 1; refIdx >= basesTrimLeft && altIdx >= basesTrimLeft; refIdx--, altIdx--, bases++)
			if (basesRef[refIdx] != basesAlt[altIdx]) return bases;

		return bases;
	}

	/**
	 * Calculate how many bases to add on each side of the sequence in order to 
	 * give some 'anchor' or 'context' to the variant
	 */
	void basesToAdd() {
		int maxVarLen = BASES_EXTRA_MULTIPLIER * Math.max(variant.getReference().length(), variant.getAlt().length());
		int addBases = Math.min(maxVarLen, MIN_BASES_EXTRA);
		basesAddedLeft = variant.getStart() - Math.max(0, variant.getStart() - addBases);
		basesAddedRight = Math.min(variant.getChromosome().size() - 1, variant.getEnd() + addBases) - variant.getEnd();
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
			if (!seqVar.startsWith(vref)) throw new RuntimeException("Variant not found in reference sequence. This should never happen!" //
					+ "\n\tSeq: '" + seqVar //
					+ "'\n\tVariant's ref: '" + vref + "'" //
			);

			seqVar = seqVar.substring(vref.length()); // Remove 'ref' part
		}

		// Combine 'alt' part
		sequenceAlt = seqPre + variant.getAlt() + seqVar;
		Gpr.debug("Sequence alt: " + sequenceAlt);
		return true;
	}

	/**
	 * Create reference sequence
	 */
	boolean createRefSeq() {
		Marker m = new Marker(variant.getChromosome(), variant.getStart() - basesAddedLeft, variant.getEnd() + basesAddedRight);
		sequenceRef = genSeqs.getSequence(m);
		Gpr.debug("start: " + basesAddedLeft + "\tend:" + basesAddedRight + "\tseq: " + sequenceRef);
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
		// Calculate how many bases we can add in order to add context to the alignment
		basesToAdd();

		if (!createRefSeq()) return false;
		if (!createAltSeq()) return false;

		realigned = realignSeqs();

		return realigned;
	}

	/**
	 * Realignment
	 */
	public boolean realignSeqs() {
		// Initialize
		basesTrimLeft = basesTrimRight = 0;

		// Create ref and alt bases
		basesRef = sequenceRef.toCharArray();
		basesAlt = sequenceAlt.toCharArray();

		// Calculate how many bases to remove form each end
		if (alignLeft) {
			basesTrimLeft = basesLeft();
			basesTrimRight = basesRight();
		} else {
			basesTrimRight = basesRight();
			basesTrimLeft = basesLeft();
		}

		if (basesTrimLeft < 0 || basesTrimRight < 0) return false;

		// Calculate new 'ref' and 'alt'
		refRealign = trimedSequence(sequenceRef).toUpperCase();
		altRealign = trimedSequence(sequenceAlt).toUpperCase();

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
		sb.append("\tBases added          : left: " + basesAddedLeft + ", right: " + basesAddedRight + "\n");
		sb.append("\tIndexes              : left: " + basesTrimLeft + ", right: " + basesTrimRight + "\n");
		sb.append("\tRef (after realign)  : '" + refRealign + "'\n");
		sb.append("\tAlt (after realign)  : '" + altRealign + "'\n");
		if (warningReachedEndOfSequence) sb.append("\tWARNING: End of genomic sequences. Unable to realign further.\n");
		return sb.toString();
	}

	String trimedSequence(String seq) {
		int end = seq.length() - basesTrimRight;
		if (basesTrimLeft <= end) return seq.substring(basesTrimLeft, end);
		return "";
	}
}
