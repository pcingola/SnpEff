package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.HashSet;
import java.util.Set;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Variant;

/**
 * Effect of a variant.
 *
 * @author pcingola
 */
public class VariantEffectStructural extends VariantEffect {

	Gene geneLeft = null;
	Gene geneRight = null;
	Set<Gene> geneOthers = null;

	public VariantEffectStructural(Variant variant, Markers intersects) {
		super(variant);
		setGenes(intersects);
		setEffect(effect());
	}

	EffectType effect() {
		switch (variant.getVariantType()) {
		case INV:
			return EffectType.GENE_INVERSION;

		case DEL:
			return EffectType.GENE_DELETED;

		case DUP:
			return EffectType.GENE_DUPLICATION;

		case BND:
			return EffectType.GENE_REARRANGEMENT;

		default:
			throw new RuntimeException("Unknown effect for variant type " + variant.getVariantType());
		}

	}

	@Override
	public Marker getMarker() {
		return geneLeft != null ? geneLeft : geneRight;
	}

	/**
	 * Set genes from all intersecting intervals
	 */
	void setGenes(Markers intersects) {
		geneOthers = new HashSet<Gene>();

		for (Marker m : intersects)
			if (m instanceof Gene) {
				if (m.intersects(variant.getStart())) geneLeft = (Gene) m;
				else if (m.intersects(variant.getEnd())) geneRight = (Gene) m;
				else geneOthers.add((Gene) m);
			}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toStr());
		sb.append("\n\tGene left  : " + geneLeft.getId() + " [" + geneLeft.getGeneName() + "]");
		sb.append("\n\tGene right : " + geneRight.getId() + " [" + geneRight.getGeneName() + "]");

		sb.append("\n\tGenes other: [");
		for (Gene g : geneOthers)
			sb.append(g.getGeneName() + " ");
		sb.append(" ]");

		return sb.toString();
	}
}
