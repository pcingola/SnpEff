package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Intergenic;
import org.snpeff.util.Log;

import java.util.List;

import static org.junit.jupiter.api.AssertEquals.assertEquals;

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
        Log.debug("Test");
        numGenes = 2;
        maxGeneLen = 100;

        initSnpEffPredictor();
        List<Intergenic> intergenics = snpEffectPredictor.getGenome().getGenes().createIntergenic();

        assertEquals("Expected 3 intergenic regions", 3, intergenics.size());
        assertEquals("Intergenic region name doesn't match", "CHR_START-geneName1", intergenics.get(0).getName());
        assertEquals("Intergenic region name doesn't match", "geneName1-geneName2", intergenics.get(1).getName());
        assertEquals("Intergenic region name doesn't match", "geneName2-CHR_END", intergenics.get(2).getName());

        assertEquals("Intergenic region ID doesn't match", "CHR_START-geneId1", intergenics.get(0).getId());
        assertEquals("Intergenic region ID doesn't match", "geneId1-geneId2", intergenics.get(1).getId());
        assertEquals("Intergenic region ID doesn't match", "geneId2-CHR_END", intergenics.get(2).getId());
    }

}
