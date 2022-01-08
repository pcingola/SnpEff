package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.motif.Jaspar;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for Jaspar parsing
 *
 * @author pcingola
 */
public class TestCasesJaspar extends TestCasesBase {

    public TestCasesJaspar() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        Jaspar jaspar = new Jaspar();
        jaspar.load(path("jaspar_old.txt.gz"));

        String actual = jaspar.getPwm("MA0001.1").toString().replace('\t', ' ');

        // Trim each line
        StringBuilder sb = new StringBuilder();
        for (String s : actual.split("\n"))
            sb.append(s.trim() + "\n");
        actual = sb.toString();

        // Compare to expected
        String expected = "Name: AGL3 Id: MA0001.1\n" //
                + "Counts:\n" //
                + "A          0           3          79          40          66          48          65          11          65           0\n" //
                + "C         94          75           4           3           1           2           5           2           3           3\n" //
                + "G          1           0           3           4           1           0           5           3          28          88\n" //
                + "T          2          19          11          50          29          47          22          81           1           6\n" //
                + "Max:          C           C           A           T           A           A           A           T           A           G\n" //
                + "\n" //
                + "Weights:\n" //
                + "A       0.01        0.03        0.18        0.14        0.17        0.15        0.17        0.06        0.17        0.01\n" //
                + "C       0.10        0.11        0.03        0.02        0.01        0.02        0.03        0.02        0.02        0.02\n" //
                + "G       0.01        0.01        0.02        0.02        0.01        0.01        0.03        0.02        0.07        0.05\n" //
                + "T       0.02        0.08        0.06        0.13        0.10        0.12        0.08        0.14        0.01        0.04\n" //
                + "Max:          C           C           A           A           A           A           A           T           A           G\n" //
                ;

        assertEquals(expected, actual);
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        Jaspar jaspar = new Jaspar();
        jaspar.load(path("jaspar_2013.txt.gz"));

        String actual = jaspar.getPwm("MA0001.1").toString().replace('\t', ' ');

        // Trim each line
        StringBuilder sb = new StringBuilder();
        for (String s : actual.split("\n"))
            sb.append(s.trim() + "\n");
        actual = sb.toString();

        // Compare to expected
        String expected = "Name: SEP4 Id: MA0001.1\n" //
                + "Counts:\n" //
                + "A          0           3          79          40          66          48          65          11          65           0\n" //
                + "C         94          75           4           3           1           2           5           2           3           3\n" //
                + "G          1           0           3           4           1           0           5           3          28          88\n" //
                + "T          2          19          11          50          29          47          22          81           1           6\n" //
                + "Max:          C           C           A           T           A           A           A           T           A           G\n" //
                + "\n" //
                + "Weights:\n" //
                + "A       0.01        0.03        0.18        0.14        0.17        0.15        0.17        0.06        0.17        0.01\n" //
                + "C       0.10        0.11        0.03        0.02        0.01        0.02        0.03        0.02        0.02        0.02\n" //
                + "G       0.01        0.01        0.02        0.02        0.01        0.01        0.03        0.02        0.07        0.05\n" //
                + "T       0.02        0.08        0.06        0.13        0.10        0.12        0.08        0.14        0.01        0.04\n" //
                + "Max:          C           C           A           A           A           A           A           T           A           G\n" //
                ;

        assertEquals(expected, actual);
    }

}
