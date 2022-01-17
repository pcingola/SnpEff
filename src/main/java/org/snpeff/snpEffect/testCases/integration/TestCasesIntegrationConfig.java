package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case
 */
public class TestCasesIntegrationConfig extends TestCasesIntegrationBase {

    /**
     * Check that config file can be overriden by command line options
     */
    @Test
    public void test_01_ConfigOverride() {
        Log.debug("Test");

        // Create command
        String repo = "http://nonsense.url/test/zzz";
        String args[] = { //
                "-configOption" //
                , Config.KEY_DATABASE_REPOSITORY + "=" + repo //
                , "testHg3775Chr22" //
                , path("test_ann_01.vcf") //
        };

        // Create command and run
        SnpEff cmd = new SnpEff(args);
        cmd.setSupressOutput(!verbose);
        cmd.setVerbose(verbose);
        cmd.setDebug(debug);
        cmd.run();

        // Check that config option really changed
        if (verbose) Log.info("Repository: " + cmd.getConfig().getDatabaseRepository());
        assertEquals(repo, cmd.getConfig().getDatabaseRepository());
    }

}
