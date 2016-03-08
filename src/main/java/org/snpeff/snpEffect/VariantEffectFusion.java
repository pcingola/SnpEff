package org.snpeff.snpEffect;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantTranslocation;
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
	Transcript trLeft, trRight;
	Gene geneLeft, geneRight;

	public VariantEffectFusion(Variant variant, Transcript trLeft, Transcript trRight) {
		super(variant);

		this.trLeft = trLeft;
		this.trRight = trRight;
		geneLeft = (Gene) trLeft.getParent();
		geneRight = (Gene) trRight.getParent();

		genesLeft.add(geneLeft);
		genesRight.add(geneRight);
		genes.add(geneLeft);
		genes.add(geneRight);

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
		VariantTranslocation vtrans = getVariantTranslocation();

		// E.g.:  C[2:321682[
		if (!vtrans.isLeft() && !vtrans.isBefore()) {
			if (trLeft.isStrandPlus()) aaLeftBegin();
			else aaLeftEnd();

			if (trRight.isStrandMinus()) aaRightBegin();
			else aaRightEnd();
		}

		// E.g.: G]17:198982]
		if (vtrans.isLeft() && !vtrans.isBefore()) {
			if (trLeft.isStrandPlus()) aaLeftBegin();
			else aaLeftEnd();

			if (trRight.isStrandPlus()) aaRightBegin();
			else aaRightEnd();
		}

		// E.g.:  [17:198983[A
		if (!vtrans.isLeft() && vtrans.isBefore()) {
			if (trLeft.isStrandMinus()) aaLeftBegin();
			else aaLeftEnd();

			if (trRight.isStrandMinus()) aaRightBegin();
			else aaRightEnd();
		}

		// E.g.:  ]13:123456]T
		if (vtrans.isLeft() && vtrans.isBefore()) {
			if (trLeft.isStrandMinus()) aaLeftBegin();
			else aaLeftEnd();

			if (trRight.isStrandPlus()) aaRightBegin();
			else aaRightEnd();
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
		boolean sameStrand = trLeft.isStrandPlus() == trRight.isStrandPlus();

		switch (variant.getVariantType()) {
		case INV:
			setEffect(sameStrand ? EffectType.GENE_FUSION_REVERESE : EffectType.GENE_FUSION);
			break;

		case DEL:
		case DUP:
			// Non-translocations: DEL, DUP
			setEffect(sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			break;

		case BND:
			marker = trLeft; // Force HGVS.c notation to use '.c' instead of '.n'

			// Translocation
			VariantTranslocation vtrans = getVariantTranslocation();
			EffectType effType = null;

			// Note: The following block of 'setEffect' could be written simply as
			//
			// 		    setEffect( (vtrans.isLeft() != vtrans.isBefore()) ^ sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			//
			//       But this would be rather cryptic, that's why I use an explicit case by case scenario

			if (!vtrans.isLeft() && !vtrans.isBefore()) {
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

	public Gene getGeneLeft() {
		return geneLeft;
	}

	public Gene getGeneRight() {
		return geneRight;
	}

	public Transcript getTrLeft() {
		return trLeft;
	}

	public Transcript getTrRight() {
		return trRight;
	}

	VariantTranslocation getVariantTranslocation() {
		return (VariantTranslocation) variant;
	}

	boolean isVariantTranslocation() {
		return variant instanceof VariantTranslocation;
	}

	/**
	 * If the translocation lies within an intron, do we want the
	 * first exonic base BEFORE the intron? (Left gene)
	 */
	boolean usePrevBaseIntronLeft() {
		return (trLeft.isStrandPlus() && !getVariantTranslocation().isBefore()) //
				|| (trLeft.isStrandMinus() && getVariantTranslocation().isBefore()) //
				;
	}

}
