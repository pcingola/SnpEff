package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdProtein;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test COVID19 build
 *
 * @author pcingola
 */
public class TestCasesIntegrationCovid19 extends TestCasesIntegrationBase {

    public TestCasesIntegrationCovid19() {
        super();
        testsDir = "tests/integration/covid19/";
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String genome = "test_NC_045512_01";
        SnpEffCmdBuild buildCmd = buildGetBuildCmd(genome);

        // Make sure all proteins are OK
        SnpEffCmdProtein protCmd = buildCmd.getSnpEffCmdProtein();
        assertEquals(5, protCmd.getTotalOk());
        assertEquals(0, protCmd.getTotalErrors());
        assertEquals(0, protCmd.getTotalWarnings());
    }
}
