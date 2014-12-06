package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test random Interval Variants (e.g. when reading a BED file)
 *
 * @author pcingola
 */
public class TestCasesIntervalVariant extends TestCasesBase {

	public static int N = 1000;

	public TestCasesIntervalVariant() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20120426;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();
			if (debug) System.out.println("INTERVAL (Variant) Test iteration: " + i + "\n" + transcript);
			else Gpr.showMark(i + 1, 1);

			// For each base in the transcript
			// For each base in this exon...
			for (int pos = 0; pos < chromosome.size(); pos++) {
				//---
				// Create seqChange
				//---
				// Interval length
				int intLen = rand.nextInt(100) + 1;
				int start = pos;
				int end = Math.min(pos + intLen, chromosome.getEnd());

				// Create a SeqChange
				Variant variant = new Variant(chromosome, start, end, "");

				// Sanity checks
				Assert.assertEquals(true, variant.isInterval()); // Is it an interval?

				//---
				// Expected Effect
				//---
				EffectType expectedEffect = null;
				if (transcript.intersects(variant)) {
					// Does it intersect any exon?
					for (Exon ex : transcript)
						if (ex.intersects(variant)) expectedEffect = EffectType.EXON;

					for (Intron intron : transcript.introns())
						if (intron.intersects(variant)) expectedEffect = EffectType.INTRON;
				} else if (gene.intersects(variant)) {
					// Gene intersects but transcript doesn't?
					if (expectedEffect == null) expectedEffect = EffectType.INTRAGENIC;
				} else expectedEffect = EffectType.INTERGENIC;

				//---
				// Calculate Effect
				//---
				VariantEffects effects = snpEffectPredictor.variantEffect(variant);

				//---
				// Check effect
				//---
				// There should be only one effect in most cases
				Assert.assertEquals(false, effects.isEmpty()); // There should be at least one effect
				if (debug && (effects.size() > 1)) {
					System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
					for (VariantEffect eff : effects)
						System.out.println("\t" + eff);
				}

				boolean isExpectedOK = false;
				StringBuilder effSb = new StringBuilder();
				for (VariantEffect effect : effects) {
					String effstr = effect.effect(true, true, true, false);

					isExpectedOK |= effect.hasEffectType(expectedEffect);
					effSb.append(effstr + " ");

				}

				if (debug || !isExpectedOK) //
					System.out.println("\nVariant         : " + variant //
							+ "\nExpected Effect : '" + expectedEffect + "'" //
							+ "\nEffects         : '" + effSb + "'" //
							+ "\n--------------------------------------------------------------\n" //
							);
				Assert.assertEquals(true, isExpectedOK);
			}
		}
	}
}