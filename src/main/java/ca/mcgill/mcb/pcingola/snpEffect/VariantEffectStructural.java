package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Variant;

/**
 * Effect of a structural variant affecting multiple genes
 *
 * @author pcingola
 */
public class VariantEffectStructural extends VariantEffect {

	Gene geneLeft = null;
	Gene geneRight = null;
	Set<Gene> genes = null;
	int countWholeGenes = 0; // How many genes does the variant fully include?
	int countPartialGenes = 0; // How many genes does the variant partially overlap?

	public VariantEffectStructural(Variant variant) {
		this(variant, null);
	}

	public VariantEffectStructural(Variant variant, Markers intersects) {
		super(variant);
		if (intersects != null) {
			setGenes(intersects);
			setEffect(effect());
		}
	}

	EffectType effect() {
		switch (variant.getVariantType()) {
		case INV:
			return countWholeGenes > 0 ? EffectType.GENE_INVERSION : EffectType.NONE;

		case DEL:
			return countWholeGenes > 0 ? EffectType.GENE_DELETED : EffectType.NONE;

		case DUP:
			return countWholeGenes > 0 ? EffectType.GENE_DUPLICATION : EffectType.NONE;

		case BND:
			return (countWholeGenes + countPartialGenes) > 1 ? EffectType.GENE_REARRANGEMENT : EffectType.NONE;

		default:
			throw new RuntimeException("Unknown effect for variant type " + variant.getVariantType());
		}

	}

	/**
	 * Is there another 'fusion' effect?
	 */
	public VariantEffect fusion() {
		// Only if both genes are different
		if (variant.isDup() //
				|| geneLeft == null //
				|| geneRight == null //
				|| geneLeft.getId().equals(geneRight.getId()) //
		) return null;

		return new VariantEffectFusion(this);
	}

	@Override
	public Gene getGene() {
		return geneLeft != null ? geneLeft : geneRight;
	}

	@Override
	public List<Gene> getGenes() {
		ArrayList<Gene> list = new ArrayList<>();
		list.addAll(genes);
		return list;
	}

	@Override
	public Marker getMarker() {
		return geneLeft != null ? geneLeft : geneRight;
	}

	@Override
	public boolean isMultipleGenes() {
		return true;
	}

	/**
	 * Set genes from all intersecting intervals
	 */
	void setGenes(Markers intersects) {
		genes = new HashSet<Gene>();

		for (Marker m : intersects)
			if (m instanceof Gene) {
				if (m.intersects(variant.getStart())) geneLeft = (Gene) m;
				if (m.intersects(variant.getEnd())) geneRight = (Gene) m;

				if (variant.includes(m)) countWholeGenes++;
				else countPartialGenes++;

				genes.add((Gene) m);
			}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toStr());
		sb.append("\n\tGene left  : " + geneLeft.getId() + " [" + geneLeft.getGeneName() + "]");
		sb.append("\n\tGene right : " + geneRight.getId() + " [" + geneRight.getGeneName() + "]");

		sb.append("\n\tGenes other: [");
		for (Gene g : genes)
			sb.append(g.getGeneName() + " ");
		sb.append(" ]");

		return sb.toString();
	}
}
