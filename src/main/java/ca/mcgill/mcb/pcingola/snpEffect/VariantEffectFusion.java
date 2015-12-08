package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Gene;

/**
 * Effect of a structural variant affecting multiple genes
 *
 * @author pcingola
 */
public class VariantEffectFusion extends VariantEffectStructural {

	public VariantEffectFusion(VariantEffectStructural variantEffectStructural) {
		super(variantEffectStructural.getVariant());
		setEffect(EffectType.GENE_FUSION);
		geneLeft = variantEffectStructural.geneLeft;
		geneRight = variantEffectStructural.geneRight;
		genes = variantEffectStructural.genes;
	}

	/**
	 * Is there another 'fusion' effect?
	 */
	@Override
	public VariantEffect fusion() {
		return this;
	}

	@Override
	public List<Gene> getGenes() {
		ArrayList<Gene> list = new ArrayList<>();
		list.add(geneLeft);
		list.add(geneRight);
		return list;
	}

}
