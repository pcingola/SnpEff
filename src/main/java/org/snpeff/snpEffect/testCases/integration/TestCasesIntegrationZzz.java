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
        verbose = true;
        String[] args = {"buildNextProt", "testHg3770Chr22", path("nextProt")};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setVerbose(verbose);
        snpEff.setSupressOutput(!verbose);
        boolean ok = snpEff.run();
        Assert.assertEquals(true, ok);
    }

    @Test
    public void test_10() {
        // TODO: Test annotation not highly conserved (synonymous change) => EffectImpact.MODIFIER;
        Log.debug("Test");
        verbose = true;
        // Gene: ODF3B, Transcript: ENST00000329363, modified-residue_Phosphoserine (annotation is always in Amino Acis 'S')
        checkNextProt("testHg3770Chr22" //
                , path("test_nextProt_07.vcf") //
                , "modified-residue_Phosphoserine" //
                , VariantEffect.EffectImpact.HIGH //
                , true //
                , "ENST00000329363" //
        );
    }

}
