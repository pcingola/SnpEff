package org.snpeff.snpEffect.testCases.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test case for EMBL file parsing (database creation)
 *
 * @author pcingola
 */
public class TestCasesIntegrationEmbl extends TestCasesIntegrationBase {

	public static boolean debug = false;
	int exonToStringVersionOri;

	public TestCasesIntegrationEmbl() {
		super();
	}

	@After
	public void after() {
		Exon.ToStringVersion = exonToStringVersionOri;
	}

	@Before
	public void before() {
		exonToStringVersionOri = Exon.ToStringVersion;
		Exon.ToStringVersion = 1; // Set "toString()" version
	}

	@Test
	public void testCase_Exon_Simple() {
		Gpr.debug("Test");
		// Create SnpEff predictor
		String genome = "testEmblPberghei";
		String resultFile = "tests/testEmblPberghei.genes.embl";
		SnpEffectPredictor sep = buildEmbl(genome, resultFile);

		int pos = 4056 - 1;
		for (Gene g : sep.getGenome().getGenes()) {
			if (debug) System.out.println("Gene: '" + g.getGeneName() + "', '" + g.getId() + "'");
			for (Transcript tr : g) {
				if (debug) System.out.println("\tTranscript: '" + tr.getId() + "'");
				for (Exon e : tr) {
					if (debug) System.out.println("\t\tExon (" + e.getStrand() + "): '" + e.getId() + "'\t" + e.toStr());
					if (e.intersects(pos)) {
						String seq = e.getSequence();
						String base = e.basesAtPos(pos, 1);
						if (debug) System.out.println("Seq : " + seq + "\nBase: " + base);
						Assert.assertEquals("g", base);
					}
				}
			}
		}
	}
}
