package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Protein interaction: An amino acid that is "in contact" with another amino acid
 * within the same protein. Evidence form PDB crystallized structures
 *
 * @author pablocingolani
 */
public class ProteinProteinInteractionLocus extends ProteinInteractionLocus {

	private static final long serialVersionUID = -2111056845758378137L;

	Transcript trInteract;

	public ProteinProteinInteractionLocus() {
		super();
		type = EffectType.PROTEIN_PROTEIN_INTERACTION_LOCUS;
	}

	public ProteinProteinInteractionLocus(Transcript parent, int start, int end, Transcript trInteract, String id) {
		super(parent, start, end, id);
		this.trInteract = trInteract;
		type = EffectType.PROTEIN_PROTEIN_INTERACTION_LOCUS;
	}

	@Override
	public ProteinProteinInteractionLocus cloneShallow() {
		ProteinProteinInteractionLocus clone = (ProteinProteinInteractionLocus) super.cloneShallow();
		clone.trInteract = trInteract;
		return clone;
	}

	/**
	 * Calculate the effect of this variant
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
		if (!intersects(variant)) return false;// Sanity check
		variantEffects.add(variant, this, EffectType.PROTEIN_PROTEIN_INTERACTION_LOCUS, EffectImpact.HIGH, "");
		return true;
	}

}
