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
