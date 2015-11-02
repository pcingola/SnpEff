package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalTreeOri;
import ca.mcgill.mcb.pcingola.interval.tree.Itree;

/**
 * Test case for interval tree structure
 */
public class TestCasesIntervalTreeOri extends TestCasesIntervalTree {

	@Override
	protected Itree newItree(Markers markers) {
		return new IntervalTreeOri(markers);
	}

}
