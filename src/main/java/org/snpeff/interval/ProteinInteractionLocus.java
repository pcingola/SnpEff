package org.snpeff.interval;

import java.util.LinkedList;
import java.util.List;

import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Protein interaction: An amino acid that is "in contact" with another amino acid.
 * This can be either within the same protein or interacting with another protein.
 * Evidence form PDB crystallized structures
 *
 * @author pablocingolani
 */
public abstract class ProteinInteractionLocus extends Marker {

	private static final long serialVersionUID = 1L;

	public static final boolean debug = false;

	/**
	 * Create interaction
	 */
	private static ProteinInteractionLocus factory(Transcript tr, int start, int end, Transcript trInteract, String id) {
		// Interaction type
		String geneId1 = tr.getParent().getId();
		String geneId2 = trInteract.getParent().getId();

		// Intervals may be swapped for transcript on the negative strand
		int s = Math.min(start, end);
		int e = Math.max(start, end);

		// Same gene? => Within protein interaction
		if (geneId1.equals(geneId2)) return new ProteinStructuralInteractionLocus(tr, s, e, id);

		// Different genes? => Protein-protein interaction
		return new ProteinProteinInteractionLocus(tr, s, e, trInteract, id);

	}

	/**
	 * Create interaction. Most of the time it is only one interval, but
	 * if introns split an amino acid, it may be more then one interval
	 */
	public static List<ProteinInteractionLocus> factory(Transcript tr, int aaPos, Transcript trInteract, String id) {
		List<ProteinInteractionLocus> list = new LinkedList<>();

		// In most cases, bases within a codon will be adjacent, but if
		// there is an intron splitting the codon, then bases will be
		// on non-contiguous positions. In such case, we need to create
		// one interaction interval for each range of contiguous bases
		// in the codon (in theory we could end up with three different
		// intervals, but that would be quite rare
		int codon2pos[] = tr.codonNumber2Pos(aaPos);

		int j = tr.isStrandPlus() ? 0 : 2;
		int start, prev, pos;
		int step = tr.isStrandPlus() ? 1 : -1;

		pos = prev = start = codon2pos[j];
		j += step;
		while (0 <= j && j <= 2) {
			pos = codon2pos[j];
			if (pos != (prev + step)) {
				// Non-contiguous, create new interval
				list.add(factory(tr, start, prev, trInteract, id));
				start = pos;
			}
			j += step;
			prev = pos;
		}

		// Add last interval
		list.add(factory(tr, start, pos, trInteract, id));
		return list;
	}

	public ProteinInteractionLocus() {
		super();
	}

	public ProteinInteractionLocus(Transcript parent, int start, int end, String id) {
		super(parent, start, end, false, id);

	}

	@Override
	public ProteinInteractionLocus cloneShallow() {
		ProteinInteractionLocus clone = (ProteinInteractionLocus) super.cloneShallow();
		return clone;
	}

	/**
	 * Calculate the effect of this variant
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;// Sanity check
		variantEffects.add(variant, this, type, EffectImpact.HIGH, "");
		return true;
	}

}
