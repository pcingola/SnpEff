package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test case for EMBL file parsing (database creation)
 *
 * @author pcingola
 */
public class TestCasesIntegrationGenBank extends TestCasesIntegrationBase {

    int exonToStringVersionOri;

    public TestCasesIntegrationGenBank() {
        super();
    }

    @Test
    public void testCase_multiple_CDS() {
        Log.debug("Test");

        // Build genome
        String genome = "test_NC_031965.1";
        String[] args = {"build", genome};
        SnpEff snpeff = new SnpEff(args);
        snpeff.setVerbose(verbose);
        snpeff.setDebug(debug);
        SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.cmd();
        snpeffBuild.run();

        // Check
        SnpEffCmdProtein sprot = snpeffBuild.getSnpEffCmdProtein();
        assertEquals(3, sprot.getTotalOk(), "Wrong number of matching proteins");
        assertEquals(0, sprot.getTotalErrors(), "Errors while checking sequenced");
    }
}
