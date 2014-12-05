package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Splice sites variants
 *
 * @author pcingola
 */
public class TestCasesSplice extends TestCasesBase {

	public static int N = 1000;

	public TestCasesSplice() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20141205;
		minExons = 2;
	}

	void checkEffect(Variant variant, String effectExpected, String effectNotExpected) {
		// Calculate effects
		VariantEffects effects = snpEffectPredictor.variantEffect(variant);

		boolean found = false;
		for (VariantEffect effect : effects) {
			String effStr = effect.getEffectTypeString(false);

			// Check effect
			if (verbose) System.out.println(effect.toStringSimple(true) + "\n\tEffect type: '" + effStr + "'\tExpected: '" + effectExpected + "'");
			found |= effectExpected.equals(effStr);

			// Check that 'effectNotExpected' is not present
			if (effectNotExpected != null && effectNotExpected.equals(effStr)) throw new RuntimeException("Effect '" + effectNotExpected + "' should not be here");
		}

		Assert.assertTrue("Effect not found: '" + effectExpected + "'", found);
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon's splice sites
		//	- Calculate effect and check
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();
			if (verbose) System.out.println("Splice Test iteration: " + i + "\n" + transcript);
			else Gpr.showMark(i + 1, 1);

			for (Intron intron : transcript.introns()) {
				int ssBases = Math.min(SpliceSite.CORE_SPLICE_SITE_SIZE - 1, intron.size());

				// Splice site donor
				String effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? "SPLICE_SITE_ACCEPTOR" : null);
				for (int pos = intron.getStart(); pos <= intron.getStart() + ssBases; pos++) {
					Variant variant = new Variant(chromosome, pos, "A", "T");
					checkEffect(variant, "SPLICE_SITE_DONOR", effectNotExpected);
				}

				// Splice site acceptor
				effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? "SPLICE_SITE_DONOR" : null);
				for (int pos = intron.getEnd() - ssBases; pos <= intron.getEnd(); pos++) {
					Variant variant = new Variant(chromosome, pos, "A", "T");
					checkEffect(variant, "SPLICE_SITE_ACCEPTOR", effectNotExpected);
				}
			}
		}

		System.err.println("");
	}
}
