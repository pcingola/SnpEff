package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.snpEffect.commandLine.SnpEffCmdPdb;
import org.snpeff.util.Log;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

    /**
     * Interaction within protein using Uniprot entry 'P18754'
     */
    @Test
    public void test_03_build_alphafold() {
        Log.debug("Test");
        verbose = debug = true;

        // Command line arguments
        String genome = "testHg19Pdb";
        String pdbDir = path("pdb");
        String idmap = path("pdb") + "/idMap_uniprotId_refSeqId.txt";
        String args[] = { "-pdbDir", pdbDir, "-idmap", idmap, genome };

        // Create command
        SnpEffCmdPdb cmd = new SnpEffCmdPdb();
        cmd.setVerbose(verbose);
        cmd.setDebug(debug);
        cmd.parseArgs(args);
        cmd.run(true);
        List<DistanceResult> distanceResults = cmd.getDistanceResults();

        // Check results for a specific interaction
        boolean ok = false;
        for (DistanceResult dr : distanceResults) {
            ok |= dr.pdbId.equals("1A12") && dr.aaPos1 == 24 && dr.aaPos2 == 135;
            if (verbose) Log.debug("INTERACTION:\t" + dr);
        }

        assertTrue(ok, "Interaction not found!");
    }


}
