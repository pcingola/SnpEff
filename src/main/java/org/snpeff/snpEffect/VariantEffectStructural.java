package org.snpeff.snpEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantTranslocation;

/**
 * Effect of a structural variant affecting multiple genes
 *
 * @author pcingola
 */
public class VariantEffectStructural extends VariantEffect {

	List<Gene> genesLeft;
	List<Gene> genesRight;
	Set<Gene> genes;
	int countWholeGenes = 0; // How many genes does the variant fully include?
	int countPartialGenes = 0; // How many genes does the variant partially overlap?

	public VariantEffectStructural(Variant variant) {
		this(variant, null);
	}

	public VariantEffectStructural(Variant variant, Markers intersects) {
		super(variant);
		genesLeft = new LinkedList<>();
		genesRight = new LinkedList<>();
		genes = new HashSet<Gene>();

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
			// Note: For one gene, we annotate all transcripts
			return countWholeGenes > 1 ? EffectType.GENE_DELETED : EffectType.NONE;

		case DUP:
			return countWholeGenes > 0 ? EffectType.GENE_DUPLICATION : EffectType.NONE;

		case BND:
			// Translocation always intersect "partial genes" since they have two (unconnected) break-points
			return countPartialGenes > 1 ? EffectType.GENE_FUSION : EffectType.NONE;

		default:
			throw new RuntimeException("Unknown effect for variant type " + variant.getVariantType());
		}

	}

	/**
	 * Is there another 'fusion' effect?
	 */
	public List<VariantEffect> fusion() {
		// Only if both genes are different
		if (genesLeft.isEmpty() || genesRight.isEmpty()) return null;

		// Add all gene pairs
		List<VariantEffect> fusions = new LinkedList<VariantEffect>();
		for (Gene gLeft : genesLeft)
			for (Gene gRight : genesRight)
				if (!gLeft.getId().equals(gRight.getId())) { // Not the same gene?
					// If both genes overlap and the variant is within that
					// region, then it's not a fusion, it's just a variant
					// acting on both genes.
					Marker gIntersect = gLeft.intersect(gRight);
					if (gIntersect != null && gIntersect.includes(variant)) continue;

					VariantEffectFusion fusion = new VariantEffectFusion(variant, gLeft, gRight);
					fusions.add(fusion);
				}

		return fusions;
	}

	@Override
	public Gene getGene() {
		if (!genesLeft.isEmpty()) return genesLeft.get(0);
		if (!genesRight.isEmpty()) return genesRight.get(0);
		return null;
	}

	@Override
	public List<Gene> getGenes() {
		ArrayList<Gene> list = new ArrayList<>();
		list.addAll(genes);
		return list;
	}

	@Override
	public Marker getMarker() {
		return getGene();
	}

	/**
	 * We say that intersects the "Left" side if the 'start' of the variant
	 * intersects the marker.
	 *
	 * Note: For translocations this is the 'main' variants (as opposed to
	 * the endPoint). This is just nomenclature and we could have defined
	 * it the other way around (or called it intersect1 and intersect2
	 * instead of intersectLeft intersercRight)
	 */
	boolean intersectsLeft(Marker m) {
		return m.intersects(variant.getStart());
	}

	/**
	 * We say that intersects the "Right" side if the 'end' of the variant
	 * intersects the marker.
	 *
	 * Note: For translocations this is the 'endPoint' (as opposed to
	 * the 'main' variant). This is just nomenclature and we could have defined
	 * it the other way around (or called it intersect1 and intersect2
	 * instead of intersectLeft intersercRight)
	 */
	boolean intersectsRight(Marker m) {
		if (variant.isBnd()) return m.intersects(((VariantTranslocation) variant).getEndPoint().getStart());
		return m.intersects(variant.getEnd());
	}

	@Override
	public boolean isMultipleGenes() {
		return true;
	}

	/**
	 * Set genes from all intersecting intervals
	 */
	void setGenes(Markers intersects) {
		for (Marker m : intersects)
			if (m instanceof Gene) {
				if (intersectsLeft(m)) genesLeft.add((Gene) m);
				if (intersectsRight(m)) genesRight.add((Gene) m);

				if (variant.includes(m)) countWholeGenes++;
				else countPartialGenes++;

				genes.add((Gene) m);
			}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toStr());

		sb.append("\n\tGene left  : [");
		for (Gene g : genesLeft)
			sb.append(" " + g.getId());
		sb.append("]");

		sb.append("\n\tGene right  : [");
		for (Gene g : genesRight)
			sb.append(" " + g.getId());
		sb.append("]");

		sb.append("\n\tGenes all: [");
		for (Gene g : genes)
			sb.append(g.getGeneName() + " ");
		sb.append(" ]");

		return sb.toString();
	}
}
