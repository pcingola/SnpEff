package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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
	public List<VariantEffect> fusion() {
		// Only if both genes are different
		if (variant.isDup() //
				|| genesLeft.isEmpty() //
				|| genesRight.isEmpty() //
		) return null;

		// Add all gene pairs
		List<VariantEffect> fusions = new LinkedList<VariantEffect>();
		for (Gene gLeft : genesLeft)
			for (Gene gRight : genesRight)
				if (!gLeft.getId().equals(gRight.getId())) {
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
				if (m.intersects(variant.getStart())) genesLeft.add((Gene) m);
				if (m.intersects(variant.getEnd())) genesRight.add((Gene) m);

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
