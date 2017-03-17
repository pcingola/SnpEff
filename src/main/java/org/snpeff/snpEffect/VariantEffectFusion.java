package org.snpeff.snpEffect;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantBnd;
import org.snpeff.interval.codonChange.CodonChange;

/**
 * Effect of a structural variant (fusion) affecting two genes
 *
 * @author pcingola
 */
public class VariantEffectFusion extends VariantEffectStructural {

	int aaNumLeftStart;
	int aaNumLeftEnd;
	int aaNumRightStart;
	int aaNumRightEnd;
	Marker mLeft, mRight;
	Transcript trLeft, trRight;
	Gene geneLeft, geneRight;

	public VariantEffectFusion(Variant variant, Marker mLeft, Marker mRight) {
		super(variant);

		this.mLeft = mLeft;
		if (isTranscript(mLeft)) {
			trLeft = (Transcript) mLeft;
			geneLeft = (Gene) mLeft.getParent();
			genes.add(geneLeft);
		}

		this.mRight = mRight;
		if (isTranscript(mRight)) {
			trRight = (Transcript) mRight;
			geneRight = (Gene) mRight.getParent();
			genes.add(geneRight);
		}

		featuresLeft.add(mLeft);
		featuresRight.add(mRight);

		calcEffect();
	}

	/**
	 * Transcript left has positions at the beginning
	 */
	void aaLeftBegin() {
		aaNumLeftStart = 0;
		aaNumLeftEnd = baseNumberCdsLeft() / CodonChange.CODON_SIZE;
	}

	/**
	 * Transcript left has positions at the end
	 */
	void aaLeftEnd() {
		aaNumLeftStart = baseNumberCdsLeft() / CodonChange.CODON_SIZE;;
		aaNumLeftEnd = trLeft.protein().length() - 1;
	}

	/**
	 * Calculate transcript's start/end AA numbers
	 */
	void aaPos() {
		VariantBnd vtrans = getVariantTranslocation();

		// E.g.:  C[2:321682[
		if (!vtrans.isLeft() && !vtrans.isBefore()) {
			if (trLeft != null) {
				if (trLeft.isStrandPlus()) aaLeftBegin();
				else aaLeftEnd();
			}

			if (trRight != null) {
				if (trRight.isStrandMinus()) aaRightBegin();
				else aaRightEnd();
			}
		}

		// E.g.: G]17:198982]
		if (vtrans.isLeft() && !vtrans.isBefore()) {
			if (trLeft != null) {
				if (trLeft.isStrandPlus()) aaLeftBegin();
				else aaLeftEnd();
			}

			if (trRight != null) {
				if (trRight.isStrandPlus()) aaRightBegin();
				else aaRightEnd();
			}
		}

		// E.g.:  [17:198983[A
		if (!vtrans.isLeft() && vtrans.isBefore()) {
			if (trLeft != null) {
				if (trLeft.isStrandMinus()) aaLeftBegin();
				else aaLeftEnd();
			}

			if (trRight != null) {
				if (trRight.isStrandMinus()) aaRightBegin();
				else aaRightEnd();
			}
		}

		// E.g.:  ]13:123456]T
		if (vtrans.isLeft() && vtrans.isBefore()) {
			if (trLeft != null) {
				if (trLeft.isStrandMinus()) aaLeftBegin();
				else aaLeftEnd();
			}

			if (trRight != null) {
				if (trRight.isStrandPlus()) aaRightBegin();
				else aaRightEnd();
			}
		}
	}

	/**
	 * Transcript right has positions at the beginning
	 */
	void aaRightBegin() {
		aaNumRightStart = 0;
		aaNumRightEnd = baseNumberCdsRight() / CodonChange.CODON_SIZE;
	}

	/**
	 * Transcript right has positions at the end
	 */
	void aaRightEnd() {
		aaNumRightStart = baseNumberCdsRight() / CodonChange.CODON_SIZE;
		aaNumRightEnd = trRight.protein().length() - 1;;
	}

	/**
	 * CDS base number for gene on the left side
	 */
	int baseNumberCdsLeft() {
		return trLeft.baseNumberCds(variant.getStart(), usePrevBaseIntronLeft());
	}

	/**
	 * CDS base number for gene on the right side
	 */
	int baseNumberCdsRight() {
		return trRight.baseNumberCds(getVariantTranslocation().getEndPoint().getStart(), !usePrevBaseIntronLeft());
	}

