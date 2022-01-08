package org.snpeff.snpEffect.testCases.unity;

import org.snpeff.interval.Markers;
import org.snpeff.interval.tree.IntervalTreeOri;
import org.snpeff.interval.tree.Itree;

/**
 * Test case for interval tree structure
 */
public class TestCasesIntervalTreeOri extends TestCasesIntervalTree {

    @Override
    protected Itree newItree(Markers markers) {
        return new IntervalTreeOri(markers);
    }

}
