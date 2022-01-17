package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test mixed variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationMixedVariants extends TestCasesIntegrationBase {

    /**
     * Compare with results from ENSEMBL's VEP to SnpEff
     * Use VCF having VEP's results
     */
    public void compareVep(String genome, String vcf) {
        CompareToVep comp = new CompareToVep(genome, verbose);
        comp.compareVep(vcf);
        if (verbose) Log.info(comp);
        assertTrue(comp.checkComapred(), "No comparissons were made!");
    }

    /**
     * Make sure we can read VCF and parse variants without producing any exception
     */
    @Test
    public void test_01_MixedVep() {
        Log.debug("Test");
        String vcfFile = path("mixed_01.vcf");

        VcfFileIterator vcf = new VcfFileIterator(vcfFile);
        for (VcfEntry ve : vcf) {
            if (verbose) Log.info(ve);
            for (Variant var : ve.variants()) {
                if (verbose) Log.info("\t" + var);
            }
        }
    }

    @Test
    public void test_02_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr22", path("mixed_chr22.vcf"));
    }

    @Test
    public void test_03_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr14", path("mixed_chr14.vcf"));
    }

    @Test
    public void test_04_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr12", path("mixed_chr12.vcf"));
    }

    @Test
    public void test_05_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr22", path("mixed_chr22.vcf"));
    }

    @Test
    public void test_06_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr7", path("mixed_chr7.vcf"));
    }

    @Test
    public void test_07_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr6", path("mixed_chr6.vcf"));
    }

    @Test
    public void test_08_MixedVep() {
        Log.debug("Test");
        compareVep("testHg3775Chr1", path("mixed_chr1.vcf"));
    }

    @Test
    public void test_09_MixedVep() {
        Log.debug("Test");

        String vcfFileName = path("mixed_09.vcf");
        VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
        for (VcfEntry ve : vcf) {
            if (verbose) Log.info(ve);
            for (Variant v : ve.variants()) {
                if (verbose) Log.info("\t\t" + v);
				assertSame(v.getVariantType(), VariantType.MIXED, "Variant is not MIXED");
            }
        }
    }

    @Test
    public void test_11_ExonRank() {
        Log.debug("Test");

        String vcfFileName = path("mixed_11.vcf");
        String[] args = {"testHg19Chr20", vcfFileName};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
        snpeff.setSupressOutput(!verbose);
        snpeff.setVerbose(verbose);

        List<VcfEntry> vcfEnties = snpeff.run(true);
        VcfEntry ve = vcfEnties.get(0);

        // Get first effect (there should be only one)
        List<VcfEffect> veffs = ve.getVcfEffects();
        VcfEffect veff = veffs.get(0);

        assertEquals(12, veff.getRank(), "Exon rank does not match");
    }

}
