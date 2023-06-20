package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for cytobands
 */
public class TestCasesCytoBands {

    boolean debug = false;
    boolean verbose = false || debug;

    /**
     * Test that we can load cytobands
     */
    @Test
    public void test01() {
        Log.debug("Test");

        String genomeVer = "testHg19Chr22";
        Config config = new Config(genomeVer);
        config.setVerbose(verbose);
        config.setDebug(debug);

        Genome genome = config.getGenome();
        CytoBands cytoBands = genome.getCytoBands();
        assertFalse(cytoBands.isEmpty(), "No cytobands found!");
    }

    /**
     * Query cytobands
     */
    @Test
    public void test02() {
        Log.debug("Test");

        String genomeVer = "testHg19Chr22";
        Config config = new Config(genomeVer);
        config.setVerbose(verbose);
        config.setDebug(debug);

        Genome genome = config.getGenome();
        CytoBands cytoBands = genome.getCytoBands();

        Chromosome chr = genome.getOrCreateChromosome("22");
        int pos = 4800000;
        Marker m = new Marker(chr, pos, pos);
        Markers cbs = cytoBands.query(m);

        if (verbose) {
            System.out.println("Resutls: ");
            for (Marker cb : cbs)
                System.out.println(cb);
        }

        assertTrue(cbs.size() == 1, "Should find one cytoband for query: " + m);
        assertEquals("p12", cbs.get(0).getId(), "Expected cytoband 'p12' not found: " + m);

    }

}
