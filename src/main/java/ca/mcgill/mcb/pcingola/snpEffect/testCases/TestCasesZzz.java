package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	Config config;
	Genome genome;

	/**
	 * Make sure all variant effects have appropriate impacts
	 */
	public void test_36_EffectImpact() {
		Chromosome chr = new Chromosome(null, 0, 1, "1");
		Variant var = new Variant(chr, 1, "A", "C");
		var.setChangeType(VariantType.SNP);

		System.out.println(var);
		for (EffectType eff : EffectType.values()) {
			VariantEffect varEff = new VariantEffect(var);
			varEff.setEffectType(eff);
			System.out.println(var.isVariant() + "\t" + eff + "\t" + varEff.getEffectImpact());
		}
	}
}
