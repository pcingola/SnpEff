package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.interval.Intergenic;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test intergenic markers
 *
 * @author pcingola
 */
public class TestCasesIntergenic extends TestCasesBase {

	public TestCasesIntergenic() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20170510;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		numGenes = 2;
		maxGeneLen = 100;

		initSnpEffPredictor();
		List<Intergenic> intergenics = snpEffectPredictor.getGenome().getGenes().createIntergenic();

		Assert.assertEquals("Expected 3 intergenic regions", 3, intergenics.size());
		Assert.assertEquals("Intergenic region name doesn't match", "CHR_START-geneName1", intergenics.get(0).getName());
		Assert.assertEquals("Intergenic region name doesn't match", "geneName1-geneName2", intergenics.get(1).getName());
		Assert.assertEquals("Intergenic region name doesn't match", "geneName2-CHR_END", intergenics.get(2).getName());

		Assert.assertEquals("Intergenic region ID doesn't match", "CHR_START-geneId1", intergenics.get(0).getId());
		Assert.assertEquals("Intergenic region ID doesn't match", "geneId1-geneId2", intergenics.get(1).getId());
		Assert.assertEquals("Intergenic region ID doesn't match", "geneId2-CHR_END", intergenics.get(2).getId());
	}

}
