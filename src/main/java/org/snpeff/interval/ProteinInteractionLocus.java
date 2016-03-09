package org.snpeff.interval;

import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;

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

	public static ProteinInteractionLocus factory(Transcript parent, int aaPos, Transcript trInteract, String id) {
		// Interaction type
		String geneId1 = parent.getParent().getId();
		String geneId2 = trInteract.getParent().getId();

		int codon2pos[] = parent.codonNumber2Pos(aaPos);
		Gpr.debug("Codon2pos[" + aaPos + "]: " + codon2pos);

		// Same gene? => Within protein interaction
		if (geneId1.equals(geneId2)) return new ProteinStructuralInteractionLocus(parent, aaPos, id);

		// Different genes? => Protein-protein interaction
		return new ProteinProteinInteractionLocus(parent, aaPos, trInteract, id);
	}

	public ProteinInteractionLocus() {
		super();
	}

	public ProteinInteractionLocus(Transcript parent, int aaPos, String id) {
		super(parent, 0, 0, false, id);
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
