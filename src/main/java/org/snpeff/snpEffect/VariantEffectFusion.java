package org.snpeff.snpEffect;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Variant;

/**
 * Effect of a structural variant (fusion) affecting two genes
 *
 * @author pcingola
 */
public class VariantEffectFusion extends VariantEffectStructural {

	public VariantEffectFusion(Variant variant, Gene geneLeft, Gene geneRight) {
		super(variant);
		setEffect(EffectType.GENE_FUSION);
		genesLeft.add(geneLeft);
		genesRight.add(geneRight);
		genes.add(geneLeft);
		genes.add(geneRight);
	}

}
