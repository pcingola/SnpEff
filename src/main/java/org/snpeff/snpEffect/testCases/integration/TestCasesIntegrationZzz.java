package org.snpeff.snpEffect.testCases.integration;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Log;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

    long randSeed = 20100629;
    String genomeName = "testCase";

    public TestCasesIntegrationZzz() {
        super();
    }

    @Test
    public void test_01_build() {
        Log.debug("Test");
        String[] args = {"buildNextProt", "testHg3770Chr22", path("nextProt")};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        boolean ok = snpEff.run();
        Assert.assertEquals(true, ok);
    }

    @Test
    public void test_06_ann_disulphide_bond() {
        Log.debug("Test");
        verbose = true;
        // Note: Normally this EffectImpact should be 'MODERATE' impact, but
        // since the database we build in test_01_build is small, there are
        // not enough stats.
        // Gene: GGT1, Transcript: ENST00000400382
		//		1:	22	25016879	25016879	NextProt	null
		//		2:	22	25016485	25016486	NextProt	null
		//		3:	22	25016889	25016891	NextProt	null
		//
		//		Annotation 'disulfide-bond'
		//		Description: null
		//		null
		//		LocationTargetIsoformInteraction(NX_P19440-1, 191, 195)
		//		LocationTargetIsoformInteraction(NX_P19440-2, 191, 195)
		//
		//		TR: ENST00000400382
		//		Gene: GGT1
        checkNextProt("testHg3770Chr22" //
                , path("test_nextProt_05.vcf") //
                , "disulfide-bond" //
                , VariantEffect.EffectImpact.MODERATE //
                , true //
                , "ENST00000400382" //
        );
    }
}
