package org.snpeff.snpEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantBnd;

/**
 * Effect of a structural variant affecting multiple genes
 *
 * @author pcingola
 */
public class VariantEffectStructural extends VariantEffect {

	List<Marker> featuresLeft; // Features intersecting at the left-most side of the interval (start)
	List<Marker> featuresRight; // Features intersecting at the right-most side of the interval (end)
	Set<Gene> genes; // All genes within this interval
	int countWholeGenes = 0; // How many genes does the variant fully include?
	int countPartialGenes = 0; // How many genes does the variant partially overlap?

	public VariantEffectStructural(Variant variant) {
		this(variant, null);
	}

	public VariantEffectStructural(Variant variant, Markers intersects) {
		super(variant);
		featuresLeft = new LinkedList<>();
		featuresRight = new LinkedList<>();
		genes = new HashSet<>();

		if (intersects != null) {
			setGenes(intersects);
			setEffect(effect());
		}
	}

	protected int countGenes(List<Marker> features) {
		int count = 0;
		for (Marker m : features)
			if (m instanceof Gene) count++;
		return count;
	}

	protected EffectType effect() {
		switch (variant.getVariantType()) {
		case INV:
			return countWholeGenes > 0 ? EffectType.GENE_INVERSION : EffectType.NONE;

		case DEL:
			// Note: For one gene, we annotate all transcripts
			return countWholeGenes > 1 ? EffectType.GENE_DELETED : EffectType.NONE;

		case DUP:
			return countWholeGenes > 0 ? EffectType.GENE_DUPLICATION : EffectType.NONE;

		case BND:
			int countGenesLeft = countGenes(featuresLeft);
			int countGenesRight = countGenes(featuresRight);

			// Both sides are genes
			if (countGenesLeft > 0 && countGenesRight > 0) return EffectType.GENE_FUSION;

			// Genes on only one side of the translocation (the other side is intergenic)
			if (countGenesLeft > 0 || countGenesRight > 0) return EffectType.GENE_FUSION_HALF;

			// We use 'FEATURE_FUSION'  when neither end of the translocation
			// lands on a gene (i.e. fusion of intergenic regions)
			return EffectType.FEATURE_FUSION;

		default:
			throw new RuntimeException("Unknown effect for variant type " + variant.getVariantType());
		}
	}

	/**
	 * Is there another 'fusion' effect?
	 */
	public List<VariantEffect> fusions() {
		// Only if both genes are different
		if (featuresLeft.isEmpty() || featuresRight.isEmpty()) return null;

		// Add all gene pairs
		List<VariantEffect> fusions = new LinkedList<>();
		for (Marker gLeft : featuresLeft)
			for (Marker gRight : featuresRight) {
				if (variant.isBnd()) {
					// Is this a translocation? OK, add a fusion
				} else if (!isGene(gLeft) || !isGene(gRight)) {
					// For non-translocations, both sides must be genes in order to create a fusion
					continue;
				} else {
					if (gLeft.getId().equals(gRight.getId())) {
						// Otherwise, make sure the variant is not acting within
						// the same gene (e.g. a one base deletion)
						continue;
					} else {
						// If both genes overlap and the variant is within that
						// region, then it's not a fusion, it's just a variant
						// acting on both genes.
						Marker gIntersect = gLeft.intersect(gRight);
						if (gIntersect != null && gIntersect.includes(variant)) continue;
					}
				}

				// Add all possible transcript fussions
				fusions.addAll(fusions(variant, gLeft, gRight));
			}

		// If there is a gene-gene fusion and other gene-intergenic fusions, only report
		// the gene-gene fusions.
		// Otherwise it's extremelly confusing for everyone reading the annotations
		if (fusions.size() > 0) {
			int countGeneGeneFusion = 0;
			for (VariantEffect fusion : fusions)
				if (((VariantEffectFusion) fusion).isGeneGeneFusion()) countGeneGeneFusion++;

			if (countGeneGeneFusion > 0 && fusions.size() > countGeneGeneFusion) {
				// Create a new list only keeping gene-gene fusions
				List<VariantEffect> fusionsFiltered = new LinkedList<>();
				for (VariantEffect fusion : fusions)
					if (((VariantEffectFusion) fusion).isGeneGeneFusion()) fusionsFiltered.add(fusion);
				fusions = fusionsFiltered;
			}
		}

		return fusions;
	}

