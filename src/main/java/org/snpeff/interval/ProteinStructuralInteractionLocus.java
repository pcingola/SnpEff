package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

/**
 * Protein interaction: An amino acid that is "in contact" with another amino acid.
 * This can be either within the same protein or interacting with another protein.
 * Evidence form PDB crystallized structures
 *
 * @author pablocingolani
 */
public class ProteinStructuralInteractionLocus extends ProteinInteractionLocus {

	private static final long serialVersionUID = 1416019843839053485L;

	public ProteinStructuralInteractionLocus() {
		super();
		type = EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS;
	}

	public ProteinStructuralInteractionLocus(Transcript parent, int aaPos, String id) {
		super(parent, aaPos, id);
		type = EffectType.PROTEIN_STRUCTURAL_INTERACTION_LOCUS;
	}

}
