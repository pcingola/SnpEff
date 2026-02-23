package org.snpeff.snpEffect.testCases.unity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.snpeff.genBank.GenBank;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for GenBank file parsing
 */
public class TestCasesGenBank {

    /**
     * Test that ORIGIN sequences with position numbers >= 100,000,000 (no leading space) are parsed correctly.
     * GenBank ORIGIN lines are normally formatted with right-justified position numbers in a 9-char column,
     * so positions < 100M start with a space. Positions >= 100M fill the column and the line no longer
     * starts with a space, which previously caused the parser to treat them as new field names.
     */
    @Test
    public void testOriginLargePositions() throws IOException {
        // Build a minimal GenBank file where the last ORIGIN line has a position >= 100,000,000
        // (no leading space), simulating a sequence > 100M bp.
        String seq1 = "aaccggttaa ccggttaacc ggttaaccgg ttaaccggtt aaccggttaa ccggttaacc"; // 60 bases
        String seq2 = "ttggccaatt ggccaattgg"; // 20 bases
        String expectedSequence = "aaccggttaaccggttaaccggttaaccggttaaccggttaaccggttaaccggttaaccttggccaattggccaattgg";

        StringBuilder sb = new StringBuilder();
        sb.append("LOCUS       TEST_SEQ             80 bp    DNA     linear   UNK\n");
        sb.append("DEFINITION  Test sequence for large ORIGIN positions.\n");
        sb.append("ACCESSION   TEST001\n");
        sb.append("VERSION     TEST001.1\n");
        sb.append("FEATURES             Location/Qualifiers\n");
        sb.append("     source          1..80\n");
        sb.append("                     /organism=\"Test\"\n");
        sb.append("ORIGIN\n");
        sb.append("        1 " + seq1 + "\n");        // Normal line (starts with spaces)
        sb.append("100000001 " + seq2 + "\n");          // Position >= 100M, no leading space
        sb.append("//\n");

        File tmpFile = File.createTempFile("testGenBank_largeOrigin_", ".gb");
        tmpFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmpFile)) {
            fw.write(sb.toString());
        }

        GenBank gb = new GenBank(tmpFile.getAbsolutePath());
        assertEquals(expectedSequence, gb.getSequence(), "Sequence truncated at ORIGIN position >= 100,000,000");
    }
}
