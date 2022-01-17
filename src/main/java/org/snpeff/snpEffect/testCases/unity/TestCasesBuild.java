package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGff3;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test case
 */
public class TestCasesBuild extends TestCasesBase {

    @Test
    public void test_01_chromoNamesDoNotMatch_Gff() {
        Log.debug("Test");

        String genome = "testChromoNamesDoNotMatch";
        String gff = path("testChromoNamesDoNotMatch.genes.gff");

        // Expected error message
        String[] expectedErrorLines = {"Error reading file" //
                , "Most Exons do not have sequences!" //
                , "There might be differences in the chromosome names used in the genes file" //
                , "and the chromosme names used in the 'reference sequence' file" //
                , "Chromosome names missing in 'reference sequence' file:\t'1'" //
                , "Chromosome names missing in 'genes' file             :\t'1ZZZ'\n" //
        };

        // Build
        Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
        SnpEffPredictorFactoryGff3 sefGff = new SnpEffPredictorFactoryGff3(config);
        sefGff.setFileName(gff);
        sefGff.setVerbose(verbose);

        // Run: We expect an error
        try {
            Log.setFatalErrorBehabiour(Log.FatalErrorBehabiour.EXCEPTION_QUIET);
            sefGff.create();
        } catch (Throwable t) {
            String errmsg = t.getMessage();
            if (debug) Log.debug("ERROR MESSAGE: '" + errmsg + "'");
            for (String errLine : expectedErrorLines)
                assertTrue(errmsg.contains(errLine), "Line not found: '" + errLine + "'");
            return;
        }
        throw new RuntimeException("Expected error not found!");
    }

    @Test
    public void test_02_chromoNamesDoNotMatch_GffFasta() {
        Log.debug("Test");

        String genome = "testChromoNamesDoNotMatch";
        String gff = path("testChromoNamesDoNotMatch.genes.no_fasta.gff");
        String fasta = path("testChromoNamesDoNotMatch.fa");

        // Expected error message
        String[] expectedErrorLines = {"Error reading file" //
                , "Most Exons do not have sequences!" //
                , "There might be differences in the chromosome names used in the genes file" //
                , "and the chromosme names used in the 'reference sequence' file" //
                , "Please check that chromosome names in both files match" //
                , "Chromosome names missing in 'reference sequence' file:\t'1'" //
                , "Chromosome names missing in 'genes' file             :\t'1ZZZ'" //"
        };

        // Build
        Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
        SnpEffPredictorFactoryGff3 sefGff = new SnpEffPredictorFactoryGff3(config);
        sefGff.setFileName(gff);
        sefGff.setFastaFile(fasta);
        sefGff.setVerbose(verbose);

        // Run: We expect an error
        try {
            Log.setFatalErrorBehabiour(Log.FatalErrorBehabiour.EXCEPTION_QUIET);
            sefGff.create();
        } catch (Throwable t) {
            String errmsg = t.getMessage();
            if (debug) Log.debug("ERROR MESSAGE: '" + errmsg + "'");
            for (String errLine : expectedErrorLines)
                assertTrue(errmsg.contains(errLine), "Line not found: '" + errLine + "'");
            return;
        }
        throw new RuntimeException("Expected error not found!");
    }

}