	/**
	 * Create all possible transcript pair fusions for these two genes
	 */
	List<VariantEffect> fusions(Variant variant, Marker mLeft, Marker mRight) {
		List<VariantEffect> fusions = new LinkedList<>();
		// One for fusion effect for each transcript
		// This can be a long list...
		Markers msLeft = new Markers();
		Markers msRight = new Markers();

		// Left: If it's a gene, add all transcripts
		Gene gLeft = null;
		if (isGene(mLeft)) {
			gLeft = (Gene) mLeft;
			msLeft.addAll(gLeft.subIntervals());
		} else msLeft.add(mLeft);

		// Right: If it's a gene, add all transcripts
		Gene gRight = null;
		if (isGene(mRight)) {
			gRight = (Gene) mRight;
			msRight.addAll(gRight.subIntervals());
		} else msRight.add(mRight);

		// Add all transcript pairs
		for (Marker ml : msLeft)
			for (Marker mr : msRight) {
				// Transcript from the same gene are added only once
				if (isTranscript(ml) //
						&& isTranscript(mr) //
						&& gLeft.getId().equals(gRight.getId()) // Genes have the same ID
						&& ml.getId().compareTo(mr.getId()) > 0 // Compare transcript IDs alphabetically
				) continue;

				VariantEffectFusion fusion = new VariantEffectFusion(variant, ml, mr);
				fusions.add(fusion);
			}

		return fusions;
	}

	@Override
	public Gene getGene() {
		for (Marker m : featuresLeft)
			if (isGene(m)) return (Gene) m;

		for (Marker m : featuresRight)
			if (isGene(m)) return (Gene) m;

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
		return m.getChromosomeName().equals(variant.getChromosomeName()) //
				&& m.intersects(variant.getStart());
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
		if (variant.isBnd()) {
			Marker endPoint = ((VariantBnd) variant).getEndPoint();
			return m.getChromosomeName().equals(endPoint.getChromosomeName()) //
					&& m.intersects(endPoint.getStart());
		}

		return m.getChromosomeName().equals(variant.getChromosomeName()) //
				&& m.intersects(variant.getEndClosed());
	}

	protected boolean isGene(Marker m) {
		return m instanceof Gene;
	}

	@Override
	public boolean isMultipleGenes() {
		return true;
	}

	protected boolean isTranscript(Marker m) {
		return m instanceof Transcript;
	}

	/**
	 * Set genes from all intersecting intervals
	 */
	void setGenes(Markers intersects) {
		for (Marker m : intersects)
			if (m instanceof Gene) {
				if (intersectsLeft(m)) featuresLeft.add(m);
				if (intersectsRight(m)) featuresRight.add(m);

				if (variant.includes(m)) countWholeGenes++;
				else countPartialGenes++;

				genes.add((Gene) m);
			} else if (!(m instanceof Chromosome)) {
				if (intersectsLeft(m)) featuresLeft.add(m);
				if (intersectsRight(m)) featuresRight.add(m);
			}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toStr());

		sb.append("\n\tGene left  : [");
		for (Marker m : featuresLeft)
			sb.append(" " + m.getId());
		sb.append("]");

		sb.append("\n\tGene right  : [");
		for (Marker m : featuresRight)
			sb.append(" " + m.getId());
		sb.append("]");

		sb.append("\n\tGenes all: [");
		for (Gene g : genes)
			sb.append(g.getGeneName() + " ");
		sb.append(" ]");

		return sb.toString();
	}
}
