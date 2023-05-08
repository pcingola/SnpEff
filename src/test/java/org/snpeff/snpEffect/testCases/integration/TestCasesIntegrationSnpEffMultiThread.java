package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Invoke multi thread integration test
 * <p>
 * WARNING: JUnit doesn't seem to work if you use multi-threading....
 *
 * @author pcingola
 */
public class TestCasesIntegrationSnpEffMultiThread extends IntegrationTest {

    @Test
    public void test_01_multi_thread() {
        Log.debug("Test");

        // FIXME: Mutithreading mode is broken: Move to Java streams + lambdas
        if (Math.random() < 2.0) Log.debug("Mutithreading mode is broken: Move to Java streams + lambdas");

        //		String expectedOutputFile = path("test.chr1.1line.out.classic.vcf");
        //		String args[] = { "eff", "-t", "-classic", "-noHgvs", "-noStats", "-noLog", "-noLof", "testHg3763Chr1", path("test.chr1.1line.vcf") };
        //		SnpEff snpeff = new SnpEff(args);
        //		snpeff.setVerbose(verbose);
        //		command(snpeff, expectedOutputFile);
    }

}
