package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test random SNP changes
 *
 * @author pcingola
 */
public class TestCasesIntegrationInsEtc extends TestCasesIntegrationBase {

    /**
     * Insertion on minus strand
     */
    @Test
    public void test_01_InsOffByOne() {
        Log.debug("Test");
        String[] args = {"-classic", "-noHgvs", "testENST00000268124", path("ins_off_by_one.vcf")};

        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
        snpeff.setSupressOutput(!verbose);
        snpeff.setVerbose(verbose);

        List<VcfEntry> vcfEnties = snpeff.run(true);
        for (VcfEntry ve : vcfEnties) {

            // Get first effect (there should be only one)
            List<VcfEffect> veffs = ve.getVcfEffects();
            VcfEffect veff = veffs.get(0);

            assertEquals("Q53QQ", veff.getAa());
        }
    }

}