	/**
	 * Calculate effect
	 * Note: For notes and example figures, see VCF 4.2 specification, Figure 1
	 */
	void calcEffect() {
		boolean sameStrand = trLeft == null //
				|| trRight == null //
				|| trLeft.isStrandPlus() == trRight.isStrandPlus();

		switch (variant.getVariantType()) {
		case INV:
			setEffect(sameStrand ? EffectType.GENE_FUSION_REVERESE : EffectType.GENE_FUSION);
			marker = geneLeft;
			break;

		case DEL:
		case DUP:
			// Non-translocations: DEL, DUP
			setEffect(sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			marker = geneLeft;
			break;

		case BND:
			marker = trLeft; // Force HGVS.c notation to use '.c' instead of '.n'
			if (marker == null) marker = trRight;
			if (marker == null) marker = mLeft;
			if (marker == null) marker = mRight;

			// Translocation
			VariantBnd vtrans = getVariantTranslocation();
			EffectType effType = null;

			// Note: The following block of 'setEffect' could be written simply as
			//
			// 		    setEffect( (vtrans.isLeft() != vtrans.isBefore()) ^ sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			//
			//       But this would be rather cryptic, that's why I use an explicit case by case scenario

			if (trLeft == null && trRight == null) {
				// Both ends lie onto intergenic regions
				effType = EffectType.FEATURE_FUSION;
			} else if (trLeft == null || trRight == null) {
				// One end lies onto intergenic regions
				effType = EffectType.GENE_FUSION_HALF;
			} else if (!vtrans.isLeft() && !vtrans.isBefore()) {
				// E.g.:  C[2:321682[
				effType = (sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			} else if (vtrans.isLeft() && !vtrans.isBefore()) {
				// E.g.: G]17:198982]
				effType = (!sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			} else if (!vtrans.isLeft() && vtrans.isBefore()) {
				// E.g.:  [17:198983[A
				effType = (!sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			} else if (vtrans.isLeft() && vtrans.isBefore()) {
				// E.g.:  ]13:123456]T
				effType = (sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			} else throw new RuntimeException("This should never happen!");

			setEffect(effType);

			// Calculate AA positions
			aaPos();

			// Is this fusion introducing a frame shift?
			if (effType == EffectType.GENE_FUSION) frameShift();

			break;

		default:
			throw new RuntimeException("Unimplemented method for variant type '" + variant.getVariantType() + "'");
		}
	}

	/**
	 * Is this fusion introducing a frame shift?
	 */
	void frameShift() {
		if (trLeft == null || trRight == null) return;

		int cdsPosLeft = baseNumberCdsLeft();
		int frameLeft = cdsPosLeft % CodonChange.CODON_SIZE;
		int cdsPosRight = baseNumberCdsRight();
		int frameRight = cdsPosRight % CodonChange.CODON_SIZE;

		// Is the next base in frame?
		// It is in frame if:
		//    (frame_before_tranlocation + 1) % 3 == frame_after_translocation
		// Otherwise, we have a frame shift
		boolean ok = false;
		boolean before = getVariantTranslocation().isBefore();

		// Do frames match for each case?
		if (!before && trLeft.isStrandPlus()) ok = ((frameLeft + 1) % CodonChange.CODON_SIZE == frameRight);
		else if (!before && trLeft.isStrandMinus()) ok = ((frameRight + 1) % CodonChange.CODON_SIZE == frameLeft);
		else if (before && trLeft.isStrandMinus()) ok = ((frameLeft + 1) % CodonChange.CODON_SIZE == frameRight);
		else if (before && trLeft.isStrandPlus()) ok = ((frameRight + 1) % CodonChange.CODON_SIZE == frameLeft);

		// Frames do not match => Add frame shift effect
		if (!ok) addEffect(EffectType.FRAME_SHIFT);
	}

	public int getAaNumLeftEnd() {
		return aaNumLeftEnd;
	}

	public int getAaNumLeftStart() {
		return aaNumLeftStart;
	}

	public int getAaNumRightEnd() {
		return aaNumRightEnd;
	}

	public int getAaNumRightStart() {
		return aaNumRightStart;
	}

	@Override
	public Gene getGene() {
		if (geneLeft != null) return geneLeft;
		if (geneRight != null) return geneRight;
		return null;
	}

	public Gene getGeneLeft() {
		return geneLeft;
	}

	public Gene getGeneRight() {
		return geneRight;
	}

	@Override
	public Marker getMarker() {
		return marker;
	}

	public Transcript getTrLeft() {
		return trLeft;
	}

	public Transcript getTrRight() {
		return trRight;
	}

	VariantBnd getVariantTranslocation() {
		return (VariantBnd) variant;
	}

	public boolean isGeneGeneFusion() {
		return geneLeft != null && geneRight != null;
	}

	boolean isVariantTranslocation() {
		return variant instanceof VariantBnd;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toStr());

		sb.append("\n\tFeatures left  : [");
		for (Marker m : featuresLeft)
			sb.append(" " + m.getId());
		sb.append("]");

		sb.append("\n\tFeatures right  : [");
		for (Marker m : featuresRight)
			sb.append(" " + m.getId());
		sb.append("]");

		sb.append("\n\tGenes: [");
		for (Gene g : genes)
			sb.append(g.getGeneName() + " ");
		sb.append(" ]");

		return sb.toString();
	}

	/**
	 * If the translocation lies within an intron, do we want the
	 * first exonic base BEFORE the intron? (Left gene)
	 */
	boolean usePrevBaseIntronLeft() {
		return trLeft != null //
				&& ((trLeft.isStrandPlus() && !getVariantTranslocation().isBefore()) //
						|| (trLeft.isStrandMinus() && getVariantTranslocation().isBefore())) //
		;
	}

}
