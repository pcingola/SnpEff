package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.pdb.DistanceResult;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Protein interaction: An amino acid that is "in contact" with another amino acid.
 * This can be either within the same protein or interacting with another protein.
 * Evidence form PDB crystallized structures
 *
 * @author pablocingolani
 */
public class ProteinInteractionLocus extends Marker {

	private static final long serialVersionUID = 1L;

	public static final boolean debug = false;
	DistanceResult distanceResult;

	public ProteinInteractionLocus() {
		super();
		type = EffectType.PROTEIN_INTERACTION_LOCUS;
	}

	public ProteinInteractionLocus(Transcript parent, int start, String id) {
		super(parent, start, start, false, id);
		type = EffectType.PROTEIN_INTERACTION_LOCUS;
	}

	@Override
	public ProteinInteractionLocus cloneShallow() {
		ProteinInteractionLocus clone = (ProteinInteractionLocus) super.cloneShallow();
		clone.distanceResult = distanceResult;
		return clone;
	}

	/**
	 * Calculate the effect of this variant
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;// Sanity check

		variantEffects.add(variant, this, EffectType.PROTEIN_INTERACTION_LOCUS, EffectImpact.HIGH, "");

		return true;
	}

}
