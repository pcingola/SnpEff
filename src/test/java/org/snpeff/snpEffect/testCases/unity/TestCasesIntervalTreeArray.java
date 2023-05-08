package org.snpeff.snpEffect.testCases.unity;

import org.snpeff.interval.Markers;
import org.snpeff.interval.tree.IntervalTreeArray;
import org.snpeff.interval.tree.Itree;

/**
 * Test case for interval tree structure
 */
public class TestCasesIntervalTreeArray extends TestCasesIntervalTree {

    @Override
    protected Itree newItree(Markers markers) {
        IntervalTreeArray ita = new IntervalTreeArray(markers);
        ita.setVerbose(verbose);
        ita.setDebug(debug);
        return ita;
    }

}
