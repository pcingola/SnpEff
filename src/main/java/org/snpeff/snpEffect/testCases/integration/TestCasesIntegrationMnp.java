package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
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
public class TestCasesIntegrationMnp extends TestCasesIntegrationBase {

    @Test
    public void test_01() {
        Log.debug("Test");
        // Run
        String[] args = {"-classic", "-noHgvs", "-ud", "0", "testHg3766Chr1", path("test.mnp.01.vcf")};
        SnpEff cmd = new SnpEff(args);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
        snpeff.setVerbose(verbose);
        snpeff.setSupressOutput(!verbose);
        snpeff.setDebug(debug);
        List<VcfEntry> results = snpeff.run(true);

        // Check
        assertEquals(1, results.size());
        VcfEntry result = results.get(0);

        for (VcfEffect eff : result.getVcfEffects()) {
            String aa = eff.getAa();
            String aaNumStr = aa.substring(1, aa.length() - 1);
            int aanum = Gpr.parseIntSafe(aaNumStr);
            if (verbose) Log.info("AA: '" + eff.getAa() + "'\tAA Num Str: '" + aaNumStr + "'\teff: " + eff);

            if (aanum <= 0) throw new RuntimeException("Missing AA number!");
        }
    }

}
