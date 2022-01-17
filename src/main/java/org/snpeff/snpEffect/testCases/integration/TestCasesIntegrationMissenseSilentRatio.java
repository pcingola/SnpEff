package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Calculate missense over silent ratio
 *
 * @author pcingola
 */
public class TestCasesIntegrationMissenseSilentRatio extends TestCasesIntegrationBase {

    public TestCasesIntegrationMissenseSilentRatio() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String args[] = {"-i", "vcf" //
                , "-classic" //
                , "-useLocalTemplate" //
                , "testHg3765Chr22" //
                , path("missenseSilent.chr22.vcf.gz") //
        };

        SnpEff cmd = new SnpEff(args);
        cmd.setVerbose(verbose);
        cmd.setSupressOutput(!verbose);
        SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();

        snpeff.run();

        double silentRatio = snpeff.getChangeEffectResutStats().getSilentRatio();
        if (verbose) Log.info("Missense / Silent ratio: " + silentRatio);

        assertEquals(1.19, silentRatio, 0.1);
    }
}
