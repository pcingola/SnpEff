package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.pdb.DistanceResult;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Protein interaction: An amino acid that is "in contact" with another amino acid.
 * This can be either within the same protein or interacting with another protein.
 * Evidence form PDB crystalized structures
 *
 * @author pablocingolani
 */
public class ProteinInteractionAa extends Marker {

	private static final long serialVersionUID = 1L;

	public static final boolean debug = false;
	DistanceResult distanceResult;

	public ProteinInteractionAa() {
		super();
		type = EffectType.PROTEIN_INTERACTION_LOCUS;
	}

	public ProteinInteractionAa(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
		type = EffectType.PROTEIN_INTERACTION_LOCUS;
	}

	@Override
	public ProteinInteractionAa cloneShallow() {
		ProteinInteractionAa clone = (ProteinInteractionAa) super.cloneShallow();
		clone.distanceResult = distanceResult;
		return clone;
	}

	/**
	 * Calculate the effect of this variant
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;// Sanity check

		if (variant.isDel() && variant.includes(this)) {
			// Site deleted?
			variantEffects.add(variant, this, EffectType.PROTEIN_INTERACTION_LOCUS_DELETED, EffectImpact.HIGH, "");
		} else {
			variantEffects.add(variant, this, EffectType.PROTEIN_INTERACTION_LOCUS, EffectImpact.HIGH, "");
		}

		return true;
	}

}
