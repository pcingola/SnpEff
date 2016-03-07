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
		aaNumLeftEnd = trLeft.baseNumberCds(variant.getStart(), true) / CodonChange.CODON_SIZE;
	}

	/**
	 * Transcript left has positions at the end
	 */
	void aaLeftEnd() {
		aaNumLeftStart = trLeft.baseNumberCds(variant.getStart(), true) / CodonChange.CODON_SIZE;;
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
		aaNumRightEnd = trRight.baseNumberCds(getVariantTranslocation().getEndPoint().getStart(), true) / CodonChange.CODON_SIZE;
	}

	/**
	 * Transcript right has positions at the end
	 */
	void aaRightEnd() {
		aaNumRightStart = trRight.baseNumberCds(getVariantTranslocation().getEndPoint().getStart(), true) / CodonChange.CODON_SIZE;
		aaNumRightEnd = trRight.protein().length() - 1;;
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

			// Note: The following block of 'setEffect' could be written simply as
			//
			// 		    setEffect( (vtrans.isLeft() != vtrans.isBefore()) ^ sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);
			//
			//       But this would be rather cryptic, that's why I use an explicit case by case scenario

			// E.g.:  C[2:321682[
			if (!vtrans.isLeft() && !vtrans.isBefore()) setEffect(sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);

			// E.g.: G]17:198982]
			if (vtrans.isLeft() && !vtrans.isBefore()) setEffect(!sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);

			// E.g.:  [17:198983[A
			if (!vtrans.isLeft() && vtrans.isBefore()) setEffect(!sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);

			// E.g.:  ]13:123456]T
			if (vtrans.isLeft() && vtrans.isBefore()) setEffect(sameStrand ? EffectType.GENE_FUSION : EffectType.GENE_FUSION_REVERESE);

			// Calculate AA positions
			aaPos();

			break;

		default:
			throw new RuntimeException("Unimplemented method for variant type '" + variant.getVariantType() + "'");
		}
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

}
