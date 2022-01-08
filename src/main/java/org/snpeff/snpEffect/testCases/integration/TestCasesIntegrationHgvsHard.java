package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test case HGSV: Hard cases
 */
public class TestCasesIntegrationHgvsHard extends TestCasesIntegrationBase {

    public TestCasesIntegrationHgvsHard() {
        super();
        shiftHgvs = true;
    }

    /**
     * Test some 'dup' form chr17
     */
    @Test
    public void test_hgvs_dup() {
        Log.debug("Test");

        String genome = "testHg19Chr17";
        String vcf = path("hgvs_dup.vcf");

        compareHgvs(genome, vcf);
    }

    @Test
    public void test_hgvs_md_chr17() {
        Log.debug("Test");
        String genome = "testHg19Chr17";
        String vcf = path("hgvs_md.chr17.vcf");
        compareHgvs(genome, vcf, false);
    }

    @Test
    public void test_hgvs_walk_and_roll_1() {
        Log.debug("Test");

        String genome = "testHg19Chr1";
        String vcf = path("hgvs_jeremy_1.vcf");

        compareHgvs(genome, vcf);
    }

    @Test
    public void test_hgvs_walk_and_roll_2() {
        Log.debug("Test");

        String genome = "testHg19Chr17";
        String vcf = path("hgvs_walk_and_roll.1.vcf");

        compareHgvs(genome, vcf, true);
    }

    @Test
    public void test_hgvs_walk_and_roll_3() {
        Log.debug("Test");
        String genome = "testHg19Chr13";
        String vcf = path("hgvs_savant.vcf");
        compareHgvs(genome, vcf, true);
    }

    /**
     * HGVS (protein) was reporting a wrong deletion length
     */
    @Test
    public void test_HgvsP_deleteion_length() {
        Log.debug("Test");
        String genome = "testHg19Chr1";
        String vcf = path("hgvs_protein_deleteion_length.vcf");
        compareHgvs(genome, vcf);
    }

    /**
     * Some difficult HGVS test case
     */
    @Test
    public void test_HgvsP_deleteion_length_2() {
        Log.debug("Test");
        String genome = "testHg19Chr2";
        String vcf = path("hgvs_protein_deleteion_length_2.vcf");
        compareHgvs(genome, vcf);
    }
}
