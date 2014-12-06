package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Splice sites variants
 *
 * @author pcingola
 */
public class TestCasesSpliceSite extends TestCasesBase {

	public static int N = 1000;

	public TestCasesSpliceSite() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20141205;
		minExons = 2;
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
				EffectType effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? EffectType.SPLICE_SITE_ACCEPTOR : null);
				for (int pos = intron.getStart(); pos <= intron.getStart() + ssBases; pos++) {
					Variant variant = new Variant(chromosome, pos, "A", "T");
					checkEffect(variant, EffectType.SPLICE_SITE_DONOR, effectNotExpected);
				}

				// Splice site acceptor
				effectNotExpected = (intron.size() > 2 * SpliceSite.CORE_SPLICE_SITE_SIZE ? EffectType.SPLICE_SITE_DONOR : null);
				for (int pos = intron.getEnd() - ssBases; pos <= intron.getEnd(); pos++) {
					Variant variant = new Variant(chromosome, pos, "A", "T");
					checkEffect(variant, EffectType.SPLICE_SITE_ACCEPTOR, effectNotExpected);
				}
			}
		}

		System.err.println("");
	}
}
