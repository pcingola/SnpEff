package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Intergenic;
import org.snpeff.util.Log;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(3, intergenics.size(), "Expected 3 intergenic regions");
        assertEquals("CHR_START-geneName1", intergenics.get(0).getName(), "Intergenic region name doesn't match");
        assertEquals("geneName1-geneName2", intergenics.get(1).getName(), "Intergenic region name doesn't match");
        assertEquals("geneName2-CHR_END", intergenics.get(2).getName(), "Intergenic region name doesn't match");

        assertEquals("CHR_START-geneId1", intergenics.get(0).getId(), "Intergenic region ID doesn't match");
        assertEquals("geneId1-geneId2", intergenics.get(1).getId(), "Intergenic region ID doesn't match");
        assertEquals("geneId2-CHR_END", intergenics.get(2).getId(), "Intergenic region ID doesn't match");
    }

}
