package org.snpeff.snpEffect;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;

/**
 * Effect of a structural variant (fusion) affecting two genes
 *
 * @author pcingola
 */
public class VariantEffectFusion extends VariantEffectStructural {

	Transcript trLeft, trRight;
	Gene geneLeft, geneRight;

	public VariantEffectFusion(Variant variant, Transcript trLeft, Transcript trRight) {
		super(variant);
		setEffect(EffectType.GENE_FUSION);

		marker = this.trLeft = trLeft;
		this.trRight = trRight;
		geneLeft = (Gene) trLeft.getParent();
		geneRight = (Gene) trRight.getParent();

		genesLeft.add(geneLeft);
		genesRight.add(geneRight);
		genes.add(geneLeft);
		genes.add(geneRight);
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

}
