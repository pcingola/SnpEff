package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.binseq.DnaSequenceByte;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationBase;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test playground
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
		super();
		testsDir = "tests/integration/covid19/";
	}

//	@Test
//	public void test_02() {
//		// Create variant
//		int start = 300;
//		Variant var = new Variant(chr, start, chrSeq.substring(300, 450), "", "");
//		if (verbose) Log.info("Transcript:" + tr + "\nVariant: " + var);
//
//		// Calculate effects
//		int countMatch = 0;
//		VariantEffects effectsAll = snpEffectPredictor.variantEffect(var);
//		for (VariantEffect eff : effectsAll) {
//			if (verbose) Log.info("\t" + eff.getEffectTypeString(false) + "\tHGVS.p: '" + eff.getHgvsProt() + "'");
//			if (eff.getEffectType() == EffectType.TRANSCRIPT_DELETED) {
//				countMatch++;
//				assertEquals("HGVS.p notation error", "p.0?", eff.getHgvsProt());
//			}
//		}
//		assertTrue(countMatch > 0, "No variant effects found");
//	}
//
//	@Test
//	public void test_01() {
//		Log.debug("Test");
//		Random random = new Random(20120907);
//		for (int len = 1; len < 1000; len++) {
//			for (int i = 0; i < 10; i++) {
//				String seq = GprSeq.randSequence(random, len);
//				DnaSequenceByte dna = new DnaSequenceByte(seq);
//
//				if (verbose) Log.info("Len: " + len + "\t" + seq + "\t" + dna);
//
//				// FIXME: Use assertEquals
//				assertEquals(seq, dna.toString());
////                if (seq.equals(dna.toString()))
////                    throw new RuntimeException("Sequences do not match! Length: " + len + "\n\t" + seq + "\n\t" + dna);
//			}
//		}
//	}

	/**
	 * Empty Quality: Not a variant
	 */
	@Test
	public void test_10_empty_QUAL() {
		Log.debug("Test");
		String file = path("empty.vcf");

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) Log.info(vcfEntry);
			System.out.println("Q: " + vcfEntry.getQuality());
			assertEquals(0.0, vcfEntry.getQuality(), 10 ^ -6);
		}
	}

}
